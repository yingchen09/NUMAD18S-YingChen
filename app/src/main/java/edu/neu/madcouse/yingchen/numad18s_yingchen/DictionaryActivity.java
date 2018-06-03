package edu.neu.madcouse.yingchen.numad18s_yingchen;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DictionaryActivity extends AppCompatActivity {

    private static String TAG = "DictionaryActivity";
    private static Map<String,ArrayList<String>> wordlist = new HashMap<String,ArrayList<String>>();
    MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        this.setTitle("Test Dictionary");

        if(wordlist.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Loading the dictionary...",
                    Toast.LENGTH_SHORT).show();
            loadWordList();
        }

        EditText editText = (EditText) findViewById(R.id.editText);

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean wordDected = false;
                String word = charSequence.toString();

                if(word.length() >= 3){
                    wordDected = wordDetect(word);
                }

                if(wordDected){
                    try {
                        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep);
                        mMediaPlayer.setVolume(0.5f, 0.5f);
                        mMediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    EditText editText = (EditText) findViewById(R.id.editText);
                    TextView textView = (TextView) findViewById(R.id.wordsTextView);

                    if(!textView.getText().toString().contains(editText.getText().toString())) {
                        textView.append(editText.getText() + "\n");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void loadWordList() {
        try {
            InputStream inputStream = getResources().getAssets().open("wordlistmap");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            wordlist = (HashMap<String, ArrayList<String>>) objectInputStream.readObject();

            objectInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean wordDetect(String word) {
        String key = word.substring(0, 3);
        if (wordlist.containsKey(key) && wordlist.get(key).contains(word)) {
            return true;
        }
        return false;
    }


    public void clear(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        TextView textView = (TextView)findViewById(R.id.wordsTextView);
        editText.setText("");
        textView.setText("");
    }

    public void launchAcknowledgments(View view) {
        Intent intent = new Intent(this, AcknowledgementsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.v(TAG, "onRestart()");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()");
        super.onStop();
    }
}
