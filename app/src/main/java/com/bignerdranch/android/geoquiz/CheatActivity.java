package com.bignerdranch.android.geoquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CheatActivity extends AppCompatActivity {

    private static final String ANSWER_IS_SHOWN_KEY = "mAnswerIsShown";

    private static final String EXTRA_ANSWER_IS_TRUE = "com.android.bignerdranch.geoquiz.answer_is_true";
    private static final String EXTRA_ANSWER_IS_SHOWN = "com.android.bignerdranch.geoquiz.answer_is_shown";
    private boolean mAnswerIsTrue;
    private boolean mAnswerIsShown;

    private TextView mAnswerTextView;
    private TextView mApiLevelTextView;
    private Button mShowAnswerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);

        if (savedInstanceState != null) {
            mAnswerIsShown = savedInstanceState.getBoolean(ANSWER_IS_SHOWN_KEY, false);
        }

        mAnswerIsTrue = getIntent().getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false);

        mAnswerTextView = findViewById(R.id.answer_text_view);

        mApiLevelTextView = findViewById(R.id.api_level_text_view);
        mApiLevelTextView.setText(String.format("API Level %s", Build.VERSION.SDK_INT));

        mShowAnswerButton = findViewById(R.id.show_answer_button);
        mShowAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAnswer();
                hideShowAnswerButton();
            }
        });

        // if answer was already shown - keep it on the screen (in case user closed-opened the app or rotated the screen
        if (mAnswerIsShown) {
            showAnswer();
            hideShowAnswerButton();
        }
    }

    /**
     * Saving the state of the activity for keeping consistency in crucial variables.
     *
     * @param savedInstanceState bundle with saved parameters
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // save the fact if user has already cheated
        savedInstanceState.putBoolean(ANSWER_IS_SHOWN_KEY, mAnswerIsShown);
    }

    /**
     * Create newIntent to start CheatActivity from QuizActivity.
     * During the initialization the method also passes the answer to the current question from QuizActivity.
     *
     * @param packageContext parent activity application context
     * @param answerIsTrue   true, if answer to the current question is true.
     * @return intent with extra of boolean value (the answer to the current question)
     */
    public static Intent newIntent(Context packageContext, boolean answerIsTrue) {
        Intent intent = new Intent(packageContext, CheatActivity.class);
        intent.putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue);
        return intent;
    }

    /**
     * Public method for parent activity (QuizActivity) to extract data from child activity's intent.
     * In this case, method returns true/false depending on user pressing the SHOW ANSWER button.
     *
     * @param result intent that was sent from CheatActivity back to QuizActivity
     * @return true, if answer was shown (cheating)
     */
    public static boolean wasAnswerShown(Intent result) {
        return result.getBooleanExtra(EXTRA_ANSWER_IS_SHOWN, false);
    }

    /**
     * Setting the results to send back to QuizActivity.
     * We create Intent and put the boolean flag ( = true, when user presses "Show Answer" button).
     * QuizActivity can obtain this value for its purpose via CheatActivity.wasAnswerShown(Intent) method.
     *
     * @param isAnswerShown true, if user was shown the answer (cheating)
     */
    private void setAnswerShownResult(boolean isAnswerShown) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ANSWER_IS_SHOWN, isAnswerShown);
        setResult(RESULT_OK, data); // setting RESULT_OK for QuizActivity to refer back to

        mAnswerIsShown = isAnswerShown;
    }

    /**
     * Show actual answer to the question.
     * Method sets text data to mAnswerTextView on CheatActivity layout.
     */
    private void showAnswer() {
        if (mAnswerIsTrue) {
            mAnswerTextView.setText(R.string.true_button);
        } else {
            mAnswerTextView.setText(R.string.false_button);
        }
        setAnswerShownResult(true);
    }

    /**
     * Hide "SHOW ANSWER".
     * For API Level 21 and higher -- adds animation while setting visibility to INVISIBLE.
     * For API Level 20 and lower -- just hides the button from the screen using standard method setVisibility()
     */
    private void hideShowAnswerButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = mShowAnswerButton.getWidth() / 2;
            int cy = mShowAnswerButton.getHeight() / 2;
            float radius = mShowAnswerButton.getWidth();

            Animator anim = ViewAnimationUtils.createCircularReveal(mShowAnswerButton, cx, cy, radius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mShowAnswerButton.setVisibility(View.INVISIBLE);
                }
            });
            anim.start();
        } else {
            mShowAnswerButton.setVisibility(View.INVISIBLE);
        }
    }
}
