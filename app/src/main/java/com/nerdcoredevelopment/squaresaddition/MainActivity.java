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
        possibilities. The code for this can be found in this MainActivity.java file
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
        (xiii) Notes related to the methods of a important classes the related to the Leaderboards feature are as follows -
        (a) LeaderboardsClient : Refer to this link to check out all the methods of this class ->
            https://developers.google.com/android/reference/com/google/android/gms/games/LeaderboardsClient
            However, all the relevant methods for our use have been tested out which are namely getLeaderboardIntent(),
            loadCurrentPlayerLeaderboardScore(), loadPlayerCenteredScores(), loadTopScores() & submitScore()
        (b) LeaderboardsClient.LeaderboardScores : Refer to this link to check out all the methods of this class ->
            https://developers.google.com/android/reference/com/google/android/gms/games/LeaderboardsClient.LeaderboardScores
            However, the only relevant method of this class is getScores() method
        (c) LeaderboardScoreBuffer : The only method this class consists which also happens to be relevant for us is the
            get() method which returns a LeaderboardScore object and this method should be used as shown in the code below
        (xiv) Sample data in the objects of important classes related to the leaderboards feature can be found in text files
        in Google Drive. The classes namely are as follows ->
        (a) LeaderboardScore (b) Player (c) CurrentPlayerInfo
        (d) PlayerLevelInfo (e) PlayerRelationshipInfo (f) PlayerLevel
    (5) Achievements =>
        (i) The basic elements which are associated with every achievement are as follows -
        (a) Id (b) Name (c) Description (d) Icon (e) List order
        To know more about these refer -> developers.google.com/games/services/common/concepts/achievements#the_basics
        (ii) Achievements can be in any one of the following 3 states
        (a) Hidden State : A hidden achievement means that details about the achievement are hidden from the player. The
            Google Play games services provides a generic placeholder description and icon for the achievement while it's in
            a hidden state. We recommend making an achievement hidden if it contains a spoiler you don't want to reveal about
            your game too early (for example, "Discover that you were a ghost all along!").
        (b) Revealed State : A revealed achievement means that the player knows about the achievement, but hasn't earned
            it yet. Most achievements start in the revealed state.
        (c) Unlocked State : An unlocked achievement means that the player has successfully earned the achievement. An
            achievement can be unlocked offline. When the game comes online, it syncs with the Google Play games services to
            update the achievement's unlocked state.
        (iii) Achievements can be designated as (a) Standard OR (b) Incremental
        (iv) Incremental Achievements : (a) Generally, an incremental achievement involves a player making gradual progress
            towards earning the achievement over a longer period of time. As the player makes progress towards the
            incremental achievement, you can report the player's partial progress to the Google Play games services.
        (b) Incremental achievements are cumulative across game sessions, and progress cannot be removed or reset from within
            the game. For example, "Win 50 games" would qualify as an incremental achievement. "Win 3 games in a row" would
            not, as the player's progress would be reset when they lose a game.
        (c) When creating an incremental achievement, you must define the total number of steps required to unlock it (this
            must be a number between 2 and 10,000). As the user makes progress towards unlocking the achievement, you should
            report the number of additional steps the user has made to the Google Play games services. Once the total number
            of steps reaches the unlock value, the achievement is unlocked (even if it was hidden). There's no need for you
            to store the user's cumulative progress.
        (v) Points : Achievements have a point value associated with them. The total points associated with an achievement
        must be a multiple of 5 and the whole game can never have a total of more than 1000 points for all of its
        achievements (although it can have less). In addition, no single achievement can have more than 200 points.
        (vi) Earning experience points (XP) : Players can gain levels on their Game Profile when they earn achievements in
        Play Games enabled games. For every point associated with an achievement, the player gains 100 experience points (XP)
        when they earn that achievement. In other words -
        XP for an achievement = 100 * (point value for the achievement)
        Play Games services keeps track of the XP earned by each player and sends out a notification to the Google Play Games
        app when the player has earned enough points to 'level up'. Players can view their level and XP history from their
        Profile page in the Google Play Games app.
        (vii) Minimum achievements : A game that integrates achievements should have at least 5 achievements before it is
        published. You can test with fewer than 5 achievements, but it is recommended you have at least 5 achievements
        created before you publish your game.
        (viii) Maximum achievements : The number of achievements is limited by the points limits and distribution. With a
        maximum number of points of 1000, and each achievement assigned 5 points, the maximum number of achievements is 200.
        However, if achievements are assigned more points then the number of achievements available decreases as a result.
        (ix) Icon guidelines : We can refer to these guidelines in the document as follows ->
        https://developers.google.com/games/services/common/concepts/achievements#icon_guidelines
        (x) Creating an achievement : To know more about how to create an achievement refer to the document below -
        https://developers.google.com/games/services/common/concepts/achievements#creating_an_achievement
        (xi) Editing an achievement : This can be done by the following 3 methods -
        (a) Undoing an edit
        (b) Deleting an achievement - Once your achievement has been published, it cannot be deleted. You can only delete an
            achievement in a pre-published state by clicking the button labeled Delete at the bottom of the form for that
            achievement.
        (c) Resetting an achievement - You can only reset player progress data for your draft achievements.
        To know more about this refer to the following link -
        https://developers.google.com/games/services/common/concepts/achievements#editing_an_achievement
        (xii) For incremental achievements, using the method setSteps() is more precise and convenient as compared to the
        method increment() as it allows us to set the exact number of steps completed as progress towards and achievement.
        If the code is not written perfectly, then using method increment() may make the number of steps reach a value that
        is not what we expect because of some bug in the code. Thus, it's far more convenient to use setSteps() method, since
        after using it we know exactly how many steps towards an achievement have been completed.
        (xiii) If we pass the maximum number of steps required to unlock an incremental achievement as argument in the method
        setSteps(), then the achievement is automatically unlocked by GPGS without us have to write any code to handle this.
        (xiv) An important thing to remember here is that after calling the reveal() method, GPGS does NOT give a
        notification in a toast message, like it does when an achievement is unlocked. So, we need to do this by ourselves
        so as to stimulate curiosity and excitement in the user about the newly revealed achievement as to know what it is.
        (xv) It seems to feel like a wise-man's strategy to reveal an achievement (either Standard or Incremental) at some
        point of progress towards that achievement for e.g. at 25% progress or 50% progress etc. to reveal a hidden
        achievement to the user so as to trigger excitement & engagement in the user regarding the newly revealed achievement
        (xvi) An important thing to note here is that as 'Steps needed' i.e. the no. of steps in an Incremental achievement
        is a part of it's type being 'Incremental' it cannot be changed once the achievement has been published
        (xvii) Notes related to the methods of a important classes the related to the Achievements feature are as follows -
        (a) AchievementsClient : Refer to this link to check out all the methods of this class ->
        https://developers.google.com/android/reference/com/google/android/gms/games/AchievementsClient
        However, all the relevant methods for our use have been tested out which are namely getAchievementsIntent(),
        increment(), load(), reveal(), setSteps() & unlock().
        (b) AchievementsBuffer : Refer to this link to check out all the methods of this class ->
        https://developers.google.com/android/reference/com/google/android/gms/games/achievement/AchievementBuffer
        However, the only relevant method of this class is get() method which returns an object of class 'Achievement'.
        (xviii) Sample data in the objects of important classes related to the achievements feature can be found in text
        files in Google Drive. The classes namely are as follows ->
        (a) Achievement (b) Player
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
// TODO -> Try and see if the number of steps in an incremental achievement can be changed after publishing
/* TODO -> You should create like a 'Fresh Data' button wherever it we may feel that it is required like before e.g. showing
           the leaderboards, achievements, loading some data etc. After this button is clicked, for the immediate next clicks
           for showing leaderboards, achievements etc. we should call the methods which fetch the fresh data from the servers
           instead of the methods which we usually call which may show us cached/stale data sometimes
*/
/* TODO -> The code written for achievements code is not the best and it can go wrong in the following ways -
           (1) Suppose if one user plays the game 25 times, then that user will unlock all achievements related to the count
           of games played which is as expected. But now if a different user signs in who has not unlocked those achievements
           will never be able to unlock those achievements as the value of the variable 'totalGamesPlayed' is stored only
           locally and only used under certain conditions to submit progress for achievement via the setSteps() method. The
           value of the variable 'totalGamesPlayed' is NOT stored online and also NOT fetched from an online database, thus
           it will be incorrect for different users
           (2) Ideally we should store the info about the achievements related to score being unlocked in a boolean local
           variable so as to reduce the API use and keep under the allowed quota. This is what we have done now as well.
           But this will mean that if the 1st user has unlocked these achievements and then a 2nd user signs in the app who
           has not unlocked these achievements, then he/she will never be able to unlock these achievements as locally the
           info about these achievements is that they have been unlocked
                Thus, the data which is stored locally and is in some way or the other used for GPGS related features can
           cause errors/unexpected behaviours etc. especially if the user signs in with a different Google Play Games
           profile. This is the reason why we should explore the 'Saved Games' feature once. If we do explore this feature
           also make sure to monitor the API usage consumption in GCP project of the GPGS API so as to ensure that we stay
           within the quota limit
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
                            LeaderboardScore leaderboardScore = leaderboardScoreBuffer.get(i);
                            String displayName = leaderboardScore.getScoreHolderDisplayName();
                            String displayRank = leaderboardScore.getDisplayRank();
                            long rawScoreValue = leaderboardScore.getRawScore();
                            Log.i("Custom Debugging", "for i = " + i + ":\n" + "displayName = " + displayName
                                    + ", displayRank = " + displayRank + ", rawScoreValue = " + rawScoreValue);
                        }
                    } else {
                        // TODO -> This too is an error branch and we should handle this as mentioned in the TODO below
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
                            LeaderboardScore leaderboardScore = leaderboardScoreBuffer.get(i);
                            String displayName = leaderboardScore.getScoreHolderDisplayName();
                            String displayRank = leaderboardScore.getDisplayRank();
                            long rawScoreValue = leaderboardScore.getRawScore();
                            Log.i("Custom Debugging", "for i = " + i + ":\n" + "displayName = " + displayName
                                    + ", displayRank = " + displayRank + ", rawScoreValue = " + rawScoreValue);
                        }
                    } else {
                        // TODO -> This too is an error branch and we should handle this as mentioned in the TODO below
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
                } else {
                    // TODO -> This too is an error branch and we should handle this as mentioned in the TODO below
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
                /* TODO -> We should remove the log statement above as it is just for our understanding and handle this
                           error branch with something like a dialog which gives a similar message to the above
                           log statement
                */
            }
        });
    }

    @Override
    public void onSettingsFragmentInteractionBackClicked() {
        onBackPressed();
    }
}