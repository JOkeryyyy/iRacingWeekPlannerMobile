package com.iracingweekplanner.mobile.domain

data class PlannerFilters(
    val carOwnership: ContentOwnershipFilter = ContentOwnershipFilter.ANY,
    val trackOwnership: ContentOwnershipFilter = ContentOwnershipFilter.ANY,
    val favorites: FavoriteFilter = FavoriteFilter.ANY,
    val ownedCarIds: Set<CarId> = emptySet(),
    val favoriteCarIds: Set<CarId> = emptySet(),
    val ownedTrackIds: Set<TrackId> = emptySet(),
    val favoriteTrackIds: Set<TrackId> = emptySet(),
    val selectedSeriesIds: Set<SeriesId> = emptySet(),
    val selectedCategories: Set<SeriesCategory> = emptySet(),
)

enum class ContentOwnershipFilter {
    ANY,
    OWNED,
    UNOWNED,
}

enum class FavoriteFilter {
    ANY,
    FAVORITES_ONLY,
}
