package uk.co.deanwild.materialshowcaseviewsample

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView

class CustomExample : AppCompatActivity() {

    private lateinit var buttonShow: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_example)
        padContentForSystemBars()

        buttonShow = findViewById(R.id.btn_show)
        buttonShow.setOnClickListener { presentShowcaseView(0) }

        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID)
            Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show()
        }

        presentShowcaseView(1000) // one second delay
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_custom_example, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_sample_action) {
            // Action bar items are ordinary views once the menu is laid out.
            val view = findViewById<View>(R.id.menu_sample_action)
            MaterialShowcaseView.Builder(this)
                .setTarget(view)
                .setShapePadding(96)
                .setDismissText("GOT IT")
                .setContentText("Example of how to setup a MaterialShowcaseView for menu items in action bar.")
                .setContentTextColor(ContextCompat.getColor(this, R.color.green))
                .setMaskColour(ContextCompat.getColor(this, R.color.purple))
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun presentShowcaseView(withDelay: Int) {
        MaterialShowcaseView.Builder(this)
            .setTarget(buttonShow)
            .setContentText("This is some amazing feature you should know about")
            .setDismissText("GOT IT")
            .setDismissOnTouch(true)
            .setContentTextColor(ContextCompat.getColor(this, R.color.green))
            .setContentTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            .setContentStyle(Typeface.DEFAULT_BOLD)
            .setMaskColour(ContextCompat.getColor(this, R.color.purple))
            .setDelay(withDelay) // optional, but starting animations immediately in onCreate can make them choppy
            .singleUse(SHOWCASE_ID) // a unique ID used to make sure it is only shown once
            .show()
    }

    companion object {
        const val SHOWCASE_ID = "custom example"
    }
}
