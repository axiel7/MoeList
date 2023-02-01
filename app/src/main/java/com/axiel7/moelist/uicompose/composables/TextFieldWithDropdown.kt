package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithDropdown(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    list: List<String>,
    label: String = ""
) {
    var dropDownExpanded by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) dropDownExpanded = true
                },
            value = value,
            onValueChange = onValueChange,
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { dropDownExpanded = !dropDownExpanded }) {
                    Icon(
                        painter = painterResource(id =
                        if (dropDownExpanded) com.axiel7.moelist.R.drawable.ic_round_keyboard_arrow_up_24
                        else com.axiel7.moelist.R.drawable.ic_round_keyboard_arrow_down_24),
                        contentDescription = "dropdown",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None)
        )
        DropdownMenu(
            expanded = dropDownExpanded,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = { dropDownExpanded = false },
            //modifier = Modifier.fillMaxWidth()
        ) {
            list.forEach { text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onValueChange(text)
                        dropDownExpanded = false
                    }
                )
            }
        }
    }
}