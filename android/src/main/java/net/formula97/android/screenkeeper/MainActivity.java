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
	private SeekBar sb_acquireTimeout;
	private TextView tv_acquire_timeout;

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
		sb_acquireTimeout = (SeekBar) findViewById(R.id.sb_acquireTimeout);
		tv_acquire_timeout = (TextView) findViewById(R.id.tv_acquire_timeout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // チェックボックスにPreferenceの値を反映する
        SharedPreferences pref = getPref();
        cb_startUp.setChecked(pref.getBoolean(Consts.Prefs.START_AFTER_BOOT, false));

        final SvcUtil util = new SvcUtil(getApplicationContext());
        final String keeper = SensorManagerService.class.getCanonicalName();

        // サービスの実行状況に応じて、ボタンのキャプションを変更する
        if (util.isServiceRunning(keeper)) {
            btn_startStopManually.setText(R.string.stop_manually);
        } else {
            btn_startStopManually.setText(R.string.start_manually);
        }

		// SeekBarにリスナーを設置
		sb_minimumPitch.setOnSeekBarChangeListener(this);
		sb_maximumPitch.setOnSeekBarChangeListener(this);
		sb_acquireTimeout.setOnSeekBarChangeListener(this);

		// SeekBarの値を復元
		int currentMinProgress = pref.getInt(Consts.Prefs.MINIMUM_PITCH, Consts.Prefs.DEFAULT_MIN_PITCH);
		int currentMaxProgress = pref.getInt(Consts.Prefs.MAXIMUM_PITCH, Consts.Prefs.DEFAULT_MAX_PITCH);
		int currentAcquireTimeout = pref.getInt(Consts.Prefs.ACQUIRE_TIMEOUT, Consts.Prefs.DEFAULT_ACQUIRE_TIMEOUT);

		sb_minimumPitch.setProgress(currentMinProgress);
		sb_maximumPitch.setProgress(currentMaxProgress);
		sb_acquireTimeout.setProgress(currentAcquireTimeout);

		onProgressChanged(sb_minimumPitch, currentMinProgress, false);
		onProgressChanged(sb_maximumPitch, currentMaxProgress, false);
		onProgressChanged(sb_acquireTimeout, currentAcquireTimeout, false);

        // ボタンを押した時の処理
        //   ボタンがひとつしかないので無名関数にする
        btn_startStopManually.setOnClickListener(new View.OnClickListener() {
			final Intent intent = new Intent(getApplicationContext(), SensorManagerService.class);

			@Override
			public void onClick(View v) {
				if (util.isServiceRunning(keeper)) {
					stopService(intent);
					btn_startStopManually.setText(R.string.start_manually);
				} else {
					startService(intent);
					btn_startStopManually.setText(R.string.stop_manually);
				}
			}
		});
    }

    @Override
    protected void onPause() {
        super.onPause();

        // チェックボックスの値をPreferenceに反映する
        SharedPreferences.Editor editor = getPref().edit();

        editor.putBoolean(Consts.Prefs.START_AFTER_BOOT, cb_startUp.isChecked());

		// SeekBarの値をPreferenceに反映する
		editor.putInt(Consts.Prefs.MINIMUM_PITCH, sb_minimumPitch.getProgress());
		editor.putInt(Consts.Prefs.MAXIMUM_PITCH, sb_maximumPitch.getProgress());
		editor.putInt(Consts.Prefs.ACQUIRE_TIMEOUT, sb_acquireTimeout.getProgress());

		editor.commit();
	}

    /**
     * SharedPreferencesを取得する。
     * @return SharedPreferences型、プリファレンスのインスタンス
     */
    private SharedPreferences getPref() {
        return getSharedPreferences(Consts.Prefs.NAME, MODE_PRIVATE);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch (seekBar.getId()) {
			case R.id.sb_minimumPitch:
				tv_currentMinPitch.setText(String.valueOf(progress));
				break;
			case R.id.sb_maximumPitch:
				// SeekBarの現在値に45を加える
				tv_currentMaxPitch.setText(String.valueOf(progress + Consts.Prefs.MAX_PITCH_OFFSET));
				break;
			case R.id.sb_acquireTimeout:
				// 値が0なら「No Timeout」と表示する
				if (sb_acquireTimeout.getProgress() == 0) {
					tv_acquire_timeout.setText(getString(R.string.no_timeout));
				} else {
					String tag = String.valueOf(sb_acquireTimeout.getProgress())
							+ getString(R.string.seconds);
					tv_acquire_timeout.setText(tag);
				}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}
}
