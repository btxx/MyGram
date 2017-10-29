package bruno.varela.tavares.mygram.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import bruno.varela.tavares.mygram.home.HomeActivity;
import bruno.varela.tavares.mygram.likes.LikesActivity;
import bruno.varela.tavares.mygram.profile.ProfileActivity;
import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.search.SearchActivity;
import bruno.varela.tavares.mygram.share.ShareActivity;

/**
 * Created by Bruno on 31/07/2017.
 */

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";


    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: ");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);

    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.ic_house:
                        intent = new Intent(context, HomeActivity.class);// ACTIVITY_NUM = 0;
                        context.startActivity(intent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_search:
                        intent = new Intent(context, SearchActivity.class); //ACTIVITY_NUM = 1;
                        context.startActivity(intent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_circle:
                        intent = new Intent(context, ShareActivity.class); // ACTIVITY_NUM = 2;
                        context.startActivity(intent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_alert:
                        intent = new Intent(context, LikesActivity.class); // ACTIVITY_NUM = 3;
                        context.startActivity(intent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_profil:
                        intent = new Intent(context, ProfileActivity.class); // ACTIVITY_NUM = 4;
                        context.startActivity(intent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;


                }


                return false;
            }
        });


    }


}
