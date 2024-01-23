package cn.fkj233.ui.activity.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import cn.fkj233.miui.R
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.LayoutPair
import cn.fkj233.ui.activity.dp2px
import cn.fkj233.ui.activity.fragment.MIUIFragment

/**
 * @Author: Liuyi
 * @Date: 2023/05/09/2:43:43
 * @Description:
 */
class ImageTextWithSwitchV(
    private val imageV: ImageV,
    private val textV: TextV,
    private val switchV: SwitchV,
    private val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {

    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        imageV.notShowMargins(true)
        textV.notShowMargins(true)
        return LinearContainerV(LinearContainerV.HORIZONTAL, arrayOf(
            LayoutPair(
                imageV.create(context, callBacks),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
            ),
            LayoutPair(
                textV.create(context, callBacks),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    marginStart = dp2px(context, 14f)
                }
            ),
            LayoutPair(
                switchV.create(context, callBacks),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
            )
        ), layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dp2px(context, 15.75f), 0, dp2px(context, 15.75f))
        }).create(context, callBacks).also {
            dataBindingRecv?.setView(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onDraw(thiz: MIUIFragment, group: LinearLayout, view: View) {
        group.apply {
            addView(view)
            setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> if (switchV.switch.isEnabled) {
                        background = context.getDrawable(R.drawable.ic_main_down_bg)
                    }

                    MotionEvent.ACTION_UP -> if (switchV.switch.isEnabled) {
                        switchV.click()
                        thiz.callBacks?.let { it -> it() }
                        background = context.getDrawable(R.drawable.ic_main_bg)
                    }

                    else -> background = context.getDrawable(R.drawable.ic_main_bg)
                }
                true
            }
        }
    }
}