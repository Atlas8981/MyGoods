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
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mygoods.Adapters.RecyclerHorizontalScrollAdapter;
import com.example.mygoods.Model.Image;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditMyItemActivity extends AppCompatActivity {
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = firestore.collection("items");
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
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

    private static final int REQUEST_CODE = 0;
    public static final int MAX_NUM_IMAGE = 5;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        initializeUI();

        putDataIntoViews();

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

        itemRef.document(itemid).set(updatedItem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                        Dismiss the dialog box if or if not the data is uploaded because we want user to be control of what happen
                progressDialog.dismiss();

                if (task.isSuccessful()){
                    Toast.makeText(EditMyItemActivity.this, "Data Updated", Toast.LENGTH_SHORT).show();
//                  When upload is successful launch the myitemAcitivity without user ability to come back
                    launchActivityWithoutBack();
                }else {
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

        }

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
        }else if (itemDescription.getLineCount()>10){
            itemDescription.setError("Description Too Long");
            flag = false;
        }else{
            itemDescription.setError(null);
        }
        if (imagesUpload==null){
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if (subCategory == null || mainCategory ==null){
            Toast.makeText(this, "Please Choose a Category", Toast.LENGTH_SHORT).show();
            flag = false;
        }

        return flag;
    }
}