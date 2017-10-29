package bruno.varela.tavares.mygram.share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.profile.AccountSettingsActivity;
import bruno.varela.tavares.mygram.utils.FilePaths;
import bruno.varela.tavares.mygram.utils.FileSearch;
import bruno.varela.tavares.mygram.utils.GridImageAdapter;

/**
 * Created by Bruno on 21/08/2017.
 */

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";


    private static final int NUM_GRID_COLUMS = 3;


    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;


    //vars
    private ArrayList<String> directories;
    ArrayList<String> directoriesCamara ;
    private String mAppend = "file:/";
    private String selectedImage;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container,false);
        Log.d(TAG, "onCreateView: Started");
        galleryImage = (ImageView)view.findViewById(R.id.galleryImageView);
        gridView = (GridView)view.findViewById(R.id.gridView);
        directorySpinner = (Spinner)view.findViewById(R.id.spinerDirectory);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        directoriesCamara =  new ArrayList<>();


        ImageView shareClose = (ImageView)view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:  fechar gallery fragment");
                getActivity().finish();
            }
        });

        TextView nextScrenn = (TextView)view.findViewById(R.id.tvShare);
        nextScrenn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Ir para final Share Screen");

                //Adicionar foto normal
                if (isRootTask()) {
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.select_image), selectedImage);
                    startActivity(intent);

                    // Se viemos de mudar a foto de profil
                } else {
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.select_image), selectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();

                }



            }
        });



        init();
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









    //v-> 46
    private void init(){
        FilePaths filePaths = new FilePaths();
        //Procurar por outro directorios dentri "/storage/emulated/0/pictures"
        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null){
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);

        }

        //------ Bruno
        //Isto foi eu que meti é o directorio do DMCI
        if (FileSearch.getDirectoryPaths(filePaths.CAMERA) != null){
            directoriesCamara = FileSearch.getDirectoryPaths(filePaths.CAMERA);
            for (int i = 0; i <directoriesCamara.size() ; i++) {
               directories.add(directoriesCamara.get(i));
            }

        }



        //Isto e para meter so os Ultimo nome no direitorio
        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i <directories.size() ; i++) {
            //Obter o ultimo index depois de /
            int index = directories.get(i).lastIndexOf("/");
            //Obter o nome e trocar / por nada estava assim antes /layout
            String string = directories.get(i).substring(index).replace("/", "");
            directoryNames.add(string);
        }


      //  directories.add(filePaths.CAMERA);

        //Meter os nomes no
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d(TAG, "onItemSelected:  " +  position);
                // Preparar o image Grid View
                setupGridView(directories.get(position));


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


       // ver por pastas dentro "/storage/emulated/0"
    }

    private void setupGridView(String selectDirectory){
        Log.d(TAG, "setupGridView:  directorio escolhido " + selectDirectory);
        //Isto tera todas  as URL das imagens
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectDirectory);

        //SET GRID COLUMN WIDTH    meter a largura / por 3
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imagewWidth = gridWidth/NUM_GRID_COLUMS;
        gridView.setColumnWidth(imagewWidth);


        //Use the grid adapter  to adapte the image grid
        // mAppend = "file:/"; no uNIVERSAL IMAGE lOADER É assim que tens de fazer para obter image no movel
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_image_view, mAppend, imgURLs);
        gridView.setAdapter(adapter);

        try {
            //Meter a primeira image em grande quando o gallery e lançada
            setImage(imgURLs.get(0), galleryImage, mAppend);
            //Obter a select image para depois passar na intent
            selectedImage = imgURLs.get(0);

        }catch (Exception e){
            Log.d(TAG, "setupGridView:  " + e.getMessage());
        }



        //Para quando clik na imagem para meter na imageView em cima
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d(TAG, "onItemClick:  selecionar Imagem: " + imgURLs.get(position));

                setImage(imgURLs.get(position), galleryImage, mAppend);
                //Obter a select image para depois passar na intent
                selectedImage = imgURLs.get(position);

            }
        });
    }


    private void setImage(String imgURL, ImageView image, String append){

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgURL,image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });
    }



}
