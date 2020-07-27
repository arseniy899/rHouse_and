package ml.arseniy899.rhouse;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ml.arseniy899.rhouse.HubManage.HubActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class BoundHubsListActivity extends AppCompatActivity
{
	MemoryWork memoryWorker;
	Hub.HubAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bound_hubs_list);
	    memoryWorker = new MemoryWork(this);
	    adapter = new Hub.HubAdapter(this,findViewById(R.id.hubsList));
	    Common.parsePushIntentForSH(this, Common.boundHubs);
	    
	    if (Common.selectedHub == null)
	    {
		    Common.selectedHub = memoryWorker.loadObj(Hub.class, "hub.selected");
	    }
	    if(Common.selectedHub != null && !getIntent().getBooleanExtra("hub.change",false))
	    {
		    Intent intent = new Intent(this, HubActivity.class);
		    intent.putExtra("hub", Common.selectedHub);
		    this.startActivity(intent);
		    this.finish();
	    }
	    loadHubs();
	    
    }
	
	void loadHubs()
	{
		Map<String, String> map = new HashMap<>();
		WebRequest.requestMem(this, "/hubs.get.bounded.php", map, new WebCallBackInterface()
		{
			
			@Override
			public void onSuccess (JsonObject result)
			{
				JsonArray data = result.getAsJsonArray("data");
				Common.boundHubs.clear();
				for (JsonElement element:data)
				{
					JsonObject hubJS = element.getAsJsonObject();
					Hub nHub = new Hub(   hubJS.get("id").getAsInt(), hubJS.get("name").getAsString(),
							hubJS.get("isOnline").getAsBoolean() );
					Common.boundHubs.add(nHub);
					Common.subscribeTopic("hub_"+nHub.hubID);
					memoryWorker.saveObjects("hubs", Common.boundHubs);
				}
				adapter.setItems(Common.boundHubs);
				
			}
		}, this.getString(R.string.loading_hubs));
	}
}
