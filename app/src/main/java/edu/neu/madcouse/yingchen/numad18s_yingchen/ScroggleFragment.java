package edu.neu.madcouse.yingchen.numad18s_yingchen;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScroggleFragment extends Fragment {


    public ScroggleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scroggle, container, false);
    }


    public void putState(String data) {

    }

    public String getState() {
        return null;
    }
}