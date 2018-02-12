package com.blurredscrollview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.jackandphantom.blurimage.BlurImage
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

            setWillNotDraw(false)
        }
    }

    // Set the events on the view
    fun setEvents(events: Collection<TimeSlotEvent>) {
        post {
            // Get the parent view
            val parentView = (parent as? View) ?: return@post

            // Remove all the views
            this.removeAllViews()

            // Work out the width of a minute
            val minuteWidth = hourWidth / MINUTES_IN_HOUR

            // For each of the passed events, inflate a view and add it
            events.forEach {
                // Set will not cache drawing
                setWillNotCacheDrawing(true)

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

            // Redraw
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Update the images on the backgrounds
        updateEventBackgroundImages()
    }

    // Cache the bmp of the root
    private var bmpScreenshot: Bitmap? = null

    private fun updateEventBackgroundImages() {
        val parentView = (parent as? View) ?: return
        parentView.post {

            if (bmpScreenshot === null) {
                // Create bmp of the root view
                val root = rootView
                root.isDrawingCacheEnabled = true
                root.buildDrawingCache()
                bmpScreenshot = root.drawingCache
            }

            // For each child view, work out the blurred background image
            for (index in 0 until childCount) {
                // Get the child view
                val child = getChildAt(index)
                child.post {
                    // If child is in bounds of parent view
                    if (child.x >= 0 && child.x <= parentView.width) {
                        if (child.width > 0 && child.height > 0) {
                            // Create clipped bmp of the root bmp
                            val location = intArrayOf(0, 0)
                            child.getLocationInWindow(location)
                            val clippedBmp = Bitmap.createBitmap(bmpScreenshot, location[0], location[1], child.width, child.height, null, false)

                            // Get reference to the cell background image view on the child
                            val cellBackgroundImage = child.findViewById<ImageView>(R.id.cellBackground)

                            // Blur the clipped bmp and apply it to the image view
                            BlurImage.with(context.applicationContext).load(clippedBmp).intensity(1f).Async(true).into(cellBackgroundImage)
                        }
                    }
                }
            }
        }
    }
}