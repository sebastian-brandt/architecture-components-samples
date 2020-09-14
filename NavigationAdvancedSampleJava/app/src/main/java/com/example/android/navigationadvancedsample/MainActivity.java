/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.navigationadvancedsample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * An activity that inflates a layout that has a [BottomNavigationView].
 */
public class MainActivity extends AppCompatActivity {

    private LiveData<NavController> currentNavController = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            setupBottomNavigationBar();
        } // Else, need to wait for onRestoreInstanceState
    }

    @Override
    public void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (currentNavController != null && currentNavController.getValue() != null) {
            return currentNavController.getValue().navigateUp();
        }
        return false;
    }

    /**
     * Called on first creation and when restoring state.
     */
    private void setupBottomNavigationBar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        List<Integer> navGraphIds = Arrays.asList(R.navigation.home, R.navigation.list, R.navigation.form);

        // Setup the bottom navigation view with a list of navigation graphs
        LiveData<NavController> controller = NavigationExtensions.setupWithNavController(
                bottomNavigationView,
                navGraphIds,
                getSupportFragmentManager(),
                R.id.nav_host_container,
                getIntent());

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this,
                navController -> setupActionBarWithNavController(MainActivity.this, navController, null));

        currentNavController = controller;
    }

    private void setupActionBarWithNavController(
            AppCompatActivity thiz,
            NavController navController,
            AppBarConfiguration configuration)
    {
        if (configuration == null) {
            configuration = appBarConfiguration(
                    navController.getGraph(),
                    null,
                    null);
        }
        NavigationUI.setupActionBarWithNavController(thiz, navController, configuration);
    }

    private AppBarConfiguration appBarConfiguration(
            NavGraph navGraph,
            DrawerLayout drawerLayout,
            AppBarConfiguration.OnNavigateUpListener fallbackOnNavigateUpListener)
    {
        if (fallbackOnNavigateUpListener == null) {
            fallbackOnNavigateUpListener = () -> false;
        }
        return new AppBarConfiguration.Builder(navGraph)
                .setDrawerLayout(drawerLayout)
                .setFallbackOnNavigateUpListener(fallbackOnNavigateUpListener)
                .build();
    }
}
