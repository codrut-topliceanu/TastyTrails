package com.example.tastytrails.main.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastytrails.utils.noRippleClickable

/**
 * Simple themed snackbar.
 */
@Composable
fun TastySnackBar(data: SnackbarData) {
    Snackbar(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 55.dp)
            .noRippleClickable {
                data.dismiss()
            },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = data.visuals.message,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
