package ayds.winchester.songinfo.moredetails.fulllogic

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.os.Bundle
import ayds.winchester.songinfo.R
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.google.gson.Gson
import com.google.gson.JsonObject
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ImageView
import ayds.winchester.songinfo.utils.UtilsInjector
import ayds.winchester.songinfo.utils.view.ImageLoader
import java.lang.StringBuilder

private const val WIKIPEDIA_URL = "https://en.wikipedia.org/w/"
private const val WIKIPEDIA_ARTICLE_URL = "https://en.wikipedia.org/?curid="
private const val WIKIPEDIA_ARTICLE_IMAGE_URL = "https://upload.wikimedia.org/wikipedia/commons/8/8c/Wikipedia-logo-v2-es.png"

private const val QUERY = "query"
private const val SEARCH = "search"
private const val PAGEID = "pageid"
private const val SNIPPET = "snippet"

interface ArtistInfo {
    val info: String
    val pageId: String
}

data class WikipediaArtistInfo(
    override val info: String,
    override val pageId: String
) : ArtistInfo


internal class OtherInfoWindow : AppCompatActivity() {

    private lateinit var artistInfoTextView: TextView
    private lateinit var openUrlButton: Button
    private lateinit var articleImageView: ImageView

    private lateinit var pageId: String
    private val imageLoader: ImageLoader = UtilsInjector.imageLoader

    private val wikipediaLocalStorage = DataBaseImpl(this)
    private val wikipediaAPIRetrofit = Retrofit.Builder()
        .baseUrl(WIKIPEDIA_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val wikipediaAPI = wikipediaAPIRetrofit.create(WikipediaAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_info)
        open(intent.getStringExtra("artistName"))
        initProperties()
        initListeners()
        updateArticleImage()
    }

    private fun initProperties() {
        artistInfoTextView = findViewById(R.id.artistInfoTextView)
        openUrlButton = findViewById(R.id.openUrlButton)
        articleImageView = findViewById(R.id.articleImageView)
    }

    private fun initListeners(){
        findViewById<View>(R.id.openUrlButton).setOnClickListener {
            openUrlAction()
        }
    }

    private fun getArtistInfoByName(artistName: String?) {
        Thread {
            var artistInfoBio = ""
            var artistInfo = wikipediaLocalStorage.getArtistInfoByName(artistName)

            if (artistInfo != null) {
                artistInfoBio = markArtistInfoBioAsLocal(artistInfo.info)
                pageId = artistInfo.pageId
            }

            else {
                artistInfo = getArtistInfoFromExternalData(artistName)
                    if (artistInfo != null) {
                        if (artistInfo.info == null) {
                            artistInfoBio = "No Results"
                        } else {
                            artistInfoBio = artistInfo.info
                            pageId = artistInfo.pageId
                            artistInfoBio = textToHtml(artistInfoBio, artistName)
                            wikipediaLocalStorage.insertArtist(artistName, artistInfoBio, pageId)
                        }
                    }
            }
            runOnUiThread {
                artistInfoTextView!!.text = Html.fromHtml(artistInfoBio)
            }
        }.start()
    }

    private fun markArtistInfoBioAsLocal(artistInfoBio : String) = "[*]${artistInfoBio}"

    private fun getArtistInfoFromExternalData(artistName: String?): WikipediaArtistInfo? =
        try {
            getServiceData(artistName)?.getFirstItem()?.let { item ->
                WikipediaArtistInfo(
                    item.getInfo(), item.getPageId()
                )
            }
        } catch (e: Exception) {
            null
        }

    private fun JsonObject.getInfo() : String = this[SNIPPET].asString.replace("\\n", "\n")

    private fun JsonObject.getPageId() : String = this[PAGEID].asString

    private fun getServiceData(artistName: String?): String? {
        return wikipediaAPI.getArtistInfo(artistName).execute().body()
    }

    private fun String?.getFirstItem(): JsonObject {
        val jobj = Gson().fromJson(this, JsonObject::class.java)
        val query = jobj[QUERY].asJsonObject
        val items = query[SEARCH].asJsonArray
        return items[0].asJsonObject
    }

    private fun open(artist: String?) {
        getArtistInfoByName(artist)
    }

    private fun updateArticleImage() {
        runOnUiThread {
            imageLoader.loadImageIntoView(WIKIPEDIA_ARTICLE_IMAGE_URL, articleImageView)
        }
    }

    private fun openUrlAction() {
        val urlString = "$WIKIPEDIA_ARTICLE_URL+$pageId"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(urlString)
        startActivity(intent)
    }

    companion object {
        const val ARTIST_NAME_EXTRA = "artistName"

        fun textToHtml(text: String, term: String?): String {
            val builder = StringBuilder()
            builder.append("<html><div width=400>")
            builder.append("<font face=\"arial\">")
            val textWithBold = text
                .replace("'", " ")
                .replace("\n", "<br>")
                .replace("(?i)" + term!!.toRegex(), "<b>" + term.uppercase() + "</b>")
            builder.append(textWithBold)
            builder.append("</font></div></html>")
            return builder.toString()
        }
    }
}