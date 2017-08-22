package com.mai.nix.maiapp.navigation_fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.mai.nix.maiapp.ChooseGroupActivity;
import com.mai.nix.maiapp.R;

/**
 * Created by Nix on 03.08.2017.
 */

public class SettingsFragment extends PreferenceFragment implements android.preference.Preference.OnPreferenceClickListener {
    private Preference mGroupPreference;
    private Preference mClearSubjectsCache;
    private Preference mClearExamsCache;
    private Preference mAbout;
    private Preference mMAI;
    private static final int REQUEST_CODE_GROUP = 0;
    private String mChoosenGroup;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_test);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mGroupPreference = getPreferenceManager().findPreference("pref_group");
        mClearSubjectsCache = getPreferenceScreen().findPreference("clear_cache_subj");
        mClearExamsCache = getPreferenceScreen().findPreference("clear_cache_ex");
        mAbout = getPreferenceScreen().findPreference("about");
        mMAI = getPreferenceScreen().findPreference("go_mai");
        mGroupPreference.setOnPreferenceClickListener(this);
        mClearSubjectsCache.setOnPreferenceClickListener(this);
        mClearExamsCache.setOnPreferenceClickListener(this);
        mAbout.setOnPreferenceClickListener(this);
        mMAI.setOnPreferenceClickListener(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onPreferenceClick(android.preference.Preference preference) {
        switch (preference.getKey()){
            case "pref_group":
                Intent i = ChooseGroupActivity.newIntent(getActivity(), false);
                startActivityForResult(i, REQUEST_CODE_GROUP);
                break;
            /*case "clear_cache_subj":
                Toast.makeText(getActivity(), R.string.clear_cache_subj_toast, Toast.LENGTH_SHORT).show();
                break;
            /*case "clear_cache_ex":
                Toast.makeText(getActivity(), R.string.clear_cache_exams_toast, Toast.LENGTH_SHORT).show();
                break;*/
            case "about":
                Toast.makeText(getActivity(), R.string.author, Toast.LENGTH_SHORT).show();
                break;
            case "go_mai":
                Uri uri = Uri.parse("http://mai.ru/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_GROUP) {
            if (data == null) {
                return;
            }
            mChoosenGroup = data.getStringExtra(ChooseGroupActivity.EXTRA_GROUP);
            mGroupPreference.setSummary(mChoosenGroup);
        }
    }
}
