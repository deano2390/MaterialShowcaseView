package uk.co.deanwild.materialshowcaseviewsample;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.btn_simple_example);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.btn_custom_example);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.btn_sequence_example);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.btn_simple_with_custom_view_example);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.btn_reset_all);
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        Intent intent = null;

        switch (v.getId()) {
            case R.id.btn_simple_example:
                intent = new Intent(this, SimpleSingleExample.class);
                break;

            case R.id.btn_custom_example:
                intent = new Intent(this, CustomExample.class);
                break;

            case R.id.btn_sequence_example:
                intent = new Intent(this, SequenceExample.class);
                break;

            case R.id.btn_simple_with_custom_view_example:
                intent = new Intent(this, CustomSingleExampleWithShownOnlyOnSecondTime.class);
                break;

            case R.id.btn_reset_all:
                MaterialShowcaseView.resetAll(this);
                Toast.makeText(this, "All Showcases reset", Toast.LENGTH_SHORT).show();
                break;
        }

        if(intent!=null){
            startActivity(intent);
        }
    }


}
