package com.udacity

fun getFileNameFromUrl(url: String): String {
    return url.substring(url.lastIndexOf('/') + 1)
}