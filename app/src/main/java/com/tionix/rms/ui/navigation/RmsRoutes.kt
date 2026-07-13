package com.tionix.rms.ui.navigation

object RmsRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val SEARCH = "search"
    const val BOX_DETAIL = "box_detail/{boxId}"
    const val FILE_SEARCH = "file_search"
    const val FILE_DETAIL = "file_detail/{fileId}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val SYNC_QUEUE = "sync_queue"
    const val REPORTS = "reports"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
    const val FRESH_BOX_MOVE = "fresh_box_move"
    const val INVENTORY_VERIFICATION = "inventory_verification"
    const val REFILE = "refile"
    const val TRANSFER = "transfer"
    const val SEGREGATION = "segregation"
    const val MERGE = "merge"

    /** Routes that show the bottom navigation bar. */
    val bottomBarRoutes = setOf(HOME, SEARCH, REPORTS, PROFILE)
    
    fun boxDetail(boxId: String) = "box_detail/$boxId"
    fun fileDetail(fileId: String) = "file_detail/$fileId"
}
