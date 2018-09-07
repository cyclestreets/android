package net.cyclestreets.photos

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import net.cyclestreets.api.Photo
import net.cyclestreets.util.ImageDownloader
import net.cyclestreets.view.R

internal class PhotoDisplay internal constructor(photo: Photo, context: Context) : DisplayDialog(photo, context) {
    private lateinit var imageView: ImageView

    override fun title(): String {
        return String.format("Photo #%d", photo.id())
    }

    override fun caption(): String {
        return photo.caption()
    }

    override fun loadLayout(): View {
        val layout = View.inflate(context, R.layout.showphoto, null)
        imageView = layout.findViewById<View>(R.id.photo) as ImageView

        //sizeView(imageView, context_);
        imageView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.spinner))
        ImageDownloader.get(photo.thumbnailUrl(), imageView)

        return layout
    }

    override fun preShowSetup(builder: AlertDialog.Builder) {
        builder.setOnCancelListener {
            val photo = (imageView.drawable as BitmapDrawable).bitmap
            photo.recycle()
        }
    }
}
