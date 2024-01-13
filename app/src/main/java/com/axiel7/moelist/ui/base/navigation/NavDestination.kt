package com.axiel7.moelist.ui.base.navigation

import com.axiel7.moelist.data.model.media.MediaType

enum class NavDestination(
    private val arguments: List<DestArgument> = emptyList()
) {
    HomeTab,

    AnimeTab(
        arguments = listOf(
            DestArgument(
                argument = NavArgument.MediaType,
                defaultValue = MediaType.ANIME.name
            )
        )
    ),

    MangaTab(
        arguments = listOf(
            DestArgument(
                argument = NavArgument.MediaType,
                defaultValue = MediaType.MANGA.name
            )
        )
    ),

    MoreTab,

    MediaRanking(
        arguments = listOf(
            DestArgument(argument = NavArgument.MediaType)
        )
    ),

    MediaDetails(
        arguments = listOf(
            DestArgument(argument = NavArgument.MediaType),
            DestArgument(argument = NavArgument.MediaId)
        )
    ),

    Calendar,

    SeasonChart,

    Recommendations,

    Profile,

    Search,

    FullPoster(
        arguments = listOf(
            DestArgument(argument = NavArgument.Pictures)
        )
    ),

    Settings,

    ListStyleSettings,

    Notifications,

    About,

    Credits,

    ;

    fun findDestArgument(argument: NavArgument) = arguments.find { it.argument == argument }

    fun route() = if (arguments.isEmpty()) name else {
        name + arguments
            .sortedBy { it.isNullable }
            .joinToString(separator = "") { arg ->
                if (arg.isNullable) "?${arg.argument.name}={${arg.argument.name}}"
                else "/{${arg.argument.name}}"
            }
    }

    val namedNavArguments get() = arguments.map { it.toNamedNavArgument() }

    fun putArguments(arguments: Map<NavArgument, String?>): String {
        var routeWithArguments = route()
        arguments.forEach { (arg, value) ->
            if (value != null)
                routeWithArguments = routeWithArguments.replace("{${arg.name}}", value)
        }
        return routeWithArguments
    }
}