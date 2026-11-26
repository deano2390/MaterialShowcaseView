package uk.co.deanwild.materialshowcaseviewsample

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

class SequenceExample : AppCompatActivity() {

    private lateinit var buttonOne: Button
    private lateinit var buttonTwo: Button
    private lateinit var buttonThree: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequence_example)
        padContentForSystemBars()

        buttonOne = findViewById(R.id.btn_one)
        buttonTwo = findViewById(R.id.btn_two)
        buttonThree = findViewById(R.id.btn_three)
        listOf(buttonOne, buttonTwo, buttonThree).forEach { it.setOnClickListener { presentShowcaseSequence() } }

        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID)
            Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show()
        }

        presentShowcaseSequence()
    }

    private fun presentShowcaseSequence() {
        val config = ShowcaseConfig().apply {
            delay = 500 // half a second between each showcase view
            dismissOnBackPress = true // back skips the rest of the sequence
        }

        val sequence = MaterialShowcaseSequence(this, SHOWCASE_ID)
        sequence.setOnItemShownListener { itemView, position ->
            Toast.makeText(itemView.context, "Item #$position", Toast.LENGTH_SHORT).show()
        }
        sequence.setOnSequenceFinishedListener { skipped ->
            val message = if (skipped) "Tutorial skipped" else "Tutorial finished"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        sequence.setConfig(config)

        sequence.addSequenceItem(buttonOne, "This is button one", "GOT IT")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setSkipText("SKIP")
                .setTarget(buttonTwo)
                .setDismissText("GOT IT")
                .setContentText("This is button two")
                .withRectangleShape(true)
                .build(),
        )

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(buttonThree)
                .setDismissText("GOT IT")
                .setContentText("This is button three")
                .withRoundedRectangleShape()
                .build(),
        )

        sequence.start()
    }

    companion object {
        const val SHOWCASE_ID = "sequence example"
    }
}
