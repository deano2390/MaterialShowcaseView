package uk.co.deanwild.materialshowcaseviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class CustomExample extends AppCompatActivity implements View.OnClickListener {

    private Button mButtonShow;
    private Button mButtonReset;

    private static final String SHOWCASE_ID = "custom example";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_example);
        mButtonShow = (Button) findViewById(R.id.btn_show);
        mButtonShow.setOnClickListener(this);

        mButtonReset = (Button) findViewById(R.id.btn_reset);
        mButtonReset.setOnClickListener(this);

        presentShowcaseView(1000); // one second delay
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_custom_example, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_sample_action) {
            View view = findViewById(R.id.menu_sample_action);
            new MaterialShowcaseView.Builder(this)
                    .setTarget(view)
                    .setDismissText("GOT IT")
                    .setContentText("Example of how to show a showcase view for menu items in action bar.")
                    .setContentTextColor(getResources().getColor(R.color.green))
                    .setMaskColour(getResources().getColor(R.color.purple))
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_show) {

            presentShowcaseView(0);

        } else if (v.getId() == R.id.btn_reset) {

            MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID);
            Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show();
        }

    }

    private void presentShowcaseView(int withDelay) {
        new MaterialShowcaseView.Builder(this)
                .setTarget(mButtonShow)
                .setContentText("This is some amazing feature you should know about")
                .setDismissOnTouch(true)
                .setContentTextColor(getResources().getColor(R.color.green))
                .setMaskColour(getResources().getColor(R.color.purple))
                .setDelay(withDelay) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .show();
    }
}
