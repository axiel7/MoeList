@file:Suppress("unused")

package com.axiel7.moelist._GitHubPRs.Anilist
import com.axiel7.moelist.BuildConfig

//package com.axiel7.anihyou.utils
//import com.axiel7.anihyou.BuildConfig

const val ANILIST_GRAPHQL_URL = "https://graphql.anilist.co"
const val ANILIST_URL = "https://anilist.co"

const val ANILIST_API_URL = "$ANILIST_URL/api/v2"
const val ANILIST_AUTH_URL = "$ANILIST_API_URL/oauth/authorize"
const val ANIHYOU_AUTH_URL =
    "$ANILIST_AUTH_URL?client_id=${BuildConfig.ANILIST_CLIENT_ID}&response_type=token"
const val ANIHYOU_SCHEME = "anihyou"
const val ANILIST_CALLBACK_URL = "$ANIHYOU_SCHEME://auth-response"
const val ANILIST_GRAPHQL = "https://graphql.anilist.co/graphql"

const val ANILIST_ANIME_URL = "$ANILIST_URL/anime/"
const val ANILIST_MANGA_URL = "$ANILIST_URL/manga/"
const val ANILIST_THREAD_URL = "$ANILIST_URL/forum/thread/"
const val ANILIST_REVIEW_URL = "$ANILIST_URL/review/"

const val MAL_API_URL = "https://api.myanimelist.net/v2/"

const val YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v="
const val DAILYMOTION_VIDEO_URL = "https://www.dailymotion.com/video/"

const val ANILIST_ACCOUNT_SETTINGS_URL = "https://anilist.co/settings/account"
const val GITHUB_REPO_URL = "https://github.com/axiel7/AniHyou-android"
const val GITHUB_PROFILE_URL = "https://github.com/axiel7"
const val DISCORD_SERVER_URL = "https://discord.gg/CTv3WdfxHh"
const val CROWDIN_URL = "https://crowdin.com/project/anihyou"

const val UNKNOWN_CHAR = "─"

const val UTF_8 = "UTF-8"
