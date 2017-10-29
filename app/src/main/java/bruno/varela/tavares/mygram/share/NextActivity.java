package bruno.varela.tavares.mygram.share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.utils.FirebaseMethods;
import bruno.varela.tavares.mygram.utils.UniversalImageLoader;

/**
 * Created by Bruno on 22/08/2017.
 */

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;

    //widgets
    private EditText mCaption;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private Intent intent;
    private Bitmap bitmap;





    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = (EditText)findViewById(R.id.caption);
        Log.d(TAG, "onDataChange:  image Count: " + imageCount);

       setupFireBaseAuth();

        ImageView backarrow = (ImageView)findViewById(R.id.ivBackArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:  fechar gallery fragment");
                finish();
            }
        });

        TextView share = (TextView)findViewById(R.id.tvNext);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Upload the image to the firebase
                Toast.makeText(NextActivity.this, R.string.toast_update_new_photo,Toast.LENGTH_LONG).show();
                String caption = mCaption.getText().toString();
                if (intent.hasExtra(getString(R.string.select_image))){
                    imgUrl = intent.getStringExtra(getString(R.string.select_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl, null);


                }else if (intent.hasExtra(getString(R.string.select_bitmap))){
                    bitmap =  (Bitmap) intent.getParcelableExtra(getString(R.string.select_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, null, bitmap);

                }







            }
        });

        setImage();
    }

    private void someMethod(){

        /*
        1)
        Create a data model for Photos

        2)
        Add properties to the Photo Objects(caption, date, imagUrl, photo_id, tags, user_id)

        3)
        Contar o numero de fotos que o user tem

        4)
         a) Upload a foto para o FireBase
         b)insert into photos node
         c)insert into user_photos_node





         */
    }





    /**     * Recebe o url da imagem da intent e mostra a image escolhida
     */
    private void setImage(){
        intent = getIntent();
        ImageView image = (ImageView)findViewById(R.id.imageShare);

        //Se veio da gallery
        if(intent.hasExtra(getString(R.string.select_image))){
            imgUrl = intent.getStringExtra(getString(R.string.select_image));
            Log.d(TAG, "setImage:  got nova imagem url " + imgUrl);
            // mAppend =  "file:/"
            UniversalImageLoader.setImage(imgUrl, image, null, mAppend);

            //se veio de bitmap
        }else if(intent.hasExtra(getString(R.string.select_bitmap))){
            bitmap =  (Bitmap) intent.getParcelableExtra(getString(R.string.select_bitmap));
            Log.d(TAG, "setImage: Novo Bitmap");
            image.setImageBitmap(bitmap);
        }

    }


    /**
     * *********************************************** FireBase**************************************
     */

    /**
     * FireBase Auth
     *
     */
    private void setupFireBaseAuth(){
        Log.d(TAG, "setupFireBaseAuth:  Conectar User Auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange:  image Count: " + imageCount);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



}
