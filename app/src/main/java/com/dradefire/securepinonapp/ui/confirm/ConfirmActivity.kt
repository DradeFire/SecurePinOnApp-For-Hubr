package com.dradefire.securepinonapp.ui.confirm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

class ConfirmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel = koinViewModel<ConfirmViewModel>()
            val pinCode by viewModel.pinCode.collectAsState()
            val isSettingPinCode =
                intent.getBooleanExtra("isSettingPinCode", false) || viewModel.isPinCodeNotExist

            LaunchedEffect(Unit) {
                viewModel.closeActivityEvent.collect {
                    finishAndRemoveTask()
                }
            }

            BackHandler {
                // block button
            }

            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    BlockScreen(
                        onButtonClick = {
                            viewModel.onButtonClick(it, isSettingPinCode)
                        },
                        pinCodeLength = pinCode.length,
                        isPinValid = viewModel.isPinValid,
                        title = if (isSettingPinCode) "Set PIN" else "Enter PIN",
                    )
                }
            }
        }
    }

    @Composable
    private fun BlockScreen(
        onButtonClick: (ConfirmViewModel.ButtonClickEvent) -> Unit,
        pinCodeLength: Int,
        isPinValid: Boolean,
        title: String,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title)
            Spacer(modifier = Modifier.height(32.dp))
            InputLine(
                pinCodeLength = pinCodeLength,
                isPinValid = isPinValid,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                NumberButton(number = 1, onButtonClick)
                Spacer(modifier = Modifier.width(8.dp))
                NumberButton(number = 2, onButtonClick)
                Spacer(modifier = Modifier.width(8.dp))
                NumberButton(number = 3, onButtonClick)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                NumberButton(number = 4, onButtonClick)
                Spacer(modifier = Modifier.width(8.dp))
                NumberButton(number = 5, onButtonClick)
                Spacer(modifier = Modifier.width(8.dp))
                NumberButton(number = 6, onButtonClick)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                NumberButton(number = 7, onButtonClick)
                Spacer(modifier = Modifier.width(8.dp))
                NumberButton(number = 8, onButtonClick)
                Spacer(modifier = Modifier.width(4.dp))
                NumberButton(number = 9, onButtonClick)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(68.dp))
                NumberButton(number = 0, onButtonClick)
                Spacer(modifier = Modifier.width(4.dp))
                DeleteButton(onClick = onButtonClick)
            }
        }
    }

    @Composable
    private fun NumberButton(
        number: Int,
        onClick: (ConfirmViewModel.ButtonClickEvent) -> Unit,
    ) {
        OutlinedButton(
            modifier = Modifier
                .size(60.dp),
            contentPadding = PaddingValues(0.dp),
            onClick = {
                onClick(ConfirmViewModel.ButtonClickEvent.Number(number))
            },
        ) {
            Text(
                fontSize = 20.sp,
                text = number.toString(),
            )
        }
    }

    @Composable
    private fun DeleteButton(
        onClick: (ConfirmViewModel.ButtonClickEvent) -> Unit,
    ) {
        OutlinedButton(
            contentPadding = PaddingValues(0.dp),
            onClick = {
                onClick(ConfirmViewModel.ButtonClickEvent.Delete)
            },
        ) {
            Text(
                fontSize = 12.sp,
                text = "Delete",
            )
        }
    }

    @Composable
    private fun InputLine(pinCodeLength: Int, isPinValid: Boolean) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            repeat(4) {
                InputBlock(
                    isEntered = it < pinCodeLength,
                    isPinValid = isPinValid,
                )
                if (it < 3) {
                    Spacer(modifier = Modifier.width(32.dp))
                }
            }
        }
    }

    @Composable
    private fun InputBlock(isEntered: Boolean = false, isPinValid: Boolean = false) {
        Canvas(
            modifier = Modifier.size(16.dp),
        ) {
            if (isPinValid) {
                drawCircle(
                    color = Color.Green,
                )
            } else {
                drawCircle(
                    color = Color.Black,
                )
                drawCircle(
                    color = if (isEntered) Color.Black else Color.White,
                    radius = size.minDimension / 2.8f,
                )
            }
        }
    }

    @Preview(
        showBackground = true,
        device = "id:Galaxy Nexus",
    )
    @Composable
    private fun Example1() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            BlockScreen({}, 1, isPinValid = false, title = "Set")
        }
    }

    @Preview(
        showBackground = true,
        device = "id:Galaxy Nexus",
    )
    @Composable
    private fun Example2() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            BlockScreen({}, 1, isPinValid = true, title = "Enter")
        }
    }
}
