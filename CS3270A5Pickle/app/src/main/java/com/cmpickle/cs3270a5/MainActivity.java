package com.cmpickle.cs3270a5;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.changeResultsContainer, new ChangeResultsFragment(), "changeResultsFragment");
        fragmentTransaction.replace(R.id.changeButtonsContainer, new ChangeButtonsFragment(), "changeButtonsFragment");
        fragmentTransaction.replace(R.id.changeActionsContainer, new ChangeActionsFragment(), "changeActionsFragment");
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.zero_correct_count) {
            return true;
        }
        if(id == R.id.set_change_max) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.changeResultsContainer, new SetChangeMaxFragment(), "setChangeMaxFragment");
            fragmentTransaction.remove(fragmentManager.findFragmentByTag("changeButtonsFragment"));
            fragmentTransaction.remove(fragmentManager.findFragmentByTag("changeActionsFragment"));
            fragmentTransaction.addToBackStack("setChangeMaxView");
            fragmentTransaction.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateChangeTotalSoFar(double amount) {
        ChangeResultsFragment changeResultsFragment = (ChangeResultsFragment) getFragmentManager().findFragmentByTag("changeResultsFragment");
        changeResultsFragment.updateChangeTotalSoFar(amount);
    }
}
