package com.nerdcoredevelopment.squaresaddition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.AuthenticationResult;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        (i) When we use the submitScore() method to submit our score to the leaderboard GPGS will by itself check if the
        submitted score is better than the current entry in the daily, weekly & all-time score list. If it is, GPGS will
        update the corresponding leaderboard.
        (ii) The methods to retrieve a player's scores from GPGS are also very well written keeping in mind all the
        possibilities.
        TODO -> Add the specific method i.e. the code to retrieve a player's score
        (iii) We can have a maximum of 70 leaderboards for a game.
        (iv) The Play Games SDK automatically creates daily, weekly, and all-time versions of every leaderboard that you
        create. There's no need for us to create separate leaderboards for each time frame.
        (v) Public and Social Leaderboards : Social leaderboards consist of entries of the user's friends as per Google Play
        Games i.e. the scores from these friends and ranking is also given as per what the ranking is among these friends.
        Public leaderboards consist of entries of all the players who have chosen to share their gameplay activity to
        everyone in the Google Play Games settings i.e. their scores will be visible and a part of this leaderboard.
        (vi) Before showing the leaderboard to the user, we will have to check first if the user has an entry on the
        leaderboard. If not, then we will have to prompt the user to change the settings for his account in Google Play
        Games. Not only will we have to check if the current player's score has been submitted to the leaderboard but also
        check if the player is currently signed in or not or the methods used to retrieve the leaderboards data may not
        function properly.
        (vii) When we show the leaderboards using a custom UI, then we should take care of the fact that the player Id may
        contain Unicode characters (for example, if the name has non-english characters).
        (viii) All scores are submitted to leaderboards and stored internally as long integers, the Games service can present
        them to the user in a number of different formats: (1) Numeric (2) Time (3) Currency. We may need to revisit this
        section for info on submitting scores if we are doing something new.
        (ix) Editing a leaderboard : Mainly consists of 3 functions which are as follows ->
        (a) Undo an edit (b) Delete a leaderboard (c) Reset a leaderboard
        For more information we can refer to the link ->
        https://developers.google.com/games/services/common/concepts/leaderboards#edit_a_leaderboard
        (x) Add translations for leaderboards : Refer to the following link ->
        https://developers.google.com/games/services/common/concepts/leaderboards#add_translations_for_leaderboards
        (xi) Hide leaderboard scores : Basically talks about the 'Leaderboard tamper Protection' feature where the
        suspected tampered scores are hidden automatically in the leaderboard. For more info. refer to the link ->
        https://developers.google.com/games/services/common/concepts/leaderboards#hide_leaderboard_scores
        (xii) For each line of Custom UI we should have the following data (with examples) ->
        (a) Gamer name/Display name/User Id = LeaderboardScore.getScoreHolderDisplayName() = (String) HokageMeetPatel1997
        (b) Rank (with ordinals like 1st, 22nd, 43rd, 5th etc.) = LeaderboardScore.getDisplayRank() = (String) 9th
            Rank (without ordinals, simple long integer) = LeaderboardScore.getRank() = (long) 9
        (c) Score = LeaderboardScore.getRawScore() = (long) 47
        (d) Avatar Image = Refer from images of object info in Google Drive folder for this app
    (5) Achievements =>
    (6) Publishing API (Reference - https://developer.android.com/games/pgs/publishing/publishing) =>
        (i) Allows us to automate some tasks or functions which can be done manually through the Google Play Console as well.
        (ii) As of right now, we choose to ignore this API until some need of this comes later.
    (7) Management API (Reference - https://developer.android.com/games/pgs/management/management) =>
        (i) As of right now, we choose to ignore this API until some need of this comes later.
    (8) Things that cannot be changed after they are published =>
        (i) Saved games -> In 'Edit properties' in 'Configuration -> Can't be turned off after publishing if 'On' is ticked
        and game is published
        (ii) Incremental achievements -> In 'Achievements' in a specific achievement -> This can't be changed after the
        achievement is published
        (iii) Initial state -> (a) In 'Achievements' in a specific achievement & (b) In 'Events' in a specific
        event -> This can't be changed after the achievement is published
        (iv) Points -> In 'Achievements' -> These are XP Points for a Google Play Games player which are added after
        unlocking an achievement to their total profile experience and per one achievement max. of 200 points can be given
        and for the whole game among all achievements a max. of 1000 points can be given
        (v) Limits -> In 'Leaderboards' in a specific leaderboard -> This is the upper limit and/or lower limit for score in
        a leaderboard
*/
// The list of TODOs related to this project is as follows
/* TODO -> Verify all the GPGS features are working correctly and proper error handling is done for the following -
           (a) When user is not signed-in
           (b) No internet connection
           or any combination of the above two
*/
/* TODO -> The GPGS feature of 'Achievements' is to be implemented in the next visit to this feature with the following
           tasks to be kept in mind -
           (a) Understanding the concept (Refer) -> https://developers.google.com/games/services/common/concepts/achievements
           (b) Client Implementation (Refer) -> https://developers.google.com/games/services/android/achievements
           (c) Take a look at the Quality checklist (Refer) -> https://developers.google.com/games/services/checklist
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
    private SharedPreferences sharedPreferences;
    private AppCompatTextView gpgsSignInStatusTextView;
    private AppCompatImageView gpgsSignInImageView;
    private GamesSignInClient gamesSignInClient;
    private LeaderboardsClient leaderboardsClient;

    private void initialise() {
        sharedPreferences = getSharedPreferences("com.nerdcoredevelopment.squaresaddition", Context.MODE_PRIVATE);
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

    private void openGamingZoneFragment(long bestScore) {
        GamingZoneFragment fragment = GamingZoneFragment.newInstance(bestScore);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.addToBackStack(null);
        transaction.add(R.id.full_screen_fragment_container_main_activity,
                fragment, "GAMING_ZONE_FRAGMENT").commit();
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

        final long[] bestScoreForSignedInPlayerFromGPGS = {sharedPreferences.getLong("bestScore", 0)};
        /* Note - (a) In the following lines of code we are retrieve the currently signed in player's score from the
                  Leaderboards data using the method loadCurrentPlayerLeaderboardScore() along with addOnSuccessListener()
                  & addOnFailureListener() listeners
                  (b) Another way to implement the retrieval of score can be with the same method but with the
                  addOnCompleteListener() listener. This is shown in the commented out code below
                  (c) Refer images on Google Drive in this app's folder to see the log data
        */
        leaderboardsClient.loadCurrentPlayerLeaderboardScore(getString(R.string.leaderboard_final_score_leaderboard),
            LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
            .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
                @Override
                public void onSuccess(AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                    LeaderboardScore leaderboardScore = leaderboardScoreAnnotatedData.get();
                    if (leaderboardScore != null) {
                        long rawScore = leaderboardScore.getRawScore();
                        String displayRank = leaderboardScore.getDisplayRank();
                        long rank = leaderboardScore.getRank();
                        String playerDisplayName = leaderboardScore.getScoreHolderDisplayName();

                        Log.i("Custom Debugging", "In addOnCompleteListener, " +
                                "LeaderboardScore.getRawScore() = (long) " + rawScore);
                        Log.i("Custom Debugging", "In addOnCompleteListener, " +
                                "LeaderboardScore.getDisplayRank() = (String) " + displayRank);
                        Log.i("Custom Debugging", "In addOnCompleteListener, " +
                                "LeaderboardScore.getRank() = (long) " + rank);
                        Log.i("Custom Debugging", "In addOnCompleteListener, " +
                                "LeaderboardScore.getScoreHolderDisplayName() = (String) " + playerDisplayName);
                        bestScoreForSignedInPlayerFromGPGS[0] = rawScore;
                    } else {
                        // TODO -> This too is an error branch and we should handle this as mentioned in the TODO below
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Custom Debugging", "onFailure: Failed to fetch GPGS score by " +
                            "LeaderboardsClient.loadCurrentPlayerLeaderboardScore() method");
                    /* TODO -> We should remove the log statement above as it is just for our understanding and handle this
                               error branch with something like a dialog which gives a similar message to the above
                               log statement
                    */
                }
            });
        /*
        leaderboardsClient.loadCurrentPlayerLeaderboardScore(getString(R.string.leaderboard_final_score_leaderboard),
            LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
            .addOnCompleteListener(new OnCompleteListener<AnnotatedData<LeaderboardScore>>() {
                @Override
                public void onComplete(@NonNull Task<AnnotatedData<LeaderboardScore>> task) {
                    if (!task.isSuccessful()) {
                        Log.i("Custom Debugging", "Failed to complete task of fetching score");
                        // TODO -> This too is an error branch and we should handle this as mentioned in the TODO above
                    }

                    if (task.getResult() != null) {
                        LeaderboardScore leaderboardScore = task.getResult().get();
                        if (leaderboardScore != null) {
                            long tempLong = leaderboardScore.getRawScore();
                            String displayRank = leaderboardScore.getDisplayRank();
                            long rank = leaderboardScore.getRank();
                            String playerDisplayName = leaderboardScore.getScoreHolderDisplayName();

                            Log.i("Custom Debugging", "In addOnCompleteListener, " +
                                    "LeaderboardScore.getRawScore() = (long) " + tempLong);
                            Log.i("Custom Debugging", "In addOnCompleteListener, " +
                                    "LeaderboardScore.getDisplayRank() = (String) " + displayRank);
                            Log.i("Custom Debugging", "In addOnCompleteListener, " +
                                    "LeaderboardScore.getRank() = (long) " + rank);
                            Log.i("Custom Debugging", "In addOnCompleteListener, " +
                                    "LeaderboardScore.getScoreHolderDisplayName() = (String) " + playerDisplayName);
                            bestScoreForSignedInPlayerFromGPGS[0] = tempLong;
                        } else {
                            // TODO -> This too is an error branch and we should handle this as mentioned in the TODO above
                        }
                    } else {
                        // TODO -> This too is an error branch and we should handle this as mentioned in the TODO above
                    }
                }
            });
        */

        /*  Before moving on to the GamingZoneFragment we want to wait for some time like for e.g. 2 seconds for the value of
            bestScoreForSignedInPlayerFromGPGS[0] to be updated from the above listeners and we send a value fetched from the
            GPGS leaderboard to the GamingZoneFragment
        */
        /* TODO -> For the 2 seconds of time we are waiting for the value to be fetched from GPGS and updated, we can show
                   like a loading screen etc. in the meanwhile
        */
        new CountDownTimer(2000, 10000) {
            @Override
            public void onTick(long l) {}
            @Override
            public void onFinish() {
                openGamingZoneFragment(bestScoreForSignedInPlayerFromGPGS[0]);
            }
        }.start();
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
    public void onGamingZoneFragmentInteractionSubmitHighScore(long newHighScore) {
        leaderboardsClient.submitScore(getString(R.string.leaderboard_final_score_leaderboard), newHighScore);
        /*  The method with following signature can be used if we want to send some more data along with the score in the
            form of a tag. For our e.g. "Score Tag"
        */
        // leaderboardsClient.submitScore(getString(R.string.leaderboard_final_score_leaderboard), newHighScore, "Score Tag");
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
        /* Note - (a) The range for last parameter is 1L to 25L i.e. 1 as a long integer to 25 as a long integer,
                  both inclusive. We have chosen 5 in the example code below
                  (b) In the following lines of code we are retrieving the top 5 scores from the Leaderboards data using the
                  method loadTopScores() along with addOnSuccessListener() & addOnFailureListener() listeners
                  (c) Another way to implement the retrieval of the top scores can be with the same method but with the
                  addOnCompleteListener() listener, but for now we are following the implementation mentioned above
         */
        leaderboardsClient.loadTopScores(getString(R.string.leaderboard_final_score_leaderboard),
            LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, 5)
            .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardsClient.LeaderboardScores>>() {
                @Override
                public void onSuccess(AnnotatedData<LeaderboardsClient.LeaderboardScores> leaderboardScoresAnnotatedData) {
                    LeaderboardsClient.LeaderboardScores leaderboardScores = leaderboardScoresAnnotatedData.get();
                    if (leaderboardScores != null) {
                        LeaderboardScoreBuffer leaderboardScoreBuffer = leaderboardScores.getScores();
                        int count = leaderboardScoreBuffer.getCount();
                        Log.i("Custom Debugging", "The data of leaderboardScoreBuffer for " +
                                "leaderboardsClient.loadTopScores() is as follows ->");
                        for (int i = 0; i < count; i++) {
                            LeaderboardScore score = leaderboardScoreBuffer.get(i);
                            String displayName = score.getScoreHolderDisplayName();
                            String displayRank = score.getDisplayRank();
                            long rawScoreValue = score.getRawScore();
                            Log.i("Custom Debugging", "for i = " + i + ":\n" + "displayName = " + displayName
                                    + ", displayRank = " + displayRank + ", rawScoreValue = " + rawScoreValue);
                        }
                        leaderboardScores.release();
                    } else {
                        // TODO -> This too is an error branch and we should handle this as mentioned in the TODO below
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Custom Debugging", "onFailure: Failed to fetch GPGS scores from leaderboard by " +
                            "LeaderboardsClient.loadTopScores() method");
                    /* TODO -> We should remove the log statement above as it is just for our understanding and handle this
                               error branch with something like a dialog which gives a similar message to the above
                               log statement
                    */
                }
            });
    }

    @Override
    public void onLeaderboardsFragmentInteractionLeaderboardsPeerScoresClicked() {
        /* Note - (a) The range for last parameter is 1L to 25L i.e. 1 as a long integer to 25 as a long integer,
                  both inclusive. We have chosen 5 as example in the code below
                  (b) The method loadPlayerCenteredScores() handles by itself if there are less entries than maxResults/2
                  above or below and loads more scores where there are more entries (int maxResults is the last parameter,
                  where we in our example we have given the value 5 as mentioned above)
                  (c) In the following lines of code we are retrieving 5 peer scores from the Leaderboards data using the
                  method loadPlayerCenteredScores() along with addOnSuccessListener() & addOnFailureListener() listeners
                  (d) Another way to implement the retrieval of the top scores can be with the same method but with the
                  addOnCompleteListener() listener, but for now we are following the implementation mentioned above
        */
        leaderboardsClient.loadPlayerCenteredScores(getString(R.string.leaderboard_final_score_leaderboard),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, 5)
            .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardsClient.LeaderboardScores>>() {
                @Override
                public void onSuccess(AnnotatedData<LeaderboardsClient.LeaderboardScores> leaderboardScoresAnnotatedData) {
                    LeaderboardsClient.LeaderboardScores leaderboardScores = leaderboardScoresAnnotatedData.get();
                    if (leaderboardScores != null) {
                        LeaderboardScoreBuffer leaderboardScoreBuffer = leaderboardScores.getScores();
                        int count = leaderboardScoreBuffer.getCount();
                        Log.i("Custom Debugging", "The data of leaderboardScoreBuffer for " +
                                "leaderboardsClient.loadPlayerCenteredScores() is as follows ->");
                        for (int i = 0; i < count; i++) {
                            LeaderboardScore score = leaderboardScoreBuffer.get(i);
                            String displayName = score.getScoreHolderDisplayName();
                            String displayRank = score.getDisplayRank();
                            long rawScoreValue = score.getRawScore();
                            Log.i("Custom Debugging", "for i = " + i + ":\n" + "displayName = " + displayName
                                    + ", displayRank = " + displayRank + ", rawScoreValue = " + rawScoreValue);
                        }
                        leaderboardScores.release();
                    } else {
                        // TODO -> This too is an error branch and we should handle this as mentioned in the TODO below
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Custom Debugging", "onFailure: Failed to fetch GPGS scores from leaderboard by " +
                            "LeaderboardsClient.loadPlayerCenteredScores() method");
                /* TODO -> We should remove the log statement above as it is just for our understanding and handle this
                           error branch with something like a dialog which gives a similar message to the above
                           log statement
                */
                }
            });
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