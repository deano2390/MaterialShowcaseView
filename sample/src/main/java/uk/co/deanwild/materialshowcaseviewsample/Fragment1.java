package uk.co.deanwild.materialshowcaseviewsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment1 extends Fragment implements View.OnClickListener {

    public static Fragment1 newInstance() {
        Fragment1 fragment = new Fragment1();

        return fragment;
    }

    public Fragment1() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment1, container, false);

        Button button = (Button) view.findViewById(R.id.btn_next_fragment);
        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_next_fragment) {
            FragmentTransaction tx = getActivity().getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.fragment_container, Fragment2.newInstance());
            tx.addToBackStack("fragment2");
            tx.commit();
        }
    }
}
