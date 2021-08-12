package com.ashenafee.miruanime.ui.search

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html.fromHtml
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.ashenafee.miruanime.Anime
import com.ashenafee.miruanime.R
import com.ashenafee.miruanime.databinding.FragmentSearchBinding
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.miruanime.AnimeQuery
import com.miruanime.type.MediaFormat
import java.lang.IndexOutOfBoundsException


class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel
    private var _binding: FragmentSearchBinding? = null
    private val animeList = mutableListOf<Anime>()
    private val prevAnimeList = mutableListOf<Anime>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getView()?.findViewById<Button>(R.id.searchButton)?.setOnClickListener {
            // Hide the detailed info and show the other covers
            activity?.runOnUiThread {
                // Show
                view.findViewById<LinearLayout>(R.id.anime2LL)?.visibility = VISIBLE
                view.findViewById<LinearLayout>(R.id.anime3LL)?.visibility = VISIBLE
                view.findViewById<LinearLayout>(R.id.anime4LL)?.visibility = VISIBLE
                view.findViewById<TextView>(R.id.title1)?.visibility = VISIBLE

                // Hide
                view.findViewById<TextView>(R.id.animeInfo)?.visibility = GONE
                view.findViewById<TextView>(R.id.animeDesc)?.visibility = GONE
            }
            anilistAnimeGraphQL()
        }

        getView()?.findViewById<ImageView>(R.id.coverImage1)?.setOnClickListener {
            if (prevAnimeList.size > 0){
                val currImage = getView()?.findViewById<ImageView>(R.id.coverImage1)?.drawable
                updateInfo(currImage, prevAnimeList[0])
            }
        }

        getView()?.findViewById<ImageView>(R.id.coverImage2)?.setOnClickListener {
            if (prevAnimeList.size > 0) {
                val currImage = getView()?.findViewById<ImageView>(R.id.coverImage2)?.drawable
                updateInfo(currImage, prevAnimeList[1])
            }
        }

        getView()?.findViewById<ImageView>(R.id.coverImage3)?.setOnClickListener {
            if (prevAnimeList.size > 0){
                val currImage = getView()?.findViewById<ImageView>(R.id.coverImage3)?.drawable
                updateInfo(currImage, prevAnimeList[2])
            }
        }

        getView()?.findViewById<ImageView>(R.id.coverImage4)?.setOnClickListener {
            if (prevAnimeList.size > 0){
                val currImage = getView()?.findViewById<ImageView>(R.id.coverImage4)?.drawable
                updateInfo(currImage, prevAnimeList[3])
            }
        }

    }

    private fun anilistAnimeGraphQL() {
        // Grab text of search query
        val searchInput = view?.findViewById<TextInputEditText>(R.id.searchTerm)?.text.toString()

        // Grab media type selected
        val mediaInput = view?.findViewById<Spinner>(R.id.mediaTypeChoice)?.selectedItem.toString()
        var mediaType: MediaFormat? = null

        when (mediaInput) {
            "TV" -> {
                mediaType = MediaFormat.TV
            }
            "Movie" -> {
                mediaType = MediaFormat.MOVIE
            }
            "OVA" -> {
                mediaType = MediaFormat.OVA
            }
            "ONA" -> {
                mediaType = MediaFormat.ONA
            }
        }

        // Check if user inputted a query
        if (searchInput != "") {

            // Grab data via AniList GraphQL API
            lifecycleScope.launchWhenResumed {
                val response = apolloClient.query(AnimeQuery(searchInput.toInput(), mediaType.toInput())).await()

                var i = 0
                while (i < 4) {

                    try {
                        // Create anime object
                        val anime = Anime()

                        // Populate <anime> with data
                        anime.name = response.data?.page?.media?.get(i)?.title?.romaji
                        anime.season = "${response.data?.page?.media?.get(i)?.season} ${response.data?.page?.media?.get(i)?.seasonYear}"
                        anime.eps = response.data?.page?.media?.get(i)?.episodes
                        anime.desc = fromHtml(response.data?.page?.media?.get(i)?.description, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                        anime.type = response.data?.page?.media?.get(i)?.format?.name
                        anime.score = response.data?.page?.media?.get(i)?.averageScore

                        anime.link = response.data?.page?.media?.get(i)?.siteUrl
                        anime.cover = response.data?.page?.media?.get(i)?.coverImage?.extraLarge

                        // If is int, anime is still airing. If not, anime is over.
                        anime.airing = response.data?.page?.media?.get(i)?.nextAiringEpisode?.episode is Int

                        // Print log of <anime>
                        Log.d("AniList GraphQL", ".\n${"=".repeat(100)}\n${anime.name} (${anime.type})\n${anime.season}\n${anime.eps}\n${anime.desc}\n${anime.link}\n${"=".repeat(100)}")

                        // Add <anime> to <animeList>
                        animeList.add(anime)
                    } catch (e: IndexOutOfBoundsException) {
                        println("No more results")
                    }

                    i += 1
                }

                // Update the UI
                updateUi()

                // Save to global variable lists
                if (prevAnimeList.size > 0) {
                    prevAnimeList.clear()
                }
                for (anime in animeList) prevAnimeList.add(anime)
                animeList.clear()
            }
        }


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
    }

    private fun updateUi() {

        val coverList = listOf<ImageView?>(view?.findViewById(R.id.coverImage1),
            view?.findViewById(R.id.coverImage2),
            view?.findViewById(R.id.coverImage3),
            view?.findViewById(R.id.coverImage4))

        val titleList = listOf<TextView?>(view?.findViewById(R.id.title1),
            view?.findViewById(R.id.title2),
            view?.findViewById(R.id.title3),
            view?.findViewById(R.id.title4))

        // Clear ImageView and TextView before loading new covers/titles in
        for (i in 0..3) {
            activity?.runOnUiThread {
                coverList[i]?.visibility = INVISIBLE
                titleList[i]?.visibility = INVISIBLE
            }
        }

        var i = 0
        while (i < animeList.size) {

            try {
                // Grab ImageView object to load cover in
                val coverImage = coverList[i]

                // Grab TextView object to load title in
                val title = titleList[i]

                // Update the cover object and title object on the UI
                activity?.runOnUiThread {
                    if (coverImage != null) {
                        coverImage.visibility = VISIBLE
                        Glide.with(this).load(animeList[i].cover).into(coverImage)
                    }
                    if (title != null) {
                        title.visibility = VISIBLE
                        title.text = animeList[i].name
                    }
                }
            } catch (e: IndexOutOfBoundsException) {
                println("No more results")
            }

            i += 1

        }

//        // Create objects for each UI element needing to be updated
//        val animeInfo = view?.findViewById<TextView>(R.id.animeInfo)
//        val animeDesc = view?.findViewById<TextView>(R.id.animeDesc)
//        val coverImage = view?.findViewById<ImageView>(R.id.coverImage)
//
//        // Update the UI elements
//        activity?.runOnUiThread {
//
//            if (coverImage != null) {
//                coverImage.visibility = VISIBLE
//                Glide.with(this).load(animeList[0].cover).into(coverImage)
//            }
//
//            if (animeInfo != null) {
//                if (animeList[0].airing == true) {
//                    animeInfo.text = "${animeList[0].name}\n${animeList[0].type}\n${animeList[0].season}\nStill Airing!\n${animeList[0].score}%"
//                } else {
//                    animeInfo.text = "${animeList[0].name}\n${animeList[0].type}\n${animeList[0].season}\n${animeList[0].eps}/${animeList[0].eps}\n${animeList[0].score}%"
//                }
//            }
//            if (animeDesc != null) {
//                animeDesc.text = animeList[0].desc
//            }
//        }
    }

    private fun updateInfo(newImage: Drawable?, anime: Anime) {
        // Get animeInfo and animeDesc TextView object
        val animeInfo: TextView? = view?.findViewById<TextView>(R.id.animeInfo)
        val animeDesc: TextView? = view?.findViewById<TextView>(R.id.animeDesc)

        // Hide the other covers and show the detailed info
        activity?.runOnUiThread {
            // Hide
            view?.findViewById<LinearLayout>(R.id.anime2LL)?.visibility = GONE
            view?.findViewById<LinearLayout>(R.id.anime3LL)?.visibility = GONE
            view?.findViewById<LinearLayout>(R.id.anime4LL)?.visibility = GONE
            view?.findViewById<TextView>(R.id.title1)?.visibility = GONE

            // Show
            animeInfo?.visibility = VISIBLE
            animeDesc?.visibility = VISIBLE
        }

        // Update cover image of the first ImageView (top left) and detailed information
        activity?.runOnUiThread {
            view?.findViewById<ImageView>(R.id.coverImage1)?.setImageDrawable(newImage)

            if (anime.airing == true) {
                animeInfo?.text = "${anime.name}\n${anime.type}\n${anime.season}\nStill Airing!\n${anime.score}"
            } else {
                animeInfo?.text = "${anime.name}\n${anime.type}\n${anime.season}\n${anime.eps}/${anime.eps}\n${anime.score}"
            }

            animeDesc?.text = anime.desc
        }
    }

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
//        val coverImage = view?.findViewById<ImageView>(R.id.coverImage)
//        val animeInfo = view?.findViewById<TextView>(R.id.animeInfo)
//
//        if (animeInfo != null) {
//            activity?.runOnUiThread {
//                Thread.sleep(1500)
//                if (coverImage != null) {
//                    coverImage.visibility = VISIBLE
//                    Glide.with(this).load(anime.cover).into(coverImage)
//                }
//                view?.findViewById<TextView>(R.id.animeDesc)?.setText(anime.desc)
//                if (anime.eps == null) {
//                    animeInfo.setText("${anime.name}\n${anime.type}\n${anime.season}\nStill Airing!\n${anime.score}")
//                } else {
//                    animeInfo.setText("${anime.name}\n${anime.type}\n${anime.season}\n${anime.eps}/${anime.eps}\n${anime.score}")
//                }
//            }
//        }
//
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}