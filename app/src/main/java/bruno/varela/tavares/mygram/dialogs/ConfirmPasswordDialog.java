package bruno.varela.tavares.mygram.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import bruno.varela.tavares.mygram.R;

/**
 * Created by Bruno on 19/08/2017.
 */

public class ConfirmPasswordDialog extends DialogFragment {

    private static final String TAG = "ConfirmPasswordDialog";

    public  interface OnConfirmPasswordListener{
        public void onConfirmPassword(String password);
    }

    public OnConfirmPasswordListener onConfirmPasswordListener;


    TextView mPassword;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container,false);
        Log.d(TAG, "onCreateView: Stated" );
        mPassword = (TextView) view.findViewById(R.id.confirm_password);

        TextView confirmDialog = (TextView)view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:  inserir e confirmar a password");

                String password = mPassword.getText().toString();
                if (!password.equals("")){
                    onConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }else{
                    Toast.makeText(getActivity(), "Tens que inserir a password", Toast.LENGTH_LONG).show();
                }




            }
        });


        TextView cancelDialog = (TextView)view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:  closing the dialog");
                getDialog().dismiss();

            }
        });



        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {

            onConfirmPasswordListener = (OnConfirmPasswordListener)getTargetFragment();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }



    }
}
