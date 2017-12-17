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
    private var color = Color.YELLOW
    private var edgeWidth = 36F
    private var edgeColor = Color.GRAY
    private var cutWidth = 6F
    private var cutColor = Color.BLACK

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

        if (attrs != null) {
            val attrsArray = context!!.obtainStyledAttributes(attrs, R.styleable.Pizza)

            noOfWedges = attrsArray.getInt(R.styleable.Pizza_no_of_wedges, 8)
            color = attrsArray.getColor(R.styleable.Pizza_color, Color.YELLOW)
            edgeWidth = attrsArray.getDimension(R.styleable.Pizza_edge_width, 36F)
            edgeColor = attrsArray.getColor(R.styleable.Pizza_edge_color, Color.MAGENTA)
            cutWidth = attrsArray.getDimension(R.styleable.Pizza_cut_width, 6F)
            cutColor = attrsArray.getColor(R.styleable.Pizza_cut_color, Color.BLACK)
        }
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom

        val cx = (width / 2 + paddingLeft).toFloat()
        val cy = (height / 2 + paddingTop).toFloat()

        fillPizza(canvas, cx, cy, width, height)
        drawEdge(canvas, cx, cy, width, height)
        drawPizzaCuts(canvas, cx, cy, width, height)
    }

    private fun fillPizza(canvas: Canvas?, cx: Float, cy: Float, width: Int, height: Int) {
        val diameter = min(width, height).toFloat()
        val radius = diameter / 2

        paint.color = color
        paint.style = Paint.Style.FILL

        canvas?.drawCircle(cx, cy, radius, paint)
    }

    private fun drawEdge(canvas: Canvas?, cx: Float, cy: Float, width: Int, height: Int) {
        val diameter = min(width, height).toFloat() - edgeWidth
        val radius = diameter / 2

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = edgeWidth
        paint.color = edgeColor

        canvas?.drawCircle(cx, cy, radius, paint)
    }

    private fun drawPizzaCuts(canvas: Canvas?, cx: Float, cy: Float, width: Int, height: Int) {
        val diameter = min(width, height).toFloat() - 2 * edgeWidth
        val radius = diameter / 2
        val degree = 360F / noOfWedges

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = cutWidth
        paint.color = cutColor

        canvas?.save()

        for (i in 1..noOfWedges) {
            canvas?.rotate(degree, cx, cy)
            canvas?.drawLine(cx, cy, cx, cy - radius, paint)
        }

        canvas?.restore()
    }
}