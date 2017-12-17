package me.shellbell.pizza

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * Created by Shailesh351 on 17/12/17.
 */

class Pizza : View {

    private lateinit var paint: Paint
    private var noOfWedges = 8

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        var strokeWidth = 4F
        var strokeColor = Color.BLACK

        if (attrs != null) {
            val attrsArray = context!!.obtainStyledAttributes(attrs, R.styleable.Pizza)

            noOfWedges = attrsArray.getInt(R.styleable.Pizza_no_of_wedges, 8)
            strokeWidth = attrsArray.getDimension(R.styleable.Pizza_stroke_width, 4F)
            strokeColor = attrsArray.getColor(R.styleable.Pizza_stroke_color, Color.BLACK)
        }

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom

        val cx = (width / 2 + paddingLeft).toFloat()
        val cy = (height / 2 + paddingTop).toFloat()

        val diameter = min(width, height).toFloat() - paint.strokeWidth
        val radius = diameter / 2

        canvas?.drawCircle(cx, cy, radius, paint)
        drawPizzaCuts(canvas, cx, cy, radius)
    }

    private fun drawPizzaCuts(canvas: Canvas?, cx: Float, cy: Float, radius: Float) {
        val degree = 360F / noOfWedges

        canvas?.save()

        for (i in 1..noOfWedges) {
            canvas?.rotate(degree, cx, cy)
            canvas?.drawLine(cx, cy, cx, cy - radius, paint)
        }

        canvas?.restore()
    }
}