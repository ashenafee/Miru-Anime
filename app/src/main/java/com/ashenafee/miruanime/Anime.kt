package com.ashenafee.miruanime

import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder

class Anime {

    // Basic information
    var name: String? = null    // Name (i.e. Shingeki no Kyojin)
    var type: String? = null    // Type (i.e. TV, Movie)
    var desc: String? = null    // Description

    // Airing information
    var season: String? = null  // Season (i.e. Spring 2013)
    var eps: Int? = null        // Episode count (i.e. 25)
    var airing: Boolean? = null // Airing status (i.e. false)

    // Statistic information
    var score: Int? = null      // Score (i.e. 85%)

    // Side information
    var link: String? = null    // AniList link (i.e. https://anilist.co/anime/16498/)
    var cover: String? = null   // Cover image (i.e. https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx16498-m5ZMNtFioc7j.png)

    // User information
    var progress: Int? = null // User progress (i.e. 10)

}