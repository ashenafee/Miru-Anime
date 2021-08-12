package com.ashenafee.miruanime.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.ashenafee.miruanime.Anime
import com.ashenafee.miruanime.R
import com.ashenafee.miruanime.databinding.FragmentHomeBinding
import com.ashenafee.miruanime.ui.search.apolloClient
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.miruanime.UserQuery
import com.miruanime.type.MediaListStatus


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val animeList = mutableListOf<Anime>()
    private val animeListL = mutableListOf<Anime>()
    private val animeListR = mutableListOf<Anime>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getView()?.findViewById<Button>(R.id.searchButton)?.setOnClickListener {
            anilistUserGraphQL()
        }
    }

    private fun anilistUserGraphQL() {
        // Grab specified username
        val searchInput = view?.findViewById<TextInputEditText>(R.id.anilistUsername)?.text.toString()

        // Grab list choice selected
        val listInput = view?.findViewById<Spinner>(R.id.animeListChoice)?.selectedItem.toString()
        var listType: MediaListStatus? = null

        // Check if user inputted a query
        if (searchInput != "") {
            // Grab data via AniList GraphQL API
            lifecycleScope.launchWhenResumed {
                // Save data to <response>
                val response = apolloClient.query(UserQuery(searchInput.toInput())).await()

                // Get length of each list
                val currLen = response.data?.mediaListCollection?.lists?.get(0)?.entries?.size
                val compLen = response.data?.mediaListCollection?.lists?.get(1)?.entries?.size
                val dropLen = response.data?.mediaListCollection?.lists?.get(2)?.entries?.size
                val planLen = response.data?.mediaListCollection?.lists?.get(3)?.entries?.size

                // Change while-loop based on list selection
                var i = 0
                when (listInput) {
                    "All" -> {
                        subListCreation(0, currLen, response, searchInput)
                        subListCreation(1, compLen, response, searchInput)
                        subListCreation(2, dropLen, response, searchInput)
                        subListCreation(3, planLen, response, searchInput)
                    }
                    "Watching" -> {
                        subListCreation(0, currLen, response, searchInput)
                    }
                    "Completed" -> {
                        subListCreation(1, compLen, response, searchInput)
                    }
                    "Dropped" -> {
                        subListCreation(2, dropLen, response, searchInput)
                    }
                    "Planning" -> {
                        subListCreation(3, planLen, response, searchInput)
                    }
                }

                // Sort <animeList> alphabetically
                animeList.sortBy{ it.name?.lowercase()}

                // Update the UI
                updateUi()

                // Clear lists
                animeList.clear()
                animeListL.clear()
                animeListR.clear()

            }
        }
    }

    private fun subListCreation(type: Int, length: Int?, response: Response<UserQuery.Data>, searchInput: String) {

        var i = 0
        while (i < length!!) {

            try {
                // Create anime object
                val anime = Anime()

                // Populate <anime> with data
                anime.name = response.data?.mediaListCollection?.lists?.get(type)?.entries?.get(i)?.media?.title?.romaji
                anime.type = response.data?.mediaListCollection?.lists?.get(type)?.entries?.get(i)?.media?.format?.name
                anime.eps = response.data?.mediaListCollection?.lists?.get(type)?.entries?.get(i)?.media?.episodes
                anime.cover = response.data?.mediaListCollection?.lists?.get(type)?.entries?.get(i)?.media?.coverImage?.extraLarge
                anime.link = response.data?.mediaListCollection?.lists?.get(type)?.entries?.get(i)?.media?.siteUrl
                anime.progress = response.data?.mediaListCollection?.lists?.get(type)?.entries?.get(i)?.progress

                // Print log of <anime>
                Log.d("AniList GraphQL", ".\n${"=".repeat(100)}\n${anime.name} (${anime.type})\n${searchInput} has watched ${anime.progress} of ${anime.eps} episodes.\n${anime.link}\n${"=".repeat(100)}")

                // Add <anime> to <animeList>
                animeList.add(anime)
            } catch (e: IndexOutOfBoundsException) {
                println("No more results")
            }

            i += 1
        }
    }

    private fun updateUi() {

        // Reset scroll
        val scrollView = view?.findViewById<ScrollView>(R.id.scrollView)
        scrollView?.scrollTo(0, 0)

        // Get parent list object
        val listEntries = view?.findViewById<LinearLayout>(R.id.listEntries)

        // Check if previous queries are there
        if (listEntries?.childCount!! > 1) {
            println("Before:" + listEntries.childCount)
            listEntries.removeViews(1, listEntries.childCount - 1)
            println("After:" + listEntries.childCount)
        }

        // Divide <animeList>
        for (i in 0 until animeList.size) {
            if (i % 2 == 0) {
                animeListL.add(animeList[i])
            } else {
                animeListR.add(animeList[i])
            }
        }

        if (animeList.size > 2) {
            for (i in 1 until (animeList.size / 2)) {
                // Duplicate the row
                val view: View = LayoutInflater.from(activity).inflate(R.layout.list_row, null)

                // Get parents to the objects of interest
                val linearLayoutParent = (view as ViewGroup).getChildAt(0)
                val linearLayoutL = (linearLayoutParent as ViewGroup).getChildAt(0)
                val linearLayoutR = linearLayoutParent.getChildAt(1)

                // Store the left and right ImageView into different variables
                val imageViewL = (linearLayoutL as ViewGroup).getChildAt(0)
                val imageViewR = (linearLayoutR as ViewGroup).getChildAt(0)

                // Store the left and right TextView into different variables
                val textViewL = linearLayoutL.getChildAt(1)
                val textViewR = linearLayoutR.getChildAt(1)


                // Change the ImageViews to display the covers
                if (imageViewL != null) {
                    imageViewL.visibility = View.VISIBLE
                    Glide.with(this).load(animeListL[i].cover).into(imageViewL as ImageView)
                }
                if (imageViewR != null) {
                    imageViewR.visibility = View.VISIBLE
                    Glide.with(this).load(animeListR[i].cover).into(imageViewR as ImageView)
                }

                (textViewL as TextView).text = animeListL[i].name
                (textViewR as TextView).text = animeListR[i].name

                activity?.runOnUiThread {

                    // Create objects for the first two entries
                    val firstEntry = activity?.findViewById<LinearLayout>(R.id.listItem1)
                    val secondEntry = activity?.findViewById<LinearLayout>(R.id.listItem2)

                    // Update images
                    val firstImage = (firstEntry as ViewGroup).getChildAt(0)
                    val secondImage = (secondEntry as ViewGroup).getChildAt(0)

                    if (firstImage != null) {
                        firstImage.visibility = View.VISIBLE
                        Glide.with(this).load(animeListL[0].cover).into(firstImage as ImageView)
                    }
                    if (secondImage != null) {
                        secondImage.visibility = View.VISIBLE
                        Glide.with(this).load(animeListR[0].cover).into(secondImage as ImageView)
                    }

                    // Update titles
                    val firstTitle = (firstEntry as ViewGroup).getChildAt(1)
                    val secondTitle = (secondEntry as ViewGroup).getChildAt(1)

                    (firstTitle as TextView).text = animeListL[0].name
                    (secondTitle as TextView).text = animeListR[0].name

                }

                listEntries.addView(view)
            }
        } else if (animeList.size == 2) {
            activity?.runOnUiThread {

                // Create objects for the first two entries
                val firstEntry = activity?.findViewById<LinearLayout>(R.id.listItem1)
                val secondEntry = activity?.findViewById<LinearLayout>(R.id.listItem2)

                // Update images
                val firstImage = (firstEntry as ViewGroup).getChildAt(0)
                val secondImage = (secondEntry as ViewGroup).getChildAt(0)

                if (firstImage != null) {
                    firstImage.visibility = View.VISIBLE
                    Glide.with(this).load(animeListL[0].cover).into(firstImage as ImageView)
                }
                if (secondImage != null) {
                    secondImage.visibility = View.VISIBLE
                    Glide.with(this).load(animeListR[0].cover).into(secondImage as ImageView)
                }

                // Update titles
                val firstTitle = (firstEntry as ViewGroup).getChildAt(1)
                val secondTitle = (secondEntry as ViewGroup).getChildAt(1)

                (firstTitle as TextView).text = animeListL[0].name
                (secondTitle as TextView).text = animeListR[0].name

            }
        } else {
            activity?.runOnUiThread {

                // Create objects for the first entry
                val firstEntry = activity?.findViewById<LinearLayout>(R.id.listItem1)

                // Update image
                val firstImage = (firstEntry as ViewGroup).getChildAt(0)

                if (firstImage != null) {
                    firstImage.visibility = View.VISIBLE
                    Glide.with(this).load(animeListL[0].cover).into(firstImage as ImageView)
                }

                // Update title
                val firstTitle = (firstEntry as ViewGroup).getChildAt(1)

                (firstTitle as TextView).text = animeListL[0].name

            }
        }

    }

//    private fun getWebsite() {
//        // Grab text of search query
//        val searchInput = view?.findViewById<TextInputEditText>(R.id.searchTerm)?.text.toString()
//
//        if (searchInput != "") {
//            // Create MAL search query link
//            val malQuery = "https://myanimelist.net/animelist/$searchInput?status=1&order=1&order2=0"
//            println(malQuery)
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
//        // Check if previous user data is present
//        val linearLayout = view?.findViewById<LinearLayout>(R.id.malOut)
//        val malCWTitle = view?.findViewById<TextView>(R.id.malCWTitle)
//
//        if (malCWTitle != null) {
//            if (malCWTitle.text != "'s Currently Watching on MAL") {
//                activity?.runOnUiThread {
//                    malCWTitle.text = "'s Currently Watching on MAL"
//                    linearLayout?.removeViews(1, linearLayout.childCount - 1)
//                }
//            }
//        }
//
//        // Get the raw data of the table after search
//        val rawTable = doc.getElementsByTag("table")[0].attr("data-items").toString()
//
//        // Create a mutable list of the data from <rawTable>
//        val splitTable = rawTable.split("}").toTypedArray()
//
//        val animeList: MutableList<Map<String, String>> = mutableListOf()
//        val searchInput = view?.findViewById<TextInputEditText>(R.id.searchTerm)?.text.toString()
//        activity?.runOnUiThread {
//            if (malCWTitle != null) {
//                malCWTitle.text = "$searchInput${malCWTitle.text}"
//            }
//        }
//        for (item in splitTable) {
//            try {
//
//                // Anime name
//                val animeNameRaw = item.slice(IntRange(item.indexOf("\"anime_title\":", 0), item.indexOf(",\"anime_num_episodes\":", 0)))
//                val animeName = animeNameRaw.slice(IntRange(14 + 1, animeNameRaw.length - 3))
//
//                // Total episodes
//                val animeTEpsRaw = item.slice(IntRange(item.indexOf("\"anime_num_episodes\":", 0), item.indexOf(",\"anime_airing_status\":", 0)))
//                val animeTEps = animeTEpsRaw.slice(IntRange(21, animeTEpsRaw.length - 2))
//
//                // Episodes watched
//                val animeEpsRaw = item.slice(IntRange(item.indexOf("\"num_watched_episodes\":", 0), item.indexOf(",\"anime_title\":", 0)))
//                val animeEps = animeEpsRaw.slice(IntRange(23, animeEpsRaw.length - 2))
//
//                // Anime cover
//                val animeCoverRaw = item.slice(IntRange(item.indexOf("\"anime_image_path\":", 0), item.indexOf(",\"is_added_to_list\":", 0)))
//                val animeCover = animeCoverRaw.slice(IntRange(20, animeCoverRaw.length - 3)).replace("\\", "")
//
//                // Anime MAL link
//                val animeMALRaw = item.slice(IntRange(item.indexOf("\"anime_url\":", 0), item.indexOf(",\"anime_image_path\":", 0)))
//                val animeMAL = "https://myanimelist.net/${animeMALRaw.slice(IntRange(13, animeMALRaw.length - 3)).replace("\\", "")}"
//
//                // Media type
//                val animeTypeRaw = item.slice(IntRange(item.indexOf("\"anime_media_type_string\":", 0), item.indexOf(",\"anime_mpaa_rating_string\":", 0)))
//                val animeType = animeTypeRaw.slice(IntRange(27, animeTypeRaw.length - 3))
//
//                // Create dictionaries
//                val animeEntry = mutableMapOf(
//                    "Name" to animeName,
//                    "Watched" to animeEps,
//                    "Total" to animeTEps,
//                    "Cover" to animeCover,
//                    "MAL" to animeMAL,
//                    "Type" to animeType
//                )
//
//                // Add HD cover
//                getAnimeMALSite(animeEntry["MAL"], animeEntry)
//
//                // Add dictionary to list
//                animeList.add(animeEntry)
//                println("Watched $animeEps / $animeTEps episode(s) of $animeName. This anime is $animeType and can be found at $animeMAL.")
//
//            } catch (e: StringIndexOutOfBoundsException) {
//                println("End of List!")
//            }
//        }
//
//        // Update UI
//        //addInfoToUi(animeList)
//    }
//
//    private fun getAnimeMALSite(url: String?, dict: MutableMap<String, String>) {
//        val t: Thread = object : Thread() {
//            override fun run() {
//                val builder = StringBuilder()
//                try {
//                    val doc: Document = Jsoup.connect(url).get()
//                    getMoreAnimeInfo(doc, dict)
//                } catch (e: IOException) {
//                    builder.append("Error : ").append(e.message).append("\n")
//                }
//            }
//        }
//        t.start()
//        t.join()
//    }
//
//    private fun getMoreAnimeInfo(doc: Document, dict: MutableMap<String, String>) {
//        val animeCover = doc.getElementsByClass("borderClass")[0].getElementsByTag("img").attr("data-src")
//        dict.put("HD", animeCover)
//        println(dict["HD"])
//
//        // Update anime list
//        val linearLayout = view?.findViewById<LinearLayout>(R.id.malOut)
//
//        if (linearLayout != null) {
//            activity?.runOnUiThread {
//                // Add images
//                val imageView = ImageView(activity)
//                imageView.adjustViewBounds = true
//                imageView.setPadding(10, 10, 10, 10)
//                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//                imageView.cropToPadding = true
//                imageView.layoutParams = LinearLayout.LayoutParams(500, 705)
//                imageView.setBackgroundColor(Color.parseColor("#E57373"))
//                Glide.with(this).load(dict["HD"]).into(imageView)
//                linearLayout.addView(imageView)
//
//                // Add text
//                val textView = TextView(activity)
//                textView.width = ViewGroup.LayoutParams.WRAP_CONTENT
//                textView.setTextColor(Color.parseColor("#D32F2F"))
//                textView.text = StringEscapeUtils.unescapeJava(
//                            "${dict["Name"]} (${dict["Type"]})\n\t" +
//                            "• Episode ${dict["Watched"]}/${dict["Total"]}\n\t"
//                )
//                linearLayout.addView(textView)
//            }
//        }
//    }
//
//    private fun addInfoToUi(list: MutableList<Map<String, String>>) {
//        // Update anime list
//        val linearLayout = view?.findViewById<LinearLayout>(R.id.malOut)
//
//        if (linearLayout != null) {
//            activity?.runOnUiThread {
//                linearLayout.removeAllViews()
//                for (item in list) {
//
//                    // Add images
//                    val imageView = ImageView(activity)
//                    imageView.layoutParams = LinearLayout.LayoutParams(400, 564)
//                    imageView.setBackgroundColor(Color.BLACK)
//                    Glide.with(this).load(item["Cover"]).into(imageView)
//                    linearLayout.addView(imageView)
//
//                    // Add text
////                    val textView = TextView(activity)
////                    textView.width = ViewGroup.LayoutParams.WRAP_CONTENT
////                    textView.setTextColor(Color.parseColor("#D32F2F"))
////                    textView.text = StringEscapeUtils.unescapeJava(
////                                "${item["Name"]} (${item["Type"]})\n\t" +
////                                "• Episode ${item["Watched"]}/${item["Total"]}\n\t" +
////                                "• Can be found at ${item["MAL"]}\n\t"
////                    )
////                    linearLayout.addView(textView)
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