package net.formula97.android.screenkeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * スタートアップ起動を行うBroadcastReceiver。<br />
 * Created by HAJIME Fukuna on 14/03/14.
 */
public class BootReceiver extends BroadcastReceiver {
    /**
     * ACTION_BOOT_COMPLETEDブロードキャストをトリガーに、センサー検知サービスを起動する。<br />
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Preferenceの値を参照する
        SharedPreferences preference = context.getSharedPreferences(Consts.Prefs.NAME, Context.MODE_PRIVATE);

        // スタートアップ起動を、StartAfterBootがtrueの場合だけに限定する
        if (preference.getBoolean(Consts.Prefs.START_AFTER_BOOT, false)) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                context.startService(new Intent(context, SensorManagerService.class));
            }
        }
    }
}
