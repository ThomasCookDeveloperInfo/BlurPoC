package com.blurredscrollview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import java.util.*

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

//        val testFragment = BookingFragment()
//        testFragment.arguments = BookingFragment.createArgs(0, 0, 1000, 1000)

//        supportFragmentManager.beginTransaction()
//                .add(R.id.container, testFragment)
//                .addToBackStack(null)
//                .commit()

        val start1 = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        start1.set(Calendar.HOUR_OF_DAY, 2)
        val end1 = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        end1.set(Calendar.HOUR_OF_DAY, 4)

        val start2 = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        start2.set(Calendar.HOUR_OF_DAY, 6)
        val end2 = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        end2.set(Calendar.HOUR_OF_DAY, 10)

        val start3 = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        start3.set(Calendar.HOUR_OF_DAY, 12)
        val end3 = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        end3.set(Calendar.HOUR_OF_DAY, 15)

        val start4 = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        start4.set(Calendar.HOUR_OF_DAY, 18)
        val end4 = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        end4.set(Calendar.HOUR_OF_DAY, 21)

        Handler().post {
            val timeslotView = findViewById<TimeSlotSelectorView>(R.id.timeSlotSelectorView)
            timeslotView.setEvents(listOf(TimeSlotEvent(start1, end1), TimeSlotEvent(start2, end2), TimeSlotEvent(start3, end3), TimeSlotEvent(start4, end4)))
//            timeslotView.setEvents(listOf(TimeSlotEvent(start1, end1), TimeSlotEvent(start2, end2), TimeSlotEvent(start3, end3)))
        }
    }
}