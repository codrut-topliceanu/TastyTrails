# TastyTrails



**Tasty trails** is a simple app designed to search for and display recipes from spoonacular.com.

_A minimum of Android 10 is required for this app._ 

It's build around an MVVM architecture using the following techstack:
 * Jetpack Compose
 * Jetpack Compose Navigation
 * Material3 UI components
 * Hilt for dependency injection
 * Retrofit2 for Api calls + Moshi for deserialization
 * Coil for async image loading
 * Room for local db
 * DataStore Preferences for saving theme configuration
 * Mockk for testing



### App features:
 * Search for recipes by name or ingredient
 * Results displayed in a LazyColumn displaying name, summary, image and health score.
 * Ability to switch between current results, previously viewed(cached) and favorites lists.
 * Ability to sort the list alphabetically or by health score.
 * See details about each recipe such as image, name, health score, full summary, ingredients, instructions, link to source url. And ability to mark a recipe as favorite.
 * Supports Material3 (Material You) dynamic color and icon theming.
 * Relevant errors are displayed to the user in a snackbar.
 * Ability to alter DarkMode(Auto/On/Off) and DynamicTheme(On/Off)



### How to run this project:
1. Download source code.
2. In the root folder of the project create a new file called `apikey.properties`.
3. Add `SPOON_API_KEY = "YOUR API KEY HERE"` to `apikey.properties`(you can get your API key by signing up here: https://spoonacular.com/food-api/pricing ).
4. Open in Android Studio.
5. Build and Run.



### App graph description:
**Tasty trails** is built in such a way as to allow further expansion of the app with more features. 
It's _main_ feature is the searching(by name or ingredients) and listing of recipes.
Adding new features(flows) can be easily achieved by adding a new package next to `.main` and creating another nested navigation graph in `MainNavigation`

**App architecture graph:**

<img width="822" alt="image" src="https://github.com/codrut-topliceanu/TastyTrails/assets/60002907/727e2ae3-d7c2-435e-a1ba-119e102071d7">




### Possible improvements:
* Some sort of lazy loading like Paging3. (Though spoonacular's free plan is a bit restrictive)
* Add a useCases class to stand between the ViewModel and the `RecipeRepository`, so that other future parts of the app can access the same functionality without having to duplicate all the logic from the `SearchViewModel`.
* Better support for landscape mode.
* More Compose Animations. Or [Lottie animations](http://airbnb.io/lottie/#/community-showcase).
* Show more info for each recipe card in the results list (cooking time, diets, cuisine, etc).
* An "Advanced Search" that can take advantage of the large array of search options the API has available.
* Auto-complete search options.
* Integration testing.



### Posibile features worth adding:
* A bot to read out loud the cooking instructions as the user is following along.
* User personalization : load preferences/intolerances into a user profile and work off that when searching for recipes.
* Calorie calculator/tracker.

### Demo:



https://github.com/codrut-topliceanu/TastyTrails/assets/60002907/882d2df1-63d0-41e9-9bd9-e259d7f7c1cd


