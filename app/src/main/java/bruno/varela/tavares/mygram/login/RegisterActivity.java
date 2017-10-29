package bruno.varela.tavares.mygram.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.models.User;
import bruno.varela.tavares.mygram.utils.FirebaseMethods;

/**
 * Created by Bruno on 12/08/2017.
 */

public class RegisterActivity extends AppCompatActivity{

    //Video 22 talvez nao vi tudo

    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private String email,username,password;
    private EditText mEmail, mPassword,mUsername;
    private TextView loadingPleaseWait;
    private ProgressBar mProgressBar;
    private Button btnRegister;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private FirebaseMethods mFirebaseMethods;



    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String append = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        mFirebaseMethods = new FirebaseMethods(mContext);
        Log.d(TAG, "onCreate: Starting");
        initWidgets();
        setupFireBaseAuth();

        init();

    }


    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                if (chechInputs(email,username,password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);

                    mFirebaseMethods.registerNewEmail(email,password,username);
                }
            }
        });

    }


    private boolean chechInputs(String email,String username, String password){
        Log.d(TAG, "chechInputs:  ver se os Inputs nao são null");
        if (email.equals("") || password.equals("") || username.equals("")){
            Toast.makeText(mContext, R.string.toast_register_empty_fields,Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }






    private void initWidgets(){
        Log.d(TAG, "initWidgets: Iniciar os Widgets");
        mEmail = (EditText)findViewById(R.id.input_email);
        mUsername = (EditText)findViewById(R.id.input_username);
        btnRegister = (Button)findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        loadingPleaseWait = (TextView)findViewById(R.id.loadingPleaseWait);
        mPassword = (EditText)findViewById(R.id.input_password);
        mContext = RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);


    }





    /**
     * *********************************************** FireBase**************************************
     */



    /**
     * FireBase Auth
     */


    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Ver se o username " + username + " existe");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    if (singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        append = myRef.push().getKey().substring(3,10);
                        Log.d(TAG, "onDataChange:  Se o username já existe adicionar um random string " + append);
                    }
                }

                String mUsername = "";
                mUsername =username + append;


                //2 adicionar o novo user a base de dados
                mFirebaseMethods.addNewUser(email,mUsername,"","","");

                Toast.makeText(mContext, R.string.toast_register_send_email,Toast.LENGTH_LONG).show();

                mAuth.signOut();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    private void setupFireBaseAuth() {
        Log.d(TAG, "setupFireBaseAuth:  Conectar User Auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();



        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);



                            //3 adicionar o novo user_account_settings a base de dados
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //Acabar com esta actuivity e ir Para a LoginActivity
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


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
