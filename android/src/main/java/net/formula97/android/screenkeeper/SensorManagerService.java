package net.formula97.android.screenkeeper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;

/**
 * Created by HAJIME on 14/03/14.
 */
public class SensorManagerService extends Service implements SensorEventListener {

    Handler mHandler;
    SensorManager sensorManager;
    boolean isMagSensor;
    boolean isAccSensor;

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link android.os.IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p/>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link android.content.Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
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
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // 傾きセンサーの登録
        //     Sensor.TYPE_ORIENTATIONがAPI 8から非推奨になったので、加速度センサー(Sensor.TYPE_ACCELEROMETER)と
        //   地磁気センサー(Sensor.TYPE_MAGNETIC_FIELD)を組み合わせて。角度を得る必要がある。
        //     センサー感度は一般UI向け(SensorManager.SENSOR_DELAY_UI)にする。
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            // 加速度センサー(Sensor.TYPE_ACCELEROMETER)の登録
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
                isMagSensor = true;
            }
            // 地磁気センサー(Sensor.TYPE_MAGNETIC_FIELD)の登録
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
                isAccSensor = true;
            }
        }

        mHandler = new Handler();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        showNotification(true, pendingIntent);

        return START_STICKY_COMPATIBILITY;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isAccSensor || isMagSensor) {
            sensorManager.unregisterListener(this);
            isMagSensor = false;
            isAccSensor = false;
        }
    }

    public void showNotification(boolean isAvailable, PendingIntent intent) {
        Context ctx = getApplicationContext();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher);

        if (isAvailable) {
            builder.setContentText(getString(R.string.activated));
        } else {
            builder.setContentText(getString(R.string.not_activated));
        }
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
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    /**
     * Called when the accuracy of a sensor has changed.
     * <p>See {@link android.hardware.SensorManager SensorManager}
     * for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
