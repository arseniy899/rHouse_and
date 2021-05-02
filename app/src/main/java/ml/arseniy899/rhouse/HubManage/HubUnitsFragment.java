package ml.arseniy899.rhouse.HubManage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ml.arseniy899.rhouse.Common;
import ml.arseniy899.rhouse.Hub;
import ml.arseniy899.rhouse.R;
import ml.arseniy899.rhouse.UnitPlotActivity;
import ml.arseniy899.rhouse.WebCallBackInterface;
import ml.arseniy899.rhouse.WebRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.zxing.common.StringUtils;
import com.koushikdutta.ion.Ion;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HubUnitsFragment extends Fragment
{
	
	private UnitAdapter adapter;
	static Context context;
	
	public static HubUnitsFragment newInstance(Context lContext)
	{
		HubUnitsFragment fragment = new HubUnitsFragment();
		fragment.context = lContext;
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
	}
	SwipeRefreshLayout swipeRefresh;
	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View root = inflater.inflate(R.layout.fragment_hub_manage, container, false);
		adapter = new UnitAdapter(inflater,root.findViewById(R.id.unitsList));
		swipeRefresh = root.findViewById(R.id.swiperefresh);
		swipeRefresh.setOnRefreshListener(() -> loadUnits(getContext()));
		loadUnits(this.getContext());
		return root;
	}
	void loadUnits(Context context)
	{
		swipeRefresh.setRefreshing(true);
		Map<String, String> map = new HashMap<>();
		WebRequest.requestMem(context, "/units.get.all.php", map, new WebCallBackInterface()
		{
			@Override
			public void onCompleted()
			{
				super.onCompleted();
				swipeRefresh.setRefreshing(false);
			}
			
			@Override
			public void onSuccess (JsonObject result)
			{
				JsonObject data = result.getAsJsonObject("data");
				JsonArray unitsJS = data.getAsJsonArray("units");
				JsonObject sectionsJS = data.getAsJsonObject("sections");
				Map<Integer, UnitSection> sections = new LinkedHashMap<>();
				for (Map.Entry<String, JsonElement> element:sectionsJS.entrySet())
				{
					JsonObject sectionJS = element.getValue().getAsJsonObject();
					UnitSection section = new UnitSection();
					section.bindJS(sectionJS);
					sections.put(section.id,section);
				}
				ArrayList<Hub.Unit> units = new ArrayList<>();
				for (JsonElement element:unitsJS)
				{
					JsonObject unitJS = element.getAsJsonObject();
					if (unitJS.get("uiShow").getAsBoolean())
					{
						Hub.Unit unit = new Hub.Unit();
						unit.bindJS(unitJS);
						sections.get(unit.sectId).units.add(unit);
						units.add(unit);
					}
				}
				adapter.setSections(sections);
			}
		}, getString(R.string.loading));
	}
	
	static class UnitSection
	{
		int id = 0;
		String name = "";
		boolean isDefHidden = true;
		ArrayList<Hub.Unit> units = new ArrayList<>();
		public UnitSection(int id, String name)
		{
			this.id = id;
			this.name = name;
		}
		
		public UnitSection()
		{
		}
		void bindJS(JsonObject json)
		{
			id          = json.get("id").getAsInt();
			isDefHidden = json.get("isDefHidden").getAsInt()==1;
			name        = json.get("name").getAsString();
		}
	}
	static class UnitAdapter extends BaseExpandableListAdapter
	{
		LayoutInflater inflater;
		ExpandableListView listView;
		Map<Integer, UnitSection> sections = new LinkedHashMap<>();
		
		public UnitAdapter(LayoutInflater inflater, ExpandableListView listView)
		{
			this.inflater = inflater;
			this.listView = listView;
			listView.setAdapter(this);
		}
		
		public void setSections(Map<Integer, UnitSection> sections)
		{
			this.sections = sections;
			int i = 0;
			for (Map.Entry<Integer,UnitSection> section:sections.entrySet())
			{
				if(!section.getValue().isDefHidden)
					listView.expandGroup(i);
				i++;
			}
			notifyDataSetChanged();
		}
		
		
		@Override
		public int getGroupCount()
		{
			return sections.size();
		}
		
		@Override
		public int getChildrenCount(int groupPosition)
		{
			return  getGroup(groupPosition).units.size();
		}
		
		@Override
		public UnitSection getGroup(int groupPosition)
		{
			return sections.get(sections.keySet().toArray()[groupPosition]);
		}
		
		@Override
		public Hub.Unit getChild(int groupPosition, int childPosition)
		{
			return getGroup(groupPosition).units.get(childPosition);
		}
		
		@Override
		public long getGroupId(int groupPosition)
		{
			return getGroup(groupPosition).id;
		}
		
		@Override
		public long getChildId(int groupPosition, int childPosition)
		{
			return getGroup(groupPosition).units.get(childPosition).id;
		}
		
		@Override
		public boolean hasStableIds()
		{
			return false;
		}
		
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{
			View row = inflater.inflate (R.layout.item_hub_unit_section, parent, false);
			if (row != null)
			{
				UnitSection section = getGroup(groupPosition);
				((TextView)row.findViewById(R.id.sectionName)).setText(section.name);
				
			}
			return row;
		}
		boolean isListenSwitch = true;
		View renderSwitch(Hub.Unit unit, ViewGroup parent)
		{
			View row = inflater.inflate (R.layout.item_hub_unit_switch, parent, false);
			Switch switchS = row.findViewById(R.id.unitValue);
			String states [] = unit.possValues.split(",");
			String offVal = states[0];
			String onVal = states[1];
			if(unit.value.equals(onVal))
			{
				switchS.setChecked(true);
				switchS.setText(context.getString(R.string.state_on));
			}
			else if(unit.value.equals(offVal))
			{
				switchS.setChecked(false);
				switchS.setText(context.getString(R.string.state_off));
			}
			else
				switchS.setText(unit.value);
			if(unit.direction.contains("O"))
			{
				
				switchS.setOnCheckedChangeListener((buttonView, isChecked) ->
				{
					
					if (isListenSwitch)
					{
						isListenSwitch = false;
						switchS.setChecked(!isChecked);
						isListenSwitch = true;
						unit.setValue(HubUnitsFragment.context, isChecked ? onVal : offVal,
								() ->
								{
									isListenSwitch = false;
									switchS.setChecked(isChecked);
									isListenSwitch = true;
									switchS.setText(isChecked ? context.getString(R.string.state_on) :
											context.getString(R.string.state_off));
								});
					}
					
				});
			}
			else
				switchS.setEnabled(false);
			return row;
		}
		
		View renderSingleState(Hub.Unit unit, ViewGroup parent)
		{
			View row = inflater.inflate (R.layout.item_hub_unit_singlestate, parent, false);
			row.findViewById(R.id.unitIcon).setOnClickListener(v -> unit.setValue(HubUnitsFragment.context, unit.possValues, () ->{}));
			
			return row;
		}
		View renderSeek(Hub.Unit unit, ViewGroup parent)
		{
			View row = inflater.inflate (R.layout.item_hub_unit_slider, parent, false);
			NumberPicker seekBar = row.findViewById(R.id.number_picker);
			String[] params = unit.possValues.split(":");
			int step    = 1;
			int min     = 0;
			int max     = 1;
			if(params.length == 1)
				max = Integer.parseInt(unit.possValues);
			else if(params.length == 2)
			{
				min = Integer.parseInt(params[0]);
				max = Integer.parseInt(params[1]);
			}
			else if(params.length == 3)
			{
				min = Integer.parseInt(params[0]);
				step = Integer.parseInt(params[1]);
				max = Integer.parseInt(params[2]);
			}
			ArrayList <String> values = new ArrayList<>();
			String valueStr = unit.value.replaceAll("[^\\d.]", "");
			int value = (valueStr.isEmpty()) ? min : Integer.parseInt(valueStr);
			int valIndex = 1;
			for (int i = min; i <= max; i+= step)
			{
				if(i == value)
					valIndex = values.size();
				values.add(i + unit.units);
			}
			seekBar.setMinValue(0);
			seekBar.setMaxValue(values.size()-1);
			seekBar.setDisplayedValues(values.toArray(new String[0]));
			seekBar.setValue(valIndex);

			ImageView apply = row.findViewById(R.id.unitApply);
			apply.setVisibility(View.GONE);
//			apply.setColorFilter(unit.textColor);
			
			seekBar.setOnValueChangedListener((picker, oldVal, newVal) ->
			{
				if (apply.getVisibility() != View.VISIBLE)
					apply.setVisibility(View.VISIBLE);
			});
			apply.setOnClickListener(v ->
					unit.setValue(HubUnitsFragment.context,
							values.get(seekBar.getValue()).replace(unit.units,"")+"",
					() ->apply.setVisibility(View.GONE)));
			return row;
		}
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
		{
			Hub.Unit unit = getChild(groupPosition, childPosition);
			View row;
			if(Common.countCharOccurrence(unit.possValues,',') == 1)
				row = renderSwitch(unit, parent);
			else if(unit.possValues.contains(":"))
				row = renderSeek(unit, parent);
			else if(unit.possValues.equals("1") || unit.possValues.equals("0") ||
					unit.possValues.equals("on") || unit.possValues.equals("off"))
				row = renderSingleState(unit, parent);
			else
			{
				row = inflater.inflate (R.layout.item_hub_unit_label, parent, false);
				((TextView)row.findViewById(R.id.unitValue)).setText(unit.value+unit.units);
				
			}
			if (row != null)
			{
				row.setBackgroundColor(unit.color);
				TextView unitName = row.findViewById(R.id.unitName);
				unitName.setText(unit.name);
				TextView unitTime = row.findViewById(R.id.unitTime);
				unitTime.setText(unit.lastTime);
				ImageView unitIcon = row.findViewById(R.id.unitIcon);
				Ion.with(unitIcon)
						.load(unit.iconURL);
				row.setOnClickListener(v ->
				{
				
				});
				if(row.findViewById(R.id.openPlot) != null)
					row.findViewById(R.id.openPlot).setOnClickListener(v ->
					{
						Intent intent = new Intent(inflater.getContext(), UnitPlotActivity.class);
						intent.putExtra("unit", unit);
						inflater.getContext().startActivity(intent);
					});
			}
			return row;
		}
		
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return false;
		}
	}
}