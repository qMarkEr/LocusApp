package com.marker.locus.Composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.marker.locus.R

@Composable
fun ContactCard(name : String, id : String, picture : String) {
    var mode by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = picture,
            contentDescription = "wtf",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .padding(15.dp)
                .clip(CircleShape)
        )
        Box (modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 0.dp, vertical = 20.dp)) {
            Text(
                text = name,
                modifier = Modifier.align(Alignment.TopStart),
                textAlign = TextAlign.Center,

                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Default
            )
            Text(text = "Username: $id",
                modifier = Modifier
                    .align(Alignment.BottomStart),
                textAlign = TextAlign.Center,

                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Default
            )
        }

        Spacer(
            Modifier
                .weight(2f)
                .fillMaxHeight()
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(70.dp)
                .padding(15.dp),
            onClick = {
                mode = !mode
//                if (mode)
//                    lst += name
//                else
//                    lst -= name
            }
        ) {
            Icon(
                painter = painterResource(id =
                if (mode)
                    R.drawable.cancel_locating
                else
                    R.drawable.locate
                ),
                contentDescription = "main action"
            )
        }
    }
}