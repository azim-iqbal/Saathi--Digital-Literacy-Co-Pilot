package com.saathi.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.saathi.core.UiNode

/** This is the privacy boundary: callers only receive redacted values. */
object NodeMasker {
    private val sensitiveTerms = listOf("pin", "otp", "password", "cvv", "mpin", "passcode", "security code")
    private const val MAX_NODES = 600

    fun flatten(root: AccessibilityNodeInfo): List<UiNode> {
        val nodes = mutableListOf<UiNode>()
        visit(root, nodes)
        return nodes
    }

    private fun visit(node: AccessibilityNodeInfo, into: MutableList<UiNode>) {
        if (into.size >= MAX_NODES) return
        val bounds = Rect().also(node::getBoundsInScreen)
        if (node.isVisibleToUser && !bounds.isEmpty) {
            val rawText = node.text?.toString()
            val rawDescription = node.contentDescription?.toString()
            val hint = if (android.os.Build.VERSION.SDK_INT >= 26) node.hintText?.toString() else null
            val id = node.viewIdResourceName
            val sensitive = isSensitive(node.isPassword, hint, id, rawDescription)
            into += UiNode(
                bounds = bounds,
                text = if (sensitive) null else rawText,
                description = if (sensitive) null else rawDescription,
                hint = if (sensitive) null else hint,
                resourceId = id,
                className = node.className?.toString(),
                isPassword = node.isPassword,
                isEnabled = node.isEnabled,
                isClickable = node.isClickable,
                isSensitive = sensitive
            )
        }
        for (index in 0 until node.childCount) {
            if (into.size >= MAX_NODES) return
            node.getChild(index)?.let { child ->
                try { visit(child, into) } finally { runCatching { child.recycle() } }
            }
        }
    }

    fun isSensitive(isPassword: Boolean, vararg values: String?): Boolean =
        isPassword || values.filterNotNull().any { value ->
            val normalized = value.lowercase()
            sensitiveTerms.any(normalized::contains)
        }
}
