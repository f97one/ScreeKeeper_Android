package net.formula97.android.screenkeeper;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SvcWatcherService extends Service {

    private final String logTag = SvcWatcherService.class.getSimpleName();

    public class SvcWatcherLocalBinder extends Binder {
        SvcWatcherService getService() {
            return SvcWatcherService.this;
        }
    }

    private final IBinder mBinder = new SvcWatcherLocalBinder();

    public SvcWatcherService() {
    }

    private SensorManagerService boundPair;
    private boolean mPairBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(logTag + "#onServiceConnected", "connected to service : name = " + name.getShortClassName());
//            boundPair = ((SensorManagerService.SensorManagerLocalBinder)service).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(logTag + "#onServiceDisconnected", "disconnected from service : name = " + name.getShortClassName());

            boundPair = null;

            // SensorManagerServiceを再起動する処理
            startAndBindManager();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(logTag + "#onBind", "bound service : intent = " + intent);
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d(logTag + "#onStartCommand", "Entered onStartCommand(Intent, int, int)");

        SvcUtil util = new SvcUtil(this);
        if (!util.isServiceRunning(SensorManagerService.class.getCanonicalName())) {
            startAndBindManager();
        }
        return START_STICKY;
    }

    private void startAndBindManager() {
        Log.d(logTag + "startAndBindManager", "Entered startAndBindManager()");

        Intent i = new Intent(this, SensorManagerService.class);
        startService(i);
        mPairBound = bindService(i, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(logTag + "#onDestroy", "Entered onDestroy()");

        unbindService(mConnection);
        mPairBound = false;

        super.onDestroy();
    }
}
