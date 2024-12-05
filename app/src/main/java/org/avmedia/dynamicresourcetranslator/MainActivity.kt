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
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.avmedia.dynamicresourcetranslator.ui.theme.DynamicResourceTranslatorTheme
import org.avmedia.translateapi.DynamicTranslator
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : ComponentActivity() {
    private val api = DynamicTranslator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("MainActivity: onCreate")

        api.setLanguage(Locale("bg"))
        val description = api.getString(this, R.string.description)
        println("============> $description")

        enableEdgeToEdge()
        setContent {
            DynamicResourceTranslatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.fillMaxSize().fillMaxHeight(),
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
                                resId = R.string.async_string
                            ),
                            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).padding(start = 40.dp, end = 40.dp)
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
            Text(text = api.stringResource(context = LocalContext.current, resId = R.string.hello, name, locale = Locale.ITALIAN))
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun translatedStringResourceAsync(
        @StringRes resId: Int,
        vararg formatArgs: Any,
        context: Context,
    ): String {
        // Original and translated text state
        val originalText = api.getString(this, resId, *formatArgs)
        val translatedTextState = remember { mutableStateOf(originalText) }

        // Perform translation
        LaunchedEffect(originalText) {
            try {
                withContext(Dispatchers.IO) {
                    translatedTextState.value = api.getStringAsync(context, resId)
                }
            } catch (e: Exception) {
                translatedTextState.value = "Translation failed: ${e.message}" // Fallback on error
            }
        }

        return translatedTextState.value
    }
}
