package me.shellbell.makepizza

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
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
        pizza.isDrawingCacheEnabled = true
        pizza.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

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
        saveMenuItem?.setOnMenuItemClickListener { savePizza() }?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupNoOfWedgesEditText() {
        noOfWedgesEditText.setText(pizza.noOfWedges.toString())
        noOfWedgesEditText.setOnEditorActionListener(TextView.OnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                        this.currentFocus?.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS)

                if (noOfWedgesEditText.text.toString().isNotEmpty()) {
                    try {
                        noOfWedgesEditText.text.toString().toInt()
                        pizza.noOfWedges = noOfWedgesEditText.text.toString().toInt()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "Not an integer", Toast.LENGTH_SHORT).show()
                        noOfWedgesEditText.setText("0")
                        pizza.noOfWedges = 0
                    }
                } else {
                    pizza.noOfWedges = 0
                }

                pizza.invalidate()

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

    private fun savePizza(): Boolean {
        pizza.buildDrawingCache()
        val bitmap = pizza.drawingCache

        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {

            if (!isStoragePermissionGranted()) {
                Toast.makeText(this@MainActivity, "Give storage permission to store Pizza", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        0)
            }

            val dir = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Pizza")

            if (!dir.mkdirs()) {
                //Directory not created
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault())
            val file = File(dir, "PIZZA-" + sdf.format(Calendar.getInstance().time) + ".png")

            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 10, outputStream)
                outputStream.close()
                Toast.makeText(this@MainActivity, "Pizza saved at " + file.absolutePath, Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
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
