package hzhl.net.hlwork.base

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import hzhl.net.hlcall.activity.BaseActivity

abstract class KtBaseActivity : BaseActivity(){
    private var onCheck: ((Boolean) -> Unit?)? = null
    private val rPermission = arrayListOf<String>()

    /**
     * 申请权限
     */
    @RequiresApi(Build.VERSION_CODES.M)
    protected fun checkPermissions(permissions: List<String>, onCheck:(isGranted:Boolean)->Unit) {
        permissions.forEach {
            if(checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED){
                rPermission.add(it)
            }
        }
        if (rPermission.size >0){
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSON_REQUESTCODE)
            this.onCheck = onCheck
        }else{
            onCheck(true)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray) {
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED){
                onCheck?.invoke(false)
                return
            }
        }

        onCheck?.invoke(true)
    }

    /** banlap：bug：实体键 拨号键和 挂机键不操作 */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_CALL) {
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }
    /** banlap：bug：实体键 拨号键和 挂机键不操作 --end*/
}