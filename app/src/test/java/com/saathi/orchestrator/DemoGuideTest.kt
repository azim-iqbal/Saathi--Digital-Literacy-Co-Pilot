package com.saathi.orchestrator

import android.graphics.Rect
import com.saathi.core.UiNode
import org.junit.Assert.assertEquals
import org.junit.Test

class DemoGuideTest {
    @Test
    fun `water goal highlights the water category`() {
        val step = DemoGuide.next("Pay my water bill", billerScreen(), "en-US", previousFailed = false)

        assertEquals("water_biller", step.target?.description)
    }

    @Test
    fun `dth goal highlights the DTH category`() {
        val step = DemoGuide.next("Recharge my DTH", billerScreen(), "en-US", previousFailed = false)

        assertEquals("dth_biller", step.target?.description)
    }

    private fun billerScreen() = listOf(
        node("electricity_biller"),
        node("water_biller"),
        node("dth_biller")
    )

    private fun node(id: String) = UiNode(
        bounds = Rect(0, 0, 100, 60),
        text = null,
        description = null,
        hint = null,
        resourceId = "com.saathi:id/$id",
        className = "android.widget.LinearLayout",
        isPassword = false,
        isEnabled = true,
        isClickable = true
    )
}
