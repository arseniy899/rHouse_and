package ml.arseniy899.rhouse;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.ContentFrameLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * \brief Class is needed for performing requests to cloff.ru web-server, counting current connections, showing loading bars.<br />
 * Template:
 *
 * @code Map<String   ,   String> map = new HashMap<> ();
 * map.put ();
 * WebRequest.request (this, , map, new WebCallBackInterface ()
 * {
 * @Override public void onSuccess(JsonObject result)
 * {
 * <p>
 * }
 * }, "");
 * @endcode
 */
public class WebRequest extends WebCallBackInterface
{
	/**
	 * Array for storing list of requests' URI
	 */
	public static ArrayList<String> requests = new ArrayList<>(), /**
 * Array for storing list of requests' URI called with activity context
 */
	requestsUI = new ArrayList<>();
	/**
	 * Variable for storing progress bar view
	 */
	public static View progressBar;
	/**
	 * Rootview of activity if given context is activity
	 */
	public static ContentFrameLayout rootView;
	/**
	 * View of request proceeding snackbar
	 */
	public static Snackbar snackbar;
	static String cookie = null;
	static String reportedInvalidJson = "";
	
	/**
	 * Get URI (id-name) of request to determine it for storing in memory
	 *
	 * @param map  list of request parameters
	 * @param page Link to page to proceed with. E.g. /rs/knock-knock.rs.php
	 * @return String with specific name unique for this requests
	 */
	public static String getRequestUri (Map<String, String> map, String page)
	{
		String line = page + ",";
		if (map != null)
		{
			String keyValues = "id mode page accid fileid folder name query";
			for (Map.Entry<String, String> point : map.entrySet())
			{
				if (keyValues.contains(point.getKey().toLowerCase()))
				{
					line += point.getKey() + "=" + point.getValue() + ",";
				}
			}
		}
		return line;
	}
	
	/**
	 * Determine if device has network connection (E.g. over WiFi or GSM)
	 *
	 * @param context
	 * @return true if has
	 */
	public static boolean isNetworkConnected (Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		return cm.getActiveNetworkInfo() != null;
	}
	
	/**
	 * Perform request to web-server without storing result in memory
	 *
	 * @param context  If context is activity, loading bar and request proceeding snackbar with text 'Запрос обрабатывается' will be shown
	 * @param page     Link to page to proceed with. E.g. /rs/knock-knock.rs.php
	 * @param map      Map of request body (POST data) in format key, data
	 * @param callback Resulting callback for actions after request
	 */
	public static void request (Context context, final String page, Map<String, String> map,
	                            final WebCallBackInterface callback)
	{
		request(context, page, map, callback, "Запрос обрабатывается");
	}
	
	/**
	 * Perform request to web-server without storing result in memory. Error codes called with onError:<br />
	 * -3 : No internet connection<br />
	 * -1 : Too much request are proceeding already (max. amount is taken numberFrom MAX_WEB_REQUESTS[RemConf] )<br />
	 * -6 : There is already a request with the same parameters<br />
	 * 4  : Login error (session expired)=
	 *
	 * @param context  If context is activity, loading bar and request proceeding snackbar with specified text will be shown
	 * @param page     Link to page to proceed with. E.g. /rs/knock-knock.rs.php
	 * @param map      Map of request body (POST data) in format key, data
	 * @param callback Resulting callback for actions after request
	 * @param text     Text to be shown on request proceeding snackbar
	 */
	public static void request (final Context context, final String page,
	                            final Map<String, String> map, final WebCallBackInterface callback,
	                            final String text)
	{
		if (context instanceof Activity)
		{
			View v = Common.getRootView((Activity) context);
			if (v instanceof ContentFrameLayout)
			{
				rootView = (ContentFrameLayout) v;
			}
		}
		
		if (!isNetworkConnected(context))
		{
			if (callback != null)
			{
				callback.onError(ErrCodes.NO_INTERNET, "Отсутствует интернет-подключение на устройстве");
				if (context instanceof Activity)
				{
					callback.errorShow(rootView, "Отсутствует интернет-подключение на устройстве", null);
				}
				callback.onCompleted();
			}
			return;
		}
		
		final String uri = getRequestUri(map, page);
		if (requests.size() > 0 && requests.contains(uri))
		{
			Log.i("WEB/RequestsCount", "Too much same requests " + requests);
			if (callback != null)
			{
				callback.onError(ErrCodes.TOO_MANY_SAME_REQUESTS, "Слишком много идентичных запросов");
				callback.onCompleted();
			}
			return;
		}
		
		if (!uri.contains("/rs/login.rs.php"))
		{
			requests.add(uri);
		}
//        curConnections++;
		if (callback != null)
		{
			callback.onStarted();
		}
		final String url;
		if (!page.contains(Common.API_URL) )
		{
			url = Common.API_URL + page;
		}
		else
		{
			url = page;
		}
//		Ion.getDefault (context.getApplicationContext ()).configure ().userAgent ("cloff_app_android");
		String version = "";
		try
		{
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e)
		{
			version = "undefined";
			e.printStackTrace();
		}
		final Builders.Any.B load = Ion.with(context.getApplicationContext()).load("POST",url).userAgent("android-app/v." + version + ";and=" + System.getProperty("os.version") + ";m=" + android.os.Build.MODEL + "/" + Build.BRAND);
		if (context instanceof Activity && progressBar == null && Looper.getMainLooper().getThread() == Thread.currentThread())
		{
//            curConnectionsUI++;
//            if(!uri.contains ("/rs/login.rs.php"))
			WebRequest.requestsUI.add(uri);
			WebRequest.snackbar = Snackbar.make(WebRequest.rootView, text, Snackbar.LENGTH_INDEFINITE).setAction("Загрузка", null);
//			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			/*ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(128, 128);
			params.topToTop = rootView.getId();
			params.bottomToBottom = rootView.getId();
			params.leftToLeft = rootView.getId();
			params.rightToRight = rootView.getId();
			params.verticalBias = (float) 0.5;
			params.horizontalBias = (float) 0.5;
			//WebRequest.progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
			*//*WebRequest.progressBar = inflater.inflate(layout.fragment_loading, null);
			ImageView loadingGif = (ImageView) WebRequest.progressBar.findViewById(R.id.logo);
			Glide.with(context.getApplicationContext()).load(drawable.ajaxloading).into(loadingGif);
			
			LogKeeper.d("WEB/Started", "requestsUI.size ()=" + requestsUI.size());
			TODO: SHOW SPINNER
			*//*
			WebRequest.rootView.addView(WebRequest.progressBar, params);
			WebRequest.progressBar.requestLayout();*/
			if (text.length() > 0)
			{
				WebRequest.snackbar.show();
			}
			
		}
		if (map != null)
		{
			for (Map.Entry<String, String> m : map.entrySet())
			{
				load.setBodyParameter(m.getKey(), m.getValue());
			}
		}
		if(Common.selectedHub != null)
			load.setBodyParameter("hubID", Common.selectedHub.hubID+"");
		if (WebRequest.progressBar != null && context instanceof Activity)
		{
			load.progress(new ProgressCallback()
			{
				@Override
				public void onProgress (long downloaded, long total)
				{
//					if(WebRequest.progressBar != null && context instanceof Activity)
//						WebRequest.progressBar.setAlpha (downloaded/total);
				}
			});
		}
		final ResponseFuture fut = load.asString();
		
		Log.i("WEB/REQUEST:" + url + " :", map.toString() + "");
		
		fut.setCallback(new FutureCallback<String>()
		{
			FutureCallback<String> th = this;
			
			@Override
			public void onCompleted (Exception e, String result)
			{
				if (callback != null)
				{
					callback.onCompleted();
				}
				WebRequest.requests.remove(uri);
//                curConnections--;
				if (context instanceof Activity && WebRequest.snackbar != null)
				{
					WebRequest.snackbar.dismiss();
				}
				
				if (context instanceof Activity && Looper.getMainLooper().getThread() == Thread.currentThread())
				{
					synchronized (WebRequest.requestsUI)
					{
						WebRequest.requestsUI.remove(uri);
					}
//
					if (WebRequest.progressBar != null && WebRequest.requestsUI.size() <= 0)
					{
//                        progressBar.setIndeterminate (false);
						WebRequest.progressBar.setVisibility(View.GONE);
						WebRequest.rootView.removeView(WebRequest.progressBar);
						WebRequest.progressBar = null;
						Log.d("WEB/RESULT_UI", "requestsUI.size ()=" + WebRequest.requestsUI.size());
					}
				}
				if (e != null)
				{
					Log.e("WEB:" + url, "ERROR: " + e.toString());
					
					if (callback != null)
					{
						if (e.getClass() == TimeoutException.class)
						{
							Log.e("WEB:" + url, "ERROR: Timeout Exception");
							callback.onError(ErrCodes.COULD_NOT_CONNECT, "Ошибка подключения к серверу");
							callback.errorShow(rootView,"Ошибка подключения к серверу",null);
						}
						else if (e.getClass() == JsonParseException.class)
						{
							Log.e("WEB/RESULT/PARSE", "ex: " + e);
							/*String comment = "Comment: +++++++ AUTO GENERATED +++++++\n" +
									" ERROR: page requested returned not json-valid data!. \n" +
									" page="+url+"\n" +
									" params="+new JSONObject(map)+"\n" +
									"raw result="+result.get (0);
							ConstStat.sendLog (context,comment,"",true);*/
							callback.onError(ErrCodes.RECEIVE_DATA, "Ошибка получения данных с сервера");
							callback.errorShow(rootView,"Ошибка получения данных с сервера",null);
//							Log.e ("WEB:"+ConstStat.DOMEN+""+url+"/Error",r);
						
						}
						else
						{
							callback.onError(ErrCodes.PARSE_DATA, "Ошибка взаимодействия с сервером");
							callback.errorShow(rootView,"Ошибка взаимодействия с сервером",null);
						}
						
					}
				}
				else
				{
					
					Log.i("WEB/RESULT:" + url + " :", result + "");
					Gson gson = new Gson();
					try
					{
						JsonObject responce = gson.fromJson(result, JsonObject.class).getAsJsonObject("responce");
						
						if (responce.get("error").getAsInt() > 0)
						{
							if (callback != null)
							{
								callback.onError(responce.get("error").getAsInt(), responce.get("desc").getAsString());
								callback.errorShow(rootView,responce.get("desc").getAsString(),null);
							}
							
						}
						else if (callback != null)
						{
							MemoryWork memoryWork = new MemoryWork(context);
							memoryWork.save(uri, responce.toString());
							//                        if(context instanceof Activity)
							try
							{
								callback.onSuccess(responce);
							} catch (NullPointerException ex)
							{
								if (!url.contains("login.rs"))
								{
									Log.e("WEB/RESULT/PARSE", "ex: " + ex);
									/*String comment = "Comment: +++++++ AUTO GENERATED +++++++\n" +
											" ERROR: find null while parsing json. \n" +
											" page=" + url + "\n" +
											" params=" + new JSONObject(map) + "\n" +
											"result=" + responce;
									Log.sendInfoToEmail(context, "JSON REPORT", comment);
									ConstStat.sendLog(context, comment, "", "", true);*/
								}
							}
							//                        callback.successShow (rootView,"Успешно");
						}
					} catch (NullPointerException ex)
					{
						if (reportedInvalidJson.length() == 0)
						{
							reportedInvalidJson = MemoryWork.loadString(context, "reportedInvalidJson");
						}
						Log.e("WEB/RESULT/PARSE", "ex: " + ex);
						if (!reportedInvalidJson.contains(uri + ";") && !url.contains("login.rs"))
						{
							String comment = "Comment: +++++++ AUTO GENERATED +++++++\n" +
									" ERROR: find null while parsing json. \n" +
									" page=" + url + "\n" +
									" params=" + new JSONObject(map) + "\n" +
									"result=" + result;
							
							callback.onError(-4, "Ошибка получения данных с сервера");
							callback.errorShow(rootView,"Ошибка получения данных с сервера",null);
//							Log.e ("WEB:"+ConstStat.DOMEN+""+url+"/Error",r);
							reportedInvalidJson += uri + ";";
							MemoryWork.save(context, "reportedInvalidJson", reportedInvalidJson);
							
						}
					} catch (JsonParseException ex)
					{
						
						if (reportedInvalidJson.length() == 0)
						{
							reportedInvalidJson = MemoryWork.loadString(context, "reportedInvalidJson");
						}
						if (!reportedInvalidJson.contains(uri + ";") && !url.contains("login.rs"))
						{
							Log.d("WEB/RESULT/PARSE/Not-JS", "result: " + result);
							Log.e("WEB/RESULT/PARSE/", "ex: " + ex);
							JSONObject obj;
							if (map != null)
							{
								obj = new JSONObject(map);
							}
							else
							{
								obj = new JSONObject();
							}
							String comment = "Comment: +++++++ AUTO GENERATED +++++++\n" +
									" ERROR: page requested returned not json-valid data!. \n" +
									" page=" + url + "\n" +
									" params=" + obj + "\n" +
									"raw result=" + result;
							callback.onError(-4, "Ошибка получения данных с сервера");
							callback.errorShow(rootView,"Ошибка получения данных с сервера",null);
//							Log.e ("WEB:"+ConstStat.DOMEN+""+url+"/Error",r);
							reportedInvalidJson += uri + ";";
							MemoryWork.save(context, "reportedInvalidJson", reportedInvalidJson);
						}
						
					}
					
				}
				/*if(callback != null)
					callback.onCompleted ();*/
			}
		});
	}
	
	/**
	 * Perform request to web-server and storer result in memory
	 *
	 * @param context  If context is activity, loading bar and request proceeding snackbar with specified text will be shown
	 * @param page     Link to page to proceed with. E.g. /rs/knock-knock.rs.php
	 * @param map      Map of request body (POST data) in format key, data
	 * @param callback Resulting callback for actions after request
	 * @param text     Text to be shown on request proceeding snackbar
	 */
	public static void requestMem (final Context context, final String page,
	                               Map<String, String> map, final WebCallBackInterface callback,
	                               String text)
	{
		MemoryWork memoryWorker = new MemoryWork(context);
		Gson gson = new Gson();
		if (callback != null)
		{
			JsonObject arr = gson.fromJson(memoryWorker.loadString(getRequestUri(map, page)), new TypeToken<JsonObject>()
			{
			}.getType());
			if (arr != null)
			{
				callback.onSuccess(arr);
			}
		}
		request(context, page, map, callback, text);
	}
	
	public static class ErrCodes
	{
		public static final int NO_INTERNET = -139;
		public static final int TOO_MANY_REQUESTS = -140;
		public static final int TOO_MANY_SAME_REQUESTS = -141;
		public static final int COULD_NOT_CONNECT = -142;
		public static final int RECEIVE_DATA = -143;
		public static final int PARSE_DATA = -144;
	}
}
