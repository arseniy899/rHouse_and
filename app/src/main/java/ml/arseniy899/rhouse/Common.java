package ml.arseniy899.rhouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;

import ml.arseniy899.rhouse.HubManage.HubActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Common
{
	public static String API_URL = "https://r-ho.ml/portal/api";
	public static Hub selectedHub;
	public static ArrayList<Hub> boundHubs = new ArrayList<>();
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
	
	public static View getRootView(Activity activity)
	{
		return activity.getWindow().getDecorView().findViewById(android.R.id.content);
	}
	@ColorInt
	public static int getContrastColor(@ColorInt int color) {
		double a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
		
		int d;
		if (a < 0.5) {
			d = 0; // bright colors - black font
		} else {
			d = 255; // dark colors - white font
		}
		
		return Color.rgb(d, d, d);
	}
	
	public static void startUp(Context context)
	{
		setupFirebase(context);
	}
	public static void setupFirebase(Context context)
	
	{
		FirebaseApp.initializeApp(context);
		FirebaseMessaging.getInstance().setAutoInitEnabled(true);
		subscribeTopic("public");
	}
	public static void subscribeTopic(String topic)
	{
		FirebaseMessaging.getInstance().subscribeToTopic(topic)
				.addOnCompleteListener(task -> Log.i("FCM/subs", task+""));
	}
	
	static void parsePushIntentForSH(Activity activity, ArrayList<Hub> hubs)
	{
		if(hubs == null || hubs.size() == 0)
			hubs = MemoryWork.loadObjects(activity,Hub.class,"boundHubs");
		Hub hub = null;
		int hubID = activity.getIntent().getIntExtra("hubID", 0);
		if (hubID != 0)
		{
			for (Hub hubIter : hubs)
			{
				if (hubIter.hubID == hubID)
				{
					hub = hubIter;
					break;
				}
			}
		}
			if( activity.getIntent().getStringExtra("push.title") != null)
			{
				MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
				builder.setTitle(activity.getIntent().getStringExtra("push.title"));
				builder.setMessage(activity.getIntent().getStringExtra("push.text"));
				
				if (hub != null)
				{
					Hub finalHub = hub;
					builder.setPositiveButton(activity.getString(R.string.goto_hub), (dialog, which) ->
					{
						finalHub.selectHub(activity, () ->
						{
							Intent intent = new Intent(activity, HubActivity.class);
							intent.putExtra("hub", finalHub);
							activity.startActivity(intent);
							activity.finish();
						}, true);
						dialog.dismiss();
					});
				}
				builder.setNegativeButton(activity.getString(R.string.close), (dialog, which) ->
					{
						dialog.dismiss();
						activity.finish();
					});
				
				builder.show();
			}
			else if(hub != null)
			{
				Hub finalHub1 = hub;
				hub.selectHub(activity, () ->
				{
					Intent intent = new Intent(activity, HubActivity.class);
					intent.putExtra("hub", finalHub1);
					activity.startActivity(intent);
				}, true);
			}
	}
}

