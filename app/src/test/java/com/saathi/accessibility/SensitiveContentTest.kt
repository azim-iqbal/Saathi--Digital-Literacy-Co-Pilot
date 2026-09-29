package com.saathi.accessibility

import org.junit.Assert.*
import org.junit.Test

class SensitiveContentTest {
    @Test fun `password flag masks unlabelled values`() { assertTrue(SensitiveContent.isSensitive(true, null)) }
    @Test fun `English Hindi and camel case identifiers are sensitive`() {
        listOf("OTP code", "पासवर्ड", "ओटीपी", "पिन", "cvv_input", "com.example:id/enterPin", "security code", "recovery-code").forEach {
            assertTrue(it, SensitiveContent.isSensitive(false, it))
        }
    }
    @Test fun `notification and ordinary text containing numeric secrets are withheld`() {
        listOf("Your code is 582139", "५८२१३९", "582139", "1234567890123456").forEach {
            assertTrue(it, SensitiveContent.isSensitive(false, it))
        }
    }
    @Test fun `shipping shopping and spinner are not pin matches`() {
        listOf("shipping", "shopping", "spinner", "Continue", "बिजली").forEach {
            assertFalse(it, SensitiveContent.isSensitive(false, it))
        }
    }
}
