package com.monsterbrain.squaregridsample

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.setPadding
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sqrLayout = findViewById<SquareGridLayout>(R.id.squareLayout)
        val srcBmp = getBitmapFromAsset(this, "cosmos.png")

        srcBmp?.let { bitmap ->
            val sqWidth = bitmap.width / 3

            for (i in 0 until 9) {
                val imgView = ImageView(this)
                val row = i % 3
                val col = i / 3
                val partialBmp = Bitmap.createBitmap(srcBmp, row*sqWidth, col*sqWidth, sqWidth, sqWidth)
                imgView.setImageBitmap(partialBmp)
                imgView.setPadding(4)
                sqrLayout.addView(imgView)
            }
        }

        sqrLayout.invalidate()
    }

    fun getBitmapFromAsset(context: Context, strName: String?): Bitmap? {
        val assetManager: AssetManager = context.assets
        val istr: InputStream
        var bitmap: Bitmap? = null
        try {
            istr = assetManager.open(strName!!)
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            return null
        }
        return bitmap
    }
}