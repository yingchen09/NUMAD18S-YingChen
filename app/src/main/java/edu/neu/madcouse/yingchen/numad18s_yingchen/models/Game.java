package edu.neu.madcouse.yingchen.numad18s_yingchen.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Game {
    public String score;
    public String datePlayed;

    public Game() {
    }

    public Game(String score) {
        this.score = score;
        this.datePlayed = new SimpleDateFormat("yyyy.MM.dd", Locale.US).format(new Date());
    }
}
