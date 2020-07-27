package ml.arseniy899.rhouse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity
{
	
	MemoryWork memoryWork;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Common.startUp(getBaseContext());
		final EditText usernameEditText = findViewById(R.id.username);
		final EditText passwordEditText = findViewById(R.id.password);
		final Button loginButton = findViewById(R.id.login);
		memoryWork = new MemoryWork(this);
		String login = memoryWork.loadString("login");
		String password = memoryWork.loadString("password");
		if(login.length() > 0)
		{
			usernameEditText.setText(login);
			passwordEditText.setText(password);
//			auth(login, password);
			openHubManage();
		}
		loginButton.setOnClickListener((View v)->{
			if(usernameEditText.getText().length() == 0)
				usernameEditText.setError(getString(R.string.field_should_be_nonempty));
			else if(passwordEditText.getText().length() == 0)
				passwordEditText.setError(getString(R.string.field_should_be_nonempty));
			else
			{
				usernameEditText.setError(null);
				passwordEditText.setError(null);
				auth(usernameEditText.getText()+"",passwordEditText.getText()+"");
			}
		});
	}
	void auth(final String login, final String password)
	{
		Map<String, String> map = new HashMap<>();
		map.put("login", login);
		map.put("passw", password);
		WebRequest.request(this, "/user.auth.login.php", map, new WebCallBackInterface()
		{
			
			@Override
			public void onSuccess (JsonObject result)
			{
				memoryWork.save("login",login);
				memoryWork.save("password",password);
				
				openHubManage();
			}
		}, getString(R.string.authorization));
		
	}
	void openHubManage()
	{
		Intent intent = new Intent(this, BoundHubsListActivity.class);
		startActivity(intent);
		finish();
	}
	
}
