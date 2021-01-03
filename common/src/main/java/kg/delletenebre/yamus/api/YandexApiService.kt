package kg.delletenebre.yamus.api

import com.serjltt.moshi.adapters.FirstElement
import com.serjltt.moshi.adapters.Wrapped
import kg.delletenebre.yamus.api.responses.*
import okhttp3.RequestBody
import retrofit2.http.*


interface YandexApiService {
    @GET("/account/status")
    @Wrapped(path = ["result"])
    suspend fun accountStatus(): AccountStatus

    @GET("/settings")
    @Wrapped(path = ["result"])
    suspend fun settings(): Settings

    @GET("/landing3?blocks=mixes")
    @Wrapped(path = ["result", "blocks"]) @FirstElement
    suspend fun blockMixes(): BlockMixes

    @GET("/landing3?blocks=personalplaylists")
    @Wrapped(path = ["result", "blocks"]) @FirstElement
    suspend fun blockPersonalPlaylists(): BlockPersonalPlaylists

    @FormUrlEncoded
    @POST("/users/{uid}/playlists")
    @Wrapped(path = ["result"]) @FirstElement
    suspend fun playlistInfo(
            @Path("uid") uid: String,
            @Field("kinds") kinds: String
    ): PlaylistInfo

    @GET("/users/{uid}/playlists/list")
    @Wrapped(path = ["result"])
    suspend fun myPlaylists(@Path("uid") uid: Long = YandexUser.uid): List<Playlist>

    @GET("/users/{uid}/likes/playlists")
    @Wrapped(path = ["result"])
    suspend fun likedPlaylists(@Path("uid") uid: Long = YandexUser.uid): List<WrappedPlaylist>

    @GET("/rotor/stations/dashboard")
    @Wrapped(path = ["result", "stations"])
    suspend fun personalStations(): List<Station>

    @GET("/rotor/stations/list")
    @Wrapped(path = ["result"])
    suspend fun stations(@Query("language") language: String = "ru"): List<Station>

    @GET("/tags/{tag}/playlist-ids")
    @Wrapped(path = ["result", "ids"])
    suspend fun playlistIdsByTag(@Path("tag") tag: String): List<PlaylistId>

    @GET("/feed/promotions/{id}")
    @Wrapped(path = ["result"])
    suspend fun promotions(@Path("id") id: String): Promotion

    @FormUrlEncoded
    @POST("/playlists/list")
    @Wrapped(path = ["result"])
    suspend fun playlists(@Field("playlistIds") playlistIds: String): List<Playlist>

    @GET("/albums/{id}/with-tracks")
    @Wrapped(path = ["result", "volumes"]) @FirstElement
    suspend fun albumTracks(@Path("id") id: String): List<Track>

    @POST("/tracks")
    @FormUrlEncoded
    @Wrapped(path = ["result"])
    suspend fun tracks(@Field("track-ids") trackIds: String): List<Track>

    @GET("/rotor{stationId}/tracks")
    @Wrapped(path = ["result"])
    suspend fun stationQueue(
            @Path("stationId") stationId: String,
            @Query("settings2") settings2: String = "true",
            @Query("queue") queue: String = ""
    ): StationQueue

    @GET("/users/{uid}/{type}s/tracks")
    @Wrapped(path = ["result"])
    suspend fun userTracks(
            @Path("uid") uid: String,
            @Path("type") type: String,
            @Query("if-modified-since-revision") revision: Long = 0
    ): StationQueue

    @GET("/tracks/{trackId}/download-info")
    @Wrapped(path = ["result"])
    suspend fun trackDownloadInfo(@Path("trackId") trackId: String): List<TrackDownloadInfo>

    @GET
    suspend fun trackDownload(@Url url: String): TrackDownloadVariant

    @FormUrlEncoded
    @POST("/play-audio")
    suspend fun playAudio(
        @Field("track-id") trackId: String = "",
        @Field("from-cache") fromCache: Boolean = false,
        @Field("from") from: String = "",
        @Field("play-id") playId: String = "",
        @Field("uid") uid: Long = 0L,
        @Field("timestamp") timestamp: String = "",
        @Field("track-length-seconds") trackLengthSeconds: String = "",
        @Field("total-played-seconds") totalPlayedSeconds: String = "",
        @Field("end-position-seconds") endPositionSeconds: String = "",
        @Field("album-id") albumId: String = "",
        @Field("client-now") clientNow: String = ""
    )

    @POST("/rotor{stationId}/feedback")
    @Headers("Content-Type: application/json")
    suspend fun stationFeedback(
            @Path("stationId") stationId: String,
            @Body body: RequestBody,
            @Query("batch-id") batchId: String = ""
    )
}
