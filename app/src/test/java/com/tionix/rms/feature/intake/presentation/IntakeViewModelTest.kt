package com.tionix.rms.feature.freshboxmove.presentation

import com.tionix.rms.testsupport.FakeFreshBoxMoveRepository
import com.tionix.rms.testsupport.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Intake workflow rules are implemented in [FreshBoxMoveViewModel] (Fresh Box Intake session).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntakeViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repository: FakeFreshBoxMoveRepository
    private lateinit var viewModel: FreshBoxMoveViewModel

    @Before
    fun setUp() {
        repository = FakeFreshBoxMoveRepository()
        viewModel = FreshBoxMoveViewModel(
            repository = repository,
            beepPlayer = mockk(relaxed = true),
            context = mockk(relaxed = true)
        )
    }

    @Test
    fun test_intake_duplicateBox_warningAndSingleEntry() = runTest {
        advanceUntilIdle()
        viewModel.startSession("device-1")
        advanceUntilIdle()
        viewModel.skipStep()
        viewModel.skipStep()
        viewModel.handleBarcodeScan("LOC-001")
        viewModel.submitScan("BOX-001")
        advanceUntilIdle()
        viewModel.submitScan("BOX-001")
        advanceUntilIdle()

        assertEquals(1, repository.scansForActiveSession().size)
    }

    @Test
    fun test_intake_skipClient_advancesToNextStep() = runTest {
        advanceUntilIdle()
        viewModel.startSession("device-1")
        advanceUntilIdle()

        viewModel.skipStep()
        assertEquals(FreshBoxMoveViewModel.STEP_RACK, viewModel.step.value)
    }
}
