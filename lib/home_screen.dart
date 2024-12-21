import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_foreground_service/data.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final eventChannel = const EventChannel('timer');

  bool isTracking = false;
  Stream<String> streamTimeFromNative() {
    return eventChannel.receiveBroadcastStream().map((event) {
      print(event);
      return event.toString();
    });
  }

  void handleForegroundService() async {
    final res = await startForegroundService();
    if (res == null) return;
    Future.delayed(Duration(seconds: 5))
        .whenComplete(() => setState(() => isTracking = res));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      floatingActionButton: FloatingActionButton.extended(
          backgroundColor: isTracking
              ? Colors.red
              : Theme.of(context).floatingActionButtonTheme.backgroundColor,
          onPressed: () => handleForegroundService(),
          label: Text('${isTracking ? "Stop" : 'Start'} Tracking')),
      appBar: AppBar(
        title: const Text('Location Tracker'),
        actions: [
          IconButton(onPressed: () {}, icon: const Icon(Icons.history))
        ],
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          if (isTracking)
            StreamBuilder<String>(
              stream: streamTimeFromNative(),
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  final f = snapshot.data!.split(' ');
                  return SelectableText(
                    'lat:${f.first} lon:${f.last}',
                    style: Theme.of(context).textTheme.displayMedium,
                  );
                } else {
                  return const CircularProgressIndicator();
                }
              },
            ),
        ],
      ),
    );
  }
}
