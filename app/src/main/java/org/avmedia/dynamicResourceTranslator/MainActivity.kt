package org.avmedia.dynamicResourceTranslator

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.avmedia.dynamicResourceTranslator.ui.theme.DynamicResourceTranslatorTheme
import org.avmedia.translateapi.DynamicResourceApi
import org.avmedia.translateapi.ResourceLocaleKey
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : ComponentActivity() {

    private val api = DynamicResourceApi
        .init()
        .setOverwrites(
            arrayOf(
                ResourceLocaleKey(R.string.hello, Locale("es")) to { "[Hola] %1\$s" },
                ResourceLocaleKey(R.string.hello, Locale("bg")) to { "Здравей %1\$s" },

                ResourceLocaleKey(R.string.hello, Locale("en")) to {
                    val currentHour = java.time.LocalTime.now().hour
                    when (currentHour) {
                        in 5..11 -> "Good Morning"
                        in 12..17 -> "Good Afternoon"
                        in 18..21 -> "Good Evening"
                        else -> "Good Night"
                    } + ", %1\$s"
                })
        )

        // Set app's language if default strings.xml not in English
        // .setAppLocale(Locale("de"))

        // If you like to replace the built in BushTranslationEngine() with your own
        // .setEngine(YourOwnTranslationEngine())

        // Convert to uppercase after translating, or add your own translator.
        // .addEngine(UppercaseTranslationEngine())

        .getApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DynamicResourceTranslatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .fillMaxHeight()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    )
                    {
                        Greeting(
                            name = "World",
                        )

                        Text(
                            text = api.stringResource(
                                context = LocalContext.current,
                                id = R.string.instructions,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 40.dp, end = 40.dp)
                        )

                        Text(
                            text = api.stringResource(
                                context = LocalContext.current,
                                id = R.string.profound_statement,
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
            // overwrite translation here if you like.
            // api.addOverwrite(ResourceLocaleKey(R.string.hello, Locale("bg")) to "Здрасти %1\$s")

            Text(
                text = api.stringResource(
                    LocalContext.current,
                    id = R.string.hello,
                    name,
                )
            )
        }
    }
}
