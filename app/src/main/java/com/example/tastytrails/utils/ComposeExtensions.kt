package com.example.tastytrails.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

/**
 * Adds a clickable to a modifier without a ripple effect.
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

/**
 * Adds a clickable to a modifier with a ripple effect.
 * The ripple effect is unbounded and will expand outside the bounds of the composable function.
 */
fun Modifier.unboundedRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    clickable(enabled = enabled,
        indication = rememberRipple(bounded = false, radius = 24.dp),
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}