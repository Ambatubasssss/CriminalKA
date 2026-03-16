package com.example.criminalintent;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.text.DateFormat;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String ARG_IS_NEW_CRIME = "is_new_crime";

    private static final int REQUEST_READ_CONTACTS = 1;

    private Crime mCrime;
    private File mPhotoFile;
    private Uri mPhotoUri;
    private boolean mIsNewCrime;
    private boolean mWasAdded;

    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private EditText mSuspectField;
    private Button mAddCrimeButton;
    private Button mContactPoliceButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private Button mAssignPoliceButton;
    private LinearLayout mPoliceInfoContainer;
    private EditText mPoliceNameField;
    private EditText mPoliceNumberField;
    private Button mChooseSuspectButton;
    private Button mCallSuspectButton;

    private final ActivityResultLauncher<Void> mPickContact =
        registerForActivityResult(new ActivityResultContracts.PickContact(), this::onContactSelected);

    private final ActivityResultLauncher<Uri> mTakePhoto =
        registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onPhotoCaptured);

    public static CrimeFragment newInstance(UUID crimeId) {
        return newInstance(crimeId, false);
    }

    public static CrimeFragment newInstance(UUID crimeId, boolean isNewCrime) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        args.putBoolean(ARG_IS_NEW_CRIME, isNewCrime);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UUID crimeId = (UUID) requireArguments().getSerializable(ARG_CRIME_ID);
        mIsNewCrime = requireArguments().getBoolean(ARG_IS_NEW_CRIME, false);

        if (mIsNewCrime) {
            mCrime = new Crime(crimeId);
        } else {
            mCrime = CrimeLab.get(requireActivity()).getCrime(crimeId);
        }

        if (mCrime == null) {
            mCrime = new Crime(crimeId);
            mIsNewCrime = true;
        }

        if (savedInstanceState != null) {
            mWasAdded = savedInstanceState.getBoolean("was_added", false);
        }

        mPhotoFile = CrimeLab.get(requireActivity()).getPhotoFile(mCrime);
        mPhotoUri = FileProvider.getUriForFile(
                requireActivity(),
                requireActivity().getPackageName() + ".fileprovider",
                mPhotoFile
        );

        getParentFragmentManager().setFragmentResultListener(
                DatePickerFragment.REQUEST_DATE,
                this,
                (requestKey, bundle) -> {
                    if (bundle == null) return;
                    java.util.Date date = (java.util.Date) bundle.getSerializable(DatePickerFragment.RESULT_DATE);
                    if (date != null) {
                        mCrime.setDate(date);
                        updateDate();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = view.findViewById(R.id.crime_title);
        mDateButton = view.findViewById(R.id.crime_date);
        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mReportButton = view.findViewById(R.id.crime_report);
        mSuspectField = view.findViewById(R.id.crime_suspect);
        mAddCrimeButton = view.findViewById(R.id.crime_add);
        mContactPoliceButton = view.findViewById(R.id.contact_police_button);
        mPhotoButton = view.findViewById(R.id.crime_camera);
        mPhotoView = view.findViewById(R.id.crime_photo);
        mAssignPoliceButton = view.findViewById(R.id.assign_police_button);
        mPoliceInfoContainer = view.findViewById(R.id.police_info_container);
        mPoliceNameField = view.findViewById(R.id.police_name);
        mPoliceNumberField = view.findViewById(R.id.police_number);
        mChooseSuspectButton = view.findViewById(R.id.choose_suspect_button);
        mCallSuspectButton = view.findViewById(R.id.call_suspect_button);

        mTitleField.setText(mCrime.getTitle());
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSuspectField.setText(mCrime.getSuspect());
        mPoliceNameField.setText(mCrime.getPoliceName());
        mPoliceNumberField.setText(mCrime.getPoliceNumber());
        updateDate();
        updatePhotoView();
        updateCallSuspectButton();

        // Show police info container if data exists for existing crimes
        if (!mIsNewCrime && (!mCrime.getPoliceName().isEmpty() || !mCrime.getPoliceNumber().isEmpty())) {
            mPoliceInfoContainer.setVisibility(View.VISIBLE);
        }

        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mCrime.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSuspectField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mCrime.setSuspect(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mPoliceNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mCrime.setPoliceName(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mPoliceNumberField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mCrime.setPoliceNumber(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mChooseSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasReadContactsPermission()) {
                    launchPickContact();
                } else {
                    requestPermissions(
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_READ_CONTACTS
                    );
                }
            }
        });

        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String suspect = mCrime.getSuspect();
                String phone = mCrime.getSuspectPhone();
                if (suspect == null || suspect.isEmpty() || phone == null || phone.isEmpty()) {
                    return;
                }

                Uri uri = Uri.parse("tel:" + Uri.encode(phone));
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                PackageManager pm = requireActivity().getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent);
                }
            }
        });

        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                if (isChecked) {
                    new AlertDialog.Builder(requireContext())
                            .setMessage("na solved na ang case mo boy so close na ito eheh")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            }
        });

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.show(getParentFragmentManager(), DatePickerFragment.TAG);
            }
        });

        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                startActivity(Intent.createChooser(intent, getString(R.string.send_report)));
            }
        });

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mTakePhoto.launch(mPhotoUri);
                } catch (ActivityNotFoundException exception) {
                    Toast.makeText(requireActivity(), R.string.no_camera_app, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAssignPoliceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPoliceInfoContainer.setVisibility(
                        mPoliceInfoContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                );
            }
        });

        mContactPoliceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.contacting_police_title)
                        .setMessage(R.string.contacting_police_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });

        if (mIsNewCrime && !mWasAdded) {
            mAddCrimeButton.setVisibility(View.VISIBLE);
            mContactPoliceButton.setVisibility(View.GONE);
            mAddCrimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CrimeLab.get(requireActivity()).addCrime(mCrime);
                    mWasAdded = true;
                    requireActivity().finish();
                }
            });
        } else {
            mAddCrimeButton.setVisibility(View.GONE);
            mContactPoliceButton.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_delete_crime) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete crime")
                    .setMessage("Are you sure you want to delete this crime?")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        CrimeLab.get(requireActivity()).removeCrime(mCrime);
                        requireActivity().finish();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("was_added", mWasAdded);
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePhotoView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPhotoView.setImageDrawable(null);
    }

    private void updateDate() {
        mDateButton.setText(DateFormat.getDateInstance(DateFormat.FULL).format(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = DateFormat.getDateInstance(DateFormat.FULL).format(mCrime.getDate());

        String suspect = mCrime.getSuspect();
        String suspectString;
        if (suspect == null || suspect.isEmpty()) {
            suspectString = getString(R.string.crime_report_no_suspect);
        } else {
            suspectString = getString(R.string.crime_report_suspect, suspect);
        }

        String policeName = mCrime.getPoliceName();
        String policeNumber = mCrime.getPoliceNumber();
        String policeString = "";
        if (!policeName.isEmpty() || !policeNumber.isEmpty()) {
            policeString = "\nAssigned Police: " + policeName + " (" + policeNumber + ")";
        }

        return getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspectString) + policeString;
    }

    private void onContactSelected(Uri contactUri) {
        if (contactUri == null) {
            return;
        }

        PackageManager pm = requireActivity().getPackageManager();
        Intent pickIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        if (pickIntent.resolveActivity(pm) == null) {
            Toast.makeText(requireContext(), "No contacts app available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] queryFields = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        try (Cursor cursor = requireActivity()
                .getContentResolver()
                .query(contactUri, queryFields, null, null, null)) {

            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }

            String contactId = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String displayName = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

            mCrime.setSuspect(displayName);
            mSuspectField.setText(displayName);

            String phone = null;
            String[] phoneProjection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
            try (Cursor phoneCursor = requireActivity()
                    .getContentResolver()
                    .query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null
                    )) {
                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    phone = phoneCursor.getString(
                            phoneCursor.getColumnIndexOrThrow(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
            }

            mCrime.setSuspectPhone(phone != null ? phone : "");
            updateCallSuspectButton();
        }
    }

    private void onPhotoCaptured(Boolean didTakePhoto) {
        if (Boolean.TRUE.equals(didTakePhoto)) {
            updatePhotoView();
        }
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
            return;
        }

        int width = mPhotoView.getWidth();
        int height = mPhotoView.getHeight();

        if (width <= 0 || height <= 0) {
            mPhotoView.post(new Runnable() {
                @Override
                public void run() {
                    updatePhotoView();
                }
            });
            return;
        }

        Bitmap scaledBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), width, height);
        mPhotoView.setImageBitmap(scaledBitmap);
        mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
    }

    private boolean hasReadContactsPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void launchPickContact() {
        mPickContact.launch(null);
    }

    private void updateCallSuspectButton() {
        if (mCallSuspectButton == null) {
            return;
        }
        String suspect = mCrime.getSuspect();
        String phone = mCrime.getSuspectPhone();
        boolean enabled = suspect != null && !suspect.isEmpty()
                && phone != null && !phone.isEmpty();
        mCallSuspectButton.setEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mChooseSuspectButton.setEnabled(true);
                launchPickContact();
            } else {
                mChooseSuspectButton.setEnabled(false);
                mCallSuspectButton.setEnabled(false);
            }
        }
    }
}
