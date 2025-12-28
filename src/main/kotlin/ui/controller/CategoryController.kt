package ui.controller

import domain.model.Category
import domain.service.CategoryService
import domain.service.PhotoService
import javafx.scene.layout.TilePane
import ui.component.CategoryCard
import ui.handler.DragDropHandler

class CategoryController(
    private val categoryService: CategoryService,
    private val photoService: PhotoService,
    private val categoryContainer: TilePane,
    private val dragDropHandler: DragDropHandler,
    private val onCategorySelected: (Category?) -> Unit,
    private val onCategoryDeleted: (Category) -> Unit
) {
    private val categoryCardMap = mutableMapOf<String, CategoryCard>()
    private var selectedCategory: Category? = null

    fun addCategory() {
        val category = categoryService.createCategory()

        lateinit var categoryCard: CategoryCard

        categoryCard = CategoryCard(
            category = category,
            onDeleteRequested = { deleteCategory(category) },
            onSelectionChanged = { isSelected -> handleCategorySelection(category, categoryCard, isSelected) }
        )

        categoryCardMap[category.id] = categoryCard
        setupCategoryCardDragHandlers(categoryCard, category)
        categoryContainer.children.add(categoryCard)
    }

    fun deleteCategory(category: Category) {
        if (selectedCategory?.id == category.id) {
            selectedCategory = null
        }

        categoryCardMap.remove(category.id)

        categoryContainer.children.removeIf {
            it is CategoryCard && it.getCategory().id == category.id
        }

        val returnedPhotos = categoryService.removeCategoryAndReturnPhotos(category.id)
        if (returnedPhotos.isNotEmpty()) {
            photoService.restorePhotos(returnedPhotos)
        }

        onCategoryDeleted(category)
    }

    fun getSelectedCategory(): Category? = selectedCategory

    fun clearSelection() {
        selectedCategory?.let { prevCategory ->
            categoryCardMap[prevCategory.id]?.setSelected(false)
        }
        selectedCategory = null
    }

    fun getCategoryCard(categoryId: String): CategoryCard? = categoryCardMap[categoryId]

    fun updateCategoryCard(categoryId: String) {
        val updatedCategory = categoryService.getCategoryById(categoryId)
        if (updatedCategory != null) {
            categoryCardMap[categoryId]?.updateCategory(updatedCategory)
        }
    }

    private fun handleCategorySelection(category: Category, categoryCard: CategoryCard, isSelected: Boolean) {
        if (isSelected) {
            selectedCategory?.let { prevCategory ->
                categoryCardMap[prevCategory.id]?.setSelected(false)
            }
            selectedCategory = categoryService.getCategoryById(category.id)
            onCategorySelected(selectedCategory)
        } else {
            selectedCategory = null
            onCategorySelected(null)
        }
    }

    private fun setupCategoryCardDragHandlers(categoryCard: CategoryCard, category: Category) {
        categoryCard.setOnDragOver { event -> dragDropHandler.handleDragOver(event) }

        categoryCard.setOnDragEntered { event ->
            if (dragDropHandler.getDraggedImageView() != null && event.dragboard.hasString()) {
                categoryCard.setDragOver(true)
            }
            event.consume()
        }

        categoryCard.setOnDragExited { event ->
            categoryCard.setDragOver(false)
            event.consume()
        }

        categoryCard.setOnDragDropped { event ->
            val currentCategory = categoryService.getCategoryById(category.id) ?: category
            val success = dragDropHandler.handleDragDropped(event, currentCategory, categoryCard.getPhotoContainer())
            if (success) {
                val updatedCategory = categoryService.getCategoryById(category.id)
                if (updatedCategory != null) {
                    categoryCard.updateCategory(updatedCategory)
                }
                if (selectedCategory?.id == category.id) {
                    selectedCategory = updatedCategory
                    onCategorySelected(selectedCategory)
                }
            }
            categoryCard.setDragOver(false)
            event.isDropCompleted = success
            event.consume()
        }
    }
}
