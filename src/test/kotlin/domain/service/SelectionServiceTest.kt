package domain.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

@DisplayName("SelectionService Tests")
class SelectionServiceTest {

    private lateinit var selectionService: SelectionService

    @BeforeEach
    fun setUp() {
        selectionService = SelectionService()
    }

    @Nested
    @DisplayName("Single Click Selection")
    inner class SingleClickTests {

        @Test
        fun `should select single photo and set anchor`() {
            val result = selectionService.handleSingleClick(0, "photo1")
            
            assertEquals(setOf("photo1"), result.selectedPhotoIds)
            assertEquals(0, result.anchorIndex)
            assertTrue(selectionService.isSelected("photo1"))
        }

        @Test
        fun `should clear previous selection when single clicking`() {
            selectionService.handleSingleClick(0, "photo1")
            selectionService.handleCtrlClick(1, "photo2")
            
            val result = selectionService.handleSingleClick(2, "photo3")
            
            assertEquals(setOf("photo3"), result.selectedPhotoIds)
            assertFalse(selectionService.isSelected("photo1"))
            assertFalse(selectionService.isSelected("photo2"))
            assertTrue(selectionService.isSelected("photo3"))
        }

        @Test
        fun `should update anchor index on single click`() {
            selectionService.handleSingleClick(5, "photo6")
            
            assertEquals(5, selectionService.getSelection().anchorIndex)
        }
    }

    @Nested
    @DisplayName("Ctrl+Click Selection")
    inner class CtrlClickTests {

        @Test
        fun `should add photo to selection with ctrl click`() {
            selectionService.handleSingleClick(0, "photo1")
            val result = selectionService.handleCtrlClick(1, "photo2")
            
            assertEquals(setOf("photo1", "photo2"), result.selectedPhotoIds)
            assertTrue(selectionService.isSelected("photo1"))
            assertTrue(selectionService.isSelected("photo2"))
        }

        @Test
        fun `should toggle photo off with ctrl click on selected photo`() {
            selectionService.handleSingleClick(0, "photo1")
            selectionService.handleCtrlClick(1, "photo2")
            
            val result = selectionService.handleCtrlClick(1, "photo2")
            
            assertEquals(setOf("photo1"), result.selectedPhotoIds)
            assertTrue(selectionService.isSelected("photo1"))
            assertFalse(selectionService.isSelected("photo2"))
        }

        @Test
        fun `should update anchor on ctrl click`() {
            selectionService.handleSingleClick(0, "photo1")
            selectionService.handleCtrlClick(5, "photo6")
            
            assertEquals(5, selectionService.getSelection().anchorIndex)
        }

        @Test
        fun `should allow selecting multiple photos with ctrl click`() {
            selectionService.handleSingleClick(0, "photo1")
            selectionService.handleCtrlClick(2, "photo3")
            selectionService.handleCtrlClick(4, "photo5")
            
            assertEquals(setOf("photo1", "photo3", "photo5"), selectionService.getSelectedPhotoIds())
        }
    }

    @Nested
    @DisplayName("Shift+Click Range Selection")
    inner class ShiftClickTests {

        @Test
        fun `should select range in single row forward`() {
            // Arrange: 3 columns, anchor at index 0, click at index 2 (same row)
            selectionService.handleSingleClick(0, "photo1")
            
            // Act
            val result = selectionService.handleShiftClick(2, 10, 3)
            
            // Assert: should select indices 0, 1, 2
            assertEquals(listOf(0, 1, 2), result.selectedIndices)
        }

        @Test
        fun `should select range in single row backward`() {
            // Arrange: 3 columns, anchor at index 2, click at index 0
            selectionService.handleSingleClick(2, "photo3")
            
            // Act
            val result = selectionService.handleShiftClick(0, 10, 3)
            
            // Assert: should select indices 0, 1, 2
            assertEquals(listOf(0, 1, 2), result.selectedIndices)
        }

        @Test
        fun `should select range across multiple rows forward`() {
            // Arrange: 3 columns grid
            // Row 0: [0, 1, 2]
            // Row 1: [3, 4, 5]
            // Row 2: [6, 7, 8]
            // Anchor at 1, click at 7
            selectionService.handleSingleClick(1, "photo2")
            
            // Act
            val result = selectionService.handleShiftClick(7, 10, 3)
            
            // Assert: should select from column 1 of row 0, all of row 1, to column 1 of row 2
            // Expected: 1, 2 (rest of row 0), 3, 4, 5 (all of row 1), 6, 7 (up to click in row 2)
            assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), result.selectedIndices)
        }

        @Test
        fun `should select range across multiple rows backward`() {
            // Arrange: 3 columns, anchor at 7, click at 1
            selectionService.handleSingleClick(7, "photo8")
            
            // Act
            val result = selectionService.handleShiftClick(1, 10, 3)
            
            // Assert: same selection as forward, just initiated differently
            assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), result.selectedIndices)
        }

        @Test
        fun `should handle range selection at grid boundaries`() {
            // Arrange: 3 columns, 10 total photos
            // Row 3: [9] (only one photo in last row)
            selectionService.handleSingleClick(0, "photo1")
            
            // Act: select from 0 to 9 (all photos)
            val result = selectionService.handleShiftClick(9, 10, 3)
            
            // Assert: should select all 10 photos
            assertEquals(10, result.selectedIndices.size)
            assertEquals((0..9).toList(), result.selectedIndices)
        }

        @Test
        fun `should not select beyond total photos`() {
            // Arrange: 3 columns, but only 8 photos total
            selectionService.handleSingleClick(0, "photo1")
            
            // Act: try to select a full range but photos end at index 7
            val result = selectionService.handleShiftClick(8, 8, 3)
            
            // Assert: should only select up to index 7
            assertEquals((0..7).toList(), result.selectedIndices)
        }

        @Test
        fun `should return empty list if no anchor is set`() {
            // Act: shift click without setting anchor first
            val result = selectionService.handleShiftClick(5, 10, 3)
            
            // Assert
            assertTrue(result.selectedIndices.isEmpty())
        }

        @Test
        fun `should select single photo when anchor equals clicked index`() {
            // Arrange
            selectionService.handleSingleClick(3, "photo4")
            
            // Act: shift click on same index as anchor
            val result = selectionService.handleShiftClick(3, 10, 3)
            
            // Assert
            assertEquals(listOf(3), result.selectedIndices)
        }

        @Test
        fun `should handle different column counts`() {
            // Arrange: 5 columns grid
            selectionService.handleSingleClick(2, "photo3")
            
            // Act: select from 2 to 12 in 5-column grid
            val result = selectionService.handleShiftClick(12, 20, 5)
            
            // Assert: 
            // Row 0: [0, 1, 2, 3, 4] -> select 2, 3, 4
            // Row 1: [5, 6, 7, 8, 9] -> select all
            // Row 2: [10, 11, 12, 13, 14] -> select 10, 11, 12
            assertEquals(listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), result.selectedIndices)
        }
    }

    @Nested
    @DisplayName("Selection State Management")
    inner class SelectionStateTests {

        @Test
        fun `should clear all selections`() {
            selectionService.handleSingleClick(0, "photo1")
            selectionService.handleCtrlClick(1, "photo2")
            selectionService.handleCtrlClick(2, "photo3")
            
            selectionService.clearSelection()
            
            assertTrue(selectionService.getSelectedPhotoIds().isEmpty())
            assertFalse(selectionService.isSelected("photo1"))
            assertFalse(selectionService.isSelected("photo2"))
            assertFalse(selectionService.isSelected("photo3"))
        }

        @Test
        fun `should return correct selection state`() {
            selectionService.handleSingleClick(0, "photo1")
            selectionService.handleCtrlClick(2, "photo3")
            
            val selection = selectionService.getSelection()
            
            assertEquals(2, selection.selectedPhotoIds.size)
            assertTrue(selection.isSelected("photo1"))
            assertTrue(selection.isSelected("photo3"))
            assertEquals(2, selection.anchorIndex)
        }

        @Test
        fun `should correctly report selected state`() {
            selectionService.handleSingleClick(0, "photo1")
            
            assertTrue(selectionService.isSelected("photo1"))
            assertFalse(selectionService.isSelected("photo2"))
        }

        @Test
        fun `should return all selected photo IDs`() {
            selectionService.handleSingleClick(0, "photo1")
            selectionService.handleCtrlClick(2, "photo3")
            selectionService.handleCtrlClick(5, "photo6")
            
            val selectedIds = selectionService.getSelectedPhotoIds()
            
            assertEquals(setOf("photo1", "photo3", "photo6"), selectedIds)
        }
    }

    @Nested
    @DisplayName("Complex Selection Scenarios")
    inner class ComplexScenarioTests {

        @Test
        fun `should handle mixed selection operations`() {
            // Single select
            selectionService.handleSingleClick(0, "photo1")
            assertTrue(selectionService.isSelected("photo1"))
            
            // Add with ctrl
            selectionService.handleCtrlClick(3, "photo4")
            assertTrue(selectionService.isSelected("photo1"))
            assertTrue(selectionService.isSelected("photo4"))
            
            // Single select clears previous
            selectionService.handleSingleClick(5, "photo6")
            assertFalse(selectionService.isSelected("photo1"))
            assertFalse(selectionService.isSelected("photo4"))
            assertTrue(selectionService.isSelected("photo6"))
        }

        @Test
        fun `should maintain anchor through ctrl clicks`() {
            selectionService.handleSingleClick(0, "photo1")
            assertEquals(0, selectionService.getSelection().anchorIndex)
            
            selectionService.handleCtrlClick(2, "photo3")
            assertEquals(2, selectionService.getSelection().anchorIndex)
            
            selectionService.handleCtrlClick(5, "photo6")
            assertEquals(5, selectionService.getSelection().anchorIndex)
        }

        @Test
        fun `should use latest anchor for shift click`() {
            // Set initial anchor at 1
            selectionService.handleSingleClick(1, "photo2")
            
            // Update anchor to 3 via ctrl click
            selectionService.handleCtrlClick(3, "photo4")
            
            // Shift click from new anchor (3) to 6
            val result = selectionService.handleShiftClick(6, 10, 3)
            
            // Should select from 3 to 6, not from 1 to 6
            assertTrue(result.selectedIndices.contains(3))
            assertTrue(result.selectedIndices.contains(6))
        }
    }
}
