package com.ashenafee.miruanime.ui.search

import com.apollographql.apollo.ApolloClient

val apolloClient = ApolloClient.builder()
    .serverUrl("https://graphql.anilist.co/")
    .build()