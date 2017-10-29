package bruno.varela.tavares.mygram.utils;

import android.os.Environment;

/**
 * Created by Bruno on 21/08/2017.
 */

public class FilePaths {
    // v-> 45 ou 46

    //"/storage/emulated/0/ Isto dá este caminho
    public  String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();


    public  String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA    = ROOT_DIR + "/DCIM";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";
}
