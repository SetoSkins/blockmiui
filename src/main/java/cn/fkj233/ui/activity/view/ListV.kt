package cn.fkj233.ui.activity.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import cn.fkj233.ui.activity.dp2px
import cn.fkj233.ui.activity.fragment.MIUIFragment

/**
 * @Author: Liuyi
 * @Date: 2023/05/09/5:54:43
 * @Description:
 */
class ListV(private val block: (ListView.() -> Unit)? = null) : BaseView {

    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return object : ListView(context) {
            override fun onViewAdded(child: View?) {
                // 给子Item设置各自的 Padding
                child?.setPadding(dp2px(context, 30f), 0, dp2px(context, 30f), 0)
            }
        }.apply {
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            isVerticalScrollBarEnabled = false
            divider = null
            block?.let { it(this) }
        }
    }

    override fun onDraw(thiz: MIUIFragment, group: LinearLayout, view: View) {
        group.apply {
            addView(view)
            // 去掉父控件设置的 Padding，ListView 应该使用自己的 Padding
            setPadding(0, 0, 0, 0)
        }
    }
}