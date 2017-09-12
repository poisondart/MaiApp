package com.mai.nix.maiapp.navigation_fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.mai.nix.maiapp.ChooseGroupActivity;
import com.mai.nix.maiapp.R;
import com.mai.nix.maiapp.UserSettings;
import com.mai.nix.maiapp.WebViewActivity;

/**
 * Created by Nix on 03.08.2017.
 */

public class SettingsFragment extends PreferenceFragment implements android.preference.Preference.OnPreferenceClickListener {
    private Preference mGroupPreference;
    private Preference mClearSubjectsCache;
    private Preference mClearExamsCache;
    private ListPreference mFregSubjects;
    private ListPreference mFregExams;
    private ListPreference mLinks;
    private Preference mAbout;
    private Preference mMAI;
    private Preference mDev;
    private static final int REQUEST_CODE_GROUP = 0;
    private String mChoosenGroup;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_test);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        UserSettings.initialize(getActivity());
        mGroupPreference = getPreferenceManager().findPreference("pref_group");
        mClearSubjectsCache = getPreferenceScreen().findPreference("clear_cache_subj");
        mClearExamsCache = getPreferenceScreen().findPreference("clear_cache_ex");
        mFregSubjects = (ListPreference)getPreferenceScreen().findPreference("freg");
        mFregExams = (ListPreference)getPreferenceScreen().findPreference("freg_ex");
        mLinks = (ListPreference) getPreferenceScreen().findPreference("links");

        mLinks.setValue(UserSettings.getLinksPreference(getActivity()));
        mGroupPreference.setSummary(UserSettings.getGroup(getActivity()));
        mFregSubjects.setValue(UserSettings.getSubjectsUpdateFrequency(getActivity()));
        mFregExams.setValue(UserSettings.getExamsUpdateFrequency(getActivity()));

        mAbout = getPreferenceScreen().findPreference("about");
        mMAI = getPreferenceScreen().findPreference("go_mai");
        mDev = getPreferenceScreen().findPreference("go_dev");
        mGroupPreference.setOnPreferenceClickListener(this);
        mClearSubjectsCache.setOnPreferenceClickListener(this);
        mClearExamsCache.setOnPreferenceClickListener(this);
        mAbout.setOnPreferenceClickListener(this);
        mMAI.setOnPreferenceClickListener(this);
        mDev.setOnPreferenceClickListener(this);
        mFregSubjects.setOnPreferenceClickListener(this);
        mFregExams.setOnPreferenceClickListener(this);
        mLinks.setOnPreferenceClickListener(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onPreferenceClick(android.preference.Preference preference) {
        switch (preference.getKey()){
            case "pref_group":
                Intent i = ChooseGroupActivity.newIntent(getActivity(), false);
                startActivityForResult(i, REQUEST_CODE_GROUP);
                break;
            case "clear_cache_subj":
                Toast.makeText(getActivity(), R.string.clear_cache_subj_toast, Toast.LENGTH_SHORT).show();
                break;
            case "clear_cache_ex":
                Toast.makeText(getActivity(), R.string.clear_cache_exams_toast, Toast.LENGTH_SHORT).show();
                break;
            case "about":
                Toast.makeText(getActivity(), R.string.author, Toast.LENGTH_SHORT).show();
                break;
            case "go_mai":
                Uri uri = Uri.parse("http://mai.ru/");
                if(UserSettings.getLinksPreference(getActivity()).equals(UserSettings.ONLY_BROWSER)){
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }else{
                    Intent intent = WebViewActivity.newInstance(getActivity(), uri);
                    startActivity(intent);
                }
                break;
            case "go_dev":
                Toast.makeText(getActivity(), R.string.not_now, Toast.LENGTH_SHORT).show();
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
            UserSettings.setGroup(getActivity(), mChoosenGroup);
            mGroupPreference.setSummary(mChoosenGroup);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        UserSettings.setLinksPreference(getActivity(), mLinks.getValue());
        UserSettings.setSubjectsUpdateFrequency(getActivity(), mFregSubjects.getValue());
        UserSettings.setExamsUpdateFrequency(getActivity(), mFregExams.getValue());
    }
}