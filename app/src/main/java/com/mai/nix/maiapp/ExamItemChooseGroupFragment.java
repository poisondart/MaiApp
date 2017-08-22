package com.mai.nix.maiapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mai.nix.maiapp.model.ExamModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nix on 17.08.2017.
 */

public class ExamItemChooseGroupFragment extends Fragment {
    private ListView mListView;
    private ArrayList<ExamModel> mExamModels;
    private ExamAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mButton;
    private static final int REQUEST_CODE_GROUP = 0;
    private final String mLink = "http://mai.ru/education/schedule/session.php?group=";
    private String mSelectedGroup;
    private TextView mChoosenGroupTextView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.student_orgs_layout, container, false);
        View header = inflater.inflate(R.layout.choose_group_ex_header, null);
        mButton = (TextView) header.findViewById(R.id.choose_view);
        mChoosenGroupTextView = (TextView)header.findViewById(R.id.group_view) ;
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ChooseGroupActivity.newIntent(getContext(), false);
                startActivityForResult(intent, REQUEST_CODE_GROUP);
            }
        });
        mExamModels = new ArrayList<>();
        mProgressBar = (ProgressBar)v.findViewById(R.id.progress_bar);
        mListView = (ListView) v.findViewById(R.id.stud_org_listview);
        mAdapter = new ExamAdapter(getContext(), mExamModels);
        //new MyThread().execute();
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        mListView.addHeaderView(header);
        mListView.setAdapter(mAdapter);
        return v;
    }
    private class MyThread extends AsyncTask<String, Void, String> {
        private Elements date, day, time, title, teacher, room;
        private Document doc;
        public MyThread() {
            super();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                doc = Jsoup.connect(mLink.concat(mSelectedGroup)).get();
                date = doc.select("div[class=sc-table-col sc-day-header sc-gray]");
                day = doc.select("span[class=sc-day]");
                time = doc.select("div[class=sc-table-col sc-item-time]");
                title = doc.select("span[class=sc-title]");
                teacher = doc.select("div[class=sc-table-col sc-item-title]");
                room = doc.select("div[class=sc-table-col sc-item-location]");
                mExamModels.clear();
                int kek = teacher.size();
                Log.d("teacher.size = ", Integer.toString(kek));
                for (int i = 0; i < kek; i++){
                    mExamModels.add(new ExamModel(date.get(i).text(), day.get(i).text(), time.get(i).text(), title.get(i).text(),
                            teacher.get(i).select("span[class=sc-lecturer]").text(), room.get(i).text()));
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            mListView.setAdapter(mAdapter);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        }
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
            mSelectedGroup = data.getStringExtra(ChooseGroupActivity.EXTRA_GROUP);
            mChoosenGroupTextView.setText(mSelectedGroup);
            mExamModels.clear();
            mAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            new MyThread().execute();
        }
    }
}
