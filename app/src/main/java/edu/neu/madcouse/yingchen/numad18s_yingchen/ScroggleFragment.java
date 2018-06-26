package edu.neu.madcouse.yingchen.numad18s_yingchen;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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

    private int mPhase1Points = 0;
    private int mPhase2Points = 0;

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
    private String phaseTwoWord;
    private MediaPlayer mediaPlayer;
    private String gameData;
    private View rootView;
    private TextView timerText;

    private List<List<Integer>> letterPositions = new ArrayList<>();
    private char[][] letterState = new char[9][9];

    private Set<ScroggleTile> mAvailable = new HashSet<>();
    private Set<ScroggleTile> tAvailable = new HashSet<>();

    private Set<ScroggleTile> nextMoves = new HashSet<>();
    private Set<ScroggleTile> phaseTwoNextMoves = new HashSet<>();

    private String[] phaseTwoWords = new String[100];
    private Map<Integer, String> listOfWordsFormed = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> smallIdsWhichFormWordPhaseOne = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> smallIdsWhichFormWordsPhaseTwo = new HashMap<>();

    private boolean[] isAWord = new boolean[9];
    private boolean[] isPhaseTwoWord = new boolean[9];
    private int phaseOneScore = 0;
    private int w2Score;
    private int count = 0;
    private int[] scoreForPhaseOneWordsFormed = {0, 0, 0, 0, 0, 0, 0, 0, 0};
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
            phaseTwoWord = "";
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
            Log.i("AsyncTask", String.valueOf(total));

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
            setValidNextMove(mLastLarge, mLastSmall);
        } else {
            setAvailPhaseTwo(mLastSmall);
            setValidNextMovePhaseTwo(mLastLarge, mLastSmall);
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
                final ScroggleTile smallTile = mSmallTiles[large][i];
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
                        if (!isAWord[l]) {
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
                final int fLarge = large;
                final int fSmall = small;
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
                                    formWord(String.valueOf(smallTile.getLetter()), fLarge, fSmall);
                                } else {
                                    smallTile.setChosen(false);
                                    word = delLastChar(listOfWordsFormed.get(fLarge));
                                    listOfWordsFormed.put(fLarge, word);
                                    inner.setBackground(getResources().getDrawable(R.drawable.letter_available));
                                }
                                //mSoundPool.play(mSoundX, mVolume, mVolume, 1, 0, 1f);
                                vibrator.vibrate(15);
                                think();
                                mLastLarge = fLarge;
                                mLastSmall = fSmall;
                                setValidNextMove(mLastLarge, mLastSmall);
                            } else {
                                //mSoundPool.play(mSoundMiss, mVolume, mVolume, 1, 0, 1f);
                            }

                        } else {
                            setValidNextMovePhaseTwo(mLastLarge, mLastSmall);
                            if (isValidPhaseTwoMove(smallTile)) {
                                if (!isChosen(smallTile)) {
                                    smallTile.setChosen(true);
                                    appendLetter(smallTile.getLetter(), fLarge, fSmall);
                                    inner.setBackground(getResources().getDrawable(R.drawable.letter_red));
                                } else {
                                    smallTile.setChosen(false);
                                }
                                //mSoundPool.play(mSoundX, mVolume, mVolume, 1, 0, 1f);
                                vibrator.vibrate(15);
                                think();
                            } else {
                                //mSoundPool.play(mSoundMiss, mVolume, mVolume, 1, 0, 1f);
                            }
                            mLastLarge = fLarge;
                            mLastSmall = fSmall;
                            setChosenForAllGrids();
                        }
                    }
                });
                inner.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //onclick recognise its a word, clearoff all other boxes in this tile
                        //call the search method and check if the word exists in the dictionary
                        if (phase == 1) {
                            if (isValidMove(smallTile)) {
                                formWord(String.valueOf(smallTile.getLetter()), fLarge, fSmall);
                                if (word.length() >= 3) {
                                    Boolean isWordPresent = searchWordInMap(word);
                                    if (isWordPresent) {
                                        phaseOneScore = 0;
                                        for (int i = 0; i < listOfWordsFormed.get(mLastLarge).length(); i++) {
                                            phaseOneScore += getLetterScore(listOfWordsFormed.get(mLastLarge).charAt(i));
                                        }
                                        scoreForPhaseOneWordsFormed[mLastLarge] = phaseOneScore;
                                        isAWord[mLastLarge] = isWordPresent;
                                        listOfWordsFormed.put(fLarge, word);
                                        word = "";
                                        smallTile.setChosen(true);
                                        setAllNextMoves();
                                        for (int small = 0; small < 9; small++) {
                                            final ScroggleTile otherTile = mSmallTiles[fLarge][small];
                                            final Button innerButton = (Button) outer.findViewById(mSmallIds[small]);
                                            otherTile.setView(innerButton);
                                            if (smallIdsWhichFormWordPhaseOne.get(fLarge).contains(small)) {
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
                                        calcPhaseOnePoints();
                                        ((ScroggleActivity) getActivity()).setPhase1Points(mPhase1Points);

                                    } else {
                                        setAllNextMoves();
                                        for (int small = 0; small < 9; small++) {
                                            final ScroggleTile otherTile = mSmallTiles[fLarge][small];
                                            final Button innerButton = (Button) outer.findViewById(mSmallIds[small]);
                                            otherTile.setView(innerButton);

                                            if (smallIdsWhichFormWordPhaseOne.get(fLarge).contains(small)) {
                                                if (otherTile.getLetter().equals(word.substring(word.length() - 1)) && (small == fSmall)) {
                                                    word = delLastChar(listOfWordsFormed.get(fLarge));
                                                    listOfWordsFormed.put(fLarge, word);
                                                    ArrayList<Integer> list = smallIdsWhichFormWordPhaseOne.get(fLarge);
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
                                        setValidNextMove(mLastLarge, mLastSmall);

                                    }
                                    vibrator.vibrate(20);

                                } else {
                                    setValidNextMove(mLastLarge, mLastSmall);
                                    scoreForPhaseOneWordsFormed[fLarge] = 0;
                                }

                            }
                        } else {
                            setValidNextMovePhaseTwo(mLastLarge, mLastSmall);
                            if (isValidPhaseTwoMove(smallTile)) {
                                appendLetter(String.valueOf(smallTile.getLetter()), fLarge, fSmall);
                                if (phaseTwoWord.length() >= 3) {
                                    if (!Arrays.asList(phaseTwoWords).contains(phaseTwoWord)) {
                                        Boolean isWordPresent = searchWordInMap(phaseTwoWord);
                                        if (isWordPresent) {
                                            phaseTwoWords[count] = phaseTwoWord;
                                            isPhaseTwoWord[mLastLarge] = isWordPresent;
                                            phaseTwoWord = "";
                                            for (int ll = 0; ll < 9; ll++) {
                                                for (int ss = 0; ss < 9; ss++) {
                                                    View out = rootView.findViewById(mLargeIds[ll]);
                                                    final Button but = (Button) out.findViewById(mSmallIds[ss]);
                                                    ScroggleTile t = mSmallTiles[ll][ss];
                                                    t.setView(but);
                                                    if (smallIdsWhichFormWordsPhaseTwo.get(ll) != null) {
                                                        if (smallIdsWhichFormWordsPhaseTwo.get(ll).contains(ss)) {
                                                            // smallTile.setChosen(true);
                                                            t.animate();
                                                            but.setEnabled(false);
                                                            but.setClickable(false);
                                                            but.setTextColor(getResources().getColor(R.color.black_color));
                                                            but.setBackgroundDrawable(getResources().getDrawable(R.drawable.letter_red));
                                                        }
                                                    }

                                                }

                                            }

                                            setAllNextPhaseTwoMoves();
                                            count++;

                                            calcPhaseTwoPoints();
                                            ((ScroggleActivity) getActivity()).setPhase2Points(mPhase2Points);

                                        } else {
                                            setAllNextPhaseTwoMoves();
                                            for (int small = 0; small < 9; small++) {
                                                final ScroggleTile otherTile = mSmallTiles[fLarge][small];
                                                final Button innerButton = (Button) outer.findViewById(mSmallIds[small]);
                                                otherTile.setView(innerButton);

                                                if (Arrays.asList(phaseTwoWords).contains(phaseTwoWord)) {
                                                    phaseTwoWord = delLastChar(phaseTwoWords[count]);
                                                    phaseTwoWords[count] = phaseTwoWord;
                                                }

                                            }

                                            setValidNextMovePhaseTwo(mLastLarge, mLastSmall);
                                        }

                                    }
                                    vibrator.vibrate(30);

                                } else {
                                    setValidNextMovePhaseTwo(mLastLarge, mLastSmall);
                                }
                                //mSoundPool.play(mSoundX, mVolume, mVolume, 1, 0, 1f);

                            }

                        }
                        return true;
                    }
                });
            }
        }


    }

    private String delLastChar(String str) {
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public void formWord(String letter, int large, int small) {
        if (large == mLastLarge) {
            word = word.concat(letter);
            if (listOfWordsFormed != null) {
                if (word.length() == 1) {
                    word = listOfWordsFormed.get(large).concat(word);
                }
                listOfWordsFormed.put(large, word);
            } else {
                listOfWordsFormed.put(large, word);
            }
            if (smallIdsWhichFormWordPhaseOne.get(large) == null) {
                smallIdsWhichFormWordPhaseOne.put(large, new ArrayList<Integer>(Arrays.asList(small)));
            } else {
                ArrayList<Integer> smalls = smallIdsWhichFormWordPhaseOne.get(large);
                smalls.add(small);
            }
        } else {
            word = "".concat(letter);
            if (listOfWordsFormed != null && listOfWordsFormed.get(large) != null) {
                word = listOfWordsFormed.get(large).concat(word);
                listOfWordsFormed.put(large, word);
            } else {
                listOfWordsFormed.put(large, word);
            }
            if (smallIdsWhichFormWordPhaseOne.get(large) == null) {
                smallIdsWhichFormWordPhaseOne.put(large, new ArrayList<Integer>(Arrays.asList(small)));
            } else {
                ArrayList<Integer> smalls = smallIdsWhichFormWordPhaseOne.get(large);
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

    public void appendLetter(String letter, int l, int s) {
        phaseTwoWord = phaseTwoWord.concat(letter);

        if (smallIdsWhichFormWordsPhaseTwo.get(l) == null) {
            smallIdsWhichFormWordsPhaseTwo.put(l, new ArrayList<Integer>(Arrays.asList(s)));
        } else {
            ArrayList<Integer> smalls = smallIdsWhichFormWordsPhaseTwo.get(l);
            smalls.add(s);
        }
    }



    public void display() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) getView().findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.text);
        //display the word formed at top
        if(phase == 1) {
            text.setText("Word: " + word);
        } else if(phase ==2 ){
            text.setText("Word: " + phaseTwoWord);
        }

        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void think() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (phase == 2) {
                    if (getActivity() == null) return;
                    setValidNextMovePhaseTwo(mLastLarge, mLastSmall);
                    if (phaseTwoWord.length() >= 3) {
                        if (!Arrays.asList(phaseTwoWords).contains(phaseTwoWord)) {
                            Boolean isWordPresent = searchWordInMap(phaseTwoWord);
                            if (isWordPresent) {
                                // Log.d("WORD PRESENT: ", phaseTwoWord);
                                display();
                                isPhaseTwoWord[mLastLarge] = isWordPresent;

                            }
                        }
                    }
                } else {
                    if (getActivity() == null) return;
                    setValidNextMove(mLastLarge, mLastSmall);
                    if (listOfWordsFormed.get(mLastLarge) != null) {
                        if (listOfWordsFormed.get(mLastLarge).length() >= 3) {
                            Boolean isWordPresent = searchWordInMap(word);
                            if (isWordPresent) {
                                //       Log.d("WORD PRESENT: ", word);
                                display();
                            } else {
                                scoreForPhaseOneWordsFormed[mLastLarge] = 0;
                            }
                            isAWord[mLastLarge] = isWordPresent;

                        }
                    }

                }

                ((ScroggleActivity) getActivity()).stopThinking();
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
            listOfWordsFormed.put(large, sequenceOfLetters);
            Boolean isWord = Boolean.valueOf(fields[index++]);
            isAWord[large] = isWord;
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
                    ScroggleTile tinyTile = mSmallTiles[l][s];
                    if (tinyTile.getIsChosen()) {
                        if (isAWord[l]) {
                            mLastLarge = -1;
                            button.setText(String.valueOf(tinyTile.getLetter()));
                            button.setClickable(true);
                            button.setBackground(getResources().getDrawable(R.drawable.letter_green));
                        } else {
                            button.setClickable(false);
                            button.setText("");
                            button.setEnabled(false);
                            tinyTile.setChosen(false);
                            tinyTile.setIsEmpty(true);
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
                    ScroggleTile tinyTile = mSmallTiles[l][s];
                    if (tinyTile.getIsChosen()) {
                        if (isAWord[l]) {
                            button.setClickable(true);
                            button.setText(String.valueOf(tinyTile.getLetter()));
                            button.setTextColor(getResources().getColor(R.color.black_color));
                            button.setBackground(getResources().getDrawable(R.drawable.letter_green));
                            tinyTile.setChosen(false);
                        } else {
                            button.setEnabled(false);
                            button.setClickable(false);
                            tinyTile.setChosen(false);
                            tinyTile.setIsEmpty(true);
                            // tinyTile.setChosen(false);
                            button.setBackground(getResources().getDrawable(R.drawable.letter_gray));
                        }
                    } else {
                        button.setEnabled(false);
                        button.setClickable(false);
                        tinyTile.setChosen(false);
                        tinyTile.setIsEmpty(true);
                        // tinyTile.setChosen(false);
                        button.setBackground(getResources().getDrawable(R.drawable.letter_gray));
                    }
                }
            }
        }
    }

    public boolean isChosen(ScroggleTile tile) {
        return tile.getIsChosen();
    }

    public Boolean searchWordInMap(String word) {
        if (wordlist.get(word.toLowerCase().substring(0, 3)).contains(word.toLowerCase())) {
            return true;
        }
        return false;
    }

    public void setPhase(int ph) {
        this.phase = ph;
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

    private void calcPhaseOnePoints() {
        mPhase1Points = 0;
        for (int i = 0; i < 9; i++) {
            if (isAWord[i]) {
                mPhase1Points += scoreForPhaseOneWordsFormed[i];
            }
        }
        int scoreForLengthOfWords = 0;
        for (Integer large : listOfWordsFormed.keySet()) {
            int sco = getScoreForLengthOfWords(listOfWordsFormed.get(large));
            scoreForLengthOfWords += sco;
        }

        mPhase1Points += scoreForLengthOfWords;
    }

    public int getLetterScore(char letter) {
        int point = 0;
        if ((letter == 'e') || (letter == 'a') || (letter == 'i') || (letter == 'o')
                || (letter == 'r') || (letter == 't') || (letter == 'l')
                || (letter == 'n') || (letter == 's') || (letter == 'u')) {
            point = 1;
        } else if (letter == ('d') || letter == ('g')) {
            point = 2;
        } else if (letter == ('b') || letter == ('c') || letter == ('m') || letter == ('p')) {
            point = 3;
        } else if (letter == ('f') || letter == ('h') || letter == ('v') || letter == ('w') || letter == ('y')) {
            point = 4;
        } else if (letter == ('k')) {
            point = 5;
        } else if (letter == ('j') || letter == ('x')) {
            point = 8;
        } else if (letter == ('q') || letter == ('z')) {
            point = 10;
        }
        return point;
    }

    public int getScoreForLengthOfWords(String str) {
        //based on the length of the words, give each letter 2 points(Bonus)
        int s = 0;
        if (str.length() == 3) {
            s = 6;
        } else if (str.length() == 4) {
            s = 8;
        } else if (str.length() == 5) {
            s = 10;
        } else if (str.length() == 6) {
            s = 12;
        } else if (str.length() == 7) {
            s = 14;
        } else if (str.length() == 8) {
            s = 16;
        } else if (str.length() == 9) {//bonus if  words is 9 in length
            s = 25;
        }

        return s;
    }

    private void calcPhaseTwoPoints() {
        mPhase2Points = 0;
        int scoreForLengthOfWords = 0;
        w2Score = 0;
        for (int cnt = 0; cnt < count; cnt++) {
            for (int i = 0; i < phaseTwoWords[cnt].length(); i++) {
                w2Score += getLetterScore(phaseTwoWords[cnt].charAt(i));
                //scoreForLengthOfWords += getScoreForLengthOfWords(phaseTwoWords[cnt]);

            }
            //mPhaseTwoPoints +=w2Score;
        }
        for(int cnt = 0; cnt<count;cnt++){
            scoreForLengthOfWords+=getScoreForLengthOfWords(phaseTwoWords[cnt]);
        }
        int tot = scoreForLengthOfWords+w2Score;
        mPhase2Points += tot;

    }

    public void onScroggleQuit() {
        timer.cancel();
        mediaPlayer.pause();
        isResume = false;
        getActivity().finish();
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
            builder.append(listOfWordsFormed.get(large));
            builder.append(',');
            builder.append(isAWord[large]);
            builder.append(',');
        }
        return builder.toString();
    }

//    private class LoadNineWords extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            try {
//                InputStream inputStream = getResources().getAssets().open("allnineletterwords");
//                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
//                nineLetterWords = (ArrayList<String>) objectInputStream.readObject();
//                objectInputStream.close();
//
//                int total = nineLetterWords.size();
//                Log.i("AsyncTask", String.valueOf(total));
//
//                for (int i = 0; i < 9; i++) {
//                    Random rand = new Random();
//                    int index = rand.nextInt(total);
//                    String word = nineLetterWords.get(index);
//                    boardWords.add(word);
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        protected void onProgressUpdate(Void... params) {
//        }
//
//        protected void onPostExecute(Void v) {
//
//    }

    //}

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
        clearAvailable();
        // Make all the tiles at the destination available
        if (small != -1) {
            for (int dest = 0; dest < 9; dest++) {
                ScroggleTile tile = mSmallTiles[small][dest];
                if (!tile.getIsChosen()) {
                    addAvailable(tile);
                }
            }
        }
        // If there were none available, make all squares available
        if (mAvailable.isEmpty()) {
            setAllAvailable();
        }
    }

    private void setAvailPhaseTwo(int small) {
        cleartAvailable();
        if (small != -1) {
            for (int dest = 0; dest < 9; dest++) {
                ScroggleTile tile = mSmallTiles[small][dest];
                if (!tile.getIsChosen()) {
                    addAvailPhaseTwo(tile);
                }
            }
        }
        if (tAvailable.isEmpty()) {
            setAllPhaseTwoAvailable();
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


    private void setAllPhaseTwoAvailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                ScroggleTile tile = mSmallTiles[large][small];
                if (!tile.getIsChosen()) {
                    addAvailPhaseTwo(tile);
                }
            }
        }
    }


    private void clearAvailable() {
        mAvailable.clear();
    }

    private void cleartAvailable() {
        tAvailable.clear();
    }

    private void addAvailable(ScroggleTile tile) {
        tile.animate();
        mAvailable.add(tile);
    }

    private void addAvailPhaseTwo(ScroggleTile tile) {
        tile.animate();
        tAvailable.add(tile);
    }

    private void clearMove() {
        nextMoves.clear();
    }

    private void clearPhaseTwoMoves() {
        nextMoves.clear();
    }

    private void addMove(ScroggleTile tile) {
        nextMoves.add(tile);
    }

    private void addPhaseTwoMove(ScroggleTile tile) {
        phaseTwoNextMoves.add(tile);
    }

    private Boolean isValidMove(ScroggleTile tile) {
        return nextMoves.contains(tile);
    }

    private Boolean isValidPhaseTwoMove(ScroggleTile tile) {
        return phaseTwoNextMoves.contains(tile);
    }


    public void setValidNextMove(int sLarge, int sSmall) {
        //clear the nextmoves hashset
        clearMove();
        if (sLarge != -1 && sSmall != -1) {

            //get all the possible squares in all directions of the grid with value small
            List<Integer> moves = getPossibleMoves(sSmall);
            for (int i = 0; i < moves.size(); i++) {
                int gridId = moves.get(i);
                ScroggleTile smallTile = mSmallTiles[sLarge][gridId];
                if (!smallTile.getIsChosen()) {
                    addMove(smallTile);
                }
            }

            for (int large = 0; large < 9; large++) {
                for (int small = 0; small < 9; small++) {
                    if (large != sLarge) {
                        addMove(mSmallTiles[large][small]);
                    }
                }
            }

        }
        if (nextMoves.isEmpty()) {
            setAllNextMoves();
        }
    }

    public void setValidNextMovePhaseTwo(int sLarge, int sSmall) {
        //clear the nextmoves hashset
        clearPhaseTwoMoves();
        if (sLarge != -1 && sSmall != -1) {

            //get all the possible squares in all directions of the grid with value small
            List<Integer> largeMoves = getPossiblePhaseTwoMoves(sLarge);
            for (int l = 0; l < largeMoves.size(); l++) {
                int gridId = largeMoves.get(l);
                for (int s = 0; s < 9; s++) {
                    ScroggleTile smallTile = mSmallTiles[gridId][s];
                    if (!smallTile.getIsChosen()) {
                        addPhaseTwoMove(smallTile);
                    }
                }
            }
            for (int small = 0; small < 9; small++) {
                ScroggleTile tile = mSmallTiles[sLarge][small];
                tile.setChosen(true);
            }

            for (int large = 0; large < 9; large++) {
                for (int small = 0; small < 9; small++) {
                    if (large != sLarge) {
                        addPhaseTwoMove(mSmallTiles[large][small]);
                    }
                }
            }
        }
        if (phaseTwoNextMoves.isEmpty()) {
            setAllNextPhaseTwoMoves();
        }
    }

    public void setAllNextMoves() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                addMove(mSmallTiles[large][small]);
            }
        }
    }

    public void setAllNextPhaseTwoMoves() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                addPhaseTwoMove(mSmallTiles[large][small]);
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

    public List<Integer> getPossiblePhaseTwoMoves(int large) {
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
                if (listOfWordsFormed.size() > 0) {
                    ((ScroggleActivity) getActivity()).setFragmentInvisible();
                    final Dialog mDialog = new Dialog(getActivity());
                    mDialog.setTitle("Scroggle");
                    mDialog.setContentView(R.layout.phase_two);
                    mDialog.setCancelable(false);

                    TextView textView = (TextView) mDialog.findViewById(R.id.alert);
                    textView.setText("END: PHASE ONE\nBEGIN: PHASE TWO\nYour Phase 1 Score: " + mPhase1Points);

                    Button ok_button = (Button) mDialog.findViewById(R.id.ok_button);
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
                    //now that the dialog is set up, it's time to show it
                    mDialog.show();
                } else {
                    final Dialog mDialog = new Dialog(getActivity());
                    mDialog.setTitle("Scroggle");
                    mDialog.setContentView(R.layout.phase_two);
                    mDialog.setCancelable(false);

                    TextView textView = (TextView) mDialog.findViewById(R.id.alert);
                    textView.setText("Game Over");

                    Button ok_button = (Button) mDialog.findViewById(R.id.ok_button);
                    ok_button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            mediaPlayer.pause();
                            getActivity().finish();
                        }
                    });
                    //now that the dialog is set up, it's time to show it
                    mDialog.show();
                }
            } else {
                //for phase two
                gameData = "";
                rootView.setVisibility(View.INVISIBLE);
                final Dialog mDialog = new Dialog(getActivity());
                mDialog.setTitle("Scroggle");
                mDialog.setContentView(R.layout.phase_two);
                mDialog.setCancelable(false);

                TextView textView = (TextView) mDialog.findViewById(R.id.alert);
                int total = mPhase1Points + mPhase2Points;
                textView.setText("Game Over\nYour Total Score: " + total);

                Button ok_button = (Button) mDialog.findViewById(R.id.ok_button);
                ok_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mediaPlayer.pause();
                        getActivity().finish();
                    }
                });
                //now that the dialog is set up, it's time to show it
                mDialog.show();
            }

        }

        @Override
        public void onTick(long millisUnitFinished) {
            String time = String.format("%02d : %02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUnitFinished) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUnitFinished)),
                    TimeUnit.MILLISECONDS.toSeconds(millisUnitFinished) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUnitFinished)));
            if (time.equals("00 : 05") || time.equals("00 : 04") || time.equals("00 : 03") || time.equals("00 : 02") || time.equals("00 : 01")) {
                Animation animation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.blink);
                timerText.startAnimation(animation);
                timerText.setTextColor(getResources().getColor(R.color.red_color));
                timerText.setText("Time Remaining: " + time);

            } else {
                timerText.setText("Time Remaning: " + time);
            }
            timeRemain = millisUnitFinished;
        }
    }



}
