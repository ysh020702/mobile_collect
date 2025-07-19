package com.example.collecthealthdata.presentation.activity.UserInputActivity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.collecthealthdata.domain.User

@Composable
fun UserInputScreen(user: User, onSave: (User) -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var age by remember { mutableStateOf(user.age.toString()) }
    var weight by remember { mutableStateOf(user.Weight.toString()) }
    var smm by remember { mutableStateOf(user.SMM.toString()) }
    var bfm by remember { mutableStateOf(user.BFM.toString()) }
    var pbf by remember { mutableStateOf(user.PBF.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "삼성 헬스에서 측정한\n사용자 정보 입력", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

        // 입력 필드
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") })
        OutlinedTextField(
            value = age, onValueChange = { age = it },
            label = { Text("나이") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = weight, onValueChange = { weight = it },
            label = { Text("체중 (kg)") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = smm, onValueChange = { smm = it },
            label = { Text("골격근량 (kg)") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = bfm, onValueChange = { bfm = it },
            label = { Text("체지방량 (kg)") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = pbf, onValueChange = { pbf = it },
            label = { Text("체지방률 (%)") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val newUser = User(
                    id = user.id,
                    email = user.email,
                    name = name,
                    age = age.toIntOrNull() ?: -1,
                    Weight = weight.toFloatOrNull() ?: -1.0f,
                    SMM = smm.toFloatOrNull() ?: -1.0f,
                    BFM = bfm.toFloatOrNull() ?: -1.0f,
                    PBF = pbf.toFloatOrNull() ?: -1.0f
                )
                onSave(newUser)
            }
        ) {
            Text("저장")
        }
    }
}
