package net.formula97.android.screenkeeper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SvcWatcherService extends Service {
    public SvcWatcherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
