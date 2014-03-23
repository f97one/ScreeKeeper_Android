package net.formula97.android.screenkeeper;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by f97one on 14/03/23.
 */
public class SvcUtil {

    Context mContext;

    private Context getContext() {
        return mContext;
    }

    private void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public SvcUtil(Context context) {
        setContext(context);
    }

    /**
     * SensorManagerServiceが実行中かどうかを判断する。
     * @return boolean型、実行中ならtrue、停止中ならfalseを返す。
     */
    public boolean isKeeperRunning(String serviceCanonicalName) {
        boolean ret = false;

        ActivityManager manager = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo info : services) {
            if (serviceCanonicalName.equals(info.service.getClassName())) {
                ret = true;
                break;
            }
        }

        return ret;
    }

}
