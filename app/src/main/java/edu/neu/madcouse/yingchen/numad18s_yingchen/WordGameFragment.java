package edu.neu.madcouse.yingchen.numad18s_yingchen;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class WordGameFragment extends Fragment {

    private static String TAG = "WordGameActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_word_game, container, false);
        View ackButton = view.findViewById(R.id.ack);
        ackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog mDialog = new Dialog(getActivity());
                mDialog.setTitle("Acknowledgement");
                mDialog.setContentView(R.layout.scroggle_acknowledgements);
                mDialog.setCancelable(true);

                TextView text = (TextView) mDialog.findViewById(R.id.scroggle_ack);
                text.setText(R.string.acknowlegements);

                Button ok_button = (Button) mDialog.findViewById(R.id.ok);
                ok_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mDialog != null)
                            mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });

        View insButton = view.findViewById(R.id.instruction);
        insButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog mDialog = new Dialog(getActivity());
                mDialog.setTitle("Instructions");
                mDialog.setContentView(R.layout.instructions);
                mDialog.setCancelable(true);

                TextView text = (TextView) mDialog.findViewById(R.id.ins);
                text.setText(R.string.ins);

                Button ok_button = (Button) mDialog.findViewById(R.id.ok);
                ok_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mDialog != null)
                            mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });

        View newgameButton = view.findViewById(R.id.newgame);
        newgameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ScroggleActivity.class);
                getActivity().startActivity(intent);
            }
        });


        return view;
    }


    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
    }

}
