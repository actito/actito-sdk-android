package com.actito.sample.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.listOf

class TagsViewModel : ViewModel() {
    private val _tags = MutableStateFlow<List<String>>(listOf())
    val tags: StateFlow<List<String>> = _tags

    init {
        fetchTags()
    }

    fun addTag(tag: String) {
        viewModelScope.launch {
            try {
                Actito.device().addTag(tag)
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to add tag.", e)
            }

            fetchTags()
        }
    }

    fun addTags(tags: List<String>) {
        viewModelScope.launch {
            try {
                Actito.device().addTags(tags)
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to add tags.", e)
            }

            fetchTags()
        }
    }

    fun removeTag(tag: String) {
        viewModelScope.launch {
            try {
                Actito.device().removeTag(tag)
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to remove tag.", e)
            }

            fetchTags()
        }
    }

    fun removeTags(tags: List<String>) {
        viewModelScope.launch {
            try {
                Actito.device().removeTags(tags)
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to remove tags.", e)
            }

            fetchTags()
        }
    }

    fun clearTags() {
        viewModelScope.launch {
            try {
                Actito.device().clearTags()
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to clear tags.", e)
            }

            fetchTags()
        }
    }

    private fun fetchTags() {
        viewModelScope.launch {
            try {
                val currentTags = Actito.device().fetchTags()
                _tags.value = currentTags
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to fetch tags.", e)
            }
        }
    }
}
