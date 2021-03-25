package net.cyclestreets.views

import android.annotation.SuppressLint
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
import net.cyclestreets.EXTRA_CIRCULAR_ROUTE_POI_CATEGORIES
import net.cyclestreets.views.CircularRouteViewModel.Companion.DURATION
import net.cyclestreets.api.POICategories
import net.cyclestreets.util.Dialog
import net.cyclestreets.views.overlay.POIOverlay

class CircularRouteActivity : AppCompatActivity() {

    private lateinit var viewModel: CircularRouteViewModel

    private lateinit var seekBar: SeekBar
    private lateinit var seekBarMin: TextView
    private lateinit var seekBarMax: TextView
    private lateinit var currentValue: TextView
    private lateinit var poiTextView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circular_route)
        viewModel = ViewModelProvider(this).get(CircularRouteViewModel::class.java)

        supportActionBar?.title = this.getString(R.string.circular_route)
        viewModel.units[DURATION] = this.getString(R.string.mins)

        seekBar = findViewById(R.id.circularRouteSeekBar)
        seekBarMin = findViewById(R.id.circularRouteSeekBarMin)
        seekBarMax = findViewById(R.id.circularRouteSeekBarMax)
        currentValue = findViewById(R.id.circularRouteCurrentValue)
        poiTextView = findViewById(R.id.circularRoutePoiTextView)

        poiTextView.text = String.format(this.getString(R.string.num_poitypes_selected), viewModel.activeCategories.count())
        val durationOrDistanceTab = findViewById<TabLayout>(R.id.circularRouteDurationOrDistanceTab)
        // If screen has been rotated, get previously-selected tab and make sure it is selected
        durationOrDistanceTab.getTabAt(viewModel.currentTab)?.let { tab ->
            tab.select()
            initSeekBar(viewModel.currentTab)
        }

        durationOrDistanceTab.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewModel.currentTab = tab.position
                    initSeekBar(tab.position)
                }
            }
            // These two overrides are needed even if not used
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
    }

    // A single Seekbar is used for both duration and distance
    // Set values and unit according to which tab is selected
    @SuppressLint("SetTextI18n")
    private fun initSeekBar(currentTab: Int) {
        seekBarMin.text = viewModel.minValues[currentTab].toString()
        seekBarMax.text = viewModel.maxValues[currentTab].toString()

        // Below API level 26, seekBar doesn't have `min` attribute, so need to calculate `progress`
        // Note that `max` must be set before `progress`.
        seekBar.max = viewModel.maxValues[currentTab] - viewModel.minValues[currentTab]
        seekBar.progress = viewModel.values[currentTab] - viewModel.minValues[currentTab]
        // Show current value of slider, e.g. 15 mins, 10 km
        currentValue.text = "${viewModel.values[currentTab]} ${viewModel.units[currentTab]}"

        seekBar.setOnSeekBarChangeListener(CircularRouteSeekBarChangeListener(currentTab))
    }

    inner class CircularRouteSeekBarChangeListener(private val currentTab: Int) : SeekBar.OnSeekBarChangeListener {
        @SuppressLint("SetTextI18n")
        override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser)
                currentValue.text = "${(viewModel.minValues[currentTab] + seekbar?.progress!!)} ${viewModel.units[currentTab]}"
        }

        override fun onStartTrackingTouch(seekbar: SeekBar?) {}

        @SuppressLint("SetTextI18n")
        override fun onStopTrackingTouch(seekbar: SeekBar?) {
            viewModel.values[currentTab] = viewModel.minValues[currentTab] + seekbar?.progress!!
            currentValue.text = "${viewModel.values[currentTab]} ${viewModel.units[currentTab]}"
        }
    }

    fun poiButtonOnClick(view: View) {

        val poiAdapter = POIOverlay.POICategoryAdapter(this, POICategories.get(), viewModel.activeCategories)

        Dialog.listViewDialog(this, R.string.poi_menu_title, poiAdapter,
                { _, _ ->
                    viewModel.activeCategories = poiAdapter.chosenCategories()
                    poiTextView.text = String.format(this.getString(R.string.num_poitypes_selected), viewModel.activeCategories.count())
                },
                { _, _ ->
                })
    }

    fun circularRouteGoButtonClick(view: View) {
        val returnIntent = Intent().apply {

            if (viewModel.currentTab == DURATION)
                putExtra(EXTRA_CIRCULAR_ROUTE_DURATION, viewModel.durationInSeconds())
            else
                putExtra(EXTRA_CIRCULAR_ROUTE_DISTANCE, viewModel.distanceInMetres())

            if (viewModel.activeCategories.isNotEmpty())
                putExtra(EXTRA_CIRCULAR_ROUTE_POI_CATEGORIES,
                         viewModel.activeCategories.joinToString(separator = ",") {it.key})
        }
        setResult(RESULT_OK, returnIntent)
        finish()
    }

}
