package uk.co.deanwild.materialshowcaseviewsample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView

/** Shows a sequence over a dialog's buttons. */
class DialogExample : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_example)
        padContentForSystemBars()

        findViewById<Button>(R.id.btn_show_dialog).setOnClickListener { showDialog() }
        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID)
        }
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete photo?")
            .setMessage("This can't be undone.")
            .setPositiveButton("Delete", null)
            .setNegativeButton("Cancel", null)
            .show()

        val sequence = MaterialShowcaseSequence(this, SHOWCASE_ID)
        sequence.setDialog(dialog)
        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(dialog.getButton(AlertDialog.BUTTON_POSITIVE))
                .withRoundedRectangleShape()
                .setContentText("Delete removes the photo from every device")
                .setDismissText("GOT IT")
                .build(),
        )
        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(dialog.getButton(AlertDialog.BUTTON_NEGATIVE))
                .withRoundedRectangleShape()
                .setContentText("Or keep it with Cancel")
                .setDismissText("GOT IT")
                .build(),
        )
        sequence.start()
    }

    companion object {
        const val SHOWCASE_ID = "dialog example"
    }
}
