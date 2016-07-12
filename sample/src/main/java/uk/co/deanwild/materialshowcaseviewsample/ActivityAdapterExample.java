package uk.co.deanwild.materialshowcaseviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.adapter.MaterialShowCaseAdapter;

/**
 * Created by TurboCoder (Yamil García Hernández) on 11/7/16.
 */
public class ActivityAdapterExample extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence_example);
        findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // With Pool Management to run 1 Sequence or 1 View at a time.
        // Material Show Case Initialization
        MaterialShowCaseAdapter.initialize(this, CustomAdapter.class);
        // Simple Material Show Case Initialization
        MaterialShowCaseAdapter.initialize(this, R.array.showcase_simple_activity_adapter_example, "SimpleActivityAdapterExample");
    }

    protected void reset() {
        MaterialShowcaseView.resetSingleUse(this, "ActivityAdapterExample");
        MaterialShowcaseView.resetSingleUse(this, "SimpleActivityAdapterExample");
        Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show();
    }
}
