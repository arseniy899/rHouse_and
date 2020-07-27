package ml.arseniy899.rhouse.HubManage;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter
{
	
	private static final String[] TAB_TITLES = new String[]{"Устройства", "Расписание"};
	private final Context mContext;
	
	public SectionsPagerAdapter(Context context, FragmentManager fm)
	{
		super(fm);
		mContext = context;
	}
	
	@Override
	public Fragment getItem(int position)
	{
		// getItem is called to instantiate the fragment for the given page.
		// Return a HubUnitsFragment (defined as a static inner class below).
		return HubUnitsFragment.newInstance(mContext);
	}
	
	@Nullable
	@Override
	public CharSequence getPageTitle(int position)
	{
		return TAB_TITLES[position];
	}
	
	@Override
	public int getCount()
	{
		// Show 2 total pages.
		return 1;
	}
}