import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtil {
    private const val PREFS_NAME = "MyPrefsFile"
    private const val AUDIO_FILE_PATHS = "audioFilePaths"

    fun saveAudioFilePaths(context: Context, filePaths: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putStringSet(AUDIO_FILE_PATHS, filePaths.toSet())
        editor.apply()
    }

    fun loadAudioFilePaths(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(AUDIO_FILE_PATHS, HashSet()) ?: HashSet()
        return set.toList()
    }
}
