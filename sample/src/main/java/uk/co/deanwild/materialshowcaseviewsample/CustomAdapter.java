package uk.co.deanwild.materialshowcaseviewsample;

import android.app.Activity;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.adapter.MaterialShowCaseAdapter;

/**
 * Created by TurboCoder (Yamil García Hernández) on 11/7/16.
 */
public class CustomAdapter extends MaterialShowCaseAdapter {

    public CustomAdapter(Activity activity) {
        super(
                activity,
                MaterialShowCaseAdapter.SHOW_CASE_DELAY, // DELAY
                "ActivityAdapterExample" // ID
        );
    }

    @Override // Setup of all show cases views
    public void setup() {
        super.addToQueue(R.id.btn_one, "", "This is button one","GOT IT", MaterialShowCaseViewShape.CIRCLE);
        super.addToQueue(R.id.btn_two, "", "This is button two","GOT IT", MaterialShowCaseViewShape.RECTANGLE);
        super.addToQueue(R.id.btn_three, "", "This is button three","GOT IT", MaterialShowCaseViewShape.RECTANGLE);
    }

    @Override // Behavior
    public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
        super.onDismiss(materialShowcaseView, i);
        switch (currentQueueItemId){
            case 1:
                Toast.makeText(activity, "You GOT Button One, nice!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
