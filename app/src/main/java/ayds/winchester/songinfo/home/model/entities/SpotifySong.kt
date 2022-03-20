package ayds.winchester.songinfo.home.model.entities

interface Song {
    val id: String
    val songName: String
    val artistName: String
    val albumName: String
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
  override val releaseDate: String,
  override val spotifyUrl: String,
  override val imageUrl: String,
  override var isLocallyStored: Boolean = false
) : Song {

    val year: String = releaseDate.split("-").first()
}

object EmptySong : Song {
    override val id: String = ""
    override val songName: String = ""
    override val artistName: String = ""
    override val albumName: String = ""
    override val releaseDate: String = ""
    override val spotifyUrl: String = ""
    override val imageUrl: String = ""
    override var isLocallyStored: Boolean = false
}