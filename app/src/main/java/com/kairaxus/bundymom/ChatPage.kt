package com.kairaxus.bundymom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit  // Add this parameter
) {
    Column(
        modifier = modifier
            .background(Color(0xFFFFF5F5))
    ) {
        AppHeader(onBackClick = onBackClick)  // Pass it to AppHeader
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList
        )
        MessageInput(
            onMessageSend = {
                viewModel.sendMessage(it)
            }
        )
    }
}

@Composable
fun AppHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFB6C1))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Add back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }




            Image(
                modifier = Modifier.padding(start = 4.dp).size(30.dp),
                painter = painterResource(id = R.drawable.ai), // replace with your image name
                contentDescription = "Pregnancy icon"
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "AI MIDWIFE CHAT BOT",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    if(messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.size(200.dp),
                painter = painterResource(id = R.drawable.ar_baby_icon),
                contentDescription = "Welcome Icon",
            )
            Text(
                text = "Hello Amma! ❤️\nI'm here to help with your pregnancy journey.",
                fontSize = 18.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) {
                MessageRow(messageModel = it)
            }
        }
    }
}

@Composable
fun MessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"
    val bubbleColor = if (isModel) Color(0xFFFFF0F5) else Color(0xFFFFB6C1)
    val textColor = if (isModel) Color(0xFF555555) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isModel) 0.dp else 16.dp,
                        bottomEnd = if (isModel) 16.dp else 0.dp
                    )
                )
                .background(bubbleColor)
                .padding(16.dp)
        ) {
            SelectionContainer {
                Text(
                    text = messageModel.message,
                    fontWeight = FontWeight.Normal,
                    color = textColor,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            value = message,
            onValueChange = { message = it },
            placeholder = {
                Text("Ask about your pregnancy...", color = Color.LightGray)
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFFFB6C1),
                unfocusedBorderColor = Color(0xFFFFD1DC),
                cursorColor = Color(0xFFFF69B4)
            )
        )

        IconButton(
            onClick = {
                if(message.isNotEmpty()) {
                    onMessageSend(message)
                    message = ""
                }
            },
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
                .background(
                    color = Color(0xFFFF69B4),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = Color.White
            )
        }
    }
}