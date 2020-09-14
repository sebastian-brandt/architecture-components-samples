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

import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Manages the various graphs needed for a [BottomNavigationView].
 *
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 */
public class NavigationExtensions {

    private static String getFragmentTag(int index) {
        return "bottomNavigation#" + index;
    }

    private static NavHostFragment obtainNavHostFragment(
            FragmentManager fragmentManager,
            String fragmentTag,
            int navGraphId,
            int containerId)
    {
        // If the Nav Host fragment exists, return it
        NavHostFragment existingFragment = (NavHostFragment) fragmentManager.findFragmentByTag(fragmentTag);
        if (existingFragment != null) {
            return existingFragment;
        }

        // Otherwise, create it and return it.
        NavHostFragment navHostFragment = NavHostFragment.create(navGraphId);
        fragmentManager.beginTransaction()
                .add(containerId, navHostFragment, fragmentTag)
                .commitNow();
        return navHostFragment;
    }

    private static void attachNavHostFragment(
            FragmentManager fragmentManager,
            NavHostFragment navHostFragment,
            boolean isPrimaryNavFragment)
    {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .attach(navHostFragment);

        if (isPrimaryNavFragment) {
            fragmentTransaction.setPrimaryNavigationFragment(navHostFragment);
        }
        fragmentTransaction.commitNow();
    }

    private static void detachNavHostFragment(
            FragmentManager fragmentManager,
            NavHostFragment navHostFragment)
    {
        fragmentManager.beginTransaction()
                .detach(navHostFragment)
                .commitNow();
    }

    private static void setupDeepLinks(
            BottomNavigationView thiz,
            List<Integer> navGraphIds,
            FragmentManager fragmentManager,
            int containerId,
            Intent intent)
    {
        for (int index=0; index<navGraphIds.size(); ++index) {
            int navGraphId = navGraphIds.get(index);
            String fragmentTag = getFragmentTag(index);

            // Find or create the Navigation host fragment
            NavHostFragment navHostFragment = obtainNavHostFragment(
                    fragmentManager,
                    fragmentTag,
                    navGraphId,
                    containerId
            );

            // Handle Intent
            if (navHostFragment.getNavController().handleDeepLink(intent)) {
                int id = navHostFragment.getNavController().getGraph().getId();
                if (thiz.getSelectedItemId() != id) {
                    thiz.setSelectedItemId(id);
                }
            }
        }
    }

    private static void setupItemReselected(
            BottomNavigationView thiz,
            Map<Integer, String> graphIdToTagMap,
            FragmentManager fragmentManager)
    {
        thiz.setOnNavigationItemReselectedListener(item -> {
            String newlySelectedItemTag = graphIdToTagMap.get(item.getItemId());
            NavHostFragment selectedFragment = (NavHostFragment) fragmentManager.findFragmentByTag(newlySelectedItemTag);
            NavController navController = selectedFragment.getNavController();
            // Pop the back stack to the start destination of the current navController graph
            navController.popBackStack(navController.getGraph().getStartDestination(), false);
        });
    }

    private static boolean isOnBackStack(FragmentManager thiz, String backStackName) {
        int backStackCount = thiz.getBackStackEntryCount();
        for (int index = 0; index < backStackCount; ++index) {
            if (thiz.getBackStackEntryAt(index).getName().equals(backStackName)) {
                return true;
            }
        }
        return false;
    }

    public static LiveData<NavController> setupWithNavController(
            BottomNavigationView thiz,
            List<Integer> navGraphIds,
            FragmentManager fragmentManager,
            int containerId,
            Intent intent)
    {
        // Map of tags
        Map<Integer, String> graphIdToTagMap = new HashMap<>();

        // Result. Mutable live data with the selected controlled
        MutableLiveData<NavController> selectedNavController = new MutableLiveData<>();

        int firstFragmentGraphId = 0;

        // First create a NavHostFragment for each NavGraph ID
        for (int index=0; index < navGraphIds.size(); ++index) {
            int navGraphId = navGraphIds.get(index);
            String fragmentTag = getFragmentTag(index);

            // Find or create the Navigation host fragment
            NavHostFragment navHostFragment = obtainNavHostFragment(
                    fragmentManager,
                    fragmentTag,
                    navGraphId,
                    containerId
            );

            // Obtain its id
            int graphId = navHostFragment.getNavController().getGraph().getId();

            if (index == 0) {
                firstFragmentGraphId = graphId;
            }

            // Save to the map
            graphIdToTagMap.put(graphId, fragmentTag);

            // Attach or detach nav host fragment depending on whether it's the selected item.
            if (thiz.getSelectedItemId() == graphId) {
                // Update livedata with the selected graph
                selectedNavController.setValue(navHostFragment.getNavController());
                attachNavHostFragment(fragmentManager, navHostFragment, index == 0);
            }
            else {
                detachNavHostFragment(fragmentManager, navHostFragment);
            }
        }

        // Now connect selecting an item with swapping Fragments
        final String[] selectedItemTag = {graphIdToTagMap.get(thiz.getSelectedItemId())};
        String firstFragmentTag = graphIdToTagMap.get(firstFragmentGraphId);
        final boolean[] isOnFirstFragment = {(equals(selectedItemTag[0], firstFragmentTag))};

        // When a navigation item is selected
        thiz.setOnNavigationItemSelectedListener(item -> {
            // Don't do anything if the state is state has already been saved.
            if (fragmentManager.isStateSaved()) {
                return false;
            }
            else {
                String newlySelectedItemTag = graphIdToTagMap.get(item.getItemId());
                if (! selectedItemTag[0].equals(newlySelectedItemTag)) {
                    // Pop everything above the first fragment (the "fixed start destination")
                    fragmentManager.popBackStack(firstFragmentTag,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    NavHostFragment selectedFragment = (NavHostFragment) fragmentManager.findFragmentByTag(newlySelectedItemTag);

                    // Exclude the first fragment tag because it's always in the back stack.
                    if (! equals(firstFragmentTag, newlySelectedItemTag)) {
                        // Commit a transaction that cleans the back stack and adds the first fragment
                        // to it, creating the fixed started destination.
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                                .setCustomAnimations(
                                        R.anim.nav_default_enter_anim,
                                        R.anim.nav_default_exit_anim,
                                        R.anim.nav_default_pop_enter_anim,
                                        R.anim.nav_default_pop_exit_anim)
                                .attach(selectedFragment)
                                .setPrimaryNavigationFragment(selectedFragment);

                        for (String fragmentTagIter : graphIdToTagMap.values()) {
                            if (!fragmentTagIter.equals(newlySelectedItemTag)) {
                                fragmentTransaction.detach(fragmentManager.findFragmentByTag(firstFragmentTag));
                            }
                        }

                        fragmentTransaction.addToBackStack(firstFragmentTag)
                                .setReorderingAllowed(true)
                                .commit();
                    }
                    selectedItemTag[0] = newlySelectedItemTag;
                    isOnFirstFragment[0] = equals(selectedItemTag[0], firstFragmentTag);
                    selectedNavController.setValue(selectedFragment.getNavController());
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        // Optional: on item reselected, pop back stack to the destination of the graph
        setupItemReselected(thiz, graphIdToTagMap, fragmentManager);

        // Handle deep link
        setupDeepLinks(thiz, navGraphIds, fragmentManager, containerId, intent);

        // Finally, ensure that we update our BottomNavigationView when the back stack changes
        final int finalFirstFragmentGraphId = firstFragmentGraphId;
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (!isOnFirstFragment[0] && !isOnBackStack(fragmentManager, firstFragmentTag)) {
                thiz.setSelectedItemId(finalFirstFragmentGraphId);
            }

            // Reset the graph if the currentDestination is not valid (happens when the back
            // stack is popped after using the back button).
            NavController controller = selectedNavController.getValue();
            if (controller != null) {
                if (controller.getCurrentDestination() == null) {
                    controller.navigate(controller.getGraph().getId());
                }
            }
        });
        return selectedNavController;
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
