package edu.neu.madcouse.yingchen.numad18s_yingchen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.PriorityQueue;

import edu.neu.madcouse.yingchen.numad18s_yingchen.models.Game;

public class ScoreBoardActivity extends AppCompatActivity {

    private PriorityQueue<Game> pq = new PriorityQueue<>();
    private String token;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);

        token = FirebaseInstanceId.getInstance().getToken();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }


    

}
