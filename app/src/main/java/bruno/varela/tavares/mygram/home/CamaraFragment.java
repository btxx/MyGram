package bruno.varela.tavares.mygram.home;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.otaliastudios.cameraview.CameraView;

import bruno.varela.tavares.mygram.R;

/**
 * Created by Bruno on 01/08/2017.
 */

public class CamaraFragment extends Fragment {
    private static final String TAG = "CamaraFragment";

    private CameraView cameraView;
    private ViewPager.DecorView mDecorView;


    private ImageView  imageVBackArrow;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
        View view = inflater.inflate(R.layout.fragment_camara, container,false);
        cameraView = (CameraView)view.findViewById(R.id.camera);
        imageVBackArrow = (ImageView)view.findViewById(R.id.backWhiteArrow);

        imageVBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ONONONONONO");
                ((HomeActivity)getActivity()).goToHomeFragment();
                

            }
        });


        return view;
    }





    public void hideStatusBar(){
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }





    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();

    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }


}
