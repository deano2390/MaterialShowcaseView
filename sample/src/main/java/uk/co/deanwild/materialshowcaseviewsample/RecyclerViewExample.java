package uk.co.deanwild.materialshowcaseviewsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.Objects;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class RecyclerViewExample extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view_example);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("How to show on recycler view?");
        arrayList.add("How to show on recycler view?");
        arrayList.add("How to show on recycler view?");

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new RecyclerViewAdapter(this, arrayList));

        checkedFillAllData();

    }

    private void checkedFillAllData() {

        // It is used to run all the data in the list after it is filled.
        // Otherwise, it will not work because it cannot find the first line in direct use, and it will give an empty object error.
        recyclerView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        // I want to get the first line
                        RecyclerViewAdapter.MyViewHolder myViewHolder = (RecyclerViewAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(0);

                        // I catch ImageView object of the first line
                        View targetImageView = myViewHolder.imageView;
                        // I catch RelativeLayout object of the first line
                        View targetRelativeLayout = myViewHolder.relativeLayout;

                        // We show the objects we obtain sequentially
                        createShowCaseView(targetImageView, targetRelativeLayout);

                        // If you don't do it, it will always create
                        // Minimum Sdk Version should be 16
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }

    private void createShowCaseView(View imageView, View relativeLayout) {
        ShowcaseConfig config = new ShowcaseConfig();
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
        sequence.setConfig(config);

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(relativeLayout)
                .withRectangleShape()
                .setContentText("This shows the whole line")
                .setDismissText("Next")
                .setDismissOnTouch(true)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(imageView)
                .withCircleShape()
                .setContentText("OK")
                .setDismissText("This shows the imageView object in the entire row")
                .setDismissOnTouch(true)
                .build());

        sequence.start();
    }
}