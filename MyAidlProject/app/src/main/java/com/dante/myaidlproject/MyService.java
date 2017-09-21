package com.dante.myaidlproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("service Staerted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("service destory");
    }
}
