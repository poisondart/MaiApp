package com.mai.nix.maiapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.mai.nix.maiapp.model.SubjectBody;
import com.mai.nix.maiapp.model.SubjectHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Nix on 02.08.2017.
 */

public class ThisWeekFragment extends Fragment {
    private ExpandableListView mListView;
    private ArrayList<SubjectHeader> mGroups;
    private SubjectsExpListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Spinner mSpinner;
    private DataLab mDataLab;
    private String mCurrentGroup, mCurrentLink;
    private int mCurrentDay, mCurrentWeek;
    private Calendar mCalendar;
    private final String mLink = "https://mai.ru/education/schedule/detail.php?group=";
    private String mWeek = "1";
    private final String PLUS_WEEK = "&week=";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.shedule_subjects_layout, container, false);
        View header = inflater.inflate(R.layout.spinner_header, null);
        mCalendar = new GregorianCalendar();
        mCurrentDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mCurrentWeek = mCalendar.get(Calendar.WEEK_OF_MONTH);
        UserSettings.initialize(getContext());
        mDataLab = DataLab.get(getContext());
        mGroups = new ArrayList<>();
        mAdapter = new SubjectsExpListAdapter(getContext(), mGroups);
        mCurrentGroup = UserSettings.getGroup(getContext());
        mSpinner = (Spinner)header.findViewById(R.id.spinner);
        mSwipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.swiperefresh);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0){
                    mWeek = Integer.toString(i);
                    mCurrentLink = mLink.concat(mCurrentGroup).concat(PLUS_WEEK).concat(mWeek);
                    new MyThread(mCurrentLink, false).execute();
                }else if(mDataLab.isSubjectsTablesEmpty()){
                    mCurrentLink = mLink.concat(mCurrentGroup);
                    new MyThread(mCurrentLink, true).execute();
                }else if(UserSettings.getSubjectsUpdateFrequency(getContext()).equals(UserSettings.EVERY_DAY) &&
                        UserSettings.getDay(getContext()) != mCurrentDay){
                    UserSettings.setDay(getContext(), mCurrentDay);
                    mDataLab.clearSubjectsCache();
                    mCurrentLink = mLink.concat(mCurrentGroup);
                    new MyThread(mCurrentLink, true).execute();
                }else if(UserSettings.getSubjectsUpdateFrequency(getContext()).equals(UserSettings.EVERY_WEEK) &&
                        UserSettings.getWeek(getContext()) != mCurrentWeek){
                    UserSettings.setWeek(getContext(), mCurrentWeek);
                    mDataLab.clearSubjectsCache();
                    mCurrentLink = mLink.concat(mCurrentGroup);
                    new MyThread(mCurrentLink, true).execute();
                }else{
                    mGroups.clear();
                    mCurrentLink = mLink.concat(mCurrentGroup);
                    ArrayList<SubjectHeader> headers = new ArrayList<>();
                    headers.addAll(mDataLab.getHeaders());
                    for(SubjectHeader header : headers){
                        header.setChildren(mDataLab.getBodies(header.getUuid()));
                    }
                    mGroups.addAll(headers);
                    for(int j = 0; j < mGroups.size(); j++){
                        mListView.expandGroup(j);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mListView = (ExpandableListView)v.findViewById(R.id.exp);
        mListView.addHeaderView(header);
        mListView.setAdapter(mAdapter);
        for(int i = 0; i < mGroups.size(); i++){
            mListView.expandGroup(i);
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);

                if(mSpinner.getSelectedItemPosition() != 0){
                    mWeek = Integer.toString(mSpinner.getSelectedItemPosition());
                    mCurrentLink = mLink.concat(mCurrentGroup).concat(PLUS_WEEK).concat(mWeek);
                    new MyThread(mCurrentLink, false).execute();
                }else {
                    mDataLab.clearSubjectsCache();
                    mCurrentLink = mLink.concat(mCurrentGroup);
                    new MyThread(mCurrentLink, true).execute();
                }

            }
        });
        return v;
    }
    private class MyThread extends AsyncTask<String, Void, String> {
        private Document doc;
        private Elements primaries;
        private String final_link;
        private boolean isCaching;
        public MyThread() {
            super();
        }
        public MyThread(String link, boolean cache){
            final_link = link;
            isCaching = cache;
        }

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                doc = Jsoup.connect(final_link).get();
                primaries = doc.select("div[class=sc-table sc-table-day]");
                Log.d("link", final_link);
                mGroups.clear();
                for(Element prim : primaries){
                    String date = prim.select("div[class=sc-table-col sc-day-header sc-gray]").text();
                    if(date.isEmpty()){
                        date = prim.select("div[class=sc-table-col sc-day-header sc-blue]").text();
                    }
                    String day = prim.select("span[class=sc-day]").text();
                    SubjectHeader header = new SubjectHeader(date, day);
                    ArrayList<SubjectBody> bodies = new ArrayList<>();
                    Elements times = prim.select("div[class=sc-table-col sc-item-time]");
                    Elements types = prim.select("div[class=sc-table-col sc-item-type]");
                    Elements titles = prim.select("span[class=sc-title]");
                    Elements teachers = prim.select("div[class=sc-table-col sc-item-title]");
                    Elements rooms = prim.select("div[class=sc-table-col sc-item-location]");
                    for (int i = 0; i < times.size(); i++){
                        SubjectBody body = new SubjectBody(titles.get(i).text(),
                                teachers.get(i).select("span[class=sc-lecturer]").text(),
                                types.get(i).text(), times.get(i).text(), rooms.get(i).text());
                        body.setUuid(header.getUuid());
                        bodies.add(body);
                    }
                    header.setChildren(bodies);
                    mGroups.add(header);
                    if (isCaching){
                        mDataLab.addBodies(bodies);
                        mDataLab.addHeader(header);
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            mListView.setAdapter(mAdapter);
            mSwipeRefreshLayout.setRefreshing(false);
            for(int i = 0; i < mGroups.size(); i++){
                mListView.expandGroup(i);
            }
            if(isCaching)Toast.makeText(getContext(), R.string.cache_updated_message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentGroup = UserSettings.getGroup(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.go_web_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.go_web_in_frags) {
                Uri uri = Uri.parse(mCurrentLink);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
