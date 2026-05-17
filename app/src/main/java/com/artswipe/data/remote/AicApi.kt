package com.artswipe.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AicApi {
    @GET("artworks")
    suspend fun getArtworks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_display,date_display,image_id,medium_display,style_title,department_title"
    ): AicResponse
}

data class AicResponse(
    val data: List<AicArtwork>,
    val config: AicConfig
)

data class AicArtwork(
    val id: Int,
    val title: String?,
    val artist_display: String?,
    val date_display: String?,
    val image_id: String?,
    val medium_display: String?,
    val style_title: String?,
    val department_title: String?
)

data class AicConfig(
    val iiif_url: String
)
