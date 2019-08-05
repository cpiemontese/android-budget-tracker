package piemontese.cristiano.budgettracking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import piemontese.cristiano.budgettracking.ManageActivity.ManagePlannedAndPeriodicActivity;


public class NotificationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String deadlineTitle = extras.getString("deadline title");
            String reminderTitle = extras.getString("reminder title");

            assert deadlineTitle != null;
            assert reminderTitle != null;

            if (!deadlineTitle.isEmpty())
                title = deadlineTitle;
            else if (!reminderTitle.isEmpty())
                title = reminderTitle;
            else
                return;
        } else
            return;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(title);

        Intent resultIntent = new Intent(context, ManagePlannedAndPeriodicActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ManagePlannedAndPeriodicActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(1, notification);
    }
}
