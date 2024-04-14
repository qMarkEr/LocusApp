package com.marker.locus.ui.theme

import android.widget.Toast
import com.marker.locus.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marker.locus.SignInState

@Composable
fun SignInUI(state: SignInState,
             onSignInClick: () -> Unit) {

    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    Box (modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                text = "Locus",
                fontWeight = FontWeight.ExtraLight,
                fontFamily = FontFamily.Default,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontSize = 100.sp
            )
            Divider(
                color = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                thickness = 2.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(150.dp)
            )
        }
        TextButton(
            onClick = { onSignInClick() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(70.dp),
            shape = CircleShape,
        ) {
            Icon(painterResource(id = R.drawable.borow_launcher_foreground_icon), contentDescription = "AAA")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Sign In", fontSize = 20.sp)
        }
    }
}