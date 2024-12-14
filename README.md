# DynamicResourceTranslator

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
> **Note:** Language-specific directories (e.g., `values-es/`, `values-fr/`) are optional. If they exist, the API will respect them.

## Features

- Dynamically translates string resources at runtime, eliminating the need for multiple language-specific `strings.xml`.
- Respect existing language-specific `string.xml`.
- Supports fine-tuning of translation when automatic translations should be corrected ot shortened.
- Allows multiple language translations on the same page.
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
            ResourceLocaleKey(R.string.hello, Locale("es")) to "Hola",
            ResourceLocaleKey(R.string.hello, Locale("bg")) to "Здравей %1\$s"
        ))
        .setLanguage(Locale("es"))
        .setEngine(BushTranslationEngine())
```
Setting the language here will override your phone's setting for the target language.

Then retrieve the API anywhere in your program:
   ```kotlin
   val api = DynamicResourceApi.getApi()
```

#### Method 2: Create the API directly

```kotlin
    val api = DynamicTranslator()
        .init ()            
        .setLanguage(Locale("es"))      // optional
        .setOverwrites(                 // optional
           arrayOf(
              ResourceLocaleKey(R.string.hello, Locale("es")) to "Hola",
              ResourceLocaleKey(R.string.hello, Locale("bg")) to "Здравей %1\$s"
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
                ResourceLocaleKey(R.string.hello, Locale("es")) to "Hola",
                ResourceLocaleKey(R.string.hello, Locale("bg")) to "Здравей %1\$s"
             )
        )
```

In addition, the API provides two functions to add overwrites from anywhere in your code:

```kotlin
    api.addOverwrites(arrayOf(
        ResourceLocaleKey(R.string.hello, Locale("es")) to "Hola",
        ResourceLocaleKey(R.string.hello, Locale("bg")) to "Здравей %1\$s"
    ))

    api.addOverwrite(ResourceLocaleKey(R.string.hello, Locale("es")) to "Hola")
```

## Adding a Custom Translation Engine
By default, the library uses the built-in `BushTranslationEngine`, based on [this](https://github.com/therealbush/translator) library.
You can provide your own translation engine for customized translations.
For the purposes of illustration, here’s a trivial engine that converts all strings to uppercase.

```kotlin
class UppercaseTranslationEngine : ITranslationEngine {
   override fun isInline(): Boolean = true
   override fun translate(text: String, target: Locale): String = text.uppercase()
   override suspend fun translateAsync(text: String, target: Locale): String = text.uppercase()
}
```
To use your custom engine, register it during initialization:

```kotlin
DynamicResourceApi.init().setEngine(UppercaseTranslationEngine())
```
After that, all translations will use the `UppercaseTranslationEngine`.

## Performance
When loading the app for the first time, if no `strings.xml` file is found for the phone's default language, 
there may be a delay per screen as the content is being translated. Subsequent access to the same screen, even after 
restarting the app, will load at normal speed.

We are exploring ways to further improve the initial load performance:

1. Use asynchronous functions like `stringResourceAsync()` to perform translations in the background and update the screen once the translation is complete. 
However, this approach requires more code changes in the app and is not recommended at this time.

2. Perform a bulk translation as the app loads, and store the translated strings in local storage. Screens can then read from local storage for better performance. Currently
the translation library does not support bulk translations, but we are looking to add this feature.

## Credits
- This project is using the great [translator](https://github.com/therealbush/translator) Kotlin library.
- Google Translate

## Note
This is using an **unofficial** Google API. This may cease to work at any point in time, and you should be prepared to use a different translation engine if needed.

## License
This project is licensed under the [LICENSE](LICENSE).
