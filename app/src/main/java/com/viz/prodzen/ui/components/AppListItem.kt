package com.viz.prodzen.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.viz.prodzen.data.model.AppInfo

@Composable
fun AppListItem(
    appInfo: AppInfo,
    isChecked: Boolean, // NEW
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = appInfo.icon),
            contentDescription = "${appInfo.appName} icon",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = appInfo.appName,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = isChecked, // UPDATED
            onCheckedChange = onCheckedChange
        )
    }
}