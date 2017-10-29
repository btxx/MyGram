package bruno.varela.tavares.mygram.utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Bruno on 21/08/2017.
 */

public class FileSearch {

    //-> v->46

    /**
     * Search a Directory and return a list de todos os directorios
     * @param directory
     * @return
     */

    public static ArrayList<String> getDirectoryPaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            //Se é um direitorio
            if (listFiles[i].isDirectory()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }

        }
        return pathArray;
    }

    /**
     *
     * earch a Directory and return a list de todos os ficheiros
     * @param directory
     * @return
     */

    public static ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            //Se é um file
            if (listFiles[i].isFile()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }

        }
        return pathArray;

    }




}
