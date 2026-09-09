package com.artswipe.domain.util

import java.net.URI
import java.net.URLDecoder
import java.util.Locale

object ShareCode {
    fun parse(input: String): String? {
        val value = input.trim()
        val code = if (value.startsWith("artswipe://", ignoreCase = true)) {
            runCatching {
                val uri = URI(value)
                if (!uri.host.equals("compare", ignoreCase = true)) return null
                uri.rawQuery.orEmpty().split("&").firstOrNull { it.startsWith("code=") }
                    ?.substringAfter("=")?.let { URLDecoder.decode(it, "UTF-8") }
            }.getOrNull() ?: return null
        } else value
        return code.trim().uppercase(Locale.ROOT).takeIf { it.matches(Regex("ART-[A-Z0-9]{4,12}")) }
    }
}
