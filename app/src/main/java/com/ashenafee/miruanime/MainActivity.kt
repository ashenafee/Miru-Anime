package com.ashenafee.miruanime

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ashenafee.miruanime.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    //private val anime = Anime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val navView: BottomNavigationView = binding.navView

        //val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_search, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        val searchButton = findViewById<Button>(R.id.searchButton)
//        searchButton.setOnClickListener {
//            getWebsite()
//        }
    }

//    private fun getWebsite() {
//        // Grab text of search query
//        val searchInput = findViewById<TextInputEditText>(R.id.searchTerm).text.toString()
//
//        if (searchInput != "") {
//            // Create MAL search query link
//            val malQuery = "https://myanimelist.net/anime.php?q=$searchInput&cat=anime"
//
//            Thread {
//                val builder = StringBuilder()
//                try {
//                    val doc: Document = Jsoup.connect(malQuery).get()
//                    searchAnime(doc)
//                } catch (e: IOException) {
//                    builder.append("Error : ").append(e.message).append("\n")
//                }
//            }.start()
//        }
//    }
//
//    private fun searchAnime(doc: Document) {
//
//        // Get the raw HTML of the first result after search
//        val firstResult = doc.getElementsByTag("table")[2].getElementsByTag("tr")[1]
//
//        // Populate the <anime> object with basic information
//        anime.name = firstResult.getElementsByTag("strong")[0].text() // Anime name
//        anime.type = firstResult.getElementsByTag("td")[2].text() // Anime type
//        if (firstResult.getElementsByTag("td")[3].text() == "-") {
//            anime.eps = null
//        } else {
//            anime.eps = firstResult.getElementsByTag("td")[3].text().toInt() // Anime episode count
//        }
//        anime.score = firstResult.getElementsByTag("td")[4].text().toDouble() // Anime score
//        anime.link = firstResult.getElementsByTag("a")[0].attr("href") // MAL link
//
//        // Get anime cover
//        getAnimeMALSite(anime.link)
//
//        // Update UI elements
//        addInfoToUi()
//    }
//
//    private fun getAnimeMALSite(url: String?) {
//        Thread {
//            val builder = StringBuilder()
//            try {
//                val doc: Document = Jsoup.connect(url).get()
//                getMoreAnimeInfo(doc)
//            } catch (e: IOException) {
//                builder.append("Error : ").append(e.message).append("\n")
//            }
//        }.start()
//    }
//
//    private fun getMoreAnimeInfo(doc: Document) {
//        val animeCover = doc.getElementsByClass("borderClass")[0].getElementsByTag("img").attr("data-src")
//        val animeDescription = doc.getElementsByAttributeValue("itemprop", "description")[0].text()
//
//        try {
//            val animeSeason = doc.getElementsByClass("information season")[0].getElementsByTag("a").text()
//            anime.season = animeSeason
//        } catch (e: IndexOutOfBoundsException) {
//            println("Exception Caught!")
//            val animeSeason = "N/A"
//            anime.season = animeSeason
//        }
//        anime.cover = animeCover
//        anime.desc = animeDescription
//        println(anime.cover)
//        println(anime.desc)
//    }
//
//    private fun addInfoToUi() {
//        // Change cover image and anime info
//        val coverImage = findViewById<ImageView>(R.id.coverImage)
//        val animeInfo = findViewById<TextView>(R.id.animeInfo)
//        val animeDesc = findViewById<TextView>(R.id.animeDesc)
//
//        runOnUiThread { Thread.sleep(1500)
//                        Glide.with(this).load(anime.cover).into(coverImage)
//                        animeDesc.setText(anime.desc)
//                        if (anime.eps == null) {
//                            animeInfo.setText("${anime.name}\n${anime.type}\n${anime.season}\nStill Airing!\n${anime.score}")
//                        } else {
//                            animeInfo.setText("${anime.name}\n${anime.type}\n${anime.season}\n${anime.eps}/${anime.eps}\n${anime.score}")
//                        } }
//
//    }

}