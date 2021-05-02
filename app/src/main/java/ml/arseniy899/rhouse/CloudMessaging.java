package ml.arseniy899.rhouse;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CloudMessaging extends FirebaseMessagingService
{
    String text_time, in_temp, temp_set, out_temp;
    boolean secur;
    String TAG = "FCM";
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage)
    {
        
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        int hubID = 0;
        String title = "";
        String text = "";
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.i(TAG, "Message data payload: " + remoteMessage.getData());
        
            if(remoteMessage.getData().containsKey("hubID"))
                hubID = Integer.parseInt(remoteMessage.getData().get("hubID"));
            if(remoteMessage.getData().containsKey("title"))
                title = remoteMessage.getData().get("title");
            if(remoteMessage.getData().containsKey("text"))
                text = remoteMessage.getData().get("text");
        
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            text = remoteMessage.getNotification().getBody();
            
            Log.i(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.i(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            
        }
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificatioId = System.currentTimeMillis();
    
        PendingIntent contentIntent = null;
        if(hubID != 0)
        {
            Intent intent = new Intent(getApplicationContext(), NotifyActivity.class); // Here pass your activity where you want to redirect.
            intent.putExtra("hubID", hubID);
            if (title != null && !title.isEmpty())
            {
                intent.putExtra("push.title", title);
                intent.putExtra("push.text", text);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            contentIntent = PendingIntent.getActivity(this.getApplicationContext(), (int) (Math.random() * 100), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    
        int icon = R.drawable.ic_launcher;
    
        String CHANNEL_ID = "main";// The id of the channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.notify_from_home), importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle(title)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(icon);
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            notificationBuilder.setSmallIcon(icon);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notificationBuilder .setChannelId(CHANNEL_ID);
            
        }
        notificationBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify((int) notificatioId, notificationBuilder.build());
    }

}