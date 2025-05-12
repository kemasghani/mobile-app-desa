package com.alya.ecommerce_serang.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

object UriToFileConverter {
    private const val TAG = "UriToFileConverter"

    fun uriToFile(uri: Uri, context: Context): File? {
        return try {
            Log.d(TAG, "Converting URI to file: $uri")

            // Try to get original filename
            val fileName = getFileNameFromUri(uri, context) ?: "upload_${System.currentTimeMillis()}"
            val extension = getFileExtension(fileName) ?: ".jpg"

            // Create a temporary file in the cache directory with proper name
            val tempFile = File.createTempFile(
                "upload_${Random.nextInt(10000)}",
                extension,
                context.cacheDir
            )

            Log.d(TAG, "Created temp file: ${tempFile.absolutePath}")

            // Open the input stream and copy content
            var inputStream: InputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e(TAG, "Failed to open input stream for URI: $uri")
                    return null
                }

                // Copy content using a buffer
                val outputStream = FileOutputStream(tempFile)
                val buffer = ByteArray(4 * 1024) // 4 KB buffer
                var bytesRead: Int
                var totalBytesRead = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                }

                outputStream.flush()
                outputStream.close()

                Log.d(TAG, "Successfully copied $totalBytesRead bytes to file")
            } catch (e: Exception) {
                Log.e(TAG, "Error copying file data", e)
                return null
            } finally {
                inputStream?.close()
            }

            // Verify the file
            if (!tempFile.exists() || tempFile.length() == 0L) {
                Log.e(TAG, "Created file doesn't exist or is empty: ${tempFile.absolutePath}")
                return null
            }

            Log.d(TAG, "Successfully converted URI to file: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file", e)
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri, context: Context): String? {
        // Try the OpenableColumns query method first
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    val fileName = c.getString(nameIndex)
                    Log.d(TAG, "Retrieved filename from OpenableColumns: $fileName")
                    return fileName
                }
            }
        }

        // Try MediaStore method
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val fileName = c.getString(nameIndex)
                    Log.d(TAG, "Retrieved filename from MediaStore: $fileName")
                    return fileName
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting filename from MediaStore", e)
        }

        // Last resort: extract from URI path
        uri.path?.let { path ->
            val fileName = path.substring(path.lastIndexOf('/') + 1)
            Log.d(TAG, "Retrieved filename from URI path: $fileName")
            return fileName
        }

        return null
    }

    private fun getFileExtension(fileName: String): String? {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot >= 0) {
            fileName.substring(lastDot)
        } else {
            null
        }
    }

    fun getFilePathFromUri(uri: Uri, context: Context): String? {
        // For Media Gallery
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file path from URI", e)
        }

        // If the above method fails, try direct conversion
        return uri.path
    }
}