package com.actito.utilities.collections

public inline fun <reified K, reified V> Map<*, *>.cast(): Map<K, V> =
    this.mapNotNull { entry ->
        val key = entry.key as? K
        val value = entry.value as? V

        if (key == null || value == null) null
        else key to value
    }.toMap()

public inline fun <K, V, R : Any> Map<out K, V>.filterNotNull(predicate: (Map.Entry<K, V>) -> R?): Map<K, R> {
    val result = LinkedHashMap<K, R>()
    for (entry in this) {
        val transformed = predicate(entry)
        if (transformed != null) {
            result[entry.key] = transformed
        }
    }
    return result
}

public fun <K, V, R : Any> Map<out K, V>.filterNotNullRecursive(predicate: (Map.Entry<K, V>) -> R?): Map<K, R> {
    val result = LinkedHashMap<K, R>()
    for (entry in this) {
        @Suppress("UNCHECKED_CAST")
        val transformed = when (val value = entry.value) {
            is Map<*, *> -> {
                val nested = (value as Map<K, V>).filterNotNullRecursive(predicate)
                if (nested.isNotEmpty()) nested as R else null
            }
            is List<*> -> {
                val nestedList = (value as List<*>).filterNotNullRecursive()
                if (nestedList.isNotEmpty()) nestedList as R else null
            }
            else -> predicate(entry)
        }
        if (transformed != null) {
            result[entry.key] = transformed
        }
    }
    return result
}
