package edu.neu.madcouse.yingchen.numad18s_yingchen;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.madcouse.yingchen.numad18s_yingchen.models.Game;
import edu.neu.madcouse.yingchen.numad18s_yingchen.models.User;

public class ScoreBoardActivity extends AppCompatActivity {

    private String token;
    private DatabaseReference mDatabase;
    private static Map<String, Game> allGames = new HashMap<>();
    private Map<Integer, TextView[]> texts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);

        token = FirebaseInstanceId.getInstance().getToken();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        DatabaseReference ref = mDatabase.child("users").child(token);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Result will be holded Here
                User user = dataSnapshot.getValue(User.class);
                allGames.putAll(user.games);
                if (allGames.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "You have not registered to show the scoreboard. Please register.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Loading the scoreboard...",
                            Toast.LENGTH_SHORT).show();
                    TextView username1 = (TextView) findViewById(R.id.username1);
                    TextView score1 = (TextView) findViewById(R.id.score1);
                    TextView username2 = (TextView) findViewById(R.id.username2);
                    TextView score2 = (TextView) findViewById(R.id.score2);
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
                    for (int i = 1; i <= 10; i++) {
                        texts.put(i, new TextView[2]);
                    }
                    texts.get(1)[0] = username1;
                    texts.get(1)[1] = score1;
                    texts.get(2)[0] = username2;
                    texts.get(2)[1] = score2;
                    texts.get(3)[0] = username3;
                    texts.get(3)[1] = score3;
                    texts.get(4)[0] = username4;
                    texts.get(4)[1] = score4;
                    texts.get(5)[0] = username5;
                    texts.get(5)[1] = score5;
                    texts.get(6)[0] = username6;
                    texts.get(6)[1] = score6;
                    texts.get(7)[0] = username7;
                    texts.get(7)[1] = score7;
                    texts.get(8)[0] = username8;
                    texts.get(8)[1] = score8;
                    texts.get(9)[0] = username9;
                    texts.get(9)[1] = score9;
                    texts.get(10)[0] = username10;
                    texts.get(10)[1] = score10;
                    List<Game> sortedGames = sortGames();
                    int size = sortedGames.size();
                    int j = 0;
                    while (j < 10 && j < size) {
                        int index = j + 1;
                        texts.get(index)[0].setText(sortedGames.get(j).datePlayed);
                        texts.get(index)[1].setText(sortedGames.get(j).score);
                        j++;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }});

    }

    public List<Game> sortGames() {
        List<Game> res = new ArrayList<>();
        for (String key : allGames.keySet()) {
            Game game = allGames.get(key);
            res.add(game);
        }
        Collections.sort(res, new Comparator<Game>(){
            @Override
            public int compare(Game o1, Game o2){
                return Integer.valueOf(o2.score) - Integer.valueOf(o1.score);
            }});
        return res;
    }


}
