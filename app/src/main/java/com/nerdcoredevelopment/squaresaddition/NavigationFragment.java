package com.nerdcoredevelopment.squaresaddition;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

public class NavigationFragment extends Fragment {
    private AppCompatTextView gamingZoneTextView;
    private AppCompatTextView leaderboardsTextView;
    private AppCompatTextView achievementsTextView;
    private AppCompatTextView settingsTextView;
    private OnNavigationFragmentInteractionListener mListener;

    public NavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initialise(View layoutView) {
        gamingZoneTextView = layoutView.findViewById(R.id.gaming_zone_text_view_navigation_fragment);
        leaderboardsTextView = layoutView.findViewById(R.id.leaderboards_text_view_navigation_fragment);
        achievementsTextView = layoutView.findViewById(R.id.achievements_text_view_navigation_fragment);
        settingsTextView = layoutView.findViewById(R.id.settings_text_view_navigation_fragment);
    }

    private void settingsOnClickListeners() {
        gamingZoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onNavigationFragmentGamingZoneClicked();
                }
            }
        });
        leaderboardsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onNavigationFragmentLeaderboardsClicked();
                }
            }
        });
        achievementsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onNavigationFragmentAchievementsClicked();
                }
            }
        });
        settingsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onNavigationFragmentSettingsClicked();
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

        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        initialise(view);

        settingsOnClickListeners();

        return view;
    }

    public interface OnNavigationFragmentInteractionListener {
        void onNavigationFragmentGamingZoneClicked();
        void onNavigationFragmentLeaderboardsClicked();
        void onNavigationFragmentAchievementsClicked();
        void onNavigationFragmentSettingsClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationFragmentInteractionListener) {
            mListener = (OnNavigationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnNavigationFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}