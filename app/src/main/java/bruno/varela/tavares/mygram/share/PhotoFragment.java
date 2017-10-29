package bruno.varela.tavares.mygram.share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.profile.AccountSettingsActivity;
import bruno.varela.tavares.mygram.utils.Permissions;

import static android.R.attr.bitmap;

/**
 * Created by Bruno on 21/08/2017.
 */

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    //CONSTANTES
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final  int GALLERY_FRAGMENT_NUM = 0;
    private static final  int CAMERA_REQUEST_CODE = 5;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container,false);
        Log.d(TAG, "onCreateView: Started");

        Button btnLaunchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:  a lançar camara");
                // Ver se estamos no PhotoFragment
                if (((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM){
                    //Ver se temos permiçao para camara
                    if (((ShareActivity)getActivity()).checkPermisions(Permissions.CAMARA_PERMISSION[0])){
                        Log.d(TAG, "onClick: usar a camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }else{
                        //Se nao tiveres permissao recomeça a Share Activity
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }


    /**
     * Se a task nao tem nem um extra return true
     * @return
     */
    private boolean isRootTask(){
        if (((ShareActivity)getActivity()).getTask() == 0){
            return true;
        }else {
            return false;
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult:  acabei d tirar as fotos");
         //   Log.d(TAG, "onActivityResult:  tentar navegar para o final share screen");

            //O try foi eu que meti
            try{
            Bitmap bitmap;
            //data e o tipo de dados que e usado
            bitmap = (Bitmap) data.getExtras().get("data");

            }catch (NullPointerException e){
                Log.d(TAG, "onActivityResult: "  + e.getMessage());
                return;
            }


            if (isRootTask()){
                try{
                    Log.d(TAG, "onActivityResult:  recebe o bitmap da camara "  + bitmap);
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.select_bitmap), bitmap);
                    startActivity(intent);
                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }
            }else {
                try{
                    Log.d(TAG, "onActivityResult:  recebe o bitmap da camara "  + bitmap);
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.select_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();

                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }

            }



        }
    }
}
