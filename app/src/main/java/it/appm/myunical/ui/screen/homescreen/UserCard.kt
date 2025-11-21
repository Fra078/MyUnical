package it.appm.myunical.ui.screen.homescreen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.appm.myunical.R
import it.appm.myunical.data.DataRepository
import it.appm.myunical.model.Carriera


@Composable
internal fun UserCard(carriera: Carriera?, userPhoto: String?){
    ElevatedCard (
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xffc70505),
            contentColor = Color.White
        ),
        enabled = carriera != null,
        onClick = {},
        modifier = Modifier.height(192.dp).fillMaxWidth().padding(8.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentPhoto(
                photoUrl = userPhoto,
                modifier = Modifier.fillMaxHeight())
            if (carriera != null) {
                Spacer(Modifier.size(16.dp))
                Column {
                    Text(
                        text = carriera.nome,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = carriera.cognome,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.matricola)+": ${carriera.matricola}"
                    )
                    Text(
                        text = carriera.facolta
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentPhoto(
    photoUrl: String?,
    modifier: Modifier = Modifier
){
    Box(
        Modifier
            .background(Color.DarkGray.copy(alpha = 0.5f))
            .aspectRatio(3f/4, true)
            .then(modifier)
    ){
        if(!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}
