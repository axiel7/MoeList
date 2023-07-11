package com.axiel7.moelist.uicompose.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberPlainTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import kotlinx.coroutines.launch

@Composable
fun TextIconHorizontal(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            modifier = Modifier.padding(end = 4.dp),
            tint = color
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp),
            color = color,
            fontSize = fontSize
        )
    }
}

@Composable
fun TextIconVertical(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = TextUnit.Unspecified,
    isLoading: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            modifier = Modifier.padding(4.dp),
            tint = color
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .defaultPlaceholder(visible = isLoading),
            color = color,
            fontSize = fontSize
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextIconVertical(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tooltip: String,
    isLoading: Boolean = false,
) {
    val tooltipState = rememberPlainTooltipState()
    val scope = rememberCoroutineScope()

    PlainTooltipBox(
        tooltip = { Text(text = tooltip) },
        tooltipState = tooltipState
    ) {
        TextIconVertical(
            text = text,
            icon = icon,
            modifier = modifier.clickable { scope.launch { tooltipState.show() } },
            isLoading = isLoading
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TextIconHorizontalPreview() {
    MoeListTheme {
        TextIconHorizontal(text = "This is an example", icon = R.drawable.ic_round_details_star_24)
    }
}

@Preview(showBackground = true)
@Composable
fun TextIconVerticalPreview() {
    MoeListTheme {
        TextIconVertical(text = "This is an example", icon = R.drawable.ic_round_details_star_24)
    }
}