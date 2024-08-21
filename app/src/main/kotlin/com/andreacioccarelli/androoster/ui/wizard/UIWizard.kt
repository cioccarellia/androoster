package com.andreacioccarelli.androoster.ui.wizard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.View

import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import com.andreacioccarelli.androoster.ui.boot.UIBoot
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment


class UIWizard : AppIntro() {


    private val MIN_SCALE = 0.85f
    private val MIN_ALPHA = 0.5f

    private lateinit var preferencesBuilder: PreferencesBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesBuilder = PreferencesBuilder(this@UIWizard)

        setCustomTransformer(ZoomOutPageTransformer())
        val background = Color.parseColor("#212121")
        val backgroundLight = Color.parseColor("#383838")

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_1_title),
                getString(R.string.intro_1_content), R.drawable.launcher, backgroundLight))

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_2_title),
                getString(R.string.intro_2_content), R.drawable.intro_image_1, background))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_3_title),
                getString(R.string.intro_3_content), R.drawable.intro_image_2, background))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_4_title),
                getString(R.string.intro_4_content), R.drawable.intro_image_3, background))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_5_title),
                getString(R.string.intro_5_content), R.drawable.intro_image_4, background))


        showStatusBar(false)
        showSkipButton(true)

        setVibrate(true)
        setVibrateIntensity(65)

        setSkipText(getString(R.string.intro_skip))
        setDoneText(getString(R.string.intro_go))
    }

    override fun onBackPressed() {}

    override fun onSkipPressed(currentFragment: Fragment?) {
        preferencesBuilder.putBoolean("firstAppStart", false)
        startActivity(Intent(this@UIWizard, UIBoot::class.java))
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        preferencesBuilder.putBoolean("firstAppStart", false)
        startActivity(Intent(this@UIWizard, UIBoot::class.java))
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        // Do something when the slide changes.
    }

    private inner class ZoomOutPageTransformer : ViewPager.PageTransformer {

        override fun transformPage(view: View, position: Float) {
            val pageWidth = view.width
            val pageHeight = view.height

            when {
                position < -1 -> // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    view.alpha = 0f
                position <= 1 -> { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    if (position < 0) {
                        view.translationX = horzMargin - vertMargin / 2
                    } else {
                        view.translationX = -horzMargin + vertMargin / 2
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    view.scaleX = scaleFactor
                    view.scaleY = scaleFactor

                    // Fade the page relative to its size.
                    view.alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)

                }
                else -> // (1,+Infinity]
                    // This page is way off-screen to the right.
                    view.alpha = 0f
            }
        }
    }
}
