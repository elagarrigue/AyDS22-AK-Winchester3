package ayds.winchester.songinfo.home.view

import ayds.winchester.songinfo.home.controller.HomeControllerInjector
import ayds.winchester.songinfo.home.model.HomeModelInjector

object HomeViewInjector {
    val resultReleaseDate : ResultReleaseDate = ResultReleaseDateImpl()
    private val songDescriptionHelper: SongDescriptionHelper = SongDescriptionHelperImpl(resultReleaseDate)

    fun init(homeView: HomeView) {
        HomeModelInjector.initHomeModel(homeView)
        HomeControllerInjector.onViewStarted(homeView)
    }
}