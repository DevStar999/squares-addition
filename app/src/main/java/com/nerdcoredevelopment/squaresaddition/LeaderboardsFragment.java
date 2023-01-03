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

public class LeaderboardsFragment extends Fragment {
    private Context context;
    private OnLeaderboardsFragmentInteractionListener mListener;
    private AppCompatImageView backButton;
    private AppCompatButton showLeaderboardsButton;
    private AppCompatButton leaderboardsTopScoresButton;
    private AppCompatButton leaderboardsPeerScoresButton;
    private AppCompatButton openInfoDialogButton;

    public LeaderboardsFragment() {
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
                    mListener.onLeaderboardsFragmentInteractionBackClicked();
                }
            }
        });
        showLeaderboardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLeaderboardsFragmentInteractionShowLeaderboardsClicked();
                }
            }
        });
        leaderboardsTopScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLeaderboardsFragmentInteractionLeaderboardsTopScoresClicked();
                }
            }
        });
        leaderboardsPeerScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLeaderboardsFragmentInteractionLeaderboardsPeerScoresClicked();
                }
            }
        });
        openInfoDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLeaderboardsFragmentInteractionOpenInfoDialogClicked();
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

        View view = inflater.inflate(R.layout.fragment_leaderboards, container, false);

        backButton = view.findViewById(R.id.title_back_leaderboards_fragment_button);
        showLeaderboardsButton = view.findViewById(R.id.show_leaderboards_button);
        leaderboardsTopScoresButton = view.findViewById(R.id.leaderboards_top_scores_button);
        leaderboardsPeerScoresButton = view.findViewById(R.id.leaderboards_peer_scores_button);
        openInfoDialogButton = view.findViewById(R.id.open_info_dialog_button);

        settingOnClickListeners();

        return view;
    }

    public interface OnLeaderboardsFragmentInteractionListener {
        void onLeaderboardsFragmentInteractionBackClicked();
        void onLeaderboardsFragmentInteractionShowLeaderboardsClicked();
        void onLeaderboardsFragmentInteractionLeaderboardsTopScoresClicked();
        void onLeaderboardsFragmentInteractionLeaderboardsPeerScoresClicked();
        void onLeaderboardsFragmentInteractionOpenInfoDialogClicked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLeaderboardsFragmentInteractionListener) {
            mListener = (OnLeaderboardsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnLeaderboardsFragmentInteractionListener");
        }
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
