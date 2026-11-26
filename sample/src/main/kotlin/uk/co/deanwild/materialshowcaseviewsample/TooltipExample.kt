package uk.co.deanwild.materialshowcaseviewsample

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseTooltip

class TooltipExample : AppCompatActivity() {

    private lateinit var fab: FloatingActionButton
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tooltip_example)
        padContentForSystemBars()

        fab = findViewById(R.id.fab)
        toolbar = findViewById(R.id.toolbar)

        findViewById<Button>(R.id.btn_show).setOnClickListener { presentShowcaseView() }
        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID)
            Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show()
        }

        presentShowcaseView()
    }

    private fun presentShowcaseView() {
        val sequence = MaterialShowcaseSequence(this, SHOWCASE_ID)
        val mask = ContextCompat.getColor(this, R.color.tooltip_mask)

        val toolTip1 = ShowcaseTooltip.build(this)
            .corner(30)
            .textColor(Color.parseColor("#007686"))
            .text(
                "This is a <b>very funky</b> tooltip<br><br>This is a very long sentence to test how this " +
                    "tooltip behaves with longer strings. <br><br>Tap anywhere to continue",
            )

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(toolbar)
                .setToolTip(toolTip1)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .setMaskColour(mask)
                .build(),
        )

        val toolTip2 = ShowcaseTooltip.build(this)
            .corner(30)
            .textColor(Color.parseColor("#007686"))
            .text("This is another <b>very funky</b> tooltip")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(fab)
                .setToolTip(toolTip2)
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .setMaskColour(mask)
                .build(),
        )

        sequence.start()
    }

    companion object {
        const val SHOWCASE_ID = "tooltip example"
    }
}
