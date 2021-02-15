package com.example.thedietitianfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.hololo.tutorial.library.Step;
import com.hololo.tutorial.library.TutorialActivity;

public class OnBoardingActivity extends TutorialActivity {

    int smileyFaceUnicode = 0x1F601;
    int goalUnicode = 0x1F6A9;
    int journalUnicode = 0x1F4D3;
    int foodUnicode = 	0x1F35B;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_on_boarding);

        // on boarding screen 1
        addFragment(new Step.Builder()
                .setTitle("Welcome to my app")
                .setContent("theDietitian is a calorie counting app\n\n" +
                        "it can be used as a food journal to count calories, macros and store your favorite foods " + new String(Character.toChars(smileyFaceUnicode)))
                .setSummary("Swipe to view more -->")
                .setBackgroundColor(Color.parseColor("#4682B4"))
                .setDrawable(R.drawable.calories_calculator).build());

        // on boarding screen 2
        addFragment(new Step.Builder()
                .setTitle("Harris Benedict Equation")
                .setContent("theDietitian uses a smart equation to calculate your daily energy expenditure, " +
                        "with this it is able to calculate your macros and caloric intake to reach your goal" + new String(Character.toChars(goalUnicode)))
                .setSummary("Swipe to view more -->")
                .setBackgroundColor(Color.parseColor("#4682B4"))
                .setDrawable(R.drawable.bmi).build());

        // on boarding screen 3
        addFragment(new Step.Builder()
                .setTitle("We've got you covered!")
                .setContent("theDietitian comes packed with pre installed foods, most of these are everyday basics. " +
                        "But not to worry our app allows you to customise these foods or add your own home made dishes" + new String(Character.toChars(foodUnicode)))
                .setSummary("Swipe to view more -->")
                .setBackgroundColor(Color.parseColor("#4682B4"))
                .setDrawable(R.drawable.calories).build());

        // on boarding screen 4
        addFragment(new Step.Builder()
                .setTitle("Firebase Barcode Scanner")
                .setContent("theDietitian is also equipped with a powerful barcode scanner which is powered by firebase. " +
                        "this barcode scanner connects to the world open food facts API to find your foods" + new String(Character.toChars(journalUnicode)))
                .setSummary("Tap finish to begin!")
                .setBackgroundColor(Color.parseColor("#4682B4"))
                .setDrawable(R.drawable.scanner).build());
    }

    @Override
    public void finishTutorial() {
        super.finishTutorial();
        // on boarding
        Intent i = new Intent(OnBoardingActivity.this, SignUp.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(i);
    }
}