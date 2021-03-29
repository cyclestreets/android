package net.cyclestreets.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import net.cyclestreets.view.R
import com.google.android.material.tabs.TabLayout
import net.cyclestreets.EXTRA_CIRCULAR_ROUTE_DISTANCE
import net.cyclestreets.EXTRA_CIRCULAR_ROUTE_DURATION

class CircularRouteActivity : AppCompatActivity() {

    private lateinit var viewModel: CircularRouteViewModel

    private lateinit var seekBarD:SeekBar
    private lateinit var minValueMins:TextView
    private lateinit var maxValueMins:TextView
    private lateinit var curValueTextView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circular_route)
        viewModel = ViewModelProvider(this).get(CircularRouteViewModel::class.java)

        supportActionBar?.title = this.getString(R.string.circular_route)
        viewModel.currentValueUnit = arrayOf(this.getString(R.string.mins), viewModel.units)

        seekBarD = findViewById(R.id.seekBarDurationDistance)
        minValueMins = findViewById(R.id.seekBarMin)
        maxValueMins = findViewById(R.id.seekBarMax)
        curValueTextView = findViewById(R.id.currentValueTextView)

        val aTabLayout = findViewById<TabLayout>(R.id.tablayout)
        // If screen has been rotated, get previously-selected tab and make sure it is selected
        val tab = aTabLayout.getTabAt(viewModel.position)
        if (tab != null) {
            tab.select()
            doSeekBar(viewModel.position)
        }

        aTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewModel.position = tab.position
                    doSeekBar(tab.position)
                }
            }
            // These two overrides are needed even if not used
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    // A single Seekbar is used for both duration and distance
    // Set values and unit according to which tab is selected
    private fun doSeekBar(position: Int) {
        viewModel.values[position] = viewModel.storeValues[position]
        // If this is duration tab, set distance to 0 and vice versa:
        viewModel.values[(position + 1) % 2] = 0

        minValueMins.text = viewModel.minValues[position].toString()
        maxValueMins.text = viewModel.maxValues[position].toString()
        // max must be set before progress
        seekBarD.max = viewModel.maxValues[position] - viewModel.minValues[position]
        // Below API level 26, seekBar doesn't have min attribute, so need to calculate progress
        seekBarD.progress = viewModel.storeValues[position] - viewModel.minValues[position]
        // Show current value of slider, e.g. 15 mins, 10 km
        curValueTextView.text = "${viewModel.storeValues[position]} ${viewModel.currentValueUnit[position]}"

        seekBarD.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser == true)
                    curValueTextView.text = "${(viewModel.minValues[position] + seekbar?.progress!!)} ${viewModel.currentValueUnit[position]}"
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                viewModel.values[position] = viewModel.minValues[position] + seekbar?.progress!!
                curValueTextView.text = "${viewModel.values[position]} ${viewModel.currentValueUnit[position]}"
                viewModel.storeValues[position] = viewModel.values[position]
            }
        })

    }

    fun createButtonOnClick(view: View) {

        val returnIntent = Intent().apply {
            putExtra(EXTRA_CIRCULAR_ROUTE_DURATION, viewModel.values[0] * 60)
            putExtra(EXTRA_CIRCULAR_ROUTE_DISTANCE, viewModel.distanceInMetres())
        }
        setResult(RESULT_OK, returnIntent)
        finish()
    }

}