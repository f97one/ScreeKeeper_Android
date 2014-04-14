package net.formula97.android.screenkeeper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;

/**
 * Created by HAJIME on 14/03/14.
 */
public class SensorManagerService extends Service implements SensorEventListener {

	public static final int NOTIFICATION_ID = 0x80869821;

    private Handler mHandler;
    private SensorManager sensorManager;

	public float[] getGravity() {
		return gravity;
	}

	private void setGravity(float[] gravity) {
		this.gravity = gravity;
	}

	public float[] getMagnetic() {
		return magnetic;
	}

	private void setMagnetic(float[] magnetic) {
		this.magnetic = magnetic;
	}

	public float[] getInclinationMatrix() {
		return inclinationMatrix;
	}

	private void setInclinationMatrix(float[] inclinationMatrix) {
		this.inclinationMatrix = inclinationMatrix;
	}

	public float[] getRotationMatrix() {
		return rotationMatrix;
	}

	private void setRotationMatrix(float[] rotationMatrix) {
		this.rotationMatrix = rotationMatrix;
	}

	public float[] getRemappedRotation() {
		return remappedRotation;
	}

	private void setRemappedRotation(float[] remappedRotation) {
		this.remappedRotation = remappedRotation;
	}

	public float[] getAttitude() {
		return attitude;
	}

	private void setAttitude(float[] attitude) {
		this.attitude = attitude;
	}

	// 角度計算用、加速度＆地磁気
	float[] gravity = new float[3];
	float[] magnetic = new float[3];
	float[] inclinationMatrix = new float[16];
	float[] rotationMatrix = new float[16];
	float[] remappedRotation = new float[16];
	float[] attitude = new float[3];

	// WAKE_LOCKのフィールド
	PowerManager.WakeLock lock;

	/**
	 * 地磁気センサーの有効／無効の状態を取得する。
	 *
	 * @return boolean型、trueなら有効、falseなら無効。
	 */
	public boolean isMagSensorFlag() {
		return magSensorFlag;
	}

	/**
	 * 地磁気センサーの有効／無効を示すフラグを変更する。
	 *
	 * @param magSensorFlag boolean型、
	 */
	void setMagSensorFlag(boolean magSensorFlag) {
		this.magSensorFlag = magSensorFlag;
		Log.d("MagSensor", "System has Sensor.TYPE_MAGNETIC_FIELD.");
	}

	/**
	 * 地磁気センサーの有効／無効を示すフラグ。
	 */
	private boolean magSensorFlag;

	/**
	 * 加速度センサーの状態を取得する。
	 *
	 * @return boolean型、有効ならtrue、無効ならfalseを返す。
	 */
	public boolean isAccSensorFlag() {
		return accSensorFlag;
	}

	/**
	 * 加速度センサーの有効／無効を示すフラグを変更する。
	 *
	 * @param accSensorFlag boolean型、有効ならtrueを、無効ならfalseをセットする
	 */
	void setAccSensorFlag(boolean accSensorFlag) {
		this.accSensorFlag = accSensorFlag;
		Log.d("AccSensor", "System has Sensor.TYPE_ACCELEROMETER.");
	}

	/**
	 * 加速度センサーの有効／無効を示すフラグ。
	 */
	private boolean accSensorFlag;

    final ScreenReceiver screenReceiver = new ScreenReceiver();

	/**
	 * ディスプレイの点灯／消灯状態を取得する。
	 *
	 * @return boolean型、点灯中ならtrueを、消灯中ならfalseを返す。
	 */
    public boolean isScreenOn() {
        return isScreenOn;
    }

	/**
	 * ディスプレイの点灯／消灯を示すフラグを変更する。
	 *
	 * @param isScreenOn boolean型、点灯ならtrueを、消灯ならfalseをセットする
	 */
    public void setScreenOn(boolean isScreenOn) {
        this.isScreenOn = isScreenOn;

        String onoff = isScreenOn ? "ON" : "OFF";
        Log.d("SensorManagerService#setScreenOn()", "Received Intent.ACTION_SCREEN_" + onoff);
    }

	/**
	 * ディスプレイの点灯／消灯を保持するフラグ。
	 */
    boolean isScreenOn;

	/**
	 * 継承されたonBind。
	 *
	 * @param intent
	 * @return
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     * <p/>
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     * <p/>
     * <p>If you need your application to run on platform versions prior to API
     * level 5, you can use the following model to handle the older {@link #onStart}
     * callback in that case.  The <code>handleCommand</code> method is implemented by
     * you as appropriate:
     * <p/>
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.java
     * start_compatibility}
     * <p/>
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link android.os.AsyncTask}.</p>
     *
     * @param intent  The Intent supplied to {@link android.content.Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.  Currently either
     *                0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     * @see #stopSelfResult(int)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // 傾きセンサーの登録
        //     Sensor.TYPE_ORIENTATIONがAPI 8から非推奨になったので、加速度センサー(Sensor.TYPE_ACCELEROMETER)と
        //   地磁気センサー(Sensor.TYPE_MAGNETIC_FIELD)を組み合わせて、角度を得る必要がある。
        //     センサー感度（＝遅延許容量）は、一般UI向け(SensorManager.SENSOR_DELAY_UI)にする。
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            // 加速度センサー(Sensor.TYPE_ACCELEROMETER)の登録
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                setAccSensorFlag(
						sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
				);
            }
            // 地磁気センサー(Sensor.TYPE_MAGNETIC_FIELD)の登録
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                setMagSensorFlag(
						sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
				);
            }
        }

        // スクリーン点灯／消灯を検知するレシーバーを登録
        //   AndroidManifest.xmlに書く方法では、ブロードキャストがキャッチできないので、プログラムで
        //   動的にフィルタとレシーバーを登録する。
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(screenReceiver, filter);

		// サービス稼働時のスクリーン点灯状況を保存
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		setScreenOn(pm.isScreenOn());

		// 最初にWAKE_LOCKのステートを取得しておく
		lock = getWakeLockState();

        return START_STICKY_COMPATIBILITY;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
	 *
	 * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (accSensorFlag || magSensorFlag) {
            sensorManager.unregisterListener(this);
            setAccSensorFlag(false);
            setMagSensorFlag(false);
        }

        // スクリーンの点灯／消灯を検知するレシーバーの削除
        this.unregisterReceiver(screenReceiver);
		dismissNotification();
    }

	/**
	 * 通知領域にアイコンと現在の状態を表示する。
	 *
	 * @param isAvailable boolean型、現在WAKE_LOCKが有効ならtrue、無効ならfalseをセットする
	 */
	@SuppressWarnings("deprecated")
    void showNotification(boolean isAvailable) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher);
		builder.setOngoing(true);

        if (isAvailable) {
            builder.setContentText(getString(R.string.activated));
        } else {
            builder.setContentText(getString(R.string.not_activated));
        }

		// API 16以上だと表示のためのメソッドが変わっているので、それに対応させる。
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			nm.notify(NOTIFICATION_ID, builder.getNotification());
		} else {
			nm.notify(NOTIFICATION_ID, builder.build());
		}
    }

	/**
	 * 通知領域の表示を消去する。
	 */
	void dismissNotification() {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}

    /**
     * Called when sensor values have changed.
     * <p>See {@link android.hardware.SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link android.hardware.SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link android.hardware.SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Preferenceの最小角度と最大角度を取得する
        SharedPreferences pref = getSharedPreferences(Consts.Prefs.NAME, MODE_PRIVATE);
        int minPitch = pref.getInt(Consts.Prefs.MINIMUM_PITCH, Consts.Prefs.DEFAULT_MIN_PITCH);
        int maxPitch = pref.getInt(Consts.Prefs.MAXIMUM_PITCH, Consts.Prefs.DEFAULT_MAX_PITCH)
				+ Consts.Prefs.MAX_PITCH_OFFSET;

        // 実際のコールバックに対する処理
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                setGravity(event.values.clone());
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                setMagnetic(event.values.clone());
                break;
        }

        if (magnetic != null && gravity != null) {
			boolean result = SensorManager.getRotationMatrix(
					rotationMatrix,
					inclinationMatrix,
					getGravity(),
					getMagnetic());
			if (BuildConfig.DEBUG) {
				if (result) {
					Log.v("onSensorChanged", "calculate succeeded.");
				} else {
					Log.w("onSensorChanged", "calculate failed.");
				}
			}

			SensorManager.remapCoordinateSystem(getRotationMatrix(),
					SensorManager.AXIS_X,
					SensorManager.AXIS_Z,
					remappedRotation);
            SensorManager.getOrientation(getRemappedRotation(), attitude);

            // 現在のピッチがPreferenceの設定値以内の場合は、端末のスリープ設定を解除する
			int currentAzimuth = (int) Math.floor(Math.toDegrees(getAttitude()[0]));
            int currentPitch = (int) Math.floor(Math.toDegrees(getAttitude()[1]));
			int currentRoll = (int) Math.floor(Math.toDegrees(getAttitude()[2]));

            if (currentPitch >= minPitch && currentPitch <= maxPitch) {
                if (isScreenOn()) {
                    disableSleep();
                }
            } else {
                enableIntoSleep();
            }

			// 現在の傾きセンサー値を表示
			if (BuildConfig.DEBUG) {
				Log.d("onSensorChanged", "Current Azimuth=" + String.valueOf(currentAzimuth) +
						", Pitch=" + String.valueOf(currentPitch) +
						", Roll=" + String.valueOf(currentRoll) + "\n" +
						"raw values = {" + String.valueOf(attitude[0]) + "/" +
						String.valueOf(attitude[1]) + "/" +
						String.valueOf(attitude[2]) + "}");
			}
		}
    }

    /**
     * Called when the accuracy of a sensor has changed.
     * <p>See {@link android.hardware.SensorManager SensorManager}
     * for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 端末のスリープを無効にする。
     */
    void disableSleep() {
		SharedPreferences pref = getSharedPreferences(Consts.Prefs.NAME, MODE_PRIVATE);
		int timeout = pref.getInt(Consts.Prefs.ACQUIRE_TIMEOUT, Consts.Prefs.DEFAULT_ACQUIRE_TIMEOUT) * 1000;

		// スクリーン点灯時のみ処理
		if (isScreenOn()) {
			//lock = getWakeLockState();

			// WAKE_LOCKを取得していない時だけ取得する
			if (!lock.isHeld()) {
				if (timeout != 0) {
					lock.acquire(timeout);
				} else {
					lock.acquire();
				}
				showNotification(true);
				if (BuildConfig.DEBUG) {
					Log.d(this.getClass().getName(), "Screen Lock acquired, timeout = " + String.valueOf(timeout) + "ms.");
				}
			} else {
				if (BuildConfig.DEBUG) {
					Log.i(this.getClass().getName(), "Screen lock is already held, Ignored.");
				}
			}
		}
    }

	/**
	 * WAKE_LOCKの操作権利を取得する。<br />
	 * @return PowerManager.WakeLock型、新規のWAKE_LOCK
	 */
	private PowerManager.WakeLock getWakeLockState() {
		PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		return powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
				Consts.WAKE_LOCK_TAG);
	}

	/**
     * 無効にしたスリープ無効設定を解除する。
     */
    void enableIntoSleep() {
		//lock = getWakeLockState();
		if (lock.isHeld()) {
			lock.release();
			if (BuildConfig.DEBUG) {
				Log.d(this.getClass().getName(), "Screen Lock released.");
			}
		} else {
			Log.d(this.getClass().getName(), "Screen Lock has already released.");
		}
		showNotification(false);
	}

    /**
     * スクリーンの点灯／消灯を検知するBroadcastReceiver。
     */
    private class ScreenReceiver extends BroadcastReceiver {

		/**
		 * 継承されたonReceived。<br />
		 * スクリーンの点灯状況に応じ、点灯状況を保持するフィールドを更新する。
		 *
		 * @param context
		 * @param intent
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // スクリーン点灯時
                setScreenOn(true);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // スクリーン消灯時
                setScreenOn(false);
            }
        }
    }
}
