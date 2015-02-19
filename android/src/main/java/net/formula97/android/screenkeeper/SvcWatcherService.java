package net.formula97.android.screenkeeper;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SvcWatcherService extends Service {

    public static final String BROADCAST_MSG = "net.formula97.android.screenkeeper.SET_UNBIND_SERVICE";

    private final String logTag = SvcWatcherService.class.getSimpleName();
    private final IBinder mBinder = new SvcWatcherLocalBinder();
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(logTag + "#onServiceConnected", "connected to service : name = " + name.getShortClassName());
//            SensorManagerService.SensorManagerLocalBinder binder = (SensorManagerService.SensorManagerLocalBinder) service;
//            boundPair = binder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(logTag + "#onServiceDisconnected", "disconnected from service : name = " + name.getShortClassName());

            // SensorManagerServiceを再起動する処理
            startAndBindManager();
        }
    };
    private boolean mPairBound = false;

    /**
     * バインド解除を受け付けるBroadcastReceiver
     */
    private BroadcastReceiver unbindReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(logTag + "#onReceive", "Received broadcast : " + intent.getAction());

            if (mPairBound) {
                unbindService(mConnection);
                mPairBound = false;
                Log.i(logTag + "#onResume", "SensorManagerService unbound.");
            }
        }
    };

    public SvcWatcherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(logTag + "#onBind", "bound service : intent = " + intent);
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(logTag + "#onStartCommand", "Entered onStartCommand(Intent, int, int)");

        Intent i = new Intent(this, SensorManagerService.class);
        mPairBound = bindService(i, mConnection, BIND_AUTO_CREATE);
        if (mPairBound) {
            Log.d(logTag + "#onStartCommand", "bound SensorManagerService");
        } else {
            Log.w(logTag + "#onStartCommand", "failed to bind SensorManagerService");
        }

        // バインド解除受付のBroadcastReceiverを登録
        IntentFilter filter = new IntentFilter(BROADCAST_MSG);
        registerReceiver(unbindReceiver, filter);

        return START_STICKY;
    }

    private void startAndBindManager() {
        Log.i(logTag + "startAndBindManager", "Entered startAndBindManager()");

        Intent i = new Intent(this, SensorManagerService.class);
        SvcUtil util = new SvcUtil(this);
        if (util.isServiceRunning(SensorManagerService.class.getCanonicalName())) {
            stopService(i);
        }
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
        Log.i(logTag + "#onDestroy", "Entered onDestroy()");

        if (mPairBound) {
            unbindService(mConnection);
            mPairBound = false;
        }

        // バインド解除BroadcastReceiverを登録解除
        unregisterReceiver(unbindReceiver);

        super.onDestroy();
    }

    public class SvcWatcherLocalBinder extends Binder {
        SvcWatcherService getService() {
            return SvcWatcherService.this;
        }
    }
}
