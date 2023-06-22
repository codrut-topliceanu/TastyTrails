package com.example.tastytrails.main.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastytrails.R
import com.example.tastytrails.datastore.ThemeSettings
import com.example.tastytrails.main.data.repository.MAX_RESULTS_PER_SEARCH
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.main.ui.components.RecipeCard
import com.example.tastytrails.main.ui.components.TastySnackBar
import com.example.tastytrails.utils.unboundedRippleClickable

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

    // Consumes the first snackBarMessage in list, recomposes when messages size changes.
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
        if (viewState.inProgress) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(top = 20.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchHeader(
                searchViewModel = searchViewModel,
                viewState = viewState,
                isQueryValid = isQueryValid
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // Header item
                if (viewState.recipesList.isNotEmpty()
                    && viewState.listDisplayMode != ListDisplayMode.CURRENT_SEARCH
                ) {
                    item {
                        val headerLabel = when (viewState.listDisplayMode) {
                            ListDisplayMode.PREVIOUSLY_VIEWED -> {
                                stringResource(id = R.string.header_previously_viewed)
                            }

                            ListDisplayMode.FAVORITES -> {
                                stringResource(id = R.string.header_favorites)
                            }

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
                            onRecipeClicked = {
                                searchViewModel.executeSaveViewedRecipe(item)
                                searchViewModel.updateSearchScreenUiState(
                                    SearchScreenStateAction.UpdateCurrentlySelectedRecipe
                                        (item)
                                )
                                onRecipeClicked()
                            }
                        )
                    }
                )
                item {
                    AnimatedVisibility(shouldShowLoadMoreButton(viewState)) {
                        AssistChip(modifier = Modifier.padding(15.dp),
                            onClick = {
                                searchViewModel.executeOnlineSearch(true)
                            },
                            enabled = !viewState.inProgress,
                            leadingIcon = {
                                Image(
                                    modifier = Modifier.height(20.dp),
                                    painter = painterResource(id = R.drawable.search_icon),
                                    contentDescription = "",
                                    contentScale = ContentScale.Inside,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.load_more_results),
                                    style = MaterialTheme.typography.labelSmall

                                )
                            }
                        )

                    }
                }
            }
            Row(
                modifier = Modifier.padding(bottom = 5.dp),
                content = listDisplayModeChips(viewState, searchViewModel)
            )
        }
    }
}

private fun shouldShowLoadMoreButton(viewState: SearchScreenUiState): Boolean {
    return viewState.resultsOffset >= MAX_RESULTS_PER_SEARCH &&
            viewState.listDisplayMode == ListDisplayMode.CURRENT_SEARCH
}

/**
 * Sets up the chips at the bottom of the screen that are used to
 * switch between list modes: [ListDisplayMode]
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun listDisplayModeChips(
    viewState: SearchScreenUiState,
    searchViewModel: SearchViewModel
): @Composable (RowScope.() -> Unit) =
    {
        FilterChip(
            selected = viewState.listDisplayMode == ListDisplayMode.CURRENT_SEARCH,
            onClick = {
                searchViewModel.executeChangeList(ListDisplayMode.CURRENT_SEARCH)
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
                searchViewModel.executeChangeList(ListDisplayMode.PREVIOUSLY_VIEWED)
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
                searchViewModel.executeChangeList(ListDisplayMode.FAVORITES)
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

/**
 * Sets up the topBar menu items, namely 'filter options' and 'theme settings'
 */
@Composable
private fun setupTopBarActions(
    contextMenuVisible: MutableState<Boolean>,
    searchViewModel: SearchViewModel,
    currentSort: FilterOptions,
): @Composable (RowScope.() -> Unit) =
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
                text = stringResource(R.string.darkmode)
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.darkmode_auto)) },
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
                text = { Text(text = stringResource(R.string.darkmode_dark)) },
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
                text = { Text(text = stringResource(R.string.darkmode_light)) },
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
                text = stringResource(R.string.dynamic_theme)
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.dynamic_theme_on)) },
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
                text = { Text(text = stringResource(R.string.dynamic_theme_off)) },
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
                text = { Text(text = stringResource(R.string.sort_by_none)) },
                onClick = {
                    searchViewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateCurrentSort(FilterOptions.NONE)
                    )
                    contextMenuVisible.value = false
                },
                leadingIcon = {
                    if (currentSort == FilterOptions.NONE) {
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

/**
 * Search bar component that contains the search input field and the
 * search by name/ingredients chips
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun SearchHeader(
    searchViewModel: SearchViewModel,
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

            searchViewModel.updateSearchScreenUiState(
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
                    searchViewModel.updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateListDisplayMode(ListDisplayMode.CURRENT_SEARCH)
                    )
                    searchViewModel.executeOnlineSearch()
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
                        searchViewModel.updateSearchScreenUiState(
                            SearchScreenStateAction.UpdateListDisplayMode(ListDisplayMode.CURRENT_SEARCH)
                        )
                        searchViewModel.executeOnlineSearch()
                        keyboardController?.hide()
                    },
                painter = painterResource(id = R.drawable.search_icon),
                contentDescription = stringResource(R.string.cd_search_button),
                contentScale = ContentScale.Inside,
                colorFilter = if (viewState.inProgress) {
                    ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                } else {
                    ColorFilter.tint(
                        MaterialTheme.colorScheme.primary
                    )
                },
            )

        },
    )
    Row {
        FilterChip(
            selected = viewState.searchByName,
            onClick = {
                searchViewModel.updateSearchScreenUiState(
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
                searchViewModel.updateSearchScreenUiState(
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
