package erebus.sincloud.Adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import erebus.sincloud.Fragments.DiscoverFragment;
import erebus.sincloud.Fragments.PopularFragment;

public class MainActivityAdapter extends FragmentPagerAdapter
{
    private final int NUMBER_OF_FRAGMENTS = 4;

    public MainActivityAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new PopularFragment();
            case 1:
                return new DiscoverFragment();
            case 2:
                return new PopularFragment();
            case 3:
                return new PopularFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return NUMBER_OF_FRAGMENTS;
    }

}
