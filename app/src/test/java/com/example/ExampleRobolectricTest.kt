package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.CurrencyFormatter
import com.example.util.DateUtil
import com.example.util.SecurityUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ລາຍຮັບ-ລາຍຈ່າຍ", appName)
  }

  @Test
  fun `security util hashing and verification`() {
    val salt = SecurityUtil.generateSalt()
    val hash = SecurityUtil.hashPassword("password123", salt)
    assertTrue(SecurityUtil.verifyPassword("password123", salt, hash))
    assertFalse(SecurityUtil.verifyPassword("wrongpass", salt, hash))
  }

  @Test
  fun `currency formatter formats correctly`() {
    val formattedLak = CurrencyFormatter.format(8000000.0, "LAK")
    assertTrue(formattedLak.contains("8,000,000"))
    assertTrue(formattedLak.contains("₭"))

    val formattedThb = CurrencyFormatter.format(1500.0, "THB")
    assertTrue(formattedThb.contains("1,500"))
    assertTrue(formattedThb.contains("฿"))

    val formattedUsd = CurrencyFormatter.format(250.0, "USD")
    assertTrue(formattedUsd.contains("250.00"))
    assertTrue(formattedUsd.contains("$"))
  }

  @Test
  fun `date util formats correctly`() {
    val today = DateUtil.todayString()
    assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
  }
}
