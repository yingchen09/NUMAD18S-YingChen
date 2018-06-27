package edu.neu.madcouse.yingchen.numad18s_yingchen;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScroggleFragment extends Fragment {

    public ScroggleCountDownTimer timer;
    public long timeRemain;
    static final int INTERVAL = 1000;
    static final int TOTAL_TIME = 90000;

    private boolean isResume = ScroggleActivity.isResume;
    private ScroggleTile mEntireBoard = new ScroggleTile(this);
    private ScroggleTile mLargeTiles[] = new ScroggleTile[9];
    private ScroggleTile mSmallTiles[][] = new ScroggleTile[9][9];

    private static int mPhase1Points = 0;
    private static int mPhase2Points = 0;

    private int mLastLarge;
    private int mLastSmall;
    private int phase = 1;

    static private int mLargeIds[] = {R.id.large1, R.id.large2, R.id.large3,
            R.id.large4, R.id.large5, R.id.large6, R.id.large7, R.id.large8,
            R.id.large9,};

    static private int mSmallIds[] = {R.id.small1, R.id.small2, R.id.small3,
            R.id.small4, R.id.small5, R.id.small6, R.id.small7, R.id.small8,
            R.id.small9,};

    private List<String> nineLetterWords = new ArrayList<>();
    private List<String> boardWords = new ArrayList<>();
    private Map<String,ArrayList<String>> wordlist = new HashMap<>();
    private Vibrator vibrator;
    private String phase2Word;
    public MediaPlayer mediaPlayer;
    private String gameData;
    private View rootView;
    private TextView timerText;

    private List<List<Integer>> letterPositions = new ArrayList<>();
    private char[][] letterState = new char[9][9];

    private Set<ScroggleTile> phase1Available = new HashSet<>();
    private Set<ScroggleTile> phase2Available = new HashSet<>();

    private Set<ScroggleTile> nextMoves1 = new HashSet<>();
    private Set<ScroggleTile> nextMoves2 = new HashSet<>();

    private String[] phase2Words = new String[100];
    private Map<Integer, String> formedWords = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> smallIdMap1 = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> smallIdMap2 = new HashMap<>();

    private boolean[] isWord1 = new boolean[9];
    private boolean[] isWord2 = new boolean[9];
    private int letterScore1 = 0;
    private int letterScore2;
    private int count = 0;
    private int[] formedWordsScores1 = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private String word = "";
    public Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (wordlist.isEmpty()) {
            new LoadWordList().execute();
        }

        Bundle b = getActivity().getIntent().getExtras();

        if (b == null) {
            loadBoardWords();
            placeLettersInBoard();
        }

        if (b != null) {
            phase2Word = "";
            phase = 2;
            gameData = b.getString("gameData");
        }

        initGame();

        vibrator = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.backgroundmusic);
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void loadBoardWords() {
        try {
            InputStream inputStream = getResources().getAssets().open("allnineletterwords");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            nineLetterWords = (ArrayList<String>) objectInputStream.readObject();
            objectInputStream.close();

            int total = nineLetterWords.size();

            for (int i = 0; i < 9; i++) {
                Random rand = new Random();
                int index = rand.nextInt(total);
                String word = nineLetterWords.get(index);
                boardWords.add(word);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initGame() {
        Log.d("ScroggleFragment", "init game");
        mEntireBoard = new ScroggleTile(this);
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large] = new ScroggleTile(this);
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small] = new ScroggleTile(this);
            }
            mLargeTiles[large].setSubTiles(mSmallTiles[large]);
        }
        mEntireBoard.setSubTiles(mLargeTiles);
        mLastLarge = -1;
        mLastSmall = -1;
        if (phase == 1) {
            setAvailableFromLastMove(mLastSmall);
            setValidNextMove1(mLastLarge, mLastSmall);
        } else {
            setAvailPhaseTwo(mLastSmall);
            setValidNextMove2(mLastLarge, mLastSmall);
        }
    }

    private void placeLettersInBoard() {
        List<List<Integer>> positions = new ArrayList<>();
        positions.addAll(Arrays.asList(
                Arrays.asList(0, 1, 2, 5, 8, 4, 3, 6, 7),
                Arrays.asList(0, 3, 6, 7, 5, 2, 1, 4, 8),
                Arrays.asList(0, 1, 4, 6, 3, 7, 8, 5, 2),
                Arrays.asList(1, 5, 8, 7, 6, 3, 0, 4, 2),
                Arrays.asList(1, 0, 3, 6, 4, 2, 5, 7, 8),
                Arrays.asList(2, 5, 7, 8, 4, 6, 3, 1, 0),
                Arrays.asList(2, 5, 1, 0, 3, 7, 6, 4, 8),
                Arrays.asList(3, 0, 4, 6, 7, 8, 5, 1, 2),
                Arrays.asList(3, 6, 4, 8, 7, 5, 2, 1, 0),
                Arrays.asList(5, 8, 4, 6, 7, 3, 0, 1, 2),
                Arrays.asList(6, 7, 8, 5, 1, 0, 3, 4, 2),
                Arrays.asList(6, 3, 1, 0, 4, 2, 5, 8, 7),
                Arrays.asList(5, 2, 4, 0, 1, 3, 6, 7, 8),
                Arrays.asList(4, 1, 2, 5, 8, 7, 6, 3, 0),
                Arrays.asList(5, 2, 1, 0, 3, 4, 6, 7, 8),
                Arrays.asList(8, 7, 6, 3, 4, 5, 2, 1, 0),
                Arrays.asList(6, 4, 3, 0, 1, 2, 5, 8, 7),
                Arrays.asList(8, 4, 0, 3, 6, 7, 5, 2, 1),
                Arrays.asList(4, 0, 1, 3, 6, 7, 8, 5, 2)
        ));

        for (int i = 0; i < 9; i++) {
            Random random = new Random();
            int index = random.nextInt(positions.size());
            letterPositions.add(positions.get(index));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.large_board, container, false);
        rootView = view;
        initViews(view);
        if (gameData != "" && gameData != null) {
            putState(gameData);
        } else {
            startGame(view);
        }
        updateAllTiles();

        if (phase == 2) {
            if (timeRemain < 1000) {
                timer = new ScroggleCountDownTimer(TOTAL_TIME, INTERVAL);
            } else {
                timer = new ScroggleCountDownTimer(timeRemain, INTERVAL);
            }
        } else {
            if (timeRemain > 0) {
                timer = new ScroggleCountDownTimer(timeRemain, INTERVAL);
            } else {
                timer = new ScroggleCountDownTimer(TOTAL_TIME, INTERVAL);
            }
        }
        timerText = (TextView) view.findViewById(R.id.timer);
        timer.start();
        return view;
    }

    private void startGame(View view) {
        mEntireBoard.setView(view);
        for (int large = 0; large < 9; large++) {
            View outer = view.findViewById(mLargeIds[large]);
            mLargeTiles[large].setView(outer);
            List<Integer> pos = letterPositions.get(large);
            String s = boardWords.get(large);
            for (int small = 0; small < 9; small++) {
                int i = pos.get(small);
                Button inner = (Button) outer.findViewById(mSmallIds[i]);
                inner.setText(String.valueOf(s.charAt(small)));
                ScroggleTile smallTile = mSmallTiles[large][i];
                letterState[large][i] = s.charAt(small);
                smallTile.setLetter(String.valueOf(s.charAt(small)));
            }
        }
    }

    private void initViews(View view) {

        mEntireBoard.setView(view);

        if(phase == 2) {
            for (int l = 0; l < 9; l++) {
                View o = view.findViewById(mLargeIds[l]);
                for (int s = 0; s < 9; s++) {
                    ScroggleTile tinyTile = mSmallTiles[l][s];
                    if (tinyTile.getIsChosen()) {
                        if (!isWord1[l]) {
                            Button but = (Button) o.findViewById(mSmallIds[s]);
                            tinyTile.setView(but);
                            but.setClickable(false);
                            but.setEnabled(false);
                        }
                    }
                }
            }
        }

        for (int large = 0; large < 9; large++) {
            final View outer = view.findViewById(mLargeIds[large]);
            mLargeTiles[large].setView(outer);
            for (int small = 0; small < 9; small++) {
                final int wordLarge = large;
                final int wordSmall = small;
                final ScroggleTile smallTile = mSmallTiles[large][small];
                final Button inner = (Button) outer.findViewById(mSmallIds[small]);
                smallTile.setView(inner);

                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        smallTile.animate();
                        if (phase == 1) {
                            if (isValidMove(smallTile)) {
                                if (!isChosen(smallTile)) {
                                    smallTile.setChosen(true);
                                    inner.setBackground(getResources().getDrawable(R.drawable.letter_green));
                                    formWord(String.valueOf(smallTile.getLetter()), wordLarge, wordSmall);
                                } else {
                                    smallTile.setChosen(false);
                                    word = deleteLastChar(formedWords.get(wordLarge));
                                    formedWords.put(wordLarge, word);
                                    inner.setBackground(getResources().getDrawable(R.drawable.letter_available));
                                }
                                vibrator.vibrate(15);
                                showWord();
                                mLastLarge = wordLarge;
                                mLastSmall = wordSmall;
                                setValidNextMove1(mLastLarge, mLastSmall);
                            }

                        } else {
                            setValidNextMove2(mLastLarge, mLastSmall);
                            if (isValidPhase2Move(smallTile)) {
                                if (!isChosen(smallTile)) {
                                    smallTile.setChosen(true);
                                    appendLetterPhase2(smallTile.getLetter(), wordLarge, wordSmall);
                                    inner.setBackground(getResources().getDrawable(R.drawable.letter_red));
                                } else {
                                    smallTile.setChosen(false);
                                }
                                vibrator.vibrate(15);
                                showWord();
                            }
                            mLastLarge = wordLarge;
                            mLastSmall = wordSmall;
                            setChosenForAllGrids();
                        }
                    }
                });
                inner.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (phase == 1) {
                            if (isValidMove(smallTile)) {
                                formWord(String.valueOf(smallTile.getLetter()), wordLarge, wordSmall);
                                if (word.length() >= 3) {
                                    boolean wordDetected = wordDetect(word);
                                    if (wordDetected) {
                                        letterScore1 = 0;
                                        for (int i = 0; i < formedWords.get(mLastLarge).length(); i++) {
                                            letterScore1 += getLetterScore(formedWords.get(mLastLarge).charAt(i));
                                        }
                                        formedWordsScores1[mLastLarge] = letterScore1;
                                        isWord1[mLastLarge] = wordDetected;
                                        formedWords.put(wordLarge, word);
                                        word = "";
                                        smallTile.setChosen(true);
                                        setAllNextMoves();
                                        for (int small = 0; small < 9; small++) {
                                            final ScroggleTile otherTile = mSmallTiles[wordLarge][small];
                                            final Button innerButton = (Button) outer.findViewById(mSmallIds[small]);
                                            otherTile.setView(innerButton);
                                            if (smallIdMap1.get(wordLarge).contains(small)) {
                                                innerButton.setEnabled(false);
                                                innerButton.setTextColor(getResources().getColor(R.color.black_color));
                                                innerButton.setBackground(getResources().getDrawable(R.drawable.letter_green));
                                            } else {
                                                innerButton.setEnabled(false);
                                                innerButton.setClickable(false);
                                                innerButton.setBackground(getResources().getDrawable(R.drawable.letter_gray));
                                            }
                                            otherTile.animate();
                                        }
                                        calPhase1Points();
                                        ((ScroggleActivity) getActivity()).setPhase1Points(mPhase1Points);

                                    } else {
                                        setAllNextMoves();
                                        for (int small = 0; small < 9; small++) {
                                            final ScroggleTile otherTile = mSmallTiles[wordLarge][small];
                                            final Button innerButton = (Button) outer.findViewById(mSmallIds[small]);
                                            otherTile.setView(innerButton);

                                            if (smallIdMap1.get(wordLarge).contains(small)) {
                                                if (otherTile.getLetter().equals(word.substring(word.length() - 1)) && (small == wordSmall)) {
                                                    word = deleteLastChar(formedWords.get(wordLarge));
                                                    formedWords.put(wordLarge, word);
                                                    ArrayList<Integer> list = smallIdMap1.get(wordLarge);
                                                    for (Integer i : list) {
                                                        if (i == small) {
                                                            list.remove(i);
                                                        }
                                                    }
                                                    otherTile.animate();
                                                } else {
                                                    innerButton.setTextColor(getResources().getColor(R.color.black_color));
                                                }

                                            }

                                        }
                                        setValidNextMove1(mLastLarge, mLastSmall);
                                    }
                                    vibrator.vibrate(20);

                                } else {
                                    setValidNextMove1(mLastLarge, mLastSmall);
                                    formedWordsScores1[wordLarge] = 0;
                                }

                            }
                        } else {
                            setValidNextMove2(mLastLarge, mLastSmall);
                            if (isValidPhase2Move(smallTile)) {
                                appendLetterPhase2(String.valueOf(smallTile.getLetter()), wordLarge, wordSmall);
                                if (phase2Word.length() >= 3) {
                                    if (!Arrays.asList(phase2Words).contains(phase2Word)) {
                                        boolean wordDetected = wordDetect(phase2Word);
                                        if (wordDetected) {
                                            phase2Words[count] = phase2Word;
                                            isWord2[mLastLarge] = wordDetected;
                                            phase2Word = "";
                                            for (int l = 0; l < 9; l++) {
                                                for (int s = 0; s < 9; s++) {
                                                    View out = rootView.findViewById(mLargeIds[l]);
                                                    final Button but = (Button) out.findViewById(mSmallIds[s]);
                                                    ScroggleTile tile = mSmallTiles[l][s];
                                                    tile.setView(but);
                                                    if (smallIdMap2.get(l) != null) {
                                                        if (smallIdMap2.get(l).contains(s)) {
                                                            tile.animate();
                                                            but.setEnabled(false);
                                                            but.setClickable(false);
                                                            but.setTextColor(getResources().getColor(R.color.black_color));
                                                            but.setBackground(getResources().getDrawable(R.drawable.letter_red));
                                                        }
                                                    }

                                                }

                                            }
                                            setAllNextPhase2Moves();
                                            count++;
                                            calPhase2Points();
                                            ((ScroggleActivity) getActivity()).setPhase2Points(mPhase2Points);

                                        } else {
                                            setAllNextPhase2Moves();
                                            for (int small = 0; small < 9; small++) {
                                                final ScroggleTile otherTile = mSmallTiles[wordLarge][small];
                                                final Button innerButton = (Button) outer.findViewById(mSmallIds[small]);
                                                otherTile.setView(innerButton);

                                                if (Arrays.asList(phase2Words).contains(phase2Word)) {
                                                    phase2Word = deleteLastChar(phase2Words[count]);
                                                    phase2Words[count] = phase2Word;
                                                }

                                            }
                                            setValidNextMove2(mLastLarge, mLastSmall);
                                        }

                                    }
                                    vibrator.vibrate(30);

                                } else {
                                    setValidNextMove2(mLastLarge, mLastSmall);
                                }

                            }

                        }
                        return true;
                    }
                });
            }
        }


    }

    private String deleteLastChar(String str) {
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public void formWord(String letter, int large, int small) {
        if (large == mLastLarge) {
            word = word.concat(letter);
            if (formedWords != null) {
                if (word.length() == 1) {
                    word = formedWords.get(large).concat(word);
                }
                formedWords.put(large, word);
            } else {
                formedWords.put(large, word);
            }
            if (smallIdMap1.get(large) == null) {
                smallIdMap1.put(large, new ArrayList<>(Arrays.asList(small)));
            } else {
                ArrayList<Integer> smalls = smallIdMap1.get(large);
                smalls.add(small);
            }
        } else {
            word = "".concat(letter);
            if (formedWords != null && formedWords.get(large) != null) {
                word = formedWords.get(large).concat(word);
                formedWords.put(large, word);
            } else {
                formedWords.put(large, word);
            }
            if (smallIdMap1.get(large) == null) {
                smallIdMap1.put(large, new ArrayList<Integer>(Arrays.asList(small)));
            } else {
                ArrayList<Integer> smalls = smallIdMap1.get(large);
                smalls.add(small);
            }
        }
    }

    public void setChosenForAllGrids() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                ScroggleTile tile = mSmallTiles[i][j];
                tile.setChosen(false);
            }
        }
    }

    public void appendLetterPhase2(String letter, int l, int s) {
        phase2Word = phase2Word.concat(letter);

        if (smallIdMap2.get(l) == null) {
            smallIdMap2.put(l, new ArrayList<Integer>(Arrays.asList(s)));
        } else {
            ArrayList<Integer> smalls = smallIdMap2.get(l);
            smalls.add(s);
        }
    }



    public void display() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.mytoast,
                (ViewGroup) getView().findViewById(R.id.container));

        TextView text = (TextView) layout.findViewById(R.id.text);
        if(phase == 1) {
            text.setText("Word: " + word);
        } else if(phase ==2 ){
            text.setText("Word: " + phase2Word);
        }

        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void showWord() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (phase == 2) {
                    if (getActivity() == null) return;
                    setValidNextMove2(mLastLarge, mLastSmall);
                    if (phase2Word.length() >= 3) {
                        if (!Arrays.asList(phase2Words).contains(phase2Word)) {
                            boolean wordDetected = wordDetect(phase2Word);
                            if (wordDetected) {
                                display();
                                isWord2[mLastLarge] = wordDetected;
                            }
                        }
                    }
                } else {
                    if (getActivity() == null) return;
                    setValidNextMove1(mLastLarge, mLastSmall);
                    if (formedWords.get(mLastLarge) != null) {
                        if (formedWords.get(mLastLarge).length() >= 3) {
                            boolean wordDetected = wordDetect(word);
                            if (wordDetected) {
                                display();
                            } else {
                                formedWordsScores1[mLastLarge] = 0;
                            }
                            isWord1[mLastLarge] = wordDetected;

                        }
                    }

                }

                ((ScroggleActivity) getActivity()).showScores();
            }

        }, 1000);
    }



    private void updateAllTiles() {
        mEntireBoard.updateDrawableState();
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large].updateDrawableState();
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small].updateDrawableState();
            }
        }
    }

    public void putState(String data) {
        String[] fields = data.split(",");
        int index = 0;

        mPhase1Points = Integer.parseInt(fields[index++]);
        ((ScroggleActivity) getActivity()).setPhase1Points(mPhase1Points);
        mPhase2Points = Integer.parseInt(fields[index++]);
        ((ScroggleActivity) getActivity()).setPhase2Points(mPhase2Points);
        timeRemain = Long.parseLong(fields[index++]);
        this.setTime(timeRemain);
        phase = Integer.parseInt(fields[index++]);
        this.setPhase(phase);
        for (int large = 0; large < 9; large++) {
            View out = rootView.findViewById(mLargeIds[large]);
            for (int small = 0; small < 9; small++) {
                String letter = (fields[index++]);
                mSmallTiles[large][small].setLetter(letter);
                Boolean isChosen = Boolean.valueOf(fields[index++]);
                mSmallTiles[large][small].setChosen(isChosen);
                char state = fields[index++].charAt(0);
                letterState[large][small] = state;
            }
            String sequenceOfLetters = (fields[index++]);
            formedWords.put(large, sequenceOfLetters);
            Boolean isWord = Boolean.valueOf(fields[index++]);
            this.isWord1[large] = isWord;
        }
        setAvailableFromLastMove(mLastSmall);
        updateAllTiles();


        if (phase == 1) {
            for (int l = 0; l < 9; l++) {
                mEntireBoard.setView(rootView);
                final View outer = rootView.findViewById(mLargeIds[l]);
                mLargeTiles[l].setView(outer);
                for (int s = 0; s < 9; s++) {
                    Button button = (Button) outer.findViewById(mSmallIds[s]);
                    ScroggleTile smallTile = mSmallTiles[l][s];
                    if (smallTile.getIsChosen()) {
                        if (isWord1[l]) {
                            mLastLarge = -1;
                            button.setText(String.valueOf(smallTile.getLetter()));
                            button.setClickable(true);
                            button.setBackground(getResources().getDrawable(R.drawable.letter_green));
                        } else {
                            button.setClickable(false);
                            button.setText("");
                            button.setEnabled(false);
                            smallTile.setChosen(false);
                            smallTile.setIsEmpty(true);
                            button.setBackground(getResources().getDrawable(R.drawable.letter_background));
                        }
                    }
                }
            }
        } else {
            for (int l = 0; l < 9; l++) {
                mEntireBoard.setView(rootView);
                final View outer = rootView.findViewById(mLargeIds[l]);
                mLargeTiles[l].setView(outer);
                for (int s = 0; s < 9; s++) {
                    Button button = (Button) outer.findViewById(mSmallIds[s]);
                    ScroggleTile smallTile = mSmallTiles[l][s];
                    if (smallTile.getIsChosen()) {
                        if (isWord1[l]) {
                            button.setClickable(true);
                            button.setText(String.valueOf(smallTile.getLetter()));
                            button.setTextColor(getResources().getColor(R.color.black_color));
                            button.setBackground(getResources().getDrawable(R.drawable.letter_green));
                            smallTile.setChosen(false);
                        } else {
                            button.setEnabled(false);
                            button.setClickable(false);
                            smallTile.setChosen(false);
                            smallTile.setIsEmpty(true);
                            button.setBackground(getResources().getDrawable(R.drawable.letter_gray));
                        }
                    } else {
                        button.setEnabled(false);
                        button.setClickable(false);
                        smallTile.setChosen(false);
                        smallTile.setIsEmpty(true);
                        button.setBackground(getResources().getDrawable(R.drawable.letter_gray));
                    }
                }
            }
        }
    }

    public boolean isChosen(ScroggleTile tile) {
        return tile.getIsChosen();
    }

    public boolean wordDetect(String word) {
        String key = word.toLowerCase().substring(0, 3);
        if (wordlist.containsKey(key) && wordlist.get(key).contains(word.toLowerCase())) {
            return true;
        }
        return false;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public void setTime(long time) {
        this.timeRemain = time;
    }


    public void onScrogglePause() {
        timer.cancel();
        mediaPlayer.pause();
        ((ScroggleActivity) getActivity()).gameFragment.getView().setVisibility(View.INVISIBLE);
        isResume = false;
    }

    public void onScroggleResume() {
        timer = new ScroggleCountDownTimer(timeRemain, INTERVAL);
        timer.start();
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.backgroundmusic);
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        ((ScroggleActivity) getActivity()).gameFragment.getView().setVisibility(View.VISIBLE);
        isResume = true;
    }

    private void calPhase1Points() {
        for (int i = 0; i < 9; i++) {
            if (isWord1[i]) {
                mPhase1Points += formedWordsScores1[i];
            }
        }
        int pointsForLength = 0;
        for (Integer large : formedWords.keySet()) {
            int sco = calPointsForLength(formedWords.get(large));
            pointsForLength += sco;
        }
        mPhase1Points += pointsForLength;
    }

    public int getLetterScore(char ch) {
        int point = 0;
        if ((ch == 'e') || (ch == 'a') || (ch == 'i') || (ch == 'o')
                || (ch == 'r') || (ch == 't') || (ch == 'l')
                || (ch == 'n') || (ch == 's') || (ch == 'u')) {
            point = 1;
        } else if (ch == ('d') || ch == ('g')) {
            point = 2;
        } else if (ch == ('b') || ch == ('c') || ch == ('m') || ch == ('p')) {
            point = 3;
        } else if (ch == ('f') || ch == ('h') || ch == ('v') || ch == ('w') || ch == ('y')) {
            point = 4;
        } else if (ch == ('k')) {
            point = 5;
        } else if (ch == ('j') || ch == ('x')) {
            point = 8;
        } else if (ch == ('q') || ch == ('z')) {
            point = 10;
        }
        return point;
    }

    public int calPointsForLength(String str) {
        int point = 0;
        if (str.length() <= 5) {
            point = str.length();
        } else if (str.length() == 6) {
            point = 10;
        } else if (str.length() == 7) {
            point = 15;
        } else if (str.length() == 8) {
            point = 20;
        } else if (str.length() == 9) {
            point = 25;
        }
        return point;
    }

    private void calPhase2Points() {
        int pointsForLength = 0;
        letterScore2 = 0;
        for (int cnt = 0; cnt < count; cnt++) {
            for (int i = 0; i < phase2Words[cnt].length(); i++) {
                letterScore2 += getLetterScore(phase2Words[cnt].charAt(i));
            }
        }
        for(int cnt = 0; cnt<count;cnt++){
            pointsForLength+= calPointsForLength(phase2Words[cnt]);
        }
        int total = pointsForLength+ letterScore2;
        mPhase2Points += total;
    }

    public void mute() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public String getState() {
        StringBuilder builder = new StringBuilder();
        builder.append(mPhase1Points);
        builder.append(',');
        builder.append(mPhase2Points);
        builder.append(',');
        builder.append(timeRemain);
        builder.append(',');
        builder.append(phase);
        builder.append(',');
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                builder.append(mSmallTiles[large][small].getLetter());
                builder.append(',');
                builder.append(mSmallTiles[large][small].getIsChosen());
                builder.append(',');
                builder.append(letterState[large][small]);
                builder.append(',');
            }
            builder.append(formedWords.get(large));
            builder.append(',');
            builder.append(isWord1[large]);
            builder.append(',');
        }
        return builder.toString();
    }

    private class LoadWordList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                InputStream inputStream = getResources().getAssets().open("wordlistmap");
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                wordlist = (HashMap<String,ArrayList<String>>) objectInputStream.readObject();
                objectInputStream.close();
                Log.i("AsyncTask", String.valueOf(wordlist.size()));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Void... params) {
        }

        protected void onPostExecute(Void v) {
        }

    }

    private void setAvailableFromLastMove(int small) {
        phase1Available.clear();
        if (small != -1) {
            for (int dest = 0; dest < 9; dest++) {
                ScroggleTile tile = mSmallTiles[small][dest];
                if (!tile.getIsChosen()) {
                    addAvailable(tile);
                }
            }
        }
        if (phase1Available.isEmpty()) {
            setAllAvailable();
        }
    }

    private void setAvailPhaseTwo(int small) {
        phase2Available.clear();
        if (small != -1) {
            for (int dest = 0; dest < 9; dest++) {
                ScroggleTile tile = mSmallTiles[small][dest];
                if (!tile.getIsChosen()) {
                    addAvailablePhase2(tile);
                }
            }
        }
        if (phase2Available.isEmpty()) {
            setAllPhase2Available();
        }
    }

    private void setAllAvailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                ScroggleTile tile = mSmallTiles[large][small];
                if (!tile.getIsChosen()) {
                    addAvailable(tile);
                }
            }
        }
    }

    private void setAllPhase2Available() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                ScroggleTile tile = mSmallTiles[large][small];
                if (!tile.getIsChosen()) {
                    addAvailablePhase2(tile);
                }
            }
        }
    }

    private void addAvailable(ScroggleTile tile) {
        tile.animate();
        phase1Available.add(tile);
    }

    private void addAvailablePhase2(ScroggleTile tile) {
        tile.animate();
        phase2Available.add(tile);
    }

    private boolean isValidMove(ScroggleTile tile) {
        return nextMoves1.contains(tile);
    }

    private boolean isValidPhase2Move(ScroggleTile tile) {
        return nextMoves2.contains(tile);
    }


    public void setValidNextMove1(int l, int s) {
        nextMoves1.clear();
        if (l != -1 && s != -1) {

            List<Integer> moves = getPossibleMoves(s);
            for (int i = 0; i < moves.size(); i++) {
                int gridId = moves.get(i);
                ScroggleTile smallTile = mSmallTiles[l][gridId];
                if (!smallTile.getIsChosen()) {
                    nextMoves1.add(smallTile);
                }
            }

            for (int large = 0; large < 9; large++) {
                for (int small = 0; small < 9; small++) {
                    if (large != l) {
                        nextMoves1.add(mSmallTiles[large][small]);
                    }
                }
            }

        }
        if (nextMoves1.isEmpty()) {
            setAllNextMoves();
        }
    }

    public void setValidNextMove2(int l, int s) {
        nextMoves2.clear();
        if (l != -1 && s != -1) {

            List<Integer> largeMoves = getPossibleMoves2(l);
            for (int i = 0; i < largeMoves.size(); i++) {
                int gridId = largeMoves.get(i);
                for (int j = 0; j < 9; j++) {
                    ScroggleTile smallTile = mSmallTiles[gridId][j];
                    if (!smallTile.getIsChosen()) {
                        nextMoves2.add(smallTile);
                    }
                }
            }
            for (int small = 0; small < 9; small++) {
                ScroggleTile tile = mSmallTiles[l][small];
                tile.setChosen(true);
            }

            for (int large = 0; large < 9; large++) {
                for (int small = 0; small < 9; small++) {
                    if (large != l) {
                        nextMoves2.add(mSmallTiles[large][small]);
                    }
                }
            }
        }
        if (nextMoves2.isEmpty()) {
            setAllNextPhase2Moves();
        }
    }

    public void setAllNextMoves() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                nextMoves1.add(mSmallTiles[large][small]);
            }
        }
    }

    public void setAllNextPhase2Moves() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                nextMoves2.add(mSmallTiles[large][small]);
            }
        }
    }

    public List<Integer> getPossibleMoves(int small) {
        List<Integer> moves = new ArrayList<Integer>();
        if (small == 0) {
            moves.addAll(Arrays.asList(1, 3, 4));
        } else if (small == 1) {
            moves.addAll(Arrays.asList(0, 2, 3, 4, 5));
        } else if (small == 2) {
            moves.addAll(Arrays.asList(1, 4, 5));
        } else if (small == 3) {
            moves.addAll(Arrays.asList(0, 1, 4, 6, 7));
        } else if (small == 4) {
            moves.addAll(Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8));
        } else if (small == 5) {
            moves.addAll(Arrays.asList(1, 2, 4, 7, 8));
        } else if (small == 6) {
            moves.addAll(Arrays.asList(3, 4, 7));
        } else if (small == 7) {
            moves.addAll(Arrays.asList(3, 4, 5, 6, 8));
        } else if (small == 8) {
            moves.addAll(Arrays.asList(4, 5, 7));
        }

        return moves;
    }

    public List<Integer> getPossibleMoves2(int large) {
        List<Integer> largeMoves = new ArrayList<Integer>();
        if (large == 0) {
            largeMoves.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        } else if (large == 1) {
            largeMoves.addAll(Arrays.asList(0, 2, 3, 4, 5, 6, 7, 8));
        } else if (large == 2) {
            largeMoves.addAll(Arrays.asList(0, 1, 3, 4, 5, 6, 7, 8));
        } else if (large == 3) {
            largeMoves.addAll(Arrays.asList(0, 1, 2, 4, 5, 6, 7, 8));
        } else if (large == 4) {
            largeMoves.addAll(Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8));
        } else if (large == 5) {
            largeMoves.addAll(Arrays.asList(0, 1, 2, 3, 4, 6, 7, 8));
        } else if (large == 6) {
            largeMoves.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 7, 8));
        } else if (large == 7) {
            largeMoves.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 8));
        } else if (large == 8) {
            largeMoves.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));
        }
        return largeMoves;
    }


    public class ScroggleCountDownTimer extends CountDownTimer {

        public ScroggleCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            if (phase == 1) {
                if (formedWords.size() > 0) {
                    ((ScroggleActivity) getActivity()).setFragmentInvisible();
                    final Dialog mDialog = new Dialog(getActivity());
                    mDialog.setTitle("Scroggle");
                    mDialog.setContentView(R.layout.phase_two);
                    mDialog.setCancelable(false);

                    TextView textView = (TextView) mDialog.findViewById(R.id.alert);
                    textView.setText("Phase one ends.\nPhase two begins.\nYour Phase 1 Score: " + mPhase1Points);

                    Button ok_button = (Button) mDialog.findViewById(R.id.ok);
                    ok_button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            phase = 2;
                            timeRemain = 0;
                            Intent intent = new Intent(getActivity(), ScroggleActivity.class);
                            intent.putExtra("gameData", getState());
                            intent.putExtra("phase", phase);
                            startActivity(intent);
                            mediaPlayer.pause();
                            getActivity().finish();
                        }
                    });
                    mDialog.show();
                } else {
                    final Dialog mDialog = new Dialog(getActivity());
                    mDialog.setTitle("Scroggle");
                    mDialog.setContentView(R.layout.phase_two);
                    mDialog.setCancelable(false);

                    TextView textView = (TextView) mDialog.findViewById(R.id.alert);
                    textView.setText("Game Over");

                    Button ok_button = (Button) mDialog.findViewById(R.id.ok);
                    ok_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaPlayer.pause();
                            getActivity().finish();
                        }
                    });
                    mDialog.show();
                }
            } else {
                gameData = "";
                rootView.setVisibility(View.INVISIBLE);
                final Dialog mDialog = new Dialog(getActivity());
                mDialog.setTitle("Scroggle");
                mDialog.setContentView(R.layout.phase_two);
                mDialog.setCancelable(false);

                TextView textView = (TextView) mDialog.findViewById(R.id.alert);
                int total = mPhase1Points + mPhase2Points;
                textView.setText("Game Over\nYour Total Score: " + total);

                Button ok_button = (Button) mDialog.findViewById(R.id.ok);
                ok_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaPlayer.pause();
                        getActivity().finish();
                    }
                });
                mDialog.show();
            }

        }

        @Override
        public void onTick(long millisUnitFinished) {
            String time = String.format("%02d : %02d", TimeUnit.MILLISECONDS.toMinutes(millisUnitFinished) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUnitFinished)),
                    TimeUnit.MILLISECONDS.toSeconds(millisUnitFinished) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUnitFinished)));
            if (time.equals("00 : 05") || time.equals("00 : 04") || time.equals("00 : 03") || time.equals("00 : 02") || time.equals("00 : 01")) {
                Animation animation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.flash);
                timerText.startAnimation(animation);
                timerText.setTextColor(getResources().getColor(R.color.red_color));
                timerText.setText("Time Remain: " + time);

            } else {
                timerText.setText("Time Remain: " + time);
            }
            timeRemain = millisUnitFinished;
        }
    }



}
