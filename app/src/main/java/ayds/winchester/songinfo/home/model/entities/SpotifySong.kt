package ayds.winchester.songinfo.home.model.entities

import java.util.Calendar.DATE

interface Song {
    val id: String
    val songName: String
    val artistName: String
    val albumName: String
    val releaseDatePrecision: DatePrecision
    val releaseDate: String
    val spotifyUrl: String
    val imageUrl: String
    var isLocallyStored: Boolean
}

data class SpotifySong(
    override val id: String,
    override val songName: String,
    override val artistName: String,
    override val albumName: String,
    override val releaseDatePrecision: DatePrecision,
    override val releaseDate: String,
    override val spotifyUrl: String,
    override val imageUrl: String,
    override var isLocallyStored: Boolean = false
) : Song

object EmptySong : Song {
    override val id: String = ""
    override val songName: String = ""
    override val artistName: String = ""
    override val albumName: String = ""
    override val releaseDatePrecision: DatePrecision = DatePrecision.YEAR
    override val releaseDate: String = ""
    override val spotifyUrl: String = ""
    override val imageUrl: String = ""
    override var isLocallyStored: Boolean = false
}