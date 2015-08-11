package uk.co.deanwild.materialshowcaseviewsample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class SequenceExample extends ActionBarActivity implements View.OnClickListener {

    private Button mButtonOne;
    private Button mButtonTwo;
    private Button mButtonThree;

    private Button mButtonReset;

    private static final String SHOWCASE_ID = "sequence example";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence_example);
        mButtonOne = (Button) findViewById(R.id.btn_one);
        mButtonOne.setOnClickListener(this);

        mButtonTwo = (Button) findViewById(R.id.btn_two);
        mButtonTwo.setOnClickListener(this);

        mButtonThree = (Button) findViewById(R.id.btn_three);
        mButtonThree.setOnClickListener(this);

        mButtonReset = (Button) findViewById(R.id.btn_reset);
        mButtonReset.setOnClickListener(this);

        presentShowcaseSequence(); // one second delay
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_show) {

            presentShowcaseSequence();

        } else if (v.getId() == R.id.btn_reset) {

            MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID);
            Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show();
        }

    }

    private void presentShowcaseSequence() {

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(1000);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(mButtonOne,
                "This is button one", "GOT IT");

        sequence.addSequenceItem(mButtonTwo,
                "This is button two", "GOT IT");

        sequence.addSequenceItem(mButtonThree,
                "This is button three", "GOT IT");

        sequence.start();

    }

}
