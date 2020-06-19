package com.rstudios.simplesortingtask;

import android.app.Application;
import android.content.Intent;

import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class NotificationOpenHandler implements OneSignal.NotificationOpenedHandler {
    private Application application;

    public NotificationOpenHandler(Application application) {
        this.application = application;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        JSONObject data = result.notification.payload.additionalData;
        Intent intent=new Intent(application,NewsActivity.class);
        intent.putExtra("url",data.optString("url"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }
}
