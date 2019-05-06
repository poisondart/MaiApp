package com.mai.nix.maiapp.expandable_list_fragments;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.mai.nix.maiapp.MainActivity;
import com.mai.nix.maiapp.R;
import com.mai.nix.maiapp.UserSettings;
import com.mai.nix.maiapp.WebViewActivity;
import com.mai.nix.maiapp.model.SubjectHeader;
import com.mai.nix.maiapp.viewmodels.ApplicationViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Nix on 02.08.2017.
 */

public class MyGroupScheduleSubjectsFragment extends Fragment {
    private ArrayList<SubjectHeader> mGroups;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ScheduleListAdapter mScheduleListAdapter;
    private String mCurrentGroup, mCurrentLink;
    private int mCurrentDay, mCurrentWeek;
    private Calendar mCalendar;
    private final String mLink = "https://mai.ru/education/schedule/detail.php?group=";
    private String mWeek = "1";
    private final String PLUS_WEEK = "&week=";

    private ApplicationViewModel mApplicationViewModel;
    private LiveData<List<SubjectHeader>> mLiveData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.shedule_subjects_layout, container, false);
        mCalendar = new GregorianCalendar();
        mCurrentDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mCurrentWeek = mCalendar.get(Calendar.WEEK_OF_MONTH);
        UserSettings.initialize(getContext());
        mGroups = new ArrayList<>();
        mCurrentGroup = UserSettings.getGroup(getContext());
        mSwipeRefreshLayout = v.findViewById(R.id.swiperefresh);
        mRecyclerView = v.findViewById(R.id.scheduleRecyclerView);
        mScheduleListAdapter = new ScheduleListAdapter();
        mRecyclerView.setAdapter(mScheduleListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mApplicationViewModel = ViewModelProviders.of(MyGroupScheduleSubjectsFragment.this)
                .get(ApplicationViewModel.class);
        mCurrentLink = mLink.concat(mCurrentGroup);
        mApplicationViewModel.initSubjectsRepository(mCurrentLink);
        mLiveData = mApplicationViewModel.getCachedSubjectsData();

        mLiveData.observe(MyGroupScheduleSubjectsFragment.this, new Observer<List<SubjectHeader>>() {
            @Override
            public void onChanged(@Nullable List<SubjectHeader> subjectHeaders) {
                mSwipeRefreshLayout.setRefreshing(false);
                mGroups.clear();
                mGroups.addAll(subjectHeaders);
                mScheduleListAdapter.setData(mGroups);
                mScheduleListAdapter.notifyDataSetChanged();
                Log.d("poisondart ", "onChanged");
            }
        });


        /*mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!((MainActivity) getActivity()).subjectsNeedToUpdate) {
                    if (i != 0) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        mWeek = Integer.toString(i);
                        mCurrentLink = mLink.concat(mCurrentGroup).concat(PLUS_WEEK).concat(mWeek);
                        mApplicationViewModel.initSubjectsRepository(mCurrentLink);
                        mLiveData = mApplicationViewModel.getCachedSubjectsData();
                    } else if (UserSettings.getSubjectsUpdateFrequency(getContext()).equals(UserSettings.EVERY_DAY) &&
                            UserSettings.getDay(getContext()) != mCurrentDay) {
                        UserSettings.setDay(getContext(), mCurrentDay);
                        mCurrentLink = mLink.concat(mCurrentGroup);
                        mApplicationViewModel.initSubjectsRepository(mCurrentLink);
                        mLiveData = mApplicationViewModel.getData();
                    } else if (UserSettings.getSubjectsUpdateFrequency(getContext()).equals(UserSettings.EVERY_WEEK) &&
                            UserSettings.getWeek(getContext()) != mCurrentWeek) {
                        UserSettings.setWeek(getContext(), mCurrentWeek);
                        mCurrentLink = mLink.concat(mCurrentGroup);
                        mApplicationViewModel.initSubjectsRepository(mCurrentLink);
                        mLiveData = mApplicationViewModel.getData();
                    } else {
                        //TODO Не будет работать пока
                        mCurrentLink = mLink.concat(mCurrentGroup);
                        mApplicationViewModel.initSubjectsRepository(mCurrentLink);
                        mLiveData = mApplicationViewModel.getCachedSubjectsData();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);

                /*if (mSpinner.getSelectedItemPosition() != 0) {
                    mWeek = Integer.toString(mSpinner.getSelectedItemPosition());
                    mCurrentLink = mLink.concat(mCurrentGroup).concat(PLUS_WEEK).concat(mWeek);
                    mApplicationViewModel.initSubjectsRepository(mCurrentLink);
                    mLiveData = mApplicationViewModel.getData();
                } else {
                    mCurrentLink = mLink.concat(mCurrentGroup);
                    mApplicationViewModel.initSubjectsRepository(mCurrentLink);
                    mLiveData = mApplicationViewModel.getCachedSubjectsData();
                }*/

            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((MainActivity) getActivity()).subjectsNeedToUpdate) {
            mCurrentGroup = UserSettings.getGroup(getContext());
            mCurrentLink = mLink.concat(mCurrentGroup);
            mApplicationViewModel.initSubjectsRepository(mCurrentLink);
            mLiveData = mApplicationViewModel.getCachedSubjectsData();
            ((MainActivity) getActivity()).subjectsNeedToUpdate = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share_button) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, mCurrentLink);
            i.putExtra(Intent.EXTRA_SUBJECT, mCurrentGroup);
            startActivity(Intent.createChooser(i, getString(R.string.share_subjects_link)));
        } else if (item.getItemId() == R.id.browser_button) {
            if (UserSettings.getLinksPreference(getContext()).equals(UserSettings.ONLY_BROWSER)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mCurrentLink));
                startActivity(intent);
            } else {
                Intent intent = WebViewActivity.newInstance(getContext(), Uri.parse(mCurrentLink));
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}