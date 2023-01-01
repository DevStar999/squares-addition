package com.nerdcoredevelopment.squaresaddition;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

public class GamingZoneFragment extends Fragment {
    private Context context;
    private OnGamingZoneFragmentInteractionListener mListener;
    private SharedPreferences sharedPreferences;
    private ConstraintLayout rootConstraintLayout;
    private AppCompatImageView backButton;
    private boolean isFinalScoreCalculated;
    private boolean hasUserScoreBeenVisited;
    private AppCompatTextView bestScoreTextView;
    private TextInputLayout userScoreTextInputLayout;
    private AppCompatEditText userScoreEditText;
    private AppCompatTextView mysteryFactorTextView;
    private AppCompatTextView finalScoreTextView;
    private Button actionButton;

    public GamingZoneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void settingOnClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onGamingZoneFragmentInteractionBackClicked();
                }
            }
        });
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFinalScoreCalculated) { // Play the game and calculate the final score
                    if (!userScoreTextInputLayout.isErrorEnabled()) {
                        if (userScoreEditText.getText() != null && !userScoreEditText.getText().toString().isEmpty()) {
                            // Input is not empty and since errorEnabled is not true, then it must be valid value,
                            // so go we ahead
                        } else if (userScoreEditText.getText() != null) {
                            // Input is empty
                            userScoreTextInputLayout.setErrorEnabled(true);
                            userScoreTextInputLayout.setError("Please enter a score between 1 to 1000");
                            Toast.makeText(context, "Please enter a score between 1 to 1000", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else { // errorEnabled is true so we can't move ahead with an invalid value entered by the user
                        Toast.makeText(context, "Please enter a score between 1 to 1000", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Defining the range
                    int max = 10;
                    int min = 1;
                    int range = max - min + 1;
                    int mysteryFactor = (int) (Math.random() * range) + min;
                    int enteredUserScore = Integer.parseInt(userScoreEditText.getText().toString());
                    int finalScore = enteredUserScore * mysteryFactor;
                    mysteryFactorTextView.setText(String.valueOf(mysteryFactor));
                    finalScoreTextView.setText(enteredUserScore + " x " + mysteryFactor
                            + " = " + (finalScore));
                    isFinalScoreCalculated = true;
                    userScoreEditText.setEnabled(false);
                    if (finalScore > Integer.parseInt(bestScoreTextView.getText().toString())) {
                        sharedPreferences.edit().putInt("bestScore", finalScore).apply();
                        bestScoreTextView.setText(String.valueOf(finalScore));
                    }
                    actionButton.setText("RESET GAME \uD83D\uDD04️");
                } else { // Reset the game to begin fresh
                    isFinalScoreCalculated = false;
                    hasUserScoreBeenVisited = false;
                    bestScoreTextView.setText(String.valueOf(sharedPreferences.getInt("bestScore", 0)));
                    userScoreEditText.setEnabled(true);
                    if (userScoreEditText.getText().toString().length() > 0) {
                        userScoreEditText.getText().clear();
                    }
                    userScoreTextInputLayout.setErrorEnabled(false);
                    mysteryFactorTextView.setText("?");
                    finalScoreTextView.setText("?");
                    actionButton.setText("REVEAL FINAL SCORE");
                }
            }
        });
    }

    private void initialiseViews(View layoutView) {
        rootConstraintLayout = layoutView.findViewById(R.id.root_layout_gaming_zone_fragment);
        backButton = layoutView.findViewById(R.id.title_back_gaming_zone_fragment_button);
        bestScoreTextView = layoutView.findViewById(R.id.best_score_value_text_view);
        bestScoreTextView.setText(String.valueOf(sharedPreferences.getInt("bestScore", 0)));
        userScoreTextInputLayout = layoutView.findViewById(R.id.user_score_text_input_layout);
        userScoreEditText = layoutView.findViewById(R.id.user_score_app_compat_edit_text);
        mysteryFactorTextView = layoutView.findViewById(R.id.mystery_factor_value_text_view);
        finalScoreTextView = layoutView.findViewById(R.id.final_score_value_text_view);
        actionButton = layoutView.findViewById(R.id.action_button);
        if (!isFinalScoreCalculated) {
            actionButton.setText("REVEAL FINAL SCORE");
        } else {
            actionButton.setText("RESET GAME \uD83D\uDD04️");
        }
    }

    private void handleNumberEntered() {
        if (userScoreEditText.getText().toString().length() > 10) {
            userScoreTextInputLayout.setErrorEnabled(true);
            userScoreTextInputLayout.setError("Please enter a score between 1 to 1000");
        } else {
            long valueOfUserScore = Long.parseLong(userScoreEditText.getText().toString());
            if (valueOfUserScore >= 1 && valueOfUserScore <= 1000) {
                userScoreTextInputLayout.setErrorEnabled(false);
            } else {
                userScoreTextInputLayout.setErrorEnabled(true);
                userScoreTextInputLayout.setError("Please enter a score between 1 to 1000");
            }
        }
    }

    private void hideKeyboardAndRemoveFocus() {
        userScoreEditText.clearFocus();
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(userScoreEditText.getWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        sharedPreferences = context.getSharedPreferences("com.nerdcoredevelopment.inappbillingdemo", Context.MODE_PRIVATE);

        isFinalScoreCalculated = false;
        hasUserScoreBeenVisited = false;

        View view = inflater.inflate(R.layout.fragment_gaming_zone, container, false);

        initialiseViews(view);

        userScoreEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            /* The following changes are made so that when the user enters some text the error
               message for the AppCompatEditText immediately is gone. */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userScoreEditText.getText() != null && !userScoreEditText.getText().toString().isEmpty()) {
                    // Input is not empty
                    handleNumberEntered();
                } else if (userScoreEditText.getText() != null) {
                    // Input is empty
                    userScoreTextInputLayout.setErrorEnabled(true);
                    userScoreTextInputLayout.setError("Please enter a score between 1 to 1000");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        userScoreEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* The following changes are made so that the error is displayed when for the first
                   focus is on this AppCompatEditText and the text inside it is empty or after this
                   anytime the text inside is left empty while user is focusing on other views or
                   simply doing something else. */
                if (userScoreEditText.getText() != null && userScoreEditText.getText().toString().isEmpty()
                        && hasUserScoreBeenVisited) {
                    // Input is empty and was visited before
                    userScoreTextInputLayout.setErrorEnabled(true);
                    userScoreTextInputLayout.setError("Please enter a score between 1 to 1000");
                } else {
                    if (userScoreEditText.getText() != null && userScoreEditText.getText().toString().isEmpty()) {
                        // Input is empty but was not visited before
                        userScoreTextInputLayout.setErrorEnabled(false);
                    } else if (userScoreEditText.getText() != null && !userScoreEditText.getText().toString().isEmpty()) {
                        // Input is not empty and was not visited before either
                        handleNumberEntered();
                    }
                }

                /* Making a tweak so that when the user focuses on the 'Username' text field for the
                   very first time and it is empty by default, then before the user enters some text
                   the error message is not displayed, making the user experience that much better. */
                hasUserScoreBeenVisited = (!hasUserScoreBeenVisited) ? true : hasUserScoreBeenVisited;
            }
        });

        /*  The following are neat little tricks to get out of focus from a view for e.g. the AppCompatEditText(s)
            used in this layout, when we click on done OR at any arbitrary point on the screen respectively.
        */
        userScoreEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboardAndRemoveFocus();
                    return true;
                }
                return false;
            }
        });
        rootConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboardAndRemoveFocus();
            }
        });

        settingOnClickListeners();

        return view;
    }

    public interface OnGamingZoneFragmentInteractionListener {
        void onGamingZoneFragmentInteractionBackClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGamingZoneFragmentInteractionListener) {
            mListener = (OnGamingZoneFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnGamingZoneFragmentInteractionListener");
        }
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
