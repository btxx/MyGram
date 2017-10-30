package bruno.varela.tavares.mygram.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import bruno.varela.tavares.mygram.R;
import bruno.varela.tavares.mygram.models.Comment;
import bruno.varela.tavares.mygram.models.Photo;
import bruno.varela.tavares.mygram.utils.BottomNavigationViewHelper;
import bruno.varela.tavares.mygram.utils.MainFeedListAdapter;

/**
 * Created by Bruno on 01/08/2017.
 */

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    //widgets
    private ImageView imageViewMyGram;
    private RelativeLayout imageViewCamera,imageViewArrow;


    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainFeedListAdapter mAdapter;
    private int mResults;
    private BottomNavigationViewEx bottomNavigationViewEx;

    public static final int ACTIVITY_NUM = 0;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container,false);
        mListView = (ListView)view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        imageViewArrow = (RelativeLayout) view.findViewById(R.id.arrow);
        imageViewCamera = (RelativeLayout) view.findViewById(R.id.camara);
        imageViewMyGram = (ImageView)view.findViewById(R.id.myGramImageView);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);

        setUpBottomNavigationView();

        imageViewArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HomeActivity)getActivity()).goToMessageFragment();



            }
        });

        imageViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HomeActivity)getActivity()).goToCamaraFragment();



            }
        });

        imageViewMyGram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HomeActivity)getActivity()).goToHomeFragment();



            }
        });




        getFollowing();

        return view;
    }


    private void getFollowing(){
        Log.d(TAG, "getFollowing: searching for following ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: dataSnapshot ");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " +
                    singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());


                    mFollowing.add( singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                //get as Fotos
                getPhotos();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < mFollowing.size(); i++) {
            final  int count = i;
            Query query = reference
                    .child(getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: dataSnapshot ");
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String,Object>) singleSnapshot.getValue();

                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());


                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);

                        }


                        photo.setComments(comments);
                        mPhotos.add(photo);

                    }
                    if (count >= mFollowing.size() -1){
                        //Mostrar as fotos
                        displayPhotos();
                    }



                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    /**
     * Mostrar as Fotos
     */
    private void displayPhotos(){
        mPaginatedPhotos = new ArrayList<>();
        try {

            //Sort em termos de data creada
        if (mPhotos != null){
            Collections.sort(mPhotos, new Comparator<Photo>() {
                @Override
                public int compare(Photo photo, Photo photo2) {
                    return photo2.getDate_created().compareTo(photo.getDate_created());
                }
            });

            //ISto ate o Adapter é para ir descaregando de 10 em 10
            int iterations = mPhotos.size();
            if (iterations > 10){
                iterations = 10;
            }

            mResults = 10;
            for (int i = 0; i < iterations; i++) {
                mPaginatedPhotos.add(mPhotos.get(i));

            }


            mAdapter = new MainFeedListAdapter(getActivity(), R.layout.layout_mainfeed_list_item, mPaginatedPhotos);
            mListView.setAdapter(mAdapter);
        }

        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException " + e.getMessage());
        }

    }




    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");
        try {

            if (mPhotos.size() > mResults && mPhotos.size() > 0){

                int iterations;
                if (mPhotos.size() > (mResults + 10)){
                    Log.d(TAG, "displayMorePhotos: é maior que 10 photos");
                    iterations = 10;

                }else {
                    Log.d(TAG, "displayMorePhotos: tem menus de 10 Fotos" );
                    iterations = mPhotos.size() + - mResults;
                }

                //Adicionar novas fotos a pagination
                for (int i = mResults; i <mResults + iterations ; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults += iterations;
                mAdapter.notifyDataSetChanged();
            }

        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException " + e.getMessage());
        }
    }


    private  void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: HomeActivity");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(),getActivity() ,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
