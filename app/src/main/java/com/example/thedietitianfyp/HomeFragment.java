package com.example.thedietitianfyp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thedietitianfyp.utils.Save;
import com.google.firebase.auth.FirebaseAuth;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private MenuItem menuItemLogOut; // logout button
    private MenuItem menuItemRefresh; // reset foods consumed
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // constructor
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateTable();
        userAddingFoodFromImageView(); // get home fragment ready
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);

        /* Set Title */
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Home");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.logout) {
            menuItemLogOut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    FirebaseAuth.getInstance().signOut();
                    Save.save(getActivity().getApplicationContext(), "session", "false");
                    Intent intToLogin = new Intent(getActivity(), Login.class);
                    startActivity(intToLogin);
                    UIUtil.hideKeyboard(getActivity()); // hide keyboard (for searching)
                    return false;
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        ((MainActivity) getActivity()).getMenuInflater().inflate(R.menu.menu_home, menu);
        menuItemLogOut = menu.findItem(R.id.logout);
        menuItemLogOut.setVisible(true);

        menuItemRefresh = menu.findItem(R.id.refresh);
        menuItemRefresh.setVisible(true);

        menuItemRefresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked

                                DBAdapter db = new DBAdapter(getActivity());
                                db.open();

                                // select
                                String fields[] = new String[] {
                                        "_id",
                                        "fd_id",
                                        "fd_date",
                                        "fd_portion",
                                        "fd_food_name",
                                        "fd_calories",
                                        "fd_protein",
                                        "fd_carbohydrates",
                                        "fd_fat"
                                };
                                Cursor c;

                                c = db.select("food_diary", fields);

                                int cursorCount = c.getCount();
                                for(int x  = 0; x < cursorCount; x++) {
                                    String currentID = c.getString(0);
                                    long longID = Long.parseLong(currentID);
                                    db.delete("food_diary", "_id", longID);
                                    c.moveToNext();

                                }
                                // Add table rows
                                TableLayout tl = (TableLayout)getActivity().findViewById(R.id.tableLayoutHomeItems); /* Find Tablelayout defined in main.xml */
                                TableRow tr1 = new TableRow(getActivity()); /* Create a new row to be added. */
                                tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                TableRow tr2 = new TableRow(getActivity()); /* Create a new row to be added. */
                                tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                // Table row: TextView Name
                                TextView textViewName = new TextView(getActivity()); // Add textview
                                textViewName.setText("");
                                tr1.addView(textViewName);

                                // Table row: TextView Calories
                                TextView textViewCalories = new TextView(getActivity()); // Add textview
                                textViewCalories.setText("");
                                tr1.addView(textViewCalories);

                                // Table row: TextView subLine
                                TextView textViewSubLine = new TextView(getActivity()); // Add textview
                                textViewSubLine.setText("");
                                tr2.addView(textViewSubLine);

                                // Add row to table
                                tl.addView(tr1, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)); /* Add row to TableLayout. */
                                tl.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)); /* Add row to TableLayout. */

                                // Move user back to correct design
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                                        .replace(R.id.nav_host_fragment, new HomeFragment(), HomeFragment.class.getName()).commit();

                                Toast.makeText(getActivity(), "Foods have been reset!", Toast.LENGTH_SHORT).show();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                Toast.makeText(getActivity(), "Foods reset cancelled!", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("This will reset your foods consumed for today\nAre you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return false;
            }
        });
    }

    public void userAddingFoodFromImageView(){
        ImageView addBreakfast = (ImageView) getActivity().findViewById(R.id.imageViewAddFood);
        addBreakfast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move user back to correct design
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.nav_host_fragment, new FoodsFragment(), FoodsFragment.class.getName()).commit();
            }
        });
    }// userAddingFoodFromImageView

    /* update table with added foods */
    public void updateTable(){
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        // select nutrition info
        String nutritionFields[] = new String[] {
                "goal_id",
                "goal_current_weight",// 1
                "goal_target_weight",// 2
                "goal_weekly_goal",// 3
                "goal_date",// 4
                "goal_goal", // 5
                "goal_calories_bmr",// 6
                "goal_calories_with_activity",// 7
                "goal_calories_with_diet",// 8
                "goal_calories_with_activity_and_diet",// 9
                "goal_proteins_bmr",// 10
                "goal_carbohydrates_bmr",// 11
                "goal_fats_bmr",// 12
                "goal_proteins_with_activity",// 13
                "goal_carbohydrates_with_activity",// 14
                "goal_fats_with_activity",// 15
                "goal_proteins_with_activity_and_diet",// 16
                "goal_carbohydrates_with_activity_and_diet",// 17
                "goal_fats_with_activity_and_diet",// 18
                "goal_proteins_with_diet",
                "goal_carbohydrates_with_diet",
                "goal_fats_with_diet"
        };
        Cursor nutritionCursor;
        nutritionCursor = db.select("goal", nutritionFields);

        // get calories goal with diet
        String caloriesGoal = nutritionCursor.getString(8);
        int intCaloriesGoal = Integer.parseInt(caloriesGoal);

        // get protein goal with activity and diet
        String proteinGoal = nutritionCursor.getString(16);
        int intProteinGoal = Integer.parseInt(proteinGoal);

        // get carbohydrates goal with activity and diet
        String carbohydratesGoal = nutritionCursor.getString(17);
        int intCarbohydratesGoal = Integer.parseInt(carbohydratesGoal);

        // get fats with activity and diet
        String fatGoal = nutritionCursor.getString(18);
        int intFatGoal = Integer.parseInt(fatGoal);

        /* set remaining nutrition values */

        // calories
        TextView textViewCaloriesAllowance = (TextView)getActivity().findViewById(R.id.textViewCaloriesAllowanceBlank);
        textViewCaloriesAllowance.setText(" " + caloriesGoal);

        // proteins
        TextView textViewProteinsAllowance = (TextView)getActivity().findViewById(R.id.textViewProteinAllowanceBlank);
        textViewProteinsAllowance.setText(" " + proteinGoal);

        // carbohydrates
        TextView textViewCarbohydratesAllowance = (TextView)getActivity().findViewById(R.id.textViewCarbohydratesAllowanceBlank);
        textViewCarbohydratesAllowance.setText(" " + carbohydratesGoal);

        // fats
        TextView textViewFatsAllowance = (TextView)getActivity().findViewById(R.id.textViewFatsAllowanceBlank);
        textViewFatsAllowance.setText(" " + fatGoal);

        // select food diary information
        String fields[] = new String[] {
                "_id",
                "fd_id",
                "fd_date",
                "fd_portion",
                "fd_food_name",
                "fd_calories",
                "fd_protein",
                "fd_carbohydrates",
                "fd_fat"
        };
        Cursor c;

        c = db.select("food_diary", fields);

        // ready variables for sum
        int intFDCaloriesConsumed = 0;
        int intFDProteinsConsumed = 0;
        int intFDCarbohydratesConsumed = 0;
        int intFDFatConsumed = 0;

        // loop through cursor looking for all items
        int intCursorCount = c.getCount();
        for(int x  = 0; x < intCursorCount; x++) {

            // get portion
            String portion = c.getString(3);

            // get food name
            String foodName = c.getString(4);

            // get food calories
            String calories = c.getString(5);
            int intCalories = Integer.parseInt(calories);

            // get protein
            String protein = c.getString(6);
            int intProtein = Integer.parseInt(protein);

            // get carbohydrates
            String carbohydrates = c.getString(7);
            int intCarbohydrates = Integer.parseInt(carbohydrates);

            // get fat
            String fat = c.getString(8);
            int intFat = Integer.parseInt(fat);

            // subline
            String subline = ("Protein: " + protein + ", Carbs: " + carbohydrates + ", Fat: " + fat
                    + ", Portion: " + portion);

            // Add table rows
            TableLayout tl = (TableLayout)getActivity().findViewById(R.id.tableLayoutHomeItems); /* Find Tablelayout defined in main.xml */
            TableRow tr1 = new TableRow(getActivity()); /* Create a new row to be added. */
            tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TableRow tr2 = new TableRow(getActivity()); /* Create a new row to be added. */
            tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            // Table row: TextView Name
            TextView textViewName = new TextView(getActivity()); // Add textview
            textViewName.setText(foodName);
            tr1.addView(textViewName);

            // Table row: TextView Calories
            TextView textViewCalories = new TextView(getActivity()); // Add textview
            textViewCalories.setText(calories);
            tr1.addView(textViewCalories);

            // Table row: TextView subLine
            TextView textViewSubLine = new TextView(getActivity()); // Add textview
            textViewSubLine.setText(subline);
            tr2.addView(textViewSubLine);

            // Table row: textview blank
            TextView textViewBlank = new TextView(getActivity()); // Add textview
            textViewBlank.setText("");
            tl.addView(textViewBlank);

            // Add row to table
            tl.addView(tr1, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)); /* Add row to TableLayout. */
            tl.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)); /* Add row to TableLayout. */

            // sum fields these get the nutrition value users have consumed and updates
            intFDCaloriesConsumed = intFDCaloriesConsumed + intCalories;
            intFDProteinsConsumed = intFDProteinsConsumed + intProtein;
            intFDCarbohydratesConsumed = intFDCarbohydratesConsumed + intCarbohydrates;
            intFDFatConsumed = intFDFatConsumed + intFat;

            c.moveToNext();
        }

        // update total calories consumed
        TextView textViewTotalCalories = (TextView) getActivity().findViewById(R.id.textViewHomeCalories);
        textViewTotalCalories.setText("" + intFDCaloriesConsumed);

        // update calories remaining for today
        TextView textViewCaloriesRemaining = (TextView)getActivity().findViewById(R.id.textViewCaloriesRemainingBlank);
        textViewCaloriesRemaining.setText(" " + String.valueOf(intCaloriesGoal - intFDCaloriesConsumed));

        // update proteins remaining for today
        TextView textViewProteinRemaining = (TextView)getActivity().findViewById(R.id.textViewProteinRemainingBlank);
        textViewProteinRemaining.setText(" " + String.valueOf(intProteinGoal - intFDProteinsConsumed));

        // update carbohydrates remaining for today
        TextView textViewCarbohydrateRemaining = (TextView)getActivity().findViewById(R.id.textViewCarbohydratesRemainingBlank);
        textViewCarbohydrateRemaining.setText(" " + String.valueOf(intCarbohydratesGoal - intFDCarbohydratesConsumed));

        // update fats remaining for today
        TextView textViewFatRemaining = (TextView)getActivity().findViewById(R.id.textViewFatsRemainingBlank);
        textViewFatRemaining.setText(" " + String.valueOf(intFatGoal - intFDFatConsumed));

        db.close();
    }// update table


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}