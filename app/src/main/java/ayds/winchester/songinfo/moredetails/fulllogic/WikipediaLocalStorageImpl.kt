package ayds.winchester.songinfo.moredetails.fulllogic

import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import java.sql.SQLException

private const val ARTISTS_TABLE = "artists"
private const val ID_COLUMN = "id"
private const val ARTIST_COLUMN = "artist"
private const val INFO_COLUMN = "info"
private const val SOURCE_COLUMN = "source"
private const val ARTIST_PAGE_ID_COLUMN = "pageid"

private const val createArtistTableQuery: String =
    "create table $ARTISTS_TABLE (" +
            "$ID_COLUMN INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$ARTIST_COLUMN string, " +
            "$INFO_COLUMN string, " +
            "$SOURCE_COLUMN integer, " +
            "$ARTIST_PAGE_ID_COLUMN string )"

private val projection = arrayOf(
    ID_COLUMN,
    ARTIST_COLUMN,
    INFO_COLUMN,
    SOURCE_COLUMN,
    ARTIST_PAGE_ID_COLUMN
)

interface WikipediaLocalStorage{

    fun insertArtist(artistName: String?, artistInfo: WikipediaArtistInfo)

    fun getArtistInfoByName(artistName: String?): WikipediaArtistInfo?

}

internal class WikipediaLocalStorageImpl(context: Context?) : WikipediaLocalStorage, SQLiteOpenHelper(context, "dictionary.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createArtistTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    override fun insertArtist(artistName: String?, artistInfo: WikipediaArtistInfo) {
        val values = ContentValues().apply {
            put(ARTIST_COLUMN, artistName)
            put(INFO_COLUMN, artistInfo.info)
            put(SOURCE_COLUMN, 1)
            put(ARTIST_PAGE_ID_COLUMN, artistInfo.pageId)
        }
        writableDatabase?.insert(ARTISTS_TABLE, null, values)
    }

    override fun getArtistInfoByName(artistName: String?): WikipediaArtistInfo? {
        val cursor = readableDatabase.query(
            ARTISTS_TABLE,
            projection,
            "$ARTIST_COLUMN = ?",
            arrayOf(artistName),
            null,
            null,
            "$ARTIST_COLUMN DESC"
        )
        return map(cursor)
    }

    private fun map(cursor: Cursor): WikipediaArtistInfo? =
    try {
        with(cursor) {
            if (moveToNext()) {
                WikipediaArtistInfo(
                    info = getString(getColumnIndexOrThrow(INFO_COLUMN)),
                    pageId = getString(getColumnIndexOrThrow(ARTIST_PAGE_ID_COLUMN)),
                )
            } else {
                null
            }
        }
    } catch (e: SQLException) {
        e.printStackTrace()
        null
    }
}