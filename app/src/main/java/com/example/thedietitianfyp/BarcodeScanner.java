package com.example.thedietitianfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.squareup.picasso.Picasso;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BarcodeScanner extends AppCompatActivity {
    CameraView cameraView;
    boolean isDetected = false;
    Button btnScan;
    String openFood = "https://world.openfoodfacts.org/api/v0/product/";
    private RequestQueue mQueue;
    private View mainView;

    FirebaseVisionBarcodeDetectorOptions options;
    FirebaseVisionBarcodeDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        mQueue = Volley.newRequestQueue(this);

        Dexter.withActivity(this)
                .withPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        setupCamera();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();
    }

    private void setupCamera() {
        btnScan = (Button) findViewById(R.id.scan_again);
        btnScan.setEnabled(isDetected);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDetected = !isDetected;
            }
        });
        cameraView = (CameraView) findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                processImage(getVisionImageFromFrame(frame));
            }
        });

        options = new FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                .build();
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
    }

    private void processImage(FirebaseVisionImage image) {
        if (!isDetected) {
            detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                @Override
                public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                    processResult(firebaseVisionBarcodes);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BarcodeScanner.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void processResult(final List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        if (firebaseVisionBarcodes.size() > 0) {
            isDetected = true;
            btnScan.setEnabled(isDetected);
            btnScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (FirebaseVisionBarcode item : firebaseVisionBarcodes) {
                        int valueType = item.getValueType();
                        switch (valueType) {
                            case FirebaseVisionBarcode.TYPE_TEXT:
                            case FirebaseVisionBarcode.TYPE_PRODUCT: {
                                String findFood = "https://world.openfoodfacts.org/api/v0/product/" + item.getRawValue() + ".json";
                                jsonParse(findFood);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    private void createDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private FirebaseVisionImage getVisionImageFromFrame(Frame frame) {
        byte[] data = frame.getData();
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setHeight(frame.getSize().getHeight())
                .setWidth(frame.getSize().getWidth())
                //.setRotation(frame.getRotation())
                .build();
        return FirebaseVisionImage.fromByteArray(data, metadata);
    }

    /* API (open food facts) */
    public void jsonParse(String url) {
        final DBAdapter db = new DBAdapter(this);
        db.open();
        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject product = response.getJSONObject("product");
                            JSONObject nutriments = product.getJSONObject("nutriments");
                            //System.out.println(nutriments);
                            //System.out.println(product);

                            String productName = null;
                            String brandName;
                            String ingredients;
                            int calories;
                            int proteins;
                            int carbohydrates;
                            int fat;
                            int servingQuantity;
                            String servingSize;
                            String image;

                            if (product.getString("product_name") == null) {
                                productName = "";
                            } else {
                                productName = product.getString("product_name");
                                //System.out.println("foodName: " + productName);
                            }

                            if (product.getString("brands") == null) {
                                brandName = "";
                            } else {
                                brandName = product.getString("brands");
                                //System.out.println("brand: " + brandName);
                            }

                            if (product.getString("ingredients_text") == null) {
                                ingredients = "";
                            } else {
                                ingredients = product.getString("ingredients_text");
                                //System.out.println("ingredients: " + ingredients);
                            }

                            if (product.getString("serving_size") == null) {
                                servingSize = "";
                            } else {
                                servingSize = product.getString("serving_size");
                                //System.out.println("brand: " + servingSize);
                            }

                            if (nutriments.getInt("energy-kcal_value") == 0) {
                                calories = 0;
                            } else {
                                calories = nutriments.getInt("energy-kcal_value");
                                //System.out.println("calories: " + calories);
                            }

                            proteins = nutriments.getInt("proteins_value");
                            //System.out.println("protiens: " + proteins);

                            carbohydrates = nutriments.getInt("carbohydrates_value");
                            //System.out.println("carbs: " + carbohydrates);

                            fat = nutriments.getInt("fat");
                            //System.out.println("fats:" + fat);

                            servingQuantity = product.getInt("serving_quantity");
                            //System.out.println("servingQuantity" + servingQuantity);


                            if (product.getString("image_front_small_url") == null) {
                                image = "";
                            } else {
                                image = product.getString("image_front_small_url");
                            }

                            String productNameSQL = db.quoteSmart(productName);
                            String brandNameSQL = db.quoteSmart(brandName);
                            String ingredientsSQL = db.quoteSmart(ingredients);
                            String servingSizeSQL = db.quoteSmart(servingSize);
                            String imageSQL = db.quoteSmart(image);

                            //Insert into database
                            String input = "NULL, " + productNameSQL + "," + calories + "," + proteins + ","
                                    + fat + "," + carbohydrates + "," + brandNameSQL + ","
                                    + ingredientsSQL + "," + servingSizeSQL + "," + servingQuantity
                                    + "," + imageSQL;

                            db.insert("food", "_id, food_name, food_calories," +
                                    "food_proteins, food_fat, food_carbohydrates," +
                                    "food_manufacturer_name, food_ingredient, food_serving_measurement, " +
                                    "food_serving_size, food_image", input);

                            //Get food details
                            String[] fields = new String[]{
                                    "_id",
                                    "food_name",
                                    "food_calories",
                                    "food_proteins",
                                    "food_fat",
                                    "food_carbohydrates",
                                    "food_manufacturer_name",
                                    "food_ingredient",
                                    "food_serving_size",
                                    "food_serving_measurement",
                                    "food_image"
                            };

                            Cursor listCursor;
                            listCursor = db.select("food", fields, "", "", "food_name", "ASC");


                            setContentView(R.layout.fragment_food_edit);

                            // food name
                            EditText editTextFoodName = (EditText) findViewById(R.id.editTextFoodName);
                            editTextFoodName.setText(productNameSQL);

                            // food manufacturer
                            EditText editTextBrandName = (EditText) findViewById(R.id.editTextFoodManufacturer);
                            editTextBrandName.setText(brandNameSQL);

                            // food ingredients
                            EditText editTextIngredients = (EditText) findViewById(R.id.editTextFoodIngredients);
                            editTextIngredients.setText(ingredientsSQL);

                            // food calories (per hundred grams)
                            EditText editTextCalories = (EditText) findViewById(R.id.editTextFoodCaloriesPerPortion);
                            editTextCalories.setText(String.valueOf(calories));

                            // food protein (per hundred grams)
                            EditText editTextProtein = (EditText) findViewById(R.id.editTextFoodProteinsPerPortion);
                            editTextProtein.setText(String.valueOf(proteins));

                            // food carbohydrates (per hundred grams)
                            EditText editTextCarbs = (EditText) findViewById(R.id.editTextFoodCarbsPerPortion);
                            editTextCarbs.setText(String.valueOf(carbohydrates));

                            // food fat (per hundred grams)
                            EditText editTextFats = (EditText) findViewById(R.id.editTextFoodFatPerPortion);
                            editTextFats.setText(String.valueOf(fat));

                            // food serving size
                            EditText editTextServingSize = (EditText) findViewById(R.id.EditFoodServingSize);
                            editTextServingSize.setText(String.valueOf(servingQuantity));

                            // food measurement type
                            EditText editTextMeasurementType = (EditText) findViewById(R.id.editTextServingMeasurement);
                            editTextMeasurementType.setText(String.valueOf(servingSizeSQL));


                            /* Edit Button listener */
                            Button buttonEditFood = (Button) findViewById(R.id.buttonSaveEditFood);
                            buttonEditFood.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    buttonEditFoodSubmitOnClick();
                                    Intent i = new Intent(BarcodeScanner.this, MainActivity.class);
                                    startActivity(i);
                                }
                            });

                            db.close();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    public void buttonEditFoodSubmitOnClick() {

        Cursor c;

        DBAdapter db = new DBAdapter(this);
        db.open();

        // Error ?
        int error = 0;

        //Get Foods
        String[] field = new String[]{
                "_id",// 0
                "food_name",  //1
                "food_manufacturer_name", //2
                "food_ingredient", //3
                "food_serving_size", //4
                "food_serving_measurement", //5
                "food_calories", //6
                "food_proteins", //7
                "food_carbohydrates", //8
                "food_fat", // 9
                "food_image",
        };

        c = db.select("food", field);
        String currentID = c.getString(0);
        long longID = Long.parseLong(currentID);

        // food name
        EditText editFoodName = (EditText) findViewById(R.id.editTextFoodName);
        String stringFoodName = editFoodName.getText().toString();
        String stringFoodNameSQL = db.quoteSmart(stringFoodName);
        if (stringFoodName.equals("")) {
            Toast.makeText(this, "Food name cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        // food manufacturer
        EditText editManufacturerName = (EditText) findViewById(R.id.editTextFoodManufacturer);
        String stringManufacturerName = editManufacturerName.getText().toString();
        String stringManufacturerNameSQL = db.quoteSmart(stringManufacturerName);
        if (stringFoodName.equals("")) {
            Toast.makeText(this, "Food manufacturer cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        // food ingredients
        EditText editIngredients = (EditText) findViewById(R.id.editTextFoodIngredients);
        String stringIngredients = editIngredients.getText().toString();
        String stringIngredientsSQL = db.quoteSmart(stringIngredients);
        if (stringFoodName.equals("")) {
            Toast.makeText(this, "Ingredients cannot be blank", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        /* Serving Table */

        // food serving size
        EditText editServingSize = (EditText) findViewById(R.id.EditFoodServingSize);
        String stringServingSize = editServingSize.getText().toString();
        String stringServingSizeSQL = db.quoteSmart(stringServingSize);
        double doubleServingSize = 0;
        if (stringServingSize.equals("")) {
            Toast.makeText(this, "Please fill in a size.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleServingSize = Double.parseDouble(stringServingSize);
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Serving size is not number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }

        // food measurement
        EditText editServingMeasurement = (EditText) findViewById(R.id.editTextServingMeasurement);
        String stringServingMeasurement = editServingMeasurement.getText().toString();
        String stringMeasurementSQL = db.quoteSmart(stringServingMeasurement);
        if (stringServingMeasurement.equals("")) {
            Toast.makeText(this, "Please fill in measurement.", Toast.LENGTH_SHORT).show();
            error = 1;
        }

        /* Calories Table */

        // calories
        EditText editTextCaloriesPerHundred = (EditText) findViewById(R.id.editTextFoodCaloriesPerPortion);
        String stringCaloriesPerHundred = editTextCaloriesPerHundred.getText().toString();
        stringCaloriesPerHundred = stringCaloriesPerHundred.replace(",", ".");
        double doubleCaloriesPerHundred = 0;
        if (stringCaloriesPerHundred.equals("")) {
            Toast.makeText(this, "Calories cannot be blank.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleCaloriesPerHundred = Double.parseDouble(stringCaloriesPerHundred);
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Calories is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringCaloriesPerHundredSQL = db.quoteSmart(stringCaloriesPerHundred);

        // proteins
        EditText editFoodProteinsPerHundred = (EditText) findViewById(R.id.editTextFoodProteinsPerPortion);
        String stringProteinsPerHundred = editFoodProteinsPerHundred.getText().toString();
        stringProteinsPerHundred = stringProteinsPerHundred.replace(",", ".");
        double doubleProteinsPerHundred = 0;
        if (stringProteinsPerHundred.equals("")) {
            Toast.makeText(this, "Proteins cannot be blank.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleProteinsPerHundred = Double.parseDouble(stringProteinsPerHundred);
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Protein is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringProteinsPerHundredSQL = db.quoteSmart(stringProteinsPerHundred);

        // carbohydrates
        EditText editCarbohydratesPerHundred = (EditText) findViewById(R.id.editTextFoodCarbsPerPortion);
        String stringCarbohydratesPerHundred = editCarbohydratesPerHundred.getText().toString();
        stringCarbohydratesPerHundred = stringCarbohydratesPerHundred.replace(",", ".");
        double doubleCarbohydratesPerHundred = 0;
        if (stringCarbohydratesPerHundred.equals("")) {
            Toast.makeText(this, "Please fill in carbohydrates.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleCarbohydratesPerHundred = Double.parseDouble(stringCarbohydratesPerHundred);
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Carbohydrates is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringCarbsPerHundredSQL = db.quoteSmart(stringCarbohydratesPerHundred);

        // fat
        EditText editFatPerHundred = (EditText) findViewById(R.id.editTextFoodFatPerPortion);
        String stringFatPerHundred = editFatPerHundred.getText().toString();
        stringFatPerHundred = stringFatPerHundred.replace(",", ".");
        double doubleFatPerHundred = 0;
        if (stringFatPerHundred.equals("")) {
            Toast.makeText(this, "Please fill in fat.", Toast.LENGTH_SHORT).show();
            error = 1;
        } else {
            try {
                doubleFatPerHundred = Double.parseDouble(stringFatPerHundred);
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Carbohydrates is not a number.", Toast.LENGTH_SHORT).show();
                error = 1;
            }
        }
        String stringFatPerHundredSQL = db.quoteSmart(stringFatPerHundred);

        /* Update */
        if (error == 0) {

            /* Calories table pr meal */
            String fields[] = new String[]{
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
            String values[] = new String[]{
                    stringFoodNameSQL,
                    stringManufacturerNameSQL,
                    stringIngredients,
                    stringServingSizeSQL,
                    stringMeasurementSQL,
                    stringCaloriesPerHundredSQL,
                    stringProteinsPerHundredSQL,
                    stringCarbsPerHundredSQL,
                    stringFatPerHundredSQL,
            };
            UIUtil.hideKeyboard(this); // hide keyboard (for searching)

            db.close();
        }
    }
}
