package bruno.varela.tavares.mygram.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.home.HomeActivity;
import bruno.varela.tavares.mygram.models.Photo;
import bruno.varela.tavares.mygram.models.User;
import bruno.varela.tavares.mygram.models.UserAccountSettings;
import bruno.varela.tavares.mygram.models.UserSettings;
import bruno.varela.tavares.mygram.profile.AccountSettingsActivity;

import static bruno.varela.tavares.mygram.R.id.username;

/**
 * Created by Bruno on 15/08/2017.
 */

public class FirebaseMethods {

    //
    private static final String TAG = "FirebaseMethods";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    //vars
    private Context mContext;
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }


    public void uploadNewPhoto(String photoType, final String caption, final int count, final String imgUrl, Bitmap bm){
        Log.d(TAG, "uploadNewPhoto:  tenta upload uma nova foto");

        FilePaths filePaths = new FilePaths();
        //caso 1 new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto:  uploading new foto");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count +1));

            if (bm == null){
                // Convert Image url to bitMap
                bm = ImageManager.getBitmap(imgUrl);

            }

            //Bimap em byte[]
            byte [] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            //Method do Firebase
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUri = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, R.string.sucess_message_up_photo, Toast.LENGTH_LONG).show();

                    //adicionar nova foto para photos node e user_photos node
                    addPhotoToDatabase(caption, firebaseUri.toString());

                    // Navegar para a Home page para ver a foto que o user carregou
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload falhou ");
                    Toast.makeText(mContext, R.string.fail_upload_photo, Toast.LENGTH_LONG).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    //Como se fosse para aparecer de 15 em 15
                    if (progress - 15 > mPhotoUploadProgress){
                        // Da numros inteiros %.0f
                        Toast.makeText(mContext, mContext.getString(R.string.upload_photo_process) + String.format("%.0f",progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress:  upload processo "  + progress + "% feito");
                }
            });

            //INSERIR FOTO DO PROFIL
        }else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto:  uploading new PROFILE photo");

            Log.d(TAG, "uploadNewPhoto:  uploading new foto");



            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            if (bm == null){
                // Convert Image url to bitMap
                bm = ImageManager.getBitmap(imgUrl);

            }
            byte [] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUri = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, "Upload da foto foi com sucesso! ", Toast.LENGTH_LONG).show();

                    //Inserir no user_accounts_settings node
                    setProfilePhoto(firebaseUri.toString());

                    //Meter viewPager ir para edit profile fragment
                    ((AccountSettingsActivity)mContext).setUpViewPager(((AccountSettingsActivity)mContext).pagerAdapter
                            .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment)));

                    //Meter viewPager ir para edit profile fragment
                    ((AccountSettingsActivity)mContext).setUpViewPager(((AccountSettingsActivity)mContext).pagerAdapter
                            .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment)));




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload falhou ");
                    Toast.makeText(mContext, "Upload da Foto falhou", Toast.LENGTH_LONG).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f",progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress:  upload processo "  + progress + "% feito");
                }
            });


        }

    }


    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto:  settings new profile image");

        //Inserir uma nova foto no profile
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }



    private void addPhotoToDatabase(String caption, String url){

        String tags = StringManipulation.getTags(caption);
        //ID da Foto
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimestamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);


        //Inserir na Base de dados
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);



    }

    public String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        return  sdf.format(new Date());
    }





    public int getImageCount(DataSnapshot  dataSnapshot){
        int count = 0;
        for (DataSnapshot ds : dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }
                return count;
    }





    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber) {

        Log.d(TAG, "updateUserAccountSettings:  actulal");

        if (displayName != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);

        }

        if (website != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);

        }

        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);

        }

        if (phoneNumber != 0) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);

        }

    }


    /**
     * Actualizar para Username para os Users Node(dbname_users) e dbname_user_account_settings node
     * @param username
     */
    public void updateUsername(String username){
        Log.d(TAG, "updateUsername:  actulizar  username para " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);


        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

    }

    /**
     * Actualizar para Email para os Users Node(dbname_users)
     * @param email
     */

    public void updateEmail(String email){
        Log.d(TAG, "updateUsername:  actulizar  username para " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(username);

    }



//    public boolean checkIfUsernameExixts(String username, DataSnapshot dataSnapshot) {
//        Log.d(TAG, "checkIfUsernameExixts: ver se o " + username + " já existe");
//
//        User user = new User();
//
//        for (DataSnapshot ds : dataSnapshot.child(userID).getChildren()) {
//            Log.d(TAG, "checkIfUsernameExixts:  DataSnapshot: " + ds);
//
//            user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "checkIfUsernameExixts: username: " + user.getUsername());
//
//            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
//                Log.d(TAG, "checkIfUsernameExixts:  Encontrou o Username");
//                return true;
//
//            }
//        }
//
//        return false;
//    }


    public void registerNewEmail(final String email, final String password, final String username) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            //Send verification Email
                            sendVerificationEmail();

                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete:  Authstate chaged: " + userID);
                        }

                        // ...
                    }
                });

    }


    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                            } else {
                                Toast.makeText(mContext, "Nao conseguimos enviar o email de verificação", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }


    /**
     * Add um novo User na base d dados
     * Add to the dbname_user_account_settings and users
     *
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */

    public void addNewUser(String email, String username, String description, String website, String profile_photo) {

        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));


        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);


        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID
        );

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);

    }


    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings:  retrieving user account settings from firebase");


        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        //User_Account_Settings_Node
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))){
                Log.d(TAG, "getUserAccountSettings:  datasnapshop  " + ds);

                try {



                settings.setDisplay_name(
                        ds.child(userID)
                        .getValue(UserAccountSettings.class)
                        .getDisplay_name()
                );
                settings.setUsername(
                        ds.child(userID)
                                .getValue(UserAccountSettings.class)
                                .getUsername()
                );
                settings.setWebsite(
                        ds.child(userID)
                                .getValue(UserAccountSettings.class)
                                .getWebsite()
                );
                settings.setDescription(
                        ds.child(userID)
                                .getValue(UserAccountSettings.class)
                                .getDescription()
                );
                settings.setProfile_photo(
                        ds.child(userID)
                                .getValue(UserAccountSettings.class)
                                .getProfile_photo()
                );
                settings.setPosts(
                        ds.child(userID)
                                .getValue(UserAccountSettings.class)
                                .getPosts()
                );
                settings.setFollowing(
                        ds.child(userID)
                                .getValue(UserAccountSettings.class)
                                .getFollowing()
                );

                settings.setUser_id(
                        ds.child(userID)
                                .getValue(UserAccountSettings.class)
                                .getUser_id()
                );

                    Log.d(TAG, "getUserAccountSettings:  A tratar da informaçºao do USER"  + settings.toString());
                }catch (NullPointerException e){
                    Log.d(TAG, "getUserAccountSettings: NullPointerException " + e.getMessage());
                }


            }

            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))){
                Log.d(TAG, "getUserAccountSettings:  datasnapshop");

                try {

                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );

                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );

                user.setPhone_number(
                        ds.child(userID)
                                .getValue(User.class)
                                .getPhone_number()
                );
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );
                Log.d(TAG, "getUserAccountSettings:  A tratar da informaçºao do USER"  + settings.toString());

                }catch (NullPointerException e){
                    Log.d(TAG, "getUserAccountSettings: NullPointerException " + e.getMessage());
                }

            }

        }


        return new UserSettings(user, settings);

    }


}



