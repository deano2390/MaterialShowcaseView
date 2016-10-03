package uk.co.deanwild.materialshowcaseviewsample;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.ViewTimesPerfManager;

/**
 * This is an example about showing a single showcase and only shows for the second time when user
 * sees the target. When user sees the UI for the first time, it does not show the showcase UI.
 */
public class CustomSingleExampleWithShownOnlyOnSecondTime extends AppCompatActivity {
    private static final String SHOWCASE_ID = "onboarding_nux";
    private static final int DELAY = 1000;

    private Button mButtonShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_single_with_shown_only_on_second_time_example);
        mButtonShow = (Button) findViewById(R.id.btn_show);
        presentShowcaseView(DELAY);
    }

    private void presentShowcaseView(int withDelay) {
        ViewTimesPerfManager manager = new ViewTimesPerfManager(getApplicationContext(), SHOWCASE_ID);
        manager.setHasSeen();

        if (!manager.hasSeenFirstTime()) {
            return;
        }

        View.OnClickListener targetListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Clicking on the target view", Toast.LENGTH_LONG).show();
            }
        };

        MaterialShowcaseWithCustomView.Builder builder =
            new MaterialShowcaseWithCustomView.CustomBuilder(this)
            .setTarget(mButtonShow)
            .setTitleText("These are some amazing features you shall try to use")
            .setDismissText("Got it!")
            .setMaskColour(ContextCompat.getColor(this, R.color.white_80))
            .setTargetTouchable(true)
            .setDismissOnTargetTouch(false)
            .setShapePadding(50)
            .setDismissOnTouch(true)
            .setDelay(withDelay);

        ((MaterialShowcaseWithCustomView.CustomBuilder)builder)
            .setTargetViewOnclickListener(targetListener)
            .setTargetBorderWidth(20)
            .show();
    }
}
