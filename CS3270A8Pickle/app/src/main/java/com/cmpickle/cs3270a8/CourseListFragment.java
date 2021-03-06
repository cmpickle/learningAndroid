package com.cmpickle.cs3270a8;


import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cmpickle.cs3270a8.courseDatabase.DatabaseHelper;
import com.cmpickle.cs3270a8.courseDatabase.CourseListTable;
import com.cmpickle.cs3270a8.CanvasObjects.Course;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;

import static com.cmpickle.cs3270a8.Authorization.AUTH_TOKEN;


/**
 * A simple {@link Fragment} subclass.
 */
public class CourseListFragment extends ListFragment implements FragmentManager.OnBackStackChangedListener {

    @BindView(R.id.add_fab)
    FloatingActionButton addFab;

    SimpleCursorAdapter adapter;

    CanvasObjects.Course[] courses;

    public CourseListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_list, container, false);
        ButterKnife.bind(this, view);

        new GetAllCourses().execute("");

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, new CourseEditFragment(), MainActivity.COURSE_EDIT_FRAGMENT).addToBackStack("courseEdit").commit();
            }
        });

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        MainActivity mainActivity = (MainActivity) getActivity();
        Log.d(CourseListFragment.class.getName(), "Setting fragment state CourseListFragment");
        mainActivity.state = MainActivity.COURSE_LIST_INT;

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long _id) {
                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                Cursor courseCursor = databaseHelper.getCourseById(_id);
                courseCursor.moveToFirst();
                String id = courseCursor.getString(courseCursor.getColumnIndex("id"));
                courseCursor.close();
                Course course = null;

                if(courses == null) {
                    return false;
                }
                for(Course c : courses) {
                    if(c.id.equals(id)) {
                        course = c;
                        break;
                    }
                }
                if(course == null)
                    return false;

                new GetClassAssignments().execute(course.id);

                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_course_list, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);
        Log.d(CourseListFragment.class.getName(), "The List Item that was clicked has an ID of " + id);

        FragmentManager fragmentManager = getFragmentManager();
        CourseViewFragment courseViewFragment = new CourseViewFragment();
        courseViewFragment.setArguments(args);
        fragmentManager.beginTransaction().replace(com.cmpickle.cs3270a8.R.id.fragment_container, courseViewFragment, MainActivity.COURSE_VIEW_FRAGMENT).addToBackStack("courseView").commit();
    }

    @Override
    public void onBackStackChanged() {
        MainActivity mainActivity = (MainActivity) getActivity();
        Log.d(CourseListFragment.class.getName(), "Setting fragment state CourseListFragment");
        mainActivity.state = MainActivity.COURSE_LIST_INT;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.import_canvas:
                new GetCanvasCourses().execute("");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetAllCourses extends AsyncTask<String, Integer, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {
            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
            Cursor cursor = databaseHelper.getAllCourses();
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

            String[] columns = new String[] {CourseListTable.COLUMN_NAME};
            int[] views = new int[] {android.R.id.text1};
            adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, cursor, columns, views, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            setListAdapter(adapter);

            Course[] newCourses = new Course[cursor.getCount()];
            if(cursor.moveToFirst()) {
                for(int i=0; i<cursor.getCount(); ++i) {
                    Log.d(CourseViewFragment.class.getName(), "Adding course: " + cursor.getString(cursor.getColumnIndex(CourseListTable.COLUMN_NAME)));
                    newCourses[i] = new Course(cursor.getString(
                            cursor.getColumnIndex(CourseListTable.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndex(CourseListTable.COLUMN_NAME)),
                            cursor.getString(cursor.getColumnIndex(CourseListTable.COLUMN_COURSE_CODE)),
                            cursor.getString(cursor.getColumnIndex(CourseListTable.COLUMN_START_AT)),
                            cursor.getString(cursor.getColumnIndex(CourseListTable.COLUMN_END_AT)));
                    cursor.moveToNext();
                }
            }
            courses = newCourses;
        }
    }

    public class GetCanvasCourses extends AsyncTask<String, Integer, String> {

        String AUTH_TOKEN = Authorization.AUTH_TOKEN;
        String rawJson = "";

        @Override
        protected String doInBackground(String... params) {
            Log.d(CourseListFragment.class.getName(), "In AsyncTask getCanvasCourses");
            try {
                URL url = new URL("https://canvas.instructure.com/api/v1/courses");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
                conn.connect();
                int status = conn.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        rawJson = br.readLine();
                        Log.d(CourseListFragment.class.getName(), "raw Json String Length = " + rawJson.length());
                        Log.d(CourseListFragment.class.getName(), "raw Json first 256 chars: " + rawJson.substring(0, 256));
                        Log.d(CourseListFragment.class.getName(), "raw Json last 256 chars: " + rawJson.substring(rawJson.length() - 256, rawJson.length()));
                }
            } catch (MalformedURLException e) {
                Log.d(CourseListFragment.class.getName(), "Malformed URL " + e.getMessage());
            } catch (IOException e) {
                Log.d(CourseListFragment.class.getName(), "IO Exception " + e.getMessage());
            } catch (Exception e) {
                Log.d(CourseListFragment.class.getName(), "" + e.getMessage());
            }
            return rawJson;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
            Log.d(CourseListFragment.class.getName(), "Clearing course database for sync process");
            databaseHelper.clearCourseTable();

            try {
                courses = jsonParse(result);
                for (CanvasObjects.Course course : courses) {
                    Log.d(CourseListFragment.class.getName(), "adding course to list: " + course.name);
                    if (course.name != null) {
                        databaseHelper.insertCourse(course.id, course.name, course.course_code, course.start_at, course.end_at);
                    }
                }
            } catch (Exception e) {
                Log.d(CourseListFragment.class.getName(), e.getMessage());
            }

            new GetAllCourses().execute("");
        }

        private CanvasObjects.Course[] jsonParse(String rawJson) {
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();

            CanvasObjects.Course[] courses = null;

            try {
                courses = gson.fromJson(rawJson, CanvasObjects.Course[].class);
                Log.d(CourseListFragment.class.getName(), "Number of courses returned is: " + courses.length);
                Log.d(CourseListFragment.class.getName(), "First Course returned is: " + courses[0].name);
            } catch (Exception e) {
                Log.d(CourseListFragment.class.getName(), e.getMessage());
            }

            return courses;
        }
    }

    public class GetClassAssignments extends AsyncTask<String, Integer, Bundle> {

        @Override
        protected Bundle doInBackground(String... params) {
            Log.d(CourseListFragment.class.getName(), "Searching for class assignments");
            String rawJson = "";
            try {
                URL url = new URL("https://canvas.instructure.com/api/v1/courses/"+params[0]+"/assignments");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
                Log.d(CourseListFragment.class.getName(), "GET " +"Bearer " + AUTH_TOKEN+ " " + url.toString());
                conn.connect();
                int status = conn.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        rawJson = br.readLine();
                        Log.d(CourseListFragment.class.getName(), "raw Json String Length = " + rawJson.length());
                        Log.d(CourseListFragment.class.getName(), "raw Json first 256 chars: " + rawJson.substring(0, 256));
                        Log.d(CourseListFragment.class.getName(), "raw Json last 256 chars: " + rawJson.substring(rawJson.length() - 256, rawJson.length()));
                }
            } catch (MalformedURLException e) {
                Log.d(CourseListFragment.class.getName(), "Malformed URL " + e.getMessage());
            } catch (IOException e) {
                Log.d(CourseListFragment.class.getName(), "IO Exception " + e.getMessage());
            } catch (Exception e) {
                Log.d(CourseListFragment.class.getName(), "" + e.getMessage());
            }

            CanvasObjects.Assignments[] assignments;

            Bundle args = new Bundle();
            args.putString("id", params[0]);
            Log.d(CourseListFragment.class.getName(), "The List Item that was long clicked has an ID of " + params[0]);

            try {
                assignments = jsonParse(rawJson);
                for (int i = 0; i < assignments.length; ++i) {
                    Log.d(CourseListFragment.class.getName(), "adding course to list: " + assignments[i].name);
                    if (assignments[i].name != null) {
                        args.putString("a"+i+1, assignments[i].name);
                    }
                }
                args.putInt("length", assignments.length);
            } catch (Exception e) {
                Log.d(CourseListFragment.class.getName(), e.getMessage());
            }
            return args;
        }

        @Override
        protected void onPostExecute(Bundle args) {
            super.onPostExecute(args);

            FragmentManager fragmentManager = getFragmentManager();
            CourseAssignmentFragment courseAssignmentFragment = new CourseAssignmentFragment();
            courseAssignmentFragment.setArguments(args);
            fragmentManager.beginTransaction().replace(com.cmpickle.cs3270a8.R.id.fragment_container, courseAssignmentFragment, MainActivity.COURSE_ASSIGNMENT_FRAGMENT).addToBackStack("courseAssignment").commit();

        }

        private CanvasObjects.Assignments[] jsonParse(String rawJson) {
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();

            CanvasObjects.Assignments[] assignmentes = null;

            try {
                assignmentes = gson.fromJson(rawJson, CanvasObjects.Assignments[].class);
                Log.d(CourseListFragment.class.getName(), "Number of courses returned is: " + assignmentes.length);
                Log.d(CourseListFragment.class.getName(), "First Course returned is: " + assignmentes[0].name);
            } catch (Exception e) {
                Log.d(CourseListFragment.class.getName(), e.getMessage());
            }

            return assignmentes;
        }
    }
}
