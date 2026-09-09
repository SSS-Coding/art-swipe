package com.artswipe.domain.util

import org.junit.Assert.*
import org.junit.Test

class ShareCodeTest {
    @Test fun acceptsLegacyAndNewCodes() {
        assertEquals("ART-4X9K", ShareCode.parse("  art-4x9k  "))
        assertEquals("ART-0123456789AB", ShareCode.parse("ART-0123456789AB"))
    }
    @Test fun acceptsSharedLinks() {
        assertEquals("ART-4X9K", ShareCode.parse("artswipe://compare?code=art-4x9k"))
        assertEquals("ART-4X9K", ShareCode.parse("artswipe://compare?source=share&code=ART%2D4X9K"))
    }
    @Test fun rejectsInvalidCodesAndUnrelatedLinks() {
        listOf("", "ART-", "ART-123", "ART-1234!", "https://example.com?code=ART-4X9K",
            "artswipe://other?code=ART-4X9K", "artswipe://compare", "artswipe://compare?code=%zz").forEach {
            assertNull(it, ShareCode.parse(it))
        }
    }
}
