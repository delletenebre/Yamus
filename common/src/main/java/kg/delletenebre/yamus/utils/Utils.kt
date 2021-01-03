package kg.delletenebre.yamus.utils

import android.content.Context


object Utils {
    fun getStringIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "string", context.packageName)
    }

    fun getResourceId(context: Context, name: String, type: String): Int {
        return context.resources.getIdentifier(name, type, context.packageName)
    }

    fun getBooleanIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "bool", context.packageName)
    }

    fun getIdIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "id", context.packageName)
    }
}