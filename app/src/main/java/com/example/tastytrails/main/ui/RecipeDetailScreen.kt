package com.example.tastytrails.main.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tastytrails.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClicked: () -> Unit = {}
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()

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
        ) {

            AsyncImage(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(viewState.currentlySelectedRecipe?.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.cd_image_of_recipe),
                placeholder = painterResource(R.drawable.downloading_icon),
                error = painterResource(R.drawable.cloud_off_icon),
                fallback = painterResource(R.drawable.cloud_off_icon),
                contentScale = ContentScale.Fit
            )

            LaunchedEffect(key1 = viewState.currentlySelectedRecipe){
                Log.e("currentlySelectedRecipe", viewState.currentlySelectedRecipe.toString())
                Log.e("searchQuery", viewState.searchQuery.toString())
                Log.e("recipesList.size", viewState.recipesList.size.toString())
                Log.e("hiltViewModel", viewModel.toString())
            }
            viewState.currentlySelectedRecipe?.title?.let { Text(it) }
        }
    }
}

@Preview
@Composable
fun RecipeDetailScreenPreview() {
    RecipeDetailScreen()
}