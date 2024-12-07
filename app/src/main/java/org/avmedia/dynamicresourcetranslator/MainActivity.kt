package org.avmedia.dynamicresourcetranslator

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
                ResourceLocaleKey(R.string.hello, Locale("es")) to "[Hola]",
                ResourceLocaleKey(R.string.async_string, Locale("bg")) to "Zdravei %1\$s  %2\$s  %3\$s"
            )
        )

    private fun Context.getString (
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale? = null
    ): String {
        return api._getString(this, resId, formatArgs, locale = locale)
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
}
