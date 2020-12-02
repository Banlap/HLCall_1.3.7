package hzhl.net.hlwork.untils

import com.baidu.location.BDLocation
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.model.LatLng
import kotlinx.android.synthetic.main.activity_work_tab.*

fun BaiduMap.location(location:BDLocation?){
    location?.let {
        val builder = MapStatus.Builder()
                .apply {
                    zoom(18.0f)
                    target(LatLng(it.latitude,it.longitude))
                }
        setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))
    }
}