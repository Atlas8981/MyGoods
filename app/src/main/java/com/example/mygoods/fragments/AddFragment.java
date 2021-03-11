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
import com.example.mygoods.Model.Image;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.User;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private final DocumentReference ref = db.collection(Constant.itemCollection).document();

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
        }else if (description.getLineCount()>10){
            description.setError("Description Too Long");
            flag = false;
        }else{
            description.setError(null);
        }
        if (imagesUpload==null){
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if (subCategory == null || mainCategory ==null){
            Toast.makeText(getContext(), "Please Choose a Category", Toast.LENGTH_SHORT).show();
            flag = false;
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

