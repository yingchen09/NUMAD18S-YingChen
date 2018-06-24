package edu.neu.madcouse.yingchen.numad18s_yingchen;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;


public class ScroggleActivity extends FragmentActivity {

    static final String KEY_RESTORE = "key_restore";
    static final String PREF_RESTORE = "pref_restore";
    private ScroggleFragment gameFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroggle);
        gameFragment = (ScroggleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_scroggle);

        boolean restore = getIntent().getBooleanExtra(KEY_RESTORE, false);
        if (restore) {
            String gameData = getPreferences(MODE_PRIVATE).getString(PREF_RESTORE, null);
            if (gameData != null) {
                gameFragment.putState(gameData);
            }
        }
        Log.d("Scroggle", "restore = " + restore);
    }


    @Override
    public void onPause() {
        super.onPause();
        String gameData = gameFragment.getState();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_RESTORE, gameData).commit();
        Log.d("Scroggle", "state = " + gameData);
    }


}
