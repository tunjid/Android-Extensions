package com.tunjid.androidbootstrap.adapters;

import com.tunjid.androidbootstrap.fragments.DoggoFragment;
import com.tunjid.androidbootstrap.model.Doggo;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class DoggoPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Doggo> doggos;

    public DoggoPagerAdapter(List<Doggo> doggos, FragmentManager fm) {
        super(fm);
        this.doggos = doggos;
    }

    public Fragment getItem(int position) { return DoggoFragment.newInstance(doggos.get(position)); }

    public int getCount() { return this.doggos.size(); }

    @Nullable
    public CharSequence getPageTitle(int position) { return String.valueOf(position); }
}
