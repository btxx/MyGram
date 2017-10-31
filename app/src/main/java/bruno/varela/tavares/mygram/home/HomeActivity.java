package bruno.varela.tavares.mygram.home;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nostra13.universalimageloader.core.ImageLoader;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.login.LoginActivity;
import bruno.varela.tavares.mygram.models.Photo;
import bruno.varela.tavares.mygram.utils.FirebaseMethods;
import bruno.varela.tavares.mygram.utils.MainFeedListAdapter;
import bruno.varela.tavares.mygram.utils.SectionsPagerAdapter;
import bruno.varela.tavares.mygram.utils.UniversalImageLoader;
import bruno.varela.tavares.mygram.utils.ViewCommentsFragment;

public class HomeActivity extends AppCompatActivity  implements MainFeedListAdapter.OnLoadMoreItemsListener{

    private static final String TAG = "HomeActivity";

    public static final int ACTIVITY_NUM = 0;
    public static final int  HOME_FRAGMENT = 1;
    public static final int  CAMARA_FRAGMENT = 0;
    public static final int  MESSAGE_FRAGMENT = 2;

    private Context mContext = HomeActivity.this;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseMethods firebaseMethods ;

    //widgets
    public ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: Home Activity Começa");
        mViewPager = (ViewPager)findViewById(R.id.viewpager_container);
        mFrameLayout = (FrameLayout)findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.relLayoutParent) ;
        firebaseMethods = new FirebaseMethods(mContext);
       // hideStatusBar();

        setupFireBaseAuth();

        initImageLoader();
    //    setUpBottomNavigationView();
        setupViewPager();

        String hotas = firebaseMethods.getTimestamp();





       // mAuth.signOut();



    }


    public void hideStatusBar(){
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


        View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void onCommentThreadSelected(Photo photo, String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread ");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction .commit();
    }


    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

        /*
        *
        * Adiciona os 3 fragment da HOme page Camara,Home,Messages
        *
        * */

    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragmend(new CamaraFragment());
        adapter.addFragmend(new HomeFragment());
        adapter.addFragmend(new MessagesFragment());

        mViewPager.setAdapter(adapter);




//        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(mViewPager);

//        View view = getLayoutInflater().inflate(R.layout.layout_tap_image_view, null);
//        view.findViewById(R.id.icon);
//        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camara);
//        tabLayout.getTabAt(1).setCustomView(view);
//        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow);

    }

    /**
     *
     */
    public void goToMessageFragment(){
        mViewPager.setCurrentItem(MESSAGE_FRAGMENT);
    }

    public void goToCamaraFragment(){
        mViewPager.setCurrentItem(CAMARA_FRAGMENT);
    }

    public void goToHomeFragment(){
        mViewPager.setCurrentItem(HOME_FRAGMENT);
    }





    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE){
            showLayout();
        }
    }
//minuto 03:35



    /*
    *
    *Botões de baixo
    * */
//    private  void setUpBottomNavigationView(){
//        Log.d(TAG, "setUpBottomNavigationView: HomeActivity");
//        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
//        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
//        BottomNavigationViewHelper.enableNavigation(mContext,this ,bottomNavigationViewEx);
//        Menu menu = bottomNavigationViewEx.getMenu();
//        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//        menuItem.setChecked(true);
//    }

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
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                //Ver se o User fez o Login
                checkCurrentUser(user);
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
        mViewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Ver se o User for igual a null vais para login activity
     * @param user
     */

    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser:  Ver se o User está conectado");
        if (user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            //Meti aqui o finish nao sei se esta bem pode ter problemas
            finish();

        }
    }


    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems:  mostrar mais fotos ");
        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());
        if (fragment != null){
            fragment.displayMorePhotos();
        }
    }






}
