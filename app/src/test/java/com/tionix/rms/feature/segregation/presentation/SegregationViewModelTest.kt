package com.tionix.rms.feature.segregation.presentation

import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import com.tionix.rms.feature.segregation.domain.model.Box
import com.tionix.rms.feature.segregation.domain.model.FileRecord
import com.tionix.rms.feature.segregation.domain.model.SegregationSession
import com.tionix.rms.feature.segregation.domain.model.SessionStatus
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import com.tionix.rms.feature.segregation.domain.usecase.CompleteSegregationSessionUseCase
import com.tionix.rms.feature.segregation.domain.usecase.MoveFileUseCase
import com.tionix.rms.feature.segregation.domain.usecase.ScanSourceBoxUseCase
import com.tionix.rms.feature.segregation.domain.usecase.ScanTargetBoxUseCase
import com.tionix.rms.feature.segregation.domain.usecase.StartSegregationSessionUseCase
import com.tionix.rms.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SegregationViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = mockk<SegregationRepository>(relaxed = true)
    private val startSegregationSessionUseCase = mockk<StartSegregationSessionUseCase>()
    private val scanSourceBoxUseCase = mockk<ScanSourceBoxUseCase>()
    private val scanTargetBoxUseCase = mockk<ScanTargetBoxUseCase>()
    private val moveFileUseCase = mockk<MoveFileUseCase>()
    private val completeSegregationSessionUseCase = mockk<CompleteSegregationSessionUseCase>(relaxed = true)
    private val scannerRepository = mockk<ScannerRepository>()
    private val initializeScannerUseCase = mockk<InitializeScannerUseCase>(relaxed = true)
    private val startScanningUseCase = mockk<StartScanningUseCase>(relaxed = true)
    private val stopScanningUseCase = mockk<StopScanningUseCase>(relaxed = true)
    private val beepPlayer = mockk<BeepPlayer>(relaxed = true)

    private lateinit var viewModel: SegregationViewModel

    private val sourceBox = Box("box-old", "BOX-OLD", "Old", "Loc A")
    private val targetBox = Box("box-new", "BOX-NEW", "New", "Loc B")
    private val sourceFile = FileRecord("file-1", "FILE-001", "Doc", "BOX-OLD")

    @Before
    fun setUp() {
        every { scannerRepository.scanResults } returns MutableSharedFlow()
        coEvery { repository.getAssignedSegregations() } returns Result.success(emptyList())
        coEvery { startSegregationSessionUseCase() } returns Result.success(
            SegregationSession(
                id = "session-1",
                sessionId = "session-1",
                sourceBox = sourceBox,
                targetBox = null,
                sourceFiles = listOf(sourceFile),
                movedFiles = emptyList(),
                status = SessionStatus.SCANNING_SOURCE,
                startTime = "2026-01-01",
                endTime = null
            )
        )
        coEvery { scanSourceBoxUseCase("BOX-OLD") } returns Result.success(sourceBox)
        coEvery { scanTargetBoxUseCase("BOX-NEW") } returns Result.success(targetBox)
        coEvery { moveFileUseCase("FILE-001") } returns Result.success(
            sourceFile.copy(boxBarcode = "BOX-NEW")
        )

        viewModel = SegregationViewModel(
            repository = repository,
            startSegregationSessionUseCase = startSegregationSessionUseCase,
            scanSourceBoxUseCase = scanSourceBoxUseCase,
            scanTargetBoxUseCase = scanTargetBoxUseCase,
            moveFileUseCase = moveFileUseCase,
            completeSegregationSessionUseCase = completeSegregationSessionUseCase,
            scannerRepository = scannerRepository,
            initializeScannerUseCase = initializeScannerUseCase,
            startScanningUseCase = startScanningUseCase,
            stopScanningUseCase = stopScanningUseCase,
            beepPlayer = beepPlayer
        )
    }

    @Test
    fun test_BR07_cannotMoveFilesBeforeOldBox() = runTest {
        advanceUntilIdle()
        viewModel.moveFile("FILE-001")
        advanceUntilIdle()
        assertEquals(null, viewModel.currentSession.value)
    }

    @Test
    fun test_BR53_newBoxEqualsOldBox_rejected() = runTest {
        advanceUntilIdle()
        viewModel.startSegregation()
        advanceUntilIdle()
        viewModel.scanSourceBox("BOX-OLD")
        advanceUntilIdle()
        viewModel.scanTargetBox("BOX-OLD")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is SegregationUiState.ValidationError)
        verify { beepPlayer.error() }
    }

    @Test
    fun test_BR08_outFile_errorBeep_inFile_positiveBeep() = runTest {
        advanceUntilIdle()
        viewModel.startSegregation()
        advanceUntilIdle()
        viewModel.scanSourceBox("BOX-OLD")
        advanceUntilIdle()
        viewModel.scanTargetBox("BOX-NEW")
        advanceUntilIdle()

        viewModel.moveFile("FILE-OUT")
        advanceUntilIdle()
        verify { beepPlayer.error() }

        viewModel.moveFile("FILE-001")
        advanceUntilIdle()
        verify { beepPlayer.positive() }
    }
}
