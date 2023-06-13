package com.example.tastytrails.main.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tastytrails.R
import com.example.tastytrails.datastore.ThemeSettings
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.ui.theme.TastyTrailsTheme
import com.example.tastytrails.utils.noRippleClickable
import com.example.tastytrails.utils.unboundedRippleClickable


// TODO : break this (and other) classes/ functions into smaller bits
// TODO: add tests for domain and data layers
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    onRecipeClicked: () -> Unit = {}
) {

    val viewState by searchViewModel.state.collectAsStateWithLifecycle()
    val contextMenuVisible = rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val isQueryValid = rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(key1 = viewState.snackBarMessages.size) {
        viewState.snackBarMessages.firstOrNull()?.let { snackBarMessage ->
            snackbarHostState.showSnackbar(snackBarMessage.message)
            searchViewModel.updateSearchScreenUiState(
                SearchScreenStateAction.RemoveFromSnackBarMessages(
                    snackBarMessage
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(id = R.string.app_name))
                },
                actions = setupTopBarActions(
                    contextMenuVisible,
                    searchViewModel,
                    viewState.currentSort
                ),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer

                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                TastySnackBar(data)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(top = 20.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchHeader(
                viewModel = searchViewModel,
                viewState = viewState,
                isQueryValid = isQueryValid
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // Loading indicator
                if (viewState.inProgress) {
                    item {
                        CircularProgressIndicator()
                    }
                }
                // Header item
                if (viewState.recipesList.isNotEmpty() && viewState.listDisplayMode != ListDisplayMode.CURRENT_SEARCH) {
                    item {
                        val headerLabel = when (viewState.listDisplayMode) {
                            ListDisplayMode.PREVIOUSLY_VIEWED -> stringResource(id = R.string.header_previously_viewed)
                            ListDisplayMode.FAVORITES -> stringResource(id = R.string.header_favorites)
                            else -> ""
                        }
                        Text(
                            text = headerLabel,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                // Search results
                itemsIndexed(viewState.recipesList,
                    key = { _, item: Recipe ->
                        item.id
                    },
                    itemContent = { _, item: Recipe ->
                        RecipeCard(
                            recipe = item,
                            saveRecipeExecute = {
                                searchViewModel.executeSaveViewedRecipe(item)
                            },
                            onRecipeClicked = {
                                searchViewModel.updateSearchScreenUiState(
                                    SearchScreenStateAction.UpdateCurrentlySelectedRecipe
                                        (item)
                                )
                                onRecipeClicked()
                            }
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.padding(bottom = 5.dp),
                content = listDisplayModeChips(viewState, searchViewModel)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun listDisplayModeChips(
    viewState: SearchScreenUiState,
    viewModel: SearchViewModel
): @Composable() (RowScope.() -> Unit) =
    {
        FilterChip(
            selected = viewState.listDisplayMode == ListDisplayMode.CURRENT_SEARCH,
            onClick = {
                viewModel.executeChangeList(ListDisplayMode.CURRENT_SEARCH)
            },
            enabled = !viewState.inProgress,
            label = {
                Text(
                    stringResource(R.string.search_chip),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        )
        FilterChip(
            modifier = Modifier.padding(start = 5.dp, end = 5.dp),
            selected = viewState.listDisplayMode == ListDisplayMode.PREVIOUSLY_VIEWED,
            onClick = {
                viewModel.executeChangeList(ListDisplayMode.PREVIOUSLY_VIEWED)
            },
            enabled = !viewState.inProgress,
            label = {
                Text(
                    stringResource(R.string.viewed_chip),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        )
        FilterChip(
            selected = viewState.listDisplayMode == ListDisplayMode.FAVORITES,
            onClick = {
                viewModel.executeChangeList(ListDisplayMode.FAVORITES)
            },
            enabled = !viewState.inProgress,
            label = {
                Text(
                    stringResource(R.string.favorites_chip),
                    style = MaterialTheme.typography.labelSmall
                )
            },
        )


    }

@Composable
private fun TastySnackBar(data: SnackbarData) {
    Snackbar(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 55.dp)
            .noRippleClickable {
                data.dismiss()
            },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = data.visuals.message,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun setupTopBarActions(
    contextMenuVisible: MutableState<Boolean>,
    searchViewModel: SearchViewModel,
    currentSort: FilterOptions,
): @Composable() (RowScope.() -> Unit) =
    {

        val themeState =
            searchViewModel.themeSettingsFlow.collectAsStateWithLifecycle(ThemeSettings())
        val themeContextMenuVisible = rememberSaveable { mutableStateOf(false) }

        // Theme Options Menu
        Image(
            modifier = Modifier
                .height(50.dp)
                .width(50.dp)
                .padding(end = 10.dp)
                .unboundedRippleClickable {
                    themeContextMenuVisible.value = !themeContextMenuVisible.value
                },
            painter = painterResource(id = R.drawable.settings_brightness_icon),
            contentScale = ContentScale.Inside,
            contentDescription = stringResource(R.string.cd_filter_button),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
        )

        DropdownMenu(
            expanded = themeContextMenuVisible.value,
            onDismissRequest = { themeContextMenuVisible.value = false }) {
            Text(
                modifier = Modifier.padding(start = 15.dp),
                text = "DarkMode"
            )
            DropdownMenuItem(
                text = { Text(text = "Auto") },
                onClick = {
                    themeContextMenuVisible.value = false
                    searchViewModel.executeUpdateThemeSettings(
                        themeState.value.copy(darkMode = ThemeSettings.DarkMode.Auto.ordinal)
                    )
                },
                leadingIcon = {
                    if (themeState.value.darkMode == ThemeSettings.DarkMode.Auto.ordinal) {
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
                text = { Text(text = "Dark") },
                onClick = {
                    themeContextMenuVisible.value = false
                    searchViewModel.executeUpdateThemeSettings(
                        themeState.value.copy(darkMode = ThemeSettings.DarkMode.Dark.ordinal)
                    )

                },
                leadingIcon = {
                    if (themeState.value.darkMode == ThemeSettings.DarkMode.Dark.ordinal) {
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
                text = { Text(text = "Light") },
                onClick = {
                    themeContextMenuVisible.value = false
                    searchViewModel.executeUpdateThemeSettings(
                        themeState.value.copy(darkMode = ThemeSettings.DarkMode.Light.ordinal)
                    )

                },
                leadingIcon = {
                    if (themeState.value.darkMode == ThemeSettings.DarkMode.Light.ordinal) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = stringResource(R.string.cd_checked),
                            contentScale = ContentScale.Inside,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            )
            Text(
                modifier = Modifier.padding(start = 15.dp),
                text = "Dynamic Theme"
            )
            DropdownMenuItem(
                text = { Text(text = "On") },
                onClick = {
                    themeContextMenuVisible.value = false
                    searchViewModel.executeUpdateThemeSettings(
                        themeState.value.copy(dynamicThemeEnabled = true)
                    )

                },
                leadingIcon = {
                    if (themeState.value.dynamicThemeEnabled) {
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
                text = { Text(text = "Off") },
                onClick = {
                    themeContextMenuVisible.value = false
                    searchViewModel.executeUpdateThemeSettings(
                        themeState.value.copy(dynamicThemeEnabled = false)
                    )

                },
                leadingIcon = {
                    if (!themeState.value.dynamicThemeEnabled) {
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

        // Sorting Options Menu
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
            Text(
                modifier = Modifier.padding(start = 15.dp),
                text = stringResource(R.string.filter_by)
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.sort_by_a_z)) },
                onClick = {
                    searchViewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateCurrentSort(FilterOptions.A_Z)
                    )
                    contextMenuVisible.value = false
                },
                leadingIcon = {
                    if (currentSort == FilterOptions.A_Z) {
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
                text = { Text(text = stringResource(R.string.sort_by_z_a)) },
                onClick = {
                    searchViewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateCurrentSort(FilterOptions.Z_A)
                    )

                    contextMenuVisible.value = false
                },
                leadingIcon = {
                    if (currentSort == FilterOptions.Z_A) {
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
                text = { Text(text = stringResource(R.string.sort_by_health_score)) },
                onClick = {
                    searchViewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateCurrentSort(FilterOptions.HEALTH_SCORE)
                    )
                    contextMenuVisible.value = false
                },
                leadingIcon = {
                    if (currentSort == FilterOptions.HEALTH_SCORE) {
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
private fun SearchHeader(
    viewModel: SearchViewModel,
    viewState: SearchScreenUiState,
    isQueryValid: MutableState<Boolean>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = viewState.searchQuery,
        onValueChange = { newValue: String ->
            isQueryValid.value =
                newValue.matches(Regex("^([a-zA-Z0-9-]+,?+\\s?)+\$"))

            viewModel.updateSearchScreenUiState(
                SearchScreenStateAction.UpdateSearchQuery(
                    newValue
                )
            )
        },
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        maxLines = 1,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = {
            if (isQueryValid.value) {
                Text(
                    text = if (viewState.searchByName) {
                        stringResource(R.string.dish_name_examples)
                    } else {
                        stringResource(R.string.ingredients_examples)
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
            } else {
                Text(
                    text = stringResource(R.string.incorrect_format_error),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
        isError = !isQueryValid.value,
        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                if (!viewState.inProgress && isQueryValid.value) {
                    viewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateListDisplayMode(ListDisplayMode.CURRENT_SEARCH)
                    )
                    viewModel.executeOnlineSearch()
                    keyboardController?.hide()
                }
            }
        ),
        trailingIcon = {
            Image(
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .unboundedRippleClickable(enabled = !viewState.inProgress && isQueryValid.value) {
                        viewModel.updateSearchScreenUiState(
                            SearchScreenStateAction.UpdateListDisplayMode(ListDisplayMode.CURRENT_SEARCH)
                        )
                        viewModel.executeOnlineSearch()
                        keyboardController?.hide()
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
            selected = viewState.searchByName,
            onClick = {
                viewModel.updateSearchScreenUiState(
                    SearchScreenStateAction.UpdateSearchByName(true)
                )
            },
            enabled = !viewState.inProgress,
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
            selected = !viewState.searchByName,
            onClick = {
                viewModel.updateSearchScreenUiState(
                    SearchScreenStateAction.UpdateSearchByName(false)
                )
            },
            enabled = !viewState.inProgress,
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

@Composable
fun RecipeCard(
    recipe: Recipe,
    saveRecipeExecute: () -> Unit = {},
    onRecipeClicked: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .noRippleClickable {
                saveRecipeExecute()
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
fun SearchScreenPreview() {
    TastyTrailsTheme {
        SearchScreen()
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
            summary = "Pasta With Tuna is a <b>pescatarian</b> main course. This recipe serves 4. For <b>\$1.68 per serving</b>, this recipe <b>covers 28%</b> of your daily requirements of vitamins and minerals. One serving contains <b>423 calories</b>, <b>24g of protein</b>, and <b>10g of fat</b>. 2 people have made this recipe and would make it again. This recipe from Foodista requires flour, parsley, non-fat milk, and parmesan cheese. From preparation to the plate, this recipe takes around <b>45 minutes</b>. All things considered, we decided this recipe <b>deserves a spoonacular score of 92%</b>. This score is amazing. <a href=\\\"https://spoonacular.com/recipes/pasta-and-tuna-salad-ensalada-de-pasta-y-atn-226303\\\">Pastan and Tuna Salad (Ensalada de Pasta y Atún)</a>, <a href=\\\"https://spoonacular.com/recipes/tuna-pasta-565100\\\">Tuna Pasta</a>, and <a href=\\\"https://spoonacular.com/recipes/tuna-pasta-89136\\\">Tuna Pasta</a> are very similar to this recipe.",
        ),
    )
}