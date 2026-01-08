package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.todo.data.Item

typealias OnItemFn = (id: String?) -> Unit

@Composable
fun ItemList(itemList: List<Item>, onItemClick: OnItemFn, modifier: Modifier) {
    Log.d("ItemList", "recompose")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(itemList) { item ->
            ItemDetail(item, onItemClick)
        }
    }
}

@Composable
fun ItemDetail(item: Item, onItemClick: OnItemFn) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Title: ${item.title}",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Price: ${item.price}",
            style = TextStyle(fontSize = 18.sp),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Date: ${item.date}",
            style = TextStyle(fontSize = 18.sp),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Sold: ",
                style = TextStyle(fontSize = 18.sp)
            )
            Checkbox(

                checked = item.sold,
                onCheckedChange = null /* Add the logic to handle checkbox state changes */,
                modifier = Modifier.padding(end = 8.dp)
            )

        }
        // You can add more Text components for other properties if needed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(item._id) }
        ) {
            Text(
                text = "Click to view details",
                color = Color.Blue,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
