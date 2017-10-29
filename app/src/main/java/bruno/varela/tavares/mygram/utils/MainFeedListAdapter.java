package bruno.varela.tavares.mygram.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.home.HomeActivity;
import bruno.varela.tavares.mygram.models.Comment;
import bruno.varela.tavares.mygram.models.Like;
import bruno.varela.tavares.mygram.models.Photo;
import bruno.varela.tavares.mygram.models.User;
import bruno.varela.tavares.mygram.models.UserAccountSettings;
import bruno.varela.tavares.mygram.profile.ProfileActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Bruno on 26/09/2017.
 */

public class MainFeedListAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "MainFeedListAdapter";

    public  interface  OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;



    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";


    public MainFeedListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder {
        CircleImageView mProfileImage;
        String likesString;
        TextView username, timeDelta, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;


        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timeDelta = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        //get the current users username (need for checking likes strings)
        getCurrentUsername();

        // get likes strings
        getLikesString(holder);

        //set Caption
        holder.caption.setText(getItem(position).getCaption());


        //Set os commentarios
        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText(mContext.getString(R.string.view_all_commens_number) + " " + comments.size() + " " +  mContext.getString(R.string.comments_number));
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: loading comment thread for " + getItem(position).getPhoto_id());
                ((HomeActivity)mContext).onCommentThreadSelected(getItem(position),
                        mContext.getString(R.string.home_activity));


                //Fazer outra coisa
                ((HomeActivity)mContext).hideLayout();


            }
        });

        String timestampDifference = getTimestampDifference(getItem(position));
        if (!timestampDifference.equals("0")){
            holder.timeDelta.setText(timestampDifference +  " " + mContext.getString(R.string.time_days_ago));
        }else {
            holder.timeDelta.setText(R.string.time_today);
        }

        //Meter a image pricipal
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(),holder.image);

        //Profile image and username user o made the post
        Log.d(TAG, "getCurrentUsername: tratar User account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: dataSnapshot ");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: User Encontrado " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                 //   currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: navegar para o profil de " + holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);

                        }
                    });

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.mProfileImage);
                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: navegar para o profil de " + holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);

                        }
                    });

                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((HomeActivity)mContext).onCommentThreadSelected(getItem(position),
                                    mContext.getString(R.string.home_activity));



                            ((HomeActivity)mContext).hideLayout();

                        }
                    });


                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Get the User Object

        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_photos))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: dataSnapshot ");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: User Encountrado " + singleSnapshot.getValue(User.class).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (reachedEndOfList(position)){
            loadMoreData();
        }



        return convertView;
    }

    private boolean reachedEndOfList(int position){
        return position == getCount() -1;
    }



    private void loadMoreData(){
        try {
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener)getContext();
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException " + e.getMessage());
        }

        try {
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (NullPointerException e){
            Log.e(TAG, "loadMoreData: ClassCastException " + e.getMessage());
        }
    }





    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        ViewHolder mHolder;

        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            Log.d(TAG, "onDoubleTap: PHOTO ID ID ID ID ID " + mHolder.photo.getPhoto_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot singleDataSnapshot : dataSnapshot.getChildren()) {
                        String asas = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

                        String keyID = singleDataSnapshot.getKey();
                        //case 1: O user já gostou da foto


                        if (mHolder.likeByCurrentUser &&
                                singleDataSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {


                            //NAO APAGA
                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(mHolder.photo.getUser_id())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            //A IMAGEM BRANCA DESAPARECE
                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        // case 2 o user nao gostou da foto
                        else if (!mHolder.likeByCurrentUser) {
                            //add novo like
                            Log.d(TAG, "onDataChange:  NOT mLikedByCurrent User");
                            addNewLike(mHolder);
                            break;
                        }
                    }

                    if (!dataSnapshot.exists()) {
                        //add novo like
                        Log.d(TAG, "onDataChange:  nao tem ne like");
                        addNewLike(mHolder);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            return true;
        }
    }


    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes strings");
        try {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: dataSnapshot ");

                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: FOR " + singleSnapshot.getValue(Like.class).getUser_id().toString());

                        /// VIDEO 72  9.08m
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: Encontrou o Like " + singleSnapshot.getValue(User.class).getUsername().toString());

                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");

                                }

                                Log.d(TAG, "onDataChange: DATA DATA DATAAAAAAAAAAAAAAAA mUsers -> -> " + holder.users.toString());


                                String[] splitUsers = holder.users.toString().split(",");
//                                for (int i = 0; i < splitUsers.length; i++) {
//                                    Log.d(TAG, "onDataChange: LIKEEEEEEEEEEEEEEEEEEEE ->  " + splitUsers[i] + splitUsers.length);
//
//                                }

                                Log.d(TAG, "onDataChange: AQUI AQUI AQUI AQUI -> " + Arrays.toString(splitUsers));

                                //ERRO O Meu LIke na Home nao fica
                                if (holder.users.toString().contains(currentUsername + ",")) { // bruno, <- ve isto
                                    holder.likeByCurrentUser = true;
                                } else {
                                    holder.likeByCurrentUser = false;
                                }

                                int length = splitUsers.length;

                                if (length == 1) {
                                    holder.likesString = mContext.getString(R.string.liked_by) + " " + splitUsers[0];

                                } else if (length == 2) {
                                    holder.likesString = mContext.getString(R.string.liked_by) + " " +splitUsers[0] + " "
                                            + mContext.getString(R.string.and)+  " " + splitUsers[1];

                                } else if (length == 3) {
                                    holder.likesString = mContext.getString(R.string.liked_by) + " " + splitUsers[0] +
                                            ", " + splitUsers[1] + " "
                                            + mContext.getString(R.string.and)+  " " +splitUsers[2];

                                } else if (length == 4) {
                                    holder.likesString = mContext.getString(R.string.liked_by)+ " " +splitUsers[0] +  " " +
                                            ", " + splitUsers[1] +
                                            ", " + splitUsers[2] + " "
                                            + mContext.getString(R.string.and)+  " " +splitUsers[3];


                                } else if (length > 4) {
                                    holder.likesString = mContext.getString(R.string.liked_by)+ " " + splitUsers[0] +  " " +
                                            ", " + splitUsers[1] +
                                            ", " + splitUsers[2]
                                            + mContext.getString(R.string.and)+  " " + (splitUsers.length - 3)+ " " + mContext.getString(R.string.others);

                                }

                                //setup likes string
                                setupLikesString(holder, holder.likesString);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                    if (!dataSnapshot.exists()) {
                        holder.likesString = "";
                        holder.likeByCurrentUser = false;
                        //setup likes string
                        setupLikesString(holder, holder.likesString);
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (NullPointerException e) {
            Log.e(TAG, "onDataChange: NullPointerException " + e.getMessage());
            holder.likesString = "";
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder, holder.likesString);

        }


    }


    private void addNewLike(final ViewHolder holder) {
        Log.d(TAG, "addNewLike: adicionar novo like");

        String newLikeId = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());


        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);


        holder.heart.toggleLike();
        getLikesString(holder);
    }
    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: tratar User account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: dataSnapshot  En");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     *
     * @param holder
     * @param likesString
     */
    private void setupLikesString(final ViewHolder holder, String likesString){
        Log.d(TAG, "setupLikesString:  likes String " + holder.likesString);

        if (holder.likeByCurrentUser){
            Log.d(TAG, "setupLikesString: A foto foi gostado por current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);
                }
            });
        }else {
            Log.d(TAG, "setupLikesString: A foto foi gostado por current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);
                }
            });
        }
        holder.likes.setText(likesString);
    }




    // Algo esta mal
    private String getTimestampDifference(Photo photo){
        Log.d(TAG, "getTimestampDifference: ter a  diferença de timestamp ");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'" , Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_created();
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
