package bruno.varela.tavares.mygram.share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.utils.BottomNavigationViewHelper;
import bruno.varela.tavares.mygram.utils.Permissions;
import bruno.varela.tavares.mygram.utils.SectionsPagerAdapter;

/**
 * Created by Bruno on 01/08/2017.
 */

public class ShareActivity extends AppCompatActivity{
    private static final String TAG = "ShareActivity";

    private Context mContext = ShareActivity.this;
    public static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate:  começa");

        if (checkPermissionsArrays(Permissions.PERMISSIONS)){
            setupViewPager();
            
        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }
        
     //   setUpBottomNavigationView();
    }


    /**
     * return the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     *
     * @return
     */
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    /**
     * Setup View Pager para utilizar as tabs
     */
    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragmend( new GalleryFragment());
        adapter.addFragmend(new PhotoFragment());

        mViewPager = (ViewPager)findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(R.string.gallery);
        tabLayout.getTabAt(1).setText(R.string.photo);

    }


    /**
     * Verificar todas as Permissions
     * @param permissions
     */

    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermisssions:  a verificar permisions ");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
        //VERIFY_PERMISSIONS_REQUEST Este é importante se estiveres onRequestPermission result
    }


    public int getTask(){
        Log.d(TAG, "getTask:  TASK: " +  getIntent().getFlags());
        return getIntent().getFlags();
    }




    /**
     * Ver varias permisoes e se foram dadas
     * @param permissions
     * @return
     */
    
    public boolean checkPermissionsArrays(String[] permissions){
        Log.d(TAG, "checkPermissionsArrays:  ver permissions array");
        for (int i = 0; i < permissions.length; i++) {
        String check = permissions[i];
            if (!checkPermisions(check)){
                return false;
            }
        }
        return true;
    }

    /**
     * Ver uma permisão se foi dada
     * @param permission
     * @return
     */

    public boolean checkPermisions(String permission){
        Log.d(TAG, "checkPermisions:  ver permisssions " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermisions:  \n Permission não foi dada : "  + permission);
            return false;

        }
        else {
            Log.d(TAG, "checkPermisions:  \n Permission foi dada");
            return true;
        }
    }
    
    
    

    /*
    *
    *Botões de baixo
    * */
    private  void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this ,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }
}
