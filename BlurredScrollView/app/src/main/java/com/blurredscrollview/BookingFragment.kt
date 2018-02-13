package com.blurredscrollview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.RelativeLayout
import java.util.*
import kotlin.properties.Delegates

data class TimeSlotEvent(val startTime: Calendar, val endTime: Calendar)

private const val TAG = "TimeSlotSelectorView"
private const val HOURS_IN_DAY = 24
private const val MINUTES_IN_HOUR = 60

class TimeSlotSelectorView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : RelativeLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    // Mutable view state
    private var hourWidth by Delegates.notNull<Float>()
    private var eventCellId by Delegates.notNull<Int>()
    private var backgroundImage by Delegates.notNull<Bitmap>()

    // Initialize the view by reading all the props from the layout
    init {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.Attributes, 0, 0)
        try {
            // Hour width
            hourWidth = attributes.getDimension(R.styleable.Attributes_hourWidth, 100f)

            // Event cell reference
            eventCellId = attributes.getResourceId(R.styleable.Attributes_eventCell, -1)
            if (eventCellId == -1)
                throw IllegalStateException("You must provide an event cell id")

            // Decode the background drawable to a bitmap
            backgroundImage = BitmapFactory.decodeResource(context.applicationContext.resources, R.drawable.background)
        } finally {
            attributes.recycle()

            // Set the layout width
            val layoutWidth = (HOURS_IN_DAY * hourWidth).toInt()
            layoutParams = RelativeLayout.LayoutParams(layoutWidth, height)
        }

        setWillNotDraw(false)
        ViewCompat.postInvalidateOnAnimation(this@TimeSlotSelectorView)
    }

    // Set the events on the view
    fun setEvents(events: Collection<TimeSlotEvent>) {
        post {
            // Get the parent view
            val parentView = (parent as? HorizontalScrollView) ?: return@post

            // Remove all the views
            this.removeAllViews()

            parentView.setOnScrollChangeListener { view, _, _, _, _ ->
                invalidate()
            }

            // Work out the width of a minute
            val minuteWidth = hourWidth / MINUTES_IN_HOUR

            // For each of the passed events, inflate a view and add it
            events.forEach {

                // Get the start and end times in minutes
                val startMinutes = it.startTime.get(Calendar.HOUR_OF_DAY) * MINUTES_IN_HOUR + it.startTime.get(Calendar.MINUTE)
                val endMinutes = it.endTime.get(Calendar.HOUR_OF_DAY) * MINUTES_IN_HOUR + it.endTime.get(Calendar.MINUTE)

                // Work out the bounds of the rectangle
                val x1 = (minuteWidth * startMinutes).toInt()
                val y1 = parentView.height / 2
                val x2 = (minuteWidth * endMinutes).toInt()
                val y2 = parentView.height
                val cellWidth = Math.abs(x2 - x1)
                val cellHeight = Math.abs(y2 - y1)

                // Inflate a cell, set it's params and add it to this view
                val cell = inflate(context, eventCellId, null)
                val params = RelativeLayout.LayoutParams(cellWidth, cellHeight)
                params.leftMargin = x1
                params.topMargin = y1
                addView(cell, params)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        updateEventBackgroundDrawables()
    }

    private fun updateEventBackgroundDrawables() {
        // Get the parent view
        val parentView = (parent as? HorizontalScrollView) ?: return
        val scrollX = parentView.scrollX

        val xScale = width.toFloat() / backgroundImage.width.toFloat()
        val yScale = height.toFloat() / backgroundImage.height.toFloat()

        for (index in 0 until childCount) {
            val event = getChildAt(index)
            event.post {
                val eventBg = getChildAt(index).findViewById<ImageView>(R.id.cellBackground)
                if (eventBg is ImageView) {
                    var bgLeft = (event.left + scrollX).toFloat() / xScale
                    if (bgLeft < 0) {
                        bgLeft = 0f
                    } else if (bgLeft > backgroundImage.width) {
                        bgLeft = backgroundImage.width.toFloat()
                    }

                    var bgRight = (event.right + scrollX).toFloat() / xScale
                    if (bgRight > backgroundImage.width) {
                        bgRight = backgroundImage.width.toFloat()
                    } else if (bgRight < 0) {
                        bgRight = 0f
                    }

                    val bgTop = event.top.toFloat() / yScale
                    val bgWidth = bgRight - bgLeft
                    val bgHeight = event.height.toFloat() / yScale

                    val clippedBgBitmap = Bitmap.createBitmap(backgroundImage, bgLeft.toInt(), bgTop.toInt(), bgWidth.toInt(), bgHeight.toInt())

                    eventBg.setImageBitmap(clippedBgBitmap)
                }
            }
        }
    }
}