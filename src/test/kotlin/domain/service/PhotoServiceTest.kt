package domain.service

import domain.model.Photo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths

@DisplayName("PhotoService Tests")
class PhotoServiceTest {

    private lateinit var photoService: PhotoService
    private lateinit var photo1: Photo
    private lateinit var photo2: Photo
    private lateinit var photo3: Photo
    private lateinit var photoList: List<Photo>

    @BeforeEach
    fun setUp() {
        photoService = PhotoService()
        photo1 = Photo.fromPath(Paths.get("/test/photo1.jpg"), 0)
        photo2 = Photo.fromPath(Paths.get("/test/photo2.jpg"), 1)
        photo3 = Photo.fromPath(Paths.get("/test/photo3.jpg"), 2)
        photoList = listOf(photo1, photo2, photo3)
    }

    @Nested
    @DisplayName("Photo Collection Management")
    inner class PhotoCollectionTests {

        @Test
        fun `should set photo collection`() {
            photoService.setPhotos(photoList)

            assertEquals(3, photoService.getPhotoCount())
            assertEquals(photoList, photoService.getPhotos())
        }

        @Test
        fun `should replace existing photos when setting new collection`() {
            val initialPhotos = listOf(photo1, photo2)
            photoService.setPhotos(initialPhotos)
            
            val newPhotos = listOf(photo3)
            photoService.setPhotos(newPhotos)

            assertEquals(1, photoService.getPhotoCount())
            assertEquals(photo3, photoService.getPhotos()[0])
        }

        @Test
        fun `should clear photos`() {
            photoService.setPhotos(photoList)
            
            photoService.clearPhotos()

            assertEquals(0, photoService.getPhotoCount())
            assertTrue(photoService.getPhotos().isEmpty())
        }

        @Test
        fun `should maintain photo order`() {
            photoService.setPhotos(photoList)

            val retrievedPhotos = photoService.getPhotos()
            assertEquals(photo1, retrievedPhotos[0])
            assertEquals(photo2, retrievedPhotos[1])
            assertEquals(photo3, retrievedPhotos[2])
        }

        @Test
        fun `should return correct photo count`() {
            assertEquals(0, photoService.getPhotoCount())

            photoService.setPhotos(photoList)
            assertEquals(3, photoService.getPhotoCount())

            photoService.clearPhotos()
            assertEquals(0, photoService.getPhotoCount())
        }
    }

    @Nested
    @DisplayName("Photo Retrieval")
    inner class PhotoRetrievalTests {

        @BeforeEach
        fun setupPhotos() {
            photoService.setPhotos(photoList)
        }

        @Test
        fun `should get photo by index`() {
            assertEquals(photo1, photoService.getPhotoByIndex(0))
            assertEquals(photo2, photoService.getPhotoByIndex(1))
            assertEquals(photo3, photoService.getPhotoByIndex(2))
        }

        @Test
        fun `should return null for invalid index`() {
            assertNull(photoService.getPhotoByIndex(-1))
            assertNull(photoService.getPhotoByIndex(10))
        }

        @Test
        fun `should get photo by id`() {
            val foundPhoto = photoService.getPhotoById(photo2.id)

            assertNotNull(foundPhoto)
            assertEquals(photo2, foundPhoto)
        }

        @Test
        fun `should return null when photo id does not exist`() {
            val foundPhoto = photoService.getPhotoById("nonexistent_id")

            assertNull(foundPhoto)
        }

        @Test
        fun `should get index of photo by id`() {
            assertEquals(0, photoService.getIndexOfPhoto(photo1.id))
            assertEquals(1, photoService.getIndexOfPhoto(photo2.id))
            assertEquals(2, photoService.getIndexOfPhoto(photo3.id))
        }

        @Test
        fun `should return -1 for non-existent photo id`() {
            val index = photoService.getIndexOfPhoto("nonexistent_id")

            assertEquals(-1, index)
        }

        @Test
        fun `should get photos by indices`() {
            val indices = listOf(0, 2)
            val retrievedPhotos = photoService.getPhotosByIndices(indices)

            assertEquals(2, retrievedPhotos.size)
            assertEquals(photo1, retrievedPhotos[0])
            assertEquals(photo3, retrievedPhotos[1])
        }

        @Test
        fun `should handle invalid indices when getting photos by indices`() {
            val indices = listOf(0, 10, 2, -1)
            val retrievedPhotos = photoService.getPhotosByIndices(indices)

            // Should skip invalid indices
            assertEquals(2, retrievedPhotos.size)
            assertEquals(photo1, retrievedPhotos[0])
            assertEquals(photo3, retrievedPhotos[1])
        }

        @Test
        fun `should return empty list for all invalid indices`() {
            val indices = listOf(-1, 10, 20)
            val retrievedPhotos = photoService.getPhotosByIndices(indices)

            assertTrue(retrievedPhotos.isEmpty())
        }

        @Test
        fun `should get all photos`() {
            val allPhotos = photoService.getPhotos()

            assertEquals(3, allPhotos.size)
            assertEquals(photoList, allPhotos)
        }
    }

    @Nested
    @DisplayName("Photo Removal")
    inner class PhotoRemovalTests {

        @BeforeEach
        fun setupPhotos() {
            photoService.setPhotos(photoList)
        }

        @Test
        fun `should remove photo`() {
            photoService.removePhoto(photo2)

            assertEquals(2, photoService.getPhotoCount())
            assertFalse(photoService.getPhotos().contains(photo2))
            assertTrue(photoService.getPhotos().contains(photo1))
            assertTrue(photoService.getPhotos().contains(photo3))
        }

        @Test
        fun `should handle removing non-existent photo`() {
            val nonExistentPhoto = Photo.fromPath(Paths.get("/test/nonexistent.jpg"), 99)
            
            // Should not throw exception
            photoService.removePhoto(nonExistentPhoto)

            assertEquals(3, photoService.getPhotoCount())
        }

        @Test
        fun `should remove photos by ids`() {
            val idsToRemove = setOf(photo1.id, photo3.id)
            
            photoService.removePhotosByIds(idsToRemove)

            assertEquals(1, photoService.getPhotoCount())
            assertFalse(photoService.getPhotos().contains(photo1))
            assertTrue(photoService.getPhotos().contains(photo2))
            assertFalse(photoService.getPhotos().contains(photo3))
        }

        @Test
        fun `should handle removing non-existent photo ids`() {
            photoService.removePhotosByIds(setOf("nonexistent1", "nonexistent2"))

            assertEquals(3, photoService.getPhotoCount())
        }

        @Test
        fun `should handle mixed existing and non-existent ids`() {
            photoService.removePhotosByIds(setOf(photo1.id, "nonexistent"))

            assertEquals(2, photoService.getPhotoCount())
            assertFalse(photoService.getPhotos().contains(photo1))
            assertTrue(photoService.getPhotos().contains(photo2))
            assertTrue(photoService.getPhotos().contains(photo3))
        }

        @Test
        fun `should update indices after removal`() {
            photoService.removePhoto(photo1)

            // photo2 should now be at index 0
            assertEquals(photo2, photoService.getPhotoByIndex(0))
            // photo3 should now be at index 1
            assertEquals(photo3, photoService.getPhotoByIndex(1))
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        fun `should handle empty photo collection`() {
            assertEquals(0, photoService.getPhotoCount())
            assertTrue(photoService.getPhotos().isEmpty())
            assertNull(photoService.getPhotoByIndex(0))
            assertNull(photoService.getPhotoById("any_id"))
        }

        @Test
        fun `should handle setting empty photo list`() {
            photoService.setPhotos(photoList)
            photoService.setPhotos(emptyList())

            assertEquals(0, photoService.getPhotoCount())
            assertTrue(photoService.getPhotos().isEmpty())
        }

        @Test
        fun `should handle large photo collection`() {
            val largePhotoList = (0 until 1000).map { index ->
                Photo.fromPath(Paths.get("/test/photo$index.jpg"), index)
            }

            photoService.setPhotos(largePhotoList)

            assertEquals(1000, photoService.getPhotoCount())
            assertEquals(largePhotoList[0], photoService.getPhotoByIndex(0))
            assertEquals(largePhotoList[999], photoService.getPhotoByIndex(999))
        }

        @Test
        fun `should handle multiple removals`() {
            photoService.setPhotos(photoList)
            
            photoService.removePhoto(photo1)
            photoService.removePhoto(photo2)
            photoService.removePhoto(photo3)

            assertEquals(0, photoService.getPhotoCount())
            assertTrue(photoService.getPhotos().isEmpty())
        }

        @Test
        fun `should return immutable copy of photos`() {
            photoService.setPhotos(photoList)
            
            val photos1 = photoService.getPhotos()
            val photos2 = photoService.getPhotos()

            // Should be equal but not the same instance
            assertEquals(photos1, photos2)
            assertNotSame(photos1, photos2)
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    inner class ComplexScenarioTests {

        @Test
        fun `should handle sequential operations`() {
            // Set photos
            photoService.setPhotos(photoList)
            assertEquals(3, photoService.getPhotoCount())

            // Remove one
            photoService.removePhoto(photo2)
            assertEquals(2, photoService.getPhotoCount())

            // Add more photos
            val photo4 = Photo.fromPath(Paths.get("/test/photo4.jpg"), 3)
            val photo5 = Photo.fromPath(Paths.get("/test/photo5.jpg"), 4)
            photoService.setPhotos(photoService.getPhotos() + listOf(photo4, photo5))
            assertEquals(4, photoService.getPhotoCount())

            // Clear all
            photoService.clearPhotos()
            assertEquals(0, photoService.getPhotoCount())
        }

        @Test
        fun `should handle photo retrieval after modifications`() {
            photoService.setPhotos(photoList)
            
            // Get initial index of photo3
            val initialIndex = photoService.getIndexOfPhoto(photo3.id)
            assertEquals(2, initialIndex)

            // Remove photo1
            photoService.removePhoto(photo1)

            // Index of photo3 should change
            val newIndex = photoService.getIndexOfPhoto(photo3.id)
            assertEquals(1, newIndex)
        }

        @Test
        fun `should maintain integrity when removing multiple photos`() {
            photoService.setPhotos(photoList)
            
            photoService.removePhotosByIds(setOf(photo1.id, photo2.id))

            assertEquals(1, photoService.getPhotoCount())
            assertEquals(photo3, photoService.getPhotoByIndex(0))
            assertEquals(0, photoService.getIndexOfPhoto(photo3.id))
        }
    }

    @Nested
    @DisplayName("Photo Restoration")
    inner class PhotoRestorationTests {

        @Test
        fun `should restore photos in original index order`() {
            photoService.setPhotos(listOf(photo1, photo3))
            
            photoService.restorePhotos(listOf(photo2))

            assertEquals(3, photoService.getPhotoCount())
            assertEquals(photo1, photoService.getPhotoByIndex(0))
            assertEquals(photo2, photoService.getPhotoByIndex(1))
            assertEquals(photo3, photoService.getPhotoByIndex(2))
        }

        @Test
        fun `should restore multiple photos maintaining sequence`() {
            photoService.setPhotos(listOf(photo2))
            
            photoService.restorePhotos(listOf(photo3, photo1))

            assertEquals(3, photoService.getPhotoCount())
            assertEquals(photo1, photoService.getPhotoByIndex(0))
            assertEquals(photo2, photoService.getPhotoByIndex(1))
            assertEquals(photo3, photoService.getPhotoByIndex(2))
        }

        @Test
        fun `should handle restoring to empty collection`() {
            photoService.restorePhotos(listOf(photo2, photo1, photo3))

            assertEquals(3, photoService.getPhotoCount())
            assertEquals(photo1, photoService.getPhotoByIndex(0))
            assertEquals(photo2, photoService.getPhotoByIndex(1))
            assertEquals(photo3, photoService.getPhotoByIndex(2))
        }

        @Test
        fun `should handle empty restore list`() {
            photoService.setPhotos(photoList)
            
            photoService.restorePhotos(emptyList())

            assertEquals(3, photoService.getPhotoCount())
        }

        @Test
        fun `should restore photos with non-contiguous original indices`() {
            val photoA = Photo.fromPath(Paths.get("/test/photoA.jpg"), 0)
            val photoB = Photo.fromPath(Paths.get("/test/photoB.jpg"), 5)
            val photoC = Photo.fromPath(Paths.get("/test/photoC.jpg"), 10)
            
            photoService.setPhotos(listOf(photoA, photoC))
            
            photoService.restorePhotos(listOf(photoB))

            assertEquals(3, photoService.getPhotoCount())
            assertEquals(photoA, photoService.getPhotoByIndex(0))
            assertEquals(photoB, photoService.getPhotoByIndex(1))
            assertEquals(photoC, photoService.getPhotoByIndex(2))
        }
    }
}
