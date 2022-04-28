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
private const val PAGE_ID = "pageid"
private const val SNIPPET = "snippet"
private const val PREFIX_LOCALLY_STORED = "[*]"
private const val NO_RESULTS = "No results"

interface ArtistInfo {
    var info: String
    val pageId: String
    var isLocallyStored: Boolean
}

data class WikipediaArtistInfo(
    override var info: String,
    override val pageId: String,
    override var isLocallyStored: Boolean = false
) : ArtistInfo

object EmptyArtistInfo : ArtistInfo {
    override var info: String = NO_RESULTS
    override val pageId: String = ""
    override var isLocallyStored: Boolean = false
}

internal class OtherInfoWindow : AppCompatActivity() {

    private lateinit var artistInfoTextView: TextView
    private lateinit var openUrlButton: Button
    private lateinit var articleImageView: ImageView

    private lateinit var pageId: String
    private var artistName: String = ""
    private val imageLoader: ImageLoader = UtilsInjector.imageLoader

    private val wikipediaLocalStorage = WikipediaLocalStorageImpl(this)
    private val wikipediaAPIRetrofit = Retrofit.Builder()
        .baseUrl(WIKIPEDIA_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val wikipediaAPI = wikipediaAPIRetrofit.create(WikipediaAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_info)
        initProperties()
        initListeners()
        updateArticleImage()
        updateArtistName()
        searchArtist(artistName)
    }

    private fun updateArtistName() {
        artistName = intent.getStringExtra(ARTIST_NAME_EXTRA).toString()
    }

    private fun initProperties() {
        artistInfoTextView = findViewById(R.id.artistInfoTextView)
        openUrlButton = findViewById(R.id.openUrlButton)
        articleImageView = findViewById(R.id.articleImageView)
    }

    private fun initListeners(){
        openUrlButton.setOnClickListener {
            openUrlAction()
        }
    }

    private fun searchArtistAction(artistName: String){
        val artistInfo = getArtistInfoByName(artistName)
        var artistInfoBio = artistInfo.info
        if (artistInfo.isLocallyStored) {
            artistInfoBio = markArtistInfoBioAsLocal(artistInfoBio)
        }
        updateArtistInfoTextView(artistInfoBio)
        pageId = artistInfo.pageId
    }

    private fun updateArtistInfoTextView(artistInfoBio: String){
        runOnUiThread {
            artistInfoTextView!!.text = Html.fromHtml(artistInfoBio)
        }
    }

    private fun getArtistInfoByName(artistName: String) : ArtistInfo{
        var artistInfo = wikipediaLocalStorage.getArtistInfoByName(artistName)

        when {
            artistInfo != null -> markArtistInfoAsLocal(artistInfo)
            else -> {
                try {
                    artistInfo = getArtistInfoFromExternalData(artistName)
                    artistInfo?.let {
                        it.info = textToHtml(it.info, artistName)
                        wikipediaLocalStorage.insertArtist(artistName, it)
                    }
                } catch (e: Exception) {
                    artistInfo = null
                }
            }
        }

        return artistInfo ?: EmptyArtistInfo
    }

    private fun markArtistInfoBioAsLocal(artistInfoBio : String) = "$PREFIX_LOCALLY_STORED${artistInfoBio}"

    private fun markArtistInfoAsLocal(artistInfo : ArtistInfo){
        artistInfo.isLocallyStored = true
    }

    private fun getArtistInfoFromExternalData(artistName: String?): WikipediaArtistInfo? =
        try {
            getServiceData(artistName)?.getFirstItem()?.let { item ->
                WikipediaArtistInfo(item.getInfo(), item.getPageId())
            }
        } catch (e: Exception) {
            null
        }

    private fun JsonObject.getInfo() : String = this[SNIPPET].asString.replace("\\n", "\n")

    private fun JsonObject.getPageId() : String = this[PAGE_ID].asString

    private fun getServiceData(artistName: String?): String? {
        return wikipediaAPI.getArtistInfo(artistName).execute().body()
    }

    private fun String?.getFirstItem(): JsonObject {
        val jobj = Gson().fromJson(this, JsonObject::class.java)
        val query = jobj[QUERY].asJsonObject
        val items = query[SEARCH].asJsonArray
        return items[0].asJsonObject
    }

    private fun searchArtist(artistName: String) {
        Thread {
            searchArtistAction(artistName)
        }.start()
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

    private fun textToHtml(text: String, term: String?): String {
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

    companion object {
        const val ARTIST_NAME_EXTRA = "artistName"
    }
}