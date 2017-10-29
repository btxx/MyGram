package bruno.varela.tavares.mygram.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bruno.varela.tavares.mygram.R;

/**
 * Created by Bruno on 01/08/2017.
 */

public class CamaraFragment extends Fragment {
    private static final String TAG = "CamaraFragment";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camara, container,false);



        return view;
    }
}
