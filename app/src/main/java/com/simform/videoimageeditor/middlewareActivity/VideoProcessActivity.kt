package com.simform.videoimageeditor.middlewareActivity

import android.view.View
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.databinding.ActivityVideoProcessBinding
import com.simform.videoimageeditor.videoProcessActivity.*

/**
 * Created by Ashvin Vavaliya on 29,December,2020
 * Simform Solutions Pvt Ltd.
 */
class VideoProcessActivity : BaseActivity(R.layout.activity_video_process, R.string.video_operations) {
    private lateinit var binding: ActivityVideoProcessBinding
    
    override fun initialization() {
        binding = ActivityVideoProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.title = getString(R.string.video_operations)
        binding.btnCutVideo.setOnClickListener(this)
        binding.btnImageToVideo.setOnClickListener(this)
        binding.btnAddWaterMarkOnVideo.setOnClickListener(this)
        binding.btnCombineImageVideo.setOnClickListener(this)
        binding.btnCombineImages.setOnClickListener(this)
        binding.btnCombineVideos.setOnClickListener(this)
        binding.btnCompressVideo.setOnClickListener(this)
        binding.btnExtractVideo.setOnClickListener(this)
        binding.btnExtractAudio.setOnClickListener(this)
        binding.btnMotion.setOnClickListener(this)
        binding.btnReverseVideo.setOnClickListener(this)
        binding.btnFadeInFadeOutVideo.setOnClickListener(this)
        binding.btnVideoConvertIntoGIF.setOnClickListener(this)
        binding.btnVideoRotateFlip.setOnClickListener(this)
        binding.btnMergeVideoAndAudio.setOnClickListener(this)
        binding.btnAddTextOnVideo.setOnClickListener(this)
        binding.btnRemoveAudioFromVideo.setOnClickListener(this)
        binding.btnMergeImageAndAudio.setOnClickListener(this)
        binding.btnSetAspectRatio.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnCutVideo -> {
                utils.openActivity(this, CutVideoUsingTimeActivity())
            }
            R.id.btnImageToVideo -> {
                utils.openActivity(this, ImageToVideoConvertActivity())
            }
            R.id.btnAddWaterMarkOnVideo -> {
                utils.openActivity(this, AddWaterMarkOnVideoActivity())
            }
            R.id.btnCombineImageVideo -> {
                utils.openActivity(this, CombineImageAndVideoActivity())
            }
            R.id.btnCombineImages -> {
                utils.openActivity(this, CombineImagesActivity())
            }
            R.id.btnCombineVideos -> {
                utils.openActivity(this, CombineVideosActivity())
            }
            R.id.btnCompressVideo -> {
                utils.openActivity(this, CompressVideoActivity())
            }
            R.id.btnExtractVideo -> {
                utils.openActivity(this, ExtractImagesActivity())
            }
            R.id.btnExtractAudio -> {
                utils.openActivity(this, ExtractAudioActivity())
            }
            R.id.btnMotion -> {
                utils.openActivity(this, FastAndSlowVideoMotionActivity())
            }
            R.id.btnReverseVideo -> {
                utils.openActivity(this, ReverseVideoActivity())
            }
            R.id.btnFadeInFadeOutVideo -> {
                utils.openActivity(this, VideoFadeInFadeOutActivity())
            }
            R.id.btnVideoConvertIntoGIF -> {
                utils.openActivity(this, VideoToGifActivity())
            }
            R.id.btnVideoRotateFlip -> {
                utils.openActivity(this, VideoRotateFlipActivity())
            }
            R.id.btnMergeVideoAndAudio -> {
                utils.openActivity(this, MergeAudioVideoActivity())
            }
            R.id.btnAddTextOnVideo -> {
                utils.openActivity(this, AddTextOnVideoActivity())
            }
            R.id.btnRemoveAudioFromVideo -> {
                utils.openActivity(this, RemoveAudioFromVideoActivity())
            }
            R.id.btnMergeImageAndAudio -> {
                utils.openActivity(this, MergeImageAndMP3Activity())
            }
            R.id.btnSetAspectRatio -> {
                utils.openActivity(this, AspectRatioActivity())
            }
        }
    }
}