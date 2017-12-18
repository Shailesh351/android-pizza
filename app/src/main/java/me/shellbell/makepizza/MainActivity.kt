package me.shellbell.makepizza

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import me.shellbell.pizza.Pizza
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var pizza: Pizza
    private lateinit var edgeWidthSeekBar: SeekBar
    private lateinit var cutWidthSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edgeWidthSeekBar = findViewById(R.id.edge_width_seek_bar)
        cutWidthSeekBar = findViewById(R.id.cut_width_seek_bar)
        pizza = findViewById(R.id.pizza)

        pizza.post { setupSeekBars() }
    }

    private fun setupSeekBars() {
        setupEdgeWidthSeekBar()
        setupCutWidthSeekBar()
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
}
