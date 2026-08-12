package com.tionix.rms.feature.refile.presentation

import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import com.tionix.rms.feature.refile.domain.model.Box
import com.tionix.rms.feature.refile.domain.model.FileRecord
import com.tionix.rms.feature.refile.domain.model.Location
import com.tionix.rms.feature.refile.domain.model.LocationType
import com.tionix.rms.feature.refile.domain.model.RefileAction
import com.tionix.rms.feature.refile.domain.model.RefileActionStatus
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import com.tionix.rms.feature.refile.domain.usecase.ConfirmRefileUseCase
import com.tionix.rms.feature.refile.domain.usecase.EndSessionUseCase
import com.tionix.rms.feature.refile.domain.usecase.GetHomeLocationUseCase
import com.tionix.rms.feature.refile.domain.usecase.OverrideMismatchUseCase
import com.tionix.rms.feature.refile.domain.usecase.ScanFileUseCase
import com.tionix.rms.feature.refile.domain.usecase.StartSessionUseCase
import com.tionix.rms.feature.refile.domain.usecase.UndoLastActionUseCase
import com.tionix.rms.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RefileViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = mockk<RefileRepository>(relaxed = true)
    private val scanFileUseCase = mockk<ScanFileUseCase>()
    private val confirmRefileUseCase = mockk<ConfirmRefileUseCase>()
    private val getHomeLocationUseCase = mockk<GetHomeLocationUseCase>(relaxed = true)
    private val overrideMismatchUseCase = mockk<OverrideMismatchUseCase>(relaxed = true)
    private val startSessionUseCase = mockk<StartSessionUseCase>(relaxed = true)
    private val endSessionUseCase = mockk<EndSessionUseCase>(relaxed = true)
    private val undoLastActionUseCase = mockk<UndoLastActionUseCase>(relaxed = true)
    private val scannerRepository = mockk<ScannerRepository>()
    private val initializeScannerUseCase = mockk<InitializeScannerUseCase>(relaxed = true)
    private val startScanningUseCase = mockk<StartScanningUseCase>(relaxed = true)
    private val stopScanningUseCase = mockk<StopScanningUseCase>(relaxed = true)
    private val beepPlayer = mockk<BeepPlayer>(relaxed = true)

    private lateinit var viewModel: RefileViewModel

    @Before
    fun setUp() {
        every { scannerRepository.scanResults } returns MutableSharedFlow()
        coEvery { repository.getAssignedRefiles() } returns Result.success(emptyList())

        viewModel = RefileViewModel(
            repository = repository,
            scanFileUseCase = scanFileUseCase,
            getHomeLocationUseCase = getHomeLocationUseCase,
            confirmRefileUseCase = confirmRefileUseCase,
            overrideMismatchUseCase = overrideMismatchUseCase,
            startSessionUseCase = startSessionUseCase,
            endSessionUseCase = endSessionUseCase,
            undoLastActionUseCase = undoLastActionUseCase,
            scannerRepository = scannerRepository,
            initializeScannerUseCase = initializeScannerUseCase,
            startScanningUseCase = startScanningUseCase,
            stopScanningUseCase = stopScanningUseCase,
            beepPlayer = beepPlayer
        )
    }

    @Test
    fun test_BR06_wrongBox_blockedWithMismatchDialog() = runTest {
        advanceUntilIdle()

        val file = sampleFile()
        coEvery { scanFileUseCase("FILE-001") } returns Result.success(file)
        coEvery { confirmRefileUseCase("FILE-001", "BOX-WRONG") } returns
            Result.failure(IllegalStateException("Wrong box"))

        viewModel.onScannedBarcodeChanged("FILE-001")
        viewModel.scanFile()
        advanceUntilIdle()

        viewModel.onDestinationBoxBarcodeChanged("BOX-WRONG")
        viewModel.confirmRefile()
        advanceUntilIdle()

        assertTrue(viewModel.showMismatchDialog.value)
        verify { beepPlayer.error() }
    }

    @Test
    fun test_BR06_correctBox_advancesSession() = runTest {
        advanceUntilIdle()

        val file = sampleFile()
        val action = RefileAction(
            id = "action-1",
            fileRecord = file,
            sourceBox = file.currentBox,
            destinationBox = file.currentBox,
            status = RefileActionStatus.CONFIRMED,
            timestamp = "2026-01-01"
        )
        coEvery { scanFileUseCase("FILE-001") } returns Result.success(file)
        coEvery { confirmRefileUseCase("FILE-001", "BOX-001") } returns Result.success(action)

        viewModel.onScannedBarcodeChanged("FILE-001")
        viewModel.scanFile()
        advanceUntilIdle()

        viewModel.onDestinationBoxBarcodeChanged("BOX-001")
        viewModel.confirmRefile()
        advanceUntilIdle()

        assertTrue(viewModel.sessionActions.value.size == 1)
        verify { beepPlayer.positive() }
    }

    private fun sampleFile(): FileRecord {
        val location = Location(
            id = "loc-1",
            barcode = "LOC-001",
            name = "Location",
            room = "Room",
            rack = null,
            shelf = null,
            type = LocationType.LOCATION
        )
        val box = Box(
            id = "box-1",
            barcode = "BOX-001",
            description = "Box",
            location = location
        )
        return FileRecord(
            id = "file-1",
            barcode = "FILE-001",
            title = "Title",
            currentBox = box,
            currentLocation = location
        )
    }
}
