import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tunahanakdere.seskaydetme.R
import java.io.File

class AudioAdapter(
    var audioList: MutableList<String>,
    private val playListener: (String) -> Unit,
    private val renameListener: (String) -> Unit,
    private val deleteListener: (String) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var currentlyPlayingPosition: Int? = null

    class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileNameTextView: TextView = view.findViewById(R.id.fileNameTextView)
        val playButton: Button = view.findViewById(R.id.playButton)
        val renameButton: Button = view.findViewById(R.id.renameButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val filePath = audioList[position]
        holder.fileNameTextView.text = File(filePath).name

        // Update the text of the play button based on the currently playing position
        holder.playButton.text = if (currentlyPlayingPosition == position) {
            "Stop Playing"
        } else {
            "Play"
        }

        holder.playButton.setOnClickListener {
            // If the item is currently playing, stop it; otherwise, start playing
            if (currentlyPlayingPosition == position) {
                // Stop playing
                playListener(filePath)
            } else {
                // Start playing
                playListener(filePath)
            }
        }

        holder.renameButton.setOnClickListener {
            renameListener(filePath)
        }

        holder.deleteButton.setOnClickListener {
            deleteListener(filePath)
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    fun updateAudioList(newAudioList: List<String>) {
        audioList = newAudioList.toMutableList()
        notifyDataSetChanged()
    }

    fun updatePlayButtonText(position: Int, isPlaying: Boolean) {
        currentlyPlayingPosition = if (isPlaying) position else null
        notifyDataSetChanged()
    }
}
