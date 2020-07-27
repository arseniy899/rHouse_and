package ml.arseniy899.rhouse;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public class MemoryWork
{
    Context context;
    SharedPreferences sharedPref;

    public MemoryWork(Context context)
    {
        this.context = context.getApplicationContext ();
        this.sharedPref = context.getSharedPreferences ("main", Context.MODE_PRIVATE);

    }

    static void save(Context context, String key, int value)
    {
        SharedPreferences sharedPref = context.getSharedPreferences ("main", Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit ();
        editor.putInt (key, value);
        editor.commit ();
        if (!key.equals ("debug-on"))
            Log.d ("MemoryWorkerSave", key + "=" + value);
    }

    static void save(Context context, String key, String value)
    {
        SharedPreferences sharedPref = context.getSharedPreferences ("main", Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit ();
        editor.putString (key, value);
        editor.commit ();
        if (!key.equals ("debug-on"))
            Log.d ("MemoryWorkerSave", key + "=" + value);
    }

    static void save(Context context, String key, boolean value)
    {
        SharedPreferences sharedPref = context.getSharedPreferences ("main", Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit ();
        editor.putBoolean (key, value);
        editor.commit ();
        if (!key.equals ("debug-on"))
            Log.d ("MemoryWorkerSave", key + "=" + value);
    }

    static int loadInt(Context context, String key)
    {
        SharedPreferences sharedPref = context.getSharedPreferences ("main", Context.MODE_PRIVATE);
        if (!key.equals ("debug-on"))
            Log.d ("MemoryWorkerLoad", key + "=" + sharedPref.getInt (key, 0));
        return sharedPref.getInt (key, 0);
    }

    static String loadString(Context context, String key)
    {
        SharedPreferences sharedPref = context.getSharedPreferences ("main", Context.MODE_PRIVATE);
        if (!key.equals ("debug-on"))
            Log.d ("MemoryWorkerLoad", key + "=" + sharedPref.getString (key, ""));
        return sharedPref.getString (key, "");
    }

    static boolean loadBool(Context context, String key)
    {
        SharedPreferences sharedPref = context.getSharedPreferences ("main", Context.MODE_PRIVATE);
        if (!key.equals ("debug-on"))
            Log.d ("MemoryWorkerLoad", key + "=" + sharedPref.getBoolean (key, false));
        return sharedPref.getBoolean (key, false);
    }

    public void save(String key, int value)
    {
        Editor editor = this.sharedPref.edit ();
        editor.putInt (key, value);
        editor.commit ();
        Log.d ("MemoryWorkerSave", key + "=" + value);
    }

    public void save(String key, String value)
    {
        Editor editor = this.sharedPref.edit ();
        editor.putString (key, value);
        editor.commit ();
        Log.d ("MemoryWorkerSave", key + "=" + value);
    }

    public void save(String key, boolean value)
    {
        Editor editor = this.sharedPref.edit ();
        editor.putBoolean (key, value);
        editor.commit ();
        Log.d ("MemoryWorkerSave", key + "=" + value);
    }

    public int loadInt(String key)
    {
        Log.d ("MemoryWorkerLoad", key + "=" + this.sharedPref.getInt (key, 0));
        return this.sharedPref.getInt (key, 0);
    }

    public String loadString(String key)
    {
        Log.d ("MemoryWorkerLoad", key + "=" + this.sharedPref.getString (key, ""));
        return this.sharedPref.getString (key, "");
    }

    public boolean loadBool(String key)
    {
        if (!key.equals ("debug-on"))
            Log.d ("MemoryWorkerLoad", key + "=" + this.sharedPref.getBoolean (key, false));
        return this.sharedPref.getBoolean (key, false);
    }

    public String getDumpOfEntries()
    {
        Map<String, ?> keys = this.sharedPref.getAll ();
        String ret = "";
        for (Entry<String, ?> entry : keys.entrySet ())
            ret += entry.getKey () + "=" + entry.getValue () + "\n";
        return ret;
    }
    public static void clearCache(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences ("main", Context.MODE_PRIVATE);
        Map<String, ?> keys = sharedPref.getAll ();
        for (Entry<String, ?> entry : keys.entrySet ())
        {
            if (entry.getValue ().toString ().length ()>100)
                Log.d ("ClearCache","lentg="+entry.getKey ());
            if(entry.getKey ().startsWith ("/rs/") || entry.getValue ().toString ().length ()>100)
                sharedPref.edit ().remove (entry.getKey ()).apply ();

        }
        sharedPref.edit ().apply ();
    }
    public <T> T loadObj (Class<T> TClass, String key)
    {
        Gson gson = new Gson();
        String text = loadString(key);
        T ret = gson.fromJson(text, TClass);
        return (T) ret;
    }
    
    public <T> T loadObject (Class<T> TClass, String key)
    {
        return loadObj(TClass, key);
    }
    
    public static  <T> T loadObject (Context context, Class<T> TClass, String key)
    {
	    Gson gson = new Gson();
	    String text = loadString(context,key);
	    T ret = gson.fromJson(text, TClass);
	    return (T) ret;
    }
    public void saveObj (String key, Object object)
    {
        Gson gson = new Gson();
        String text = gson.toJson(object);
        save(key, text);
    }
    
    public void saveObject (String key, Object object)
    {
	    saveObj(key, object);
    }
    
    public static  void saveObject (Context context, String key, Object object)
    {
	    Gson gson = new Gson();
	    String text = gson.toJson(object);
	    save(context,key, text);
    }
    public void clearAll()
    {
        Editor editor = this.sharedPref.edit ();
        editor = editor.clear ();
        editor.apply ();
    }
	public <T> ArrayList<T> saveObjects (String key, ArrayList objects)
	{
		Gson gson = new Gson();
		ArrayList objectsN = new ArrayList();
		objectsN.addAll(objects);
		synchronized (objectsN)
		{
			save(key, gson.toJson(objectsN));
		}
		return objectsN;
	}
	public static  <T> ArrayList<T> saveObjects (Context context, String key, ArrayList objects)
	{
		Gson gson = new Gson();
		ArrayList objectsN = new ArrayList();
		objectsN.addAll(objects);
		synchronized (objectsN)
		{
			save(context, key, gson.toJson(objectsN));
		}
		return objectsN;
	}
	public <T> ArrayList<T> loadObjects (Class<T> clazz, String name)
	{
		Gson gson = new Gson();
		String strJson = loadString(name);
		ArrayList<T> lst = new ArrayList<T>();
		JsonParser parser = new JsonParser();
		if (parser.parse(strJson) == null || !parser.parse(strJson).isJsonArray())
		{
			return lst;
		}
		JsonArray array = parser.parse(strJson).getAsJsonArray();
		for (JsonElement json : array)
		{
			try
			{
				T entity = gson.fromJson(json, clazz);
				lst.add(entity);
			} catch (JsonSyntaxException e)
			{
				Log.e("loadObjects/ParseError", "class=" + clazz + ";value=" + json);
			}
		}
		return lst;
	}
	
	public static <T> ArrayList<T> loadObjects (Context context, Class<T> clazz, String name)
	{
		Gson gson = new Gson();
		String strJson = loadString(context, name);
		ArrayList<T> lst = new ArrayList<T>();
		JsonParser parser = new JsonParser();
		if (parser.parse(strJson) == null || !parser.parse(strJson).isJsonArray())
		{
			return lst;
		}
		JsonArray array = parser.parse(strJson).getAsJsonArray();
		for (JsonElement json : array)
		{
			try
			{
				T entity = gson.fromJson(json, clazz);
				lst.add(entity);
			} catch (JsonSyntaxException e)
			{
				Log.e("loadObjects/ParseError", "class=" + clazz + ";value=" + json);
			}
		}
		return lst;
	}
	
}