package ml.arseniy899.rhouse.HubManage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import ml.arseniy899.rhouse.BoundHubsListActivity;
import ml.arseniy899.rhouse.Hub;
import ml.arseniy899.rhouse.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

public class HubActivity extends AppCompatActivity
{
	Hub selectedHub;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hub_manage);
//		HubUnitsFragment.context = this;
		if(getIntent().getSerializableExtra("hub") != null)
			selectedHub = (Hub) getIntent().getSerializableExtra("hub");
		
		if(selectedHub == null)
			goToHubChange();
		
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);
		
		TextView hubName = findViewById(R.id.hubName);
		hubName.setText(selectedHub.name);
		findViewById(R.id.change).setOnClickListener(view -> hubName.callOnClick());
		hubName.setOnClickListener(view -> {
			MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
			builder.setTitle(this.getString(R.string.hub_change_title));
			builder.setMessage(this.getString(R.string.hub_change_confirm_text));
			
			builder.setPositiveButton(this.getString(R.string.yes), (dialog, which) ->
			{
				goToHubChange();
			});
			
			builder.setNegativeButton(this.getString(R.string.close), (dialog, which) ->
			{
				dialog.dismiss();
			});
			
			builder.show();
		});
		
	}
	void goToHubChange()
	{
		Intent intent = new Intent(this, BoundHubsListActivity.class);
		intent.putExtra("hub.change", true);
		startActivity(intent);
		finish();
	}
}