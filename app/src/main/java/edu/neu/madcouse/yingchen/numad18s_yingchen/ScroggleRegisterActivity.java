package edu.neu.madcouse.yingchen.numad18s_yingchen;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashSet;
import java.util.Set;

import edu.neu.madcouse.yingchen.numad18s_yingchen.models.User;

public class ScroggleRegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String token;
    private static Set<String> usernames = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroggle_register);
        this.setTitle("Register");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        token = FirebaseInstanceId.getInstance().getToken();

        if(usernames.isEmpty()) {
            new LoadUsernames().execute();
        }

        Button register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    public void register() {
        // [END subscribe_topics]
        final EditText editText = (EditText) findViewById(R.id.username);

        // Log and toast
        final String username = editText.getText().toString();
        DatabaseReference tokenRef = mDatabase.child("users").child(token);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user;
                if(!usernames.contains(username)) {
                    //create new user
                    if (dataSnapshot.exists()) {
                        user = dataSnapshot.getValue(User.class);
                        user.setUsername(username);
                        Toast.makeText(ScroggleRegisterActivity.this,
                                "Updated your username!", Toast.LENGTH_SHORT).show();
                    } else {
                        user = new User(username, "0", token);
                        FirebaseMessaging.getInstance().subscribeToTopic("Scroggle");
                        Toast.makeText(ScroggleRegisterActivity.this,
                                "Registered!", Toast.LENGTH_SHORT).show();
                    }
                    mDatabase.child("users").child(token).setValue(user);
                    usernames.add(username);
                } else {
                    Toast.makeText(ScroggleRegisterActivity.this,
                            "username exists! Please choose another one.", Toast.LENGTH_SHORT).show();
                }
                editText.setText("");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        tokenRef.addListenerForSingleValueEvent(eventListener);

    }

    private class LoadUsernames extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            DatabaseReference ref = mDatabase.child("users");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        User user = dsp.getValue(User.class);
                        usernames.add(user.username);
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }});
            return null;
        }
        protected void onProgressUpdate(Void... params) {
        }

        protected void onPostExecute(Void v) {
        }
    }

}
