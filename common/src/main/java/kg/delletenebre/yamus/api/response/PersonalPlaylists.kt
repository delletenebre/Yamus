package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class PersonalPlaylists(
    val data: Data = Data(),
    val id: String = "",
    val type: String = ""
) {
    @Serializable
    data class Data(
        val data: Data = Data(),
        val notify: Boolean = false,
        val ready: Boolean = false,
        val type: String = ""
    ) {
        @Serializable
        data class Data(
                val animatedCoverUri: String = "",
                val available: Boolean = false,
                val collective: Boolean = false,
                val cover: Cover = Cover(),
                val coverWithoutText: Cover = Cover(),
                val created: String = "",
                val description: String = "",
                val descriptionFormatted: String = "",
                val durationMs: Int = 0,
                val everPlayed: Boolean = false,
                val generatedPlaylistType: String = "",
                val idForFrom: String = "",
                val isBanner: Boolean = false,
                val isPremiere: Boolean = false,
                val kind: Int = 0,
                val madeFor: MadeFor = MadeFor(),
                val modified: String = "",
                val ogImage: String = "",
                val owner: UserInfo = UserInfo(),
                val prerolls: List<String> = listOf(),
                val revision: Int = 0,
                val snapshot: Int = 0,
                val tags: List<String> = listOf(),
                val title: String = "",
                val trackCount: Int = 0,
                val uid: Int = 0,
                val visibility: String = ""
        ) {
            @Serializable
            data class Cover(
                val custom: Boolean = false,
                val dir: String = "",
                val type: String = "",
                val uri: String = "",
                val version: String = ""
            )

            @Serializable
            data class MadeFor(
                val userInfo: UserInfo = UserInfo()
            )

            @Serializable
            data class UserInfo(
                val login: String = "",
                val name: String = "",
                val sex: String = "",
                val uid: Int = 0,
                val verified: Boolean = false
            )
        }
    }
}