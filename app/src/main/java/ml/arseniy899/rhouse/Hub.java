package ml.arseniy899.rhouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ml.arseniy899.rhouse.HubManage.HubActivity;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Hub implements Serializable
{
	int hubID = 0;
	String ip = "";
	public String name = "";
	boolean isOnline = false;
	
	public Hub(int hubID, String name, boolean isOnline)
	{
		this.hubID = hubID;
		this.ip = ip;
		this.name = name;
		this.isOnline = isOnline;
	}
	
	void selectHub(Context context, Runnable runnable, boolean force)
	{
		if(force || Common.selectedHub == null || Common.selectedHub.hubID != this.hubID)
		{
			Map<String, String> map = new HashMap<>();
			map.put("id",this.hubID+"");
			WebRequest.request(context, "/hubs.bound.select.php", map, new WebCallBackInterface()
			{
				
				@Override
				public void onSuccess (JsonObject result)
				{
					runnable.run();
					MemoryWork.saveObject(context,"hub.selected",Hub.this);
				}
			}, context.getString(R.string.hub_select_saving));
		}
		else
			runnable.run();
	}
	static class HubAdapter extends BaseAdapter
	{
		
		private Activity activity;
		ListView listView;
		ArrayList<Hub> items = new ArrayList<>();
		
		public HubAdapter(Activity activity, ListView listView)
		{
			this.activity = activity;
			this.listView = listView;
			listView.setAdapter(this);
		}
		
		public void setItems(ArrayList<Hub> items)
		{
			this.items = items;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount()
		{
			return items.size();
		}
		
		@Override
		public Hub getItem(int position)
		{
			return items.get(position);
		}
		
		@Override
		public long getItemId(int position)
		{
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = this.activity.getLayoutInflater ();
			final View row;
			row = inflater.inflate (R.layout.item_hub, parent, false);
			if (row != null)
			{
				Hub hub = getItem(position);
				((TextView)row.findViewById(R.id.hubName)).setText(hub.name);
				ImageView isOnline = row.findViewById(R.id.isOnline);
				isOnline.setImageResource( hub.isOnline ?
														android.R.drawable.presence_online :
														android.R.drawable.presence_offline);
				row.setOnClickListener(v ->
				{
					hub.selectHub(activity, ()->{
						Intent intent = new Intent(activity, HubActivity.class);
						intent.putExtra("hub",hub);
						activity.startActivity(intent);
					}, true);
				});
			}
			return row;
		}
	}
	
	public static class Unit implements Serializable
	{
		public int id = 0;
		public String name = "";
		public String value = "";
		public String lastTime = "";
		public String possValues = "";
		public String units = "";
		public String direction;
		public String iconURL;
		public int sectId = 0;
		public int color = 0;
		public int textColor = 0;
		public Unit(int id, String name, String value, String iconURL)
		{
			this.id = id;
			this.name = name;
			this.value = value;
			if(iconURL.startsWith("//"))
				iconURL = "http:"+iconURL;
			this.iconURL = iconURL;
			
		}
		
		public Unit()
		{
		}
		public void bindJS(JsonObject unitJS)
		{
			id      = unitJS.get("id").getAsInt();
			sectId  = unitJS.get("sectId").getAsInt();
			name    = unitJS.get("name").getAsString();
			value   = unitJS.get("lastValue").getAsString();
			lastTime= unitJS.get("lastTime").getAsString();
			units   = unitJS.get("units").getAsString();
			direction   = unitJS.get("direction").getAsString();
			iconURL = unitJS.get("icon").getAsString();
			possValues= unitJS.get("possValues").getAsString();
			color= Color.parseColor("#"+unitJS.get("color").getAsString());
			textColor = Common.getContrastColor(color);
			if(iconURL.startsWith("//"))
				iconURL = "http:"+iconURL;
		}
		public void setValue(Context context, String value, Runnable runnable)
		{
			MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
			builder.setTitle(name);
			builder.setMessage(context.getString(R.string.unit_value_set_confirm)+" "+
					value+units+"?");
			
			builder.setPositiveButton(context.getString(R.string.unit_value_set_now), (dialog, which) ->
			{
				this.value = value;
				Map<String, String> map = new HashMap<>();
				map.put("id",id+"");
				map.put("value",value);
				WebRequest.request(context, "/units.set.value.php", map, new WebCallBackInterface()
				{
					
					@Override
					public void onSuccess (JsonObject result)
					{
						if(result.toString().contains("scheduled"))
							Toast.makeText(context,context.getString(R.string.unit_value_set_res_scheduled)+"  "+
									value+units+context.getString(R.string.unit_value_set_for)+name,Toast.LENGTH_LONG).show();
						else
							Toast.makeText(context,context.getString(R.string.unit_value_set_res)+" "+
									value+units+context.getString(R.string.unit_value_set_for)+name,Toast.LENGTH_LONG).show();
						runnable.run();
					}
				}, context.getString(R.string.unit_value_setting));
			});
			
			builder.setNeutralButton(context.getString(R.string.unit_value_set_schedule), (dialog, which) ->
			{
				
				new SingleDateAndTimePickerDialog.Builder(context)
						.listener(date -> {
							this.value = value;
							Map<String, String> map = new HashMap<>();
							map.put("id",id+"");
							map.put("value",value);
							map.put("date",Common.dateFormat.format(date));
							WebRequest.request(context, "/units.schedule.value.php", map, new WebCallBackInterface()
							{
								
								@Override
								public void onSuccess (JsonObject result)
								{
									Toast.makeText(context,context.getString(R.string.unit_value_set_res_scheduled)+"  "+
											value+units+context.getString(R.string.unit_value_set_for)+name,Toast.LENGTH_LONG).show();
									runnable.run();
								}
							}, context.getString(R.string.unit_value_set_scheduling));
						})
						.mustBeOnFuture()
						.title(context.getString(R.string.unit_value_set_chos_dateTime))
						.mainColor(Color.RED)
						.titleTextColor(Color.WHITE)
					.display();
				
				dialog.dismiss();
			});
			
			builder.setNegativeButton(context.getString(R.string.cancel), (dialog, which) ->
			{
				dialog.dismiss();
			});
			
			builder.show();
			
		}
	}
}
