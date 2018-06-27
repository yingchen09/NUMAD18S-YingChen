package edu.neu.madcouse.yingchen.numad18s_yingchen;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ScroggleTile {

    private final ScroggleFragment gameFragment;
    private ScroggleTile mSubTiles[];
    private View mView;
    private String letter;
    private boolean isChosen;
    private boolean isEmpty;

    public ScroggleTile(ScroggleFragment gameFragment) {
        this.gameFragment = gameFragment;
        this.isChosen = false;
        this.isEmpty = false;
    }

    public boolean getIsEmpty(){
        return this.isEmpty;
    }

    public void setIsEmpty(Boolean isEmpty){
        this.isEmpty = isEmpty;
    }

    public void setSubTiles(ScroggleTile[] mSubTiles) {
        this.mSubTiles = mSubTiles;
    }

    public void setView(View view) {
        this.mView = view;
    }

    public String getLetter(){
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public View getView(){
        return mView;
    }

    public void setChosen(boolean bool){
        this.isChosen = bool;
    }

    public boolean getIsChosen(){
        return isChosen;
    }


    public void updateDrawableState() {
        if (mView == null) return;
        int level = getLevel();
        boolean chosen = getIsChosen();
        if (mView.getBackground() != null) {
            if(chosen == true) {
                mView.getBackground().setLevel(level);
            } else {
                mView.getBackground().setLevel(level);
            }
        }
        if (mView instanceof Button) {
            mView.getBackground().setLevel(level);
        }
    }

    private int getLevel() {
        int level;
        if(getIsChosen()){
            level = R.drawable.letter_green;
        }
        else {
            level = R.drawable.letter_available;
        }

        if(mView instanceof Button){
            level = R.drawable.letter_gray;
        }
        return level;
    }

    public void animate() {
        Animator animator = AnimatorInflater.loadAnimator(gameFragment.getActivity(),
                R.animator.scroggle);
        if (getView() != null) {
            animator.setTarget(getView());
            animator.start();
        }
    }
    
    

}
