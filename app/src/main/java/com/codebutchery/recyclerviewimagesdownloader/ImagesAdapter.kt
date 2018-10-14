package com.codebutchery.recyclerviewimagesdownloader

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.codebutchery.recyclerviewimagesdownloader.data.ImageDescriptor
import com.google.gson.Gson
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.*
import android.widget.ImageView
import java.io.ByteArrayOutputStream

class ImagesAdapter(private val context: Context) : RecyclerView.Adapter<ImagesViewHolder>() {
    private val downloaderThread: HandlerThread = HandlerThread("downloaderThread")
    private val bgHandler: ImageDownloadHandler
    private val uiHandler: ImageShowHandler = ImageShowHandler(Looper.getMainLooper())
    private val imageDescriptors:Array<ImageDescriptor>

    init {
        downloaderThread.start()
        bgHandler = ImageDownloadHandler(downloaderThread.looper, uiHandler)

        val jsonContent = context.assets.open("images.json")
                .bufferedReader()
                .use { it.readText() }

        imageDescriptors = Gson().fromJson(jsonContent , Array<ImageDescriptor>::class.java)
    }

    override fun getItemCount(): Int {
        return imageDescriptors.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_entry_view, parent, false)
        return ImagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        holder.view.setBackgroundColor(
                if (position % 2 == 0) context.resources.getColor(R.color.evenListItem)
                else context.resources.getColor(R.color.oddListItem))

        holder.tvName.text = imageDescriptors[position].name
        holder.tvAuthor.text = "By " + imageDescriptors[position].author
        holder.ivImage.setImageResource(R.drawable.ic_picture)

        val message = Message()
        message.obj = holder.ivImage
        message.what = 1
        message.data = Bundle()
        message.data.putString("imageUrl", imageDescriptors[position].url)

        bgHandler.sendMessage(message)
    }

    override fun onViewRecycled(holder: ImagesViewHolder) {
        bgHandler.removeMessages(1, holder.ivImage)
        uiHandler.removeMessages(1, holder.ivImage)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        downloaderThread.quit()
    }
}

class ImageDownloadHandler(looper:Looper, private val uiHandler:ImageShowHandler) : Handler(looper) {
    override fun handleMessage(inMessage: Message) {
        val url = inMessage.data["imageUrl"] as String

        downloadImage(url)?.let {
            val ivImage = inMessage.obj as ImageView
            val outMessage = Message()
            outMessage.obj = ivImage
            outMessage.what = 1
            outMessage.data = Bundle()
            outMessage.data.putByteArray("imageData", bitmapToByteArray(it))

            val newMessagesEnqueuedForThisImageView = hasMessages(1, ivImage)
            if (!newMessagesEnqueuedForThisImageView) {
                uiHandler.sendMessage(outMessage)
            }
        }
    }

    private fun downloadImage(url:String) : Bitmap? {
        var bitmap: Bitmap? = null
        java.net.URL(url).openStream()?.use { bitmap = BitmapFactory.decodeStream(it) }
        return bitmap
    }

    private fun bitmapToByteArray(bitmap:Bitmap) : ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}

class ImageShowHandler(looper:Looper) : Handler(looper) {
    override fun handleMessage(inMessage: Message) {
        val ivImage = inMessage.obj as ImageView
        val newMessagesEnqueuedForThisImageView = hasMessages(1, ivImage)
        if (!newMessagesEnqueuedForThisImageView) {
            val imageByteArray = inMessage.data["imageData"] as ByteArray
            val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            ivImage.setImageBitmap(bitmap)
        }
    }
}