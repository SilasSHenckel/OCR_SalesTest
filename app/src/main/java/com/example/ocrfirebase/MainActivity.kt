package com.example.ocrfirebase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ocrfirebase.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private val REQUEST_CAMERA_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE)

        viewBinding.btCapture.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewBinding.btCapture.visibility = View.GONE
        viewBinding.progress.visibility = View.VISIBLE

        val mThread = Thread {
            if (resultCode == RESULT_OK) {
                assert(data != null)
                val uri = data!!.data
                try {
                    val bitmap = BitmapFactory.decodeFile(uri!!.path)
                    getTextRecognize(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mThread.start()

    }

    private fun getTextRecognize(bitmap: Bitmap) {
        val matrix = Matrix()
        matrix.postRotate(270F)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
        val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
        val textRecognizer = TextRecognizer.Builder(this@MainActivity).build()
        if (!textRecognizer.isOperational) {
            Toast.makeText(this@MainActivity, "ERROR", Toast.LENGTH_SHORT).show()
        } else {
            val frame = Frame.Builder().setBitmap(rotatedBitmap).build()
            val textBlockSparseArray = textRecognizer.detect(frame)
            val textBlock = textBlockSparseArray.valueAt(0)
            val string3 = textBlock.value.toUpperCase().split("PRODUTO").toTypedArray()
            val products = mutableListOf<Item>()

            for (i in string3.indices) {
                if (i != 0){
                    val data = string3[i].trim().split("\n")
                    if (data.size > 2){
                        //Se tiver a QTD => index: QTD = 0
                        products.add(Item(id = data[2], description = data[1], qnt = data[0]))
                    } else if(data.size > 1){
                        //Se não tiver quantidade
                        products.add(Item(id = data[1], description = data[0], qnt = "0"))
                    }
                }
            }
            setAdapter(products)
        }
        runOnUiThread {
            viewBinding.btCapture.visibility = View.VISIBLE
            viewBinding.progress.visibility = View.GONE
            viewBinding.tvObs.visibility = View.VISIBLE
        }
    }

    private fun setAdapter(products: MutableList<Item>){
        runOnUiThread {
            viewBinding.recycler.adapter = ItemsAdapter(
                products,
                this,
                ItemClickListener {}
            )
        }
    }
}

data class Item(val id: String = "", val description: String = "", val qnt: String = "", val units: List<String> = listOf("UN", "SAC", "PAC", "CAM"))