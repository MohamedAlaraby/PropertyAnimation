/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.propertyanimation

import android.animation.*
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView


class MainActivity : AppCompatActivity() {

    lateinit var star: ImageView
    lateinit var rotateButton: Button
    lateinit var translateButton: Button
    lateinit var scaleButton: Button
    lateinit var fadeButton: Button
    lateinit var colorizeButton: Button
    lateinit var showerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        star = findViewById(R.id.star)
        rotateButton = findViewById<Button>(R.id.rotateButton)
        translateButton = findViewById<Button>(R.id.translateButton)
        scaleButton = findViewById<Button>(R.id.scaleButton)
        fadeButton = findViewById<Button>(R.id.fadeButton)
        colorizeButton = findViewById<Button>(R.id.colorizeButton)
        showerButton = findViewById<Button>(R.id.showerButton)

        rotateButton.setOnClickListener {
            rotater()

        }

        translateButton.setOnClickListener {
            translater()
        }

        scaleButton.setOnClickListener {
            scalar()
        }

        fadeButton.setOnClickListener {
            fader()
        }

        colorizeButton.setOnClickListener {
            colorizer()
        }

        showerButton.setOnClickListener {
            shower()
        }
    }
    private fun ObjectAnimator.disableViewDuringAnimation(view: View){
        //Avoiding discontinuous motion
        /*
        * This discontinuous motion is an example of what we call “jank”;
        *  it causes a disruptive flow for the user,
        *  instead of the smooth experience you would like.*/
        addListener(object :AnimatorListenerAdapter(){
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
               view.isEnabled=false
            }
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                view.isEnabled=true
            }
            //This way, each animation is completely separate from any other rotation animation,
            // avoiding the "jank" of restarting in the middle.
        })
    }
    private fun rotater() {
        val animator=ObjectAnimator.ofFloat(star, View.ROTATION,-360f,0f)
        animator.duration=1000L
        animator.disableViewDuringAnimation(rotateButton)
        animator.start()
    }

    private fun translater() {
        val animator=ObjectAnimator.ofFloat(star,View.TRANSLATION_X,200f)
        animator.repeatCount=1
        animator.repeatMode=ObjectAnimator.REVERSE
        animator.disableViewDuringAnimation(translateButton)
        animator.start()
    }

    private fun scalar() {
        //we use PropertyValuesHolder we scale in x,y in parallel because there is only one animator object we are working with
        val scaleX=PropertyValuesHolder.ofFloat(View.SCALE_X,4f)
        val scaleY=PropertyValuesHolder.ofFloat(View.SCALE_Y,4f)
        val animator=ObjectAnimator.ofPropertyValuesHolder(star,scaleX,scaleY)
        animator.repeatCount=1
        animator.repeatMode=ObjectAnimator.REVERSE
        animator.disableViewDuringAnimation(scaleButton)
        animator.start()
    }

    private fun fader() {
        val animator=ObjectAnimator.ofFloat(star,View.ALPHA,0f)
        animator.repeatCount=1
        animator.repeatMode=ObjectAnimator.REVERSE
        animator.disableViewDuringAnimation(fadeButton)
        animator.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun colorizer() {
        val animator=ObjectAnimator.ofArgb (star.parent,
            "backgroundColor",Color.BLACK,Color.RED)
        animator.duration = 1000
        animator.repeatCount=1
        animator.repeatMode=ObjectAnimator.REVERSE
        animator.disableViewDuringAnimation(colorizeButton)
        animator.start()
    }

    private fun shower() {

        val container=star.parent as ViewGroup
        //the width and height of that container (which you will use to calculate
        // the end translation values for our falling stars
        val containerW=container.width
        val containerH=container.height
        //the default width and height of our star,
        // which you will later alter with a scale factor to get different-sized stars
        var starW=star.width.toFloat()
        var starH=star.height.toFloat()
        val newStar=AppCompatImageView(this)
        newStar.setImageResource(R.drawable.ic_star)
        newStar.layoutParams=FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        container.addView(newStar)
        /*
        * Use this scale factor to change the cached width/height values,
        * because you will need to know the actual pixel height/width for later calculations
        * */

            newStar.scaleX = Math.random().toFloat() * 1.5f + .1f
            newStar.scaleY = newStar.scaleX
            starW *= newStar.scaleX
            starH *= newStar.scaleY
            newStar.translationX = Math.random().toFloat() * containerW - starW / 2
            //The actual animation
            /*
             There will be different types of motion, what we call “interpolation”
             on these two animations. Specifically, the rotation will use a smooth linear motion
             (moving at a constant rate over the entire rotation animation),
             while the falling animation will use an accelerating motion
             (simulating gravity pulling the star downward at a constantly faster rate).
             So you'll create two animators and add an interpolator to each.
            * */
            // First, create two animators, along with their interpolates:
            val mover=ObjectAnimator.ofFloat(newStar,View.TRANSLATION_Y,-starH,containerH+starH)
            //The AccelerateInterpolator “interpolator” that we are setting on the star causes a gentle acceleration motion.
            mover.interpolator=AccelerateInterpolator(1f)
            val rotator=ObjectAnimator.ofFloat(newStar,View.ROTATION,(Math.random()*1080).toFloat())
            rotator.interpolator=LinearInterpolator()
            //Now it is time to put these two animators together into a single AnimatorSet
            val set=AnimatorSet()
            set.playTogether(mover,rotator)
            set.duration=(Math.random()*1500+1000).toLong()
            set.addListener(object :AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    container.removeView(newStar)
                }
            })
            set.start()


    }

}
