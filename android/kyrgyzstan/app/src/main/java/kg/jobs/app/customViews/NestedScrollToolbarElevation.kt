package kg.jobs.app.customViews

import android.os.Build
import com.google.android.material.appbar.AppBarLayout
import androidx.core.widget.NestedScrollView
import kg.jobs.app.toPx

class NestedScrollToolbarElevation(private val appbar: AppBarLayout) : NestedScrollView.OnScrollChangeListener {
    override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            if (scrollY > 5)
                appbar.elevation = 3.toPx.toFloat()
            else appbar.elevation = 0f
    }

}