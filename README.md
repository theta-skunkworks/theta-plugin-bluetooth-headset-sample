# Bluetooth Audio Sample for RICOH THETA

This is a sample program that allows you to control THETA from a Bluetooth Audio device.<br>
[Don't forget to update the firmware!](https://api.ricoh/news/2020/06/17/ricoh-theta-api-update-z1-1.50.1-v-3.40.1/)

より詳しい日本語の説明は[こちら](https://qiita.com/mShiiina/items/4b9f74625deeb43763e9)。<br>
[Click here](https://qiita.com/mShiiina/items/4b9f74625deeb43763e9) for a more detailed explanation in Japanese.

[![](http://img.youtube.com/vi/wF3f3BWbe4M/0.jpg)](http://www.youtube.com/watch?v=wF3f3BWbe4M "")


<2020/07/30 Update:apk Ver 1.1.0>
- Supports two languages, Japanese and English. The last language setting used is retained when the plugin is closed.
- Improved sound quality by using Amazon Polly to generate synthetic speech.

[![](https://img.youtube.com/vi/NN33SM5_SJQ/0.jpg)](https://www.youtube.com/watch?v=NN33SM5_SJQ)

<2020/11/16 Update:apk Ver 1.2.0>
- Added Bluetooth related state saving and recovery processing. 
- Added volume saving and restoring processing.
- Removed NanoHTTPD import.
- Changed the plugin name.
- A license check was conducted.

## Usage

### Pairing

1. ~~If you have enabled the remote control function of THETA using THETA's pre-installed plug-in "Remote Control", please disable this function.~~ <br>(This is an unnecessary procedure in apk Ver 1.2.0 or later.)
2. When using the audio device used on the smartphone, temporarily turn off Bluetooth on the smartphone so that the audio device is not connected to the smartphone.
3. If you need to operate the audio device when pairing the audio device (eg Anker Icon Mini, Apple AirPods series), follow the manual of the audio device.
4. Launch the plug-in to automatically pair with the audio device

When the plug-in is started, the search operation for pairing will be performed for up to 12 seconds. <br>
When pairing is completed, the following display will appear depending on the model. <br>

- THETA V: WLAN LED turns white
- THETA Z1: OLED display becomes "CONNECTED"

If the pairing fails, the LED will turn blue. Please close the plug-in and try again.
If pairing does not succeed, restart THETA (power off by pressing and holding the power button → power on) and try again. This is also a rare occurrence when pairing a smartphone with an audio device.

After successful pairing, you can operate THETA from your audio device.

### Audio device operation

The behavior of THETA corresponding to the operation of audio equipment is as follows.

| Audio device operation | THETA operation |
|----|----|
| Play/Pause | Shooting |
| Next track | Exposure +1 step(+0.3EV)|
| Previous track | Exposure -1 step(-0.3EV)  |
| Volume up | Volume +1 step |
| Volume down | Volume -1 step |

### Button operation on THETA

|THETA Button|THETA operation|
|----|----|
|Shutter Short press|Shooting|
|WLAN Short press|Volume +1 step|
|Mode Short press|Volume -1 step|
|WLAN Long press|Language switching|


## Bluetooth audio device confirmed to work

|audio device|Play/Pause<br>（Shooting）|Next track<br>（Exposure +1step）|Next track<br>（Exposure -1step）|Volume +1step|Volume -1step|
|---|---|---|---|---|---|
|Anker<br>Icon Mini<br>(speaker)|○|○|○|○|○|
|Anker<br>Liberty Air<br>(earphone)|○|△<br>Difficult to operate|△<br>Difficult to operate|N/A|N/A|
|Anker<br>Zolo Liberty<br>(earphone)|○|×<br>unstable|×<br>unstable|N/A|N/A|
|JPRiDE<br>TWS-520<br>(earphone)|○|○|○|N/A|N/A|
|VANKYO<br>X100<br>(earphone)|○|○|○|△<br>Difficult to operate|△<br>Difficult to operate|
|Apple<br>Air Pods Pro<br>(earphone)|○|○|○|N/A|N/A|

* N/A has no function on the earphone side
* [The JPRiDE TWS-520 manual](http://7654ed4e6d78812.main.jp/jprmanual/TWS520_manual_ja.pdf) explains that the volume can be adjusted with a single tap, but since it could not be recognized by smartphones (iOS or Android), it was treated as N/A.
* With Anker Liberty Air, the earphone operation itself was difficult because of the touch recognition position and touch sensitivity.
* The volume control of VANKYO X100 is a long tap. Since the key code is sent continuously during tapping, it is difficult to set the target volume.
* When pairing an Apple Air Pods Pro with an Android OS device, you need to press the button on the case to enter the pairing state. Please be careful.

### Restrictions

Launching this plug-in switches the capture mode to still image capture mode.<br>
Even when the plug-in ends, the capture mode remains in the still image capture mode.

## Development Environment

### Camera
* RICOH THETA V Firmware ver.3.40.1 and above
* RICOH THETA Z1 Firmware ver.1.50.1 and above

### SDK/Library
* RICOH THETA Plug-in SDK ver.2.1.0

### Development Software
* Android Studio ver.3.5.3
* gradle ver.5.1.1

## About voice data licensing 

Since apk version V1.1.0, voice data has been created with [Amazon Polly](https://aws.amazon.com/jp/polly/) for both English and Japanese. License notation is unnecessary.

The audio data used by the old apk (Japanese only) was created at the following site.

[https://note.cman.jp/other/voice/](https://note.cman.jp/other/voice/)

For the sound created at the above site, the following license notation is necessary.

[![Creative Commons Attribution (CC-BY) 3.0 license](http://mirrors.creativecommons.org/presskit/buttons/80x15/png/by.png)](https://creativecommons.org/licenses/by/3.0/deed.ja)

HTS Voice "Mei(Normal)" Copyright (c) 2009-2013 Nagoya Institute of Technology

For details of license data of audio data, please refer to [http://www.mmdagent.jp/](http://www.mmdagent.jp/).


## License

```
Copyright 2018 Ricoh Company, Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contact
![Contact](img/contact.png)

