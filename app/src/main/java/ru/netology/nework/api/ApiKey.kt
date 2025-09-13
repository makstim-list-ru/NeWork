package ru.netology.nework.api

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKey @Inject constructor(@ApplicationContext context: Context) {

    private val manifestKey = "ru.netology.nework.backend_api_key"
    private var _value: String

    init {
        _value = getMetaDataValue(context, manifestKey)
    }

    val value = _value

    fun getMetaDataValue(context: Context, key: String): String {
        val pm: PackageManager = context.packageManager

        val ai = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return ai.metaData.getString(key)
            ?: throw IllegalArgumentException("ERROR: getMetaDataValue KEY collection fault")
    }
}