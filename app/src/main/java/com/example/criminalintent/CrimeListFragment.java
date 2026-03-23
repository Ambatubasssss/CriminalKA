package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private static final int MAX_CRIMES = 10;
    private static final String[] LANGUAGE_CODES = {"en", "es"};
    private static final int LANGUAGE_ENGLISH_INDEX = 0;
    private static final int LANGUAGE_SPANISH_INDEX = 1;

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private View mEmptyView;
    private Button mEmptyAddButton;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeSelected(UUID crimeId);
        void onCreateNewCrimeRequested();
        void onCrimeDeleted(UUID crimeId);
        void onCrimeAdded(UUID crimeId);
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof Callbacks) {
            mCallbacks = (Callbacks) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mEmptyView = view.findViewById(R.id.empty_view);
        mEmptyAddButton = view.findViewById(R.id.empty_add_crime_button);

        mEmptyAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNewCrime();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        if (mAdapter == null) {
                            return;
                        }
                        int position = viewHolder.getBindingAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) {
                            return;
                        }
                        Crime crime = mAdapter.getCrimeAt(position);
                        CrimeLab.get(requireActivity()).removeCrime(crime);
                        if (mCallbacks != null) {
                            mCallbacks.onCrimeDeleted(crime.getId());
                        } else {
                            updateUI();
                        }
                    }
                }
        );
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem newCrimeItem = menu.findItem(R.id.new_crime);
        if (newCrimeItem != null) {
            newCrimeItem.setVisible(!isAtCrimeLimit());
        }

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.new_crime) {
            if (isAtCrimeLimit()) {
                return true;
            }
            requestNewCrime();
            return true;
        }

        if (item.getItemId() == R.id.show_subtitle) {
            mSubtitleVisible = !mSubtitleVisible;
            requireActivity().invalidateOptionsMenu();
            updateSubtitle();
            return true;
        }

        if (item.getItemId() == R.id.language_settings) {
            showLanguageDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(requireActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        if (crimes.isEmpty()) {
            mCrimeRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mCrimeRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }

        mEmptyAddButton.setVisibility(isAtCrimeLimit() ? View.GONE : View.VISIBLE);
        requireActivity().invalidateOptionsMenu();
        updateSubtitle();
    }

    public void refreshUI() {
        updateUI();
    }

    private boolean isAtCrimeLimit() {
        return CrimeLab.get(requireActivity()).getCrimes().size() >= MAX_CRIMES;
    }

    private void requestNewCrime() {
        if (mCallbacks != null) {
            mCallbacks.onCreateNewCrimeRequested();
            return;
        }
        Intent intent = CrimeActivity.newCrimeIntent(requireActivity());
        startActivity(intent);
    }

    private void showLanguageDialog() {
        String[] languageNames = {
                getString(R.string.language_english),
                getString(R.string.language_spanish)
        };

        int selectedIndex = getCurrentLanguageIndex();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.language_settings)
                .setSingleChoiceItems(languageNames, selectedIndex, (dialog, which) -> {
                    LocaleManager.setLocale(requireContext(), LANGUAGE_CODES[which]);
                    dialog.dismiss();
                    requireActivity().recreate();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int getCurrentLanguageIndex() {
        String currentLanguage = LocaleManager.getSavedLanguageCode(requireContext());
        if (LANGUAGE_CODES[LANGUAGE_SPANISH_INDEX].equals(currentLanguage)) {
            return LANGUAGE_SPANISH_INDEX;
        }
        return LANGUAGE_ENGLISH_INDEX;
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(requireActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount,
                crimeCount);

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        if (mSubtitleVisible) {
            actionBar.setSubtitle(subtitle);
        } else {
            actionBar.setSubtitle(null);
        }
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Crime mCrime;
        private final TextView mTitleTextView;
        private final TextView mDateTextView;
        private final TextView mSuspectTextView;
        private final ImageView mSolvedImageView;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));

            itemView.setOnClickListener(this);
            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSuspectTextView = itemView.findViewById(R.id.crime_suspect);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved_icon);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            
            String suspect = mCrime.getSuspect();
            if (suspect != null && !suspect.isEmpty()) {
                mSuspectTextView.setText("Suspect: " + suspect);
                mSuspectTextView.setVisibility(View.VISIBLE);
            } else {
                mSuspectTextView.setVisibility(View.GONE);
            }

            if (mCrime.isSolved()) {
                mSolvedImageView.setVisibility(View.VISIBLE);
            } else {
                mSolvedImageView.setVisibility(View.GONE);
            }

            if (mCrime.isSolved()) {
                itemView.setBackgroundResource(R.drawable.bg_list_item_card_solved);
            } else {
                itemView.setBackgroundResource(R.drawable.bg_list_item_card);
            }
        }

        @Override
        public void onClick(View view) {
            if (mCallbacks != null) {
                mCallbacks.onCrimeSelected(mCrime.getId());
                return;
            }
            Intent intent = CrimePagerActivity.newIntent(requireActivity(), mCrime.getId());
            startActivity(intent);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        public Crime getCrimeAt(int position) {
            return mCrimes.get(position);
        }
    }
}
