package com.artswipe.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MetApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("hasImages") hasImages: Boolean = true,
        @Query("isHighlight") isHighlight: Boolean? = null,
        @Query("departmentId") departmentId: Int? = null
    ): MetSearchResponse

    @GET("objects/{objectId}")
    suspend fun getObject(
        @Path("objectId") objectId: Int
    ): MetObjectResponse

    @GET("departments")
    suspend fun getDepartments(): MetDepartmentsResponse
}

data class MetSearchResponse(
    val total: Int,
    val objectIDs: List<Int>?
)

data class MetObjectResponse(
    val objectID: Int,
    val title: String?,
    val artistDisplayName: String?,
    val objectDate: String?,
    val primaryImage: String?,
    val medium: String?,
    val repository: String?,
    val department: String?,
    val objectURL: String?,
    val isHighlight: Boolean?,
    val isPublicDomain: Boolean?
)

data class MetDepartmentsResponse(
    val departments: List<MetDepartment>
)

data class MetDepartment(
    val departmentId: Int,
    val displayName: String
)
