package it.appm.myunical.ui.screen.homescreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.appm.myunical.R
import it.appm.myunical.ui.screen.libretto.LibrettoActivity
import it.appm.myunical.ui.screen.schedule.ScheduleActivity


@Composable
fun StudentMenu(){
    val context = LocalContext.current
    Column {
        Text(
            text = stringResource(R.string.student),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ActionButton(stringResource(R.string.libretto), Icons.Filled.Book){
                context.startActivity(Intent(context, LibrettoActivity::class.java))
            }
            ActionButton(stringResource(R.string.esami), Icons.Filled.School)
            ActionButton(stringResource(R.string.orario), Icons.Filled.CalendarToday){
                context.startActivity(Intent(context, ScheduleActivity::class.java))
            }
            ActionButton(stringResource(R.string.tasse), Icons.Filled.AttachMoney)
        }
    }
}

@Composable
fun UniversitySubmenu(){
    val context = LocalContext.current
    Column {
        Text(
            text = stringResource(R.string.university),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ActionButton(stringResource(R.string.website), Icons.Filled.TravelExplore){
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.unical.it"))
                context.startActivity(intent)
            }
            ActionButton(stringResource(R.string.esse3), Icons.Filled.Computer){
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.unical.esse3.cineca.it"))
                context.startActivity(intent)
            }
            ActionButton(stringResource(R.string.map), Icons.Filled.Map){
                val mapLink = "https://www.google.com/maps/d/embed?mid=17RXFIMpyGYs5LC7TGMFLzcFw52Q&ehbc=2E312F"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapLink))
                context.startActivity(intent)
            }

            ActionButton(stringResource(R.string.unical_life), Icons.Filled.RestaurantMenu){
                avviaUnicalLife(context)
            }
        }
        Spacer(Modifier.size(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ActionButton(
                label = stringResource(R.string.facebook),
                icon = ImageVector.vectorResource(R.drawable.facebook_logo),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/unicalufficiale/"))
                    context.startActivity(intent)
                }
            )
            ActionButton(
                label = stringResource(R.string.instagram),
                icon = ImageVector.vectorResource(R.drawable.instagram_logo),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/unical_official/"))
                    context.startActivity(intent)
                }
            )
            ActionButton(
                label = stringResource(R.string.youtube),
                icon = ImageVector.vectorResource(R.drawable.youtube_logo),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@campusunical"))
                    context.startActivity(intent)
                }
            )

            ActionButton(
                label = stringResource(R.string.twitter_x),
                icon = ImageVector.vectorResource(R.drawable.x_logo),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/UniCalPortale"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

private fun avviaUnicalLife(ctx: Context){
    val pkg = "it.seleuco.unical_cr_app"
    val intent = ctx.packageManager.getLaunchIntentForPackage(pkg)
    if (intent != null) {
        ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } else {
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(playStoreIntent)
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
){
    Column(
        modifier = Modifier.size(96.dp).then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FilledIconButton (
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(),
            shape = MaterialTheme.shapes.medium,
            content = {
                Icon(icon, null, Modifier.size(32.dp))
            }
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = label,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium)
    }
}