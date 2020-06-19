package com.rstudios.simplesortingtask;

import android.app.Application;

import com.onesignal.OneSignal;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this).inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(new NotificationOpenHandler(this))
                .unsubscribeWhenNotificationsAreDisabled(true).init();
    }
}
