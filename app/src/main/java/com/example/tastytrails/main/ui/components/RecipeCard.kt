package com.example.tastytrails.main.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tastytrails.R
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.utils.noRippleClickable

/**
 * Displays a custom card for a recipe.
 * Contains AsyncImage, title, health score, truncated description.
 * @param recipe the recipe to display
 * @param onRecipeClicked lambda to execute on click
 */
@Composable
fun RecipeCard(
    recipe: Recipe,
    onRecipeClicked: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .noRippleClickable {
                onRecipeClicked()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            Modifier
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                modifier = Modifier
                    .height(100.dp)
                    .width(135.dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.cd_image_of_recipe),
                placeholder = painterResource(R.drawable.downloading_icon),
                error = painterResource(R.drawable.cloud_off_icon),
                fallback = painterResource(R.drawable.cloud_off_icon),
            )
            Column(
                modifier = Modifier
                    .padding(start = 5.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                var summary =
                    HtmlCompat.fromHtml(recipe.summary, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                summary = if (summary.isBlank()) {
                    stringResource(R.string.no_description_available_error)
                } else {
                    "${recipe.healthScore}❤ : $summary"
                }
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeCardPreview() {
    RecipeCard(
        recipe = Recipe(
            id = 7724,
            title = "Pasta With Tuna",
            healthScore = 30,
            imageUrl = null,
            summary = "Pasta With Tuna is a pescatarian main course. This recipe serves 4.",
        ),
    )
}