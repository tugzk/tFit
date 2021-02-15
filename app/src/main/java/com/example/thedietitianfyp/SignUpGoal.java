package com.example.thedietitianfyp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tomer.fadingtextview.FadingTextView;

import org.w3c.dom.Text;

import java.util.Calendar;

public class SignUpGoal extends AppCompatActivity {

    private FadingTextView fadingTextView; // animation of text array

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_goal);

        fadingTextView = findViewById(R.id.textViewAnimation);
        fadingTextView.pause();

        /* Listener */
        Button buttonBegin = (Button) findViewById(R.id.buttonBegin);
        buttonBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpGoalBegin();
            }
        });
        //Hide errors upon activity creation
        hideErrorHandling();

        /* Detects measurement type selected by upon activity creation */
        measurementUsed();
    }//onCreate

    /*Sign up goal begin button*/
    public void signUpGoalBegin() {
        //Open Database
        DBAdapter db = new DBAdapter(this);
        db.open();

        //Error
        TextView textViewErrorMessage = (TextView) findViewById(R.id.textViewErrorMessage);
        ImageView imageViewError = (ImageView) findViewById(R.id.imageViewError);
        String errorMessage = "";

        /* Extracts data from target weight (EditText) */
        EditText editTextTargetWeight = (EditText) findViewById(R.id.editTextTargetWeight);
        String stringTargetWeight = editTextTargetWeight.getText().toString();
        double doubleTargetWeight = 0;
        try {
            doubleTargetWeight = Double.parseDouble(stringTargetWeight);

        } catch (NumberFormatException nfe) {
            errorMessage = "Target weight is null";
        }// User Target Weight

        /* Extracts data from goal A (Spinner) */
        Spinner spinnerGoalGoal = (Spinner) findViewById(R.id.spinnerWeeklyGoal);
        int intWeeklyGoal = spinnerGoalGoal.getSelectedItemPosition();
        // 0 = Lose Weight
        // 1 = Gain Weight

        /* Extracts data from goal B (Spinner) */
        Spinner spinnerWeeklyGoalB = (Spinner) findViewById(R.id.spinnerWeeklyGoalB);
        String stringWeeklyGoalB = spinnerWeeklyGoalB.getSelectedItem().toString();

        /* If there are no errors update database with goals */
        if (errorMessage.isEmpty()) {
            long goalID = 1;

            /* Update database (Target Weight) */
            double doubleTargetWeightSQL = db.quoteSmart(doubleTargetWeight);
            db.updateDatabase("goal", "_id", goalID, "goal_target_weight",
                    doubleTargetWeightSQL);

            /* Update database (Weekly Goal A) */
            int intWeeklyGoalSQL = db.quoteSmart(intWeeklyGoal);
            db.updateDatabase("goal", "_id", goalID, "goal_goal", intWeeklyGoalSQL);
            // 1: lose
            // 0: Gain

            /* Update database (Weekly Goal B) */
            String stringWeeklyGoalSQL = db.quoteSmart(stringWeeklyGoalB);
            db.updateDatabase("goal", "_id", goalID, "goal_weekly_goal", stringWeeklyGoalSQL);
        }

        /* calculate calories needed for user to reach goal */
        if (errorMessage.isEmpty()) {
            /* Retrieve information from user */
            long rowID = 1;
            String[] fields = new String[]{
                    "_id",
                    "user_dob",
                    "user_gender",
                    "user_height",
                    "user_activity_level"
            };
            Cursor c = db.selectPrimaryKey("user", "_id", rowID, fields);
            String stringUserDOB = c.getString(1);
            String stringUserGender = c.getString(2);
            String stringUserHeight = c.getString(3);
            String stringUserAL = c.getString(4);

            /* Convert birthday into age */
            String[] userAge = stringUserDOB.split("/");
            String stringDay = userAge[0];
            String stringMonth = userAge[1];
            String stringYear = userAge[2];

            int intYear = 0;
            try {
                intYear = Integer.parseInt(stringYear);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            int intMonth = 0;
            try {
                intMonth = Integer.parseInt(stringMonth);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            int intDay = 0;
            try {
                intDay = Integer.parseInt(stringDay);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            // uses getAge method to convert birthday to age
            String stringUserAge = getAge(intDay, intMonth, intYear);

//            Toast.makeText(this, "DOB:" + stringUserDOB + "\nAge:" + stringUserAge + "\nGender:" + stringUserGender
//                    + "\nHeight:" + stringUserHeight + "\nAL:" + stringUserAL , Toast.LENGTH_LONG).show();

            //Convert height to double
            double doubleUserHeight = 0;
            try {
                doubleUserHeight = Double.parseDouble(stringUserHeight);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            //Convert age to integer
            double intUserAge = 0;
            try {
                intUserAge = Integer.parseInt(stringUserAge);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            /* BMR  formula for male & female*/
            //Calculation for male/female
            double bmr = 0;
            if (stringUserGender.startsWith("m")) {
                //Male
                //BMR = 66.5 + (13.75 x body weight (kg)) + (5.003 x height (cm)) - (6.755 x age)
                bmr = 66.5 + (13.75 * doubleTargetWeight) + (5.003 * doubleUserHeight) - (6.755 * intUserAge);
            } else {
                //Female
                //BMR = 655.1 + (9.563 x body weight (kg)) + (1.85 x height (cm)) - (4.676 x age)
                bmr = 655.1 + (9.563 * doubleTargetWeight) + (1.85 * doubleUserHeight) - (4.676 * intUserAge);

                //Toast.makeText(this, "BMR before activity:" + bmr, Toast.LENGTH_LONG).show();
            }
            bmr = Math.round(bmr);

            long goalID = 1;
            double caloriesBmrSQL = db.quoteSmart(bmr);
            db.updateDatabase("goal", "_id", goalID, "goal_calories_bmr", caloriesBmrSQL);

            /*********************** Calculate proteins, carbs & fats  ***********************/

            /* 1: Calculate proteins, carbs & fats (factoring in bmr) */

            //Protein = 10-35%
            //Carbs = 45-65%
            //Fats = 20-35%
            double proteinsBMR = Math.round((bmr * 22.5) / 100);
            double carbsBMR = Math.round((bmr * 50) / 100);
            double fatsBMR = Math.round((bmr * 27.5) / 100);

            //Update database
            double proteinsBMRSQL = db.quoteSmart(proteinsBMR);
            double carbsBMRSQL = db.quoteSmart(carbsBMR);
            double fatsBMRSQL = db.quoteSmart(fatsBMR);
            db.updateDatabase("goal", "_id", goalID, "goal_proteins_bmr", proteinsBMRSQL);
            db.updateDatabase("goal", "_id", goalID, "goal_carbohydrates_bmr", carbsBMRSQL);
            db.updateDatabase("goal", "_id", goalID, "goal_fats_bmr", fatsBMRSQL);

            /* 2: Calculate proteins, carbs & fats (factoring in diet),
             taking into account whether users goal is to lose or gains weight */

            //1 KG = 7700 kcal

            double doubleWeeklyGoal = 0;
            try {
                doubleWeeklyGoal = Double.parseDouble(stringWeeklyGoalB);
            } catch (NumberFormatException nfe) {
                System.out.println("could not parse" + nfe);
            }

            double kcal = 0;
            double caloriesDiet = 0;
            kcal = 7700 * doubleWeeklyGoal;
            if (intWeeklyGoal == 1) {
                // lose weight
                caloriesDiet = Math.round(bmr - (kcal / 7));
            } else {
                // gain weight
                caloriesDiet = Math.round(bmr + (kcal / 7));
            }

            //Update database
            double caloriesDietSQL = db.quoteSmart(caloriesDiet);
            db.updateDatabase("goal", "_id", goalID, "goal_calories_with_diet", caloriesDietSQL);

            /* Calculate proteins, carbs & fats with diet */
            //Protein = 10-35%
            //Carbs = 45-65%
            //Fats = 20-35%
            double proteinsDiet = Math.round((bmr * 22.5) / 100);
            double carbsDiet = Math.round((bmr * 50) / 100);
            double fatsDiet = Math.round((bmr * 27.5) / 100);

            //Update database
            double proteinsDietSQL = db.quoteSmart(proteinsDiet);
            double carbsDietSQL = db.quoteSmart(carbsDiet);
            double fatsDietSQL = db.quoteSmart(fatsDiet);
            db.updateDatabase("goal", "_id", goalID, "goal_proteins_with_diet", proteinsDietSQL);
            db.updateDatabase("goal", "_id", goalID, "goal_carbohydrates_with_diet", carbsDietSQL);
            db.updateDatabase("goal", "_id", goalID, "goal_fats_with_diet", fatsDietSQL);


            /* 3: Calculate proteins, carbs & fats with activity level */

            double caloriesWithActivity = 0;
            switch (stringUserAL) {
                case "0":
                    caloriesWithActivity = bmr * 1.2; // no exercise
                    break;
                case "1":
                    caloriesWithActivity = bmr * 1.375; // light exercise
                    break;
                case "2":
                    caloriesWithActivity = bmr * 1.55; // moderate exercise
                    break;
                case "3":
                    caloriesWithActivity = bmr * 1.725; // heavy exercise
                    break;
            }
            caloriesWithActivity = Math.round(caloriesWithActivity);
            double caloriesWithActivitySQL = db.quoteSmart(caloriesWithActivity);
            db.updateDatabase("goal", "goal_id", goalID, "goal_calories_with_activity", caloriesWithActivitySQL);

            //Protein = 10-35%
            //Carbs = 45-65%
            //Fats = 20-35%
            double proteinsWithActivity = Math.round((caloriesWithActivity * 22.5) / 100);
            double carbsWithActivity = Math.round((caloriesWithActivity * 50) / 100);
            double fatsWithActivity = Math.round((caloriesWithActivity * 27.5) / 100);

            //Update database
            double proteinsWithActivitySQL = db.quoteSmart(proteinsWithActivity);
            double carbsWithActivitySQL = db.quoteSmart(carbsWithActivity);
            double fatsWithActivitySQL = db.quoteSmart(fatsWithActivity);
            db.updateDatabase("goal", "_id", goalID, "goal_proteins_with_activity", proteinsWithActivitySQL);
            db.updateDatabase("goal", "_id", goalID, "goal_carbohydrates_with_activity", carbsWithActivitySQL);
            db.updateDatabase("goal", "_id", goalID, "goal_fats_with_activity", fatsWithActivitySQL);

            //Toast.makeText(this, "BMR after activity:" + bmr, Toast.LENGTH_LONG).show();

            /*4: Calculate proteins, carbs & fats with activity and diet*/

            kcal = 0;
            double caloriesWithActivityAndDiet = 0;
            kcal = 7700 * doubleWeeklyGoal;
            if (intWeeklyGoal == 1) {
                // lose weight
                caloriesWithActivityAndDiet = Math.round(bmr - (kcal / 7));
            } else {
                // gain weight
                caloriesWithActivityAndDiet = Math.round(bmr + (kcal / 7));
            }
            //Update database
            double caloriesSQL = db.quoteSmart(bmr);

            db.updateDatabase("goal", "_id", goalID, "goal_calories_with_activity_and_diet", caloriesSQL);

            //Protein = 10-35%
            //Carbs = 45-65%
            //Fats = 20-35%
            double proteins = Math.round((caloriesWithActivityAndDiet * 22.5) / 100);
            double carbs = Math.round((caloriesWithActivityAndDiet * 50) / 100);
            double fats = Math.round((caloriesWithActivityAndDiet * 27.5) / 100);

            //Update database
            double proteinsSQL = db.quoteSmart(proteins);
            double carbsSQL = db.quoteSmart(carbs);
            double fatsSQL = db.quoteSmart(fats);
            db.updateDatabase("goal", "_id", goalID, "goal_proteins_with_activity_and_diet", proteinsSQL);
            db.updateDatabase("goal", "_id", goalID, "goal_carbohydrates_with_activity_and_diet", carbsSQL);
            db.updateDatabase("goal", "_id", goalID, "goal_fats_with_activity_and_diet", fatsSQL);

        }// calculate energy (begin button)

        /*********************** Calculate proteins, carbs & fats  ***********************/

        //Error Handling
        if (!(errorMessage.isEmpty())) {
            //Error!
            textViewErrorMessage.setText(errorMessage);
            imageViewError.setVisibility(View.VISIBLE);
            textViewErrorMessage.setVisibility(View.VISIBLE);
        }
        db.close();

        /* if no errors move to main activity */
        if (errorMessage.isEmpty()) {
            Intent i = new Intent(SignUpGoal.this, MainActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }//signUpGoalBegin

    /*Detects what measurement type specified by user*/
    public void measurementUsed() {
        DBAdapter db = new DBAdapter(this);
        db.open();

        /* Retrieve row 1 from user */
        long rowID = 1;
        String fields[] = new String[]{
                "_id",
                "user_measurement"
        };

        Cursor c = db.selectPrimaryKey("user", "_id", rowID, fields);
        String measurement;
        measurement = c.getString(1);

        /* Metric/Imperial */
        if (measurement.startsWith("M")) {
            //Metric
        } else {
            //Imperial
            /* KG to Pounds */
            TextView textViewTargetMeasurementType = (TextView) findViewById(R.id.textViewTargetMesurmentType);
            textViewTargetMeasurementType.setText("Pounds");

            /* KG each week to Pounds each week */
            TextView textViewKgEachWeek = (TextView) findViewById(R.id.textViewKgEachWeek);
            textViewKgEachWeek.setText("Pounds each week");
        }

        //Toast.makeText(this, "Measurement:" + measurement, Toast.LENGTH_SHORT).show();
        db.close(); //close database
    }// measurementUsed

    /*Hide error icon/message*/
    public void hideErrorHandling() {
        ImageView imageViewError = (ImageView) findViewById(R.id.imageViewError);
        imageViewError.setVisibility(View.GONE);

        TextView textViewErrorMessage = (TextView) findViewById(R.id.textViewErrorMessage);
        textViewErrorMessage.setVisibility(View.GONE);
    }// hideErrorHandling

    /* convert birthday to age */
    private String getAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(day, month, year);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }// getAge
}
