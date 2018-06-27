package edu.neu.madcouse.yingchen.numad18s_yingchen;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScroggleControlFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_scroggle_control, container, false);
        final View resume = rootView.findViewById(R.id.resume);
        final View pause = rootView.findViewById(R.id.pause);
        final View mute = rootView.findViewById(R.id.mute);
        final View unmute = rootView.findViewById(R.id.unmute);
        resume.setVisibility(View.GONE);
        unmute.setVisibility(View.GONE);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resume.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
                ((ScroggleActivity) getActivity()).gameFragment.onScrogglePause();
            }
        });
        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause.setVisibility(View.VISIBLE);
                resume.setVisibility(View.GONE);
                ((ScroggleActivity) getActivity()).gameFragment.onScroggleResume();
            }
        });

        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mute.setVisibility(View.GONE);
                unmute.setVisibility(View.VISIBLE);
                ((ScroggleActivity) getActivity()).gameFragment.mute();
            }
        });

        unmute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unmute.setVisibility(View.GONE);
                mute.setVisibility(View.VISIBLE);
                ((ScroggleActivity) getActivity()).gameFragment.mute();
            }
        });

        return rootView;
    }

}
