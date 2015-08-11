package uk.co.deanwild.materialshowcaseviewsample;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

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
        }

        if(intent!=null){
            startActivity(intent);
        }
    }


}
