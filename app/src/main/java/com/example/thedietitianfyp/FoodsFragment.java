package com.example.thedietitianfyp;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FoodsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FoodsFragment extends Fragment {

    /* Variables */
    private Cursor listCursor; // cursor
    private View mainView;// view

    // menu items
    private MenuItem menuItemSearch;
    private MenuItem menuItemEdit;
    private MenuItem menuItemDelete;
    private MenuItem menuItemAdd;
    MenuItem menuItemAddFoodToDiary;

    // search
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    // holders
    String currentID;
    private String currentFoodID;
    

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /* Constructor */
    public FoodsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FoodsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FoodsFragment newInstance(String param1, String param2) {
        FoodsFragment fragment = new FoodsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /*********************** Default fragment methods  ***********************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Find ListView to populate
        ListView lvItems = (ListView) getActivity().findViewById(R.id.listViewFoods);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        /* Set Title */
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Food");
    }

    /* on Create Options Menu */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu
        ((MainActivity) getActivity()).getMenuInflater().inflate(R.menu.menu_food, menu);

        // Assign variables to XML ID
        // Creating action icon on toolbar (add, edit and delete button)
        menuItemDelete = menu.findItem(R.id.action_delete_food);// delete food from database
        menuItemDelete.setVisible(false);

        menuItemSearch = menu.findItem(R.id.action_search_food);// search feature

        menuItemAdd = menu.findItem(R.id.action_add_food);// user adds custom food

        menuItemAddFoodToDiary = menu.findItem(R.id.action_add_food_to_diary);// user adds food to diary
        menuItemAddFoodToDiary.setVisible(false);

        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        //searchView = (SearchView) menu.findItem(R.id.action_search_food);

        /* Search foods */
        if (menuItemSearch != null) {
            searchView = (SearchView) menuItemSearch.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("onQueryTextChange", newText);

                    DBAdapter db = new DBAdapter(getActivity());
                    db.open();

                    //Get Foods
                    String[] fields = new String[]{
                            "_id",
                            "food_name",
                            "food_manufacturer_name",
                            "food_ingredient",
                            "food_serving_size",
                            "food_serving_measurement",
                            "food_proteins",
                            "food_carbohydrates",
                            "food_fat",
                            "food_calories"
                    };

                    listCursor = db.select("food", fields);
                    String currentID = listCursor.getString(0);
                    int intCurrentID = Integer.parseInt(currentID);

                    // Find ListView to populate
                    ListView lvItems = (ListView) getActivity().findViewById(R.id.listViewFoods);

                    // Attach cursor adapter to the ListView
                    int foodCount = listCursor.getCount();

                    ArrayList<String> foodName = new ArrayList<>();
                    for (int x = 0; x < foodCount; x++) {
                        foodName.add(listCursor.getString(listCursor.getColumnIndex("food_name")));
                        listCursor.moveToNext();
                    }

                    SearchableAdapter food = new SearchableAdapter(getActivity(), foodName);

                    //FoodListAdapter adapter = new FoodListAdapter(getActivity(), listCursor);

                    lvItems.setAdapter(food); // uses foodCursorAdapter

                    food.getFilter().filter(newText);

                    if (newText.isEmpty()) {
                        setFoodList();
                    }

                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i("onQueryTextSubmit", query);
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }// Search food

        // Assign variables to XML ID
        menuItemEdit = menu.findItem(R.id.action_edit_food);

        // Hide as default
        menuItemEdit.setVisible(false);
    }// onCreateOptionMenu

    /* Activity Created */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Populate list of foods with relevant information
        setFoodList(); // sets adapter to list view

        //Create Menu
        setHasOptionsMenu(true);

        //Toast.makeText(getActivity(), "onActivityCreated", Toast.LENGTH_SHORT).show();
    }// onActivityCreated

    /* on Options Selected Item */
    // Add button functionality
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.action_add_food) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);

            setMainView(R.layout.fragment_food_edit);
            Button submitButton = (Button) getActivity().findViewById(R.id.buttonSaveEditFood);

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFoodButton();
                }
            });
            //Toast.makeText(getActivity(), "ID: " + id, Toast.LENGTH_LONG).show();
        }

        if (id == R.id.action_edit_food) {
            editFood();
        }

        searchView.setOnQueryTextListener(queryTextListener);

        return super.onOptionsItemSelected(menuItem);
    }// onOptionsItemSelected

    /* Navigation Selected Item */
    public boolean onNavigationItemSelected(MenuItem item) {
        //Toast.makeText(getActivity(), "m", Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_foods, container, false);
    }

    /* set Main View */
    public void setMainView(int id) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(id, null);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(mainView);
    }

    /*********************** Default fragment methods  ***********************/

    /* Populate List */
    public void setFoodList() {
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        //Get Foods
        String[] fields = new String[]{
                "_id",
                "food_name",
                "food_manufacturer_name",
                "food_ingredient",
                "food_serving_size",
                "food_serving_measurement",
                "food_proteins",
                "food_carbohydrates",
                "food_fat",
                "food_calories"
        };

        //listCursor = db.select("food", fields);
        listCursor = db.select("food", fields, "", "", "food_name", "ASC"); // Order foods alphabetically

        // Find ListView to populate
        final ListView lvItems = (ListView) getActivity().findViewById(R.id.listViewFoods);

        // Setup cursor adapter using cursor from last step
        final FoodListAdapter foodAdapter = new FoodListAdapter(getActivity(), listCursor);

        // Attach cursor adapter to the ListView
        lvItems.setAdapter(foodAdapter); // uses foodCursorAdapter

        foodAdapter.notifyDataSetChanged();

        //onClick Listener
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(position);
            }
        });

        // Close database
        db.close();
    }// setFoodList

    /* Item Clicked Method */
    public void listItemClicked(final int position) {
        /* Change Layout */
        int ID = R.layout.fragment_food_view;
        setMainView(ID);

        menuItemDelete.setVisible(true);
        menuItemEdit.setVisible(true);
        menuItemSearch.setVisible(false);
        menuItemAdd.setVisible(false);
        menuItemSearch.collapseActionView(); // after clicking on search item collapse

        menuItemAddFoodToDiary.setVisible(true);

        //Move cursor to id clicked
        listCursor.moveToPosition(position);

        // Get ID and name from cursor
        String currentID = listCursor.getString(0);
        String currentName = listCursor.getString(1);

        // Change Title to selected item
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(currentName); // change title to selected food

        // Get data
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        //Get Foods
        String[] fields = new String[]{
                "_id",
                "food_name",  //1
                "food_manufacturer_name", //2
                "food_description", //3
                "food_ingredient", //4
                "food_serving_size", //5
                "food_serving_measurement", //6
                "food_calories", //7
                "food_proteins", //8
                "food_carbohydrates", //9
                "food_fat",
                "food_image",
        };
        String currentIDSQL = db.quoteSmart(currentID);
        Cursor foodCursor = db.select("food", fields, "_id", currentIDSQL);

        String foodName = "Name";
        String brandName = "Manufacturer";
        String foodIngredients = "Ingredients";

        // food name
        TextView textViewFoodName = (TextView) getView().findViewById(R.id.textViewFoodName);
        textViewFoodName.setText(foodName + ": " + foodCursor.getString(1));

        // food manufacturer
        TextView textViewBrandName = (TextView) getView().findViewById(R.id.textViewFoodManufacturerName);
        textViewBrandName.setText(brandName + ": " + foodCursor.getString(2));

        // food ingredients
        TextView textViewIngredients = (TextView) getView().findViewById(R.id.textViewFoodIngredients);
        if (foodCursor.getString(4) == null) {
            textViewIngredients.setText(foodIngredients + " n/a\n");
        } else {
            textViewIngredients.setText(foodIngredients + ": " + foodCursor.getString(4));
        }

        // food calories (per hundred grams)
        TextView textViewCalories = (TextView) getView().findViewById(R.id.textViewFoodCaloriesPerPortion);
        if (foodCursor.getString(7).length() == 1) {
            textViewCalories.setText(foodCursor.getString(7).substring(0, 1));
        } else if (foodCursor.getString(7).length() == 2) {
            textViewCalories.setText(foodCursor.getString(7).substring(0, 2));
        } else {
            textViewCalories.setText(foodCursor.getString(7).substring(0, 3));
        }
        // food protein (per hundred grams)
        TextView textViewProtein = (TextView) getView().findViewById(R.id.textViewFoodProteinsPerPortion);
        if (foodCursor.getString(8) == null) {
            textViewProtein.setText("0");
        } else if (foodCursor.getString(8).length() == 1) {
            textViewProtein.setText(foodCursor.getString(8).substring(0, 1)); // single digit
        } else {
            textViewProtein.setText(foodCursor.getString(8).substring(0, 2)); // double digits
        }

        // food carbohydrates (per hundred grams)
        TextView textViewCarbs = (TextView) getView().findViewById(R.id.textViewFoodCarbsPerPortion);
        if (foodCursor.getString(9) == null) {
            textViewCarbs.setText("0");
        } else if (foodCursor.getString(9).length() == 1) {
            textViewCarbs.setText(foodCursor.getString(9).substring(0, 1));
        } else {
            textViewCarbs.setText(foodCursor.getString(9).substring(0, 2));
        }

        // food fat (per hundred grams)
        TextView textViewFats = (TextView) getView().findViewById(R.id.textViewFoodFatPerPortion);
        if (foodCursor.getString(10) == null) {
            textViewFats.setText("0");
        } else if (foodCursor.getString(10).length() == 1) {
            textViewFats.setText(foodCursor.getString(10).substring(0, 1));
        } else {
            textViewFats.setText(foodCursor.getString(10).substring(0, 2));
        }

        // food image
        ImageView foodImage = (ImageView) getView().findViewById(R.id.imageViewFoodImage);
        if (foodCursor.getString(11) == null) {
        } else {
            Picasso.get().load(foodCursor.getString(11)).into(foodImage);
        }

        UIUtil.hideKeyboard(getActivity()); // hide keyboard (for searching)

        menuItemDelete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                deleteFood();
                return false;
            }
        });

        menuItemAddFoodToDiary.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                addFoodToDiary(position);
                return false;
            }
        });

        db.close();
    }// listItemClicked

    /* Edit Food */
    public void editFood() {
        /* Change Layout */
        int ID = R.layout.fragment_food_edit;
        setMainView(ID);

        // Get ID and name from cursor
        currentID = listCursor.getString(0);
        String currentName = listCursor.getString(1);

        // Change Title to selected item
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Edit " + currentName);

        // Get data
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        //Get Foods
        String[] fields = new String[]{
                "_id",
                "food_name",  //1
                "food_manufacturer_name", //2
                "food_ingredient", //3
                "food_serving_size", //4
                "food_serving_measurement", //5
                "food_calories", //6
                "food_proteins", //7
                "food_carbohydrates", //8
                "food_fat", // 9
                "food_image"
        };
        String currentIDSQL = db.quoteSmart(currentID);

        /* Set cursor to retrieve string from column index */
        Cursor foodCursor = db.select("food", fields, "_id", currentIDSQL);

        /* Get Edit Food Properties */

        // food name
        String stringName = foodCursor.getString(1);

        EditText editTextEditFoodName = (EditText) getView().findViewById(R.id.editTextFoodName);
        editTextEditFoodName.setText(stringName);

        // food manufacturer
        String stringBrandName = foodCursor.getString(2);

        EditText editTextBrandName = (EditText) getView().findViewById(R.id.editTextFoodManufacturer);
        editTextBrandName.setText(stringBrandName);

        // food ingredients
        String stringIngredients = foodCursor.getString(3);

        EditText editTextFoodIngredients = (EditText) getView().findViewById(R.id.editTextFoodIngredients);
        editTextFoodIngredients.setText(stringIngredients);

        /* Serving Table */

        // Serving Size
        String stringServingSize = foodCursor.getString(4);

        EditText editTextFoodServingSize = (EditText) getView().findViewById(R.id.EditFoodServingSize);
        editTextFoodServingSize.setText(stringServingSize);

        // Measurement Type
        String stringServingMeasurement = foodCursor.getString(5);

        EditText editTextMeasurementType = (EditText) getView().findViewById(R.id.editTextServingMeasurement);
        editTextMeasurementType.setText(stringServingMeasurement);

        /* Calories table */

        // food calories (per hundred grams)
        EditText editTextFoodCaloriesPerPortion = (EditText) getView().findViewById(R.id.editTextFoodCaloriesPerPortion);
        if (foodCursor.getString(6).length() == 1) {
            editTextFoodCaloriesPerPortion.setText(foodCursor.getString(6).substring(0, 1));
        } else if (foodCursor.getString(6).length() == 0) {
            editTextFoodCaloriesPerPortion.setText("0");
        } else if (foodCursor.getString(6).length() == 2) {
            editTextFoodCaloriesPerPortion.setText(foodCursor.getString(6).substring(0, 2));
        } else {
            editTextFoodCaloriesPerPortion.setText(foodCursor.getString(6).substring(0, 3));
        }

        // food proteins (per hundred grams)
        EditText editTextFoodProteinsPerPortion = (EditText) getView().findViewById(R.id.editTextFoodProteinsPerPortion);
        if (foodCursor.getString(7) == null) {
            editTextFoodProteinsPerPortion.setText("0");
        } else if (foodCursor.getString(7).length() == 1) {
            editTextFoodProteinsPerPortion.setText(foodCursor.getString(7).substring(0, 1)); // single digit
        } else {
            editTextFoodProteinsPerPortion.setText(foodCursor.getString(7)); // double digits
        }

        // food carbohydrates (per hundred grams)
        EditText editTextFoodCarbsPerPortion = (EditText) getView().findViewById(R.id.editTextFoodCarbsPerPortion);
        if (foodCursor.getString(8) == null) {
            editTextFoodCarbsPerPortion.setText("0");
        } else if (foodCursor.getString(8).length() == 1) {
            editTextFoodCarbsPerPortion.setText(foodCursor.getString(8).substring(0, 1));
        } else {
            editTextFoodCarbsPerPortion.setText(foodCursor.getString(8).substring(0, 2));
        }

        // food fats (per hundred grams)
        EditText editTextFoodFatPerPortion = (EditText) getView().findViewById(R.id.editTextFoodFatPerPortion);
        if (foodCursor.getString(9) == null) {
            editTextFoodFatPerPortion.setText("0");
        } else if (foodCursor.getString(9).length() == 1) {
            editTextFoodFatPerPortion.setText(foodCursor.getString(9).substring(0, 1));
        } else {
            editTextFoodFatPerPortion.setText(foodCursor.getString(9).substring(0, 2));
        }

        /* Edit Button listener */
        Button buttonEditFood = (Button) getActivity().findViewById(R.id.buttonSaveEditFood);
        buttonEditFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonEditFoodSubmitOnClick();
                UIUtil.hideKeyboard(getActivity()); // hide keyboard (for searching)

            }
        });

        /* Close db */
        db.close();
    } // editFood

    /* Edit food submit */
    public void buttonEditFoodSubmitOnClick() {

        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        // Error ?
        int error = 0;

        String currentID = listCursor.getString(0);

        // food name
        EditText editFoodName = (EditText) getActivity().findViewById(R.id.editTextFoodName);
        String stringFoodName = editFoodName.getText().toString();
        String stringFoodNameSQL = db.quoteSmart(stringFoodName);
        if (stringFoodName.equals("")) {
            Toast.makeText(getActivity(), "Food name cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        // food manufacturer
        EditText editManufacturerName = (EditText) getActivity().findViewById(R.id.editTextFoodManufacturer);
        String stringManufacturerName = editManufacturerName.getText().toString();
        String stringManufacturerNameSQL = db.quoteSmart(stringManufacturerName);
        if (stringFoodName.equals("")) {
            Toast.makeText(getActivity(), "Food manufacturer cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        // food ingredients
        EditText editIngredients = (EditText) getActivity().findViewById(R.id.editTextFoodIngredients);
        String stringIngredients = editIngredients.getText().toString();
        String stringIngredientsSQL = db.quoteSmart(stringIngredients);
        if (stringFoodName.equals("")) {
            Toast.makeText(getActivity(), "Ingredients cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        /* Serving Table */

        // food serving size
        EditText editServingSize = (EditText) getActivity().findViewById(R.id.EditFoodServingSize);
        String stringServingSize = editServingSize.getText().toString();
        String stringServingSizeSQL = db.quoteSmart(stringServingSize);
        double doubleServingSize = 0;
        if (stringServingSize.equals("")) {
            Toast.makeText(getActivity(), "Please fill in a size.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleServingSize = Double.parseDouble(stringServingSize);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Serving size is not number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }

        // food measurement
        EditText editServingMeasurement = (EditText) getActivity().findViewById(R.id.editTextServingMeasurement);
        String stringServingMeasurement = editServingMeasurement.getText().toString();
        String stringMeasurementSQL = db.quoteSmart(stringServingMeasurement);
        if (stringServingMeasurement.equals("")) {
            Toast.makeText(getActivity(), "Please fill in measurement.", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        /* Calories Table */

        // calories
        EditText editTextCaloriesPerPortion = (EditText) getActivity().findViewById(R.id.editTextFoodCaloriesPerPortion);
        String stringCaloriesPerPortion = editTextCaloriesPerPortion.getText().toString();
        stringCaloriesPerPortion = stringCaloriesPerPortion.replace(",", ".");
        double doubleCaloriesPerPortion = 0;
        if (stringCaloriesPerPortion.equals("")) {
            Toast.makeText(getActivity(), "Calories cannot be blank.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleCaloriesPerPortion = Double.parseDouble(stringCaloriesPerPortion);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Calories is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringCaloriesPerPortionSQL = db.quoteSmart(stringCaloriesPerPortion);

        // proteins
        EditText editFoodProteinsPerPortion = (EditText) getActivity().findViewById(R.id.editTextFoodProteinsPerPortion);
        String stringProteinsPerPortion = editFoodProteinsPerPortion.getText().toString();
        stringProteinsPerPortion = stringProteinsPerPortion.replace(",", ".");
        double doubleProteinsPerPortion = 0;
        if (stringProteinsPerPortion.equals("")) {
            Toast.makeText(getActivity(), "Proteins cannot be blank.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleProteinsPerPortion = Double.parseDouble(stringProteinsPerPortion);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Protein is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringProteinsPerPortionSQL = db.quoteSmart(stringProteinsPerPortion);

        // carbohydrates
        EditText editCarbohydratesPerPortion = (EditText) getActivity().findViewById(R.id.editTextFoodCarbsPerPortion);
        String stringCarbohydratesPerPortion = editCarbohydratesPerPortion.getText().toString();
        stringCarbohydratesPerPortion = stringCarbohydratesPerPortion.replace(",", ".");
        double doubleCarbohydratesPerPortion = 0;
        if (stringCarbohydratesPerPortion.equals("")) {
            Toast.makeText(getActivity(), "Please fill in carbohydrates.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleCarbohydratesPerPortion = Double.parseDouble(stringCarbohydratesPerPortion);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Carbohydrates is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringCarbsPerPortionSQL = db.quoteSmart(stringCarbohydratesPerPortion);

        // fat
        EditText editFatPerPortion = (EditText) getActivity().findViewById(R.id.editTextFoodFatPerPortion);
        String stringFatPerPortion = editFatPerPortion.getText().toString();
        stringFatPerPortion = stringFatPerPortion.replace(",", ".");
        double doubleFatPerPortion = 0;
        if (stringFatPerPortion.equals("")) {
            Toast.makeText(getActivity(), "Please fill in fat.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleFatPerPortion = Double.parseDouble(stringFatPerPortion);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Carbohydrates is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringFatPerPortionSQL = db.quoteSmart(stringFatPerPortion);

        /* Update */
        if (error == 0) {

            String fields[] = new String[]{
                    "food_name",  //1
                    "food_manufacturer_name", //2
                    "food_ingredient", //3
                    "food_serving_size", //4
                    "food_serving_measurement", //5
                    "food_calories", //6
                    "food_proteins", //7
                    "food_carbohydrates", //8
                    "food_fat"
            };
            String values[] = new String[]{
                    stringFoodNameSQL,
                    stringManufacturerNameSQL,
                    stringIngredients,
                    stringServingSizeSQL,
                    stringMeasurementSQL,
                    stringCaloriesPerPortionSQL,
                    stringProteinsPerPortionSQL,
                    stringCarbsPerPortionSQL,
                    stringFatPerPortionSQL
            };

            long longCurrentID = Long.parseLong(currentID);

            db.update("food", "_id", longCurrentID, fields, values);

            // Move user back to correct design
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.nav_host_fragment, new FoodsFragment(), FoodsFragment.class.getName()).commit();

            db.close();
        }
    }// buttonEditFoodSubmit

    /* logic for user deleting selected foods */
    public void deleteFood() {
        /* Database */
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        String currentID = listCursor.getString(0);
        long longID = Long.parseLong(currentID);

        // Delete
        db.delete("food", "_id", longID);

        // Close db
        db.close();

        // Give message
        Toast.makeText(getActivity(), "Food deleted", Toast.LENGTH_LONG).show();

        // Move user back to correct design
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.nav_host_fragment, new FoodsFragment(), FoodsFragment.class.getName()).commit();
    }// delete food


    /* logic for inputting new foods by user */
    public void addFoodButton() {
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        // Error ?
        int error = 0;

        // food name
        EditText editFoodName = (EditText) getActivity().findViewById(R.id.editTextFoodName);
        String stringFoodName = editFoodName.getText().toString();
        String stringFoodNameSQL = db.quoteSmart(stringFoodName);
        if (stringFoodName.equals("")) {
            Toast.makeText(getActivity(), "Food name cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        // food manufacturer
        EditText editManufacturerName = (EditText) getActivity().findViewById(R.id.editTextFoodManufacturer);
        String stringManufacturerName = editManufacturerName.getText().toString();
        String stringManufacturerNameSQL = db.quoteSmart(stringManufacturerName);
        if (stringFoodName.equals("")) {
            Toast.makeText(getActivity(), "Food manufacturer cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        // food ingredients
        EditText editIngredients = (EditText) getActivity().findViewById(R.id.editTextFoodIngredients);
        String stringIngredients = editIngredients.getText().toString();
        String stringIngredientsSQL = db.quoteSmart(stringIngredients);
        if (stringFoodName.equals("")) {
            Toast.makeText(getActivity(), "Ingredients cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        /* Serving Table */

        // food serving size
        EditText editServingSize = (EditText) getActivity().findViewById(R.id.EditFoodServingSize);
        String stringServingSize = editServingSize.getText().toString();
        String stringServingSizeSQL = db.quoteSmart(stringServingSize);
        double doubleServingSize = 0;
        if (stringServingSize.equals("")) {
            Toast.makeText(getActivity(), "Please fill in a size.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleServingSize = Double.parseDouble(stringServingSize);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Serving size is not number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }

        // food measurement
        EditText editServingMeasurement = (EditText) getActivity().findViewById(R.id.editTextServingMeasurement);
        String stringServingMeasurement = editServingMeasurement.getText().toString();
        String stringMeasurementSQL = db.quoteSmart(stringServingMeasurement);
        if (stringServingMeasurement.equals("")) {
            Toast.makeText(getActivity(), "Please fill in measurement.", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        /* Calories Table */

        // calories
        EditText editTextCaloriesPerPortion = (EditText) getActivity().findViewById(R.id.editTextFoodCaloriesPerPortion);
        String stringCaloriesPerPortion = editTextCaloriesPerPortion.getText().toString();
        stringCaloriesPerPortion = stringCaloriesPerPortion.replace(",", ".");
        double doubleCaloriesPerPortion = 0;
        if (stringCaloriesPerPortion.equals("")) {
            Toast.makeText(getActivity(), "Calories cannot be blank.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleCaloriesPerPortion = Double.parseDouble(stringCaloriesPerPortion);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Calories is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringCaloriesPerPortionSQL = db.quoteSmart(stringCaloriesPerPortion);

        // proteins
        EditText editFoodProteinsPerPortion = (EditText) getActivity().findViewById(R.id.editTextFoodProteinsPerPortion);
        String stringProteinsPerPortion = editFoodProteinsPerPortion.getText().toString();
        stringProteinsPerPortion = stringProteinsPerPortion.replace(",", ".");
        double doubleProteinsPerPortion = 0;
        if (stringProteinsPerPortion.equals("")) {
            Toast.makeText(getActivity(), "Proteins cannot be blank.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleProteinsPerPortion = Double.parseDouble(stringProteinsPerPortion);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Protein is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringProteinsPerPortionSQL = db.quoteSmart(stringProteinsPerPortion);

        // carbohydrates
        EditText editCarbohydratesPerPortion = (EditText) getActivity().findViewById(R.id.editTextFoodCarbsPerPortion);
        String stringCarbohydratesPerPortion = editCarbohydratesPerPortion.getText().toString();
        stringCarbohydratesPerPortion = stringCarbohydratesPerPortion.replace(",", ".");
        double doubleCarbohydratesPerPortion = 0;
        if (stringCarbohydratesPerPortion.equals("")) {
            Toast.makeText(getActivity(), "Please fill in carbohydrates.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleCarbohydratesPerPortion = Double.parseDouble(stringCarbohydratesPerPortion);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Carbohydrates is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringCarbsPerPortionSQL = db.quoteSmart(stringCarbohydratesPerPortion);

        // fat
        EditText editFatPerPortion = (EditText) getActivity().findViewById(R.id.editTextFoodFatPerPortion);
        String stringFatPerPortion = editFatPerPortion.getText().toString();
        stringFatPerPortion = stringFatPerPortion.replace(",", ".");
        double doubleFatPerPortion = 0;
        if (stringFatPerPortion.equals("")) {
            Toast.makeText(getActivity(), "Please fill in fat.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleFatPerPortion = Double.parseDouble(stringFatPerPortion);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getActivity(), "Carbohydrates is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringFatPerPortionSQL = db.quoteSmart(stringFatPerPortion);

        /* Update */
        if (error == 0) {
            //Insert into database
            String input = "NULL, " + stringFoodNameSQL + ", " + stringManufacturerNameSQL + ", " +
                    stringIngredientsSQL + ", " + stringServingSizeSQL + ", " + stringMeasurementSQL + ", "
                    + stringCaloriesPerPortionSQL + ", " + stringProteinsPerPortionSQL + ", " + stringCarbsPerPortionSQL
                    + ", " + stringFatPerPortionSQL;

            db.insert("food", "_id, food_name, food_manufacturer_name , food_ingredient," +
                    "food_serving_size, food_serving_measurement, food_calories, food_proteins, " +
                    "food_carbohydrates, food_fat", input);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.nav_host_fragment, new FoodsFragment(), FoodsFragment.class.getName())
                    .addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();


            UIUtil.hideKeyboard(getActivity());// Hide keyboard

            db.close();
        }
    }// add food button

    private void addFoodToDiary(int position) {

        // error ?
        int error = 0;

        // get portion size from edit text
        EditText portionSize = (EditText) getActivity().findViewById(R.id.editTextPortionSize);
        String stringPortionSize = portionSize.getText().toString();
        double doublePortionSize = 0;
        try {
            doublePortionSize = Double.parseDouble(stringPortionSize); // this is used to multiply calories, protein, carbs and fats to retrieve the intake for user
        } catch (NumberFormatException e) {
            error = 1;
            Toast.makeText(getActivity(), "Portion size is empty", Toast.LENGTH_SHORT).show();
        }
        if (stringPortionSize.equals(0)) {
            Toast.makeText(getActivity(), "Portion size cannot be 0", Toast.LENGTH_SHORT).show();
        }

        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        //Get Foods
        String[] fields = new String[]{
                "_id",
                "food_name",
                "food_manufacturer_name",
                "food_ingredient",
                "food_serving_size",
                "food_serving_measurement",
                "food_proteins",
                "food_carbohydrates",
                "food_fat",
                "food_calories"
        };
        listCursor.moveToPosition(position); // get position of selected item
        currentFoodID = listCursor.getString(0);// get _id
        listCursor = db.select("food", fields, "_id", currentFoodID);// set list cursor to food

        // get string from cursor
        String stringFoodName = listCursor.getString(1);
        String stringProtein = listCursor.getString(6);
        String stringCarbohydrates = listCursor.getString(7);
        String stringFat = listCursor.getString(8);
        String stringCalories = listCursor.getString(9);

        String stringFoodNameSQL = db.quoteSmart(stringFoodName);

        // date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // day
        String stringDay = "";
        if (day < 10) {
            stringDay = "0" + day; // adds 0 if day is less than 10 (dd/mm/yyyy)
        } else {
            stringDay = "" + day;
        }
        // month
        month = month + 1; // month start with 0
        String stringMonth = "";
        if (month < 10) {
            stringMonth = "0" + month; // adds 0 if month is less than 10 (dd/mm/yyyy)
        } else {
            stringMonth = "" + month;
        }
        String stringFdDate = year + "/" + stringMonth + "/" + stringDay;
        String stringFdDateSQL = db.quoteSmart(stringFdDate);

        /* calories */
        double doubleCalories = Double.parseDouble(stringCalories);

        // depending on how many portions get total calories
        double doubleCaloriesCalculated = Math.round((doublePortionSize * doubleCalories));

        /* proteins */
        double doubleProteins = Double.parseDouble(stringProtein);

        // depending on how many portions get total proteins
        double doubleProteinsCalculated = Math.round((doublePortionSize * doubleProteins));

        /* carbohydrates */
        double doubleCarbohydrates = Double.parseDouble(stringCarbohydrates);

        // depending on how many portions get total calories
        double doubleCarbohydratesCalculated = Math.round((doublePortionSize * doubleCarbohydrates));

        /* fats */
        double doubleFat = Double.parseDouble(stringFat);

        // depending on how many portions get total calories
        double doubleFatCalculated = Math.round((doublePortionSize * doubleFat));

        // insert into food diary sql
        if (error == 0) {
            String inputFields = "_id, fd_date, fd_food_name, fd_portion, fd_calories, fd_protein, fd_carbohydrates, fd_fat";

            String inputValues = "NULL, " + stringFdDateSQL + ", " + stringFoodNameSQL + ", " + stringPortionSize + ", "
                    + doubleCaloriesCalculated + ", " + doubleProteinsCalculated + ", "
                    + doubleCarbohydratesCalculated + ", " + doubleFatCalculated;

            db.insert("food_diary", inputFields, inputValues);
            Toast.makeText(getActivity(), "food diary updated!", Toast.LENGTH_SHORT).show();

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.nav_host_fragment, new HomeFragment(), HomeFragment.class.getName())
                    .addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
        db.close();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}