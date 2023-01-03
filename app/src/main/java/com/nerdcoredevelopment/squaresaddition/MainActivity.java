package com.nerdcoredevelopment.squaresaddition;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.games.AuthenticationResult;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/*  Notes related to Google Play Games Services (GPGS)
    (1) Consent Screen (In GCP Project) =>
    (2) Anti-Piracy =>
        (i) The option to turn this on or off is available at Play Console -> Current App -> Play Games services -> Setup
        and management -> Configuration -> Credentials -> <In any Android credential>
        (ii) We are currently keeping this option as 'OFF' but should turn this 'ON' when we have published the code to
        production.
    (3) Sign-In =>
        (i) The GPGS SDK will automatically attempt to login to GPGS at the start of the game without us having to
        do anything in code to make that happen.
        (ii) We have come across a sign-in even without internet connection i.e. when the device is offline. So, sign-in can
        in fact happen even if the device is offline.
        (iii) Best Practise : In-case, the user did not login then we should show a button to the user so that the user can
        login when he/she is ready. This button should be placed where it's most visible and accessible (so as to increase
        the login rates of the users) for e.g. on the Homepage. We should immediately hide this button when login is
        successful. Optionally, we can also place this button at one more place like the settings page (Remember, it is not
        recommended to put this button buried deep inside the app like the settings page if we are providing this trigger
        button there only and no place else).
        (iv) Best Practise : Whenever we are trying to access a GPGS feature we should verify if the user has signed in so
        as to avoid any unexpected & incorrect behaviour when to run the code for that feature. If we find out that the user
        is not signed in when we should somehow show this to the user in the UI and give to button to sign in.
        (v) Decide what needs to be done to the commented out code where we can retrieve the Player ID to identify the user.
        Refer to this link if required (especially the 'Note' boxes related to 'Enable server-side access' etc.) ->
        https://developer.android.com/games/pgs/android/android-signin#get_the_sign-in_result
    (4) Leaderboards =>
    (5) Achievements =>
    (6) Publishing API (Reference - https://developer.android.com/games/pgs/publishing/publishing) =>
        (i) Allows us to automate some tasks or functions which can be done manually through the Google Play Console as well.
        (ii) As of right now, we choose to ignore this API until some need of this comes later.
    (7) Management API (Reference - https://developer.android.com/games/pgs/management/management) =>
        (i) As of right now, we choose to ignore this API until some need of this comes later.
*/
public class MainActivity extends AppCompatActivity implements
        InfoFragment.OnInfoFragmentInteractionListener,
        NavigationFragment.OnNavigationFragmentInteractionListener,
        GamingZoneFragment.OnGamingZoneFragmentInteractionListener,
        LeaderboardsFragment.OnLeaderboardsFragmentInteractionListener,
        CustomLeaderboardsFragment.OnCustomLeaderboardsFragmentInteractionListener,
        AchievementsFragment.OnAchievementsFragmentInteractionListener,
        SettingsFragment.OnSettingsFragmentInteractionListener {
    public static final int RC_LEADERBOARD_UI = 9004;
    private AppCompatTextView gpgsSignInStatusTextView;
    private AppCompatImageView gpgsSignInImageView;
    private GamesSignInClient gamesSignInClient;
    private LeaderboardsClient leaderboardsClient;

    private void initialise() {
        gpgsSignInStatusTextView = findViewById(R.id.gpgs_sign_in_status_text_view);
        gpgsSignInImageView = findViewById(R.id.gpgs_sign_in_image_view);
        gamesSignInClient = PlayGames.getGamesSignInClient(MainActivity.this);
        leaderboardsClient = PlayGames.getLeaderboardsClient(MainActivity.this);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Following lines of code hide the status bar at the very top of the screen which battery
        indicator, network status other icons etc. Note, this is done before setting the layout with
        the line -> setContentView(R.layout.activity_main);
        */
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // To Disable screen rotation and keep the device in Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // To set the app theme to 'LIGHT' (even if 'DARK' theme is selected, however if user in their
        // settings enables 'DARK' theme for our individual app, then it will override the following line
        // no matter what)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // To hide the navigation bar as default i.e. it will hide by itself if left unused or unattended
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        NavigationFragment navigationFragment = new NavigationFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.navigation_fragment_container_main_activity, navigationFragment, "NAVIGATION_FRAGMENT")
                .commit();

        initialise();

        setupOnClickListeners();

        verifyPlayGamesSignIn();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onResume() {
        super.onResume();

        /* Persisting the screen settings even if the user leaves the app mid use for when he/she
           returns to use the app again
        */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void infoButtonClicked(View view) {
        // If InfoFragment was opened and is currently on top, then return
        int countOfFragments = getSupportFragmentManager().getFragments().size();
        if (countOfFragments > 0) {
            Fragment topMostFragment = getSupportFragmentManager().getFragments().get(countOfFragments-1);
            if (topMostFragment != null && topMostFragment.getTag() != null && !topMostFragment.getTag().isEmpty()
                    && topMostFragment.getTag().equals("INFO_FRAGMENT")) {
                return;
            }
        }

        InfoFragment fragment = InfoFragment.newInstance("<Add any text you would like to print here>");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.addToBackStack(null);
        transaction.add(R.id.full_screen_fragment_container_main_activity,
                fragment, "INFO_FRAGMENT").commit();
    }

    private void setupOnClickListeners() {
        gpgsSignInImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gamesSignInClient.signIn();
                // Waiting for 2 seconds for sign-in to complete, then verifying if sign-in was successful or not
                new CountDownTimer(1000, 10000) {
                    @Override
                    public void onTick(long l) {}
                    @Override
                    public void onFinish() {
                        verifyPlayGamesSignIn();
                    }
                }.start();
            }
        });
    }

    private void verifyPlayGamesSignIn() {
        gamesSignInClient.isAuthenticated().addOnCompleteListener(new OnCompleteListener<AuthenticationResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthenticationResult> isAuthenticatedTask) {
                boolean isAuthenticated = (isAuthenticatedTask.isSuccessful()
                        && isAuthenticatedTask.getResult().isAuthenticated());
                if (isAuthenticated) {
                    // Continue with Play Games Services
                    gpgsSignInStatusTextView.setText("GPGS Sign In Status : Signed In ✅");
                    gpgsSignInImageView.setVisibility(View.GONE);
                    /* TODO -> Remove the following code if we do find a way to implement the 'Enable server-side access'
                               document in the GPGS documentation
                    PlayGames.getPlayersClient(MainActivity.this).getCurrentPlayer()
                            .addOnCompleteListener(new OnCompleteListener<Player>() {
                        @Override
                        public void onComplete(@NonNull Task<Player> task) {
                            String playerId = task.getResult().getPlayerId();
                        }
                    });
                    */
                } else {
                    // Disable your integration with Play Games Services or show a login button to ask players to sign-in.
                    // Clicking it should call GamesSignInClient.signIn().
                    /* Own Notes - As of right now, the default settings for UI (i.e. buttons & text-views etc. related to
                                   sign-in) and other things is to accommodate the state where is user is not signed in to
                                   GPGS as default or else we would have not kept this code branch as empty.
                    */
                    gpgsSignInStatusTextView.setText("GPGS Sign In Status : NOT Signed In");
                    gpgsSignInImageView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onInfoFragmentInteractionBackClicked() {
        onBackPressed();
    }

    @Override
    public void onNavigationFragmentGamingZoneClicked() {
        // If GamingZoneFragment was opened and is currently on top, then return
        int countOfFragments = getSupportFragmentManager().getFragments().size();
        if (countOfFragments > 0) {
            Fragment topMostFragment = getSupportFragmentManager().getFragments().get(countOfFragments-1);
            if (topMostFragment != null && topMostFragment.getTag() != null && !topMostFragment.getTag().isEmpty()
                    && topMostFragment.getTag().equals("GAMING_ZONE_FRAGMENT")) {
                return;
            }
        }

        GamingZoneFragment fragment = new GamingZoneFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.addToBackStack(null);
        transaction.add(R.id.full_screen_fragment_container_main_activity,
                fragment, "GAMING_ZONE_FRAGMENT").commit();
    }

    @Override
    public void onNavigationFragmentLeaderboardsClicked() {
        // If LeaderboardsFragment was opened and is currently on top, then return
        int countOfFragments = getSupportFragmentManager().getFragments().size();
        if (countOfFragments > 0) {
            Fragment topMostFragment = getSupportFragmentManager().getFragments().get(countOfFragments-1);
            if (topMostFragment != null && topMostFragment.getTag() != null && !topMostFragment.getTag().isEmpty()
                    && topMostFragment.getTag().equals("LEADERBOARDS_FRAGMENT")) {
                return;
            }
        }

        LeaderboardsFragment fragment = new LeaderboardsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.addToBackStack(null);
        transaction.add(R.id.full_screen_fragment_container_main_activity,
                fragment, "LEADERBOARDS_FRAGMENT").commit();
    }

    @Override
    public void onNavigationFragmentCustomLeaderboardsClicked() {
        // If CustomLeaderboardsFragment was opened and is currently on top, then return
        int countOfFragments = getSupportFragmentManager().getFragments().size();
        if (countOfFragments > 0) {
            Fragment topMostFragment = getSupportFragmentManager().getFragments().get(countOfFragments-1);
            if (topMostFragment != null && topMostFragment.getTag() != null && !topMostFragment.getTag().isEmpty()
                    && topMostFragment.getTag().equals("CUSTOM_LEADERBOARDS_FRAGMENT")) {
                return;
            }
        }

        CustomLeaderboardsFragment fragment = new CustomLeaderboardsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.addToBackStack(null);
        transaction.add(R.id.full_screen_fragment_container_main_activity,
                fragment, "CUSTOM_LEADERBOARDS_FRAGMENT").commit();
    }

    @Override
    public void onNavigationFragmentAchievementsClicked() {
        // If AchievementsFragment was opened and is currently on top, then return
        int countOfFragments = getSupportFragmentManager().getFragments().size();
        if (countOfFragments > 0) {
            Fragment topMostFragment = getSupportFragmentManager().getFragments().get(countOfFragments-1);
            if (topMostFragment != null && topMostFragment.getTag() != null && !topMostFragment.getTag().isEmpty()
                    && topMostFragment.getTag().equals("ACHIEVEMENTS_FRAGMENT")) {
                return;
            }
        }

        AchievementsFragment fragment = new AchievementsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.addToBackStack(null);
        transaction.add(R.id.full_screen_fragment_container_main_activity,
                fragment, "ACHIEVEMENTS_FRAGMENT").commit();
    }

    @Override
    public void onNavigationFragmentSettingsClicked() {
        // If SettingsFragment was opened and is currently on top, then return
        int countOfFragments = getSupportFragmentManager().getFragments().size();
        if (countOfFragments > 0) {
            Fragment topMostFragment = getSupportFragmentManager().getFragments().get(countOfFragments-1);
            if (topMostFragment != null && topMostFragment.getTag() != null && !topMostFragment.getTag().isEmpty()
                    && topMostFragment.getTag().equals("SETTINGS_FRAGMENT")) {
                return;
            }
        }

        SettingsFragment fragment = new SettingsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.addToBackStack(null);
        transaction.add(R.id.full_screen_fragment_container_main_activity,
                fragment, "SETTINGS_FRAGMENT").commit();
    }

    @Override
    public void onGamingZoneFragmentInteractionBackClicked() {
        onBackPressed();
    }

    @Override
    public void onGamingZoneFragmentInteractionSubmitHighScore(int newHighScore) {
        leaderboardsClient.submitScore(getString(R.string.leaderboard_final_score_leaderboard), newHighScore);
    }

    @Override
    public void onLeaderboardsFragmentInteractionBackClicked() {
        onBackPressed();
    }

    @Override
    public void onLeaderboardsFragmentInteractionShowLeaderboardsClicked() {
        leaderboardsClient.getLeaderboardIntent(getString(R.string.leaderboard_final_score_leaderboard))
            .addOnCompleteListener(new OnCompleteListener<Intent>() {
                @Override
                public void onComplete(@NonNull Task<Intent> task) {
                    startActivityForResult(task.getResult(), RC_LEADERBOARD_UI);
                }
            });
    }

    @Override
    public void onLeaderboardsFragmentInteractionLeaderboardsTopScoresClicked() {
        Toast.makeText(this, "Leaderboards - Top 5 Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLeaderboardsFragmentInteractionLeaderboardsPeerScoresClicked() {
        Toast.makeText(this, "Leaderboards - Peer 5 Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLeaderboardsFragmentInteractionOpenInfoDialogClicked() {
        Toast.makeText(this, "Open Info Dialog Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCustomLeaderboardsFragmentInteractionBackClicked() {
        onBackPressed();
    }

    @Override
    public void onAchievementsFragmentInteractionBackClicked() {
        onBackPressed();
    }

    @Override
    public void onSettingsFragmentInteractionBackClicked() {
        onBackPressed();
    }
}