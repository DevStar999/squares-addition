package com.nerdcoredevelopment.squaresaddition;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class GamingZoneFragment extends Fragment {
    private Context context;
    private OnGamingZoneFragmentInteractionListener mListener;
    private SharedPreferences sharedPreferences;
    private AppCompatImageView backButton;
    private boolean isFinalScoreCalculated;
    private AppCompatTextView bestScoreTextView;
    private TextInputLayout firstNumberTextInputLayout;
    private TextInputLayout secondNumberTextInputLayout;
    private AutoCompleteTextView firstNumberValueTextView;
    private AutoCompleteTextView secondNumberValueTextView;
    private AppCompatTextView mysteryBonusTextView;
    private LinearLayout finalScoreLine1LinearLayout;
    private LinearLayout finalScoreLine2LinearLayout;
    private AppCompatTextView finalScoreLine1TextView;
    private AppCompatTextView finalScoreLine2TextView;
    private AppCompatTextView finalScoreLine3TextView;
    private Button actionButton;

    public GamingZoneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initialiseViews(View layoutView) {
        backButton = layoutView.findViewById(R.id.title_back_gaming_zone_fragment_button);
        bestScoreTextView = layoutView.findViewById(R.id.best_score_value_text_view);
        bestScoreTextView.setText(String.valueOf(sharedPreferences.getInt("bestScore", 0)));
        firstNumberTextInputLayout = layoutView.findViewById(R.id.first_number_text_input_layout);
        secondNumberTextInputLayout = layoutView.findViewById(R.id.second_number_text_input_layout);
        firstNumberValueTextView = layoutView.findViewById(R.id.first_number_value_text);
        secondNumberValueTextView = layoutView.findViewById(R.id.second_number_value_text);
        mysteryBonusTextView = layoutView.findViewById(R.id.mystery_bonus_value_text_view);
        finalScoreLine1LinearLayout = layoutView.findViewById(R.id.final_score_line1_linear_layout);
        finalScoreLine2LinearLayout = layoutView.findViewById(R.id.final_score_line2_linear_layout);
        finalScoreLine1TextView = layoutView.findViewById(R.id.final_score_value_line1_text_view);
        finalScoreLine2TextView = layoutView.findViewById(R.id.final_score_value_line2_text_view);
        finalScoreLine3TextView = layoutView.findViewById(R.id.final_score_value_line3_text_view);
        actionButton = layoutView.findViewById(R.id.action_button);
        if (!isFinalScoreCalculated) {
            actionButton.setText("REVEAL FINAL SCORE");
        } else {
            actionButton.setText("RESET GAME \uD83D\uDD04️");
        }
    }

    private void setNumberOptionMenus() {
        List<String> numberOptions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            numberOptions.add(String.valueOf(i));
        }
        ArrayAdapter<String> firstNumberAdapter = new ArrayAdapter<>(context, R.layout.drop_down_item, numberOptions);
        firstNumberValueTextView.setAdapter(firstNumberAdapter);
        firstNumberValueTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                firstNumberValueTextView.setText((String) adapterView.getItemAtPosition(position), false);
            }
        });
        ArrayAdapter<String> secondNumberAdapter = new ArrayAdapter<>(context, R.layout.drop_down_item, numberOptions);
        secondNumberValueTextView.setAdapter(secondNumberAdapter);
        secondNumberValueTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                secondNumberValueTextView.setText((String) adapterView.getItemAtPosition(position), false);
            }
        });
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
                    int max = 100;
                    int min = 1;
                    int range = max - min + 1;
                    int mysteryBonus = (int) (Math.random() * range) + min;
                    int enteredFirstNumber = Integer.parseInt(firstNumberValueTextView.getText().toString());
                    int enteredSecondNumber = Integer.parseInt(secondNumberValueTextView.getText().toString());
                    int finalScore = (enteredFirstNumber * enteredFirstNumber) +
                            (enteredSecondNumber * enteredSecondNumber) + mysteryBonus;

                    firstNumberTextInputLayout.setEnabled(false);
                    secondNumberTextInputLayout.setEnabled(false);
                    mysteryBonusTextView.setText(String.valueOf(mysteryBonus));
                    finalScoreLine1LinearLayout.setVisibility(View.VISIBLE);
                    finalScoreLine1TextView.setText("(" + enteredFirstNumber + ")^2 + "
                            + "(" + enteredSecondNumber + ")^2 + " + mysteryBonus);
                    finalScoreLine2LinearLayout.setVisibility(View.VISIBLE);
                    finalScoreLine2TextView.setText((enteredFirstNumber * enteredFirstNumber) + " + " +
                            (enteredSecondNumber * enteredSecondNumber) + " + " + mysteryBonus);
                    finalScoreLine3TextView.setText(String.valueOf(finalScore));
                    isFinalScoreCalculated = true;
                    if (finalScore > Integer.parseInt(bestScoreTextView.getText().toString())) {
                        sharedPreferences.edit().putInt("bestScore", finalScore).apply();
                        bestScoreTextView.setText(String.valueOf(finalScore));
                    }
                    actionButton.setText("RESET GAME \uD83D\uDD04️");
                } else { // Reset the game to begin fresh
                    firstNumberTextInputLayout.setEnabled(true);
                    secondNumberTextInputLayout.setEnabled(true);
                    isFinalScoreCalculated = false;
                    bestScoreTextView.setText(String.valueOf(sharedPreferences.getInt("bestScore", 0)));
                    firstNumberValueTextView.setText("1");
                    secondNumberValueTextView.setText("1");
                    mysteryBonusTextView.setText("?");
                    finalScoreLine1LinearLayout.setVisibility(View.INVISIBLE);
                    finalScoreLine1TextView.setText("");
                    finalScoreLine2LinearLayout.setVisibility(View.INVISIBLE);
                    finalScoreLine2TextView.setText("");
                    finalScoreLine3TextView.setText("?");
                    actionButton.setText("REVEAL FINAL SCORE");
                    setNumberOptionMenus();
                }
            }
        });
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

        View view = inflater.inflate(R.layout.fragment_gaming_zone, container, false);

        initialiseViews(view);

        setNumberOptionMenus();

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
