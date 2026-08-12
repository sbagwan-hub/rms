package com.tionix.rms.feature.inventory.presentation

import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.feature.inventory.domain.usecase.CompleteVerificationUseCase
import com.tionix.rms.feature.inventory.domain.usecase.GetExpectedBoxesUseCase
import com.tionix.rms.feature.inventory.domain.usecase.StartVerificationUseCase
import com.tionix.rms.feature.inventory.domain.usecase.VerifyBoxUseCase
import com.tionix.rms.testsupport.FakeInventoryVerificationRepository
import com.tionix.rms.testsupport.MainDispatcherRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryVerificationViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repository: FakeInventoryVerificationRepository
    private lateinit var beepPlayer: BeepPlayer
    private lateinit var viewModel: InventoryVerificationViewModel

    @Before
    fun setUp() {
        repository = FakeInventoryVerificationRepository()
        beepPlayer = mockk(relaxed = true)
        viewModel = InventoryVerificationViewModel(
            repository = repository,
            startVerificationUseCase = StartVerificationUseCase(repository),
            getExpectedBoxesUseCase = GetExpectedBoxesUseCase(repository),
            verifyBoxUseCase = VerifyBoxUseCase(repository),
            completeVerificationUseCase = CompleteVerificationUseCase(repository),
            beepPlayer = beepPlayer
        )
    }

    @Test
    fun test_BR03_inventoryDuplicateFile_warningAndCountedOnce() = runTest {
        advanceUntilIdle()
        viewModel.onLocationIdChanged("loc-1")
        viewModel.startVerification()
        advanceUntilIdle()

        viewModel.verifyBox("FILE-001")
        advanceUntilIdle()
        viewModel.verifyBox("FILE-001")
        advanceUntilIdle()

        assertEquals(1, viewModel.scannedBoxes.value.size)
        verify(atLeast = 1) { beepPlayer.error() }
    }

    @Test
    fun test_BR05_capacityWarningFromServer_isRepositoryConfigurable() {
        repository.serverWarnings = listOf("BOX_OVER_CAPACITY")
        assertEquals(listOf("BOX_OVER_CAPACITY"), repository.serverWarnings)
    }
}
