package bruno.varela.tavares.mygram.utils;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.models.Comment;
import bruno.varela.tavares.mygram.models.UserAccountSettings;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

/**
 * Created by Bruno on 09/09/2017.
 */

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;





    public CommentListAdapter(@NonNull Context context,
                              @LayoutRes int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private static  class ViewHolder{
        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView  == null){
            convertView = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();

            holder.comment = (TextView)convertView.findViewById(R.id.comment);
            holder.username = (TextView)convertView.findViewById(R.id.comment_username);
            holder.timestamp = (TextView)convertView.findViewById(R.id.comment_time_posted);
            holder.reply = (TextView)convertView.findViewById(R.id.comment_reply);
            holder.like = (ImageView) convertView.findViewById(R.id.comment_like);
            holder.likes = (TextView)convertView.findViewById(R.id.comment_likes);
            holder.profileImage = (CircleImageView)convertView.findViewById(R.id.comment_profile_image);

            convertView.setTag(holder);

        }else {

            holder = (ViewHolder)convertView.getTag();
        }

        //set comment
        holder.comment.setText(getItem(position).getComment());

        //set o timestamp
        String timesTampDifference = getTimestampDifference(getItem(position));
        if (!timesTampDifference.equals("0")){
            holder.timestamp.setText(timesTampDifference + " d");
        }else {
            holder.timestamp.setText(mContext.getString(R.string.time_today));
        }

        //username e imagem de profil
        Log.d(TAG, "getPhotoDetails: ");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        //Mudai aqui estava orderByChild em vez de .orderByKey() video 68 de min 4 pra frente
        Log.d(TAG, "getPhotoDetails:  meu ");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Meu Antes do for ");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: Meu" + singleSnapshot.getValue().toString());
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);



                }

                //  setupWidgets();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelado");

            }
        });


        try {

        if (position == 0){
            holder.like.setVisibility(View.GONE);
            holder.likes.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);
        }

        }catch (NullPointerException e){
            Log.d(TAG, "getView: NullPointerException: " + e.getMessage());
        }



        return convertView;
    }

    private String getTimestampDifference(Comment comment){
        Log.d(TAG, "getTimestampDifference: ter a  diferença de timestamp ");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'" , Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.d(TAG, "getTimestampDifference:  ParseException: " + e.getMessage());
            difference = "0";

        }
        return difference;
    }
}
