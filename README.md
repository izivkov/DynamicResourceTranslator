# Dynamic Resource Translator

DynamicResourceTranslator is an Android library that simplifies internationalization for your app. You only need to create 
a single `strings.xml` file in your native language (not necessarily English), and the library will 
automatically translate your app into the system language set on the user's phone "on-the-fly".

### Traditional Android Project
```
res/
├── values/
│   └── strings.xml
├── values-es/
│   └── strings.xml
├── values-fr/
│   └── strings.xml
```

### Using DynamicResourceTranslator
```
res/
├── values/
│   └── strings.xml
```
> **Note:** Language-specific directories (e.g., `values-es/`, `values-fr/`) are optional. If they exist, the API will use them.

## Features

- Dynamically translates string resources at runtime, eliminating the need for multiple language-specific `strings.xml`.
- Respects existing language-specific `strings.xml`.
- Supports fine-tuning of translation when automatic translations should be corrected ot shortened.
- Provides a pluggable translation engine architecture.

## How It Works

The library intercepts calls to `getString()` and `stringResource()`, which read from `strings.xml` resource files. 
It then uses a Google translation service to translate the strings based on the language set in the phone's settings. 
Translated values are stored in [local storage](https://developer.android.com/training/data-storage) for reuse and better performance.

## Prerequisites
Your app must have Internet access, at least the first time your app runs, to perform the initial translations.
Ensure you add the following permissions to your manifest:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
## Quick Start

Add the following to your **settings.gradle** file:

```groovy

dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      mavenCentral()
      maven { url 'https://jitpack.io' }
   }
}
```
Add the following to your **build.gradle** file:

```groovy
dependencies {
   implementation 'com.github.izivkov:DynamicResourceTranslator:Tag'
}
```

## Usage

### Getting Access to the API

#### Method 1: Use Singleton Object
The easiest way to get access to the API is through a Singleton object `DynamicResourceApi`, which wraps the class containing the API methods.

Initialize `DynamicResourceApi` once, typically in `MainActivity` or your `Application` class:
   ```kotlin
   DynamicResourceApi.init()
   ```
Optionally, during initialization, you can also set `language`, `overWrites` and `Translation Engine` like this: 

```kotlin
    DynamicResourceApi.init()
        .setOverwrites(arrayOf(
            ResourceLocaleKey(R.string.hello, Locale("es")) to {"Hola"},
            ResourceLocaleKey(R.string.hello, Locale("bg")) to {"Здравей %1\$s"}
        ))
        .setAppLocale(Locale("es"))
```
Setting the `App Locale` tells the library that your default `strings.xml` contains string in the specified language, Spanish in this case.
For more information, see [Application-language-vs-Default-Language](#application-language-vs-default-language).

Then retrieve the API anywhere in your program:
   ```kotlin
   val api = DynamicResourceApi.getApi()
```

#### Method 2: Create the API directly

```kotlin
    val api = DynamicTranslator()
        .init ()            
        .setAppLocale(Locale("es"))      // optional
        .setOverwrites(                 // optional
           arrayOf(
              ResourceLocaleKey(R.string.hello, Locale("es")) to {"Hola"},
              ResourceLocaleKey(R.string.hello, Locale("bg")) to {"Здравей %1\$s"}
           )
        )
```
This method is better suitable if you like to use [Dagger / Hilt](https://developer.android.com/training/dependency-injection/hilt-android) and inject the API in you code.

### Using it in your code
**Replace** `context.getString` calls with `api.getString`:
   ```kotlin
   // Before:
   val text = context.getString(R.string.hello_world) 
   
   // After:
   val text = api.getString(context, R.string.hello_world)
   ```

**For Jetpack Compose**, replace `stringResource` with `api.stringResource`:
   ```kotlin
   // Before:
   val text = stringResource(id = R.string.hello_world)

   // After:
   val text = api.stringResource(LocalContext.current, R.string.hello_world)
   ``` 
### Application Language vs. Default Language

`Application language` is the language of your default `strings.xml` file. If you create this file with German strings instead of English,
your **Application Language** is German. To avoid unnecessary translation, you should call 
`setAppLocale(Locale("de"))` during library initialization. If your `strings.xml` file is in English, there is no need to call this function.

On the other hand, the **Default Language** refers to the language currently set on the user's device. For example, if a user in Spain 
has their device set to Spanish, the default system language will be Spanish. This is the language into which strings should be translated.

## Documentation
API documentation can ge found [here](https://izivkov.github.io/DynamicResourceTranslator/api/org.avmedia.translateapi/-dynamic-translator/index.html):

## Fine-Tuning Translations
You can override specific translations in one of the following two ways:

1. **Language-Specific `strings.xml` File**  
   Add a partial `strings.xml` file for specific languages with only the strings you like to overwrite.

   Example for Spanish (`values-es/strings.xml`):
   ```xml
   <resources>
       <string name="title_time">Hora</string>
   </resources>
   ```
   The string with the Id `R.string.title_time` will always be translated as `Hora`, regardless of the automatic translation.

2. **Providing Overwrites in Code**  
   Call the `setOverwrites()` method during initialization:

```kotlin
      DynamicResourceApi.init()
          .setOverwrites(                 // optional 
             arrayOf(
                ResourceLocaleKey(R.string.hello, Locale("es")) to {"Hola"},
                ResourceLocaleKey(R.string.hello, Locale("bg")) to {"Здравей %1\$s"}
             )
        )
```

In addition, the API provides two functions to add overwrites from anywhere in your code:

```kotlin
    api.addOverwrites(arrayOf(
        ResourceLocaleKey(R.string.hello, Locale("es")) to {"Hola"},
        ResourceLocaleKey(R.string.hello, Locale("bg")) to {"Здравей %1\$s"}
    ))

    api.addOverwrite(ResourceLocaleKey(R.string.hello, Locale("es")) to {"Hola"})
```

**Translation Overwrites** map a pair of `(ID, Locale)` to a `lambda` that returns a `String`. 
The lambda can perform arbitrary transformations on the string, providing flexibility for dynamic content generation.

For example, the following overwrite customizes the English string with the ID `R.string.hello` to give a time-of-day-specific greeting:

```kotlin
ResourceLocaleKey(R.string.hello, Locale("en")) to {
    val currentHour = java.time.LocalTime.now().hour
    when (currentHour) {
        in 5..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        in 18..21 -> "Good Evening"
        else -> "Good Night"
    } + ", %1\$s"
}
```

Keep in mind that **overwrites** take precedence over both translations and strings defined in `strings.xml`, ensuring they are applied even if a translation exists for the target locale.

### When is Run-Time Translation Initiated?

Run-time translation is triggered when the following conditions are met:

1. **No Overwrite Exists:**  
   If an `overWrite` value exists for this string and language, it will be used directly without further translation.

2. **Not Cached in Local Storage:**  
   If the string is already cached in local storage, the cached value will be used, bypassing translation.

3. **Missing in Resource Files:**  
   If the string ID cannot be found in the device's resource files (e.g., `strings.xml` for the current language), translation will proceed.

4. **Network Connection Available:**  
   A network connection is required to access translation services.

When all these conditions are met, the library translates the string and stores the result in the cache for future use.

This applies to any translation engine, even if the translation logic is trivial. Additionally, 
for multiple chained engines, either all will execute if the conditions are satisfied, or none will execute.

## Adding a Custom Translation Engine
By default, the library uses the built-in `BushTranslationEngine`, based on [this](https://github.com/therealbush/translator) library.
You can provide your own translation engine for customized translations.
For the purposes of illustration, here’s a trivial engine that converts all strings to uppercase.

```kotlin
class UppercaseTranslationEngine : ITranslationEngine {
   override fun translate(text: String, target: Locale): String = text.uppercase()
   override suspend fun translateAsync(text: String, target: Locale): String = text.uppercase()
}
```
To use your custom engine, register it during initialization:

```kotlin
DynamicResourceApi.init().setEngine(UppercaseTranslationEngine())
```
After that, all translations will use the `UppercaseTranslationEngine`.

## Cascading Engines
Suppose you want to convert your translated text to uppercase after translating. You can achieve this by cascading (or chaining) two or more engines like this:

```kotlin
// Using the Singleton object:
DynamicResourceApi.init().setEngines(
    listOf(
        BushTranslationEngine(), 
        UppercaseTranslationEngine()))

// Or directly in the dynamic translator:
DynamicTranslator().init()
    .setEngines(
        listOf(
            BushTranslationEngine(), 
            UppercaseTranslationEngine()))

// Or to add an engine, you would call:
DynamicTranslator().init()
   .addEngine(
         UppercaseTranslationEngine())

// Or: 
DynamicTranslator().init()
   .addEngines(
      listOf(
         UppercaseTranslationEngine(),
         // more engines here...
       ))
```
The output of `BushTranslationEngine` will be fed to `UppercaseTranslationEngine` for further transformation.

*Note that values in the `Overwrites` list are not translated and are passed as they are.*

Adding translation engines, you can perform all sorts of transformations on the text. For example, if you want to remove 
offensive words or expressions for certain languages, you can write an engine to do that.

## Performance
When loading the app for the first time, if no `strings.xml` file is found for the phone's default language, 
there may be a delay per screen as the content is being translated. Subsequent access to the same screen, even after 
restarting the app, will load at normal speed.

We are exploring ways to further improve the initial load performance:

1. Use asynchronous functions like `stringResourceAsync()` to perform translations in the background and update the screen once the translation is complete. 
However, this approach requires more code changes in the app and is not recommended at this time.

2. Perform a bulk translation as the app loads, and store the translated strings in local storage. Screens can then read from local storage for better performance. Currently
the translation library does not support bulk translations, but we are looking to add this feature.

## Who is using it
The [Casio GShock Smart Sync](https://github.com/izivkov/CasioGShockSmartSync) app is an open-source alternative to the Casio app. It uses this
library to provide localisation to GShock users around the World.

If you like us to list your project which uses this library, contact us and we will include a link.

## Credits
- This project is using the great [translator](https://github.com/therealbush/translator) Kotlin library.
- Google Translate

## Note
This is using an **unofficial** Google API. This may cease to work at any point in time, and you should be prepared to use a different translation engine if needed.

## License
This project is licensed under the [LICENSE](LICENSE).
