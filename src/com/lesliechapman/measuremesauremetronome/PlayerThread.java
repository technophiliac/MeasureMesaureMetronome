package com.lesliechapman.measuremesauremetronome;

import java.util.concurrent.locks.LockSupport;

public class PlayerThread extends Thread {

	int _running = 0;
	MainActivity _ma;
	int _bpm = 0;
	int _sig = 4;
	
	public PlayerThread(MainActivity ma){
		_ma = ma;
	}

	public void run() {

		while (_running >= 0) {
			if (_running == 1){
				if (_ma._meterCount == 1) {
					_ma._sndPool.play(_ma._sndHigh, 1f, 1f, 1, 0, 1f);
					// Log.d("asd", "beep");
					LockSupport.parkNanos(((240000 / _bpm) / 4) * 1000000); // 300bpm
				} else {
					_ma._sndPool.play(_ma._sndLow, 1f, 1f, 1, 0, 1f);
					// Log.d("asd", "beep");
					LockSupport.parkNanos(((240000 / _bpm) / 4) * 1000000); // 300bpm
				}
	
				_ma.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						_ma.setMeasureText(_ma._measureCount);
						_ma.setMeterText(_ma._meterCount);
					}
				});		
				
	
					if (_ma._meterCount == _sig) {
						_ma._measureCount++;
						_ma._meterCount = 1;
					} else {
						_ma._meterCount++;
					}
			} else if (_running == 0){
				//just pause
			}
		} 
	}
	
	public void setBPM(int bpm){
		_bpm = bpm;
	}
	
	public void setRunning(int run){
		_running = run;
	}

	public void setSignature(int sig) {
		_sig = sig;		
	}
}

