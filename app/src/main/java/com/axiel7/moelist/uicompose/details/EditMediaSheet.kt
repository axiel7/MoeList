package com.axiel7.moelist.uicompose.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.*
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaSheet(
    coroutineScope: CoroutineScope,
    sheetState: SheetState,
    viewModel: MediaDetailsViewModel,
) {
    val statusValues = if (viewModel.mediaType == MediaType.ANIME) listStatusAnimeValues() else listStatusMangaValues()
    var selectedStatus by remember { mutableStateOf(statusValues[0]) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { coroutineScope.launch { sheetState.hide() } }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    coroutineScope.launch { sheetState.hide() }
                }) {
                    Text(text = stringResource(R.string.cancel))
                }

                Button(onClick = { /*TODO*/ }) {
                    Text(text = stringResource(R.string.apply))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusValues.forEach { status ->
                    StatusItem(
                        status = status,
                        selectedStatus = selectedStatus,
                        onClick = {
                            selectedStatus = status
                        }
                    )
                }
            }

            OutlinedTextField(
                value = "0",
                onValueChange = { },
                label = { Text(text = stringResource(R.string.episodes)) },
            )
            /*TextFieldWithDropdown(
                options = if (viewModel.mediaType == MediaType.ANIME) listStatusAnimeValues().map { it.localized() }
                else listStatusMangaValues().map { it.localized() },
                onValueChange = { status = it },
                label = stringResource(R.string.status),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            )*/
        }//:Column
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusItem(
    status: ListStatus,
    selectedStatus: ListStatus,
    onClick: () -> Unit
) {
    val tooltipState = remember { PlainTooltipState() }
    val scope = rememberCoroutineScope()

    PlainTooltipBox(
        tooltip = { Text(status.localized()) },
        tooltipState = tooltipState
    ) {
        FilledIconToggleButton(
            checked = status == selectedStatus,
            onCheckedChange = {
                scope.launch { tooltipState.show() }
                onClick()
            }
        ) {
            Icon(painter = painterResource(status.icon()), contentDescription = "")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun EditMediaSheetPreview() {
    MoeListTheme {
        EditMediaSheet(
            coroutineScope = rememberCoroutineScope(),
            sheetState = rememberModalBottomSheetState(),
            viewModel = MediaDetailsViewModel(MediaType.ANIME)
        )
    }
}