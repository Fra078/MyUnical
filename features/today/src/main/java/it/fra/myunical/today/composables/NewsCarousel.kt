package it.fra.myunical.today.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselItemDrawInfo
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import it.fra.myunical.today.models.NoticeUiModel
import it.fra.myunical.ui.animation.aboveSharedElements
import it.fra.myunical.ui.animation.newsImageKey
import it.fra.myunical.ui.animation.sharedImage
import kotlin.math.roundToInt

private val CornerRadius = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsCarousel(
    notices: List<NoticeUiModel>,
    onNoticeClick: (NoticeUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { notices.size },
        preferredItemWidth = 280.dp,
        itemSpacing = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
    ) { index ->
        val notice = notices[index]
        // Items are laid out at full size and masked: placing the card on the visible area
        // keeps the shared image bounds equal to what the user sees
        Box(
            Modifier
                .fillMaxSize()
                .fitToMask(carouselItemDrawInfo)
                .clip(RoundedCornerShape(CornerRadius))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable { onNoticeClick(notice) }
        ) {
            if (notice.imageUrl != null) {
                AsyncImage(
                    model = notice.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedImage(newsImageKey(notice.id), cornerRadius = CornerRadius)
                )
            } else {
                Icon(
                    Icons.Rounded.Newspaper,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                )
            }
            // Gradient keeps the caption readable on any picture; both stay above the image
            // when it flies back from the detail
            Box(
                Modifier
                    .fillMaxSize()
                    .aboveSharedElements()
                    .clip(RoundedCornerShape(CornerRadius))
                    .background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.8f)))
            )
            // As wide as the largest item: it doesn't reflow while the item shrinks, it gets masked
            val captionWidth = with(LocalDensity.current) { carouselItemDrawInfo.maxSize.toDp() }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .aboveSharedElements()
                    .wrapContentWidth(Alignment.Start, unbounded = true)
                    .width(captionWidth)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    listOf(notice.category, notice.date).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
                Text(
                    notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Measures and places the content on the visible (masked) part of a carousel item. */
@OptIn(ExperimentalMaterial3Api::class)
private fun Modifier.fitToMask(drawInfo: CarouselItemDrawInfo): Modifier = layout { measurable, constraints ->
    val mask = drawInfo.maskRect
    val width = mask.width.roundToInt().takeIf { it > 0 } ?: constraints.maxWidth
    val height = mask.height.roundToInt().takeIf { it > 0 } ?: constraints.maxHeight
    val placeable = measurable.measure(Constraints.fixed(width, height))
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(mask.left.roundToInt(), mask.top.roundToInt())
    }
}
