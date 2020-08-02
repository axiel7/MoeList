package com.axiel7.moelist.model

data class AnimeList(
    val data: Data
)

data class Data(
    val node: Node
)

data class Node(
    val id: Int,
    val title: String,
    val main_picture: MainPicture
)

data class MainPicture(
    val medium: String,
    val large: String
)

