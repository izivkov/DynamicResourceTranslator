Here is the polished version of your README file:

---

# DynamicResourceApi

DynamicResourceApi is an Android library that simplifies internationalization for your app. You only need to create 
a single `strings.xml` file in your native language (not necessarily English), and the library will 
automatically translate your app into the system language set on the user's phone.

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

### Using DynamicResourceApi
```
res/
├── values/
│   └── strings.xml
```
> **Note:** Language-specific directories (e.g., `values-es/`, `values-fr/`) are optional. If they exist, the API will respect them.

## Features

- Dynamically translates default string resources at runtime.
- Eliminates the need for multiple language-specific `strings.xml` files, while respecting existing ones.
- Works with all languages.
- Preserves and used existing language specific resources.
- Supports fine-tuned translations when automatic translations are insufficient.
- Allows multiple language translations on the same page.
- Provides a pluggable translation engine architecture.
- Lightweight and easy to integrate.


## How It Works

The library intercepts calls to `getString()` and `stringResource()`, which read from `strings.xml` resource files. 
It then uses a Google translation service to translate the strings based on the language set in the phone's settings. 
Translated values are stored in local storage for reuse and better performance.

## Prerequisites
Your app must have Internet access at least the first time it runs to perform the initial translations.
Ensure you add the following permissions to your manifest:

```xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Usage

You get access to the API like this:

Initialize the `DynamicResourceApi` once, usually in `MainActivity` or your application class:
```kotlin
DynamicResourceApi.init()
```
Then access the API anywhere in your program:
```kotlin
DynamicResourceApi.getApi()
```

In order to translate your string, you must replace`context.getString` calls with the API's `api.getString` method.

```kotlin
val text = context.getString(R.string.hello_world) 
```
becomes:
```kotlin
val text = api.getString(context, R.string.hello_world)
```

For Jetpack Compose calls:

```kotlin
val text = stringResource(id = R.string.hello_world)
```
become:
```kotlin
val text = api.stringResource(context = LocalContext.current, resId = R.string.hello_world)
```

## Adding a Custom Translation Engine
By default, the library uses the built-in `BushTranslationEngine`, based on [this library](https://github.com/therealbush/translator). You can provide your own translation engine for customized translations.

### Example: UppercaseTranslationEngine
Here is an example of a custom engine that converts all strings to uppercase:

```kotlin
class UppercaseTranslationEngine : ITranslationEngine {
    override fun isInline(): Boolean = true
    override fun translate(text: String, target: Locale): String = text.uppercase()
    override suspend fun translateAsync(text: String, target: Locale): String = text.uppercase()
}
```

### Using a Custom Engine
Register your custom engine during initialization:
```kotlin
DynamicResourceApi.init(engine = UppercaseTranslationEngine())
```
After registration, all translations will use the `UppercaseTranslationEngine`.

## Respecting Existing Language-Specific Resources
DynamicResourceApi prioritizes existing language-specific resources. For example:
- If a `values-es/strings.xml` file is available for Spanish, it will use translations from this file.
- If no such file exists, the default `strings.xml` is dynamically translated at runtime.

## Fine-Tuning Translations
Override translations in one of the following ways:

1. **Language-Specific `strings.xml` File**  
   Add a partial `strings.xml` file for specific languages with only the strings you want to override.

   Example for Spanish (`values-es/strings.xml`):
   ```xml
   <resources>
       <string name="title_time">Hora</string>
   </resources>
   ```
   The string with the ID `R.string.title_time` will always be translated as `Hora`, regardless of the automatic translation.

2. **Providing Overrides in Code**  
   Use the `overWrites` parameter during initialization:
   ```kotlin
   DynamicResourceApi.init(
       overWrites = arrayOf(
           ResourceLocaleKey(R.string.hello, Locale("es").language) to "Hola",
           ResourceLocaleKey(R.string.hello, Locale("bg").language) to "Здравей %1$s"
       )
   )
   ```

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

---

This version improves grammar, consistency, and readability while retaining all original details. Let me know if you need additional adjustments!