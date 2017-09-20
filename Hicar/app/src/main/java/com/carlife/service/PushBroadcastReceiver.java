package com.carlife.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

public class PushBroadcastReceiver extends BroadcastReceiver {		
	public static MediaPlayer mPlayer;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {  
            Intent service = new Intent(context, PushService.class);             
            service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(service);  
        }  	
	}

}
