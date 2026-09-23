package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OffGrid Connect", appName)
  }

  @Test
  fun `test crypto key generation and safety number`() {
    val crypto = com.example.crypto.CryptoManager()
    val pubKey = crypto.exportPublicKey()
    assert(pubKey.isNotEmpty())
    val safetyNumber = crypto.calculateSafetyNumber(pubKey)
    assert(safetyNumber.numericCode.length == 7) // e.g. "123-456"
  }
}
