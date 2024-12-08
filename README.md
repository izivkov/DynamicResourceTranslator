# DynamicResourceApi

DynamicResourceApi is an Android library designed to simplify internationalization and localization by dynamically translating string resources at runtime. The API eliminates the need for multiple language-specific resource files while respecting existing language-specific resources if they are present. With this API, you only need the default `strings.xml` file under the `res/values/` directory.

## Features

- Translates default string resources dynamically at runtime.
- Respects existing language-specific resources if available.
- Pluggable translation engine architecture.
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
Replace traditional `getString` calls with the new API's `getDynamicString` method. For example:

**Before:**
```kotlin
val text = context.getString(R.string.hello_world)
```

**After:**
```kotlin
val text = DynamicStringApi.getDynamicString(context, R.string.hello_world)
```

### Step 2: Replace `stringResource` Calls in Compose
Replace traditional `stringResource` calls with `dynamicStringResource`.

**Before:**
```kotlin
val text = stringResource(id = R.string.hello_world)
```

**After:**
```kotlin
val text = dynamicStringResource(id = R.string.hello_world)
```

## Adding a Custom Translation Engine

You can provide your own translation engine to customize the way strings are translated. For example, the `UppercaseTranslationEngine` translates all strings to uppercase.

### Example: UppercaseTranslationEngine
```kotlin
class UppercaseTranslationEngine : ITranslationEngine {
    override fun translate(text: String, targetLanguage: Locale): String {
        return text.uppercase(targetLanguage)
    }
}
```

### Plugging in a Custom Engine
To use your custom translation engine, register it using the API's configuration.

```kotlin
val engine = UppercaseTranslationEngine()
DynamicStringApi.setTranslationEngine(engine)
```

After registering, all string translations will use the `UppercaseTranslationEngine`.

## Respecting Existing Language-Specific Resources
DynamicResourceApi prioritizes existing language-specific resources if they exist. For example:

- If a `values-es/strings.xml` file is present for Spanish, the API will use the translation from this file for `getString` or `stringResource` calls.
- If no language-specific resource exists, the default `strings.xml` is translated dynamically at runtime.

## License
This project is licensed under the MIT License. See the LICENSE file for more details.

