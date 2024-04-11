package com.marker.locus

import android.os.Bundle
import android.util.Log
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomSheetScaffoldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marker.locus.ui.theme.LocusTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lst = mutableStateListOf("");
        setContent {
            LocusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                     BottomSheetScaffold (
                         sheetContent = {
                             Column (modifier = Modifier.fillMaxHeight()) {
                                 Text(
                                     text = "Contacts",
                                     fontFamily = FontFamily.Default,
                                     fontWeight = FontWeight.Bold,
                                     modifier = Modifier
                                         .align(Alignment.CenterHorizontally)
                                         .padding(20.dp),
                                     textAlign = TextAlign.Center,
                                     fontSize = 30.sp
                                 )
                                 TextButton(onClick = { /*TODO*/ }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                     Icon(
                                         imageVector = Icons.Default.Add,
                                         contentDescription = "add",
                                         modifier = Modifier.size(18.dp)
                                     )
                                     Spacer(modifier = Modifier.width(8.dp))
                                     Text(text = "Add new")
                                 }
                                 ContactCard("Scoof Borov", id = "3et21f", lst)
                                 ContactCard("Alt borovitski", id = "4hg4q23hgb", lst)
                                 ContactCard("Alexiy Chuvashskiy", id = "4hb3qh34", lst)
                             }
                         },
                         content =  {
                            LazyColumn {
                              items(lst.size) {
                                  Text(text = lst[it])
                              }
                            }
                         },
                         sheetPeekHeight = BottomSheetScaffoldDefaults.SheetPeekHeight
                     )
                }
            }
        }
    }
}