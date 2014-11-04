package com.evilgeniustechnologies.Wordrific.activities;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.adapters.PuzzleAdapter;
import com.evilgeniustechnologies.Wordrific.daomodel.Configuration;
import com.evilgeniustechnologies.Wordrific.daomodel.Puzzle;
import com.evilgeniustechnologies.Wordrific.daomodel.PuzzleDao;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.services.DawnIntentService;
import com.evilgeniustechnologies.Wordrific.utilties.DawnUtilities;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;
import com.evilgeniustechnologies.Wordrific.utilties.L;

import java.util.List;

/**
 * Created by Ivan on 5/29/2014.
 */
public class PlayActivity extends ServiceActivity {
    public static final String SET = "set";

    public static final int EASY = 1;
    public static final int MEDIUM = 2;
    public static final int HARD = 3;

    private static final int TOAST_DURATION = 2000;

    private DawnDatabase database;
    private Configuration configuration;
    private PlayTimerListener tickListener;
    private int score = 0;
    private int set = 0;
    private boolean loaded = false;
    private boolean finished = false;
    private boolean disable = false;
    private boolean bonus = false;
    private boolean dialogShowed = false;
    private long currentTime = 0;
    private long totalTime = 0;

    private ViewPager playPuzzle;
    private TextView playDifficulty;
    private Chronometer playTimer;
    private TextView playCoins;
    private WebView playClue;
    private ImageView previousClue;
    private ImageView nextClue;
    private TextView nextClueTitle;
    private PuzzleAdapter puzzleAdapter;
    private PuzzleDialog dialog;
//    private MediaPlayer tickPlayer;
    private MediaPlayer soundPlayer;
    private Toast notification;
    private Dialog publishDialog;
    private View bonusBackground;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = DawnDatabase.getInstance();
        configuration = database.getConfigurationDao().loadAll().get(0);

        if (getIntent() != null) {
            set = getIntent().getIntExtra(SET, 0);
            L.e("Set", set);
            if (DawnUtilities.isConnected(this)) {
                DawnIntentService.startActionLoadPuzzles(this, set);
            } else {
                List<Puzzle> puzzles = database.getPuzzleDao()
                        .queryBuilder()
                        .where(PuzzleDao.Properties.Set.eq(set))
                        .list();
                if (!puzzles.isEmpty()) {
                    onDisplay(puzzles);
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    protected void onRefresh(DawnDatabase.Status status, int progress) {
        if (status == DawnDatabase.Status.SCORE_PUBLISHED) {
            // Dismiss
            publishDialog.dismiss();
            finish();
        } else if (status == DawnDatabase.Status.PREPARING_PUZZLES) {
            DialogManager.updateProgress(progress);
        } else if (status == DawnDatabase.Status.PUZZLES_READY) {
            DialogManager.closeProgress();
            List<Puzzle> puzzles = database.getPuzzleDao()
                    .queryBuilder()
                    .where(PuzzleDao.Properties.Set.eq(set))
                    .list();
            onDisplay(puzzles);
        }
    }

    private void onDisplay(List<Puzzle> puzzles) {
        setContentView(R.layout.play_activity);
        loaded = true;
        playPuzzle = (ViewPager) findViewById(R.id.play_puzzle);
        playDifficulty = (TextView) findViewById(R.id.play_difficulty);
        playCoins = (TextView) findViewById(R.id.play_coins);
        playClue = (WebView) findViewById(R.id.play_clue);
        playClue.setBackgroundColor(0);
        previousClue = (ImageView) findViewById(R.id.play_previous);
        nextClue = (ImageView) findViewById(R.id.play_next);
        nextClueTitle = (TextView) findViewById(R.id.play_next_title);
        bonusBackground = findViewById(R.id.bonus_bg);

        puzzleAdapter = new PuzzleAdapter(
                this,
                playPuzzle,
                puzzles);
        dialog = new PuzzleDialog();

        if (playTimer == null) {
//            tickPlayer = MediaPlayer.create(this, R.raw.tick);
            tickListener = new PlayTimerListener();
            playTimer = (Chronometer) findViewById(R.id.play_timer);
            playTimer.setOnChronometerTickListener(tickListener);
            playTimer.start();
        }

        populateContent();
    }

    @Override
    protected void onPause() {
        if (playTimer != null) {
            playTimer.stop();
        }
//        if (tickPlayer != null) {
//            tickPlayer.release();
//            tickPlayer = null;
//        }
        if (soundPlayer != null) {
            soundPlayer.release();
            soundPlayer = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetTimer();
    }

    public void resetTimer() {
        if (loaded && !finished && !dialogShowed) {
//            tickPlayer = MediaPlayer.create(this, R.raw.tick);
            playTimer.stop();
            playTimer.setBase(SystemClock.elapsedRealtime() - currentTime);
            playTimer.start();
        }
    }

    public void dialogOff() {
        dialogShowed = false;
    }

    public void populateContent() {
        DawnUtilities.Tuple puzzle = puzzleAdapter.getCurrentPuzzle();
        if (((PuzzleAdapter.PuzzleState) puzzle.tail).getCurrentTime() != 0) {
            playTimer.stop();
            playTimer.setBase(((PuzzleAdapter.PuzzleState) puzzle.tail).getCurrentTime() + SystemClock.elapsedRealtime());
            playTimer.start();
        } else {
            playTimer.stop();
            playTimer.setBase(SystemClock.elapsedRealtime());
            playTimer.start();
        }
        tickListener.setDifficulty(((Puzzle) puzzle.head).getDifficulty(), (SystemClock.elapsedRealtime() - playTimer.getBase()) / 1000);
        switch (((Puzzle) puzzle.head).getDifficulty()) {
            case EASY:
                playDifficulty.setText((20 - puzzleAdapter.getCount()) + "/" + configuration.getTotalPuzzles() + " Easy");
                break;
            case MEDIUM:
                playDifficulty.setText((20 - puzzleAdapter.getCount()) + "/" + configuration.getTotalPuzzles() + " Medium");
                break;
            case HARD:
                playDifficulty.setText((20 - puzzleAdapter.getCount()) + "/" + configuration.getTotalPuzzles() + " Hard");
                break;
        }
        dialog.clearText();
        playCoins.setText(score + " points");
        playClue.loadData("", "text/html", null);
        displayClueState();
        displayNextClueLabel();
    }

    public void onAnswer(View v) {
        if (disable) {
            return;
        }
        switch (v.getId()) {
            case R.id.play_answer:
                playTimer.stop();
                dialogShowed = true;
                dialog.show(getFragmentManager(), "answer");
                break;
        }
    }

    public void onSkip(View v) {
        if (disable) {
            return;
        }
        totalTime += currentTime / 1000;
        L.e("totalTime", totalTime);
        soundPlayer = MediaPlayer.create(this, R.raw.wrong_answer);
        soundPlayer.start();
        showToast("Oops! You suffer " + configuration.getSkipPuzzlePenalty() + " points penalty");
        delay(TOAST_DURATION);
        updateScore(-configuration.getSkipPuzzlePenalty());
    }

    public void onExit(View v) {
        finished = true;
        playTimer.stop();
        finish();
    }

    public void displayClueState() {
        DawnUtilities.Tuple puzzle = puzzleAdapter.getCurrentPuzzle();
        PuzzleAdapter.PuzzleState puzzleState = (PuzzleAdapter.PuzzleState) puzzle.tail;
        if (bonus) {
            puzzleState.enableBonus();
            bonus = false;
        }
        DawnUtilities.Tuple clue = puzzleState.getCurrentClue();
        if (clue != null) {
            playClue.loadData((String) clue.head, "text/html", null);
        }
    }

    public void onNextClue(View v) {
        if (disable) {
            return;
        }
        DawnUtilities.Tuple puzzle = puzzleAdapter.getCurrentPuzzle();
        PuzzleAdapter.PuzzleState puzzleState = (PuzzleAdapter.PuzzleState) puzzle.tail;
        if (!puzzleState.isUsed()) {
            tickListener.playFadeOut();
        }
        DawnUtilities.Tuple clue = puzzleState.nextClue();
        if (!(Boolean) clue.tail) {
            switch (((Puzzle) puzzle.head).getDifficulty()) {
                case EASY:
                    showInterruptToast(configuration.getCluePenaltyEasy() + " points deducted for using clue");
                    updateScore(-configuration.getCluePenaltyEasy());
                    playClue.loadData((String) clue.head, "text/html", null);
                    clue.tail = true;
                    break;
                case MEDIUM:
                    showInterruptToast(configuration.getCluePenaltyMedium() + " points deducted for using clue");
                    updateScore(-configuration.getCluePenaltyMedium());
                    playClue.loadData((String) clue.head, "text/html", null);
                    clue.tail = true;
                    break;
                case HARD:
                    showInterruptToast(configuration.getCluePenaltyHard() + " points deducted for using clue");
                    updateScore(-configuration.getCluePenaltyHard());
                    playClue.loadData((String) clue.head, "text/html", null);
                    clue.tail = true;
                    break;
            }
        } else {
            playClue.loadData((String) clue.head, "text/html", null);
        }
        displayNextClueLabel();
    }

    private void displayNextClueLabel() {
        DawnUtilities.Tuple puzzle = puzzleAdapter.getCurrentPuzzle();
        PuzzleAdapter.PuzzleState puzzleState = (PuzzleAdapter.PuzzleState) puzzle.tail;

        if (!puzzleState.isNextCluePurchased()) {
            switch (((Puzzle) puzzle.head).getDifficulty()) {
                case EASY:
                    nextClueTitle.setText("NEXT (" + configuration.getCluePenaltyEasy() + " points)");
                    break;
                case MEDIUM:
                    nextClueTitle.setText("NEXT (" + configuration.getCluePenaltyMedium() + " points)");
                    break;
                case HARD:
                    nextClueTitle.setText("NEXT (" + configuration.getCluePenaltyHard() + " points)");
                    break;
            }
        } else {
            nextClueTitle.setText("NEXT");
        }
        if (puzzleState.isTop()) {
            previousClue.setAlpha(0.5f);
        } else {
            previousClue.setAlpha(1f);
        }
        if (puzzleState.isBottom()) {
            nextClue.setAlpha(0.5f);
        } else {
            nextClue.setAlpha(1f);
        }
    }

    public void onPreviousClue(View v) {
        if (disable) {
            return;
        }
        DawnUtilities.Tuple puzzle = puzzleAdapter.getCurrentPuzzle();
        PuzzleAdapter.PuzzleState puzzleState = (PuzzleAdapter.PuzzleState) puzzle.tail;
        DawnUtilities.Tuple clue = puzzleState.previousClue();
        if (clue != null) {
            playClue.loadData((String) clue.head, "text/html", null);
            displayNextClueLabel();
        }
    }

    private void analyzeAnswer(String answer) {
        DawnUtilities.Tuple puzzle = puzzleAdapter.getCurrentPuzzle();
        totalTime += currentTime / 1000;
        L.e("totalTime", totalTime);
        if (DawnUtilities.getCleanAnswer(((Puzzle) puzzle.head).getAnswer())
                .equals(DawnUtilities.getCleanAnswer(answer))) {
            soundPlayer = MediaPlayer.create(this, R.raw.correct_answer);
            soundPlayer.start();
            long answerTime = currentTime / 1000;
            PuzzleAdapter.PuzzleState state = (PuzzleAdapter.PuzzleState) puzzle.tail;
            switch (((Puzzle) puzzle.head).getDifficulty()) {
                case EASY:
                    showToast("Great! " + configuration.getCorrectAnswerEasy() + " points added for correct answer");
                    updateScore(configuration.getCorrectAnswerEasy());
                    if (answerTime <= configuration.getTimeForBonusEasy() && !state.isUsed()) {
                        bonus = true;
                        showToast("You got " + configuration.getQuickAnswerEasy() + " bonus for quick answer");
                        updateScore(configuration.getQuickAnswerEasy());
                        delay(TOAST_DURATION * 2);
                    } else {
                        delay(TOAST_DURATION);
                    }
                    break;
                case MEDIUM:
                    showToast("Great! " + configuration.getCorrectAnswerMedium() + " points added for correct answer");
                    updateScore(configuration.getCorrectAnswerMedium());
                    if (answerTime <= configuration.getTimeForBonusMedium() && !state.isUsed()) {
                        bonus = true;
                        showToast("You got " + configuration.getQuickAnswerMedium() + " bonus for quick answer");
                        updateScore(configuration.getQuickAnswerMedium());
                        delay(TOAST_DURATION * 2);
                    } else {
                        delay(TOAST_DURATION);
                    }
                    break;
                case HARD:
                    showToast("Great! " + configuration.getCorrectAnswerHard() + " points added for correct answer");
                    updateScore(configuration.getCorrectAnswerHard());
                    if (answerTime <= configuration.getTimeForBonusHard() && !state.isUsed()) {
                        bonus = true;
                        showToast("You got " + configuration.getQuickAnswerHard() + " bonus for quick answer");
                        updateScore(configuration.getQuickAnswerHard());
                        delay(TOAST_DURATION * 2);
                    } else {
                        delay(TOAST_DURATION);
                    }
                    break;
            }
        } else {
            soundPlayer = MediaPlayer.create(this, R.raw.wrong_answer);
            soundPlayer.start();
            switch (((Puzzle) puzzle.head).getDifficulty()) {
                case EASY:
                    showToast("Oops! " + configuration.getWrongAnswerEasy() + " points deducted for wrong answer");
                    updateScore(-configuration.getWrongAnswerEasy());
                    showToast("\"" + ((Puzzle) puzzle.head).getAnswer() + "\"");
                    delay(TOAST_DURATION * 2);
                    break;
                case MEDIUM:
                    showToast("Oops! " + configuration.getWrongAnswerMedium() + " points deducted for wrong answer");
                    updateScore(-configuration.getWrongAnswerMedium());
                    showToast("\"" + ((Puzzle) puzzle.head).getAnswer() + "\"");
                    delay(TOAST_DURATION * 2);
                    break;
                case HARD:
                    showToast("Oops! " + configuration.getWrongAnswerHard() + " points deducted for wrong answer");
                    updateScore(-configuration.getWrongAnswerHard());
                    showToast("\"" + ((Puzzle) puzzle.head).getAnswer() + "\"");
                    delay(TOAST_DURATION * 2);
                    break;
            }
        }
    }

    private void updateScore(int value) {
        score += value;
        playCoins.setText(score + " points");
    }

    private void showInterruptToast(String message) {
        if (notification != null) {
            notification.cancel();
        }
        showToast(message);
    }

    private void showToast(String message) {
        notification = Toast.makeText(this, Html.fromHtml(message), Toast.LENGTH_SHORT);
        notification.setGravity(Gravity.TOP, 0, 0);
        notification.show();
    }

    private void delay(int duration) {
        playTimer.stop();
        disable = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loaded && !finished) {
                    if (!puzzleAdapter.skipPuzzle()) {
                        gameFinished();
                    } else {
                        populateContent();
                    }
                    disable = false;
                }
            }
        }, duration);
    }

    private void gameFinished() {
        soundPlayer = MediaPlayer.create(this, R.raw.win);
        soundPlayer.start();
        playTimer.stop();
        finished = true;

        if (score < 0) {
            DialogManager.show(this, DialogManager.Alert.GAME_FINISH, "You lose", "Game Over");
            return;
        }

        showPublishDialog(set, score, (int) totalTime);
    }

    private void showPublishDialog(int set, int score, int time) {
        publishDialog = new Dialog(this);
        publishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        publishDialog.setContentView(R.layout.publish);

        ImageView exitBtn = (ImageView) publishDialog.findViewById(R.id.publish_exit);
        TextView setTxt = (TextView) publishDialog.findViewById(R.id.publish_set);
        TextView scoreTxt = (TextView) publishDialog.findViewById(R.id.publish_score);
        TextView timeTxt = (TextView) publishDialog.findViewById(R.id.publish_time);
        TextView scoreAveTxt = (TextView) publishDialog.findViewById(R.id.publish_ave_score);
        TextView timeAveTxt = (TextView) publishDialog.findViewById(R.id.publish_ave_time);
        ImageView publishBtn = (ImageView) publishDialog.findViewById(R.id.publish_button);

        PublishListener listener = new PublishListener(publishDialog, set, score, time);

        exitBtn.setOnClickListener(listener);
        setTxt.setText("SET #" + set);
        scoreTxt.setText("" + score);
        timeTxt.setText("" + time);
        scoreAveTxt.setText("" + (Math.round(score / 20.0 * 10.0) / 10.0));
        timeAveTxt.setText("" + (Math.round(time / 20.0 * 10.0) / 10.0));
        publishBtn.setOnClickListener(listener);

        Window publishDialogWindow = publishDialog.getWindow();
        publishDialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams publishDialogLayout = publishDialogWindow.getAttributes();
        publishDialogLayout.gravity = Gravity.CENTER;
        publishDialogWindow.setAttributes(publishDialogLayout);

        publishDialog.show();
    }

    public static class PuzzleDialog extends DialogFragment {
        private EditText answerField;
        private boolean answerClicked = false;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final View view = View.inflate(getActivity(), R.layout.answer_dialog, null);
            builder.setView(view);
            answerField = (EditText) view.findViewById(R.id.dialog_answer);
            answerField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            });
            ImageView answerButton = (ImageView) view.findViewById(R.id.dialog_answer_button);
            answerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answerClicked = true;
                    String answer = answerField.getText().toString();
                    onReceiveResult(answer);
                    dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.gravity = Gravity.BOTTOM;
            return dialog;
        }

        public void onReceiveResult(String answer) {
            L.e("answer", answer);
            ((PlayActivity) getActivity()).analyzeAnswer(answer);
        }

        public void clearText() {
            if (answerField != null) {
                answerField.setText("");
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            ((PlayActivity) getActivity()).dialogOff();
            if (!answerClicked) {
                ((PlayActivity) getActivity()).resetTimer();
            }
            answerClicked = false;
        }
    }

    private class PlayTimerListener implements Chronometer.OnChronometerTickListener {
        private int difficulty;
        private ObjectAnimator fadeOut;

        public PlayTimerListener() {
            fadeOut = ObjectAnimator.ofFloat(bonusBackground, "alpha", 0f);
            fadeOut.setDuration(1000);
        }

        public void setDifficulty(int difficulty, long elapsedTime) {
            this.difficulty = difficulty;
            fadeOut.cancel();
            DawnUtilities.Tuple puzzle = puzzleAdapter.getCurrentPuzzle();
            PuzzleAdapter.PuzzleState state = (PuzzleAdapter.PuzzleState) puzzle.tail;
            switch (difficulty) {
                case EASY:
                    if (configuration.getTimeForBonusEasy() -1 >= elapsedTime && !state.isUsed()) {
                        bonusBackground.setAlpha(0.6f);
                    } else {
                        bonusBackground.setAlpha(0f);
                    }
                    break;
                case MEDIUM:
                    if (configuration.getTimeForBonusMedium() -1 >= elapsedTime && !state.isUsed()) {
                        bonusBackground.setAlpha(0.6f);
                    } else {
                        bonusBackground.setAlpha(0f);
                    }
                    break;
                case HARD:
                    if (configuration.getTimeForBonusHard() -1 >= elapsedTime && !state.isUsed()) {
                        bonusBackground.setAlpha(0.6f);
                    } else {
                        bonusBackground.setAlpha(0f);
                    }
                    break;
            }
        }

        @Override
        public void onChronometerTick(Chronometer chronometer) {
            long elapsedTime = (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
            L.e("ticktock", elapsedTime);
//            if (tickPlayer != null) {
//                tickPlayer.start();
//            }
            DawnUtilities.Tuple puzzle = puzzleAdapter.getCurrentPuzzle();
            PuzzleAdapter.PuzzleState state = (PuzzleAdapter.PuzzleState) puzzle.tail;
            switch (difficulty) {
                case EASY:
                    if (configuration.getTimeForBonusEasy() - elapsedTime == 1 && !state.isUsed()) {
                        fadeOut.start();
                    }
                    break;
                case MEDIUM:
                    if (configuration.getTimeForBonusMedium() - elapsedTime == 1 && !state.isUsed()) {
                        fadeOut.start();
                    }
                    break;
                case HARD:
                    if (configuration.getTimeForBonusHard() - elapsedTime == 1 && !state.isUsed()) {
                        fadeOut.start();
                    }
                    break;
            }
            ((PuzzleAdapter.PuzzleState) puzzle.tail).setCurrentTime(chronometer.getBase() - SystemClock.elapsedRealtime());
            currentTime = SystemClock.elapsedRealtime() - chronometer.getBase();
        }

        public void playFadeOut() {
            fadeOut.start();
        }
    }

    private class PublishListener implements View.OnClickListener {
        private Dialog dialog;
        private int set;
        private int score;
        private int time;

        public PublishListener(Dialog dialog, int set, int score, int time) {
            this.dialog = dialog;
            this.set = set;
            this.score = score;
            this.time = time;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.publish_button:
                    // If no user is currently logged in
                    if (database.getCurrentUser() == null) {
                        Intent intent = new Intent(PlayActivity.this, LoginActivity.class);
                        startActivity(intent);
                        return;
                    }
                    DawnIntentService.startActionPublishScore(PlayActivity.this, score, time, set);
                    break;
                case R.id.publish_exit:
                    // Dismiss
                    dialog.dismiss();
                    PlayActivity.this.finish();
                    break;
            }
        }
    }
}