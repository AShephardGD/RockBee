package com.example.rockbee;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private SettingFragment sf;
    private CatalogFragment cf;
    private MusicFragment mf;
    private PlaylistFragment pf;
    private ServerMusicFragment smf;
    private Context context;
    private static final int[] TABS_TITLES = new int[]{R.string.settings,R.string.catalog, R.string.isPlaying, R.string.Playlists, R.string.Lobby};
    public SectionsPagerAdapter(FragmentManager fm, SettingFragment sf, CatalogFragment cf,
                                MusicFragment mf, PlaylistFragment pf, ServerMusicFragment smf,
                                Context context) {
        super(fm);
        this.sf = sf;
        this.cf = cf;
        this.mf = mf;
        this.pf = pf;
        this.smf = smf;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return sf;
            case 1:
                return cf;
            case 2:
                return mf;
            case 3:
                return pf;
            case 4:
                return smf;
            default:
                return cf;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position){
        return context.getResources().getText(TABS_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 5;
    }
}
