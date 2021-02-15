package com.example.thedietitianfyp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.thedietitianfyp.utils.Save;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.SearchManager;
import android.widget.SearchView.OnQueryTextListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,
        FoodsFragment.OnFragmentInteractionListener {

    /* Variables */
    private AppBarConfiguration mAppBarConfiguration;
    private TextView mTextViewResult;
    private RequestQueue mQueue;
    private Cursor listCursor;
    boolean session;

    /*food variables*/

    // Nutrition X
    String vegan = "https://api.nutritionix.com/v1_1/search/vegan?results=0:20&fields=item_name,brand_name,item_id,nf_calories,nf_protein,nf_total_carbohydrate,nf_total_fat,item_description,nf_ingredient_statement,upc&appId=82a20e19&appKey=96ef83c70fbe42d320ac2bd9c7a39b24";
    String fruit = "https://api.nutritionix.com/v1_1/search/fruit?results=0:20&fields=item_name,brand_name,item_id,nf_calories,nf_protein,nf_total_carbohydrate,nf_total_fat,item_description,nf_ingredient_statement,upc&appId=82a20e19&appKey=96ef83c70fbe42d320ac2bd9c7a39b24";
    String vegetable = "https://api.nutritionix.com/v1_1/search/vegetable?results=0:20&fields=item_name,brand_name,item_id,nf_calories,nf_protein,nf_total_carbohydrate,nf_total_fat,item_description,nf_ingredient_statement,upc&appId=82a20e19&appKey=96ef83c70fbe42d320ac2bd9c7a39b24";
    String salad = "https://api.nutritionix.com/v1_1/search/salad?results=0:20&fields=item_name,brand_name,item_id,nf_calories,nf_protein,nf_total_carbohydrate,nf_total_fat,item_description,nf_ingredient_statement,upc&appId=82a20e19&appKey=96ef83c70fbe42d320ac2bd9c7a39b24";
    String meat = "https://api.nutritionix.com/v1_1/search/meat?results=0:20&fields=item_name,brand_name,item_id,nf_calories,nf_protein,nf_total_carbohydrate,nf_total_fat,item_description,nf_ingredient_statement,upc&appId=82a20e19&appKey=96ef83c70fbe42d320ac2bd9c7a39b24";
    String poultry = "https://api.nutritionix.com/v1_1/search/chicken?results=0:20&fields=item_name,brand_name,item_id,nf_calories,nf_protein,nf_total_carbohydrate,nf_total_fat,item_description,nf_ingredient_statement,upc&appId=82a20e19&appKey=96ef83c70fbe42d320ac2bd9c7a39b24";
    String fish = "https://api.nutritionix.com/v1_1/search/fish?results=0:20&fields=item_name,brand_name,item_id,nf_calories,nf_protein,nf_total_carbohydrate,nf_total_fat,item_description,nf_ingredient_statement,upc&appId=82a20e19&appKey=96ef83c70fbe42d320ac2bd9c7a39b24";

    // Edamam
    String tesco = "https://api.edamam.com/api/food-database/v2/parser?ingr=%20tesco&app_id=4dc39dbc&app_key=1e2fdb8297b9c1b5ee88f64a0fc3349f";
    String mcdonalds = "https://api.edamam.com/api/food-database/v2/parser?ingr=%20mcdonalds&app_id=4dc39dbc&app_key=1e2fdb8297b9c1b5ee88f64a0fc3349f";
    String pizza = "https://api.edamam.com/api/food-database/v2/parser?ingr=%20pizza&app_id=4dc39dbc&app_key=1e2fdb8297b9c1b5ee88f64a0fc3349f";

    /* onCreate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Session Start/Check
        // Create shared preferences for checking and saving session
        SESSION();

        /* Volley Request */
        mQueue = Volley.newRequestQueue(this);

        /* Toolbar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title
        toolbar.setTitle("Dietitian");

        /* Initialize home fragment */
        Fragment fragment = null;
        Class fragmentClass = null;
        fragmentClass = HomeFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).replace(R.id.nav_host_fragment, fragment).commit();

        /* Barcode button */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, BarcodeScanner.class);
                startActivity(i);
            }
        });

        /* Navigation */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Navigation items
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Stetho */
        Stetho.initializeWithDefaults(this);

        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        /*********************** Populate foods & users to SQLLite database  ***********************/

        /* Open Database */
        DBAdapter db = new DBAdapter(this);
        db.open();

        //If no foods populate food from API
        int numberRowsFood = db.count("food");
        if (numberRowsFood < 1) {

            /* NutritionX */
            jsonParse(fruit);
            jsonParse(vegan);
            jsonParse(vegetable);
            jsonParse(salad);
            jsonParse(meat);
            jsonParse(poultry);
            jsonParse(fish);

            /* Edamam */
            jsonParse2(tesco);
            //jsonParse2(mcdonalds);
            //jsonParse2(pizza);

            /* Edamam api works, however there is a small bug sometimes where it throws an index error when app is launched for the first time
               it causes a crash and does to import the foods from this api, this does not always happen, as a result by default i have disabled it
                 however if you are testing this app clear storage of app and retry it will import the data on the second attempt */
        }

        // Count rows in user table
        int numberRowsUser = db.count("user");

        if (numberRowsUser < 1) {
            // Sign up
            Intent i = new Intent(MainActivity.this, SignUp.class);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(i);
        }

        if(numberRowsFood < 1) {
            // starts onboarding activity if app is booted for the first time
            Intent i = new Intent(MainActivity.this, OnBoardingActivity.class);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(i);
        }
        /* Close database */
        db.close();

        /*********************** Populate foods & users to SQLLite database  ***********************/
    }// onCreate


    /* BackButton Navigation */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }// onBackPressed

    /* Handle action bar item clicks */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Animation

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }// onOptionsItemSelected

    /* onOptionsCreated */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }// onCreateOptions

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }// onSupportNavigation

    /* Navigation between fragments */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;

        if (id == R.id.nav_home) {
            fragmentClass = HomeFragment.class;

        } else if (id == R.id.nav_profile) {
            fragmentClass = ProfileFragment.class;

        } else if (id == R.id.nav_food) {
            fragmentClass = FoodsFragment.class;

        }
        // Try to add item to fragment
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Try to show that content
        FragmentManager fragmentManager = getSupportFragmentManager();
        try {
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).replace(R.id.nav_host_fragment, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }// onNavigationItemSelected

    /* API (NutritionX) */
    public void jsonParse(String url) {
        final DBAdapter db = new DBAdapter(this);
        db.open();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("hits");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                String data = json.getString("fields");

                                /* Populate Foods */
                                int itemNameStart = data.indexOf("item_name") + 11;
                                int itemNameEnd = data.indexOf("brand_name") - 2;

                                int brandNameStart = data.indexOf("brand_name") + 12;
                                int brandNameEnd = data.indexOf("item_description") - 2;

                                int descriptionStart = data.indexOf("item_description") + 18;
                                int descriptionEnd = data.indexOf("nf_ingredient_statement") - 2;

                                int ingredientStart = data.indexOf("nf_ingredient_statement") + 25;
                                int ingredientEnd = data.indexOf("nf_calories") - 2;

                                int caloriesStart = data.indexOf("nf_calories") + 13;
                                int caloriesEnd = data.indexOf("nf_total_fat") - 2;

                                int fatStart = data.indexOf("nf_total_fat") + 14;
                                int fatEnd = data.indexOf("nf_total_carbohydrate") - 2;

                                int carbStart = data.indexOf("nf_total_carbohydrate") + 23;
                                int carbEnd = data.indexOf("nf_protein") - 2;

                                int proteinStart = data.indexOf("nf_protein") + 12;
                                int proteinEnd = data.indexOf("nf_serving_size_qty") - 2;


                                int servingQuantityStart = data.indexOf("nf_serving_size_qty") + 21;
                                int servingQuantityEnd = data.indexOf("nf_serving_size_unit") - 2;

                                int servingUnitStart = data.indexOf("nf_serving_size_unit") + 22;
                                int servingUnitEnd = data.lastIndexOf('}');

                                //System.out.println(data);

                                //Quote smart to escape any special characters
                                int itemNameStartSQL = db.quoteSmart(itemNameStart);
                                int itemNameEndSQL = db.quoteSmart(itemNameEnd);

                                int brandNameStartSQL = db.quoteSmart(brandNameStart);
                                int brandNameEndSQL = db.quoteSmart(brandNameEnd);

                                int descriptionStartSQL = db.quoteSmart(descriptionStart);
                                int descriptionEndSQL = db.quoteSmart(descriptionEnd);

                                int ingredientStartSQL = db.quoteSmart(ingredientStart);
                                int ingredientEndSQL = db.quoteSmart(ingredientEnd);

                                int caloriesStartSQL = db.quoteSmart(caloriesStart);
                                int caloriesEndSQL = db.quoteSmart(caloriesEnd);

                                int fatStartSQL = db.quoteSmart(fatStart);
                                int fatEndSQL = db.quoteSmart(fatEnd);

                                int carbStartSQL = db.quoteSmart(carbStart);
                                int carbEndSQL = db.quoteSmart(carbEnd);

                                int proteinStartSQL = db.quoteSmart(proteinStart);
                                int proteinEndSQL = db.quoteSmart(proteinEnd);

                                int servingQuantityStartSQL = db.quoteSmart(servingQuantityStart);
                                int servingQuantityEndSQL = db.quoteSmart(servingQuantityEnd);

                                int servingUnitStartSQL = db.quoteSmart(servingUnitStart);
                                int servingUnitEndSQL = db.quoteSmart(servingUnitEnd);

                                //System.out.println(data);
                                //Insert into database
                                String input = "NULL, " + data.substring(itemNameStartSQL, itemNameEndSQL)
                                        + "," + data.substring(brandNameStartSQL, brandNameEndSQL) + "," +
                                        data.substring(descriptionStartSQL, descriptionEndSQL) + "," +
                                        data.substring(ingredientStartSQL, ingredientEndSQL) + "," +
                                        data.substring(caloriesStartSQL, caloriesEndSQL) + "," +
                                        data.substring(fatStartSQL, fatEndSQL) + "," +
                                        data.substring(carbStartSQL, carbEndSQL) + "," +
                                        data.substring(proteinStartSQL, proteinEndSQL) + "," +
                                        data.substring(servingQuantityStartSQL, servingQuantityEndSQL) + "," +
                                        data.substring(servingUnitStartSQL, servingUnitEndSQL);

                                db.insert("food", "_id, food_name, food_manufacturer_name," +
                                        "food_description, food_ingredient, food_calories," +
                                        "food_fat, food_carbohydrates, food_proteins," +
                                        "food_serving_size, food_serving_measurement", input);
                            }
                            //Get food details
                            String[] fields = new String[]{
                                    "_id",
                                    "food_name",
                                    "food_manufacturer_name",
                                    "food_calories",
                                    "food_serving_size",
                                    "food_serving_measurement"
                            };

                            listCursor = db.select("food", fields, "", "", "food_name", "ASC");

                            db.close();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    /* API (Edamam) */
    public void jsonParse2(String url) {
        final DBAdapter db = new DBAdapter(this);
        db.open();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("hints");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                String food = json.getString("food");
                                //String measurement = json.getString("measures");

                                /* Populate Foods */
                                int labelStart = food.indexOf("label") + 7; // food name
                                int labelEnd = food.indexOf("nutrients") - 2;

                                int caloriesStart = food.indexOf("ENERC_KCAL") + 12; // calories
                                int caloriesEnd = food.indexOf("PROCNT") - 2;

                                int proteinStart = food.indexOf("PROCNT") + 8; // protein
                                int proteinEnd = food.indexOf("FAT") - 2;

                                int fatStart = food.indexOf("FAT") + 5; // fat
                                int fatEnd = food.indexOf("CHOCDF") - 2;

                                int carbsStart = food.indexOf("CHOCDF") + 8; // carbs
                                int carbsEnd = food.indexOf("FIBTG") - 2;

                                int brandStart = food.indexOf("brand") + 7; // brand
                                int brandEnd = food.indexOf("category") - 2;

                                int ingredientsStart = food.indexOf("foodContentsLabel") + 19; // ingredients
                                int ingredientsEnd = food.indexOf("image") - 2;

                                int imageStart = food.indexOf("image") + 7; // image
                                int imageEnd = food.indexOf("servingSizes") - 2;

//                                System.out.println(food);
//                                System.out.println("label: " + food.substring(labelStart, labelEnd));
//                                System.out.println("calories: " + food.substring(caloriesStart, caloriesEnd));
//                                System.out.println("protein: " + food.substring(proteinStart, proteinEnd));
//                                System.out.println("fats: " + food.substring(fatStart, fatEnd));
//                                System.out.println("carbs: " + food.substring(carbsStart, carbsEnd));
//                                System.out.println("brand: " + food.substring(brandStart, brandEnd));
//                                System.out.println("ingredients: " + food.substring(ingredientsStart, ingredientsEnd));
//                                System.out.println("ingredients: " + food.substring(imageStart, imageEnd));

                                //Quote smart to escape any special characters (prevent sql injection)
                                int labelStartSQL = db.quoteSmart(labelStart);
                                int labelEndSQL = db.quoteSmart(labelEnd);

                                int caloriesStartSQL = db.quoteSmart(caloriesStart);
                                int caloriesEndSQL = db.quoteSmart(caloriesEnd);

                                int proteinStartSQL = db.quoteSmart(proteinStart);
                                int proteinEndSQL = db.quoteSmart(proteinEnd);

                                int fatStartSQL = db.quoteSmart(fatStart);
                                int fatEndSQL = db.quoteSmart(fatEnd);

                                int carbsStartSQL = db.quoteSmart(carbsStart);
                                int carbsEndSQL = db.quoteSmart(carbsEnd);

                                int brandStartSQL = db.quoteSmart(brandStart);
                                int brandEndSQL = db.quoteSmart(brandEnd);

                                int ingredientsStartSQL = db.quoteSmart(ingredientsStart);
                                int ingredientsEndSQL = db.quoteSmart(ingredientsEnd);

                                int servingQuantity = 0;

                                //Insert into database
                                String input = "NULL, " + food.substring(labelStartSQL, labelEndSQL)
                                        + "," + food.substring(caloriesStartSQL, caloriesEndSQL) + "," +
                                        food.substring(proteinStartSQL, proteinEndSQL) + "," +
                                        food.substring(fatStartSQL, fatEndSQL) + "," +
                                        food.substring(carbsStartSQL, carbsEndSQL) + "," +
                                        food.substring(brandStartSQL, brandEndSQL) + "," +
                                        food.substring(ingredientsStartSQL, ingredientsEndSQL) + "," +
                                        food.substring(imageStart, imageEnd);

                                db.insert("food", "_id, food_name, food_calories," +
                                        "food_proteins, food_fat, food_carbohydrates," +
                                        "food_manufacturer_name, food_ingredient, food_image", input);
                            }
                            //Get food details
                            String[] fields = new String[]{
                                    "_id",
                                    "food_name",
                                    "food_manufacturer_name",
                                    "food_calories",
                                    "food_serving_size",
                                    "food_serving_measurement"
                            };

                            listCursor = db.select("food", fields, "", "", "food_name", "ASC");

                            db.close();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request); // volley
    }

    public void SESSION() {
        session = Boolean.valueOf(Save.read(getApplicationContext(), "session", "false"));
        if (!session) {

            // If user is logged out direct to login page
            Intent loginScreen = new Intent(getApplicationContext(), Login.class);
            startActivity(loginScreen);
            finish();
        }
    }

    public void onFragmentInteraction(Uri uri) {
    }
}
