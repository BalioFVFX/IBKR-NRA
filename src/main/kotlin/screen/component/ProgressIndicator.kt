package screen.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    percent: Int,
) {
    Column(
        modifier = modifier
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = percent / 100f,
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            modifier = Modifier.align(alignment = Alignment.End),
            text = "Завършено: $percent%"
        )
    }
}

@Preview
@Composable
fun ProgressIndicatorPreview() {
    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        ProgressIndicator(
            percent = 53,
        )
    }
}