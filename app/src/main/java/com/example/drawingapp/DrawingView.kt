package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

/*
    The Canvas class holds the "draw" calls. To draw something, you need 4 basic components:
    A Bitmap to hold the pixels, a Canvas to host the draw calls (writing into the bitmap),
    a drawing primitive (e.g. Rect, Path, text, Bitmap),
    and a Paint (to describe the colors and styles for the drawing).
 */
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0f
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    fun onClickUndo() {
        if (mPaths.size > 0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    fun onClickRedo() {
        if (mUndoPaths.size > 0) {
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.size - 1))
            invalidate()
        }
    }

    private fun setUpDrawing() {
        mDrawPath = CustomPath(color, mBrushSize)

        //The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
        mDrawPaint = Paint()
        mDrawPaint!!.apply {
            isAntiAlias = true
            color = color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20f
    }

    /*
     Android doesn't know the real size at start, it needs to calculate it.
     Once it's done, onSizeChanged() will notify you with the real size.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        /*
         A bitmap configuration describes how pixels are stored. This affects the quality (color depth)
         as well as the ability to display transparent/translucent colors (Bitmap.Config.ARGB_8888 is used mostly)
         */
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    //when the view should be drawn onDraw() is called.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mDrawPaint)

        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        //mDrawPath should not be Empty in order to draw it on Canvas
        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                if (touchX != null && touchY != null) {
                    mDrawPath!!.moveTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null && touchY != null) {
                    mDrawPath!!.lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()

        return true
    }

    fun setSizeForBrush(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    /*
    The Path class encapsulates compound (multiple contour) geometric paths consisting of straight line segments,
    quadratic curves, and cubic curves. It can be drawn with canvas.drawPath(path, paint), either filled or stroked
    (based on the paint's Style), or it can be used for clipping or to draw text on a path.
     */
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }
}