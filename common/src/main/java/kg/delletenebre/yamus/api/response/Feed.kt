package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Feed(
        val invocationInfo: InvocationInfo = InvocationInfo(),
        val result: Result = Result()
) {
    @Serializable
    data class Result(
        val canGetMoreEvents: Boolean = false,
        val days: List<Day> = listOf(),
        val generatedPlaylists: List<GeneratedPlaylists> = listOf(),
        @Transient
        val headlines: List<String> = listOf(),
        val isWizardPassed: Boolean = false,
        val nextRevision: String = "",
        val pumpkin: Boolean = false,
        val today: String = ""
    ) {
        @Serializable
        data class Day(
            val day: String,
            val events: List<Event>,
            val tracksToPlay: List<TracksToPlay>,
            val tracksToPlayWithAds: List<TracksToPlayWithAd>
        ) {
            @Serializable
            data class Event(
                val albums: List<Album>,
                val artist: Artist,
                val artistsWithArtistsFromHistory: List<ArtistsWithArtistsFromHistory>,
                val genre: String,
                val id: String,
                val likedTrack: LikedTrack,
                val message: String,
                val radioIsAvailable: Boolean,
                val similarArtists: List<SimilarArtist>,
                val similarGenre: String,
                val similarToArtist: SimilarToArtist,
                val similarToGenre: String,
                val title: List<Title>,
                val tracks: List<Track>,
                val type: String,
                val typeForFrom: String
            ) {
                @Serializable
                data class Album(
                    val artists: List<Artist>,
                    val available: Boolean,
                    val availableForMobile: Boolean,
                    val availableForPremiumUsers: Boolean,
                    val availablePartially: Boolean,
                    val bests: List<Int>,
                    @Transient
                    val buy: List<Buy> = listOf(),
                    val contentWarning: String,
                    val coverUri: String,
                    val genre: String,
                    val id: Int,
                    val labels: List<Label>,
                    val ogImage: String,
                    val recent: Boolean,
                    val releaseDate: String,
                    val title: String,
                    val trackCount: Int,
                    val veryImportant: Boolean,
                    val year: Int
                ) {
                    @Serializable
                    data class Artist(
                        val composer: Boolean,
                        val cover: Cover,
                        @Transient
                        val genres: List<String> = listOf(),
                        val id: Int,
                        val name: String,
                        val various: Boolean
                    ) {
                        @Serializable
                        data class Cover(
                            val prefix: String,
                            val type: String,
                            val uri: String
                        )
                    }

                    @Serializable
                    data class Label(
                        val id: Int,
                        val name: String
                    )
                }

                @Serializable
                data class Artist(
                    val available: Boolean,
                    val composer: Boolean,
                    val counts: Counts,
                    val cover: Cover,
                    val genres: List<String>,
                    val id: String,
                    val links: List<Link>,
                    val name: String,
                    val ogImage: String,
                    val ratings: Ratings,
                    val ticketsAvailable: Boolean,
                    val various: Boolean
                ) {
                    @Serializable
                    data class Counts(
                        val alsoAlbums: Int,
                        val alsoTracks: Int,
                        val directAlbums: Int,
                        val tracks: Int
                    )

                    @Serializable
                    data class Cover(
                        val prefix: String,
                        val type: String,
                        val uri: String
                    )

                    @Serializable
                    data class Link(
                        val href: String,
                        val socialNetwork: String,
                        val title: String,
                        val type: String
                    )

                    @Serializable
                    data class Ratings(
                        val day: Int,
                        val month: Int,
                        val week: Int
                    )
                }

                @Serializable
                data class ArtistsWithArtistsFromHistory(
                    val artist: Artist,
                    val artistsFromHistory: List<ArtistsFromHistory>
                ) {
                    @Serializable
                    data class Artist(
                        val available: Boolean,
                        val composer: Boolean,
                        val counts: Counts,
                        val cover: Cover,
                        val genres: List<String>,
                        val id: String,
                        val links: List<Link>,
                        val name: String,
                        val ogImage: String,
                        val ratings: Ratings,
                        val ticketsAvailable: Boolean,
                        val various: Boolean
                    ) {
                        @Serializable
                        data class Counts(
                            val alsoAlbums: Int,
                            val alsoTracks: Int,
                            val directAlbums: Int,
                            val tracks: Int
                        )

                        @Serializable
                        data class Cover(
                            val prefix: String,
                            val type: String,
                            val uri: String
                        )

                        @Serializable
                        data class Link(
                            val href: String,
                            val socialNetwork: String,
                            val title: String,
                            val type: String
                        )

                        @Serializable
                        data class Ratings(
                            val day: Int,
                            val month: Int,
                            val week: Int
                        )
                    }

                    @Serializable
                    data class ArtistsFromHistory(
                        val available: Boolean,
                        val composer: Boolean,
                        val counts: Counts,
                        val cover: Cover,
                        val genres: List<String>,
                        val id: String,
                        val links: List<Link>,
                        val name: String,
                        val ogImage: String,
                        val ratings: Ratings,
                        val ticketsAvailable: Boolean,
                        val various: Boolean
                    ) {
                        @Serializable
                        data class Counts(
                            val alsoAlbums: Int,
                            val alsoTracks: Int,
                            val directAlbums: Int,
                            val tracks: Int
                        )

                        @Serializable
                        data class Cover(
                            val prefix: String,
                            val type: String,
                            val uri: String
                        )

                        @Serializable
                        data class Link(
                            val href: String,
                            val socialNetwork: String,
                            val title: String,
                            val type: String
                        )

                        @Serializable
                        data class Ratings(
                            val day: Int,
                            val month: Int,
                            val week: Int
                        )
                    }
                }

                @Serializable
                data class LikedTrack(
                    val albums: List<Album>,
                    val artists: List<Artist>,
                    val available: Boolean,
                    val availableForPremiumUsers: Boolean,
                    val availableFullWithoutPermission: Boolean,
                    val contentWarning: String,
                    val coverUri: String,
                    val durationMs: Int,
                    val fileSize: Int,
                    val id: String,
                    val lyricsAvailable: Boolean,
                    val major: Major,
                    val normalization: Normalization,
                    val ogImage: String,
                    val previewDurationMs: Int,
                    val realId: String,
                    val storageDir: String,
                    val title: String,
                    val type: String
                ) {

                    @Serializable
                    data class Major(
                        val id: Int,
                        val name: String
                    )

                    @Serializable
                    data class Normalization(
                        val gain: Double,
                        val peak: Int
                    )
                }

                @Serializable
                data class SimilarArtist(
                    val artist: Artist,
                    val subscribed: Boolean
                ) {
                    @Serializable
                    data class Artist(
                        val available: Boolean,
                        val composer: Boolean,
                        val counts: Counts,
                        val cover: Cover,
                        val genres: List<String>,
                        val id: String,
                        val links: List<Link>,
                        val name: String,
                        val ogImage: String,
                        val ratings: Ratings,
                        val ticketsAvailable: Boolean,
                        val various: Boolean
                    ) {
                        @Serializable
                        data class Counts(
                            val alsoAlbums: Int,
                            val alsoTracks: Int,
                            val directAlbums: Int,
                            val tracks: Int
                        )

                        @Serializable
                        data class Cover(
                            val prefix: String,
                            val type: String,
                            val uri: String
                        )

                        @Serializable
                        data class Link(
                            val href: String,
                            val socialNetwork: String,
                            val title: String,
                            val type: String
                        )

                        @Serializable
                        data class Ratings(
                            val day: Int,
                            val month: Int,
                            val week: Int
                        )
                    }
                }

                @Serializable
                data class SimilarToArtist(
                    val available: Boolean,
                    val composer: Boolean,
                    val counts: Counts,
                    val cover: Cover,
                    val genres: List<String>,
                    val id: String,
                    @Transient
                    val links: List<String> = listOf(),
                    val name: String,
                    val ogImage: String,
                    val ratings: Ratings,
                    val ticketsAvailable: Boolean,
                    val various: Boolean
                ) {
                    @Serializable
                    data class Counts(
                        val alsoAlbums: Int,
                        val alsoTracks: Int,
                        val directAlbums: Int,
                        val tracks: Int
                    )

                    @Serializable
                    data class Cover(
                        val prefix: String,
                        val type: String,
                        val uri: String
                    )

                    @Serializable
                    data class Ratings(
                        val day: Int,
                        val month: Int,
                        val week: Int
                    )
                }

                @Serializable
                data class Title(
                    val name: String,
                    val text: String,
                    val type: String
                )
            }

            @Serializable
            data class TracksToPlay(
                val albums: List<Album>,
                val artists: List<Artist>,
                val available: Boolean,
                val availableForPremiumUsers: Boolean,
                val availableFullWithoutPermission: Boolean,
                val best: Boolean,
                val contentWarning: String,
                val coverUri: String,
                val durationMs: Int,
                val fileSize: Int,
                val id: String,
                val lyricsAvailable: Boolean,
                val major: Major,
                val normalization: Normalization,
                val ogImage: String,
                val previewDurationMs: Int,
                val realId: String,
                val storageDir: String,
                val title: String,
                val type: String,
                val version: String
            ) {
                @Serializable
                data class Major(
                    val id: Int,
                    val name: String
                )

                @Serializable
                data class Normalization(
                    val gain: Double,
                    val peak: Int
                )
            }

            @Serializable
            data class TracksToPlayWithAd(
                val track: Track,
                val type: String
            ) {
                @Serializable
                data class Track(
                    val albums: List<Album>,
                    val artists: List<Artist>,
                    val available: Boolean,
                    val availableForPremiumUsers: Boolean,
                    val availableFullWithoutPermission: Boolean,
                    val best: Boolean,
                    val coverUri: String,
                    val durationMs: Int,
                    val fileSize: Int,
                    val id: String,
                    val lyricsAvailable: Boolean,
                    val major: Major,
                    val normalization: Normalization,
                    val ogImage: String,
                    val previewDurationMs: Int,
                    val realId: String,
                    val storageDir: String,
                    val title: String,
                    val type: String
                ) {
                    @Serializable
                    data class Major(
                        val id: Int,
                        val name: String
                    )

                    @Serializable
                    data class Normalization(
                        val gain: Double,
                        val peak: Int
                    )
                }
            }
        }

        @Serializable
        data class GeneratedPlaylists(
            val `data`: Data,
            val notify: Boolean,
            val ready: Boolean,
            val type: String
        ) {
            @Serializable
            data class Data(
                val animatedCoverUri: String,
                val available: Boolean,
                val collective: Boolean,
                val cover: Cover,
                val coverWithoutText: CoverWithoutText,
                val created: String,
                val description: String,
                val descriptionFormatted: String,
                val durationMs: Int,
                val everPlayed: Boolean,
                val generatedPlaylistType: String,
                val idForFrom: String,
                val isBanner: Boolean,
                val isPremiere: Boolean,
                val kind: Int,
                val madeFor: MadeFor,
                val modified: String,
                val ogImage: String,
                val owner: Owner,
                @Transient
                val prerolls: List<String> = listOf(),
                val revision: Int,
                val snapshot: Int,
                val tags: List<String> = listOf(),
                val title: String,
                val trackCount: Int,
                val tracks: List<Track>,
                val uid: Long,
                val visibility: String
            ) {
                @Serializable
                data class Cover(
                    val custom: Boolean,
                    val dir: String,
                    val type: String,
                    val uri: String,
                    val version: String
                )

                @Serializable
                data class CoverWithoutText(
                    val custom: Boolean,
                    val dir: String,
                    val type: String,
                    val uri: String,
                    val version: String
                )

                @Serializable
                data class MadeFor(
                    val userInfo: UserInfo
                ) {
                    @Serializable
                    data class UserInfo(
                        val login: String,
                        val name: String,
                        val sex: String,
                        val uid: Long,
                        val verified: Boolean
                    )
                }

                @Serializable
                data class Owner(
                    val login: String,
                    val name: String,
                    val sex: String,
                    val uid: Long,
                    val verified: Boolean
                )

                @Serializable
                data class Track(
                    val id: Int,
                    val timestamp: String
                )
            }
        }
    }
}