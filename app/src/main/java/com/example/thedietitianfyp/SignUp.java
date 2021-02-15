package com.example.thedietitianfyp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thedietitianfyp.utils.Save;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class SignUp extends AppCompatActivity {

    /* Variables */
    private String[] arraySpinnerDOBDay = new String[31];
    private String[] arraySpinnerDOBYear = new String[100];
    EditText emailID, password;
    FirebaseAuth mFirebaseAuth; // store credentials of users to firebase

    /* onCreate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailID = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);

        /*********************** Populate spinners for dobDay & dobYear ***********************/

        /* Fill numbers for date of birth days */
        int user_counter = 0;
        for (int x = 0; x < 31; x++) {
            user_counter = x + 1;
            this.arraySpinnerDOBDay[x] = "" + user_counter;
        }
        Spinner spinnerDOBDay = (Spinner) findViewById(R.id.spinnerDOBDay);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinnerDOBDay);
        spinnerDOBDay.setAdapter(adapter);

        //Fill numbers for date of birth year
        // get current year、month and day
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int end = year - 100;
        int index = 0;
        for (int x = year; x > end; x--) {
            this.arraySpinnerDOBYear[index] = "" + x;

            index++;
        }

        Spinner spinnerDOBYear = (Spinner) findViewById(R.id.spinnerDOBYear);
        ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinnerDOBYear);
        spinnerDOBYear.setAdapter(adapterYear);

        /*********************** Populate spinners for dobDay & dobYear ***********************/

        /*********************** Listeners ***********************/

        /* Measurement Listener (checks if user has selected Imperial or Metric) */
        Spinner spinnerMeasurement = (Spinner) findViewById(R.id.spinnerMeasurement);
        spinnerMeasurement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                measurementChanged(); // Checks if user has selected Imperial or Metric
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //measurementChanged();
            }
        });

        //Button Listener (SignUp Submit)
        Button buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailID.getText().toString();
                String pwd  = password.getText().toString();

                // Check email
                if(email.isEmpty()) {
                    emailID.setError("Please enter email");
                    emailID.requestFocus();
                }
                // Check password
                else if(pwd.isEmpty()) {
                    password.setError("Please enter password");
                    password.requestFocus();
                }
                else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(SignUp.this, "Fields are empty!", Toast.LENGTH_SHORT).show();
                }
                // Create user
                else if (!(email.isEmpty() && pwd.isEmpty())) {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                                Toast.makeText(SignUp.this, "SignUp Unsuccessful, please try again!", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                signUpSubmit();// Process information and move to signupGoal

                                // Save session if user completes signup
                                // Save value is true for session
                                Save.save(getApplicationContext(), "session", "true");
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(SignUp.this, "Error!, please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });// signUp Submit

        /*********************** Listeners ***********************/

        //Hide error icon/message
        ImageView imageViewError = (ImageView) findViewById(R.id.imageViewError);
        imageViewError.setVisibility(View.GONE);

        TextView textViewErrorMessage = (TextView) findViewById(R.id.textViewErrorMessage);
        textViewErrorMessage.setVisibility(View.GONE);

        //Hide Inches field
        EditText editTextHeightInches = (EditText) findViewById(R.id.editTextInches);
        editTextHeightInches.setVisibility(View.GONE);
    }// onCreate

    /* Measurement Changed Method */
    /* Detects whether user has selected Imperial or Metric */
    public void measurementChanged() {
        //Measurement
        Spinner spinnerMeasurement = (Spinner) findViewById(R.id.spinnerMeasurement);
        String stringMeasurement = spinnerMeasurement.getSelectedItem().toString();

        EditText editTextHeightCM = (EditText) findViewById(R.id.editTextHeight);
        EditText editTextHeightInches = (EditText) findViewById(R.id.editTextInches);
        String stringHeightCM = editTextHeightCM.getText().toString();
        String stringHeightInches = editTextHeightInches.getText().toString();

        TextView textViewInches = (TextView) findViewById(R.id.textViewInches);
        TextView textViewKG = (TextView) findViewById(R.id.textViewKG);

        double heightCM = 0;
        double heightFeet = 0;
        double heightInches = 0;

        /* Updates value on measurement when switching from metric to imperial */
        if (stringMeasurement.startsWith("I")) {
            //Imperial
            editTextHeightInches.setVisibility(View.VISIBLE);
            textViewInches.setText("Feet,inches");
            textViewKG.setText("Pounds");

            // Height
            try {
                heightCM = Double.parseDouble(stringHeightCM);
            } catch (NumberFormatException nfe) {

            }
            if (heightCM != 0) {
                //convert cm to feet/inches
                //feet = (cm * 0.3937008)/12

                heightFeet = (heightCM * 0.3937008) / 12;
                heightFeet = Math.round(heightFeet);
                int intHeightFeet = (int) heightFeet;

                editTextHeightCM.setText("" + heightFeet);
            }

            //Metric
        } else {
            editTextHeightInches.setVisibility(View.GONE);
            textViewInches.setText("cm");
            textViewKG.setText("KG");

            /* Convert feet into cm */
            try {
                heightFeet = Double.parseDouble(stringHeightCM);
            } catch (NumberFormatException nfe) {
            }

            //Convert Inches
            try {
                heightInches = Double.parseDouble(stringHeightInches);
            } catch (NumberFormatException nfe) {

            }

            /* Stores data in CM to put int SQLLite database */
            //converting feet into centimetres
            //Formula = ((foot * 12) + inches) * 2.54)
            if (heightFeet != 0 && heightInches != 0) {

                heightCM = ((heightFeet * 12) + heightInches) * 2.54;
                heightCM = Math.round(heightCM);
                editTextHeightCM.setText("" + heightCM);
            }
        }

        // Weight
        EditText editTextWeight = (EditText) findViewById(R.id.editTextWeight);
        String stringWeight = editTextWeight.getText().toString();

        double doubleWeight = 0;

        try {
            doubleWeight = Double.parseDouble(stringWeight);
        } catch (NumberFormatException nfe) {
        }

        if (doubleWeight != 0) {

            //Imperial
            if (stringMeasurement.startsWith("I")) {
                //KG to lbs
                // 1 lb = 0.45359237 kg
                // 1KG = 2.204623lb
                doubleWeight = Math.round(doubleWeight / 0.45359237);
            }
            //Metric
            else {
                //lbs to KG
                doubleWeight = Math.round(doubleWeight * 0.45359237);
            }
            editTextWeight.setText("" + doubleWeight);
        }
    }// measurementChanged

    /* Sign Up Submit */
    /* Logic for submit button */
    public void signUpSubmit() {
        //Error
        TextView textViewErrorMessage = (TextView) findViewById(R.id.textViewErrorMessage);
        ImageView imageViewError = (ImageView) findViewById(R.id.imageViewError);
        String errorMessage = "";

        //Error Handling
        if (errorMessage.isEmpty()) {
            imageViewError.setVisibility(View.GONE);
            textViewErrorMessage.setVisibility(View.GONE);
        } else {
            textViewErrorMessage.setText(errorMessage);
            imageViewError.setVisibility(View.VISIBLE);
            textViewErrorMessage.setVisibility(View.VISIBLE);
        }

        /* Processing information from sign up activity */

        /* Extracts data from user name (EditText) */
        TextView textViewUserName = (TextView) findViewById(R.id.textViewUserName);
        EditText editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        String stringUserName = editTextUserName.getText().toString();

        if (stringUserName.isEmpty() || stringUserName.startsWith(" ")) {
            textViewUserName.setTextColor(Color.RED);
            errorMessage = "Invalid User Name!"; // Error Handling
        } else {
            textViewUserName.setTextColor(Color.GRAY);
        }// User Name

        /* Extracts data from user password (EditText) */
        TextView textViewUserPassword = (TextView) findViewById(R.id.textViewPassword);
        EditText editTextUserPassword = (EditText) findViewById(R.id.editTextPassword);
        String stringUserPassword = editTextUserPassword.getText().toString();

        if (stringUserPassword.isEmpty() || stringUserPassword.startsWith(" ")) {
            textViewUserPassword.setTextColor(Color.RED);
            errorMessage = "Password cannot be empty!"; // Error Handling
        } else {
            textViewUserName.setTextColor(Color.GRAY);
        }// User Password

        /* Extracts data from user email (EditText) */
        TextView textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        String stringEmail = editTextEmail.getText().toString();

        if (stringEmail.isEmpty() || stringEmail.startsWith(" ")) {
            textViewEmail.setTextColor(Color.RED);
            errorMessage = "Invalid Email!"; // Error Handling
        } else {
            textViewEmail.setTextColor(Color.GRAY);
        }// User Email


        /* Extracts data from user dobDay (Spinner) */
        Spinner spinnerDOBDay = (Spinner) findViewById(R.id.spinnerDOBDay);
        String stringDOBDay = spinnerDOBDay.getSelectedItem().toString();

        int intDOBDay = 0;
        try {
            intDOBDay = Integer.parseInt(stringDOBDay);

            if (intDOBDay < 10) {
                stringDOBDay = "0" + stringDOBDay;
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
            errorMessage = "Day for DOB cannot be empty";
        }// dobDay

        /* Extracts data from user dobMonth spinner */
        Spinner spinnerDOBMonth = (Spinner) findViewById(R.id.spinnerDOBMonth);
        String stringDOBMonth = spinnerDOBMonth.getSelectedItem().toString();
        if (stringDOBMonth.startsWith("Jan")) {
            stringDOBMonth = "01";
        } else if (stringDOBMonth.startsWith("Feb")) {
            stringDOBMonth = "02";
        } else if (stringDOBMonth.startsWith("Mar")) {
            stringDOBMonth = "03";
        } else if (stringDOBMonth.startsWith("Apr")) {
            stringDOBMonth = "04";
        } else if (stringDOBMonth.startsWith("May")) {
            stringDOBMonth = "05";
        } else if (stringDOBMonth.startsWith("Jun")) {
            stringDOBMonth = "06";
        } else if (stringDOBMonth.startsWith("Jul")) {
            stringDOBMonth = "07";
        } else if (stringDOBMonth.startsWith("Aug")) {
            stringDOBMonth = "08";
        } else if (stringDOBMonth.startsWith("Sep")) {
            stringDOBMonth = "09";
        } else if (stringDOBMonth.startsWith("Oct")) {
            stringDOBMonth = "10";
        } else if (stringDOBMonth.startsWith("Nov")) {
            stringDOBMonth = "11";
        } else if (stringDOBMonth.startsWith("Dec")) {
            stringDOBMonth = "12";
        }// dobMonth

        /* Extracts data from user dobYear spinner */
        Spinner spinnerDOBYear = (Spinner) findViewById(R.id.spinnerDOBYear);
        String stringDOBYear = spinnerDOBYear.getSelectedItem().toString(); // get selected item
        int intDOBYear = 0;

        try {
            intDOBYear = Integer.parseInt(stringDOBYear);
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
            errorMessage = "Year for DOB cannot be empty";
        }// dobYear

        /* Stores DOB in this format */
        String dateOfBirth = stringDOBDay + "/" + stringDOBMonth + "/" + intDOBYear;
        //Toast.makeText(this, dateOfBirth, Toast.LENGTH_SHORT).show();

        /* Extracts data from Gender radio button */
        RadioGroup radioGroupGender = (RadioGroup) findViewById(R.id.radioGroupGender);
        int radioButtonID = radioGroupGender.getCheckedRadioButtonId(); // get selected radio button from radioGroup
        View radioButtonGender = radioGroupGender.findViewById(radioButtonID);
        int position = radioGroupGender.indexOfChild(radioButtonGender);

        String stringGender = "";
        if (position == 0) {
            stringGender = "male";
        } else {
            stringGender = "female";
        }// Gender

        //Toast.makeText(this, radioButtonGender.getText(), Toast.LENGTH_SHORT).show();

        /* Extracts data from user height (EditText) */
        EditText editTextHeightCM = (EditText) findViewById(R.id.editTextHeight);
        EditText editTextHeightInches = (EditText) findViewById(R.id.editTextInches);
        String stringHeightCM = editTextHeightCM.getText().toString(); // get height cm (metric)
        String stringHeightInches = editTextHeightInches.getText().toString(); // get height feet (imperial)

        double heightCM = 0;
        double heightFeet = 0;
        double heightInches = 0;
        boolean isMetric = true;

        Spinner spinnerMeasurement = (Spinner) findViewById(R.id.spinnerMeasurement);
        String stringMeasurement = spinnerMeasurement.getSelectedItem().toString();

        /* Checks if user has specified metric or imperial */
        int intMeasurement = spinnerMeasurement.getSelectedItemPosition(); // checks for imperial or metric

        if (intMeasurement == 0) {
            stringMeasurement = "Metric";
        } else {
            stringMeasurement = "Imperial";
        }

        // Imperial
        if (stringMeasurement.startsWith("I")) {
            isMetric = false;
        }

        // Metric
        if (isMetric) {
            //Convert CM
            try {
                heightCM = Double.parseDouble(stringHeightCM);
                heightCM = Math.round(heightCM);
            } catch (NumberFormatException nfe) {
                errorMessage = "Height (cm) value invalid!";
            }
        }
        // Imperial
        else {
            //Convert Feet
            try {
                heightFeet = Double.parseDouble(stringHeightCM);
            } catch (NumberFormatException nfe) {
                errorMessage = "Height (feet) value invalid!";
            }

            //Convert Inches
            try {
                heightInches = Double.parseDouble(stringHeightInches);
            } catch (NumberFormatException nfe) {
                errorMessage = "Height (inches) value invalid!";
            }

            //converting feet into centimetres (user has selected imperial)
            //Formula = ((foot * 12) + inches) * 2.54)

            /* Stores data in CM to put int SQLLite database */
            heightCM = ((heightFeet * 12) + heightInches) * 2.54;
            heightCM = Math.round(heightCM);
        }// User Height

//        Toast.makeText(this, "Height cm = " + heightCM + "\nHeight feet = " + heightFeet + "and  " + heightInches, Toast.LENGTH_LONG).show();

        /* Extracts data from weight (EditText) */
        EditText editTextWeight = (EditText) findViewById(R.id.editTextWeight);
        String stringWeight = editTextWeight.getText().toString();

        double doubleWeight = 0;

        try {
            doubleWeight = Double.parseDouble(stringWeight);
        } catch (NumberFormatException nfe) {
            errorMessage = "Weight is not valid";
        }

        /* Checks if user has specified metric or imperial */
        if (isMetric == true) {
        } else {
            //lbs to KG
            doubleWeight = Math.round(doubleWeight * 0.45359237);
        }// User Weight

        //* Extracts data from activity level (Spinner) */
        Spinner spinnerAL = (Spinner) findViewById(R.id.spinnerActivityLevel);
        String stringAL = spinnerAL.getSelectedItem().toString();
        int intAL = spinnerAL.getSelectedItemPosition(); // User ActivityLevel

        // 0: Little to no exercise
        // 1: Light exercise (1–3 days per week)
        // 2: Moderate exercise (3–5 days per week)
        // 3: Heavy exercise (6–7 days per week)

        /*********************** Inserts all data from signUp to SQLLite database (user information) ***********************/

        /* Open Database */
        DBAdapter db = new DBAdapter(this);
        db.open();

        /*Quote Smart (Prevent SQL injection) */
        String stringEmailSQL = db.quoteSmart(stringEmail);
        String dateOfBirthSQL = db.quoteSmart(dateOfBirth);
        String stringGenderSQL = db.quoteSmart(stringGender);
        String stringMeasurementSQL = db.quoteSmart(stringMeasurement);
        String stringUserNameSQL = db.quoteSmart(stringUserName);
        String stringUserPasswordSQL = db.quoteSmart(stringUserPassword);

        int intActivityLevelSQL = db.quoteSmart(intAL);
        double heightCMSQL = db.quoteSmart(heightCM);
        double doubleWeightSQL = db.quoteSmart(doubleWeight);

        //Input for user
        String stringInput = "NULL, " + stringEmailSQL + "," + stringUserPasswordSQL + "," + dateOfBirthSQL + "," +
                stringGenderSQL + "," + heightCMSQL + "," + stringUserNameSQL + "," + stringMeasurementSQL + "," +
                intActivityLevelSQL + "," + doubleWeightSQL;
        db.insert("user",
                "_id, user_email, user_password, user_dob, user_gender, user_height, user_name, " +
                        "user_measurement, user_activity_level, user_weight",
                stringInput);


        DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");//format date
        String goalDate = df1.format(Calendar.getInstance().getTime());


        String goalDateSQL = db.quoteSmart(goalDate);

        //Input for goals
        stringInput = "NULL, " + doubleWeightSQL + "," + goalDateSQL;
        db.insert("goal",
                "_id, goal_current_weight, goal_date",
                stringInput);

        /* Close database */
        db.close();

        //Toast.makeText(this, "Selected Item: " + intAL, Toast.LENGTH_LONG).show();

        /*********************** Inserts all data from signUp to SQLLite database (user information) ***********************/

        /* Move user to MainActivity if there are no errors */
        if (stringEmail.isEmpty() || stringGender.isEmpty() || doubleWeight == 25) {

        } else {
            Intent i = new Intent(SignUp.this, SignUpGoal.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // add animation
        }

        /* Error Handling */
        if (errorMessage.isEmpty()) {
            imageViewError.setVisibility(View.GONE);
            textViewErrorMessage.setVisibility(View.GONE);
        } else {
            textViewErrorMessage.setText(errorMessage);
            imageViewError.setVisibility(View.VISIBLE);
            textViewErrorMessage.setVisibility(View.VISIBLE);
        }
    }// signUpSubmit
}
