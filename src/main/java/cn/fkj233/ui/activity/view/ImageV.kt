package cn.fkj233.ui.activity.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.LayoutPair
import cn.fkj233.ui.activity.dp2px
import cn.fkj233.ui.activity.fragment.MIUIFragment

/**
 * @Author: Liuyi
 * @Date: 2023/05/08/23:33:50
 * @Description:
 */
class ImageV(
    private val image: Drawable,
    private var size: Float = 60f,
    private var width: Float = 60f,
    private var height: Float = 60f,
    private val position: Int = POSITION_LEFT,
    private val round: Float = 0f,
    private val dataBindingRecv: DataBinding.Binding.Recv? = null,
    val onClickListener: (() -> Unit)? = null
) : BaseView {
    private var notShowMargins = false

    companion object {
        const val POSITION_LEFT = Gravity.LEFT
        const val POSITION_CENTER = Gravity.CENTER
        const val POSITION_RIGHT = Gravity.RIGHT
    }

    override fun getType(): BaseView {
        return this
    }

    fun notShowMargins(boolean: Boolean) {
        notShowMargins = boolean
    }

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        val imageSize = dp2px(context, size).toInt()
        val params = if (width > 0f && height > 0f) {
            LinearLayout.LayoutParams(
                dp2px(context, width).toInt(),
                dp2px(context, height).toInt()
            )
        } else {
            LinearLayout.LayoutParams(imageSize, imageSize)
        }

        return LinearContainerV(
            LinearContainerV.HORIZONTAL, arrayOf(
                LayoutPair(
                    RoundCornerImageView(context, dp2px(context, round), dp2px(context, round))
                        .apply {
                            setPadding(0, dp2px(context, 10f), 0, dp2px(context, 10f))
                            background = image
                        },
                    params
                )
            ), layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (notShowMargins) setMargins(0, dp2px(context, 15f), 0, dp2px(context, 15f))
            }
        ).create(context, callBacks).also {
            dataBindingRecv?.setView(it)
            (it as? LinearLayout)?.gravity = position
        }
    }

    override fun onDraw(thiz: MIUIFragment, group: LinearLayout, view: View) {
        thiz.apply {
            group.apply {
                addView(view)
                onClickListener?.let { unit ->
                    setOnClickListener {
                        unit()
                        callBacks?.let { block -> block() }
                    }
                }
            }
        }
    }
}