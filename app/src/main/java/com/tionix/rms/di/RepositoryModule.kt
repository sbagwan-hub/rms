package com.tionix.rms.di

import com.tionix.rms.core.location.data.repository.LocationRepositoryImpl
import com.tionix.rms.core.location.domain.repository.LocationRepository
import com.tionix.rms.core.scanner.data.repository.ScannerRepositoryImpl
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.sync.data.repository.SyncRepositoryImpl
import com.tionix.rms.core.sync.domain.repository.SyncRepository
import com.tionix.rms.feature.auth.data.repository.AuthRepositoryImpl
import com.tionix.rms.feature.auth.domain.repository.AuthRepository
import com.tionix.rms.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.tionix.rms.feature.dashboard.domain.repository.DashboardRepository
import com.tionix.rms.feature.freshboxmove.data.repository.FreshBoxMoveRepositoryImpl
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository
import com.tionix.rms.feature.inventory.data.repository.InventoryVerificationRepositoryImpl
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import com.tionix.rms.feature.merge.data.repository.MergeRepositoryImpl
import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import com.tionix.rms.feature.notifications.data.repository.NotificationsRepositoryImpl
import com.tionix.rms.feature.notifications.domain.repository.NotificationsRepository
import com.tionix.rms.feature.refile.data.repository.RefileRepositoryImpl
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import com.tionix.rms.feature.reports.data.repository.ReportsRepositoryImpl
import com.tionix.rms.feature.reports.domain.repository.ReportsRepository
import com.tionix.rms.feature.search.data.repository.SearchRepositoryImpl
import com.tionix.rms.feature.search.domain.repository.SearchRepository
import com.tionix.rms.feature.segregation.data.repository.SegregationRepositoryImpl
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import com.tionix.rms.feature.transfer.data.repository.TransferRepositoryImpl
import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import com.tionix.rms.utils.scanner.ScannerManager
import com.tionix.rms.utils.scanner.HoneywellScannerManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScannerManager(impl: HoneywellScannerManager): ScannerManager

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindFreshBoxMoveRepository(impl: FreshBoxMoveRepositoryImpl): FreshBoxMoveRepository

    @Binds
    @Singleton
    abstract fun bindInventoryVerificationRepository(impl: InventoryVerificationRepositoryImpl): InventoryVerificationRepository

    @Binds
    @Singleton
    abstract fun bindMergeRepository(impl: MergeRepositoryImpl): MergeRepository

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository

    @Binds
    @Singleton
    abstract fun bindRefileRepository(impl: RefileRepositoryImpl): RefileRepository

    @Binds
    @Singleton
    abstract fun bindReportsRepository(impl: ReportsRepositoryImpl): ReportsRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindSegregationRepository(impl: SegregationRepositoryImpl): SegregationRepository

    @Binds
    @Singleton
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindScannerRepository(impl: ScannerRepositoryImpl): ScannerRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: com.tionix.rms.feature.history.data.repository.HistoryRepositoryImpl
    ): com.tionix.rms.feature.history.domain.repository.HistoryRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: com.tionix.rms.feature.profile.data.repository.ProfileRepositoryImpl
    ): com.tionix.rms.feature.profile.domain.repository.ProfileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: com.tionix.rms.feature.settings.data.repository.SettingsRepositoryImpl
    ): com.tionix.rms.feature.settings.domain.repository.SettingsRepository

    @Binds
    @Singleton
    abstract fun bindFeatureSyncRepository(
        impl: com.tionix.rms.feature.sync.data.repository.SyncRepositoryImpl
    ): com.tionix.rms.feature.sync.domain.repository.SyncRepository

    @Binds
    @Singleton
    abstract fun bindFileSearchRepository(
        impl: com.tionix.rms.feature.filesearch.data.repository.FileSearchRepositoryImpl
    ): com.tionix.rms.feature.filesearch.domain.repository.FileSearchRepository
}
