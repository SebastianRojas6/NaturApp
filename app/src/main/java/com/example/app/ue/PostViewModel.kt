package com.example.app.ue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.Post
import com.example.app.data.PostApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val api = PostApiService.create()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var currentPage = 1
    private var canLoadMore = true

    init {
        loadPosts()
    }

    fun loadPosts() {
        if (_isLoading.value || !canLoadMore) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = api.getPosts(currentPage)
                if (result.isEmpty()) canLoadMore = false
                else {
                    _posts.value = _posts.value + result
                    currentPage++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}