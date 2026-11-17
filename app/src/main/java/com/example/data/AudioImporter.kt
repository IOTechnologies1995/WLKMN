package com.example.data

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AudioImporter {

    private val audioExtensions = setOf(
        "mp3", "wav", "flac", "m4a", "aac", "ogg", "opus", "wma", "aiff"
    )

    suspend fun importAudioUris(
        context: Context,
        uris: List<Uri>
    ): List<TrackEntity> = withContext(Dispatchers.IO) {
        uris.mapNotNull { uri ->
            takePersistablePermissionSafe(context, uri)
            extractTrackEntity(context, uri)
        }
    }

    suspend fun importAudioTree(
        context: Context,
        treeUri: Uri
    ): List<TrackEntity> = withContext(Dispatchers.IO) {
        takePersistablePermissionSafe(context, treeUri)
        val rootDocId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: return@withContext emptyList()
        val foundUris = mutableListOf<Uri>()
        collectAudioFromTree(context, treeUri, rootDocId, foundUris, depth = 0)
        foundUris.mapNotNull { extractTrackEntity(context, it) }
    }

    private fun collectAudioFromTree(
        context: Context,
        treeUri: Uri,
        parentDocId: String,
        out: MutableList<Uri>,
        depth: Int
    ) {
        if (depth > 4 || out.size >= 250) return
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                while (cursor.moveToNext()) {
                    val docId = if (idIdx >= 0) cursor.getString(idIdx) else continue
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty() else ""
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx).orEmpty() else ""
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        collectAudioFromTree(context, treeUri, docId, out, depth + 1)
                    } else if (isAudioFile(mime, name)) {
                        out.add(DocumentsContract.buildDocumentUriUsingTree(treeUri, docId))
                    }
                }
            }
        }
    }

    private fun isAudioFile(mime: String, name: String): Boolean {
        if (mime.startsWith("audio/")) return true
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in audioExtensions
    }

    private fun takePersistablePermissionSafe(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun extractTrackEntity(context: Context, uri: Uri): TrackEntity? {
        val displayName = queryDisplayName(context, uri)
            ?: uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')
            ?: "Unknown Track"
        val fallbackTitle = displayName.substringBeforeLast('.').ifBlank { displayName }

        val retriever = MediaMetadataRetriever()
        var title = fallbackTitle
        var artist = "Unknown Artist"
        var album = "Mixtape Singles"
        var durationMs = 0L
        var trackNumber = 0

        try {
            retriever.setDataSource(context, uri)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.trim()?.takeIf { it.isNotEmpty() } ?: fallbackTitle
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.trim()?.takeIf { it.isNotEmpty() }
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                    ?.trim()?.takeIf { it.isNotEmpty() }
                ?: "Unknown Artist"
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.trim()?.takeIf { it.isNotEmpty() } ?: "Mixtape Singles"
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val rawTrackNum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            trackNumber = rawTrackNum?.substringBefore('/')?.trim()?.toIntOrNull() ?: 0
        } catch (_: Exception) {
            // Keep filename fallback if metadata extraction is unsupported for this container
        } finally {
            runCatching { retriever.release() }
        }

        return TrackEntity(
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            uriString = uri.toString(),
            rawResName = null,
            trackNumber = trackNumber,
            isDemo = false
        )
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        }.getOrNull()
    }
}
