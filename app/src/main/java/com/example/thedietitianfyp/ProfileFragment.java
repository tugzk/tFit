package com.example.thedietitianfyp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private static String TAG = "ProfileFragment";
    private Cursor listCursor;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get data from db and display
        initializeGetDataFromDbAndDisplay();

        // change goal
        editGoal();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: start to create pie chart");

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        /* Set Title */
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Profile");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflate menu
        MenuInflater menuInflater = ((MainActivity) getActivity()).getMenuInflater();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    public void initializeGetDataFromDbAndDisplay() {

        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        long rowID = 1;
        String fields[] = new String[]{
                "_id",
                "user_dob",
                "user_gender",
                "user_height",
                "user_measurement"
        };

        Cursor c = db.select("user", fields, "_id", rowID);
        String stringUserDOB = c.getString(1);
        String stringUserGender = c.getString(2);
        String stringUserHeight = c.getString(3);
        String stringUserMeasurement = c.getString(4); // gets user info and displays them

        /* DOB */
        String[] items1 = stringUserDOB.split("/");
        String stringUserDobYear = items1[0];
        String stringUserDobMonth = items1[1];
        String stringUserDobYDay = items1[2];

        /* DOB: Day */

        // Fill numbers for date of birth days
        int spinnerDOBDaySelectedIndex = 0;
        //Toast.makeText(getActivity(), "Day: " + stringUserDobYDay, Toast.LENGTH_LONG).show();

        String[] arraySpinnerDOBDay = new String[31];
        int human_counter = 0;
        for (int x = 0; x < 31; x++) {
            human_counter = x + 1;
            arraySpinnerDOBDay[x] = "" + human_counter;

            if (stringUserDobYDay.equals("0" + human_counter) || stringUserDobYDay.equals("" + human_counter)) {
                spinnerDOBDaySelectedIndex = x;
                //Toast.makeText(getActivity(), "Day: " + stringUserDobYDay + " Index: " + spinnerDOBDaySelectedIndex, Toast.LENGTH_LONG).show();
            }
        }

        Spinner spinnerDOBDay = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileDay);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, arraySpinnerDOBDay);
        spinnerDOBDay.setAdapter(adapter);

        spinnerDOBDay.setSelection(spinnerDOBDaySelectedIndex); // Select index

        /* DOB: Month */
        int intUserDobMonth = 0;
        stringUserDobYDay.replace("0", "");
        try {
            intUserDobMonth = Integer.parseInt(stringUserDobMonth);
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        intUserDobMonth = intUserDobMonth - 1;
        Spinner spinnerDOBMonth = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileYear);
        spinnerDOBMonth.setSelection(intUserDobMonth); // Select index

        /* DOB: Year */
        // Fill numbers for date of birth year

        int spinnerDOBYearSelectedIndex = 0;

        // get current year、month and day
        String[] arraySpinnerDOBYear = new String[100];
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int end = year - 100;
        int index = 0;
        for (int x = year; x > end; x--) {
            arraySpinnerDOBYear[index] = "" + x;
            // Toast.makeText(this, "x = " + x, Toast.LENGTH_SHORT).show();

            if (stringUserDobYear.equals("" + x)) {
                spinnerDOBYearSelectedIndex = index;
                //Toast.makeText(getActivity(), "Year: " + x + " Index: " + spinnerDOBYearSelectedIndex, Toast.LENGTH_LONG).show();
            }
            index++;
        }
        Spinner spinnerDOBYear = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileYear);
        ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, arraySpinnerDOBYear);
        spinnerDOBYear.setAdapter(adapterYear);
        spinnerDOBYear.setSelection(spinnerDOBYearSelectedIndex); // Select index

        /* Gender */
        RadioButton radioButtonGenderMale = (RadioButton) getActivity().findViewById(R.id.editProfileRadioButtonGenderMale);
        RadioButton radioButtonGenderFemale = (RadioButton) getActivity().findViewById(R.id.editProfileRadioButtonGenderFemale);
        if (stringUserGender.startsWith("m")) {
            radioButtonGenderMale.setChecked(true);
            radioButtonGenderFemale.setChecked(false);
        } else {
            radioButtonGenderMale.setChecked(false);
            radioButtonGenderFemale.setChecked(true);
        }

        /* Height */
        EditText editTextEditProfileHeightCM = (EditText) getActivity().findViewById(R.id.editTextEditProfileHeightCm);
        EditText editTextEditProfileHeightInches = (EditText) getActivity().findViewById(R.id.editTextEditProfileHeightInches);
        TextView textViewEditProfileCm = (TextView) getActivity().findViewById(R.id.textViewEditProfileCM);
        if (stringUserMeasurement.startsWith("m")) {
            editTextEditProfileHeightInches.setVisibility(View.GONE);
            editTextEditProfileHeightCM.setText(stringUserHeight);
        } else {
            textViewEditProfileCm.setText("feet and inches");
            double heightCm = 0;
            double heightFeet = 0;
            double heightInches = 0;

            // Find feet
            try {
                heightCm = Double.parseDouble(stringUserHeight);
            } catch (NumberFormatException nfe) {

            }
            if (heightCm != 0) {
                // Convert CM into feet
                // feet = cm * 0.3937008)/12
                heightFeet = (heightCm * 0.3937008) / 12;
                // heightFeet = Math.round(heightFeet);
                int intHeightFeet = (int) heightFeet;

                editTextEditProfileHeightCM.setText("" + intHeightFeet);
            }
        }

        /* Measurement */
        Spinner spinnerEditProfileMeasurement = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileMeasurement);
        if (stringUserMeasurement.startsWith("m")) {
            spinnerEditProfileMeasurement.setSelection(0); // Select index

        } else {
            spinnerEditProfileMeasurement.setSelection(1); // Select index
        }

        /* Listener Measurement spinner */
        spinnerEditProfileMeasurement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                measurementChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        /* Listener buttonSignUp */
        Button buttonEditProfileSubmit = (Button) getActivity().findViewById(R.id.buttonEditProfileSave);
        buttonEditProfileSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfileSubmit();
            }
        });

        // Close db
        db.close();
    }

    public void measurementChanged() {
        // measurement spinner
        Spinner spinnerMeasurement = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileMeasurement);
        String stringMeasurement = spinnerMeasurement.getSelectedItem().toString();

        EditText editTextEditProfileHeightCm = (EditText) getActivity().findViewById(R.id.editTextEditProfileHeightCm);
        EditText editTextEditProfileHeightInches = (EditText) getActivity().findViewById(R.id.editTextEditProfileHeightInches);

        TextView textViewEditProfileCm = (TextView) getActivity().findViewById(R.id.textViewEditProfileCM);

        if (stringMeasurement.startsWith("M")) {
            // Metric
            editTextEditProfileHeightInches.setVisibility(View.GONE);
            textViewEditProfileCm.setText("cm");
        } else {
            // Imperial
            editTextEditProfileHeightInches.setVisibility(View.VISIBLE);
            textViewEditProfileCm.setText("feet & inches");
        }
    }// measurementChanged

    /* edit profile submit */
    private void editProfileSubmit() {
        /*  Get data from database */
        // Database
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        /* Error? */
        int error = 0;

        // Date of Birth Day
        Spinner spinnerDOBDay = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileDay);
        String stringDOBDay = spinnerDOBDay.getSelectedItem().toString();
        int intDOBDay = 0;
        try {
            intDOBDay = Integer.parseInt(stringDOBDay);

            if (intDOBDay < 10) {
                stringDOBDay = "0" + stringDOBDay;
            }

        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
            error = 1;
            Toast.makeText(getActivity(), "Please select a day for birthday.", Toast.LENGTH_SHORT).show();
        }


        // Date of Birth Month
        Spinner spinnerDOBMonth = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileMonth);
        String stringDOBMonth = spinnerDOBMonth.getSelectedItem().toString();
        int positionDOBMonth = spinnerDOBMonth.getSelectedItemPosition();
        int month = positionDOBMonth + 1;
        if (month < 10) {
            stringDOBMonth = "0" + month;
        } else {
            stringDOBMonth = "" + month;
        }
        // Toast.makeText(this, "Month: " + stringDOBMonth, Toast.LENGTH_LONG).show();


        // Date of Birth Year
        Spinner spinnerDOBYear = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileYear);
        String stringDOBYear = spinnerDOBYear.getSelectedItem().toString();
        int intDOBYear = 0;
        try {
            intDOBYear = Integer.parseInt(stringDOBYear);
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
            error = 1;
            Toast.makeText(getActivity(), "Please select a year for your birthday.", Toast.LENGTH_SHORT).show();
        }

        // Put date of birth togheter
        String dateOfBirth = intDOBYear + "-" + stringDOBMonth + "-" + stringDOBDay;
        String dateOfBirthSQL = db.quoteSmart(dateOfBirth);


        // Gender
        RadioGroup radioGroupGender = (RadioGroup) getActivity().findViewById(R.id.editProfileRadioGroupGender);
        int radioButtonID = radioGroupGender.getCheckedRadioButtonId(); // get selected radio button from radioGroup
        View radioButtonGender = radioGroupGender.findViewById(radioButtonID);
        int position = radioGroupGender.indexOfChild(radioButtonGender); // If you want position of Radiobutton

        String stringGender = "";
        if (position == 0) {
            stringGender = "male";
        } else {
            stringGender = "female";
        }
        String genderSQL = db.quoteSmart(stringGender);

        /* Height */
        EditText editTextHeightCm = (EditText) getActivity().findViewById(R.id.editTextEditProfileHeightCm);
        EditText editTextHeightInches = (EditText) getActivity().findViewById(R.id.editTextEditProfileHeightInches);
        String stringHeightCm = editTextHeightCm.getText().toString();
        String stringHeightInches = editTextHeightInches.getText().toString();

        double heightCm = 0;
        double heightFeet = 0;
        double heightInches = 0;
        boolean metric = true;

        // Metric or imperial?
        Spinner spinnerMeasurement = (Spinner) getActivity().findViewById(R.id.spinnerEditProfileMeasurement);
        String stringMeasurement = spinnerMeasurement.getSelectedItem().toString();

        int intMeasurement = spinnerMeasurement.getSelectedItemPosition();
        if (intMeasurement == 0) {
            stringMeasurement = "metric";
        } else {
            stringMeasurement = "imperial";
            metric = false;
        }
        String measurementSQL = db.quoteSmart(stringMeasurement);

        if (metric == true) {

            // Convert CM
            try {
                heightCm = Double.parseDouble(stringHeightCm);
                heightCm = Math.round(heightCm);
            } catch (NumberFormatException nfe) {
                error = 1;
                Toast.makeText(getActivity(), "Height (cm) has to be a number.", Toast.LENGTH_SHORT).show();
            }
        } else {

            // Convert Feet
            try {
                heightFeet = Double.parseDouble(stringHeightCm);
            } catch (NumberFormatException nfe) {
                error = 1;
                Toast.makeText(getActivity(), "Height (feet) has to be a number.", Toast.LENGTH_SHORT).show();
            }

            // Convert inches
            try {
                heightInches = Double.parseDouble(stringHeightInches);
            } catch (NumberFormatException nfe) {
                error = 1;
                Toast.makeText(getActivity(), "Height (inches) has to be a number.", Toast.LENGTH_SHORT).show();
            }

            // Need to convert, we want to save the number in cm
            // cm = ((foot * 12) + inches) * 2.54
            heightCm = ((heightFeet * 12) + heightInches) * 2.54;
            heightCm = Math.round(heightCm);
        }
        stringHeightCm = "" + heightCm;
        String heightCmSQL = db.quoteSmart(stringHeightCm);

        if (error == 0) {

            long id = 1;

            String fields[] = new String[]{
                    "user_dob",
                    "user_gender",
                    "user_height",
                    "user_measurement"
            };
            String values[] = new String[]{
                    dateOfBirthSQL,
                    genderSQL,
                    heightCmSQL,
                    measurementSQL
            };

            db.update("user", "_id", id, fields, values);

            Toast.makeText(getActivity(), "Changes saved", Toast.LENGTH_SHORT).show();

        } // error == 0

        // Close db
        db.close();
    } // editProfileSubmit


    public void addDataSet(PieChart chart) {
        Log.d(TAG, "addDataSet started");
        ArrayList<PieEntry> grams = new ArrayList<>();
        ArrayList<String> macros = new ArrayList<>();

        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        //Get Foods
        String[] fields = new String[]{
                "_id",
                "food_proteins",
                "food_carbohydrates",
                "food_fat"
        };
    }

    public void editGoal() {
        Button buttonEditGoal = (Button) getActivity().findViewById(R.id.buttonGoalEdit);
        buttonEditGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SignUpGoal.class);
                startActivity(intent);
            }
        });
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);

    }
}