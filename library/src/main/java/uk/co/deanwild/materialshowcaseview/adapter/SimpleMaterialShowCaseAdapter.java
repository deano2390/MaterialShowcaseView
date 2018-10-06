package uk.co.deanwild.materialshowcaseview.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;

/**
 * Created by TurboCoder (Yamil García Hernández) on 7/7/16.
 */
public class SimpleMaterialShowCaseAdapter extends MaterialShowCaseAdapter {

    public int[] ids;
    public String[] titles;
    public String[] contents;
    public String[] buttons;
    public String[] shapes;

    public SimpleMaterialShowCaseAdapter(Activity activity, TypedArray array, String id) {
        super(
                activity,
                MaterialShowCaseAdapter.SHOW_CASE_DELAY,
                id
        );
        Resources res = activity.getResources();
        TypedArray idz = res.obtainTypedArray(array.getResourceId(0, 0));
        this.ids = new int[idz.length()];
        for (int n = 0; n < idz.length(); n++)
            this.ids[n] = idz.getResourceId(n, 0);
        idz.recycle();
        this.titles = res.getStringArray(array.getResourceId(1, 0));
        this.contents = res.getStringArray(array.getResourceId(2, 0));
        this.buttons = res.getStringArray(array.getResourceId(3, 0));
        this.shapes = res.getStringArray(array.getResourceId(4, 0));
        array.recycle();

    }

    @Override
    public void setup() {

        for (int n = 0; n < ids.length; n++) {
            switch (shapes[n]) {
                case "circle":
                    addToQueue(ids[n], titles[n], contents[n], buttons[n], MaterialShowCaseViewShape.CIRCLE);
                    break;
                case "rectangle":
                    addToQueue(ids[n], titles[n], contents[n], buttons[n], MaterialShowCaseViewShape.RECTANGLE);
                    break;
            }
        }

    }
}