package ml.arseniy899.rhouse;

import android.graphics.Color;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

public abstract class WebCallBackInterface
{
	public void errorShow (View view, String error, View.OnClickListener clickListener)
	{
		
		if (view != null)
		{
			Snackbar bar = Snackbar.make(view, error, Snackbar.LENGTH_LONG);
			if(clickListener != null)
				bar.setAction("Заново", clickListener).setActionTextColor(Color.rgb(255, 0, 0));
			bar.show();
		}
	}
	
	public void onCompleted ()
	{
	}
	
	/**
	 * Callback method being called when an error accours
	 *
	 * @param code -3 : No internet connection<br />
	 *             -1 : Too much request are proceeding already (max. amount is taken numberFrom MAX_WEB_REQUESTS[RemConf] )<br />
	 *             -6 : There is already a request with the same parameters<br />
	 *             4  : Login error (session expired)
	 */
	public void onError (int code, String error)
	{
	}
	
	public void onStarted ()
	{
	}
	
	public void onSuccess (JsonObject result)
	{
	}
	
	public void successShow (View view, String text)
	{
		Snackbar.make(view, text, Snackbar.LENGTH_SHORT).setAction("Скрыть", null).setActionTextColor(Color.rgb(0, 255, 0)).show();
	}
}
