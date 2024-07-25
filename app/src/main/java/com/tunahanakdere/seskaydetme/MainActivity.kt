package com.tunahanakdere.seskaydetme

import AudioAdapter
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.tunahanakdere.seskaydetme.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioAdapter: AudioAdapter
    private val audioViewModel: AudioViewModel by viewModels()

    private var isRecording = false
    private var isPlaying = false
    private var currentRecordingFilePath: String? = null
    private var currentPlayingFilePath: String? = null

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupRecyclerView()
        checkPermissions()

        audioViewModel.audioList.observe(this, Observer { audioList ->
            audioAdapter.updateAudioList(audioList)
        })
    }

    private fun setupViews() {
        binding.recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        audioAdapter = AudioAdapter(mutableListOf(), { filePath ->
            if (isPlaying) {
                stopPlaying()
            } else {
                startPlaying(filePath)
            }
        }, { filePath ->
            showRenameDialog(filePath)
        }, { filePath ->
            showDeleteDialog(filePath)
        })
        binding.recyclerView.adapter = audioAdapter
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    private fun startRecording() {
        val fileName = "${getExternalFilesDir(null)?.absolutePath}/recording_${System.currentTimeMillis()}.3gp"
        currentRecordingFilePath = fileName
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(fileName)
            try {
                prepare()
                start()
                isRecording = true
                binding.recordButton.text = "Stop Recording"
                Toast.makeText(this@MainActivity, "Recording..", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Recording started: $fileName")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Error starting recording: ${e.message}")
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        binding.recordButton.text = "Start Recording"
        currentRecordingFilePath?.let {
            audioViewModel.addAudio(it)
            Log.d(TAG, "Recording Completed: $it")
        }
        Toast.makeText(this@MainActivity, "Recording Stopped", Toast.LENGTH_SHORT).show()
    }

    private fun startPlaying(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                currentPlayingFilePath = filePath
                this@MainActivity.isPlaying = true
                val position = audioAdapter.audioList.indexOf(filePath)
                audioAdapter.updatePlayButtonText(position, true)
                Toast.makeText(this@MainActivity, "Playing: $filePath", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Playback started: $filePath")
                setOnCompletionListener {
                    stopPlaying()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Playback error: ${e.message}")
            }
        }
    }

    private fun stopPlaying() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        currentPlayingFilePath = null
        isPlaying = false
        audioAdapter.updatePlayButtonText(-1, false)
        Toast.makeText(this@MainActivity, "Playback Stopped", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Playback Stopped")
    }

    private fun showRenameDialog(filePath: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename File")

        val input = EditText(this)
        input.setText(File(filePath).name)
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newFileName = input.text.toString()
            renameAudio(filePath, newFileName)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun renameAudio(filePath: String, newFileName: String) {
        val file = File(filePath)
        val newFile = File(file.parent, newFileName)

        if (file.renameTo(newFile)) {
            audioViewModel.removeAudio(filePath)
            audioViewModel.addAudio(newFile.absolutePath)
            Toast.makeText(this, "File name changed", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "File name changed: ${newFile.absolutePath}")
        } else {
            Toast.makeText(this, "File name could not be changed", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "File name could not be changed: $filePath")
        }
    }

    private fun deleteAudio(filePath: String) {
        val file = File(filePath)
        if (file.exists() && file.delete()) {
            audioViewModel.removeAudio(filePath)
            Toast.makeText(this, "File Removed", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "File Removed: $filePath")
        } else {
            Toast.makeText(this, "File Couldn't Be Removed", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "File Couldn't Be Removed: $filePath")
        }
    }

    private fun showDeleteDialog(filePath: String) {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle("Delete File")
        builder.setMessage("Are you sure that you want to delete this file?")
        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteAudio(filePath)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
    }
}
