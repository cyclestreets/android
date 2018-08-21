package net.cyclestreets.addphoto

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.media.ExifInterface
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import net.cyclestreets.AccountDetailsActivity
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.Undoable
import net.cyclestreets.api.PhotomapCategories
import net.cyclestreets.api.Upload
import net.cyclestreets.fragments.R
import net.cyclestreets.iconics.IconicsHelper
import net.cyclestreets.util.*
import net.cyclestreets.util.MenuHelper.createMenuItem
import net.cyclestreets.util.MenuHelper.enableMenuItem
import net.cyclestreets.views.CycleMapView
import net.cyclestreets.views.overlay.ThereOverlay
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import java.io.File
import java.util.*

internal val TAG = Logging.getTag(AddPhotoFragment::class.java)

class AddPhotoFragment : Fragment(), View.OnClickListener, Undoable, ThereOverlay.LocationListener {
    // Android classes
    private lateinit var inflater: LayoutInflater
    private lateinit var inputMethodManager: InputMethodManager

    // Package configuration
    private var allowUploadByKey: Boolean = false
    private var allowTextOnly: Boolean = false
    private var noShare: Boolean = false

    // Views for each step in the Add Photo process
    private lateinit var photoRoot: LinearLayout
    private lateinit var photo1Start: View
    private var photo2Caption: View? = null
    private lateinit var photo3Category: View
    private lateinit var photo4Location: View
    private lateinit var photo5View: View

    // Location view/overlay
    private var map: CycleMapView? = null
    private lateinit var there: ThereOverlay

    // State
    private var step: AddStep = AddStep.START
    private var photoFilename: String? = null
    private var photo: Bitmap? = null
    private var dateTime: String? = ""
    private lateinit var caption: String
    private var metaCatId: Int = -1
    private var catId: Int = -1
    private var geolocated: Boolean = false
    private var uploadedUrl: String? = null

    // Drawables
    private var restartDrawable: Drawable? = null

    companion object {
        private var photomapCategories: PhotomapCategories? = null
    }

    ///////////// Fragment methods - views
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.inflater = LayoutInflater.from(activity)
        inputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        initialiseDrawables(this.inflater)
        initialiseFromMetadata()
        initialiseViews(this.inflater)
        caption = ""

        there = ThereOverlay(activity)
        there.setLocationListener(this)

        setupView()

        return photoRoot
    }

    private fun initialiseDrawables(inflater: Any) {
        if (restartDrawable == null) {
            val colorFunction: (Context) -> Int = { cxt: Context -> Theme.lowlightColorInverse(cxt) }
            restartDrawable = IconicsHelper.drawable(inflater, GoogleMaterial.Icon.gmd_replay, colorFunction)?.sizeDp(24)
        }
    }

    private fun initialiseFromMetadata() {
        val metaData = photoUploadMetaData(activity)
        allowUploadByKey = metaData.contains("ByKey")
        allowTextOnly = metaData.contains("AllowTextOnly")
        noShare = metaData.contains("NoShare")
    }

    private fun initialiseViews(inflater: LayoutInflater) {
        photoRoot = inflater.inflate(R.layout.addphoto_root, null) as LinearLayout

        photo1Start = inflater.inflate(R.layout.addphoto_1_start, null)
        (photo1Start.findViewById<View>(R.id.takephoto_button) as Button).apply {
            setOnClickListener(this@AddPhotoFragment)
            isEnabled = activity!!.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }
        photo1Start.findViewById<View>(R.id.chooseexisting_button).setOnClickListener(this)
        (photo1Start.findViewById<View>(R.id.textonly_button) as Button).apply {
            setOnClickListener(this@AddPhotoFragment)
            if (!allowTextOnly) visibility = View.GONE
        }

        //photo2CaptionView is recreated each time

        photo3Category = inflater.inflate(R.layout.addphoto_3_category, null)
        backNextButtons(photo3Category,
                        getString(R.string.all_button_back), android.R.drawable.ic_media_rew,
                        getString(R.string.all_button_next), android.R.drawable.ic_media_ff)
        if (photomapCategories == null)
            GetPhotomapCategoriesTask().execute()
        else
            setupSpinners()

        photo4Location = inflater.inflate(R.layout.addphoto_4_location, null)
        backNextButtons(photo4Location,
                        getString(R.string.all_button_back), android.R.drawable.ic_media_rew,
                        "Upload!", android.R.drawable.ic_menu_upload)

        photo5View = inflater.inflate(R.layout.addphoto_5_view, null)
        backNextButtons(photo5View,
                        "Upload another", android.R.drawable.ic_menu_revert,
                        "Close", android.R.drawable.ic_menu_close_clear_cancel)
        (photo5View.findViewById<View>(R.id.next) as Button).apply {
            isEnabled = false
            visibility = View.GONE
        }
    }

    private fun setupMap() {
        val v = photo4Location.findViewById(R.id.mapholder) as RelativeLayout

        if (map != null) {
            map!!.onPause()
            (map!!.parent as RelativeLayout).removeView(map)
        } else {
            map = CycleMapView(activity, this.javaClass.name)
            map!!.overlayPushTop(there)
        }

        map!!.apply {
            v.addView(this, RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            if (hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                enableAndFollowLocation()
            }
            onResume()
            there.setMapView(this)
        }
    }

    private fun setupView() {
        when (step) {
            AddStep.START -> {
                metaCategorySpinner().setSelection(0)
                categorySpinner().setSelection(0)
                caption = ""
                geolocated = false
                there.noOverThere(null)
                setContentView(photo1Start)
            }
            AddStep.CAPTION -> {
                // why recreate this view each time - well *sigh* because we have to force the
                // keyboard to hide, if we don't recreate the view afresh, Android won't redisplay
                // the keyboard if we come back to this view
                photo2Caption = inflater.inflate(R.layout.addphoto_2_caption, null).apply {
                    backNextButtons(this,
                                    getString(R.string.all_button_back), android.R.drawable.ic_media_rew,
                                    getString(R.string.all_button_next), android.R.drawable.ic_media_ff)
                    setContentView(this)
                }
                captionEditor().setText(caption)
                if (photo == null && allowTextOnly) {
                    (photoRoot.findViewById(R.id.label) as TextView).setText(R.string.report_title)
                    (photoRoot.findViewById(R.id.caption) as EditText).setLines(10)
                }
            }
            AddStep.CATEGORY -> {
                caption = captionText()
                store()
                setContentView(photo3Category)
            }
            AddStep.LOCATION -> {
                metaCatId = metaCategoryId()
                catId = categoryId()
                setupMap()
                setContentView(photo4Location)
                there.recentre()
                if (photo == null && allowTextOnly) {
                    (photoRoot.findViewById(R.id.label) as TextView).setText(R.string.report_location_hint)
                    (photoRoot.findViewById(R.id.nogeo) as View).visibility = View.GONE
                } else {
                    (photoRoot.findViewById(R.id.label) as TextView).setText(R.string.photo_location_hint)
                    (photoRoot.findViewById(R.id.nogeo) as View).visibility = if (geolocated) View.GONE else View.VISIBLE
                }
            }
            AddStep.VIEW -> {
                setContentView(photo5View)
                (photo5View.findViewById(R.id.photo_text) as TextView).text = caption
                val url = photo5View.findViewById(R.id.photo_url) as TextView
                val share = photo5View.findViewById(R.id.photo_share) as Button
                if (noShare) {
                    url.visibility = View.GONE
                    share.visibility = View.GONE
                } else {
                    url.text = uploadedUrl
                    share.setOnClickListener(this)
                }
            }
            AddStep.DONE -> {
                step = AddStep.START
                setupView()
            }
        }

        previewPhoto()
        hookUpNext()
    }

    private fun setContentView(child: View) {
        photoRoot.removeAllViewsInLayout()
        photoRoot.addView(child)
    }

    private fun hookUpNext() {
        (photoRoot.findViewById(R.id.back) as Button?)?.setOnClickListener(this)
        (photoRoot.findViewById(R.id.next) as Button?)?.apply {
            setOnClickListener(this@AddPhotoFragment)
            if (step === AddStep.LOCATION)
                isEnabled = there.there() != null
        }
    }

    private fun previewPhoto() {
        val iv = photoRoot.findViewById(R.id.photo) as ImageView? ?: return
        if (photo == null && allowTextOnly) {
            iv.visibility = View.GONE
            return
        }

        // TODO: scaling?
        iv.setImageBitmap(photo)
        val size = Point()
        activity!!.windowManager.defaultDisplay.getSize(size)
        val newHeight = size.y / 10 * 4
        val newWidth = size.x

        iv.layoutParams = LinearLayout.LayoutParams(newWidth, newHeight)
        iv.scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    ///////////// Fragment methods - options menus
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        initialiseDrawables(inflater!!)
        createMenuItem(menu!!, R.string.all_menu_restart, Menu.NONE, restartDrawable)
        createMenuItem(menu, R.string.all_menu_back, Menu.NONE, R.drawable.ic_menu_revert)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        enableMenuItem(menu!!, R.string.all_menu_restart, step !== AddStep.START)
        enableMenuItem(menu, R.string.all_menu_back, step !== AddStep.START && step !== AddStep.VIEW)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val menuItem = item!!.itemId

        return when (menuItem) {
            R.string.all_menu_restart -> {
                step = AddStep.START
                setupView()
                true
            }
            R.string.all_menu_back -> {
                onBackPressed()
                true
            }
            else -> false
        }
    }

    ///////////// Fragment methods - Activity result processing
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return
        if (requestCode != TAKE_PHOTO && requestCode != CHOOSE_PHOTO)
            return

        try {
            if (requestCode == CHOOSE_PHOTO)
                photoFilename = getImageFilePath(data!!, activity)

            photo?.recycle()
            photo = Bitmaps.loadFile(photoFilename)

            val exif = ExifInterface(photoFilename!!)
            dateTime = photoTimestamp(exif)
            val photoLoc = photoLocation(exif)
            geolocated = photoLoc != null
            there.noOverThere(photoLoc)
            nextStep()
        } catch (e: Exception) {
            Toast.makeText(activity, "There was a problem grabbing the photo : " + e.message, Toast.LENGTH_LONG).show()
            Log.w(TAG, "onActivityResult threw exception when processing requestCode $requestCode", e)
            if (requestCode == TAKE_PHOTO)
                startActivityForResult(
                    Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                    CHOOSE_PHOTO
                )
        }
    }

    ///////////// Fragment methods - State store / retrieval
    override fun onPause() {
        prefs().edit().apply {
            putLong("WHEN", Date().time)
            putString("CAPTION", captionText())
            putInt("METACAT", metaCategoryId())
            putInt("CATEGORY", categoryId())
            val p = there.there()
            if (p != null) {
                putInt("THERE-LAT", (p.latitude * 1e6).toInt())
                putInt("THERE-LON", (p.longitude * 1e6).toInt())
            } else
                putInt("THERE-LAT", -1)
            putBoolean("GEOLOC", geolocated)
            putString("UPLOADED-URL", uploadedUrl)
            apply()
        }
        store()

        map?.onPause()

        super.onPause()
    }

    private fun store() {
        prefs().edit().apply {
            putInt("STEP", step.id)
            putString("PHOTOFILE", photoFilename)
            putString("DATETIME", dateTime)
            putString("CAPTION", caption)
            putBoolean("GEOLOC", geolocated)
            apply()
        }
    }

    override fun onResume() {
        try {
            doOnResume()
        } catch (e: RuntimeException) {
            step = AddStep.START
        }
        super.onResume()
        setupView()
    }

    private fun doOnResume() {
        prefs().apply {
            step = AddStep.fromId(getInt("STEP", 1))!!

            photoFilename = getString("PHOTOFILE", photoFilename)
            if (photo == null && photoFilename != null) {
                // TODO scaling?
                photo = Bitmaps.loadFile(photoFilename)
            }
            dateTime = getString("DATETIME", "")

            caption = getString("CAPTION", "")!!

            metaCatId = getInt("METACAT", -1)
            catId = getInt("CATEGORY", -1)
            setSpinnerSelections()

            val lat = getInt("THERE-LAT", -1)
            val lon = getInt("THERE-LON", -1)
            if (lat != -1 && lon != -1)
                there.noOverThere(GeoPoint(lat / 1e6, lon / 1e6))
            geolocated = getBoolean("GEOLOC", false)

            uploadedUrl = getString("UPLOADED-URL", uploadedUrl)

            map?.onResume()

            // If we've not viewed the fragment for more than 5 minutes, reset to the starting step.
            val now = Date().time
            val fragmentPauseTime = getLong("WHEN", now)
            if (Date().time - fragmentPauseTime > fiveMinutes)
                step = AddStep.START
        }
    }

    private val fiveMinutes = (5 * 60 * 1000).toLong()

    private fun prefs(): SharedPreferences {
        return activity!!.getSharedPreferences("net.cyclestreets.AddPhotoActivity", Context.MODE_PRIVATE)
    }

    ///////////// Caption text
    private fun captionEditor(): EditText {
        return photo2Caption!!.findViewById(R.id.caption)
    }
    private fun captionText(): String {
        if (photo2Caption == null)
            return caption
        inputMethodManager.hideSoftInputFromWindow(captionEditor().windowToken, 0)
        return captionEditor().text.toString()
    }

    ///////////// Category spinners
    private fun metaCategorySpinner(): Spinner { return photo3Category.findViewById(R.id.metacat) }
    private fun categorySpinner(): Spinner { return photo3Category.findViewById(R.id.category) }
    private fun metaCategoryId(): Int { return metaCategorySpinner().selectedItemId.toInt() }
    private fun categoryId(): Int { return categorySpinner().selectedItemId.toInt() }

    private fun setupSpinners() {
        if (activity == null) {
            Log.d(TAG, "Activity was null when setting up spinners - break out")
            return
        }
        metaCategorySpinner().adapter = CategoryAdapter(activity!!, photomapCategories!!.metaCategories())
        categorySpinner().adapter = CategoryAdapter(activity!!, photomapCategories!!.categories())
        setSpinnerSelections()
    }
    private fun setSpinnerSelections() {
        // ids == position
        if (metaCatId != -1)
            metaCategorySpinner().setSelection(metaCatId)
        if (catId != -1)
            categorySpinner().setSelection(catId)
    }

    ///////////// View.OnClickListener methods
    override fun onClick(v: View) {
        when (v.id) {
            R.id.takephoto_button -> doOrLogin {
                doOrRequestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    dispatchTakePhotoIntent()
                }
            }
            R.id.chooseexisting_button -> doOrLogin {
                doOrRequestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) {
                    startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                                           CHOOSE_PHOTO)
                }
            }
            R.id.textonly_button -> doOrLogin {
                photo = null
                photoFilename = null
                dateTime = null
                nextStep()
            }
            R.id.photo_share ->
                Share.Url(activity, uploadedUrl, caption, "Photo on CycleStreets.net")
            R.id.back -> {
                if (step === AddStep.VIEW) {
                    step = AddStep.START
                    store()
                    setupView()
                } else
                    onBackPressed()
            }
            R.id.next -> {
                if (step === AddStep.LOCATION) {
                    assert(!needAccountDetails())
                    upload()
                } else if (step != AddStep.VIEW) {
                    nextStep()
                }
            }
        }
    }

    private fun doOrLogin(function: () -> Unit) {
        if (needAccountDetails())
            startActivityForResult(Intent(activity, AccountDetailsActivity::class.java), ACCOUNT_DETAILS)
        else
            function()
    }

    private fun needAccountDetails(): Boolean {
        return !allowUploadByKey && !CycleStreetsPreferences.accountOK()
    }

    private fun dispatchTakePhotoIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity!!.packageManager) == null) {
            Log.i(TAG, "Unable to identify a camera activity")
            Toast.makeText(activity, "Unable to identify a camera activity", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // Create the File where the photo should go
            val photoFile: File = createImageFile(activity)
            // Save a file: path for use with ACTION_VIEW intents
            photoFilename = photoFile.absolutePath

            val photoUri = FileProvider.getUriForFile(activity!!, "net.cyclestreets.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, TAKE_PHOTO)
        } catch (e: Exception) {
            // Error occurred while creating the File
            Log.w(TAG, "Error occured while creating image file", e)
            Toast.makeText(activity, "There was a problem creating an image file : " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun nextStep() {
        if (step === AddStep.LOCATION && there.there() == null) {
            Toast.makeText(activity, "Please set photo location", Toast.LENGTH_LONG).show()
            return
        }
        step = step.next!!
        store()
        setupView()
    }

    private fun upload() {
        try {
            UploadPhotoTask(activity!!,
                            photoFilename!!,
                            CycleStreetsPreferences.username(),
                            CycleStreetsPreferences.password(),
                            there.there(),
                            photomapCategories!!.metaCategories()[metaCatId].tag,
                            photomapCategories!!.categories()[catId].tag,
                            dateTime!!,
                            caption).execute()
        } catch (e: RuntimeException) {
            Toast.makeText(activity, R.string.photo_could_not_upload, Toast.LENGTH_LONG).show()
            step = AddStep.LOCATION
        }
    }

    ///////////// Undoable methods
    override fun onBackPressed(): Boolean {
        if (step === AddStep.START || step === AddStep.VIEW) {
            step = AddStep.START
            store()
            return false
        }
        step = step.previous!!
        store()
        setupView()
        return true
    }

    ///////////// LocationListener methods
    override fun onSetLocation(point: IGeoPoint?) {
        (photo4Location.findViewById(R.id.next) as Button).isEnabled = point != null
    }

    ///////////// Tasks
    private inner class GetPhotomapCategoriesTask : AsyncTask<Any, Void, PhotomapCategories>() {
        override fun doInBackground(vararg params: Any): PhotomapCategories? {
            return try {
                PhotomapCategories.get()
            } catch (ex: Exception) {
                null
            }
        }

        override fun onPostExecute(categories: PhotomapCategories?) {
            if (categories == null) {
                if (activity != null) {
                    Toast.makeText(activity, R.string.photo_could_not_load_categories, Toast.LENGTH_LONG).show()
                }
                return
            }
            photomapCategories = categories
            setupSpinners()
        }
    }

    private inner class UploadPhotoTask(context: Context,
                                        filename: String,
                                        private val username: String,
                                        private val password: String,
                                        private val location: IGeoPoint,
                                        private val metaCat: String,
                                        private val category: String,
                                        private val dateTime: String,
                                        private val caption: String) : AsyncTask<Any, Void, Upload.Result>() {
        private val smallImage: Boolean = CycleStreetsPreferences.uploadSmallImages()
        private val filename: String
        private val progress: ProgressDialog

        init {
            this.filename = if (smallImage) Bitmaps.resizePhoto(filename) else filename
            progress = Dialog.createProgressDialog(context, R.string.photo_uploading)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress.show()
        }

        override fun doInBackground(vararg params: Any): Upload.Result {
            return try {
                Upload.photo(filename, username, password, location,
                    metaCat, category, dateTime, caption)
            } catch (e: Exception) {
                Upload.Result.error(e.message)
            }
        }

        override fun onPostExecute(result: Upload.Result) {
            if (smallImage)
                File(filename).delete()
            progress.dismiss()

            if (result.ok())
                uploadComplete(result.url())
            else
                uploadFailed(result.message())
        }
    }

    private fun uploadComplete(photoUrl: String) {
        uploadedUrl = photoUrl
        nextStep()
    }

    private fun uploadFailed(msg: String) {
        MessageBox.OK(photo4Location, msg) { _, _ ->
            step = AddStep.LOCATION
            setupView()
        }
    }
}
