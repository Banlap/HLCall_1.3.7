package hzhl.net.hlwork.ui.work

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.MyLocationData
import hzhl.net.hlcall.R
import hzhl.net.hlwork.base.BaseAdapter
import hzhl.net.hlwork.base.KtBaseActivity
import hzhl.net.hlwork.untils.location
import kotlinx.android.synthetic.main.activity_work_tab.*


class WorkTabActivity : KtBaseActivity() {
    private var locationON : BDLocation? = null

    override fun getTopTitle() = "工作台"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun init() {
        checkPermissions(listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )){
            if (it)initMap()
            else showToast("未授权,无法使用定位功能")
        }


        rec_tab_menu.apply {
            layoutManager = LinearLayoutManager(this@WorkTabActivity)

        }




    }



    private fun initMap(){
        val myLocationListener = object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation?) { //mapView 销毁后不在处理新接收的位置
                location?.let {
                    val locData = MyLocationData.Builder()
                            .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(location.direction)
                            .latitude(location.latitude)
                            .longitude(location.longitude).build()

                    if (locationON==null) {
                        map_view.map.location(location)
                    }
                    locationON = location
                    map_view.map.setMyLocationData(locData)
                }

            }
        }

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when(event){
                    Lifecycle.Event.ON_PAUSE -> map_view.onPause()
                    Lifecycle.Event.ON_RESUME -> map_view.onResume()
                    Lifecycle.Event.ON_DESTROY -> {
                        map_view.map.isMyLocationEnabled =false
                        map_view.onDestroy()
                    }
                    else -> {}
                }
            }
        })
        map_view.map.isMyLocationEnabled = true
        map_view.showZoomControls(false)

        //定位初始化
        val option = LocationClientOption().apply {
            isOpenGps = true // 打开gps
            setCoorType("bd09ll") // 设置坐标类型
            setScanSpan(1000)
        }
        val mLocationClient = LocationClient(this).apply {
            //通过LocationClientOption设置LocationClient相关参数
            //设置locationClientOption
            locOption = option
            //注册LocationListener监听器
            registerLocationListener(myLocationListener)
            //开启地图定位图层
            start()
        }


        text_my_location.setOnClickListener {
            map_view.map.location(locationON)
        }

    }

    override fun getLayoutResID() = R.layout.activity_work_tab


}
