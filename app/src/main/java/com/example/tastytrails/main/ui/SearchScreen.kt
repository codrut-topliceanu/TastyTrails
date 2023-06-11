package com.example.tastytrails.main.ui

import android.content.Context
import android.text.Spanned
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tastytrails.R
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.ui.theme.TastyTrailsTheme


enum class FilterOptions(val value: String) { // todo implement sort, add more. move somewhere else
    A_Z("A-Z"),
    Z_A("Z-A"),
    healthScore("Health Score") // todo
}

// TODO: remember to handle configuration changes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val contextMenuVisible = rememberSaveable { mutableStateOf(false) }

    Scaffold(modifier = Modifier
        .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(id = R.string.app_name))
                },
                actions = TopBarActions(contextMenuVisible, viewModel, viewState.currentSort),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer

                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(top = 20.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchHeader(
                context = context,
                viewModel = viewModel,
                searchQuery = viewState.searchQuery,
                searchByName = viewState.searchByName
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // Search results
                itemsIndexed(viewState.recipesList) { _, item: Recipe ->
                    RecipeCard(recipe = item)
                }
                //TODO: use item keys to keep list from unnecessary recompositions?

                // Cached results
                if (viewState.cachedRecipesList.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.previously_viewed_label),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    itemsIndexed(viewState.cachedRecipesList) { _, item: Recipe ->
                        RecipeCard(recipe = item)
                    }
                }
            }
        }
    }

}

@Composable
private fun TopBarActions(
    contextMenuVisible: MutableState<Boolean>,
    viewModel: SearchViewModel,
    currentSort: FilterOptions
): @Composable() (RowScope.() -> Unit) =
    {
        Image(
            modifier = Modifier
                .height(50.dp)
                .width(50.dp)
                .padding(end = 10.dp)
                .unboundedRippleClickable {
                    contextMenuVisible.value = !contextMenuVisible.value
                },
            painter = painterResource(id = R.drawable.filter_list_icon),
            contentScale = ContentScale.Inside,
            contentDescription = stringResource(R.string.cd_filter_button),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
        )
        DropdownMenu(
            expanded = contextMenuVisible.value,
            onDismissRequest = { contextMenuVisible.value = false }) {
            DropdownMenuItem(
                text = { Text(text = FilterOptions.A_Z.value) },
                onClick = {
                    viewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateCurrentSort(FilterOptions.A_Z)
                    )
                    contextMenuVisible.value = false
                },
                leadingIcon = {
                    if (currentSort.value == FilterOptions.A_Z.value) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = stringResource(R.string.cd_checked),
                            contentScale = ContentScale.Inside,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            )
            DropdownMenuItem(
                text = { Text(text = FilterOptions.Z_A.value) },
                onClick = {
                    viewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateCurrentSort(FilterOptions.Z_A)
                    )

                    contextMenuVisible.value = false
                },
                leadingIcon = {
                    if (currentSort.value == FilterOptions.Z_A.value) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = stringResource(R.string.cd_checked),
                            contentScale = ContentScale.Inside,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            )
            DropdownMenuItem(
                text = { Text(text = FilterOptions.healthScore.value) },
                onClick = {
                    viewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateCurrentSort(FilterOptions.healthScore)
                    )
                    contextMenuVisible.value = false
                },
                leadingIcon = {
                    if (currentSort.value == FilterOptions.healthScore.value) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = stringResource(R.string.cd_checked),
                            contentScale = ContentScale.Inside,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            )

        }
    }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchHeader(
    context: Context,
    viewModel: SearchViewModel,
    searchQuery: String,
    searchByName: Boolean
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = searchQuery,
        onValueChange = { newValue: String ->
            viewModel.updateSearchScreenUiState(
                SearchScreenStateAction.UpdateSearchQuery(newValue)
            )

        },
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        maxLines = 1,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = {
            Text(
                text = if (searchByName) stringResource(R.string.dish_name_examples) else stringResource(
                    R.string.ingredients_examples
                ),
                style = MaterialTheme.typography.labelSmall,
            )
        },
        trailingIcon = {
            Image(
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .unboundedRippleClickable {
                        Toast
                            .makeText(context, "test search", Toast.LENGTH_SHORT)
                            .show() // TODO execute search
                    },
                painter = painterResource(id = R.drawable.search_icon),
                contentDescription = stringResource(R.string.cd_search_button),
                contentScale = ContentScale.Inside,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )

        },
    )
    Row {
        FilterChip(
            selected = searchByName,
            onClick = {
                viewModel.updateSearchScreenUiState(
                    SearchScreenStateAction.UpdateSearchByName(true)
                )
            },
            label = {
                Text(
                    stringResource(R.string.search_by_name),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            shape = RoundedCornerShape(
                topStart = 30.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp,
                bottomStart = 30.dp
            ),
        )
        FilterChip(
            selected = !searchByName,
            onClick = {
                viewModel.updateSearchScreenUiState(
                    SearchScreenStateAction.UpdateSearchByName(false)
                )
            },
            label = {
                Text(
                    stringResource(R.string.search_by_ingredients),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 30.dp,
                bottomEnd = 30.dp,
                bottomStart = 0.dp
            )
        )

    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

// TODO: move these to another file
private fun Modifier.unboundedRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = rememberRipple(bounded = false, radius = 24.dp),
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

@Composable
fun RecipeCard(recipe: Recipe) {
    Card(
        modifier = Modifier.padding(5.dp),
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
                placeholder = painterResource(R.drawable.cloud_off),
                error = painterResource(R.drawable.cloud_off),
                fallback = painterResource(R.drawable.cloud_off),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .padding(start = 5.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                val spannedText: Spanned = HtmlCompat.fromHtml(
                    recipe.summary.substringBefore(".") + ".",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                Text(
                    text = spannedText.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    TastyTrailsTheme {
        SearchScreen()
    }
}


@Preview(showBackground = true)
@Composable
fun RecipeCardPreview() {
    RecipeCard(
        Recipe(
            id = 7724,
            title = "Pasta With Tuna",
            imageUrl = null,
            summary = "Pasta With Tuna is a <b>pescatarian</b> main course. This recipe serves 4. For <b>\$1.68 per serving</b>, this recipe <b>covers 28%</b> of your daily requirements of vitamins and minerals. One serving contains <b>423 calories</b>, <b>24g of protein</b>, and <b>10g of fat</b>. 2 people have made this recipe and would make it again. This recipe from Foodista requires flour, parsley, non-fat milk, and parmesan cheese. From preparation to the plate, this recipe takes around <b>45 minutes</b>. All things considered, we decided this recipe <b>deserves a spoonacular score of 92%</b>. This score is amazing. <a href=\\\"https://spoonacular.com/recipes/pasta-and-tuna-salad-ensalada-de-pasta-y-atn-226303\\\">Pastan and Tuna Salad (Ensalada de Pasta y Atún)</a>, <a href=\\\"https://spoonacular.com/recipes/tuna-pasta-565100\\\">Tuna Pasta</a>, and <a href=\\\"https://spoonacular.com/recipes/tuna-pasta-89136\\\">Tuna Pasta</a> are very similar to this recipe.",
        )
    )
}