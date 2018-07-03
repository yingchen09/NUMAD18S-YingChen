package edu.neu.madcouse.yingchen.numad18s_yingchen;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.neu.madcouse.yingchen.numad18s_yingchen.models.Game;
import edu.neu.madcouse.yingchen.numad18s_yingchen.models.User;

public class LeaderBoardActivity extends AppCompatActivity {

    private String token;
    private DatabaseReference mDatabase;
    private List<User> allUsers = new ArrayList<>();
    private Map<Integer, TextView[]> texts = new HashMap<>();
    private Map<Integer, Button> buttons = new HashMap<>();

    private static final String TAG = LeaderBoardActivity.class.getSimpleName();

    private static final String SERVER_KEY = "key=AAAAzfNE7ro:APA91bF2FxRBR0XKv_Z1IaencnXQ-yHayNii_HNnTmj2wBjpg_y5PoE6IlZfHJXeaQH9PczVuucqfVEo7Dcx0R2iLx6PaS5W1Lv47bZ2SQbFZnuHoTjEdAWIHxMnvFPC47ACmQqbb32s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        token = FirebaseInstanceId.getInstance().getToken();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        TextView username1 = (TextView) findViewById(R.id.username1);
        TextView score1 = (TextView) findViewById(R.id.score1);
        TextView time1 = (TextView) findViewById(R.id.time1);
        Button congrats1 = (Button) findViewById(R.id.congrats1);
        TextView username2 = (TextView) findViewById(R.id.username2);
        TextView score2 = (TextView) findViewById(R.id.score2);
        TextView time2 = (TextView) findViewById(R.id.time2);
        Button congrats2 = (Button) findViewById(R.id.congrats2);
        TextView time3 = (TextView) findViewById(R.id.time3);
        Button congrats3 = (Button) findViewById(R.id.congrats3);
        TextView time4 = (TextView) findViewById(R.id.time4);
        Button congrats4 = (Button) findViewById(R.id.congrats4);
        TextView time5 = (TextView) findViewById(R.id.time5);
        Button congrats5 = (Button) findViewById(R.id.congrats5);
        TextView time6 = (TextView) findViewById(R.id.time6);
        Button congrats6 = (Button) findViewById(R.id.congrats6);
        TextView time7 = (TextView) findViewById(R.id.time7);
        Button congrats7 = (Button) findViewById(R.id.congrats7);
        TextView time8 = (TextView) findViewById(R.id.time8);
        Button congrats8 = (Button) findViewById(R.id.congrats8);
        TextView time9 = (TextView) findViewById(R.id.time9);
        Button congrats9 = (Button) findViewById(R.id.congrats9);
        TextView time10 = (TextView) findViewById(R.id.time10);
        Button congrats10 = (Button) findViewById(R.id.congrats10);
        TextView username3 = (TextView) findViewById(R.id.username3);
        TextView score3 = (TextView) findViewById(R.id.score3);
        TextView username4 = (TextView) findViewById(R.id.username4);
        TextView score4 = (TextView) findViewById(R.id.score4);
        TextView username5 = (TextView) findViewById(R.id.username5);
        TextView score5 = (TextView) findViewById(R.id.score5);
        TextView username6 = (TextView) findViewById(R.id.username6);
        TextView score6 = (TextView) findViewById(R.id.score6);
        TextView username7 = (TextView) findViewById(R.id.username7);
        TextView score7 = (TextView) findViewById(R.id.score7);
        TextView username8 = (TextView) findViewById(R.id.username8);
        TextView score8 = (TextView) findViewById(R.id.score8);
        TextView username9 = (TextView) findViewById(R.id.username9);
        TextView score9 = (TextView) findViewById(R.id.score9);
        TextView username10 = (TextView) findViewById(R.id.username10);
        TextView score10 = (TextView) findViewById(R.id.score10);
        congrats1.setVisibility(View.INVISIBLE);
        buttons.put(1, congrats1);
        congrats2.setVisibility(View.INVISIBLE);
        buttons.put(2, congrats2);
        congrats3.setVisibility(View.INVISIBLE);
        buttons.put(3, congrats3);
        congrats4.setVisibility(View.INVISIBLE);
        buttons.put(4, congrats4);
        congrats5.setVisibility(View.INVISIBLE);
        buttons.put(5, congrats5);
        congrats6.setVisibility(View.INVISIBLE);
        buttons.put(6, congrats6);
        congrats7.setVisibility(View.INVISIBLE);
        buttons.put(7, congrats7);
        congrats8.setVisibility(View.INVISIBLE);
        buttons.put(8, congrats8);
        congrats9.setVisibility(View.INVISIBLE);
        buttons.put(9, congrats9);
        congrats10.setVisibility(View.INVISIBLE);
        buttons.put(10, congrats10);
        for (int i = 1; i <= 10; i++) {
            texts.put(i, new TextView[3]);
        }
        texts.get(1)[0] = username1;
        texts.get(1)[1] = score1;
        texts.get(1)[2] = time1;
        texts.get(2)[0] = username2;
        texts.get(2)[1] = score2;
        texts.get(2)[2] = time2;
        texts.get(3)[0] = username3;
        texts.get(3)[1] = score3;
        texts.get(3)[2] = time3;
        texts.get(4)[0] = username4;
        texts.get(4)[1] = score4;
        texts.get(4)[2] = time4;
        texts.get(5)[0] = username5;
        texts.get(5)[1] = score5;
        texts.get(5)[2] = time5;
        texts.get(6)[0] = username6;
        texts.get(6)[1] = score6;
        texts.get(6)[2] = time6;
        texts.get(7)[0] = username7;
        texts.get(7)[1] = score7;
        texts.get(7)[2] = time7;
        texts.get(8)[0] = username8;
        texts.get(8)[1] = score8;
        texts.get(8)[2] = time8;
        texts.get(9)[0] = username9;
        texts.get(9)[1] = score9;
        texts.get(9)[2] = time9;
        texts.get(10)[0] = username10;
        texts.get(10)[1] = score10;
        texts.get(10)[2] = time10;

        DatabaseReference ref = mDatabase.child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Result will be holded Here
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    User user = dsp.getValue(User.class);
                    allUsers.add(user);
                }
                if (!allUsers.isEmpty()) {
                    Collections.sort(allUsers, new Comparator<User>(){
                        @Override
                        public int compare(User o1, User o2){
                            return Integer.valueOf(o2.topscore) - Integer.valueOf(o1.topscore);
                        }});
                    int size = allUsers.size();
                    int j = 0;
                    while(j < 10 && j < size) {
                        int index = j + 1;
                        final String tagetToken = allUsers.get(j).token;
                        if (!tagetToken.equals(token)) {
                            buttons.get(index).setVisibility(View.VISIBLE);
                            buttons.get(index).setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    congratsToDevice(tagetToken);
                                }
                            });
                        }

                        texts.get(index)[0].setText(allUsers.get(j).username);
                        texts.get(index)[1].setText(allUsers.get(j).topscore);
                        texts.get(index)[2].setText(allUsers.get(j).datePlayed);
                        j++;
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }});
    }

    public void congratsToDevice(final String targetToken) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessageToDevice(targetToken);
            }
        }).start();
    }

    /**
     * Pushes a notification to a given device-- in particular, this device,
     * because that's what the instanceID token is defined to be.
     */
    private void sendMessageToDevice(String targetToken) {
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();
        try {
            jNotification.put("title", "Scroggle");
            jNotification.put("body", "Congrats to your high score!");
            jNotification.put("sound", "default");
            jNotification.put("badge", "1");
            /*
            // We can add more details into the notification if we want.
            // We happen to be ignoring them for this demo.
            jNotification.put("click_action", "OPEN_ACTIVITY_1");
            */

            /***
             * The Notification object is now populated.
             * Next, build the Payload that we send to the server.
             */

            // If sending to a single client
            jPayload.put("to", targetToken); // CLIENT_REGISTRATION_TOKEN);

            /*
            // If sending to multiple clients (must be more than 1 and less than 1000)
            JSONArray ja = new JSONArray();
            ja.put(CLIENT_REGISTRATION_TOKEN);
            // Add Other client tokens
            ja.put(FirebaseInstanceId.getInstance().getToken());
            jPayload.put("registration_ids", ja);
            */

            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);


            /***
             * The Payload object is now populated.
             * Send it to Firebase to send the message to the appropriate recipient.
             */
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayload.toString().getBytes());
            outputStream.close();

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            final String resp = convertStreamToString(inputStream);

            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run: " + resp);
                    Toast.makeText(LeaderBoardActivity.this,"sent!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper function
     * @param is
     * @return
     */
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

}
