package com.bignerdranch.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {

    // string keys for saving and loading various parameters of the activity
    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String ANSWERED_QUESTIONS_KEY = "answeredQuestions";
    private static final String CORRECT_ANSWERS_KEY = "correctAnswers";
    private static final String FINISHED_KEY = "isQuizFinished";
    private static final String CHEATER_KEY = "isCheater";
    private static final String CHEAT_COUNTER_KEY = "mCheatCount";

    private static final int REQUEST_CODE_CHEAT = 0;
    private static final int MAX_CHEAT_COUNT = 3;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mStartAgainButton;
    private Button mCheatButton;
    private ImageButton mPrevButton;
    private ImageButton mNextButton;
    private TextView mQuestionTextView;
    private int mCorrectAnswers;
    private boolean isQuizFinished;
    private boolean mIsCheater;

    private final Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true)
    };

    private HashMap<Integer, Boolean> mAnsweredQuestions = new HashMap<>(mQuestionBank.length);

    {
        clearInitAnsweredQuestions();
    }

    private int mCurrentIndex = 0;
    private int mCheatCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        // Loading the state of the app if user got back to it in some way
        if (savedInstanceState != null) {

            // obtaining the question that was active previously
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);

            // obtaining the number of correct answers
            mCorrectAnswers = savedInstanceState.getInt(CORRECT_ANSWERS_KEY, 0);

            // obtaining the collection of all the answered questions to date
            try {
                if (savedInstanceState.getSerializable(ANSWERED_QUESTIONS_KEY) instanceof HashMap)
                    mAnsweredQuestions = (HashMap<Integer, Boolean>) savedInstanceState.get(ANSWERED_QUESTIONS_KEY);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }

            // obtaining the flag of finished quiz
            isQuizFinished = savedInstanceState.getBoolean(FINISHED_KEY, false);

            // obtaining the flag if user has cheated
            mIsCheater = savedInstanceState.getBoolean(CHEATER_KEY, false);

            // obtaining the flag if user has cheated
            mCheatCount = savedInstanceState.getInt(CHEAT_COUNTER_KEY, 0);
        }

        mQuestionTextView = findViewById(R.id.question_text_view);
        // user can get the next question by pressing the text of the question
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateQuestion(true);
            }
        });

        mTrueButton = findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(true);
            }
        });

        mFalseButton = findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(false);
            }
        });

        mPrevButton = findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateQuestion(false);
            }
        });

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateQuestion(true);
            }
        });

        mStartAgainButton = findViewById(R.id.start_again_button);
        mStartAgainButton.setVisibility(View.INVISIBLE);
        mStartAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAgain();
            }
        });

        mCheatButton = findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        initQuestions();
    }

    /**
     * Handling the response from CheatActivity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);

            if (mIsCheater) { // if user has successfully cheated, then increment the "cheat count"
                mCheatCount++;
            }

            // disable CHEAT! button only if cheat count is maxed out after returning to QuizActivity
            checkCheatCount(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    /**
     * Standard method for saving crucial variables of the app to facilitate the workflow
     * and keep the state intact in various cases (changing the app, rotating the phone...)
     *
     * @param savedInstanceState app state entity (saved variables etc...)
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");

        // save current index for the active question
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);

        // save the collection of answered questions
        savedInstanceState.putSerializable(ANSWERED_QUESTIONS_KEY, mAnsweredQuestions);

        // save number of correct answers
        savedInstanceState.putInt(CORRECT_ANSWERS_KEY, mCorrectAnswers);

        // save the flag if the quiz is finished
        savedInstanceState.putBoolean(FINISHED_KEY, isQuizFinished);

        // save the flag if user has cheated on a question
        savedInstanceState.putBoolean(CHEATER_KEY, mIsCheater);

        // save the number of cheats in the session
        savedInstanceState.putInt(CHEAT_COUNTER_KEY, mCheatCount);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    /**
     * Initializing questions after starting the app or after opening up again.
     */
    private void initQuestions() {
        mQuestionTextView.setText(mQuestionBank[mCurrentIndex].getTextResId());
        changeButtonsAfterUpdate();
    }

    /**
     * Changing the question on the screen after user presses NEXT or PREV buttons.
     * If the current question is the first/last one, then the sequence may loop.
     *
     * @param isNext true if user pressed NEXT, false - PREV.
     */
    private void updateQuestion(boolean isNext) {
        // update the index of the question
        if (isNext) {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
        } else {
            if (mCurrentIndex != 0) {
                mCurrentIndex = mCurrentIndex - 1;
            } else {
                mCurrentIndex = mQuestionBank.length - 1;
            }
        }

        // updating the text of the question
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);

        changeButtonsAfterUpdate();
    }

    /**
     * This function checks the answer for correctness.
     * User gets the appropriate message (Correct/Incorrect) depending on her answer.
     * After receiving the answer the app locks answer buttons.
     * Also the app checks whether the answered question was the last one and orchestrates the closure.
     *
     * @param userPressedTrue true, if user pressed "Correct", false - "Incorrect"
     */
    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId;

        if (mIsCheater) { // check if user has cheated on CheatActivity
            messageResId = R.string.judgement_toast;
            mIsCheater = false;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                mCorrectAnswers++;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }
        // checking the question as answered
        mAnsweredQuestions.put(mCurrentIndex, true);

        // show info message depending on the answer
        Toast toast = Toast.makeText(
                QuizActivity.this, messageResId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 400);
        toast.show();

        lockAnswerButtons();

        // if the current question was the last one, the app locks the buttons and shows the feedback about the result
        if (!checkIfFinished()) { // if the quiz is finished the question won't update
            // after giving the answer the question automatically updates to the next one
            updateQuestion(true);
        }
    }

    /**
     * Method to lock answer buttons (Correct/Incorrect).
     */
    private void lockAnswerButtons() {
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);
        checkCheatCount(false);
    }

    /**
     * Method to unlock answer buttons (Correct/Incorrect).
     */
    private void unlockAnswerButtons() {
        mTrueButton.setEnabled(true);
        mFalseButton.setEnabled(true);
        checkCheatCount(true);
    }

    /**
     * Method to lock navigation buttons (PREV/NEXT).
     */
    private void lockNavigationButtons() {
        mPrevButton.setEnabled(false);
        mNextButton.setEnabled(false);
        mQuestionTextView.setClickable(false);
    }

    /**
     * Method to lock navigation buttons (PREV/NEXT).
     */
    private void unlockNavigationButtons() {
        mPrevButton.setEnabled(true);
        mNextButton.setEnabled(true);
        mQuestionTextView.setClickable(true);
    }

    /**
     * Showing message about the result at the end of the quiz (after all questions are answered)
     */
    private void showFinalResult() {
        double finalResult = (double) mCorrectAnswers / mQuestionBank.length * 100;
        StringBuilder finalReviewMessage = new StringBuilder()
                .append("Well done! You've scored ")
                .append((int) finalResult)
                .append("% correct answers!");

        Toast.makeText(
                QuizActivity.this,
                String.format(Locale.ENGLISH, "Well done! You've scored %d%% correct answers!", (int) finalResult),
                Toast.LENGTH_LONG).show();

        isQuizFinished = true;
    }

    /**
     * Updating buttons' state after an action.
     * If there's still some questions unanswered AND the question is marked as answered - answer buttons are locked.
     * If there's no more questions to answer - answer/navigation buttons are locked.
     * Otherwise answer buttons are kept unlocked (quiz is just initialized) .
     */
    @SuppressWarnings("ConstantConditions")
    public void changeButtonsAfterUpdate() {
        if (mAnsweredQuestions.get(mCurrentIndex) &&
                !isQuizFinished) {
            lockAnswerButtons();
        } else if (isQuizFinished) {
            lockAnswerButtons();
            lockNavigationButtons();
            mStartAgainButton.setVisibility(View.VISIBLE);
        } else {
            unlockAnswerButtons();
            unlockNavigationButtons();
            mStartAgainButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Check if the quiz is finished by iterating through collection of answered questions.
     * If all the values in the collection are true - quiz is finished.
     * Otherwise - some questions are still unanswered.
     * <p>
     * Returns true if the quiz is finished, otherwise - false.
     */
    private boolean checkIfFinished() {
        int counter = 0;
        for (Map.Entry<Integer, Boolean> entry : mAnsweredQuestions.entrySet()) {
            if (!entry.getValue()) {
                return false; // exiting the method if getting unanswered question
            } else {
                counter++; //counting the number of answered questions
            }

            if (counter == mQuestionBank.length) {
                showFinalResult();
                lockNavigationButtons();
                mStartAgainButton.setVisibility(View.VISIBLE);
                return true;
            }
        }
        return false;
    }

    /**
     * Method to reinitialize the quiz from the beginning.
     * Clears counters, sets the question to the first one, unlocks the buttons etc...
     */
    private void startAgain() {
        clearInitAnsweredQuestions();
        mCorrectAnswers = 0;
        isQuizFinished = false;
        mCurrentIndex = 0;
        mCheatCount = 0;
        mQuestionTextView.setText(mQuestionBank[mCurrentIndex].getTextResId());
        changeButtonsAfterUpdate();
    }

    /**
     * Additional method for filling answered questions collection
     * with indexes according to the total number of questions.
     * Helps to avoid getting NPE on accessing.
     */
    private void clearInitAnsweredQuestions() {
        Integer i = 0;
        for (Question q : mQuestionBank) {
            mAnsweredQuestions.put(i, false);
            i++;
        }
    }

    /**
     * Additional check for CHEAT! button.
     * If the number of cheats is higher than 2, then the button is disabled.
     *
     * @param isHardEnable true, if unlockAnswerButtons() was called
     */
    private void checkCheatCount(boolean isHardEnable) {
        if (mCheatCount >= MAX_CHEAT_COUNT) { // disable CHEAT button if number of cheats maxed out
            mCheatButton.setEnabled(false);
        } else {
            if (isHardEnable) { // enabled CHEAT button in cases of positive updating buttons
                mCheatButton.setEnabled(true);
            } else {
                mCheatButton.setEnabled(false);
            }
        }
    }
}
