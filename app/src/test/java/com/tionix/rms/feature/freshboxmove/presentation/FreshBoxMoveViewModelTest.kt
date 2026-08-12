package com.tionix.rms.feature.freshboxmove.presentation

import android.content.Context
import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository
import com.tionix.rms.testsupport.FakeFreshBoxMoveRepository
import com.tionix.rms.testsupport.MainDispatcherRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FreshBoxMoveViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repository: FakeFreshBoxMoveRepository
    private lateinit var beepPlayer: BeepPlayer
    private lateinit var viewModel: FreshBoxMoveViewModel

    @Before
    fun setUp() {
        repository = FakeFreshBoxMoveRepository()
        beepPlayer = mockk(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        viewModel = FreshBoxMoveViewModel(repository, beepPlayer, context)
    }

    @Test
    fun test_BR01_boxBeforeLocation_notSubmitted() = runTest {
        advanceUntilIdle()
        viewModel.startSession("device-1")
        advanceUntilIdle()

        viewModel.submitScan("BOX-001")
        advanceUntilIdle()

        assertEquals(FreshBoxMoveViewModel.STEP_ROOM, viewModel.step.value)
        assertTrue(repository.scansForActiveSession().isEmpty())
    }

    @Test
    fun test_BR02_roomRackLocationSequence_advancesSteps() = runTest {
        advanceUntilIdle()
        viewModel.startSession("device-1")
        advanceUntilIdle()

        viewModel.handleBarcodeScan("ROOM-001")
        assertEquals(FreshBoxMoveViewModel.STEP_RACK, viewModel.step.value)

        viewModel.handleBarcodeScan("RACK-001")
        assertEquals(FreshBoxMoveViewModel.STEP_LOCATION, viewModel.step.value)

        viewModel.handleBarcodeScan("LOC-001")
        assertEquals(FreshBoxMoveViewModel.STEP_BOXES, viewModel.step.value)
        assertEquals("LOC-001", viewModel.locationBarcode.value)
    }

    @Test
    fun test_BR02_rackBeforeRoom_treatedAsRoom() = runTest {
        advanceUntilIdle()
        viewModel.startSession("device-1")
        advanceUntilIdle()

        viewModel.handleBarcodeScan("RACK-001")
        assertEquals(FreshBoxMoveViewModel.STEP_RACK, viewModel.step.value)
        assertEquals("RACK-001", viewModel.roomBarcode.value)
    }

    @Test
    fun test_BR03_duplicateBox_warningAndSingleEntry() = runTest {
        advanceUntilIdle()
        viewModel.startSession("device-1")
        advanceUntilIdle()

        viewModel.handleBarcodeScan("ROOM-001")
        viewModel.handleBarcodeScan("RACK-001")
        viewModel.handleBarcodeScan("LOC-001")
        viewModel.submitScan("BOX-001")
        advanceUntilIdle()
        viewModel.submitScan("BOX-001")
        advanceUntilIdle()

        assertEquals(1, repository.scansForActiveSession().size)
        verify(atLeast = 1) { beepPlayer.error() }
    }

    @Test
    fun test_BR04_tenthBox_warningStillSaved() = runTest {
        advanceUntilIdle()
        viewModel.startSession("device-1")
        advanceUntilIdle()

        viewModel.handleBarcodeScan("ROOM-001")
        viewModel.handleBarcodeScan("RACK-001")
        viewModel.handleBarcodeScan("LOC-001")

        repeat(10) { index ->
            viewModel.submitScan("BOX-${index + 1}")
            advanceUntilIdle()
        }

        assertEquals(10, repository.scansForActiveSession().size)
        verify(atLeast = 1) { beepPlayer.warning() }
    }

    @Test
    fun test_intake_skipRoomAndRack_advancesToLocation() = runTest {
        advanceUntilIdle()
        viewModel.startSession("device-1")
        advanceUntilIdle()

        viewModel.skipStep()
        assertEquals(FreshBoxMoveViewModel.STEP_RACK, viewModel.step.value)

        viewModel.skipStep()
        assertEquals(FreshBoxMoveViewModel.STEP_LOCATION, viewModel.step.value)
    }
}
