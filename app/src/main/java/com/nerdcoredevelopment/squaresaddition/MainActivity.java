package com.nerdcoredevelopment.squaresaddition;

import static com.google.android.gms.games.achievement.Achievement.STATE_HIDDEN;
import static com.google.android.gms.games.achievement.Achievement.STATE_REVEALED;
import static com.google.android.gms.games.achievement.Achievement.STATE_UNLOCKED;
import static com.google.android.gms.games.achievement.Achievement.TYPE_INCREMENTAL;
import static com.google.android.gms.games.achievement.Achievement.TYPE_STANDARD;

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

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.AuthenticationResult;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        InfoFragment.OnInfoFragmentInteractionListener,
        NavigationFragment.OnNavigationFragmentInteractionListener,
        GamingZoneFragment.OnGamingZoneFragmentInteractionListener,
        LeaderboardsFragment.OnLeaderboardsFragmentInteractionListener,
        CustomLeaderboardsFragment.OnCustomLeaderboardsFragmentInteractionListener,
        AchievementsFragment.OnAchievementsFragmentInteractionListener,
        SettingsFragment.OnSettingsFragmentInteractionListener {
    public static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_ACHIEVEMENT_UI = 9003;
    private SharedPreferences sharedPreferences;
    private AppCompatTextView gpgsSignInStatusTextView;
    private AppCompatImageView gpgsSignInImageView;
    private GamesSignInClient gamesSignInClient;
    private boolean isUserSignedIn;
    private LeaderboardsClient leaderboardsClient;
    private AchievementsClient achievementsClient;

    private void initialise() {
        sharedPreferences = getSharedPreferences("com.nerdcoredevelopment.squaresaddition", Context.MODE_PRIVATE);
        gpgsSignInStatusTextView = findViewById(R.id.gpgs_sign_in_status_text_view);
        gpgsSignInImageView = findViewById(R.id.gpgs_sign_in_image_view);
        gamesSignInClient = PlayGames.getGamesSignInClient(MainActivity.this);
        isUserSignedIn = false;
        leaderboardsClient = PlayGames.getLeaderboardsClient(MainActivity.this);
        achievementsClient = PlayGames.getAchievementsClient(MainActivity.this);
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
                    gpgsSignInStatusTextView.setText("Google Play Games Sign In Status : Signed In ✅");
                    gpgsSignInImageView.setVisibility(View.GONE);
                    isUserSignedIn = true;
                    /*
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
                    gpgsSignInStatusTextView.setText("Google Play Games Sign In Status : NOT Signed In");
                    gpgsSignInImageView.setVisibility(View.VISIBLE);
                    isUserSignedIn = false;
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
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Custom Debugging", "onFailure: Failed to fetch GPGS score by " +
                            "LeaderboardsClient.loadCurrentPlayerLeaderboardScore() method");
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
                            Log.i("Custom Debugging", "Failed to complete task of fetching score");
                        }
                    } else {
                        Log.i("Custom Debugging", "Failed to complete task of fetching score");
                    }
                }
            });
        */

        /*  Before moving on to the GamingZoneFragment we want to wait for some time like for e.g. 2 seconds for the value of
            bestScoreForSignedInPlayerFromGPGS[0] to be updated from the above listeners and we send a value fetched from the
            GPGS leaderboard to the GamingZoneFragment
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
    public void onGamingZoneFragmentInteractionIncrementGamesPlayed(int totalGamesPlayed) {
        if (totalGamesPlayed <= 5) {
            achievementsClient.setSteps(getString(R.string.achievement_newbie), totalGamesPlayed);
            achievementsClient.setSteps(getString(R.string.achievement_consistent), totalGamesPlayed);
            achievementsClient.setSteps(getString(R.string.achievement_seasoned), totalGamesPlayed);
        } else if (totalGamesPlayed <= 10) {
            achievementsClient.setSteps(getString(R.string.achievement_consistent), totalGamesPlayed);
            achievementsClient.setSteps(getString(R.string.achievement_seasoned), totalGamesPlayed);
        } else if (totalGamesPlayed <= 20) {
            if (totalGamesPlayed == 11) {
                achievementsClient.reveal(getString(R.string.achievement_seasoned));
            }
            achievementsClient.setSteps(getString(R.string.achievement_seasoned), totalGamesPlayed);
        }
    }

    @Override
    public void onGamingZoneFragmentInteractionCheckScoreBasedAchievement(int currentScore) {
        if (currentScore <= 10 && !sharedPreferences.getBoolean("backbencherAchievementUnlocked", false)) {
            achievementsClient.unlock(getString(R.string.achievement_backbencher));
            sharedPreferences.edit().putBoolean("backbencherAchievementUnlocked", true).apply();
        } else if (currentScore >= 100 && !sharedPreferences.getBoolean("superScorerAchievementUnlocked", false)) {
            achievementsClient.unlock(getString(R.string.achievement_super_scorer));
            sharedPreferences.edit().putBoolean("superScorerAchievementUnlocked", true).apply();
        }
    }

    @Override
    public void onLeaderboardsFragmentInteractionBackClicked() {
        onBackPressed();
    }

    @Override
    public void onLeaderboardsFragmentInteractionShowLeaderboardsClicked() {
        if (!isUserSignedIn) {
            Toast.makeText(MainActivity.this, "This feature cannot be used unless you are signed in",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        /*
        leaderboardsClient.getLeaderboardIntent(getString(R.string.leaderboard_final_score_leaderboard))
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_LEADERBOARD_UI);
                }
            });
         */
        leaderboardsClient.loadLeaderboardMetadata(false)
                .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardBuffer>>() {
            @Override
            public void onSuccess(AnnotatedData<LeaderboardBuffer> leaderboardBufferAnnotatedData) {
                LeaderboardBuffer leaderboardBuffer = leaderboardBufferAnnotatedData.get();
                if (leaderboardBuffer != null) {
                    int count = leaderboardBuffer.getCount();
                    for (int index = 0; index < count; index++) {
                        Leaderboard leaderboard = leaderboardBuffer.get(index);
                        // Log.i("Custom Debugging", "onSuccess: Leaderboard.toString() = " + leaderboard);
                        List<LeaderboardVariant> variants = leaderboard.getVariants();
                        for (int variantIndex = 0; variantIndex < variants.size(); variantIndex++) {
                            LeaderboardVariant currentVariant = variants.get(variantIndex);
                            // Log.i("Custom Debugging", "currentVariant.toString() = " + currentVariant);
                            /*
                            if (currentVariant.getCollection() == LeaderboardVariant.COLLECTION_FRIENDS) {
                                Log.i("Custom Debugging", "variantIndex = " + variantIndex + ", "
                                        + "Collection is of FRIENDS");
                            } else if (currentVariant.getCollection() == LeaderboardVariant.COLLECTION_PUBLIC) {
                                Log.i("Custom Debugging", "variantIndex = " + variantIndex + ", "
                                        + "Collection is PUBLIC");
                            }
                            */
                            if (currentVariant.getTimeSpan() == LeaderboardVariant.TIME_SPAN_ALL_TIME &&
                                    currentVariant.getCollection() == LeaderboardVariant.COLLECTION_PUBLIC) {
                                // Will get a proper score
                                Log.i("Custom Debugging", "rawPlayerScore = " + currentVariant.getRawPlayerScore());
                            } else if (currentVariant.getTimeSpan() == LeaderboardVariant.TIME_SPAN_WEEKLY &&
                                    currentVariant.getCollection() == LeaderboardVariant.COLLECTION_FRIENDS) {
                                // Will get 'null' or 'none' values
                                Log.i("Custom Debugging", "rawPlayerScore = " + currentVariant.getRawPlayerScore());
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Keeping blank for now
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
                            LeaderboardScore leaderboardScore = leaderboardScoreBuffer.get(i);
                            String displayName = leaderboardScore.getScoreHolderDisplayName();
                            String displayRank = leaderboardScore.getDisplayRank();
                            long rawScoreValue = leaderboardScore.getRawScore();
                            Log.i("Custom Debugging", "for i = " + i + ":\n" + "displayName = " + displayName
                                    + ", displayRank = " + displayRank + ", rawScoreValue = " + rawScoreValue);
                        }
                    }

                    if (leaderboardScores != null) {
                        leaderboardScores.release();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Custom Debugging", "onFailure: Failed to fetch GPGS scores from leaderboard by " +
                            "LeaderboardsClient.loadTopScores() method");
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
                            LeaderboardScore leaderboardScore = leaderboardScoreBuffer.get(i);
                            String displayName = leaderboardScore.getScoreHolderDisplayName();
                            String displayRank = leaderboardScore.getDisplayRank();
                            long rawScoreValue = leaderboardScore.getRawScore();
                            Log.i("Custom Debugging", "for i = " + i + ":\n" + "displayName = " + displayName
                                    + ", displayRank = " + displayRank + ", rawScoreValue = " + rawScoreValue);
                        }
                    }

                    if (leaderboardScores != null) {
                        leaderboardScores.release();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Custom Debugging", "onFailure: Failed to fetch GPGS scores from leaderboard by " +
                            "LeaderboardsClient.loadPlayerCenteredScores() method");
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
    public void onAchievementsFragmentInteractionShowAchievementsClicked() {
        if (!isUserSignedIn) {
            Toast.makeText(MainActivity.this, "This feature cannot be used unless you are signed in",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        achievementsClient.getAchievementsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(intent, RC_ACHIEVEMENT_UI);
            }
        });
    }

    @Override
    public void onAchievementsFragmentInteractionLoadAchievementsDataClicked() {
        if (!isUserSignedIn) {
            Toast.makeText(MainActivity.this, "This feature cannot be used unless you are signed in",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        achievementsClient.load(false).addOnSuccessListener(new OnSuccessListener<AnnotatedData<AchievementBuffer>>() {
            @Override
            public void onSuccess(AnnotatedData<AchievementBuffer> achievementBufferAnnotatedData) {
                AchievementBuffer achievementBuffer = achievementBufferAnnotatedData.get();
                if (achievementBuffer != null) {
                    int count = achievementBuffer.getCount();
                    Log.i("Custom Debugging", "The data of achievementBuffer for " +
                            "achievementsClient.load() is as follows ->");
                    for (int i = 0; i < count; i++) {
                        Achievement achievement = achievementBuffer.get(i);
                        String achievementId = achievement.getAchievementId();
                        String achievementName = achievement.getName();
                        int achievementState = achievement.getState();
                        int achievementType = achievement.getType();
                        Log.i("Custom Debugging", "for i = " + i + ":\n" + "achievementId = " + achievementId
                                + ", achievementName = " + achievementName);
                        if (achievementState == STATE_HIDDEN) {
                            Log.i("Custom Debugging", "achievementState = STATE_HIDDEN");
                        } else if (achievementState == STATE_REVEALED) {
                            Log.i("Custom Debugging", "achievementState = STATE_REVEALED");
                        } else if (achievementState == STATE_UNLOCKED) {
                            Log.i("Custom Debugging", "achievementState = STATE_UNLOCKED");
                        }

                        if (achievementType == TYPE_STANDARD) {
                            Log.i("Custom Debugging", "achievementState = TYPE_STANDARD");
                        } else if (achievementType == TYPE_INCREMENTAL) {
                            Log.i("Custom Debugging", "achievementState = TYPE_INCREMENTAL");
                        }
                    }
                }

                if (achievementBuffer != null) {
                    achievementBuffer.release();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Custom Debugging", "onFailure: Failed to fetch GPGS achievements by " +
                        "AchievementClient.load() method");
            }
        });
    }

    @Override
    public void onSettingsFragmentInteractionBackClicked() {
        onBackPressed();
    }
}