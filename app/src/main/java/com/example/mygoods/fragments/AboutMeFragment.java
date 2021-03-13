package com.example.mygoods.fragments;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mygoods.Activity.AboutOurAppActivity;
import com.example.mygoods.Activity.EditProfileActivity;
import com.example.mygoods.Activity.FullScreenImageActivity;
import com.example.mygoods.Activity.HomeActivity;
import com.example.mygoods.Activity.MyItemActivity;
import com.example.mygoods.Activity.MySaveItemActivity;
import com.example.mygoods.Activity.TermAndConditionActivity;
import com.example.mygoods.Adapters.RecyclerCategoryItemAdapter;
import com.example.mygoods.Firewall.ForgotPasswordActivity;
import com.example.mygoods.Firewall.SignUp.PersonalInformationActivity;
import com.example.mygoods.Model.Image;
import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class AboutMeFragment extends Fragment {

    private ImageView myImage;
    private TextView myName, myPhone,myAddress;
    private Button signOutBtn;
    private ProgressBar imageProgress;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference userInfoRef = firestore.collection("users");

    private Button myItemBtn;
    private Button mySaveItemBtn;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private static User currentUser;

    private static final int placeHolder = R.drawable.account;

    public AboutMeFragment() {/*Required empty public constructor*/}

    private View view;
    private Context context;

    private ProgressDialog progressDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (auth.getCurrentUser()!=null){
//            if (!auth.getCurrentUser().isAnonymous()) {
//                setHasOptionsMenu(true);
//            }
//        }
        setHasOptionsMenu(false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about_me, container, false);
        this.view = view;

        myImage = view.findViewById(R.id.myImage);
        myName = view.findViewById(R.id.myName);
        myPhone = view.findViewById(R.id.myPhone);
        myAddress = view.findViewById(R.id.myAddress);
        signOutBtn = view.findViewById(R.id.signOutBtn);
        imageProgress = view.findViewById(R.id.imageProgress);

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(getContext());

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Are you sure you want to sign out ?");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.setTitle("Signing Out...");
                        progressDialog.show();

                        auth.signOut();

//                // Create new fragment and transaction
//                FragmentManager fragmentManager = getFragmentManager();
//                assert fragmentManager != null;
//                FragmentTransaction transaction = fragmentManager.beginTransaction();
//                transaction.setReorderingAllowed(true);
//
//                // Replace whatever is in the fragment_container view with this fragment
//                transaction.replace(R.id.fragment_container, HomeFragment.class, null);
//
//                // Commit the transaction
//                transaction.commit();
                        Intent intent = new Intent();
                        intent.setClass(getContext(), HomeActivity.class);
                        startActivity(intent);

                        Toast.makeText(getContext(), "Sign Out Successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();

            }
        });

//        Reset views to make it nothing in case default have something
        resetViews();

//        Load user data from internal storage for faster loading
//        check if loadFunction is null before put it in
        if (loadCurrentUser() != null){
            currentUser = loadCurrentUser();
        }


//        Put old data in if user is not change
        putOldDataIntoView(view);


//        For Menu Items at the bottom
        settingUpListMenu(view);

        myImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                choosePicture();
                return true;
            }
        });

        myImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<Image> justOneImagae = new ArrayList<>();
                justOneImagae.add(currentUser.getImage());

                Intent intent = new Intent(getContext(), FullScreenImageActivity.class);
                intent.putExtra("images", (Serializable) justOneImagae);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        Update and Save Data if user change and
        getDataFromDatabase();

    }

    private void getDataFromDatabase(){

        if (auth.getUid() != null){
            userInfoRef.document(auth.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User tempUser = documentSnapshot.toObject(User.class);


                    if (tempUser != null ) {
                        if (currentUser==null || !currentUser.equals(tempUser) || !currentUser.getUserId().equals(auth.getUid())){
                            currentUser = new User(tempUser);

                            if (tempUser.getImage() == null || tempUser.getImage().getImageURL() == null){
                                Uri uri=Uri.parse("R.drawable.account");
                                tempUser.setImage(new Image("defaultImage",uri.toString()));
                            }

                            Glide.with(view)
                                    .load(tempUser.getImage().getImageURL())
                                    .placeholder(placeHolder)
                                    .into(myImage);

                            myName.setText(tempUser.getFirstname() + " " + tempUser.getLastname());
                            myPhone.setText(tempUser.getPhoneNumber());
                            myAddress.setText(tempUser.getAddress());

                            saveCurrentUser(tempUser);

                            setHasOptionsMenu(true);
                        }
                    }else{

//                        currentUser = new User();
//                        saveCurrentUser(null);


//                        currentUser = new User();
//                        if (auth.getCurrentUser() != null ){
//                            if (auth.getCurrentUser().getEmail()!= null){
//                                currentUser.setEmail(auth.getCurrentUser().getEmail());
//                                currentUser.setUserId(auth.getUid());
//                            }
//                        }
                        Intent intent = new Intent();
                        intent.setClass(getContext(), PersonalInformationActivity.class);
                        startActivity(intent);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            myImage.setImageResource(0);
            myName.setText("");
            myPhone.setText("");
            myAddress.setText("");
        }
    }

    public static final int REQUEST_CODE = 1969;

    private void choosePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_CODE);

    }

    private Bitmap userChosenProfileInBitmap;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            if (data != null) {
                //Pick one image
                Uri uri = data.getData();
                try {
                    Bitmap tempBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    Bitmap rotatedBitmap = checkOrientation(getContext(), uri, tempBitmap);
                    userChosenProfileInBitmap = rotatedBitmap;

                    if (userChosenProfileInBitmap != null) {
                        updateProfilePicture(userChosenProfileInBitmap);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }else{
            Toast.makeText(getContext(), "No Image Chosen", Toast.LENGTH_SHORT).show();
        }


    }

    private void updateProfilePicture(Bitmap bitmap) {
        imageProgress.setVisibility(View.VISIBLE);
        new CompressAndUpload().execute(bitmap);
    }

    private class CompressAndUpload extends AsyncTask<Bitmap, Integer, byte[]> {


        public CompressAndUpload(){
        }

        @Override
        protected byte[] doInBackground(Bitmap... bitmaps) {
//            progressDialog.setTitle("Compressing Image...");
//            Start Compressing Image in the back thread
            return getBytesFromBitmap(userChosenProfileInBitmap, 25);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            uploadImageToStorage(bytes);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }
    }

    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private void uploadImageToStorage(byte[] bytes) {
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


                        if (currentUser.getImage() != null && currentUser.getImage().getImageURL() !=null) {
                            firebaseStorage.getReference().child("images/" + currentUser.getImage().getImageName()).delete();
                        }


                        updateDataToFirestore(imageData);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        });
    }

    private void updateDataToFirestore(Image newImageData) {
        if (currentUser != null && auth.getUid() !=null) {
            currentUser.setImage(newImageData);
            userInfoRef.document(auth.getUid()).set(currentUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    saveCurrentUser(currentUser);
                    if (getContext() != null) {
                        Glide.with(getContext())
                                .load(getBytesFromBitmap(userChosenProfileInBitmap, 25))
                                .placeholder(R.drawable.account)
                                .into(myImage);
                        Toast.makeText(getContext(), "Image Update Successfully", Toast.LENGTH_LONG).show();
                        imageProgress.setVisibility(View.INVISIBLE);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static byte[] getBytesFromBitmap (Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
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



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.about_me_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editProfile: {
                Intent intent = new Intent();
                intent.setClass(getContext(), EditProfileActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
            break;
            case R.id.resetPassword: {
                Intent intent = new Intent();
                intent.setClass(getContext(), ForgotPasswordActivity.class);
                if (currentUser != null && currentUser.getEmail() != null) {
                    intent.putExtra("email", currentUser.getEmail());
                }
                startActivity(intent);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String[] arrMenusName;
    private int[] arrMenusImage;
    private RecyclerView menuRecyclerView;

    private void settingUpListMenu(View view) {
        arrMenusName = getResources().getStringArray(R.array.aboutMe_Menu);

        arrMenusImage = new int[]{
                R.drawable.myitem,
                R.drawable.saved,
                R.drawable.aboutus,
                R.drawable.term
        };

        menuRecyclerView = view.findViewById(R.id.aboutMeRecyclerView);
        menuRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerCategoryItemAdapter recyclerCategoryItemAdapter = new RecyclerCategoryItemAdapter(arrMenusName,arrMenusImage);

        menuRecyclerView.setLayoutManager(layoutManager);
        menuRecyclerView.setAdapter(recyclerCategoryItemAdapter);

        recyclerCategoryItemAdapter.setOnItemClickListener(new RecyclerCategoryItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                switch (position){
                    case 0:{
                        Intent intent = new Intent();
                        intent.setClass(getContext(), MyItemActivity.class);
                        startActivity(intent);
                    }break;
                    case 1:{
                        Intent intent = new Intent();
                        intent.setClass(getContext(), MySaveItemActivity.class);
                        startActivity(intent);
                    }break;
                    case 2:{
                        Intent intent = new Intent();
                        intent.setClass(getContext(), AboutOurAppActivity.class);
                        startActivity(intent);
                    } break;
                    case 3:{
                        Intent intent = new Intent();
                        intent.setClass(getContext(), TermAndConditionActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void putOldDataIntoView(View view) {

        if (currentUser != null) {
            if (currentUser.getUserId() != null){
                if (currentUser.getUserId().equals(auth.getUid())){
                    if (currentUser.getImage() != null && currentUser.getImage().getImageURL() != null) {
                        Glide.with(view)
                                .load(currentUser.getImage().getImageURL())
                                .placeholder(placeHolder)
                                .into(myImage);
                    }else{
                        Glide.with(view)
                                .load(R.drawable.account)
                                .placeholder(placeHolder)
                                .into(myImage);
                    }

                    myName.setText(currentUser.getFirstname() + " " + currentUser.getLastname());
                    myPhone.setText(currentUser.getPhoneNumber());
                    myAddress.setText(currentUser.getAddress());
                    setHasOptionsMenu(true);
                }

            }

        }
    }

    private void resetViews() {
        myImage.setImageResource(0);
        myName.setText("");
        myPhone.setText("");
        myAddress.setText("");
    }

    public static final String FILE_NAME = "currentUser.ser";

    private void saveCurrentUser(User user){

        try {

            FileOutputStream fileOutputStream =  context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(user);

            objectOutputStream.close();
            fileOutputStream.close();

            System.out.println("Save Data into " + FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private User loadCurrentUser(){
        User userData = null;
        try {
            FileInputStream fileInputStream = getContext().openFileInput(FILE_NAME);

            ObjectInputStream ois = new ObjectInputStream(fileInputStream);

            User tempUser = (User) ois.readObject();

            if (tempUser !=null) {
                userData = new User(tempUser);
            }

            ois.close();
            fileInputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


        return userData;
    }


    private void settingUpButton (View view){
        myItemBtn = view.findViewById(R.id.myItemBtn);
        mySaveItemBtn = view.findViewById(R.id.saveItemBtn);

        myItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getContext(), MyItemActivity.class);
                startActivity(intent);
            }
        });

        mySaveItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getContext(), MySaveItemActivity.class);
                startActivity(intent);
            }
        });
    }
}