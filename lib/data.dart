import 'dart:io';

import 'package:alarm/alarm.dart';
import 'package:flutter/services.dart';
import 'package:flutter_foreground_service/location_model.dart';
import 'package:http/http.dart';

const channel = MethodChannel('flutter_foreground_service');

final alarmSettings = AlarmSettings(
  id: 42,
  dateTime: DateTime.now(),
  assetAudioPath: 'assets/alarm.mp3',
  loopAudio: true,
  vibrate: true,
  volume: 0.8,
  fadeDuration: 3.0,
  notificationTitle: 'This is the title',
  notificationBody: 'This is the body',
  enableNotificationOnKill: Platform.isIOS,
);

//await Alarm.set(alarmSettings: alarmSettings)

Future<bool?> startForegroundService() async => await channel
    .invokeMethod<bool>('startAndStopService', {'title': "", 'text': ''});

// Future<LocationResponseModel> getActualLocation() async {
//   var url =
//       "http://api.openweathermap.org/geo/1.0/reverse?lat={lat}&lon={lon}&limit={limit}&appid={API key}";
//   final response = await get(Uri.parse(url));

// }
