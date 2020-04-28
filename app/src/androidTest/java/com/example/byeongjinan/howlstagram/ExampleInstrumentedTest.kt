package com.example.byeongjinan.howlstagram

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
//import android.support.test.InstrumentationRegistry // deprecated
//import android.support.test.runner.AndroidJUnit4 // deprecated
//import androidx.test.ext.junit.runners.AndroidJUnit4 // 추가 : deprecated 대체
//import androidx.test.platform.app.InstrumentationRegistry //추가 : deprecated 대체

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getTargetContext() // androidX에서 사용이 안됨 제거
        var appContext = InstrumentationRegistry.getInstrumentation().targetContext // 추가 : deprecated 대체
        assertEquals("com.example.byeongjinan.howlstagram", appContext.packageName)
    }
}
