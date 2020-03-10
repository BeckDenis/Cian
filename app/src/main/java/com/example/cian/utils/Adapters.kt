package com.example.cian.utils

import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val VALUE_TAG = "ValueEventListenerAdapt"
private const val CHILD_TAG = "ChildEventListenerAdapt"

class ValueEventListenerAdapter(val handler: (DataSnapshot) -> Unit) : ValueEventListener {
    override fun onCancelled(error: DatabaseError) {
        Log.e(VALUE_TAG, "onCancelled: ", error.toException())
    }

    override fun onDataChange(data: DataSnapshot) {
        handler(data)
    }
}

class ChildEventListenerAdapter(val handler: (DataSnapshot) -> Unit) : ChildEventListener {
    override fun onCancelled(p0: DatabaseError) {
    }

    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
    }

    override fun onChildChanged(child: DataSnapshot, p1: String?) {
        handler(child)
    }

    override fun onChildAdded(child: DataSnapshot, p1: String?) {
        handler(child)
    }

    override fun onChildRemoved(p0: DataSnapshot) {
    }
}

class OnPageChangeListenerAdapter(val handler: (Int) -> Unit): ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        handler(position)
    }

}