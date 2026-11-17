package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.wlkmnDataStore: DataStore<Preferences> by preferencesDataStore(name = "wlkmn_settings")

class TapeSettingsRepository(private val context: Context) {

    private object Keys {
        val PRESET = stringPreferencesKey("tape_preset")
        val BAND_0 = intPreferencesKey("eq_band_0")
        val BAND_1 = intPreferencesKey("eq_band_1")
        val BAND_2 = intPreferencesKey("eq_band_2")
        val BAND_3 = intPreferencesKey("eq_band_3")
        val BAND_4 = intPreferencesKey("eq_band_4")
        val WARMTH = floatPreferencesKey("tape_warmth")
        val HISS = floatPreferencesKey("tape_hiss")
        val SPEED = floatPreferencesKey("tape_speed")
        val SHELL_STYLE = stringPreferencesKey("cassette_shell")
        val SHUFFLE = booleanPreferencesKey("is_shuffle")
        val REPEAT = stringPreferencesKey("repeat_mode")
        val SEEDED_STARTER = booleanPreferencesKey("seeded_starter_tape")
    }

    val settingsFlow: Flow<TapeSettings> = context.wlkmnDataStore.data.map { prefs ->
        val preset = prefs[Keys.PRESET]?.let { name ->
            TapeFilterPreset.entries.firstOrNull { it.name == name }
        } ?: TapeFilterPreset.TYPE_II_CHROME

        val defaultBands = preset.bandsMb
        val bands = listOf(
            prefs[Keys.BAND_0] ?: defaultBands[0],
            prefs[Keys.BAND_1] ?: defaultBands[1],
            prefs[Keys.BAND_2] ?: defaultBands[2],
            prefs[Keys.BAND_3] ?: defaultBands[3],
            prefs[Keys.BAND_4] ?: defaultBands[4]
        )

        val shellStyle = prefs[Keys.SHELL_STYLE]?.let { name ->
            CassetteShellStyle.entries.firstOrNull { it.name == name }
        } ?: CassetteShellStyle.CLASSIC_BLACK

        val repeatMode = prefs[Keys.REPEAT]?.let { name ->
            RepeatMode.entries.firstOrNull { it.name == name }
        } ?: RepeatMode.ALL

        TapeSettings(
            preset = preset,
            eqBandsMb = bands,
            tapeWarmth = prefs[Keys.WARMTH] ?: preset.defaultWarmth,
            tapeHiss = prefs[Keys.HISS] ?: preset.defaultHiss,
            tapeSpeed = prefs[Keys.SPEED] ?: 1.0f,
            shellStyle = shellStyle,
            isShuffle = prefs[Keys.SHUFFLE] ?: false,
            repeatMode = repeatMode
        )
    }

    suspend fun hasSeededStarterTape(): Boolean {
        return context.wlkmnDataStore.data.first()[Keys.SEEDED_STARTER] ?: false
    }

    suspend fun markStarterTapeSeeded() {
        context.wlkmnDataStore.edit { it[Keys.SEEDED_STARTER] = true }
    }

    suspend fun selectPreset(preset: TapeFilterPreset) {
        context.wlkmnDataStore.edit { prefs ->
            prefs[Keys.PRESET] = preset.name
            if (preset != TapeFilterPreset.CUSTOM) {
                prefs[Keys.BAND_0] = preset.bandsMb[0]
                prefs[Keys.BAND_1] = preset.bandsMb[1]
                prefs[Keys.BAND_2] = preset.bandsMb[2]
                prefs[Keys.BAND_3] = preset.bandsMb[3]
                prefs[Keys.BAND_4] = preset.bandsMb[4]
                prefs[Keys.WARMTH] = preset.defaultWarmth
                prefs[Keys.HISS] = preset.defaultHiss
            }
        }
    }

    suspend fun updateEqBand(bandIndex: Int, levelMb: Int) {
        val bandKey = when (bandIndex) {
            0 -> Keys.BAND_0
            1 -> Keys.BAND_1
            2 -> Keys.BAND_2
            3 -> Keys.BAND_3
            4 -> Keys.BAND_4
            else -> return
        }
        context.wlkmnDataStore.edit { prefs ->
            prefs[Keys.PRESET] = TapeFilterPreset.CUSTOM.name
            prefs[bandKey] = levelMb
        }
    }

    suspend fun updateWarmth(warmth: Float) {
        context.wlkmnDataStore.edit { it[Keys.WARMTH] = warmth.coerceIn(0f, 1f) }
    }

    suspend fun updateHiss(hiss: Float) {
        context.wlkmnDataStore.edit { it[Keys.HISS] = hiss.coerceIn(0f, 1f) }
    }

    suspend fun updateSpeed(speed: Float) {
        context.wlkmnDataStore.edit { it[Keys.SPEED] = speed.coerceIn(0.85f, 1.15f) }
    }

    suspend fun updateShellStyle(style: CassetteShellStyle) {
        context.wlkmnDataStore.edit { it[Keys.SHELL_STYLE] = style.name }
    }

    suspend fun toggleShuffle() {
        context.wlkmnDataStore.edit { prefs ->
            val current = prefs[Keys.SHUFFLE] ?: false
            prefs[Keys.SHUFFLE] = !current
        }
    }

    suspend fun cycleRepeatMode() {
        context.wlkmnDataStore.edit { prefs ->
            val current = prefs[Keys.REPEAT]?.let { name ->
                RepeatMode.entries.firstOrNull { it.name == name }
            } ?: RepeatMode.ALL
            val next = when (current) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            prefs[Keys.REPEAT] = next.name
        }
    }
}
