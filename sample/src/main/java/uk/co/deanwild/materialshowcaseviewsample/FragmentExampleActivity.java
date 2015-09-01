package uk.co.deanwild.materialshowcaseviewsample;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class FragmentExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_example);

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.add(R.id.fragment_container, Fragment1.newInstance());
        tx.commit();

    }



}
