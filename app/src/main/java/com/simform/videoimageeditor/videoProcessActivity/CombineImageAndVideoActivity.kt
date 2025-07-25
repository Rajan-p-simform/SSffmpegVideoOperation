package com.simform.videoimageeditor.videoProcessActivity

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.databinding.ActivityMergeImageAndVideoBinding
import com.simform.videoimageeditor.utils.enableEdgeToEdge
import com.simform.videooperations.CallBackOfQuery
import com.simform.videooperations.Common
import com.simform.videooperations.FFmpegCallBack
import com.simform.videooperations.LogMessage
import com.simform.videooperations.Paths
import java.util.concurrent.CompletableFuture.runAsync

class CombineImageAndVideoActivity : BaseActivity(R.layout.activity_merge_image_and_video, R.string.merge_image_and_video) {
    private lateinit var binding: ActivityMergeImageAndVideoBinding
    private var isInputVideoSelected = false
    private var isWaterMarkImageSelected = false

    override fun initialization() {
        binding = ActivityMergeImageAndVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge(binding.toolbar.root)
        binding.toolbar.textTitle.text = getString(R.string.merge_image_and_video)
        binding.apply {
            btnVideoPath.setOnClickListener(this@CombineImageAndVideoActivity)
            btnImagePath.setOnClickListener(this@CombineImageAndVideoActivity)
            btnCombine.setOnClickListener(this@CombineImageAndVideoActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                } else {
                    // Fallback for devices below Android 14
                    Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
                }
            }
            R.id.btnImagePath -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    // Fallback for devices below Android 14
                    Common.selectFile(this, maxSelection = 1, isImageSelection = true, isAudioSelection = false)
                }
            }
            R.id.btnCombine -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    !isWaterMarkImageSelected -> {
                        Toast.makeText(this, getString(R.string.input_image_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(binding.edtSecond.text.toString().trim()) || binding.edtSecond.text.toString().trim().toInt() == 0 -> {
                        Toast.makeText(this, getString(R.string.please_enter_second), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        combineImageAndVideoProcess()
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun selectedFiles(mediaFiles: List<MediaFile>?, fileRequestCode: Int) {
        when (fileRequestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    binding.tvInputPathVideo.text =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Common.saveFileToTempAndGetPath(this, mediaFiles[0].uri)
                        } else {
                            mediaFiles[0].path
                        }
                    isInputVideoSelected = true
                    runAsync {
                        retriever = MediaMetadataRetriever()
                        retriever?.setDataSource(binding.tvInputPathVideo.text.toString())
                        val bit = retriever?.frameAtTime
                        width = bit?.width
                        height = bit?.height
                    }
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
            Common.IMAGE_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    binding.tvInputPathImage.text =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Common.saveFileToTempAndGetPath(this, mediaFiles[0].uri)
                        } else {
                            mediaFiles[0].path
                        }
                    isWaterMarkImageSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.image_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun combineImageAndVideoProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        val paths = ArrayList<Paths>()

        val videoPaths1 = Paths()
        videoPaths1.filePath = binding.tvInputPathImage.text.toString()
        videoPaths1.isImageFile = true

        val videoPaths2 = Paths()
        videoPaths2.filePath = binding.tvInputPathVideo.text.toString()
        videoPaths2.isImageFile = false

        paths.add(videoPaths1)
        paths.add(videoPaths2)

        val query = ffmpegQueryExtension.combineImagesAndVideos(paths, width, height, binding.edtSecond.text.toString(), outputPath)

        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                binding.tvOutputPath.text = logMessage.text
            }

            override fun success() {
                binding.tvOutputPath.text = String.format(getString(R.string.output_path), outputPath)
                processStop()
            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }
        })
    }

    private fun processStop() {
        binding.apply {
            btnVideoPath.isEnabled = true
            btnImagePath.isEnabled = true
            btnCombine.isEnabled = true
            mProgressView.root.visibility = View.GONE
        }
    }

    private fun processStart() {
        binding.apply {
            btnVideoPath.isEnabled = false
            btnImagePath.isEnabled = false
            btnCombine.isEnabled = false
            mProgressView.root.visibility = View.VISIBLE
        }
    }
}