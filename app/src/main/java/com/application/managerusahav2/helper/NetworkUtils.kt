package com.application.managerusahav2.helper


/**
 * Utility functions untuk validasi network
 */
object NetworkUtils {

    /**
     * Validasi format base URL
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "http://$url"
            } else {
                url
            }

            java.net.URL(formattedUrl)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Format URL menjadi format yang benar
     */
    fun formatBaseUrl(url: String): String {
        var formattedUrl = url.trim()

        // Tambahkan http:// jika tidak ada protocol
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "http://$formattedUrl"
        }

        // Tambahkan /api/ jika belum ada
        if (!formattedUrl.endsWith("/api/")) {
            formattedUrl = if (formattedUrl.endsWith("/")) {
                "${formattedUrl}api/"
            } else {
                "$formattedUrl/api/"
            }
        }

        return formattedUrl
    }
}