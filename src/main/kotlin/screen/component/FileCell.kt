package screen.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FileCell(
    modifier: Modifier = Modifier,
    name: String,
    isRemoveEnabled: Boolean,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .background(color = Color.LightGray)
    ) {
        Text(
            modifier = Modifier
                .weight(weight = 1f)
                .align(alignment = Alignment.CenterVertically),
            text = name,
        )

        Spacer(modifier = Modifier.size(8.dp))

        Button(
            enabled = isRemoveEnabled,
            content = {
                Text("Премахни")
            },
            onClick = {
                onRemoveClick.invoke()
            }
        )
    }
}

@Preview
@Composable
private fun FileCellPreview() {
    FileCell(
        name = "open_trades-2023.txt",
        onRemoveClick = {},
        isRemoveEnabled = true,
    )
}