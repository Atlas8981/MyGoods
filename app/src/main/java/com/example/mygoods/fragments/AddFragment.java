package com.example.mygoods.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygoods.Adapters.RecyclerHorizontalScrollAdapter;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.Firewall.SignUp.PersonalInformationActivity;
import com.example.mygoods.Model.AdditionalInfo;
import com.example.mygoods.Model.Car;
import com.example.mygoods.Model.Image;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.Phone;
import com.example.mygoods.Model.User;
import com.example.mygoods.Other.AddBottomSheetDialog;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class AddFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Firebase


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    // Variables

    private List<Bitmap> imageBitmap;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private DocumentReference ref;

    // Views
    private View v;
    private Context addFragmentContext;
    private EditText itemName;
    private String subCategory;
    private String mainCategory;
    private EditText amount;
    private EditText price;
    private EditText address;
    private EditText phone;
    private EditText description;
    private TextView categorySelector;
    private TextView mainCategoryText;
    private TextView remainingImages;

    private ArrayList<Image> imagesUpload;

    int uploadNumber = 0;
    private ProgressDialog pd;
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
    public AddFragment() {

    }

    public static AddFragment newInstance(String param1, String param2) {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }


    //****************
    // onCreate
    //****************
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (carList==null
                || bikePartsList ==null
                || phoneList == null
                || motoTypeList == null
                || computerPartsList ==null) {
            getDataFromApi();
        }

    }

    //****************
    // onAttach
    //****************
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        addFragmentContext = context;
    }

    //****************
    // onCreateView
    //****************
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.activity_edit_item, container, false);

        // Setup item
        inflateViews();

        amount.setKeyListener(DigitsKeyListener.getInstance(true,true));
        price.setKeyListener(DigitsKeyListener.getInstance(true,true));

        initializeVariable();

        autoCompleteField();

        return v;
    } // End of onCreateView()

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
//                    URL url = new URL("https://parseapi.back4app.com/classes/Dataset_Cell_Phones_Model_Brand?count=1&limit=8634&order=Brand&keys=Brand,Model");
                    String where = URLEncoder.encode("{" +
                            "    \"Brand\": {" +
                            "        \"$exists\": true" +
                            "    }," +
                            "    \"Model\": {" +
                            "        \"$exists\": true" +
                            "    }," +
                            "    \"Display_resolution\": {" +
                            "        \"$exists\": true" +
                            "    }" +
                            "}", "utf-8");
                    URL url = new URL("https://parseapi.back4app.com/classes/Dataset_Cell_Phones_Model_Brand?count=1&limit=7418&order=Brand&keys=Brand,Model,Display_resolution&where=" + where);
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

//                        for (Phone p : generatePhoneData(data)){
//                            System.out.println(p.getPhoneBrand());
//                        }
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

                String mystring = jObj.getString("Display_resolution");
                String[] arr = mystring.split(" ", 2);
                String firstWord = arr[0];
                String theRest = arr[1];
                double screenSize = 0;
                try {
                    screenSize = Double.parseDouble(firstWord);
                }catch (NumberFormatException e){

                }


                if (screenSize > 1.8 &&screenSize < 7.0){

                    setPhone.add(new Phone(jObj.getString("Brand"),
                            jObj.getString("Model").replace("_","")));
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (Phone p: setPhone){
            System.out.println(p.getPhoneBrand());
        }

        return setPhone;

    }

    private void autoCompleteField() {
        if (auth.getUid()!=null) {
            db.collection("users")
                    .document(auth.getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        if (currentUser.getAddress() != null) {
                            address.setText(currentUser.getAddress());
                        }

                        if (currentUser.getPhoneNumber() != null) {
                            phone.setText(currentUser.getPhoneNumber());
                        }
                    }else{
                        Intent intent = new Intent();
                        intent.setClass(getContext(), PersonalInformationActivity.class);
                        startActivity(intent);
                    }

                }
            });
        }
    }


    private void initializeVariable() {
        firebaseStorage = FirebaseStorage.getInstance();

        imagesUpload = new ArrayList<>();
        recyclerView = v.findViewById(R.id.imageRecyclerView);
        imageBitmap = new ArrayList<>();
        recyclerAdapter = new RecyclerHorizontalScrollAdapter(imageBitmap);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);

        recyclerAdapter.setOnItemClickListener(onItemClickListener);

        additionalInfo = new AdditionalInfo();
    }
    RecyclerHorizontalScrollAdapter.OnItemClickListener onItemClickListener = new RecyclerHorizontalScrollAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {

        }

        @Override
        public void onMinusSignClick(int position) {
            NotifyData();
            imageBitmap.remove(position);
            notifyImageNumberChange();
        }

        @Override
        public void onPlusSignClick(int position) {
            choosePicture();
        }

    };

    private void NotifyData(){
        recyclerAdapter = new RecyclerHorizontalScrollAdapter(imageBitmap);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnItemClickListener(onItemClickListener);
    }

    //****************
    // METHOD USE TO NAVIGATE TO GALLERY
    //****************
    private void choosePicture(){
        if (imageBitmap.size()<MAX_NUM_IMAGE) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), REQUEST_CODE);
        }else {
            Toast.makeText(addFragmentContext, "You cannot add more than 5 pictures", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getClipData() != null) {
                // picked multiple images
                int count = data.getClipData().getItemCount();



//                Check if count >= max number of image
                int check = count + imageBitmap.size();

                if (count <= MAX_NUM_IMAGE && check <= MAX_NUM_IMAGE) {
                    for (int i = 0; i < count; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();

                        try {
                            Bitmap tempBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                            Bitmap rotatedBitmap = checkOrientation(getContext(),uri,tempBitmap);
                            imageBitmap.add(rotatedBitmap);
                            notifyImageNumberChange();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        NotifyData();
                    }
                } else {
                    Toast.makeText(addFragmentContext, "Cannot Select More than 5 pictures", Toast.LENGTH_SHORT).show();
                }

            } else {
                //Pick one image
                Uri uri = data.getData();
                try {
                    Bitmap tempBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    Bitmap rotatedBitmap = checkOrientation(getContext(),uri,tempBitmap);
                    imageBitmap.add(rotatedBitmap);
                    notifyImageNumberChange();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                NotifyData();
            }


        }else{
            Toast.makeText(addFragmentContext, "Cannot upload image, try choosing another one", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap checkOrientation (Context context,Uri uri,Bitmap bitmap){

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


    //****************
    // METHOD USE FOR UPLOAD DATA EXCEPT IMAGE URL
    //****************
    private void uploadData(){

        Item item = getItem();
        db.collection("items").document(item.getItemid()).set(item).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                additionalInfo.setCondition(conditionSelector.getText().toString().trim());
                ref.collection("additionInfo")
                        .document(subCategory)
                        .set(additionalInfo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(addFragmentContext, "Item Uploaded", Toast.LENGTH_SHORT).show();
                                if (pd!=null) {
                                    pd.dismiss();
                                }
                                clearDataAfterUpload();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pd!=null) {
                            pd.dismiss();
                        }
                        Toast.makeText(addFragmentContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (pd!=null) {
                    pd.dismiss();
                }
                Toast.makeText(addFragmentContext, "Failed Item Uploaded", Toast.LENGTH_SHORT).show();
            }
        });


        // Add a new document with a generated ID
//        db.collection("items").add(item).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//            @Override
//            public void onSuccess(DocumentReference documentReference) {
//                Toast.makeText(addFragmentContext, "Item Uploaded", Toast.LENGTH_SHORT).show();
//                if (pd!=null) {
//                    pd.dismiss();
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                if (pd!=null) {
//                    pd.dismiss();
//                }
//                Toast.makeText(addFragmentContext, "Failed Item Uploaded", Toast.LENGTH_SHORT).show();
//            }
//        });

    }

    private Item getItem() {

        Date date = new Date();

        Item item = new Item(
                itemName.getText().toString().trim(),
                address.getText().toString().trim(),
                imagesUpload,
                subCategory,
                mainCategory,
                description.getText().toString().trim(),
                auth.getUid(),
                phone.getText().toString().trim(),
                Double.parseDouble(price.getText().toString().trim()),
                date
        );
        ref = db.collection(Constant.itemCollection).document();
        item.setItemid(ref.getId());

        return item;
    }

    //****************
    // METHOD USE FOR UPLOAD DATA WITH IMAGEURL
    //****************
    private void uploadDataAndPicture(byte[] bytes){

        final String randomKey = UUID.randomUUID().toString();
        storageRef = firebaseStorage.getReference().child("images/" +randomKey);

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

                        if (uploadNumber<imageBitmap.size()){
                            new BackgroundImageResize().execute(imageBitmap.get(uploadNumber));
                        }else{
                            uploadNumber = 0;
                            uploadData();
                            Toast.makeText(addFragmentContext, "Image Upload Successfully", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(addFragmentContext, "Upload Fail, TRY AGAIN", Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progressPercent = (100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                pd.setMessage("Percentage: " + (int) progressPercent + "%");
                pd.setMessage("Uploaded Image(s) " + uploadNumber + " of " + imageBitmap.size());
            }
        });
    }

    //****************
    // METHOD USE FOR CONVERTING BITMAP TO BYTES
    //****************
    private static byte[] getBytesFromBitmap (Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);

        return stream.toByteArray();
    }

    //****************
    // METHOD USE TO PERFORM TASK AT THE BACKGROUND
    //****************
    private class BackgroundImageResize extends AsyncTask<Bitmap, Integer, byte[]> {

        public BackgroundImageResize() {

        }

        @Override
        protected byte[] doInBackground(Bitmap... bitmaps) {
            return getBytesFromBitmap(imageBitmap.get(uploadNumber), 25);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            uploadDataAndPicture(bytes);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.confirm_menu_item,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.confirmPost: {
                if (checkView()) {
                    pd = new ProgressDialog(addFragmentContext);
                    pd.setTitle("Uploading...");
                    pd.setCancelable(false);
                    pd.show();
                    new BackgroundImageResize().execute(imageBitmap.get(uploadNumber));
                }

            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkView (){
        String errorMsg = "Here";
        boolean flag = true;

        if (itemName.getText().toString().isEmpty()){
            itemName.setError(errorMsg);
            flag = false;
        }else{
            itemName.setError(null);
        }
        if (!price.getText().toString().isEmpty()) {
            try {
                Double.parseDouble(price.getText().toString());
                price.setError(null);
            } catch (NumberFormatException e) {
                price.setError("Number Format Error");
                flag = false;
            }
        }else {
            price.setError(errorMsg);
            flag = false;
        }

        if (address.getText().toString().isEmpty()){
            address.setError(errorMsg);
            flag = false;
        }else{
            address.setError(null);
        }
        if (phone.getText().toString().isEmpty()){
            phone.setError(errorMsg);
            flag = false;
        }else{
            phone.setError(null);
        }
        if (description.getText().toString().isEmpty()){
            description.setError(errorMsg);
            flag = false;
        }else if (description.getLineCount()>20){
            description.setError("Description Too Long");
            flag = false;
        }else{
            description.setError(null);
        }
        if (imageBitmap.size() == 0){
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "Please Choose a Category", Toast.LENGTH_SHORT).show();
            flag = false;
        }else{
            if (subCategory.equalsIgnoreCase("cars")){
                if (additionalInfo.getCar() == null){
                    brandSelector.setError("Please Enter Car Detail");
                    flag = false;
                }else{
                    if (additionalInfo.getCar().getBrand() == null
                            | additionalInfo.getCar().getModel() == null
                            | additionalInfo.getCar().getCategory() == null
                            | additionalInfo.getCar().getYear() == null){
                        brandSelector.setError("Not Enough Information");
                        flag=false;
                    }else{
                        brandSelector.setError(null);
                    }

                }
            }else if (subCategory.equalsIgnoreCase("phone")){
                if (additionalInfo.getPhone() ==null){
                    brandSelector.setError("Please Enter Phone Detail");
                    flag = false;
                }else{
                    if (additionalInfo.getPhone().getPhoneBrand() == null
                            | additionalInfo.getPhone().getPhoneModel() == null ){
                        brandSelector.setError("Not Enough Information");
                        flag=false;
                    }else {
                        brandSelector.setError(null);
                    }
                }
            }else if (subCategory.toLowerCase().contains("parts")){
                if (additionalInfo.getComputerParts() == null){
                    typeSelector.setError("Please Select Type of Item");
                    flag = false;
                }else {
                    typeSelector.setError(null);
                }
            }else if (subCategory.equalsIgnoreCase("Bicycle".toLowerCase())){
                if (additionalInfo.getBikeType() == null){
                    typeSelector.setError("Please Select Type of Item");
                    flag = false;
                }else {
                    typeSelector.setError(null);
                }
            }else if (subCategory.toLowerCase().contains("moto")){
                if (additionalInfo.getMotoType() == null){
                    typeSelector.setError("Please Select Type of Item");
                    flag = false;
                }else {
                    typeSelector.setError(null);
                }
            }
        }

        return flag;
    }


    //****************
    // METHOD USE FOR INFLATING VIEWS
    //****************
    private void inflateViews(){
        itemName = (EditText)v.findViewById(R.id.itemNameTextField);
        amount = (EditText)v.findViewById(R.id.amountTextField);
        price = (EditText)v.findViewById(R.id.priceTextField);
        address = (EditText)v.findViewById(R.id.addressTextField);
        phone = (EditText)v.findViewById(R.id.phoneTextField);
        description = (EditText)v.findViewById(R.id.descriptionTextField);
        categorySelector = v.findViewById(R.id.categorySelector);
        mainCategoryText = v.findViewById(R.id.mainCategoryText);
        remainingImages = v.findViewById(R.id.remainingImages);

        remainingImages.setText("(0/5)");

        categorySelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        brandLayout = v.findViewById(R.id.brandLayout);
        brandText = v.findViewById(R.id.brandText);
        brandSelector = v.findViewById(R.id.brandSelector);

        conditionLayout = v.findViewById(R.id.conditionLayout);
        conditionText = v.findViewById(R.id.conditionText);
        conditionSelector = v.findViewById(R.id.conditionSelector);

        yearLayout = v.findViewById(R.id.yearLayout);
        yearText = v.findViewById(R.id.yearText);
        yearSelector = v.findViewById(R.id.yearSelector);

        modelLayout = v.findViewById(R.id.modelLayout);
        modelText = v.findViewById(R.id.modelText);
        modelSelector = v.findViewById(R.id.modelSelector);

        typeLayout = v.findViewById(R.id.carTypeLayout);
        typeText = v.findViewById(R.id.carTypeText);
        typeSelector = v.findViewById(R.id.carTypeSelector);

        conditionText.setText("Condition");
        conditionSelector.setOnClickListener(new View.OnClickListener() {
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
                bottomSheet.show(getFragmentManager(),getTag());
            }
        });

        layoutGone();
    }

    private void layoutGone(){
        brandLayout.setVisibility(View.GONE);
        yearLayout.setVisibility(View.GONE);
        modelLayout.setVisibility(View.GONE);
        typeLayout.setVisibility(View.GONE);

        brandSelector.setError(null);
        yearSelector.setError(null);
        modelSelector.setError(null);
        typeSelector.setError(null);
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
                    bottomSheets = new AddBottomSheetDialog(sortSetString(brandList));

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

                            bottomSheets = new AddBottomSheetDialog(sortSetString(modelList));

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

                                    bottomSheets = new AddBottomSheetDialog(sortSetString(carTypeList));

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



                                            bottomSheets = new AddBottomSheetDialog(sortSetString(yearList));

                                            bottomSheets.setOnItemBottomSheetListener(new AddBottomSheetDialog.onItemBottomSheetListener() {
                                                @Override
                                                public void onItemClicked(String name) {
                                                    bottomSheets.dismiss();
                                                    tempCar.setYear(name);
                                                    brandSelector.setText(tempCar.toString());

                                                    additionalInfo.setCar(tempCar);
                                                }
                                            });
                                            bottomSheets.show(getFragmentManager(), getTag());
                                        }
                                    });
                                    bottomSheets.show(getFragmentManager(), getTag());
                                }
                            });
                            bottomSheets.show(getFragmentManager(), getTag());

                        }
                    });
                    bottomSheets.show(getFragmentManager(), getTag());

                }else{
                    Toast.makeText(addFragmentContext, "Please Wait A bit", Toast.LENGTH_SHORT).show();
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

                Set<String> listPhoneBrand= new HashSet<>();
                for (Phone p: phoneList){
                    listPhoneBrand.add(p.getPhoneBrand());
                }



                bottomSheets = new AddBottomSheetDialog(sortSetString(listPhoneBrand));

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

                        bottomSheets = new AddBottomSheetDialog(sortSetString(listPhoneModel));

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
                        bottomSheets.show(getFragmentManager(),getTag());
                    }
                });
                assert getFragmentManager() != null;
                bottomSheets.show(getFragmentManager(),getTag());
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
                bottomSheets.show(getFragmentManager(),getTag());
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
                bottomSheets.show(getFragmentManager(),getTag());
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
                bottomSheets.show(getFragmentManager(),getTag());
            }
        });
    }

    private ArrayList<String> sortSetString (Set<String> unsortedSet){
        ArrayList<String> sortedList = new ArrayList<>(unsortedSet);
        Collections.sort(sortedList);
        return sortedList;
    }
    private Spinner mainCategorySpinner;
    private Spinner subCategorySpinner;

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
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

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.categories, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainCategorySpinner.setAdapter(spinnerAdapter);

        ArrayAdapter<CharSequence> subSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.electronic, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subCategorySpinner.setAdapter(subSpinnerAdapter);

        mainCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayAdapter<CharSequence> subSpinnerAdapter;

                if (mainCategorySpinner.getSelectedItem().toString().toLowerCase().contains("electronic")) {
                    subSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.electronic, android.R.layout.simple_spinner_item);
                } else if (mainCategorySpinner.getSelectedItem().toString().toLowerCase().contains("vehicle")) {
                    subSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.vehicle, android.R.layout.simple_spinner_item);
                }else{
                    subSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.furiture, android.R.layout.simple_spinner_item);
                }
                subSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCategorySpinner.setAdapter(subSpinnerAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }


    //****************
    // DETACH FRAGMENT
    //****************
    @Override
    public void onDetach() {
        super.onDetach();
        addFragmentContext = null;
    }

    private void clearDataAfterUpload(){
        itemName.setText(null);
        amount.setText(null);
        price.setText(null);
        address.setText(null);
        phone.setText(null);
        description.setText(null);
        mainCategoryText.setText("Category");
        remainingImages.setText("(0/5)");
        categorySelector.setText("(Select Category)");

        layoutGone();

        conditionSelector.setText("(Condition)");

        subCategory = null;
        mainCategory = null;

        autoCompleteField();

        initializeVariable();

    }

    private void notifyImageNumberChange(){
        remainingImages.setText("(" +
                imageBitmap.size() +
                "/5)");
    }

}

