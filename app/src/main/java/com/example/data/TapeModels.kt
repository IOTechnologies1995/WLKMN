package com.example.data

enum class TapeFilterPreset(
    val displayName: String,
    val badge: String,
    val description: String,
    val bandsMb: List<Int>,
    val defaultWarmth: Float,
    val defaultHiss: Float
) {
    OFF(
        displayName = "Clean Digital",
        badge = "FLAT",
        description = "Bypass tape coloration for neutral playback",
        bandsMb = listOf(0, 0, 0, 0, 0),
        defaultWarmth = 0f,
        defaultHiss = 0f
    ),
    TYPE_I_FERRIC(
        displayName = "Type I • Ferric",
        badge = "FE-I",
        description = "Warm low-mid body and gentle ferric high-end roll-off",
        bandsMb = listOf(250, 350, 100, -250, -650),
        defaultWarmth = 0.35f,
        defaultHiss = 0.14f
    ),
    TYPE_II_CHROME(
        displayName = "Type II • Chrome",
        badge = "CrO2",
        description = "Balanced Walkman CrO2 clarity with smooth analog highs",
        bandsMb = listOf(150, 200, 50, 150, -350),
        defaultWarmth = 0.25f,
        defaultHiss = 0.08f
    ),
    TYPE_IV_METAL(
        displayName = "Type IV • Metal",
        badge = "MTL",
        description = "High-bias punch with tight sub-bass and crisp detail",
        bandsMb = listOf(350, 150, -50, 250, -150),
        defaultWarmth = 0.45f,
        defaultHiss = 0.05f
    ),
    WORN_CASSETTE(
        displayName = "Worn Cassette",
        badge = "LO-FI",
        description = "Aged magnetic tape boxiness with steep treble attenuation",
        bandsMb = listOf(-200, 400, 300, -450, -1000),
        defaultWarmth = 0.50f,
        defaultHiss = 0.28f
    ),
    CUSTOM(
        displayName = "Custom EQ",
        badge = "USER",
        description = "Manual 5-band equalizer calibration",
        bandsMb = listOf(0, 0, 0, 0, 0),
        defaultWarmth = 0.20f,
        defaultHiss = 0.05f
    )
}

enum class CassetteShellStyle(
    val displayName: String,
    val tapeSpec: String
) {
    CLASSIC_BLACK("Classic Black", "C-90 NORMAL"),
    NAVY_WALKMAN("Navy Edition", "WLK-90 BIAS"),
    SILVER_CHROME("Silver Chrome", "CrO2-90"),
    RED_STUDIO("Studio Red", "PRO-60 IV")
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class LibraryViewMode {
    SONGS,
    ALBUMS
}

data class TapeSettings(
    val preset: TapeFilterPreset = TapeFilterPreset.TYPE_II_CHROME,
    val eqBandsMb: List<Int> = TapeFilterPreset.TYPE_II_CHROME.bandsMb,
    val tapeWarmth: Float = TapeFilterPreset.TYPE_II_CHROME.defaultWarmth,
    val tapeHiss: Float = TapeFilterPreset.TYPE_II_CHROME.defaultHiss,
    val tapeSpeed: Float = 1.0f,
    val shellStyle: CassetteShellStyle = CassetteShellStyle.CLASSIC_BLACK,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.ALL
)

data class AlbumGroup(
    val albumName: String,
    val artistSummary: String,
    val tracks: List<TrackEntity>
)

fun formatDurationMs(ms: Long): String {
    val totalSec = (ms.coerceAtLeast(0L) / 1000L)
    val min = totalSec / 60L
    val sec = totalSec % 60L
    return "%02d:%02d".format(min, sec)
}
