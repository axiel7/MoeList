package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.axiel7.moelist.uicompose.theme.MoeListTheme

@Composable
fun TextCheckBox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun TextCheckBoxPreview() {
    MoeListTheme {
        TextCheckBox(
            text = "This is a CheckBox",
            checked = false,
            onCheckedChange = {}
        )
    }
}