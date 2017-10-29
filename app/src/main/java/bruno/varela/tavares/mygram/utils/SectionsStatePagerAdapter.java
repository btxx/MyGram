package bruno.varela.tavares.mygram.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bruno on 07/08/2017.
 */

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {



    private final List<Fragment> mFragmentList = new ArrayList<>();
    //Se tiver o fragment poaao ter o numero do fragment
    private final HashMap<Fragment,Integer> mFragments = new HashMap<>();
    //Se tiver o nome do fragment posso ter o numero do fragment
    private final HashMap<String, Integer> mFragmentNumbers = new HashMap<>();
    //Se o numero do Fragment posso ter o nome do fragment
    private final HashMap<Integer, String> mFragmentNames = new HashMap<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }


    public void addFragment(Fragment fragment, String fragmentName){
        mFragmentList.add(fragment);
        mFragments.put(fragment, mFragmentList.size()-1);
        mFragmentNumbers.put(fragmentName, mFragmentList.size()-1);
        mFragmentNames.put(mFragmentList.size()-1, fragmentName);
    }

    /**
     * retorna fragment com o nome param
     * @param fragmentName
     * @return
     */
    public Integer getFragmentNumber(String fragmentName){
        if (mFragmentNumbers.containsKey(fragmentName)){
            return mFragmentNumbers.get(fragmentName);
        }else {
            return null;
        }
    }


    /**
     * retorna fragment com o pelo fragment
     * @param fragment
     * @return
     */
    public Integer getFragmentNumber(Fragment fragment){
        //Mudai de  mFragmentNumbers para mFragments
        if (mFragments.containsKey(fragment)){
            return mFragments.get(fragment);
        }else {
            return null;
        }
    }

    public String getFragmentNumber(Integer fragmentNumber){
        if (mFragmentNames.containsKey(fragmentNumber)){
            return mFragmentNames.get(fragmentNumber);
        }else {
            return null;
        }
    }


}
