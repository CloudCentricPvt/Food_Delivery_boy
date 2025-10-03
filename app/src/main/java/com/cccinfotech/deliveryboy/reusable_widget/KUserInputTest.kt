package com.cccinfotech.deliveryboy.reusable_widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cccinfotech.deliveryboy.R

class KUserInputTest {
    @Composable
    fun UserTextField(
        value: String,
        onValueChange: (String) -> Unit,
        hint: String,
        isError: Boolean = false,
        textColor: Color = Color.Black,
        hintColor: Color = Color.Gray,
        letterSpacing: TextUnit = 1.sp,
        fontSize: TextUnit = 16.sp,
        fontWeight: FontWeight = FontWeight.Normal,
        keyboardType: KeyboardType = KeyboardType.Text,
        shape: RoundedCornerShape = RoundedCornerShape(12.dp),
        shapeColor: Color = Color(LocalContext.current.getColor(R.color.purple_200))


    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = hint,
                    color = hintColor,
                    fontSize = fontSize,
                    fontWeight = fontWeight

                )

            })

    }
}