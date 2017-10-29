package bruno.varela.tavares.mygram.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import bruno.varela.tavares.mygram.R;

/**
 * Created by Bruno on 01/08/2017.
 */

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragment";

    private ImageView imageViewBackArrow;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container,false);
        Log.d(TAG, "onCreateView: ");
        imageViewBackArrow = view.findViewById(R.id.backArrow);

        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HomeActivity)getActivity()).goToHomeFragment();

            }
        });



        return view;
    }
}
