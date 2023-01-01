package com.nerdcoredevelopment.squaresaddition;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements
        NavigationFragment.OnNavigationFragmentInteractionListener,
        GamingZoneFragment.OnGamingZoneFragmentInteractionListener,
        SettingsFragment.OnSettingsFragmentInteractionListener {

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
    public void onSettingsFragmentInteractionBackClicked() {
        onBackPressed();
    }
}