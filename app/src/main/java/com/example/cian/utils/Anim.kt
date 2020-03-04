package com.example.cian.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import android.view.View

fun View.showView() {
    visibility = View.VISIBLE
    animate().alpha(1.0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            super.onAnimationStart(animation)
            Log.d("Utils", "start animate(1)")
        }

        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
            super.onAnimationEnd(animation, isReverse)
            Log.d("Utils", "end animate(1)")
        }
    })
}

fun View.hideView() {
    animate().alpha(0.0f).setListener(object : AnimatorListenerAdapter() {

        override fun onAnimationStart(animation: Animator?) {
            super.onAnimationStart(animation)
            Log.d("Utils", "start animate(2)")
        }

        override fun onAnimationEnd(animation: Animator?) {
            visibility = View.GONE
            Log.d("Utils", "end animate(2)")
        }

    })

}