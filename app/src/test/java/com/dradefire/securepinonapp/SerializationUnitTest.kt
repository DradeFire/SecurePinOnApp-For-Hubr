package com.dradefire.securepinonapp

import com.google.gson.Gson
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SerializationUnitTest {
    private val packageForPinList = listOf(
        "android.apps.messaging",
        "chrome",
        "android.dialer",
        "browser",
        "mms",
    )

    private val gson = Gson()

    @Test
    fun arraySerializationCheck() {
        val encoded = gson.toJson(packageForPinList)
        println(encoded)

        val decoded = gson.fromJson(encoded, List::class.java) as List<*>
        println(decoded)

        decoded.forEachIndexed { index, str ->
            assertEquals(packageForPinList[index], str)
        }
    }

    @Test(expected = com.google.gson.JsonSyntaxException::class)
    fun arraySerializationCheckNull() {
        val encoded = gson.toJson("")
        println(encoded)

        val decoded = gson.fromJson(encoded, List::class.java) as List<*>
    }

    @Test
    fun arraySerializationEmptyList() {
        val encoded = gson.toJson(emptyList<String>())
        println(encoded)

        val decoded = gson.fromJson(encoded, List::class.java) as List<*>
        println(decoded)

        assert(decoded.isEmpty())
    }
}