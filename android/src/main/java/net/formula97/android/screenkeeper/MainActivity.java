package net.formula97.android.screenkeeper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    private CheckBox cb_startUp;
    private Button btn_startStopManually;
	private SeekBar sb_minimumPitch;
	private SeekBar sb_maximumPitch;
	private TextView tv_currentMinPitch;
	private TextView tv_currentMaxPitch;

	private final int MAX_PITCH_OFFSET = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cb_startUp = (CheckBox) findViewById(R.id.cb_startup);
        btn_startStopManually = (Button) findViewById(R.id.btn_startStopManually);
		sb_minimumPitch = (SeekBar) findViewById(R.id.sb_minimumPitch);
		sb_maximumPitch = (SeekBar) findViewById(R.id.sb_maximumPitch);
		tv_currentMinPitch = (TextView) findViewById(R.id.tv_currentMinPitch);
		tv_currentMaxPitch = (TextView) findViewById(R.id.tv_currentMaxPitch);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // チェックボックスにPreferenceの値を反映する
        SharedPreferences pref = getPref();
        cb_startUp.setChecked(pref.getBoolean("StartAfterBoot", false));

        final SvcUtil util = new SvcUtil(getApplicationContext());
        final String keeper = SensorManagerService.class.getCanonicalName();

        // サービスの実行状況に応じて、ボタンのキャプションを変更する
        if (util.isKeeperRunning(keeper)) {
            btn_startStopManually.setText(R.string.start_manually);
        } else {
            btn_startStopManually.setText(R.string.stop_manually);
        }

		// SeekBarにリスナーを設置
		sb_minimumPitch.setOnSeekBarChangeListener(this);
		sb_maximumPitch.setOnSeekBarChangeListener(this);

		// SeekBarの値を復元
		onProgressChanged(sb_minimumPitch, pref.getInt("MinimumPitch", 5), false);
		onProgressChanged(sb_maximumPitch, pref.getInt("MaximumPitch", 35), false);

        // ボタンを押した時の処理
        //   ボタンがひとつしかないので無名関数にする
        btn_startStopManually.setOnClickListener(new View.OnClickListener() {
            final Intent intent = new Intent(getApplicationContext(), SensorManagerService.class);
            @Override
            public void onClick(View v) {
                if (util.isKeeperRunning(keeper)) {
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

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch (seekBar.getId()) {
			case R.id.sb_minimumPitch:
				tv_currentMinPitch.setText(String.valueOf(progress));
				break;
			case R.id.sb_maximumPitch:
				// SeekBarの現在値に45を加える
				tv_currentMaxPitch.setText(String.valueOf(progress + MAX_PITCH_OFFSET));
				break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}
}
