package org.avmedia.dynamicresourcetranslator

import android.app.Application
import android.content.Context
import org.avmedia.translateapi.DynamicTranslator
import java.util.Locale

class TranslateApplication : Application() {

    private val api = DynamicTranslator()

    fun Context.stringResource(resId: Int, vararg formatArgs: Any): String {
        api.setLanguage(Locale("bg"))
        return api.getString(applicationContext, resId, *formatArgs)
    }

    override fun onCreate() {
        super.onCreate()
        println("TranslateApplication: onCreate")
    }
}