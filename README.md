# EEG_Bluetooth_Android
EEG measuring via Raspberry Pi Zero W, Visualizing on Android Application

1
Flash app.py to Raspberry Pi, which get EEG value from ADC. This program conducts FFT(Fast Fourier Transform) and classify by bands, sum the value and send to Android Application.

2
In 'test' folder, android project is included. it gets each band data from 1, and chart on circular graphs.