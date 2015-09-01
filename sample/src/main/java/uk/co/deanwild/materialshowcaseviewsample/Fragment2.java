package uk.co.deanwild.materialshowcaseviewsample;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class Fragment2 extends Fragment {


    private Button mButtonShow;

    public static Fragment2 newInstance() {
        Fragment2 fragment = new Fragment2();

        return fragment;
    }

    public Fragment2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment2, container, false);

        mButtonShow = (Button) view.findViewById(R.id.btn_feature);

        presentShowcaseView(0);

        return view;
    }


    private void presentShowcaseView(int withDelay) {
        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(mButtonShow)
                .setDismissText("GOT IT")
                .setContentText("This is some amazing feature you should know about")
                .setDelay(withDelay) // optional but starting animations immediately in onCreate can make them choppy
                        //.singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .show();
    }

}
