package com.axiel7.moelist.data.model.media

abstract class BaseMediaNode {
    abstract val id: Int
    abstract val title: String
    abstract val mainPicture: MainPicture?
    abstract val numListUsers: Int?
    abstract val mediaType: String?
    abstract val status: String?
    abstract val mean: Float?
}