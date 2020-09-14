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

package com.example.android.navigationadvancedsample.listscreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android.navigationadvancedsample.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shows a static leaderboard with multiple users.
 */
public class Leaderboard extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        ArrayList<String> data = new ArrayList<>();
        for (int i=0; i<10; ++i) {
            data.add("Person " + (i+1));
        }
        MyAdapter viewAdapter = new MyAdapter(data);

        RecyclerView leaderBoardView = view.findViewById(R.id.leaderboard_list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        leaderBoardView.setHasFixedSize(true);
        // specify an viewAdapter (see also next example)
        leaderBoardView.setAdapter(viewAdapter);

        return view;
    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    public static final String USERNAME_KEY = "userName";

    private ArrayList<String> myDataset;

    public MyAdapter(ArrayList<String> myDataset) {
        this.myDataset = myDataset;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    static class ViewHolder extends RecyclerView.ViewHolder {
        View item;
        public ViewHolder(View item) {
            super(item);
            this.item = item;
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_view_item, parent, false);

        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        TextView textView = holder.item.findViewById(R.id.user_name_text);
        textView.setText(myDataset.get(position));

        ImageView imageView = holder.item.findViewById(R.id.user_avatar_image);
        imageView.setImageResource(ListOfAvatars.get().get(position % ListOfAvatars.get().size()));

        holder.item.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString(USERNAME_KEY, myDataset.get(position));
            Navigation.findNavController(holder.item).navigate(
                    R.id.action_leaderboard_to_userProfile,
                    bundle);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myDataset.size();
    }
}

class ListOfAvatars {
    static List<Integer> get() {
        return Arrays.asList(
                R.drawable.avatar_1_raster,
                R.drawable.avatar_2_raster,
                R.drawable.avatar_3_raster,
                R.drawable.avatar_4_raster,
                R.drawable.avatar_5_raster,
                R.drawable.avatar_6_raster
        );
    }
}
