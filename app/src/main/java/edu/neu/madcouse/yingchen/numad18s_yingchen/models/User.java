package edu.neu.madcouse.yingchen.numad18s_yingchen.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class User {
    public String username;
    public String score;
    public String datePlayed;
    public String token;
    public Map<String, Game> games;


    public User(String username, String score, String token){
        this.username = username;
        this.score = score;
        this.datePlayed = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(new Date());;
        this.token = token;
        games = new HashMap<>();
        Game game = new Game(score, datePlayed);
        games.put("game0", game);
    }

}
