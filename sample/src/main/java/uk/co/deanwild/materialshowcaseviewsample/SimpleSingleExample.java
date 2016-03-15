package uk.co.deanwild.materialshowcaseviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class SimpleSingleExample extends AppCompatActivity implements View.OnClickListener {

    private Button mButtonShow;
    private Button mButtonReset;

    private static final String SHOWCASE_ID = "simple example";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_single_example);
        mButtonShow = (Button) findViewById(R.id.btn_show);
        mButtonShow.setOnClickListener(this);

        mButtonReset = (Button) findViewById(R.id.btn_reset);
        mButtonReset.setOnClickListener(this);

        presentShowcaseView(1000); // one second delay
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
                .setTitleText("Hello")
                .setDismissText("GOT IT")
                .setContentText("This is some amazing feature you should know about")
                .setDelay(withDelay) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .show();
    }


}
