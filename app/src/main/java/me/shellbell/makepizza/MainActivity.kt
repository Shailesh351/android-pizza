package me.shellbell.makepizza

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.android.colorpicker.ColorPickerDialog
import me.shellbell.pizza.Pizza
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
}
