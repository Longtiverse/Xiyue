package com.xiyue.app.domain

interface PracticeLibraryRepository {
    fun getLibraryItems(): List<PracticeLibraryItem>
    fun searchLibraryItems(query: String, kind: PracticeKind? = null): List<PracticeLibraryItem>

    fun findLibraryItem(itemId: String): PracticeLibraryItem? =
        getLibraryItems().firstOrNull { it.id == itemId }
}