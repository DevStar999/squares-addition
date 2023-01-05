package com.nerdcoredevelopment.squaresaddition;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

public class AchievementsFragment extends Fragment {
    private Context context;
    private OnAchievementsFragmentInteractionListener mListener;
    private AppCompatImageView backButton;
    private AppCompatButton showAchievementsButton;

    public AchievementsFragment() {
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
                    mListener.onAchievementsFragmentInteractionBackClicked();
                }
            }
        });
        showAchievementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onAchievementsFragmentInteractionShowAchievementsClicked();
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

        View view = inflater.inflate(R.layout.fragment_achievements, container, false);

        backButton = view.findViewById(R.id.title_back_achievements_fragment_button);
        showAchievementsButton = view.findViewById(R.id.show_achievements_button);

        settingOnClickListeners();

        return view;
    }

    public interface OnAchievementsFragmentInteractionListener {
        void onAchievementsFragmentInteractionBackClicked();
        void onAchievementsFragmentInteractionShowAchievementsClicked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAchievementsFragmentInteractionListener) {
            mListener = (OnAchievementsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnAchievementsFragmentInteractionListener");
        }
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
