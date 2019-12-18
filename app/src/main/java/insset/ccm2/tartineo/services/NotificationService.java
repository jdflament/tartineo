package insset.ccm2.tartineo.services;

import android.app.Activity;
import android.app.Notification;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationService {
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

}
