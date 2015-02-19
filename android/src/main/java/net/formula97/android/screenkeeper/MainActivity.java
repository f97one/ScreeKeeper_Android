package net.formula97.android.screenkeeper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener, MessageDialogs.OnButtonSelectionListener {

    private CheckBox cb_startUp;
	private SeekBar sb_minimumPitch;
	private SeekBar sb_maximumPitch;
	private TextView tv_currentMinPitch;
	private TextView tv_currentMaxPitch;
	private SeekBar sb_acquireTimeout;
	private TextView tv_acquire_timeout;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cb_startUp = (CheckBox) findViewById(R.id.cb_startup);
		sb_minimumPitch = (SeekBar) findViewById(R.id.sb_minimumPitch);
		sb_maximumPitch = (SeekBar) findViewById(R.id.sb_maximumPitch);
		tv_currentMinPitch = (TextView) findViewById(R.id.tv_currentMinPitch);
		tv_currentMaxPitch = (TextView) findViewById(R.id.tv_currentMaxPitch);
		sb_acquireTimeout = (SeekBar) findViewById(R.id.sb_acquireTimeout);
		tv_acquire_timeout = (TextView) findViewById(R.id.tv_acquire_timeout);

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // チェックボックスにPreferenceの値を反映する
        SharedPreferences pref = getPref();
        cb_startUp.setChecked(pref.getBoolean(Consts.Prefs.START_AFTER_BOOT, false));

        final SvcUtil util = new SvcUtil(getApplicationContext());
        final String keeper = SensorManagerService.class.getCanonicalName();

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

        if (!util.isServiceRunning(keeper)) {
            Intent i = new Intent(this, SensorManagerService.class);
            startService(i);
        }

        adView.resume();
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

        adView.pause();
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
    protected void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }

    @Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;

        switch (item.getItemId()) {
            case R.id.action_restore_default:
                // デフォルトに戻す確認をさせる
                MessageDialogs dialogs = MessageDialogs.newInstance(
                        getString(R.string.confirm_reset),
                        getString(R.string.confirmation),
                        MessageDialogs.SET_BOTH_BUTTON
                );
                dialogs.show(getFragmentManager(), MessageDialogs.FRAGMENT_KEY);
                break;
            case R.id.stop_service:
                // バインド解除指示のブロードキャストを投げる
                Intent i = new Intent(SvcWatcherService.BROADCAST_MSG);
                sendBroadcast(i);

                // サービス停止
                Intent svc = new Intent(this, SensorManagerService.class);
                if (stopService(svc)) {
                    Log.d(this.getClass().getSimpleName() + "#onOptionsItemSelected", "SensorManagerServiceの停止に成功");
                    Toast.makeText(this, R.string.svc_stop_successfuly, Toast.LENGTH_LONG).show();
                } else {
                    Log.w(this.getClass().getSimpleName() + "#onOptionsItemSelected", "SensorManagerServiceはすでに停止済み");
                }

                break;
            default:
                ret = super.onOptionsItemSelected(item);
        }

        return ret;
    }

    @Override
    public void onButtonSelected(String messageBody, int whichButton) {
        if (messageBody.equals(getString(R.string.confirm_reset))
                && whichButton == MessageDialogs.PRESSED_POSITIVE) {
            // 設定の初期値を復元
            cb_startUp.setChecked(false);

            sb_minimumPitch.setProgress(Consts.Prefs.DEFAULT_MIN_PITCH);
            onProgressChanged(sb_minimumPitch, Consts.Prefs.DEFAULT_MIN_PITCH, false);

            sb_maximumPitch.setProgress(Consts.Prefs.DEFAULT_MAX_PITCH);
            onProgressChanged(sb_maximumPitch, Consts.Prefs.DEFAULT_MAX_PITCH, false);

            sb_acquireTimeout.setProgress(Consts.Prefs.DEFAULT_ACQUIRE_TIMEOUT);
            onProgressChanged(sb_acquireTimeout, Consts.Prefs.DEFAULT_ACQUIRE_TIMEOUT, false);
        }
    }
}
