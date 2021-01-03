package net.cyclestreets.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import net.cyclestreets.view.R
import com.google.android.material.tabs.TabLayout
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.RoutePlans.PLAN_LEISURE
import net.cyclestreets.routing.Route
import java.util.*

class CircularRouteActivity : AppCompatActivity() {

    val CIRCULAR_ROUTE_MIN_MINUTES = 5
    val CIRCULAR_ROUTE_MAX_MINUTES = 200
    val CIRCULAR_ROUTE_MIN_MILES = 1
    val CIRCULAR_ROUTE_MAX_MILES = 30
    val CIRCULAR_ROUTE_MIN_KM = 1
    val CIRCULAR_ROUTE_MAX_KM = 50

    val units = CycleStreetsPreferences.units()
    val minKmOrMiles = if (units == "km") CIRCULAR_ROUTE_MIN_KM else CIRCULAR_ROUTE_MIN_MILES
    val maxKmOrMiles = if (units == "km") CIRCULAR_ROUTE_MAX_KM else CIRCULAR_ROUTE_MAX_MILES

    val minValues = arrayOf(CIRCULAR_ROUTE_MIN_MINUTES, minKmOrMiles)
    val maxValues = arrayOf(CIRCULAR_ROUTE_MAX_MINUTES, maxKmOrMiles)
    // Duration and distance values
    var values = arrayOf(CIRCULAR_ROUTE_MIN_MINUTES, 0)

    var storeValues = arrayOf(CIRCULAR_ROUTE_MIN_MINUTES, minKmOrMiles)
    private lateinit var currentValueUnit: Array<String>

    private lateinit var seekBarD:SeekBar
    private lateinit var minValueMins:TextView
    private lateinit var maxValueMins:TextView
    private lateinit var curValueTextView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circular_route)

        supportActionBar?.title = this.getString(R.string.circular_route)
        currentValueUnit = arrayOf(this.getString(R.string.mins), units)

        seekBarD = findViewById(R.id.seekBarDurationDistance)
        minValueMins = findViewById(R.id.seekBarMin)
        maxValueMins = findViewById(R.id.seekBarMax)
        curValueTextView = findViewById(R.id.currentValueTextView)

        doSeekBar(0)
        val aTabLayout = findViewById<TabLayout>(R.id.tablayout)

        aTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
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

    private fun doSeekBar(position: Int) {
        values[position] = storeValues[position]
        // If this is duration tab, set distance to 0 and vice versa:
        values[(position + 1) % 2] = 0

        minValueMins.text = minValues[position].toString()
        maxValueMins.text = maxValues[position].toString()
        // max must be set before progress
        seekBarD.max = maxValues[position] - minValues[position]
        // For below API level 26, seekBar doesn't have min attribute, so need to calculate progress
        seekBarD.progress = storeValues[position] - minValues[position]

        curValueTextView.text = "${storeValues[position]} ${currentValueUnit[position]}"

        seekBarD.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser == true)
                    curValueTextView.text = "${(minValues[position] + seekbar?.progress!!)} ${currentValueUnit[position]}"
            }
            override fun onStartTrackingTouch(seekbar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                values[position] = minValues[position] + seekbar?.progress!!
                curValueTextView.text = "${values[position]} ${currentValueUnit[position]}"
                storeValues[position] = values[position]
            }
        })

    }

    fun createButtonOnClick(view: View) {

        // Convert distance
        val metres: Int
        if (values[1] == 0) {
            metres = 0
        }
        else {
            metres = if (units.toLowerCase(Locale.ROOT) == "miles") {
                (values[1] * 8000 / 5)
                }
                else values[1] * 1000
        }

        val returnIntent = Intent()
        returnIntent.putExtra("circular_route_duration", values[0]*60)
        returnIntent.putExtra("circular_route_distance", metres)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

}