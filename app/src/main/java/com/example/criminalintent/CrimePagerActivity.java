package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_ID = "com.example.criminalintent.crime_id";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleManager.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = getCrimeIdFromIntent();

        mViewPager = findViewById(R.id.crime_view_pager);
        TabLayout tabLayout = findViewById(R.id.crime_tab_layout);

        mCrimes = CrimeLab.get(this).getCrimes();

        if (mCrimes.isEmpty()) {
            finish();
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(
                fragmentManager,
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return String.valueOf(position + 1);
            }
        };

        mViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mViewPager);

        if (crimeId != null) {
            for (int i = 0; i < mCrimes.size(); i++) {
                if (mCrimes.get(i).getId().equals(crimeId)) {
                    mViewPager.setCurrentItem(i);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private UUID getCrimeIdFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return intent.getSerializableExtra(EXTRA_CRIME_ID, UUID.class);
        }
        return (UUID) intent.getSerializableExtra(EXTRA_CRIME_ID);
    }
}

