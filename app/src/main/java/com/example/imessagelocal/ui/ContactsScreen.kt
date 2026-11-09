package com.example.imessagelocal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private data class PhoneContact(val id: Long, val name: String, val number: String)

@Composable
fun ContactsScreen(vm: AppViewModel, onOpen: (Long) -> Unit) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }
    var denied by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) contacts = loadContacts(context) else denied = true
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) contacts = loadContacts(context)
        else launcher.launch(Manifest.permission.READ_CONTACTS)
    }

    Column(Modifier.fillMaxSize().padding(bottom = 92.dp)) {
        Text("مخاطبین", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), singleLine = true, placeholder = { Text("جستجوی نام یا شماره") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
        if (denied) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("برای نمایش مخاطبین، مجوز مخاطبین لازم است", color = TextSecondary)
                    TextButton(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) { Text("اعطای مجوز", color = IOSBlue) }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(contacts.filter { it.name.contains(query, true) || it.number.contains(query, true) }, key = { "${it.id}:${it.number}" }) { c ->
                    Row(Modifier.fillMaxWidth().clickable { vm.createConversation(c.name, c.number) }.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF3D73B9)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.White)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, fontWeight = FontWeight.SemiBold)
                            Text(c.number, color = TextSecondary)
                        }
                        TextButton(onClick = {
                            vm.createConversation(c.name, c.number)
                        }) { Text("پیام", color = IOSBlue) }
                    }
                }
            }
        }
    }
}

private fun loadContacts(context: android.content.Context): List<PhoneContact> {
    val result = mutableListOf<PhoneContact>()
    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
    context.contentResolver.query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")?.use { c ->
        val id = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val name = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val number = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val seen = HashSet<String>()
        while (c.moveToNext()) {
            val n = c.getString(number).orEmpty()
            if (n.isNotBlank() && seen.add(android.telephony.PhoneNumberUtils.normalizeNumber(n).ifBlank { n.filter { ch -> ch.isDigit() } })) result += PhoneContact(c.getLong(id), c.getString(name).orEmpty(), n)
        }
    }
    return result
}
