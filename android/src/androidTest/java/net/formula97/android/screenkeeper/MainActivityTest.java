package net.formula97.android.screenkeeper;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.test.SingleLaunchActivityTestCase;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.robotium.solo.Solo;

public class MainActivityTest extends SingleLaunchActivityTestCase<MainActivity> {

    public static final String APP_PACKAGE="net.formula97.android.screenkeeper";

    private Solo solo;

    public MainActivityTest() {
        super(APP_PACKAGE, MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        solo.finishOpenedActivities();
    }

    public void test001_OptionsMenu() throws Throwable {

        final int minPitch = 15;
        final int maxPitch = 25;
        final int timeout = 60;

        // UIの設定を変える
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 「起動時から有効」オン
                getCbStartup().setChecked(true);
                // 最小角 15
                getSbMin().setProgress(minPitch);
                getActivity().onProgressChanged(getSbMin(), minPitch, false);
                // 最大角 25 (+ 45 = 70)
                getSbMax().setProgress(maxPitch);
                getActivity().onProgressChanged(getSbMax(), maxPitch, false);
                // タイムアウト60
                getSbTimeout().setProgress(timeout);
                getActivity().onProgressChanged(getSbTimeout(), timeout, false);
            }
        });
        getInstrumentation().waitForIdleSync();

        // Robotium SoloでOptionsMenuをクリックする
        solo.clickOnMenuItem(getActivity().getString(R.string.restore_default));
        solo.sleep(1000);

        Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(MessageDialogs.FRAGMENT_KEY);

        assertNotNull("MessageDialogのDialogFragmentが取得できている", fragment);
        assertTrue(fragment instanceof MessageDialogs);
        assertTrue(((MessageDialogs) fragment).getShowsDialog());

        final AlertDialog ad = (AlertDialog) ((MessageDialogs) fragment).getDialog();

        assertNotNull("PositiveButtonを持っている", ad.getButton(DialogInterface.BUTTON_POSITIVE));
        assertNotNull("NegativeButtonを持っている", ad.getButton(DialogInterface.BUTTON_NEGATIVE));
//        assertNull("NeutralButtonは持たない", ad.getButton(DialogInterface.BUTTON_NEUTRAL));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ad.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();

        int currentMin = Integer.parseInt(getCurrentMinPitch().getText().toString());
        int currentMax = Integer.parseInt(getCurrentMaxPitch().getText().toString());
        String currentTimeout = getAquireTimeout().getText().toString();

        assertEquals("最小角は15のまま", minPitch, currentMin);
        assertEquals("最大角は70のまま", maxPitch + Consts.Prefs.MAX_PITCH_OFFSET, currentMax);
        assertEquals("タイムアウトは60秒", String.valueOf(timeout) + getActivity().getString(R.string.seconds), currentTimeout);

        fragment = getActivity().getFragmentManager().findFragmentByTag(MessageDialogs.FRAGMENT_KEY);

        assertNull("MessageDialogのDialogFragmentが消えている", fragment);

        // 再度表示してPositiveButtonを押す
        solo.clickOnMenuItem(getActivity().getString(R.string.restore_default));
        solo.sleep(1000);

        fragment = getActivity().getFragmentManager().findFragmentByTag(MessageDialogs.FRAGMENT_KEY);
        final AlertDialog ad2 = (AlertDialog) ((MessageDialogs) fragment).getDialog();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ad2.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();

        currentMin = Integer.parseInt(getCurrentMinPitch().getText().toString());
        currentMax = Integer.parseInt(getCurrentMaxPitch().getText().toString());
        currentTimeout = getAquireTimeout().getText().toString();

        assertEquals("最小角は5に戻っている", Consts.Prefs.DEFAULT_MIN_PITCH, currentMin);
        assertEquals("最大角は80に戻っている", Consts.Prefs.DEFAULT_MAX_PITCH + Consts.Prefs.MAX_PITCH_OFFSET, currentMax);
        assertEquals("タイムアウトは180秒に戻っている", String.valueOf(Consts.Prefs.DEFAULT_ACQUIRE_TIMEOUT) + getActivity().getString(R.string.seconds), currentTimeout);
        assertFalse("スタートアップ起動は無効に戻っている", getCbStartup().isChecked());

        fragment = getActivity().getFragmentManager().findFragmentByTag(MessageDialogs.FRAGMENT_KEY);

        assertNull("MessageDialogのDialogFragmentが消えている", fragment);


    }

    private CheckBox getCbStartup() {
        return (CheckBox) getActivity().findViewById(R.id.cb_startup);
    }

    private SeekBar getSbMin() {
        return (SeekBar)getActivity().findViewById(R.id.sb_minimumPitch);
    }

    private SeekBar getSbMax() {
        return (SeekBar)getActivity().findViewById(R.id.sb_maximumPitch);
    }

    private SeekBar getSbTimeout() {
        return (SeekBar)getActivity().findViewById(R.id.sb_acquireTimeout);
    }

    private TextView getCurrentMinPitch() {
        return (TextView) getActivity().findViewById(R.id.tv_currentMinPitch);
    }

    private TextView getCurrentMaxPitch() {
        return (TextView) getActivity().findViewById(R.id.tv_currentMaxPitch);
    }

    private TextView getAquireTimeout() {
        return (TextView) getActivity().findViewById(R.id.tv_acquire_timeout);
    }
}