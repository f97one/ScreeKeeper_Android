package net.formula97.android.screenkeeper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.List;

public class MainActivity extends Activity {

    CheckBox cb_startUp;
    Button btn_startStopManually;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cb_startUp = (CheckBox) findViewById(R.id.cb_startup);
        btn_startStopManually = (Button) findViewById(R.id.btn_startStopManually);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // チェックボックスにPreferenceの値を反映する
        SharedPreferences pref = getPref();
        cb_startUp.setChecked(pref.getBoolean("StartAfterBoot", false));

        // サービスの実行状況に応じて、ボタンのキャプションを変更する
        if (isKeeperRunning()) {
            btn_startStopManually.setText(R.string.start_manually);
        } else {
            btn_startStopManually.setText(R.string.stop_manually);
        }

        // ボタンを押した時の処理
        //   ボタンがひとつしかないので無名関数にする
        btn_startStopManually.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(getApplicationContext(), SensorManagerService.class);
            @Override
            public void onClick(View v) {
                if (isKeeperRunning()) {
                    startService(intent);
                    btn_startStopManually.setText(R.string.stop_manually);
                } else {
                    stopService(intent);
                    btn_startStopManually.setText(R.string.start_manually);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // チェックボックスの値をPreferenceに反映する
        SharedPreferences.Editor editor = getPref().edit();
        editor.putBoolean("StartAfterBoot", cb_startUp.isChecked());
        editor.commit();
    }

    /**
     * SharedPreferencesを取得する。
     * @return SharedPreferences型、プリファレンスのインスタンス
     */
    private SharedPreferences getPref() {
        return getSharedPreferences("ScreenKeeper_pref", MODE_PRIVATE);
    }

    /**
     * SensorManagerServiceが実行中かどうかを判断する。
     * @return boolean型、実行中ならtrue、停止中ならfalseを返す。
     */
    private boolean isKeeperRunning() {
        boolean ret = false;

        String svcname = SensorManagerService.class.getCanonicalName();
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo info : services) {
            if (svcname.equals(info.service.getClassName())) {
                ret = true;
            } else {
                ret = false;
            }
        }

        return ret;
    }
}
