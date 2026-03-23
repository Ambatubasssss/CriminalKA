package com.example.criminalintent;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.UUID;

public class CrimeListActivity extends AppCompatActivity implements CrimeListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleManager.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_list);

        mTwoPane = findViewById(R.id.detail_fragment_container) != null;

        if (getSupportFragmentManager().findFragmentById(R.id.crime_list_container) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.crime_list_container, new CrimeListFragment())
                    .commit();
        }

        if (mTwoPane && savedInstanceState == null) {
            showInitialDetail();
        }
    }

    @Override
    public void onCrimeSelected(UUID crimeId) {
        if (mTwoPane) {
            Fragment detailFragment = CrimeFragment.newInstance(crimeId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_fragment_container, detailFragment)
                    .commit();
        } else {
            startActivity(CrimePagerActivity.newIntent(this, crimeId));
        }
    }

    @Override
    public void onCreateNewCrimeRequested() {
        if (mTwoPane) {
            UUID crimeId = UUID.randomUUID();
            Fragment detailFragment = CrimeFragment.newInstance(crimeId, true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_fragment_container, detailFragment)
                    .commit();
        } else {
            startActivity(CrimeActivity.newCrimeIntent(this));
        }
    }

    @Override
    public void onCrimeDeleted(UUID crimeId) {
        if (!mTwoPane) return;

        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.crime_list_container);
        if (listFragment != null) {
            listFragment.refreshUI();
        }

        List<Crime> crimes = CrimeLab.get(this).getCrimes();
        Fragment detailFragment;
        if (crimes.isEmpty()) {
            detailFragment = new EmptyDetailFragment();
        } else {
            detailFragment = CrimeFragment.newInstance(crimes.get(0).getId());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_fragment_container, detailFragment)
                .commit();
    }

    @Override
    public void onCrimeAdded(UUID crimeId) {
        if (!mTwoPane) return;

        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.crime_list_container);
        if (listFragment != null) {
            listFragment.refreshUI();
        }
    }

    private void showInitialDetail() {
        List<Crime> crimes = CrimeLab.get(this).getCrimes();
        Fragment detailFragment;
        if (crimes.isEmpty()) {
            detailFragment = new EmptyDetailFragment();
        } else {
            detailFragment = CrimeFragment.newInstance(crimes.get(0).getId());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_fragment_container, detailFragment)
                .commit();
    }
}
