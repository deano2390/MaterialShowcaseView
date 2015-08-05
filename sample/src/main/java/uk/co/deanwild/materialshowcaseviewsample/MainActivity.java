package uk.co.deanwild.materialshowcaseviewsample;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= 21)
            // getTheme().applyStyle(android.R.attr.windowDrawsSystemBarBackgrounds, true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);
        presentShowcaseView(1000); // one second delay


    }

    @Override
    public void onClick(View v) {
        presentShowcaseView(0);
    }

    private void presentShowcaseView(int withDelay) {
        new MaterialShowcaseView.Builder(this)
                .setTarget(mButton)
                .setDismissText("GOT IT")
                .setContentText("This is some amazing feature you should know about")
                .setMaskColour(Color.parseColor("#bb660000"))
                .setContentTextColor(Color.parseColor("#bb006600"))
                .setContentTextColor(Color.parseColor("#bb000066"))
                .setDelay(withDelay)
                .build();
    }
}
