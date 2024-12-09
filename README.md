Here is the polished version of your README file:

---

# DynamicResourceApi

DynamicResourceApi is an Android library designed to simplify internationalization and localization by dynamically translating string resources at runtime. It eliminates the need for multiple language-specific resource files while respecting existing ones if present. With this API, you only need the default `strings.xml` file under the `res/values/` directory.

## Directory Structure Comparison

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
- Supports fine-tuned translations when automatic translations are insufficient.
- Allows multiple language translations on the same page.
- Provides a pluggable translation engine architecture—customize or transform strings as needed.
- Lightweight and easy to integrate.

## How It Works

The library intercepts calls to `getString()` and `stringResource()`, which read from `strings.xml` resource files. It uses a Google translation service to translate the strings based on the language set in the phone's settings. Translated values are stored locally for reuse.

## Usage

### Step 1: Replace `getString` Calls
Replace traditional `context.getString` calls with the API's `api.getString` method.

**Before:**
```kotlin
val text = context.getString(R.string.hello_world)
```

**After:**
```kotlin
val text = api.getString(context, R.string.hello_world)
```

### Step 2: Replace `stringResource` Calls in Compose
Replace traditional `stringResource` calls with `dynamicStringResource`.

**Before:**
```kotlin
val text = stringResource(id = R.string.hello_world)
```

**After:**
```kotlin
val text = api.stringResource(context = LocalContext.current, resId = R.string.hello_world)
```

#### Initializing the API
Initialize the `DynamicResourceApi` once:
```kotlin
DynamicResourceApi.init()
```

Access the API anywhere in your program:
```kotlin
private val api = DynamicResourceApi.getApi()
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