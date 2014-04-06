package net.formula97.android.screenkeeper;

/**
 * Created by f97one on 14/03/24.
 */
class Consts {
	static class Prefs {
		static final String NAME = "ScreenKeeper_pref";
		static final String START_AFTER_BOOT = "StartAfterBoot";
		static final String MINIMUM_PITCH = "MinimumPitch";
		static final String MAXIMUM_PITCH = "MaximumPitch";
		static final int MAX_PITCH_OFFSET = 45;
		static final int DEFAULT_MAX_PITCH = 35;
		static final int DEFAULT_MIN_PITCH = 5;
	}

	static final String WAKE_LOCK_TAG = "net.formula97.android.screenkeeper.ACTION_SCREEN_KEEP";
}
