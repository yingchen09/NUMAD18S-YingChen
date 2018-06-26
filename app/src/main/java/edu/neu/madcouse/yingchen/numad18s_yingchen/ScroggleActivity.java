package edu.neu.madcouse.yingchen.numad18s_yingchen;

import android.media.MediaPlayer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class ScroggleActivity extends FragmentActivity {

    static final String KEY_RESTORE = "key_restore";
    static final String PREF_RESTORE = "pref_restore";
    public ScroggleFragment gameFragment;

    private int phase1Points = 0;
    private int phase2Points = 0;
    private int phase = 1;
    private String phase2Word;
    private TextView scoreView;
    private String gameData;
    private MediaPlayer mediaPlayer;
    public static boolean isResume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            phase2Word = "";
            phase = 2;
            gameData = b.getString("gameData");
        }
        setContentView(R.layout.activity_scroggle);
        gameFragment = (ScroggleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_scroggle);
        scoreView = (TextView) findViewById(R.id.score);

        boolean restore = getIntent().getBooleanExtra(KEY_RESTORE, false);
        if (restore) {
            String gameData = getPreferences(MODE_PRIVATE).getString(PREF_RESTORE, null);
            if (gameData != null) {
                gameFragment.putState(gameData);
            }
        }
        Log.d("Scroggle", "restore = " + restore);
        if (phase == 1) {
            scoreView.setText(String.valueOf(phase1Points));
        } else {
            scoreView.setText(String.valueOf(phase1Points + phase2Points));
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        gameFragment.mHandler.removeCallbacks(null);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        gameFragment.timer.cancel();

        if (isResume) {
            isResume = false;
        }
        String gameData = gameFragment.getState();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_RESTORE, gameData).commit();
        Log.d("Scroggle", "state = " + gameData);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isResume) {
            isResume = true;
            if (gameFragment.timeRemain > 0) {
                gameFragment.timer.start();
            } else {
                gameFragment.timer.start();
            }
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.backgroundmusic);
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        if (phase == 1) {
            scoreView.setText(String.valueOf(this.phase1Points));
        } else {
            scoreView.setText(String.valueOf(this.phase1Points + this.phase2Points));
        }

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        gameFragment.mHandler.removeCallbacks(null);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        gameFragment.timer.cancel();
        finish();
    }

    public void setFragmentInvisible() {
        gameFragment.getView().setVisibility(View.INVISIBLE);
    }

    public void setPhase1Points(int phase1Points) {
        this.phase1Points = phase1Points;
    }

    public void setPhase2Points(int phase2Points) {
        this.phase2Points = phase2Points;
    }

    public void stopThinking() {
        if (phase == 1) {
            scoreView.setText(String.valueOf(this.phase1Points));
        } else {
            scoreView.setText(String.valueOf(this.phase2Points + this.phase1Points));
        }
    }
}
