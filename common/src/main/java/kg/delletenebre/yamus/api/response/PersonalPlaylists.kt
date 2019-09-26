package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class PersonalPlaylists(
    val data: Data,
    val id: String,
    val type: String
) {
    @Serializable
    data class Data(
        val data: Data,
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
            val prerolls: List<String> = listOf(),
            val revision: Int,
            val snapshot: Int,
            val tags: List<String> = listOf(),
            val title: String,
            val trackCount: Int,
            val uid: Int,
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
                    val uid: Int,
                    val verified: Boolean
                )
            }

            @Serializable
            data class Owner(
                val login: String,
                val name: String,
                val sex: String,
                val uid: Int,
                val verified: Boolean
            )
        }
    }
}