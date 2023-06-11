package com.example.tastytrails.main.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tastytrails.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: SearchViewModel,
    onBackClicked: () -> Unit = {}
) {

    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val recipe = viewState.currentlySelectedRecipe
    val context = LocalContext.current
    val summaryExpanded = remember { mutableStateOf(false) }

    if (recipe == null) return

    Scaffold(modifier = Modifier
        .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                actions = {
                    Image(
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                            .padding(end = 10.dp)
                            .unboundedRippleClickable {
                                viewModel.executeSaveFavoriteRecipe(recipe, !recipe.favorite)
                            },
                        painter = painterResource(
                            id = if (recipe.favorite) R.drawable.star_icon else R.drawable.star_border_icon
                        ),
                        contentScale = ContentScale.Inside,
                        contentDescription = stringResource(
                            if (recipe.favorite) R.string.cd_un_favorite_button else R.string.cd_favorite_button
                        ),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                    )
                },
                navigationIcon = {
                    Image(
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                            .padding(end = 10.dp)
                            .unboundedRippleClickable {
                                onBackClicked()
                            },
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentScale = ContentScale.Inside,
                        contentDescription = stringResource(R.string.cd_back_button),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                    )

                }
            )

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(top = 20.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = CenterHorizontally
        ) {
            // Recipe image
            AsyncImage(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.cd_image_of_recipe),
                placeholder = painterResource(R.drawable.downloading_icon),
                error = painterResource(R.drawable.cloud_off_icon),
                fallback = painterResource(R.drawable.cloud_off_icon),
                contentScale = ContentScale.Fit
            )

            // Recipe title
            Text(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .align(CenterHorizontally),
                text = recipe.title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Justify
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Recipe summary
                item {
                    Card(
                        modifier = Modifier
                            .padding(5.dp)
                            .noRippleClickable {
                                summaryExpanded.value = !summaryExpanded.value
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        val summary = HtmlCompat.fromHtml(
                            recipe.summary, HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString()
                        Text(
                            modifier = Modifier
                                .padding(5.dp)
                                .animateContentSize(),
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (summaryExpanded.value) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                // Recipe Ingredients
                if (recipe.ingredients.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(top = 20.dp),
                            text = stringResource(R.string.ingredients),
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 5
                        )
                    }
                    itemsIndexed(recipe.ingredients) { _, ingredient ->
                        Text(
                            modifier = Modifier.padding(top = 5.dp),
                            text = ingredient,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                // Recipe Steps/Instructions
                if (recipe.instructions.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(top = 10.dp),
                            text = stringResource(R.string.steps_to_cook),
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 5
                        )
                    }
                    itemsIndexed(recipe.instructions) { index, instruction ->
                        Text(
                            modifier = Modifier.padding(top = 5.dp),
                            text = "${index + 1}: $instruction",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                if (recipe.spoonSourceUrl.isNotBlank()) {
                    item {
                        Text(
                            modifier = Modifier
                                .padding(top = 10.dp, bottom = 10.dp)
                                .noRippleClickable {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse(recipe.spoonSourceUrl)
                                    context.startActivity(intent)
                                },
                            text = recipe.spoonSourceUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis

                        )
                    }
                }
            }
        }
    }
}