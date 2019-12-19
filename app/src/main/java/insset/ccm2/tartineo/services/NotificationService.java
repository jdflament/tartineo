package insset.ccm2.tartineo.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import insset.ccm2.tartineo.R;

public class NotificationService {
    public static final String MARKERS_CHANNEL_ID = "MARKERS_NOTIFICATION_CHANNEL";
    public static final String RELATIONS_CHANNEL_ID = "RELATIONS_NOTIFICATION_CHANNEL";

    private static final NotificationService instance = new NotificationService();

    private NotificationService() { }

    public static NotificationService getInstance() {
        return instance;
    }

    /**
     * Créer une notification.
     *
     * @param CHANNEL_ID  The channel ID on which to transmit the notification.
     * @param activity    The activity on which to create the notification.
     * @param icon        The notification icon.
     * @param textTitle   The notification title.
     * @param textContent The notification content.
     */
    public void createNotification(String CHANNEL_ID, Activity activity, int icon, String textTitle, String textContent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(icon)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);

        int notificationId = (int) System.currentTimeMillis();

        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because
     * the NotificationChannel class is new and not in the support library
     *
     * @param id L'identifiant du canal
     * @param name Le nom du canal
     * @param description La description du canal
     *
     * @return NotificationChannel
     *
     * @throws Exception Erreur si API inférieur à 26.
     */
    public NotificationChannel createNotificationChannel(String id, CharSequence name, String description) throws Exception {
        if (Build.VERSION.SDK_INT< Build.VERSION_CODES.O) {
            throw new Exception("You must be in API 26+ to create NotificationChannel");
        }

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);

        return channel;
    }
}
