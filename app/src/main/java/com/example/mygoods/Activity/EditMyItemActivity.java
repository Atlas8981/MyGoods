package com.example.mygoods.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mygoods.Adapters.RecyclerHorizontalScrollAdapter;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.Model.AdditionalInfo;
import com.example.mygoods.Model.Car;
import com.example.mygoods.Model.Image;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.Phone;
import com.example.mygoods.Other.AddBottomSheetDialog;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EditMyItemActivity extends AppCompatActivity {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = firestore.collection("items");
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private Item mitem;
    private UploadTask uploadtask;

    // Variables
    private List<Bitmap> preImageBitmaps;
    private List<Bitmap> currentImageBitmaps;
    private Bundle bundle;

    // Views
    private EditText itemName;
    private EditText itemPrice;
    private EditText itemAddress;
    private EditText itemPhone;
    private EditText itemDescription;
    private TextView categorySelector;
    private TextView mainCategoryText;
    private Spinner mainCategorySpinner;
    private Spinner subCategorySpinner;
    private TextView remainingImages;

    private String mainCategory;
    private String subCategory;


    private List<Image> imagesUpload;

    int uploadNumber = 0;
    private RecyclerView recyclerView;
    private RecyclerHorizontalScrollAdapter recyclerAdapter;

    private LinearLayout brandLayout, conditionLayout, yearLayout, modelLayout, typeLayout;
    private TextView brandText, conditionText, yearText, modelText, typeText;
    private TextView brandSelector, yearSelector, modelSelector, typeSelector,conditionSelector;


    private AddBottomSheetDialog bottomSheets;

    private static List<Car> carList;
    private static List<String> bikePartsList;
    private static List<Phone> phoneList;
    private static List<String> motoTypeList;
    private static List<String> computerPartsList;

    private AdditionalInfo additionalInfo;

    private static final int REQUEST_CODE = 0;
    public static final int MAX_NUM_IMAGE = 5;

    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        if (carList==null
                || bikePartsList ==null
                || phoneList == null
                || motoTypeList == null
                || computerPartsList ==null) {
            getDataFromApi();
        }

        initializeUI();

        putDataIntoViews();

    }

    private void getDataFromApi() {
        carList = new ArrayList<>();
        computerPartsList = new ArrayList<>();
        phoneList = new ArrayList<>();
        motoTypeList = new ArrayList<>();
        bikePartsList = new ArrayList<>();

        getCarBrandApi();
        getMotobikeApi();
        getBikeApi();
        getComputerApi();
        getPhoneApi();
    }

    private void getCarBrandApi() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL("https://parseapi.back4app.com/classes/Car_Model_List?limit=9581&order=Make");
                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                    urlConnection.setRequestProperty("X-Parse-Application-Id", "hlhoNKjOvEhqzcVAJ1lxjicJLZNVv36GdbboZj3Z"); // This is the fake app's application id
                    urlConnection.setRequestProperty("X-Parse-Master-Key", "SNMJJF0CZZhTPhLDIqGhTlUNV9r60M2Z5spyWfXW"); // This is the fake app's readonly master key
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        JSONObject data = new JSONObject(stringBuilder.toString()); // Here you have the data that you need

                        carList.addAll(generateCarData(data));
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        })).start();
    }

    private void getMotobikeApi(){
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://api.trademe.co.nz/v1/Categories/MotorBikes.json");

                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        JSONObject data = new JSONObject(stringBuilder.toString()); // Here you have the data that you need
                        motoTypeList.addAll(generateMotoBikeData(data));

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        })).start();

    }

    private void getBikeApi(){

        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://api.trademe.co.nz/v1/Categories/0005.json");

                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        JSONObject data = new JSONObject(stringBuilder.toString()); // Here you have the data that you need

//                        System.out.println(data.toString(2));

                        bikePartsList.addAll(generateBikeData(data));

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        })).start();
    }

    private void getComputerApi(){
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://api.trademe.co.nz/v1/Categories/0002.json");

                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
//                    urlConnection.setRequestProperty("X-Parse-Application-Id", "MEqvn3N742oOXsF33z6BFeezRkW8zXXh4nIwOQUT"); // This is the fake app's application id
//                    urlConnection.setRequestProperty("X-Parse-Master-Key", "uZ1r1iHnOQr5K4WggIibVczBZSPpWfYbSRpD6INw"); // This is the fake app's readonly master key
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        JSONObject data = new JSONObject(stringBuilder.toString()); // Here you have the data that you need

//                        System.out.println(generateComputerData(data).toString(2));
                        computerPartsList.addAll(generateComputerData(data));
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        })).start();
    }

    private void getPhoneApi() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://parseapi.back4app.com/classes/Dataset_Cell_Phones_Model_Brand?count=1&limit=8634&order=Brand&keys=Brand,Model");
                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                    urlConnection.setRequestProperty("X-Parse-Application-Id", "MEqvn3N742oOXsF33z6BFeezRkW8zXXh4nIwOQUT"); // This is the fake app's application id
                    urlConnection.setRequestProperty("X-Parse-Master-Key", "uZ1r1iHnOQr5K4WggIibVczBZSPpWfYbSRpD6INw"); // This is the fake app's readonly master key
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        JSONObject data = new JSONObject(stringBuilder.toString()); // Here you have the data that you need

                        phoneList.addAll(generatePhoneData(data));

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        })).start();

    }

    private Set<Car> generateCarData(JSONObject thisData) {

        Set<Car> setCars =new HashSet<>();

//        Set<String> carBrands =new HashSet<>();

        //extracting data array from json string
        JSONArray ja_data = null;
        try {
            ja_data = thisData.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert ja_data != null;
        int length = ja_data.length();
        //loop to get all json objects from data json array
        for(int i=0; i<length; i++)
        {
            JSONObject jObj = null;
            try {
                jObj = ja_data.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                assert jObj != null;
//                carBrands.add(jObj.getString("Make"));

                Car tempCar = new Car(jObj.getString("Make"),
                        jObj.getString("Model"),
                        jObj.getString("Category"),
                        jObj.getString("Year"));
                setCars.add(tempCar);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return setCars;
    }

    private Set<String> generateMotoBikeData(JSONObject thisData) {

        Set<String> motoTypeSet = new HashSet<>();

        JSONObject jObj;
        //extracting data array from json string
        JSONArray ja_data = null;
        try {
            ja_data = thisData.getJSONArray("Subcategories");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert ja_data != null;
        int length = ja_data.length();
        //loop to get all json objects from data json array
        for(int i=0; i<length; i++)
        {
            try {
                jObj = ja_data.getJSONObject(i);
                assert jObj != null;
                motoTypeSet.add(jObj.getString("Name"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return motoTypeSet;
    }

    private Set<String> generateBikeData(JSONObject thisData) {

        Set<String> bikeSet = new HashSet<>();

        JSONObject jObj ;
        //extracting data array from json string
        JSONArray ja_data = null;
        try {
            ja_data = thisData.getJSONArray("Subcategories");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert ja_data != null;
        int length = ja_data.length();
        //loop to get all json objects from data json array
        for(int i=0; i<length; i++)
        {

            try {
                jObj = ja_data.getJSONObject(i);
                assert jObj != null;

                // getting inner array Ingredients
                JSONArray ja = jObj.getJSONArray("Subcategories");
                int len = ja.length();
//
                if (jObj.getString("Name").equalsIgnoreCase("Cycling")) {
                    // getting json objects from Ingredients json array
                    for (int j = 0; j < len; j++) {
                        JSONObject json = ja.getJSONObject(j);
//                        Here Where you get type of stuff that going to sell on the site in category of bike
                        bikeSet.add(json.getString("Name"));
//                        System.out.println(json.toString(2));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return bikeSet;
    }

    private Set<String> generateComputerData(JSONObject thisData) {
        Set<String > computerSet = new HashSet<>();

        JSONObject jObj;

        //extracting data array from json string
        JSONArray ja_data = null;
        try {
            ja_data = thisData.getJSONArray("Subcategories");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert ja_data != null;
        int length = ja_data.length();
        //loop to get all json objects from data json array
        for(int i=0; i<length; i++)
        {

            try {
                jObj = ja_data.getJSONObject(i);
                assert jObj != null;
//                System.out.println("Krav " + jObj.getString("Name"));

                // getting inner array Ingredients
                JSONArray ja = jObj.getJSONArray("Subcategories");
                int len = ja.length();

                if (jObj.getString("Name").equalsIgnoreCase("Components")) {
                    // getting json objects from Ingredients json array
                    for (int j = 0; j < len; j++) {
                        JSONObject json = ja.getJSONObject(j);
//                        System.out.println("Knong " + .replace("Other",""));
                        computerSet.add(json.getString("Name"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return computerSet;
    }

    private Set<Phone> generatePhoneData(JSONObject thisData) {

        Set<Phone> setPhone =new HashSet<>();


        //extracting data array from json string
        JSONArray ja_data = null;
        try {
            ja_data = thisData.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert ja_data != null;
        int length = ja_data.length();
        //loop to get all json objects from data json array
        for(int i=0; i<length; i++)
        {
            JSONObject jObj = null;
            try {
                jObj = ja_data.getJSONObject(i);

                assert jObj != null;
                setPhone.add(new Phone(jObj.getString("Brand"),jObj.getString("Model").replace("_","")));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return setPhone;

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confirm_menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.confirmPost) {
            if (checkView()) {
                progressDialog.setTitle("Updating...");
                progressDialog.show();
                if (preImageBitmaps.equals(currentImageBitmaps)) {
                    uploadDataToFirestore(mitem.getImages());
                } else {
                    new CompressAndUpload().execute(currentImageBitmaps.get(uploadNumber));

                }
            }

        }
        return super.onOptionsItemSelected(item);
    }



    private void choosePicture(){
        if (currentImageBitmaps.size()<5) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), REQUEST_CODE);
        }else {
            Toast.makeText(this, "You cannot add more than 5 pictures", Toast.LENGTH_SHORT).show();
        }
    }

//    The function to get the image from file
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){


            if (data.getClipData() != null) {
                // picked multiple images
                int count = data.getClipData().getItemCount();

    //                Check if count >= max number of image
                int check = count + currentImageBitmaps.size();

                if (count <= MAX_NUM_IMAGE && check <= MAX_NUM_IMAGE) {
                    for (int i = 0; i < count; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();

                        try {
                            Bitmap tempBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            Bitmap rotatedBitmap = checkOrientation(getApplicationContext(),uri,tempBitmap);
                            currentImageBitmaps.add(rotatedBitmap);
                            notifyImageNumberChange();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        NotifyData();
                    }
                } else {
                    Toast.makeText(this, "Cannot Select More than 5 pictures", Toast.LENGTH_SHORT).show();
                }

            } else {
                //Pick one image
                Uri uri = data.getData();
                try {
                    Bitmap tempBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Bitmap rotatedBitmap = checkOrientation(getApplicationContext(),uri,tempBitmap);
                    currentImageBitmaps.add(rotatedBitmap);
                    notifyImageNumberChange();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                NotifyData();
            }


        }else{
            Toast.makeText(this, "Cannot upload image, try choosing another one", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap checkOrientation (Context context, Uri uri, Bitmap bitmap){

        ExifInterface ei = null;
        try {
            InputStream imageStream = context.getContentResolver().openInputStream(uri);
            ei = new ExifInterface(imageStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;

        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    //    The first function or class to be call when update button is click
    private class CompressAndUpload extends AsyncTask<Bitmap, Integer, byte[]> {


        public CompressAndUpload(){
        }

        @Override
        protected byte[] doInBackground(Bitmap... bitmaps) {
//            progressDialog.setTitle("Compressing Image...");
//            Start Compressing Image in the back thread
            return getBytesFromBitmap(currentImageBitmaps.get(uploadNumber), 75);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            uploadImageToFirestore(bytes);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {}
    }

    private void uploadImageToFirestore (byte[] bytes){

        final String randomKey = UUID.randomUUID().toString();
        StorageReference storageRef = firebaseStorage.getReference().child("images/" +randomKey);

        UploadTask uploadTask = storageRef.putBytes(bytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get url from Firebase storage
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Image imageData = new Image(
                                uri.toString(),
                                randomKey);
                        imagesUpload.add(imageData);
                        uploadNumber++;

                        if (uploadNumber< currentImageBitmaps.size()){
                            new CompressAndUpload().execute(currentImageBitmaps.get(uploadNumber));
                        }else{
                            uploadNumber = 0;
                            if (progressDialog!=null) {
                                progressDialog.dismiss();
                            }
                            for (int i =0;i<mitem.getImages().size();i++) {
                                firebaseStorage.getReference().child("images/" + mitem.getImages().get(i).getImageName()).delete();
                            }
                            uploadDataToFirestore(imagesUpload);
                            Toast.makeText(EditMyItemActivity.this, "Image Update Successfully", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(EditMyItemActivity.this, "Upload Fail, TRY AGAIN", Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progressPercent = (100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                pd.setMessage("Percentage: " + (int) progressPercent + "%");
                progressDialog.setMessage("Uploaded Image(s) " + uploadNumber + " of " + currentImageBitmaps.size());
            }
        });

    }

    private void uploadDataToFirestore(List<Image> images){

        String itemid = mitem.getItemid();
        String name = itemName.getText().toString().trim();
        String addressString = itemAddress.getText().toString().trim();
        String descriptionString = itemDescription.getText().toString().trim();
        String phoneString = itemPhone.getText().toString().trim();
        double priceDouble = Double.parseDouble(itemPrice.getText().toString().trim());
        if (subCategory == null){
            subCategory = mitem.getSubCategory();
        }
        if (mainCategory == null){
            mainCategory = mitem.getMainCategory();
        }

        Item updatedItem = mitem;
        updatedItem.setName(name);
        updatedItem.setAddress(addressString);
        updatedItem.setDescription(descriptionString);
        updatedItem.setPhone(phoneString);
        updatedItem.setPrice(priceDouble);
        updatedItem.setMainCategory(mainCategory);
        updatedItem.setSubCategory(subCategory);
        updatedItem.setImages(images);

        itemRef.document(itemid)
                .set(updatedItem)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    additionalInfo.setCondition(conditionSelector.getText().toString().trim());
                    firestore.collection(Constant.itemCollection)
                            .document(mitem.getItemid())
                            .collection("additionInfo")
                            .document(subCategory)
                            .set(additionalInfo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    if (progressDialog!=null) {
                                        progressDialog.dismiss();
                                    }
                                    Toast.makeText(EditMyItemActivity.this, "Data Updated", Toast.LENGTH_SHORT).show();
//                                  When upload is successful launch the myitemAcitivity without user ability to come back
                                    launchActivityWithoutBack();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog!=null) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else {
                    if (progressDialog!=null) {
                        progressDialog.dismiss();
                    }
//                  Else stay in the same activity until everything workout
                    Toast.makeText(EditMyItemActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

//    Getting Byte from bitmap to covert
    public static byte[] getBytesFromBitmap (Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }

//    FindViewById in the layout and other necessary things
    private void initializeUI(){
        setTitle("Edit Item");
        progressDialog = new ProgressDialog(EditMyItemActivity.this);

        itemName = findViewById(R.id.itemNameTextField);
        itemPrice = findViewById(R.id.priceTextField);
        itemAddress = findViewById(R.id.addressTextField);
        itemPhone = findViewById(R.id.phoneTextField);
        itemDescription = findViewById(R.id.descriptionTextField);
        categorySelector = findViewById(R.id.categorySelector);
        mainCategoryText = findViewById(R.id.mainCategoryText);
        remainingImages = findViewById(R.id.remainingImages);

        categorySelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        imagesUpload = new ArrayList<>();
        recyclerView = findViewById(R.id.imageRecyclerView);
        preImageBitmaps = new ArrayList<>(0);
        currentImageBitmaps = new ArrayList<>(0);
        recyclerAdapter = new RecyclerHorizontalScrollAdapter(preImageBitmaps);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);

        recyclerAdapter.setOnItemClickListener(onItemClickListener);

        additionalInfo = new AdditionalInfo();

        brandLayout = findViewById(R.id.brandLayout);
        brandText = findViewById(R.id.brandText);
        brandSelector = findViewById(R.id.brandSelector);

        conditionLayout = findViewById(R.id.conditionLayout);
        conditionText = findViewById(R.id.conditionText);
        conditionSelector = findViewById(R.id.conditionSelector);

        yearLayout = findViewById(R.id.yearLayout);
        yearText = findViewById(R.id.yearText);
        yearSelector = findViewById(R.id.yearSelector);

        modelLayout = findViewById(R.id.modelLayout);
        modelText = findViewById(R.id.modelText);
        modelSelector = findViewById(R.id.modelSelector);

        typeLayout = findViewById(R.id.carTypeLayout);
        typeText = findViewById(R.id.carTypeText);
        typeSelector = findViewById(R.id.carTypeSelector);

        conditionText.setText("Condition");
        conditionSelector.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {

                List<String> conditions = new ArrayList<>();
                conditions.add("Used");
                conditions.add("New");

                AddBottomSheetDialog bottomSheet = new AddBottomSheetDialog(conditions);

                bottomSheet.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                    @Override
                    public void onItemClicked(String name) {
                        bottomSheet.dismiss();
                        conditionSelector.setError(null);
                        conditionSelector.setText(name);
                    }
                });
                bottomSheet.show(getSupportFragmentManager(), "AddBottomSheet");
            }
        });

        layoutGone();

    }

    private void layoutGone(){
        brandLayout.setVisibility(View.GONE);
        yearLayout.setVisibility(View.GONE);
        modelLayout.setVisibility(View.GONE);
        typeLayout.setVisibility(View.GONE);
    }

    private void carProcedure(){
        brandLayout.setVisibility(View.VISIBLE);
        brandText.setText("Car Information (Brand, Model, Type of Car, Year)");
        brandSelector.setText("(Enter Car Information)");

        yearLayout.setVisibility(View.GONE);
        yearText.setText(null);
        yearSelector.setText(null);

        modelLayout.setVisibility(View.GONE);
        modelText.setText(null);
        modelSelector.setText(null);

        typeLayout.setVisibility(View.GONE);
        typeText.setText(null);
        typeSelector.setText(null);

        brandSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Car tempCar = new Car();

                if (carList.size()!=0) {
                    Set<String> brandList = new HashSet<>();
                    for (Car c : carList) {
                        brandList.add(c.getBrand());
                    }
                    brandList.add("Other");
                    bottomSheets = new AddBottomSheetDialog(new ArrayList<>(brandList));

                    bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                        @Override
                        public void onItemClicked(String name) {
                            bottomSheets.dismiss();
                            tempCar.setBrand(name);
                            brandSelector.setText(tempCar.toString());
                            additionalInfo.setCar(tempCar);

                            Set<String> modelList = new HashSet<>();
                            for (Car c : carList) {
                                if (c.getBrand().equalsIgnoreCase(name)){
                                    modelList.add(c.getModel());
                                }
                            }
                            modelList.add("Other");

                            bottomSheets = new AddBottomSheetDialog(new ArrayList<>(modelList));

                            bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                                @Override
                                public void onItemClicked(String name) {
                                    bottomSheets.dismiss();
                                    tempCar.setModel(name);
                                    brandSelector.setText(tempCar.toString());
                                    additionalInfo.setCar(tempCar);

                                    Set<String> carTypeList = new HashSet<>();
                                    for (Car c : carList) {
                                        if (c.getModel().equals(name)){
                                            carTypeList.add(c.getCategory());
                                        }
                                    }
                                    carTypeList.add("Other");
                                    if (carTypeList.size()==1){
                                        for (Car c : carList) {
                                            carTypeList.add(c.getCategory());
                                        }
                                    }

                                    bottomSheets = new AddBottomSheetDialog(new ArrayList<>(carTypeList));

                                    bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                                        @Override
                                        public void onItemClicked(String name) {
                                            bottomSheets.dismiss();
                                            tempCar.setCategory(name);
                                            brandSelector.setText(tempCar.toString());
                                            additionalInfo.setCar(tempCar);

                                            Set<String> yearList = new HashSet<>();
                                            if (!name.equalsIgnoreCase("Other")) {
                                                for (Car c : carList) {
                                                    if (c.getModel().equalsIgnoreCase(tempCar.getModel())) {
                                                        yearList.add(c.getYear());
                                                    }
                                                }
                                            }
                                            yearList.add("Other");
                                            if (yearList.size()==1){
                                                for (Car c : carList) {
                                                    yearList.add(c.getYear());
                                                }
                                            }

                                            ArrayList<String> sortedList = new ArrayList<>(yearList);
                                            Collections.sort(sortedList);

                                            bottomSheets = new AddBottomSheetDialog(sortedList);

                                            bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                                                @Override
                                                public void onItemClicked(String name) {
                                                    bottomSheets.dismiss();
                                                    tempCar.setYear(name);
                                                    brandSelector.setText(tempCar.toString());

                                                    additionalInfo.setCar(tempCar);
                                                }
                                            });
                                            bottomSheets.show(getSupportFragmentManager(), "AddBottomSheetDialog");
                                        }
                                    });
                                    bottomSheets.show(getSupportFragmentManager(), "AddBottomSheetDialog");
                                }
                            });
                            bottomSheets.show(getSupportFragmentManager(), "AddBottomSheetDialog");

                        }
                    });
                    bottomSheets.show(getSupportFragmentManager(), "AddBottomSheetDialog");

                }else{
                    Toast.makeText(getApplicationContext(), "Please Wait A bit", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    //    Work for Phone, Tablet
    private void phoneProcedure(){

        brandLayout.setVisibility(View.VISIBLE);
        brandText.setText("Phone Detail (Brand, Model)");
        brandSelector.setText("(Enter Phone Detail)");


        yearLayout.setVisibility(View.GONE);
        yearText.setText(null);
        yearSelector.setText(null);

        modelLayout.setVisibility(View.GONE);
        modelText.setText(null);
        modelSelector.setText(null);

        typeLayout.setVisibility(View.GONE);
        typeText.setText(null);
        typeSelector.setText(null);

        brandSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Phone tempPhone = new Phone();

                Set<String> listPhoneBrand = new HashSet<>();
                for (Phone p: phoneList){
                    listPhoneBrand.add(p.getPhoneBrand());
                }
                bottomSheets = new AddBottomSheetDialog(new ArrayList<>(listPhoneBrand));

                bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                    @Override
                    public void onItemClicked(String name) {
                        bottomSheets.dismiss();
                        tempPhone.setPhoneBrand(name);
                        brandSelector.setError(null);
                        brandSelector.setText(tempPhone.getPhoneBrand());
                        additionalInfo.setPhone(tempPhone);


                        Set<String> listPhoneModel = new HashSet<>();
                        for (Phone p: phoneList){
                            if (p.getPhoneBrand().equalsIgnoreCase(name)){
                                listPhoneModel.add(p.getPhoneModel());
                            }
                        }
                        listPhoneModel.add("Other");

                        bottomSheets = new AddBottomSheetDialog(new ArrayList<>(listPhoneModel));

                        bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                            @Override
                            public void onItemClicked(String name) {
                                bottomSheets.dismiss();
                                tempPhone.setPhoneModel(name);
                                brandSelector.setError(null);
                                brandSelector.setText(tempPhone.getPhoneBrand() + ", " + tempPhone.getPhoneModel());
                                additionalInfo.setPhone(tempPhone);
                            }
                        });
                        assert getFragmentManager() != null;
                        bottomSheets.show(getSupportFragmentManager(), "AddBottomSheetDialog");
                    }
                });
                assert getFragmentManager() != null;
                bottomSheets.show(getSupportFragmentManager(), "AddBottomSheetDialog");
            }
        });


    }

    //    For Electronic Part and accessories
    private void partAccessoriesComputerProcedure(){

        brandLayout.setVisibility(View.GONE);
        brandText.setText(null);
        brandSelector.setText(null);


        yearLayout.setVisibility(View.GONE);
        yearText.setText(null);
        yearSelector.setText(null);

        modelLayout.setVisibility(View.GONE);
        modelText.setText(null);
        modelSelector.setText(null);

        typeLayout.setVisibility(View.VISIBLE);
        typeText.setText("Type of Item");
        typeSelector.setText("(Select Type of Item)");

        typeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheets = new AddBottomSheetDialog(new ArrayList<>(computerPartsList));

                bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                    @Override
                    public void onItemClicked(String name) {
                        bottomSheets.dismiss();
                        typeSelector.setText(name);

                        additionalInfo.setComputerParts(name);
                    }
                });
                bottomSheets.show(getSupportFragmentManager(), "AddBottomSheet");
            }
        });

    }

    private void bikeProcedure(){


        brandLayout.setVisibility(View.GONE);
        brandText.setText(null);
        brandSelector.setText(null);


        yearLayout.setVisibility(View.GONE);
        yearText.setText(null);
        yearSelector.setText(null);

        modelLayout.setVisibility(View.GONE);
        modelText.setText(null);
        modelSelector.setText(null);

        typeLayout.setVisibility(View.VISIBLE);
        typeText.setText("Type of Bike Item");
        typeSelector.setText("(Select Type of Bike Item)");

        typeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheets = new AddBottomSheetDialog(new ArrayList<>(bikePartsList));

                bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                    @Override
                    public void onItemClicked(String name) {
                        bottomSheets.dismiss();
                        typeSelector.setText(name);

                        additionalInfo.setBikeType(name);
                    }
                });
                bottomSheets.show(getSupportFragmentManager(), "AddBottomSheet");
            }
        });
    }

    private void motoProcedure(){

        brandLayout.setVisibility(View.GONE);
        brandText.setText(null);
        brandSelector.setText(null);


        yearLayout.setVisibility(View.GONE);
        yearText.setText(null);
        yearSelector.setText(null);

        modelLayout.setVisibility(View.GONE);
        modelText.setText(null);
        modelSelector.setText(null);

        typeLayout.setVisibility(View.VISIBLE);
        typeText.setText("Type of Moto Item");
        typeSelector.setText("(Select Type of Moto Item)");

        typeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheets = new AddBottomSheetDialog(new ArrayList<>(motoTypeList));

                bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                    @Override
                    public void onItemClicked(String name) {
                        bottomSheets.dismiss();
                        typeSelector.setText(name);

                        additionalInfo.setMotoType(name);
                    }
                });
                bottomSheets.show(getSupportFragmentManager(), "AddBottomSheet");
            }
        });
    }

    RecyclerHorizontalScrollAdapter.OnItemClickListener onItemClickListener = new RecyclerHorizontalScrollAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {

        }

        @Override
        public void onMinusSignClick(int position) {
            NotifyData();
            currentImageBitmaps.remove(position);
            notifyImageNumberChange();

        }

        @Override
        public void onPlusSignClick(int position) {
            choosePicture();
        }

    };

    private void NotifyData(){
        recyclerAdapter = new RecyclerHorizontalScrollAdapter(currentImageBitmaps);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnItemClickListener(onItemClickListener);
    }

    private void openDialog() {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.category_dialog_box,null);


        setUpSpinner(view);

        builder.setView(view)
                .setTitle("Select Category")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mainCategory = mainCategorySpinner.getSelectedItem().toString();
                        subCategory = subCategorySpinner.getSelectedItem().toString();

                        mainCategoryText.setText("Category: " + mainCategory);
                        categorySelector.setText(subCategory);

                        additionalInfo = new AdditionalInfo();

                        if (subCategory.equalsIgnoreCase("cars")){
                            carProcedure();
                        }else if (subCategory.equalsIgnoreCase("phone")){
                            phoneProcedure();
                        }else if (subCategory.toLowerCase().contains("parts")){
                            partAccessoriesComputerProcedure();
                        }else if (subCategory.equalsIgnoreCase("Bicycle".toLowerCase())){
                            bikeProcedure();
                        }else if (subCategory.toLowerCase().contains("moto")){
                            motoProcedure();
                        }else{
                            layoutGone();
                        }

                        brandSelector.setError(null);
                        yearSelector.setError(null);
                        modelSelector.setError(null);
                        typeSelector.setError(null);
//                        listener.applyTexts(cat);
                    }
                });

        builder.show();
    }

    private void setUpSpinner(View view){
        mainCategorySpinner = view.findViewById(R.id.categorySpinner);
        subCategorySpinner = view.findViewById(R.id.subCategorySpinner);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainCategorySpinner.setAdapter(spinnerAdapter);

//        ArrayAdapter<CharSequence> subSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.electronic, android.R.layout.simple_spinner_item);
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        subCategorySpinner.setAdapter(subSpinnerAdapter);



        mainCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayAdapter<CharSequence> subSpinnerAdapter;

                if (mainCategorySpinner.getSelectedItem().toString().toLowerCase().contains("electronic")) {
                    subSpinnerAdapter = ArrayAdapter.createFromResource(EditMyItemActivity.this, R.array.electronic, android.R.layout.simple_spinner_item);
                } else if (mainCategorySpinner.getSelectedItem().toString().toLowerCase().contains("vehicle")) {
                    subSpinnerAdapter = ArrayAdapter.createFromResource(EditMyItemActivity.this, R.array.vehicle, android.R.layout.simple_spinner_item);
                }else{
                    subSpinnerAdapter = ArrayAdapter.createFromResource(EditMyItemActivity.this, R.array.furiture, android.R.layout.simple_spinner_item);
                }
                subSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCategorySpinner.setAdapter(subSpinnerAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (mitem.getSubCategory()!=null && mitem.getMainCategory() !=null) {
            for (int i = 0; i < mainCategorySpinner.getCount(); i++) {
                if (mainCategorySpinner.getItemAtPosition(i).toString().equalsIgnoreCase(mitem.getMainCategory())) {
                    mainCategorySpinner.setSelection(i);
                    break;
                }
            }
        }


    }

//    Data from Detail Activity will be place accordingly to each views
    private void putDataIntoViews(){
        bundle = getIntent().getExtras();
        mitem = new Item();
        if (bundle != null) {
            mitem = (Item) bundle.get("myitem");

            itemName.setText(mitem.getName());

            if (mitem.getSubCategory()!=null && mitem.getMainCategory() !=null) {
                mainCategoryText.setText("Category : " + mitem.getMainCategory());
                categorySelector.setText(mitem.getSubCategory());
                subCategory = mitem.getSubCategory();
                mainCategory = mitem.getMainCategory();

                if (subCategory.equalsIgnoreCase("cars")){
                    carProcedure();
                }else if (subCategory.equalsIgnoreCase("phone")){
                    phoneProcedure();
                }else if (subCategory.toLowerCase().contains("parts")){
                    partAccessoriesComputerProcedure();
                }else if (subCategory.equalsIgnoreCase("Bicycle".toLowerCase())){
                    bikeProcedure();
                }else if (subCategory.toLowerCase().contains("moto")){
                    motoProcedure();
                }else{
                    layoutGone();
                }
            }

            itemPrice.setText(String.valueOf(mitem.getPrice()));
            itemAddress.setText(mitem.getAddress());
            itemDescription.setText(mitem.getDescription());
            itemPhone.setText(mitem.getPhone());

            recyclerAdapter = new RecyclerHorizontalScrollAdapter(currentImageBitmaps);
            recyclerView.setAdapter(recyclerAdapter);
            recyclerAdapter.setOnItemClickListener(onItemClickListener);
            for (int i = 0; i<mitem.getImages().size();i++) {
                Glide.with(this)
                        .load(mitem.getImages().get(i).getImageURL())
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                preImageBitmaps.add(((BitmapDrawable) resource).getBitmap());
                                currentImageBitmaps = new ArrayList<>(preImageBitmaps);
                                NotifyData();
                                notifyImageNumberChange();
                            }
                        });
            }
            getAdditionalInfo(mitem);
        }
    }

    private void getAdditionalInfo(Item i) {
        firestore.collection("items")
                .document(i.getItemid())
                .collection("additionInfo")
                .document(i.getSubCategory())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                AdditionalInfo tempAdditionalInfo = documentSnapshot.toObject(AdditionalInfo.class);

                if (tempAdditionalInfo != null) {
                    conditionSelector.setText(tempAdditionalInfo.getCondition());
                    if (tempAdditionalInfo.getBikeType()!=null){
                        typeSelector.setText(tempAdditionalInfo.getBikeType());
                    }else if (tempAdditionalInfo.getCar()!=null){
                        brandSelector.setText(tempAdditionalInfo.getCar().toString());

                    }else if (tempAdditionalInfo.getComputerParts()!=null){
                        typeSelector.setText(tempAdditionalInfo.getComputerParts());

                    }else if (tempAdditionalInfo.getPhone()!=null){
                        brandSelector.setText(tempAdditionalInfo.getPhone().getPhoneBrand()
                                + ", " +tempAdditionalInfo.getPhone().getPhoneModel() );

                    }else if (tempAdditionalInfo.getMotoType()!=null){
                        typeSelector.setText(tempAdditionalInfo.getMotoType());

                    }
                    additionalInfo = tempAdditionalInfo;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyImageNumberChange(){
        remainingImages.setText("(" + currentImageBitmaps.size() + "/5)");
    }



//    Launch Activity that user cannot go back
    private void launchActivityWithoutBack(){
        Intent intent = new Intent(EditMyItemActivity.this, MyItemActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

//    A simple but long function body just to check if the views are all corrected and no bad data
    private boolean checkView (){
        String errorMsg = "Here";
        boolean flag = true;

        if (itemName.getText().toString().isEmpty()){
            itemName.setError(errorMsg);
            flag = false;
        }else{
            itemName.setError(null);
        }
        if (!itemPrice.getText().toString().isEmpty()) {
            try {
                Double.parseDouble(itemPrice.getText().toString());
                itemPrice.setError(null);
            } catch (NumberFormatException e) {
                itemPrice.setError("Number Format Error");
                flag = false;
            }
        }else {
            itemPrice.setError(errorMsg);
            flag = false;
        }

        if (itemAddress.getText().toString().isEmpty()){
            itemAddress.setError(errorMsg);
            flag = false;
        }else{
            itemAddress.setError(null);
        }
        if (itemPhone.getText().toString().isEmpty()){
            itemPhone.setError(errorMsg);
            flag = false;
        }else{
            itemPhone.setError(null);
        }
        if (itemDescription.getText().toString().isEmpty()){
            itemDescription.setError(errorMsg);
            flag = false;
        }else if (itemDescription.getLineCount()>20){
            itemDescription.setError("Description Too Long");
            flag = false;
        }else{
            itemDescription.setError(null);
        }
        if (currentImageBitmaps.size()==0){
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
            flag = false;
        }

        if (conditionSelector.getText().toString().equalsIgnoreCase("")
                || conditionSelector.getText().toString().equalsIgnoreCase("(Condition)")
                || conditionSelector.getText().toString().isEmpty()){
            conditionSelector.setError("Please select Condition");
            flag = false;
        }else {
            conditionSelector.setError(null);
        }

        if (subCategory == null || mainCategory ==null){
            Toast.makeText(this, "Please Choose a Category", Toast.LENGTH_SHORT).show();
            flag = false;
        }else {
            if (subCategory.equalsIgnoreCase("cars")) {
                if (additionalInfo.getCar() == null) {
                    brandSelector.setError("Please Enter Car Detail");
                    flag = false;
                } else {
                    if (additionalInfo.getCar().getBrand() == null
                            | additionalInfo.getCar().getModel() == null
                            | additionalInfo.getCar().getCategory() == null
                            | additionalInfo.getCar().getYear() == null) {
                        brandSelector.setError("Not Enough Information");
                        flag = false;
                    } else {
                        brandSelector.setError(null);
                    }

                }
            } else if (subCategory.equalsIgnoreCase("phone")) {
                if (additionalInfo.getPhone() == null) {
                    brandSelector.setError("Please Enter Phone Detail");
                    flag = false;
                } else {
                    if (additionalInfo.getPhone().getPhoneBrand() == null
                            | additionalInfo.getPhone().getPhoneModel() == null) {
                        brandSelector.setError("Not Enough Information");
                        flag = false;
                    } else {
                        brandSelector.setError(null);
                    }
                }
            } else if (subCategory.toLowerCase().contains("parts")) {
                if (additionalInfo.getComputerParts() == null) {
                    typeSelector.setError("Please Select Type of Item");
                    flag = false;
                } else {
                    typeSelector.setError(null);
                }
            } else if (subCategory.equalsIgnoreCase("Bicycle".toLowerCase())) {
                if (additionalInfo.getBikeType() == null) {
                    typeSelector.setError("Please Select Type of Item");
                    flag = false;
                } else {
                    typeSelector.setError(null);
                }
            } else if (subCategory.toLowerCase().contains("moto")) {
                if (additionalInfo.getMotoType() == null) {
                    typeSelector.setError("Please Select Type of Item");
                    flag = false;
                } else {
                    typeSelector.setError(null);
                }
            }
        }

        return flag;
    }
}