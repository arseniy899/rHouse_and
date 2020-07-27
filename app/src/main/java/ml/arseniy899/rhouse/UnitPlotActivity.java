package ml.arseniy899.rhouse;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.core.util.Pair;

import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;

import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UnitPlotActivity extends AppCompatActivity
{
	Hub.Unit unit;
	WebView plotVW;
	String dateToPlotStart;
	Long dateToPlotStartMs;
	String dateToPlotEnd;
	Long dateToPlotEndMs;
	Calendar calendarStartDef;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(getIntent().getSerializableExtra("unit") != null)
			unit = (Hub.Unit) getIntent().getSerializableExtra("unit");
		if(unit == null)
			finish();
		setContentView(R.layout.activity_unit_plot);
		findViewById(R.id.back).setOnClickListener(v -> finish());
		((TextView)findViewById(R.id.title)).setText(unit.name);
		plotVW = findViewById(R.id.plotWV);
		plotVW.loadUrl("file:///android_asset/unit_values_plot.html");
		plotVW.setWebChromeClient(new WebChromeClient());
		plotVW.getSettings().setJavaScriptEnabled(true);
		calendarStartDef = Calendar.getInstance();
		Date startDate = new Date();
		dateToPlotEnd  = Common.dateFormat.format(startDate);
//		calendar.setTime(myDate);
		calendarStartDef.add(Calendar.DAY_OF_YEAR, -14);
		dateToPlotStartMs = calendarStartDef.getTimeInMillis();
		dateToPlotEndMs = MaterialDatePicker.todayInUtcMilliseconds();
		dateToPlotStart= Common.dateFormat.format(dateToPlotStartMs );
		updatePlot();
		
		findViewById(R.id.dateRange).setOnClickListener(v -> showDateRangePicker());
	}
	
	private void showDateRangePicker()
	{
		MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.dateRangePicker();
		Pair<Long, Long> preselect = new Pair<>(dateToPlotStartMs,dateToPlotEndMs);
		CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.set(2010, 1, 1);
		
		constraintsBuilder.setStart(calendarStart.getTimeInMillis());
		constraintsBuilder.setEnd(MaterialDatePicker.todayInUtcMilliseconds());
		builder.setCalendarConstraints(constraintsBuilder.build());
		List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
		validators.add(DateValidatorPointForward.from(calendarStart.getTimeInMillis()));
		validators.add(DateValidatorPointForward.now());
		
		builder.setSelection(preselect);
		MaterialDatePicker picker = builder.build();
		picker.show(getSupportFragmentManager(),"range");
		picker.addOnPositiveButtonClickListener(selection ->
		{
			Pair<Long, Long> selected = (Pair<Long, Long>) selection;
			dateToPlotStartMs =selected.first;
			dateToPlotEndMs = selected.second;
			dateToPlotStart = Common.dateFormat.format(selected.first);
			dateToPlotEnd = Common.dateFormat.format(selected.second);
			updatePlot();
		});
		
	}
	
	private void updatePlot()
	{
		Map<String, String> map = new HashMap<>();
		map.put("id",unit.id+"");
		map.put("from",dateToPlotStart);
		map.put("to",dateToPlotEnd);
		WebRequest.request(this, "/units.get.values.php", map, new WebCallBackInterface()
		{
			
			@Override
			public void onSuccess (JsonObject result)
			{
				JsonObject data = result.getAsJsonObject("data");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				{
					plotVW.evaluateJavascript("setValues('" + data.toString().replace("\\","\\\\") + "');", new ValueCallback<String>()
					{
						@Override
						public void onReceiveValue(String value)
						{
							Log.i("Unit/Plot/WW/SetVal","Recv value:" + value);
						}
					});
				}
			}
		}, this.getString(R.string.loading));
	}
	
	
	
}
