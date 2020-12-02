package hzhl.net.hlwork.ui.setting

import androidx.recyclerview.widget.LinearLayoutManager
import hzhl.net.hlcall.LinphoneService
import hzhl.net.hlcall.R
import hzhl.net.hlwork.base.KtBaseActivity
import hzhl.net.hlwork.ui.view.PopBottomMenu
import kotlinx.android.synthetic.main.activity_video_setting.*
import org.linphone.core.Core

class VideoSettingActivity : KtBaseActivity() {
    override fun getTopTitle() = "视频设置"
    override fun getLayoutResID() = R.layout.activity_video_setting
    val bitrates = listOf(200,400,600,800,1000)
    val fps = listOf("default","custom","high-fps")
    val fps_int = arrayListOf<Int>().apply {
        repeat(6){
            add(it*5+5)
        }
    }
    override fun init() {
        val core:Core? = LinphoneService.getCore()
        rec_setting.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SettingAdapter().apply {
                core?.run {
                    setData(videoPayloadTypes.toList())
                }
            }
        }
        core?.run {

            //设置setNormalBitrate 默认36
            val bitrate = config.getInt("video", "codec_bitrate_limit", 600)
            tv_bitrate.text = bitrate.toString()
            ll_bitrate.setOnClickListener {
                PopBottomMenu(this@VideoSettingActivity).apply {
                    bitrates.forEach { bitrate ->
                        addMenu(bitrate.toString()){
                            tv_bitrate.text = it
                            config.setInt("video", "codec_bitrate_limit", bitrate)
                            videoPayloadTypes.forEach {payloadType->
                                payloadType.normalBitrate = bitrate
                            }
                        }
                    }
                    show()
                }

            }
            setFpsText(this)
            ll_fps.setOnClickListener {

                PopBottomMenu(this@VideoSettingActivity).apply {
                    fps.forEach { it ->
                        addMenu(it){

                            setVideoPreset(it,this@run)
                            setFpsText(this@run)
                    }
                    }
                    show()
                }

            }
        }

    }


    private fun setFpsText(core: Core) =
        core.apply {
            tv_fps.text =   if (videoPreset == fps[1]){
                preferredFramerate.toString()
            }else {
                videoPreset ?: fps[0]
            }
        }
    private fun setVideoPreset(preset: String?, core: Core) {
        var presetCopy = preset
        if (preset == "default") presetCopy = null
        core.videoPreset = presetCopy
        presetCopy = core.videoPreset
        if (presetCopy != "custom") {
            core.preferredFramerate = 0f
        }else{

            PopBottomMenu(this).apply {
                fps_int.forEach { it_int ->
                    addMenu(it_int.toString()){
                        core.preferredFramerate = it_int.toFloat()
                        setFpsText(core)
                    }
                }
                show()
            }

        }
    }



}
