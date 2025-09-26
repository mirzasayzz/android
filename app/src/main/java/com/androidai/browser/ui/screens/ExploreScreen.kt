package com.androidai.browser.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidai.browser.data.AiLink
import com.androidai.browser.data.AiLinksData
import com.androidai.browser.ui.WebViewActivity

@Composable
fun ExploreScreen() {
    val context = LocalContext.current
    val aiLinks = remember {
        AiLinksData.getDefaultLinks().filter { it.category != "Chatbots" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Discover AI Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(aiLinks.groupBy { it.category }.toList()) { (category, links) ->
            ExploreCategorySection(
                category = category,
                links = links,
                onLinkClick = { url ->
                    val intent = Intent(context, WebViewActivity::class.java).apply {
                        putExtra("url", url)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ExploreCategorySection(
    category: String,
    links: List<AiLink>,
    onLinkClick: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = getIconForCategory(category),
                contentDescription = category,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        links.forEach { link ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onLinkClick(link.url) },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = link.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = link.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "Writing" -> Icons.Default.Create
        "Coding" -> Icons.Default.Code
        "Image" -> Icons.Default.Image
        "Music" -> Icons.Default.MusicNote
        "Productivity" -> Icons.Default.Bolt
        else -> Icons.Default.Category
    }
} 