package screen.dialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class ConversionCompleteDialogUi(
    val notices: Int,
    val errors: Int,
)

@Composable
fun ConversionCompletedDialog(
    ui: ConversionCompleteDialogUi,
    onDismissAction: () -> Unit,
    onShowResultAction: () -> Unit,
) {
    Dialog(
        onDismissRequest = {

        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        content = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)

            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Конвентирането завърши",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Забележки: ${ui.notices}",
                )

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Грешки: ${ui.errors}",
                )

                Spacer(modifier = Modifier.size(8.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onDismissAction,
                    content = {
                        Text(
                            text = "Затвори",
                            color = Color.Black,
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                    ),
                    border = BorderStroke(1.dp, Color.Black)
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onShowResultAction,
                    content = {
                        Text("Покажи резултат")
                    },
                )
            }
        }
    )
}

@Preview
@Composable
private fun ConversionCompletedDialogPreview() {
    ConversionCompletedDialog(
        ui = ConversionCompleteDialogUi(
            notices = 3,
            errors = 2,
        ),
        onDismissAction = {},
        onShowResultAction = { },
    )
}
