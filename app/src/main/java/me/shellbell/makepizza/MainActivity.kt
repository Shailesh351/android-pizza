package me.shellbell.makepizza

import android.Manifest
import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.android.colorpicker.ColorPickerDialog
import me.shellbell.pizza.Pizza
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private lateinit var pizza: Pizza

    private lateinit var edgeWidthSeekBar: SeekBar
    private lateinit var cutWidthSeekBar: SeekBar
    private lateinit var noOfWedgesEditText: EditText

    private lateinit var pizzaColorButton: ImageButton
    private lateinit var edgeColorButton: ImageButton
    private lateinit var cutColorButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edgeWidthSeekBar = findViewById(R.id.edge_width_seek_bar)
        cutWidthSeekBar = findViewById(R.id.cut_width_seek_bar)
        noOfWedgesEditText = findViewById(R.id.no_of_wedges_edit_text)

        pizzaColorButton = findViewById(R.id.pizza_color_picker)
        edgeColorButton = findViewById(R.id.edge_color_picker)
        cutColorButton = findViewById(R.id.cut_color_picker)

        pizza = findViewById(R.id.pizza)
        pizza.post { setup() }
    }

    private fun setup() {
        setupNoOfWedgesEditText()
        setupEdgeWidthSeekBar()
        setupCutWidthSeekBar()
        setupColorPickers()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val saveMenuItem = menu?.add("Save pizza")
        saveMenuItem?.icon = ContextCompat.getDrawable(this, R.drawable.ic_save_white_24dp)
        saveMenuItem?.setOnMenuItemClickListener { savePizza() }?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupNoOfWedgesEditText() {
        noOfWedgesEditText.setText(pizza.noOfWedges.toString())
        noOfWedgesEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int,
                                           after: Int) {
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    try {
                        text.toString().toInt()
                        pizza.noOfWedges = text.toString().toInt()
                    } catch (e: Throwable) {
                        Toast.makeText(applicationContext, "Not an integer", Toast.LENGTH_SHORT).show()
                        noOfWedgesEditText.setText("0")
                        pizza.noOfWedges = 0
                    }
                    if (pizza.noOfWedges <= 100) {
                        pizza.invalidate()
                    } else {
                        noOfWedgesEditText.setText("")
                        pizza.noOfWedges = 0
                        Toast.makeText(applicationContext, "Wedges can not be more than 100", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    pizza.noOfWedges = 0
                    pizza.invalidate()
                }
            }
        })
        noOfWedgesEditText.setOnEditorActionListener(TextView.OnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                        this.currentFocus?.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS)

                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun setupEdgeWidthSeekBar() {
        val width = pizza.width - pizza.paddingLeft - pizza.paddingRight
        val height = pizza.height - pizza.paddingTop - pizza.paddingBottom

        edgeWidthSeekBar.max = min(width, height) / 2
        edgeWidthSeekBar.progress = pizza.edgeWidth.toInt()

        edgeWidthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                pizza.edgeWidth = progress.toFloat()
                pizza.invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupCutWidthSeekBar() {
        val MAX_CUT_WIDTH = 24F

        cutWidthSeekBar.max = MAX_CUT_WIDTH.toInt()
        cutWidthSeekBar.progress = pizza.cutWidth.toInt()

        cutWidthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                pizza.cutWidth = progress.toFloat()
                pizza.invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupColorPickers() {
        pizzaColorButton.setOnClickListener { showColorPicker(pizza.color, "pizza") }
        edgeColorButton.setOnClickListener { showColorPicker(pizza.edgeColor, "edge") }
        cutColorButton.setOnClickListener { showColorPicker(pizza.cutColor, "cut") }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun showColorPicker(selectedColor: Int, label: String) {
        val colorPickerDialog = ColorPickerDialog()
        val colors = this@MainActivity.resources.getIntArray(R.array.colors)

        colorPickerDialog.initialize(R.string.color_picker_default_title,
                colors, selectedColor, 5, 2)

        colorPickerDialog.setOnColorSelectedListener {
            when (label) {
                "pizza" -> {
                    pizza.color = it
                    pizzaColorButton.background = ColorDrawable(it)
                }
                "edge" -> {
                    pizza.edgeColor = it
                    edgeColorButton.background = ColorDrawable(it)
                }
                "cut" -> {
                    pizza.cutColor = it
                    cutColorButton.background = ColorDrawable(it)
                }
            }
            pizza.invalidate()
        }

        colorPickerDialog.show(this.fragmentManager, "color_picker")
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun savePizza(): Boolean {

        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {

            if (!isStoragePermissionGranted()) {
                Toast.makeText(this@MainActivity, "Give storage permission to store Pizza", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        0)
                return false
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault())
            var fos: FileOutputStream? = null
            var file: File? = null
            var imageUri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = applicationContext.contentResolver
                val contentValues: ContentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "PIZZA-" + sdf.format(Calendar.getInstance().time))
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "pizza")
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) } as FileOutputStream?
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), "pizza")

                if (!dir.exists()) {
                    dir.mkdir()
                }

                file = File(dir, "PIZZA-" + sdf.format(Calendar.getInstance().time) + ".png")
                fos = FileOutputStream(file)
            }

            try {
                getBitmapFromView(pizza)?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                if (fos != null) {
                    fos.flush()
                    fos.close()
                }
                if (imageUri != null) {
                    Toast.makeText(this@MainActivity, "Pizza saved at " + imageUri.path, Toast.LENGTH_SHORT).show()
                }
                if (file != null) {
                    Toast.makeText(this@MainActivity, "Pizza saved at " + file.absolutePath, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }

    private fun isStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}
