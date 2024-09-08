import 'dart:convert';

import 'package:alarm/alarm.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_foreground_service/home_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Alarm.init();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const HomeScreen(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final channel = const MethodChannel('flutter_foreground_service');
  final d = const EventChannel('');
  final title = TextEditingController();
  final text = TextEditingController();

  bool serviceState = false;
  String location = '';

  Future sendNotificationData() async {
    try {
      final res = await channel.invokeMethod(
          'getNotificationData', {"title": title.text, "text": text.text});
      print(jsonDecode(res.toString()));
    } catch (e) {
      print(e);
      rethrow;
    }
  }

  Future<void> startForegroundService() async {
    try {
      final res = await channel.invokeMethod<String>('startAndStopService');

      await Future.delayed(const Duration(seconds: 2)).whenComplete(() {
        setState(() => location = res!);
      });
    } catch (e) {
      print(e);
      rethrow;
    }
  }

  Future<void> share() async {
    try {
      await channel.invokeMethod<String>('shareLocation');
    } catch (e) {
      print(e);
      rethrow;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Flutter foreground service'),
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: Column(
          children: [
            const SizedBox(height: 50),
            TextField(
              controller: title,
              decoration: const InputDecoration(hintText: 'Title'),
            ),
            TextField(
              controller: text,
              decoration: const InputDecoration(hintText: 'Text'),
            ),
            const SizedBox(height: 16),
            Text(
              location,
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            TextButton(
                onPressed: () async {
                  await startForegroundService();
                },
                child: Text(
                    '${serviceState ? 'Stop' : 'Start'} foreground service')),
            TextButton(
                onPressed: () async {
                  //await sendNotificationData();
                  await share();
                },
                child: Text('Set Text'))
          ],
        ),
      ),
    );
  }
}
