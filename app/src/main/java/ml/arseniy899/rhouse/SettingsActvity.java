package ml.arseniy899.rhouse;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SettingsActvity extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
	String TAG = "SettingAct";
	private ZXingScannerView mScannerView;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		if(getIntent().getBooleanExtra("force-settings",false))
			findViewById(R.id.topBar).setVisibility(View.GONE);
		else
		{
			findViewById(R.id.exit).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					finish();
				}
			});
			findViewById(R.id.qrScan).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mScannerView = new ZXingScannerView(getBaseContext());   // Programmatically initialize the scanner view
					setContentView(mScannerView);
				}
			});
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
		mScannerView.startCamera();          // Start camera on resume
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();           // Stop camera on pause
	}
	
	@Override
	public void handleResult(Result rawResult) {
		// Do something with the result here
		Log.v(TAG, rawResult.getText()); // Prints scan results
		Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
		
		// If you would like to resume scanning, call this method below:
//		mScannerView.resumeCameraPreview(this);
	}
}
