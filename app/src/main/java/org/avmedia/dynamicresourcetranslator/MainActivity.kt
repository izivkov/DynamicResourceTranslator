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
        DynamicTranslator()
            .init()
            .setEngine()
            .setLanguage(Locale.getDefault())
            .setOverwrites(
            arrayOf(
                ResourceLocaleKey(R.string.hello, Locale("es")) to "[Hola]",
                ResourceLocaleKey(R.string.hello, Locale("bg")) to "Zdavey"
            )
        )

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
                            modifier = Modifier.padding(innerPadding)
                        )

                        Text(
                            text = translatedStringResourceAsync(
                                context = LocalContext.current,
                                resId = R.string.async_string,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 40.dp, end = 40.dp)
                        )

                        Text(
                            text = translatedStringResourceAsync(
                                context = LocalContext.current,
                                resId = R.string.async_string,
                                //locale = Locale("es")
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 40.dp, end = 40.dp)
                        )

                        Text(
                            text = translatedStringResourceAsync(
                                context = LocalContext.current,
                                resId = R.string.async_string,
                                //locale = Locale("jp")
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 40.dp, end = 40.dp)
                        )

                        Text(
                            text = translatedStringResourceAsync(
                                context = LocalContext.current,
                                resId = R.string.async_string,
                                //locale = Locale("ko")
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
    fun Greeting(name: String, modifier: Modifier = Modifier) {
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

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun translatedStringResourceAsync(
        @StringRes resId: Int,
        vararg formatArgs: Any,
        context: Context,
        locale: Locale? = null,
    ): String {
        val originalText = api.getString(
            context = context,
            resId = resId,
            formatArgs = formatArgs,
            locale = locale
        )
        val translatedTextState = remember { mutableStateOf(originalText) }

        LaunchedEffect(originalText) {
            try {
                withContext(Dispatchers.IO) {
                    translatedTextState.value =
                        api.getStringAsync(context = context, resId = resId, locale = locale)
                }
            } catch (e: Exception) {
                translatedTextState.value = "Translation failed: ${e.message}" // Fallback on error
            }
        }

        return translatedTextState.value
    }
}
