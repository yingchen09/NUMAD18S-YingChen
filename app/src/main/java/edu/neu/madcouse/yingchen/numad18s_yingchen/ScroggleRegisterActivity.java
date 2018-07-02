package edu.neu.madcouse.yingchen.numad18s_yingchen;

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

import edu.neu.madcouse.yingchen.numad18s_yingchen.models.User;

public class ScroggleRegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroggle_register);
        this.setTitle("Register");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        token = FirebaseInstanceId.getInstance().getToken();

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
        DatabaseReference userNameRef = mDatabase.child("users").child(username);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    //create new user
                    User user = new User(username, "0", token);
                    mDatabase.child("users").child(user.username).setValue(user);
                    FirebaseMessaging.getInstance().subscribeToTopic("Scroggle");
                    Toast.makeText(ScroggleRegisterActivity.this,
                            "Registered!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ScroggleRegisterActivity.this,
                            "username exists! Please choose another one.", Toast.LENGTH_SHORT).show();
                }
                editText.setText("");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(eventListener);

    }


}
