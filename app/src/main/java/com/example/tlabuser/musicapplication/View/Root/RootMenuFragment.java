package com.example.tlabuser.musicapplication.View.Root;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tlabuser.musicapplication.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class RootMenuFragment extends Fragment{

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager            mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.root_menu, container, false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //PagerTabカスタム
        PagerTabStrip strip = (PagerTabStrip) rootView.findViewById(R.id.pager_title_strip);
        strip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        strip.setTextSpacing(50);
        strip.setNonPrimaryAlpha(0.3f);

        return rootView;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch(position){
                case 0: fragment = new SituationMenuFragment(); break;
                case 1: fragment = new TrackMenuFragment();     break;
                case 2: fragment = new AlbumMenuFragment();     break;
                case 3: fragment = new ArtistMenuFragment();    break;
            }
            return fragment;
        }

        @Override
        public int getCount() { return 4; }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Situation";
                case 1: return "Track";
                case 2: return "Album";
                case 3: return "Artist";
            }
            return null;
        }

    }

}
