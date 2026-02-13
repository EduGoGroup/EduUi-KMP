package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.tokens.ButtonSpacing

@Composable
fun DSFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Sizes.iconLarge),
                color = ButtonDefaults.buttonColors().contentColor,
                strokeWidth = ButtonSpacing.iconSpacing / 4,
            )
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
        }
        leadingIcon?.let { icon ->
            if (!loading) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.iconMedium),
                )
                Spacer(Modifier.width(ButtonSpacing.iconSpacing))
            }
        }
        Text(text)
        trailingIcon?.let { icon ->
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Sizes.iconMedium),
            )
        }
    }
}
