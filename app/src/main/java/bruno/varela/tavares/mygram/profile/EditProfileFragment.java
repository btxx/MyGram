package bruno.varela.tavares.mygram.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.dialogs.ConfirmPasswordDialog;
import bruno.varela.tavares.mygram.models.User;
import bruno.varela.tavares.mygram.models.UserAccountSettings;
import bruno.varela.tavares.mygram.models.UserSettings;
import bruno.varela.tavares.mygram.share.ShareActivity;
import bruno.varela.tavares.mygram.utils.FirebaseMethods;
import bruno.varela.tavares.mygram.utils.UniversalImageLoader;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Bruno on 07/08/2017.
 */

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener {

    private static final String TAG = "EditProfileFragment";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //Vars
    private UserSettings mUserSettings;


    //Fragments Widgets
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhotoNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;


    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: Got the password " + password);

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        // ///////////////////Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");


                            // Ver se o email ainda nao esta na base de dados
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        try {
                                            if (task.getResult().getProviders().size() == 1) {
                                                Log.d(TAG, "onComplete: Este email já esta em uso");
                                                Toast.makeText(getActivity(), "O Email já existe na base de dados", Toast.LENGTH_LONG).show();
                                            } else {
                                                Log.d(TAG, "onComplete: este email está desponivel");

                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                                //O Email pode ser actualizado
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(), "O Email foi actualizado com sucesso", Toast.LENGTH_LONG).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }


                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "onComplete: NullPointerException " + e.getMessage());
                                        }

                                    }


                                }


                            });


                        } else {
                            Log.d(TAG, "User re-authenticated fail fail");

                        }

                    }
                });

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUsername = (EditText) view.findViewById(R.id.username);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mDescription = (EditText) view.findViewById(R.id.description);
        mEmail = (EditText) view.findViewById(R.id.email);
        mPhotoNumber = (EditText) view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);

        mFirebaseMethods = new FirebaseMethods(getActivity());

        // setProfileImage();
        setupFireBaseAuth();
        // back to ProfileActivity
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:  navigating back to ProfileActivity");
                getActivity().finish();
            }
        });

        ImageView checkMarck = (ImageView) view.findViewById(R.id.saveChanges);
        checkMarck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: a tentar salvar mudanças no profil");
                saveProfileSettings();

            }
        });


        return view;
    }


    private void setProfileWidgets(UserSettings userSettings) {

        mUserSettings = userSettings;


        // User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhotoNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));


        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Mudar a foto do profil");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                //mete um codigo unico
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//268435456
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

    }


    /**
     * *********************************************** FireBase**************************************
     */

    /**
     * FireBase Auth
     *
     */


    /**
     * Trata dos dados que foram escritos no widgets e e insere na base de dados
     * antes de fazer vai ver se o username e valido(unico)
     */

    private void saveProfileSettings() {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhotoNumber.getText().toString());
        boolean  canClose = true;


        // Se o Utilizador fez uma mudança no username
        if (!mUserSettings.getUser().getUsername().equals(username)) {
            canClose = false;
            checkIfUsernameExists(username);
        }
        //Se o User fez uma mudança no Email
        if (!mUserSettings.getUser().getEmail().equals(email)) {
            // 1- Reauthenticate
            // -> Confirmar a password e o email
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);
            // 2- ver se o email já esta registrado
            // ->
            // 3- mudar o email
            // -> meter o novo email na base de dados

            /**
             * Mudar os outros settings que não sao unicos
             */
            canClose = false;

        }

        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            mFirebaseMethods.updateUserAccountSettings(displayName,null,null,0);
        }
        if(!mUserSettings.getSettings().getWebsite().equals(website)){
            mFirebaseMethods.updateUserAccountSettings(null,website,null,0);
        }

        if(!mUserSettings.getSettings().getDescription().equals(description)){
            mFirebaseMethods.updateUserAccountSettings(null,null,description,0);
        }

        //Erro video 40
//        if(!mUserSettings.getSettings().getProfile_photo().equals(phoneNumber)){
//            mFirebaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);
//
//
//        }

        //Eu
        if (canClose) {
            getActivity().finish();
        }



    }

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


                if (!dataSnapshot.exists()) {

                    //Adicionar o Username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "O username foi registado com sucesso!! :) ", Toast.LENGTH_LONG).show();

                }
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "O username já existe. ", Toast.LENGTH_LONG).show();
                    }
                }
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
                //Tratar da informação do user na d«base de dados
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
                //Trabar das images do utilizador
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
