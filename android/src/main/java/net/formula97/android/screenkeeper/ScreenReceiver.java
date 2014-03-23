package net.formula97.android.screenkeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {
    public ScreenReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            // ToDo: スクリーン点灯時の処理を書く
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            // ToDo: スクリーン消灯時の処理（＝サービスが開始されている場合は停止）を書く
        }
    }
}
