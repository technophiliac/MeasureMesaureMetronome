package com.lesliechapman.measuremesauremetronome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	public int _measureCount = 0;
	public int _meterCount = 1;
	public TextView _measure;
	public TextView _largeMeter;
	public SoundPool _sndPool;
	public int _sndHigh;
	public int _sndLow;
	private TextView _bpm;
	private PlayerThread _pt;
	private View _transitionView;
	private View _transitionRedView;
	private View _transitionGreenView;
	private SeekBar _timeSignatureSeekbar;
	private TextView _signature;
	private int _sig = 4;
	private boolean _isStarted = false;
	private SeekBar _bpmSeekBar;
	private ToggleButton _toggleBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		_measure = (TextView) findViewById(R.id.measure);
		_largeMeter = (TextView) findViewById(R.id.largeMeter);
		_bpm = (TextView) findViewById(R.id.bpm);
		_transitionGreenView = (View) findViewById(R.id.myTransitionGreenView);
		_transitionRedView = (View) findViewById(R.id.myTransitionRedView);
		_transitionRedView.setVisibility(View.INVISIBLE);
		_transitionView = (View) findViewById(R.id.myTransitionView);
		_transitionView.setVisibility(View.INVISIBLE);
		_timeSignatureSeekbar = (SeekBar) findViewById(R.id.timeSignatureSeekBar);
		_signature = (TextView) findViewById(R.id.signature);
		_toggleBtn = (ToggleButton) findViewById(R.id.startStop);
		_bpmSeekBar = (SeekBar) findViewById(R.id.bpmSeekBar);
		_timeSignatureSeekbar.setProgress(_sig - 1);
		_bpmSeekBar.setProgress(80);
		_bpm.setText(Integer.toString(80 + 40));
		

		_sndPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
		_sndHigh = _sndPool.load(this, R.raw.high, 1);
		_sndLow = _sndPool.load(this, R.raw.low, 1);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		_pt = new PlayerThread(this);
		_pt.setPriority(Thread.MAX_PRIORITY);

		_measureCount = 0;
		_meterCount = 1;

		setMeasureText(_measureCount);
		setMeterText(_meterCount);

		_timeSignatureSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				_signature.setText(Integer.toString(progress + 1));
				_sig = progress + 1;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		
		_bpmSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				_bpm.setText(Integer.toString(progress + 40));
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		
		_toggleBtn.setOnClickListener(new OnClickListener()
		{
		    @Override
		    public void onClick(View v)
		    {
		        startStop(v);
		    }
		});


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void reset() {
		_sndPool.stop(_sndHigh);
		_sndPool.stop(_sndLow);
		_measureCount = 0;
		_meterCount = 1;
		setMeasureText(_measureCount);
		setMeterText(_meterCount);
	}

	/** Called when the user clicks the start button */
	public boolean start(View view) {
		reset();

		String bpmString = _bpm.getText().toString();
		try {
			final int bpm = Integer.parseInt(bpmString);
			if (bpm <= 0) {
				// alert that it must be greater than 0
				showAlert("BPM too low");
			} else if (bpm >= 201) {
				// alert that it must be less than 201
				showAlert("BPM too high");
			} else {

				_pt.setBPM(bpm);
				_pt.setSignature(_sig);
				_pt.setRunning(1);
				if (!_pt.isAlive()) {
					_pt.start();					
				}
				return true;
			}
		} catch (NumberFormatException e) {
			showAlert("You must enter BPM");

		}
		return false;

	}

	private void showAlert(String s) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title		

		alertDialogBuilder.setTitle(s);

		// set dialog message
		alertDialogBuilder
				.setMessage("Please choose a value between 1 and 200.")
				.setCancelable(false)
				/*
				 * .setPositiveButton("Yes", new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int id) { // if this button
				 * is clicked, close // current activity
				 * MainActivity.this.finish(); } })
				 */
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}
	
	

	/** Called when the user clicks the start/stop button */
	public void startStop(View view) {
		if (_isStarted){
			_isStarted = false;
			// finish();
			_pt.setRunning(0);
			
		} else {			
			_isStarted = start(view);
		}
		_bpmSeekBar.setEnabled(!_isStarted);
		_timeSignatureSeekbar.setEnabled(!_isStarted);
	}

	@Override
	public void onDestroy() {
		_pt.setRunning(-1);
		_sndPool.release();
		super.onDestroy();
	}

	public void setMeasureText(int i) {
		String s = Integer.toString(i);
		_measure.setText(s);
	}

	public void setMeterText(int i) {
		String s = Integer.toString(i);
		if (_measureCount == 0) {
			_transitionView.setVisibility(View.INVISIBLE);
			_transitionRedView.setVisibility(View.INVISIBLE);
			_transitionGreenView.setVisibility(View.VISIBLE);

			TransitionDrawable transition = (TransitionDrawable) _transitionGreenView
					.getBackground();
			transition.startTransition(100);
			
		} else {

			if (i == 1) {
				_transitionView.setVisibility(View.INVISIBLE);
				_transitionGreenView.setVisibility(View.INVISIBLE);
				_transitionRedView.setVisibility(View.VISIBLE);

				
				
			} else {

				_transitionRedView.setVisibility(View.INVISIBLE);
				_transitionGreenView.setVisibility(View.INVISIBLE);
				_transitionView.setVisibility(View.VISIBLE);

				TransitionDrawable transition = (TransitionDrawable) _transitionView
						.getBackground();
				transition.startTransition(200);

			}
		}

		_largeMeter.setText(s);

	}

}
