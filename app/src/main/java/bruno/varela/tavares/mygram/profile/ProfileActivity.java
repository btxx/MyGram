package bruno.varela.tavares.mygram.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.models.Photo;
import bruno.varela.tavares.mygram.models.User;
import bruno.varela.tavares.mygram.utils.ViewCommentsFragment;
import bruno.varela.tavares.mygram.utils.ViewPostFragment;
import bruno.varela.tavares.mygram.utils.ViewProfileFragment;


/**
 * Created by Bruno on 01/08/2017.
 */

public class ProfileActivity extends AppCompatActivity implements ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener, ViewProfileFragment.OnGridImageSelectedListener {


    private static final String TAG = "ProfileActivity";

    private Context mContext = ProfileActivity.this;

    public static final int ACTIVITY_NUM = 4;

    private ProgressBar mProgressBar;

    private ImageView profilePhoto;

    private static final int NUM_GRID_COLUMNS = 3;


    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener:  selecionar a comment thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }


    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected:  selected an image gridview: " + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate:  começa");


//
//        setUpBottomNavigationView();
//        setupToolbar();
//        setUpActivityWidgets();
//        setProfileImage();
//
//
//        tempGridSetup();

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        Log.d(TAG, "init:  inflating " + getString(R.string.profile_fragment));

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity))) {
            if (intent.hasExtra(getString(R.string.intent_user))) {
                User user = intent.getParcelableExtra(getString(R.string.intent_user));
                if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    Log.d(TAG, "init: Inflating View Profile ");
                    Log.d(TAG, "init: procurar o objecto user que vem com a intent extra");
                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user), intent.getParcelableExtra(getString(R.string.intent_user)));
                    fragment.setArguments(args);


                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.commit();
                }else {
                    Log.d(TAG, "init: Inflating  Profile ");
                    ProfileFragment fragment = new ProfileFragment();
                    FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.commit();
                }


            } else {
                Toast.makeText(mContext, "Algo de errado acontece ", Toast.LENGTH_LONG).show();
            }

        } else {
            Log.d(TAG, "init: Inflating  Profile ");
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
           // transaction.addToBackStack(getString(R.string.profile_fragment)); TIRAI ISTO
            transaction.commit();

        }


    }


}


//    private void tempGridSetup(){
//        ArrayList<String> imaUrl = new ArrayList<>();
//        imaUrl.add("http://www.gettyimages.fr/gi-resources/images/Embed/new/embed2.jpg");
//        imaUrl.add("http://www.gettyimages.fr/gi-resources/images/Embed/new/embed1.jpg");
//        imaUrl.add("http://www.gettyimages.fr/gi-resources/images/Embed/new/embed3.jpg");
//        imaUrl.add("http://media.gettyimages.com/photos/mutant-nature-picture-id517087243?s=170667a");
//        imaUrl.add("http://media.gettyimages.com/photos/girl-looking-through-the-train-window-picture-id517805609?s=170667a");
//        imaUrl.add("http://media.gettyimages.com/photos/little-girl-waiting-in-a-railway-station-picture-id539443505?s=170667a");
//        imaUrl.add("http://media.gettyimages.com/photos/young-woman-looks-across-mtns-and-valley-sunrise-picture-id178808529?s=170667a");
//        imaUrl.add("http://media.gettyimages.com/photos/man-walking-in-the-middle-of-a-city-road-picture-id499775115?s=170667a");
//        imaUrl.add("http://media.gettyimages.com/photos/caucasian-man-on-paddle-board-in-ocean-picture-id482135469?s=170667a");
//        imaUrl.add("http://media.gettyimages.com/photos/friends-enjoying-at-dinner-party-picture-id605559183?s=170667a");
//        imaUrl.add("http://media.gettyimages.com/photos/people-dive-into-the-sea-picture-id82819747?s=170667a");
//        imaUrl.add("http://media.gettyimages.com/photos/young-woman-walking-along-the-lake-picture-id595493909?s=170667a");
//
//
//        setupImageGrid(imaUrl);
//    }
//
//
//    private void setupImageGrid(ArrayList<String> imgUrl){
//        GridView gridView = (GridView) findViewById(R.id.gridView);
//
//
//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
//        gridView.setColumnWidth(imageWidth);
//
//        GridImageAdapter adapter = new GridImageAdapter(mContext,R.layout.layout_grid_image_view,"",imgUrl);
//
//        gridView.setAdapter(adapter);
//
//    }
//
//
//
//
//    private void setProfileImage(){
//        Log.d(TAG, "setProfileImage:  setting profile image");
//        String imgUrl = "www.decom.ufop.br/imobilis/wp-content/uploads/2014/08/blog-nimboz-android.jpg";
//        UniversalImageLoader.setImage(imgUrl, profilePhoto,null, "http://");
//    }
//
//
//    private void setUpActivityWidgets(){
//        mProgressBar = (ProgressBar)findViewById(R.id.profileProgressBar);
//        mProgressBar.setVisibility(View.GONE);
//        profilePhoto = (ImageView)findViewById(R.id.profile_photo);
//
//
//
//    }
//
//
//

//
//
//
