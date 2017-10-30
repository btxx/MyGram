package bruno.varela.tavares.mygram.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.models.Comment;
import bruno.varela.tavares.mygram.models.Like;
import bruno.varela.tavares.mygram.models.Photo;
import bruno.varela.tavares.mygram.models.User;
import bruno.varela.tavares.mygram.models.UserAccountSettings;
import bruno.varela.tavares.mygram.models.UserSettings;
import bruno.varela.tavares.mygram.utils.BottomNavigationViewHelper;
import bruno.varela.tavares.mygram.utils.FirebaseMethods;
import bruno.varela.tavares.mygram.utils.GridImageAdapter;
import bruno.varela.tavares.mygram.utils.UniversalImageLoader;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Bruno on 15/08/2017.
 */

public class ProfileFragment  extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface  OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;



    private static final int ACTIVITY_NUM = 4;
    public static final int NUM_GRID_COLUMNS = 3;
    private TextView mPosts,mFollowers,mFollowing,mDisplayName,mUsername,mWebsite,mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;

    private Context mContext;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //vars
    private int mFollowersCount = 0;
    private int mFollowingCount = 0;
    private int mPostsCount = 0;


    /***
     *
     * Estou m 10 video 81
     */


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        mDisplayName = (TextView)view.findViewById(R.id.display_name);
        mUsername = (TextView)view.findViewById(R.id.username);
        mWebsite  = (TextView)view.findViewById(R.id.webSite);
        mDescription = (TextView)view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView)view.findViewById(R.id.profile_photo);
        mPosts = (TextView) view.findViewById(R.id.tvPosts);
        mFollowers = (TextView)view.findViewById(R.id.tvFallowers);
        mFollowing = (TextView)view.findViewById(R.id.tvFolowing);
        mProgressBar = (ProgressBar)view.findViewById(R.id.profileProgressBar);
        gridView  = (GridView)view.findViewById(R.id.gridView);
        toolbar = (Toolbar)view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView)view.findViewById(R.id.profileMenu);
        bottomNavigationViewEx = (BottomNavigationViewEx)view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());


        setUpBottomNavigationView();
        setupToolbar();

        setupFireBaseAuth();

        setupGridView();

        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        TextView editProfile = (TextView)view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:  navigating to " + mContext.getString(R.string.edit_profile_fragment));
                Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                //Eu tirai a animaçao
                // getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        Log.d(TAG, "onCreateView: stared");
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        setupFireBaseAuth();
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener)getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: "  + e.getMessage());

        }
        super.onAttach(context);
    }


    private void setupGridView(){
        Log.d(TAG, "setupGridView:  Setting image Grid");

        //Criar uma array list para meter as fotos da base de dados
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String,Object>) singleSnapshot.getValue();

                    try{

                    photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());// esta linha deu problemas
                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());


                    ArrayList<Comment> comments = new ArrayList<Comment>();
                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_comments)).getChildren()){
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        comments.add(comment);

                    }


                    photo.setComments(comments);

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_likes)).getChildren()){
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    photo.setLikes(likesList);
                    photos.add(photo);

                    }catch (NullPointerException e){
                        Log.e(TAG, "onDataChange: NullPointerException " +  e.getMessage());
                    }catch (InstantiationException e){
                        //Ista foi eu que meti ver melhor depois
                        Log.e(TAG, "onDataChange: InstantiationException " +  e.getMessage());
                    }


                }

                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);


                ArrayList<String> imgUrls = new ArrayList<String>();
                for (int i = 0; i <photos.size(); i++) {
                    Log.d(TAG, "onDataChange:  Images path " + photos.get(i).getImage_path());
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter = new  GridImageAdapter(getActivity(),R.layout.layout_grid_image_view,"",imgUrls);
                gridView.setAdapter(adapter);


                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
                    }
                });
                
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelado");

            }
        });
    }




    private void getFollowersCount(){
        mFollowersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange:  follower Encontrado: "  +  singleSnapshot.getValue());
                    mFollowersCount++;
                }

                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getFollowingCount(){
        mFollowingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange:  following Encontrado: "  +  singleSnapshot.getValue());
                    mFollowingCount++;
                }

                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getPostsCount(){
        mPostsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange:  following Encontrado: "  +  singleSnapshot.getValue());
                    mPostsCount++;
                }

                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }










    private void setProfileWidgets(UserSettings userSettings){
        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null,"");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
//        mPosts.setText(String.valueOf(settings.getPosts()));
//        mFollowing.setText(String.valueOf(settings.getFollowing()));
//        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mProgressBar.setVisibility(View.GONE);

    }





        private void setupToolbar(){
            ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);

            profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to account settings");

                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);


            }
        });



    }


        /*
    *
    *Botões de baixo
    * */
    private  void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,getActivity(),bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

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
