package org.avmedia.dynamicresourcetranslator

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.avmedia.dynamicresourcetranslator.ui.theme.DynamicResourceTranslatorTheme
import org.avmedia.translateapi.DynamicTranslator
import org.avmedia.translateapi.ResourceLocaleKey
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : ComponentActivity() {
    private val api =
        DynamicTranslator(
            // can pass your own translation engine here like that:
            // UppercaseTranslationEngine()
        )
            .init () // call optional
            .setLanguage(Locale.getDefault()) // optional call to set any language
            .setOverwrites(
            arrayOf(
                ResourceLocaleKey(R.string.hello,
                        Locale("es").language) to "[Hola]",
                ResourceLocaleKey(R.string.async_string, Locale("bg").language) to "Zdravei %1\$s  %2\$s  %3\$s"
            )
        )

    private fun Context.getString (
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale? = null
    ): String {
        return api.getString(this, resId, *formatArgs, locale = locale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            DynamicResourceTranslatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        Greeting(
                            name = "Android",
                        )

                        Text(
                            text = api.stringResource(
                                context = LocalContext.current,
                                resId = R.string.async_string,
                                "Xone", "Xtwo", "Xthree", "xfour"
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 40.dp, end = 40.dp)
                        )

                        Text(
                            text = api.stringResource(
                                context = LocalContext.current,
                                resId = R.string.async_string,
                                formatArgs = arrayOf("one", "two", "three"),
                                locale = Locale("ja")
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 40.dp, end = 40.dp)
                        )

                        Text(
                            text = api.stringResource(
                                context = LocalContext.current,
                                resId = R.string.async_string,
                                formatArgs = arrayOf("one", "two", "three"),
                                locale = Locale("bg")
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 40.dp, end = 40.dp)
                        )

                        Text(
                            text = api.stringResource (
                                context = LocalContext.current,
                                resId = R.string.async_string,
                                formatArgs = arrayOf("one", "two", "three"),
                                locale = Locale("sa")
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 40.dp, end = 40.dp)
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun Greeting(name: String) {
        Column(
            Modifier
        ) {
            Text(
                text = api.stringResource(
                    context = LocalContext.current,
                    resId = R.string.hello,
                    name,
                )
            )
        }
    }

    // Async calls...
    private suspend fun Context.getStringAsync (
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale? = null
    ): String {
        return api.stringResourceAsync(this, resId, *formatArgs, locale = locale)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun translatedStringResourceAsync(
        @StringRes resId: Int,
        vararg formatArgs: Any,
        context: Context,
        locale: Locale? = null,
    ): String {
        val originalText = this.getString(
            resId = resId,
            formatArgs = formatArgs,
            locale = locale
        )
        val translatedTextState = remember { mutableStateOf(originalText) }

        LaunchedEffect(originalText) {
            try {
                withContext(Dispatchers.IO) {
                    translatedTextState.value =
                        context.getStringAsync(resId = resId, locale = locale)
                }
            } catch (e: Exception) {
                translatedTextState.value = "Translation failed: ${e.message}" // Fallback on error
            }
        }

        return translatedTextState.value
    }
}
