package bruno.varela.tavares.mygram.utils;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.models.Comment;
import bruno.varela.tavares.mygram.models.Like;
import bruno.varela.tavares.mygram.models.Photo;
import bruno.varela.tavares.mygram.models.User;
import bruno.varela.tavares.mygram.models.UserAccountSettings;

/**
 * Created by Bruno on 27/08/2017.
 */

public class ViewPostFragment extends Fragment {
    private static final String TAG = "ViewPostFragment";



    public interface OnCommentThreadSelectedListener {
        void onCommentThreadSelectedListener(Photo  photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private GestureDetector mGestureDetector;

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView mBackLabel, mCaption,mUsername,mTimestamp, mLikes, mComments;
    private ImageView mBackArrow,mEllipses, mHeartRed,mHeartWhite, mProfileImage, mComment;


    private String photoUsername;
    private String photoProfile;
    private String mLikesString = "";




    private UserAccountSettings mUserAccountSettings;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private User mCurrentUser;
    private Context mContext;


    public  ViewPostFragment(){
        super();
        setArguments(new Bundle());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mPostImage = (SquareImageView)view.findViewById(R.id.post_image);
        bottomNavigationViewEx = (BottomNavigationViewEx)view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted);
        mEllipses = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView)view.findViewById(R.id.profile_photo);
        mLikes = (TextView)view.findViewById(R.id.image_likes);
        mComment = (ImageView) view.findViewById(R.id.speech_bubble);
        mComments = (TextView) view.findViewById(R.id.image_comments_link);
        Log.d(TAG, "onCreateView: ViewPostFragment");
        mContext = getActivity();


        //Animação do coração
        mHeart = new Heart(mHeartWhite, mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());


        setupFireBaseAuth();
        setUpBottomNavigationView();


        //setupWidgets();
        return view;
    }


    private void init(){
        try{
            //  mPhoto = getPhotoFromBundle();

            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), mPostImage,null, "");
            mActivityNumber = getActivityNumFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();


            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.dbname_photos))
                    .orderByChild(mContext.getString(R.string.field_photo_id))
                    .equalTo(photo_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange:  ");

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String,Object>) singleSnapshot.getValue();

                        photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());


                        List<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot.child(mContext.getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);

                        }


                        photo.setComments(comments);

                        mPhoto = photo;

                        getCurrentUser();
                        getPhotoDetails();
                        //     getLikesString();


                    }



                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: Query Cancelado");

                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: "  + e.getMessage() );
        }



    }


    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()){
            init();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener)getActivity();

        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " +  e.getMessage() );
        }
    }

    private void getLikesString(){
        Log.d(TAG, "getLikesString: getting likes strings");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: dataSnapshot ");
                mUsers = new StringBuilder();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: FOR " + singleSnapshot.getValue(Like.class).getUser_id().toString());

                    /// VIDEO 72  9.08m
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(mContext.getString(R.string.dbname_users))
                            .orderByChild(mContext.getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                             Log.d(TAG, "onDataChange: Encontrou o Like " + singleSnapshot.getValue(User.class).getUsername().toString());

                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(",");

                            }




                            String [] splitUsers = mUsers.toString().split(",");
                            for (int i = 0; i < splitUsers.length; i++) {

                            }


                            if (mUsers.toString().contains(mCurrentUser.getUsername() + ",")){
                                mLikedByCurrentUser = true;
                            }else{
                                mLikedByCurrentUser = false;
                            }

                            int length = splitUsers.length;

                            if (length == 1){
                                mLikesString = mContext.getString(R.string.liked_by) + " " + splitUsers[0];

                            }else if (length == 2){
                                mLikesString = mContext.getString(R.string.liked_by) + " " + splitUsers[0] + " "
                                        + getString(R.string.and)+  " " + splitUsers[1];

                            }else if (length == 3){
                                mLikesString = mContext.getString(R.string.liked_by) + " " + splitUsers[0] +
                                        ", " + splitUsers[1] +
                                " "   + getString(R.string.and)+  " "  + splitUsers[2];

                            }else if (length == 4){
                                mLikesString = mContext.getString(R.string.liked_by) + " " + splitUsers[0] +
                                        ", " + splitUsers[1] +
                                        ", " + splitUsers[2] +  " "
                                        + mContext.getString(R.string.and)+  " "  + splitUsers[3];


                            }else if (length > 4){
                                mLikesString = " Liked by " + splitUsers[0] +
                                        ", " + splitUsers[1] +
                                        ", " + splitUsers[2]  +  " "
                                        + mContext.getString(R.string.and)+  " "  + (splitUsers.length - 3) + " " + mContext.getString(R.string.others);

                            }

                            setupWidgets();


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                if (!dataSnapshot.exists()){
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     *
     */
    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Log.d(TAG, "getPhotoDetails:  meu ");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Meu Antes do for ");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    mCurrentUser = singleSnapshot.getValue(User.class);
//                    Log.d(TAG, "onDataChange: no for " );
                    Log.d(TAG, "onDataChange: Meu" + singleSnapshot.getValue().toString());
                }

                getLikesString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelado");

            }
        });
    }



    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot singleDataSnapshot : dataSnapshot.getChildren()){
                        String asas = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

                        //Id da foto
                        String keyID = singleDataSnapshot.getKey();
                        //case 1: O user já gostou da foto
                        if (mLikedByCurrentUser &&
                                singleDataSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){



                            myRef.child(mContext.getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            myRef.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(mPhoto.getUser_id())
                                    .child(mPhoto.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            //A IMAGEM BRANCA DESAPARECE
                            mHeart.toggleLike();
                            getLikesString();
                        }
                        // case 2 o user nao gostou da foto
                        else if (!mLikedByCurrentUser){
                            //add novo like
                            Log.d(TAG, "onDataChange:  NOT mLikedByCurrent User");
                            addNewLike();
                            break;
                        }
                    }

                    if (!dataSnapshot.exists()){
                        //add novo like
                        Log.d(TAG, "onDataChange:  nao tem ne like");
                        addNewLike();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG, "addNewLike: adicionar novo like");

        String newLikeId = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());


        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();

    }


    /**
     *
     * Faz um query a base de dados e vai buscar o User ID pela photo
     */
    private void getPhotoDetails(){
        Log.d(TAG, "getPhotoDetails: ");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        Log.d(TAG, "getPhotoDetails:  meu ");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Meu Antes do for ");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
//                    Log.d(TAG, "onDataChange: no for " );
                    Log.d(TAG, "onDataChange: Meu" + singleSnapshot.getValue().toString());
                }
              //  setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelado");

            }
        });
    }



    private void setupWidgets(){
        String timestampDiff = getTimestampDifference();
        if (!timestampDiff.equals("0")){
            mTimestamp.setText(timestampDiff +  " " + mContext.getString(R.string.time_days_ago));
        }else {
            mTimestamp.setText(R.string.time_today);
        }

        /***
         * EM baixo estava a dar um erro e meti um try
         */
        //Ver isto foi eu que meti aqui!!!!!!
        try {

            UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
            mUsername.setText(mUserAccountSettings.getUsername());
            mLikes.setText(mLikesString);
            mCaption.setText(mPhoto.getCaption());

        }catch (NullPointerException e){
            Log.d(TAG, "setupWidgets:  "  + e.getMessage());
        }



        if (mPhoto.getComments().size() > 0){
           mComments.setText(mContext.getString(R.string.view_all_commens_number) +  " " +  mPhoto.getComments().size() +  " " + mContext.getString(R.string.comments_number));
        }else {
            mComments.setText("");
        }

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating para tras");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating para tras");
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });


        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        if (mLikedByCurrentUser){
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return mGestureDetector.onTouchEvent(motionEvent);
                }

            });

        }else {
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });

        }







    }




    private String getTimestampDifference(){
        Log.d(TAG, "getTimestampDifference: ter a  diferença de timestamp ");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'" , Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = mPhoto.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.d(TAG, "getTimestampDifference:  ParseException: " + e.getMessage());
            difference = "0";

        }
        return difference;
    }

    /**
     * Tratar do Objecto Photo e que veio da bundle da profileActivity interface
     * @return
     */
    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle:  arguments " + getArguments());

        Bundle  bundle = this.getArguments();
        if (bundle != null){
            return  bundle.getParcelable(mContext.getString(R.string.photo));
        }else {
            return null;
        }
    }

    /**
     * Tratar do Numero da activity da bundle que da profileActivity interface
     * @return
     */

    public int getActivityNumFromBundle() {
        Log.d(TAG, "getActivityNumFromBundle:  arguments " + getArguments());

        Bundle  bundle = this.getArguments();
        if (bundle != null){
            return  bundle.getInt(mContext.getString(R.string.activity_number));
        }else {
            return 0;
        }
    }

    private  void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(),getActivity(),bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
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
