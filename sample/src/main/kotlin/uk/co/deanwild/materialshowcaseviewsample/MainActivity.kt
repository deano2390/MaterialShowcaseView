package uk.co.deanwild.materialshowcaseviewsample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        padContentForSystemBars()

        open(R.id.btn_simple_example, SimpleSingleExample::class.java)
        open(R.id.btn_custom_example, CustomExample::class.java)
        open(R.id.btn_sequence_example, SequenceExample::class.java)
        open(R.id.btn_tooltip_example, TooltipExample::class.java)

        findViewById<Button>(R.id.btn_reset_all).setOnClickListener {
            MaterialShowcaseView.resetAll(this)
            Toast.makeText(this, "All Showcases reset", Toast.LENGTH_SHORT).show()
        }
    }

    private fun open(buttonId: Int, activity: Class<*>) {
        findViewById<Button>(buttonId).setOnClickListener { startActivity(Intent(this, activity)) }
    }
}
