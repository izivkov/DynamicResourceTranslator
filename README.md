# DynamicResourceApi

DynamicResourceApi is an Android library designed to simplify internationalization and localization by dynamically translating string resources at runtime. 
The API eliminates the need for multiple language-specific resource files while respecting existing language-specific resources if they are present. 
With this API, you only need the default `strings.xml` file under the `res/values/` directory.

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
> Note: Language-specific directories (e.g., `values-es/`, `values-fr/`) are optional. If they exist, they will be respected by the API.

## Features

- Translates default string resources dynamically at runtime.
- Eliminates the need to create a language-specific strings.xml files, but will respect the ones that are present.
- Can fine-tune translations if automatic translation for some string not acceptable.
- Allows for multiple language translations on the same page.
- Pluggable translation engine architecture. Provide your own translation engine, or even transform your string in any way you see fit.
- Lightweight and easy to integrate.

## How it Works

The library intercepts calls to `getString()` and `stringResource()`, which read from `strings.xml` resource files. It than uses a Google translation service over the Internet
to translate the strings according to the language set in your phones settings. Once translated, the values are stored in local storage, to be re-used next time.

## How to Use the API

### Step 1: Replace `getString` Calls
Replace traditional `context.getString` calls with the new API's `api.getString` method. For example:

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

The ```api``` variable can be created as follows:

Initialise the ```DynamicResourceApi``` once:
```kotlin
DynamicResourceApi.init()
```

Then obtain the api anywhere in your program like this:

```kotlin
private val api = DynamicResourceApi.getApi()
```

## Adding a Custom Translation Engine
By default, this library uses ts built-in `BushTranslationEngine` for translation, which is based in [this](https://github.com/therealbush/translator) library. 
You can provide your own translation engine to customize the way strings are translated. For an illustration purpouse, here is a simple `UppercaseTranslationEngine` which simply 
transforms all passed strings to uppercase. Note that the Translation Engine must implement interface `ITranslationEngine`.

### Example: UppercaseTranslationEngine
```kotlin
class UppercaseTranslationEngine: ITranslationEngine {

    override fun isInline(): Boolean {
        return true
    }

    override fun translate(text: String, target: Locale): String {
        return text.uppercase()
    }

    override suspend fun translateAsync(text: String, target: Locale): String {
        return text.uppercase() 
    }
}
```

### Plugging in a Custom Engine
To use your custom translation engine, register it using the API's configuration.

We plug in into the api like this when we first initialise it:

```kotlin
DynamicResourceApi.init(engine = UppercaseTranslationEngine())
```
After registering, all string translations will use the `UppercaseTranslationEngine`.

## Respecting Existing Language-Specific Resources
DynamicResourceApi prioritizes existing language-specific resources if they exist. For example:

- If a `values-es/strings.xml` file is present for Spanish, the API will use the translation from this file for `getString` or `stringResource` calls.
- If no language-specific resource exists, the default `strings.xml` is translated dynamically at runtime.

## Fine tuning the translation
If you like to override the way a particular string is translated, you can do it an one of two ways:

1. Provide a partial `string.xml` file for that language, containing only yhr strings you like yo override. For example for Spanish, in file:

├── values-es/
│   └── strings.xml

if you have:

```xml
<resources>
    <string name="title_time">Hora</string>
</resources>
```
the string with the ID 
```kotlin
R.strins.title_time
```
will be always translated as `Hora`, no matter what the automatic translation returns.

2. When initializing the api, you can provide an `overWrites` parameter containg an array of strings and Local to overwrite the automatic translation: 
```kotlin
DynamicResourceApi
        .init(
            overWrites = arrayOf(
                ResourceLocaleKey(R.string.hello, Locale("es").language) to "[Hola]",
                ResourceLocaleKey(R.string.hello, Locale("bg").language) to "Здравей %1$s"
            )
        )
```

## License
This project is licensed under the MIT License. See the LICENSE file for more details.

