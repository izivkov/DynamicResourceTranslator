# DynamicResourceApi

DynamicResourceApi is an Android library designed to simplify internationalization and localization by dynamically translating string resources at runtime. The API eliminates the need for multiple language-specific resource files while respecting existing language-specific resources if they are present. With this API, you only need the default `strings.xml` file under the `res/values/` directory.

## Features

- Translates default string resources dynamically at runtime.
- Eliminates the need to create a language-specific strings.xml files, but will respect the ones that are present.
- Can fine-tune translations if automatic translation for some string not acceptable.
- Allows for multiple language translations on the same page.
- Pluggable translation engine architecture. Provide your own translation engine, or even transform your string in any way you see fit.
- Lightweight and easy to integrate.

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
You can provide your own translation engine to customize the way strings are translated. As an example, here is a simple `UppercaseTranslationEngine` which simply 
translates all strings to uppercase.

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

## License
This project is licensed under the MIT License. See the LICENSE file for more details.

