package org.omeryildiz.dukatvapplication;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

public class BroadcastReceiveStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent singleApplication = new Intent(context, MainActivity.class);
            singleApplication.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(singleApplication);
        }
    }

}