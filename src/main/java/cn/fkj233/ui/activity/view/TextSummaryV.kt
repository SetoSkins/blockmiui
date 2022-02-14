/*
 * BlockMIUI
 * Copyright (C) 2022 fkj@fkj233.cn
 * https://github.com/577fkj/BlockMIUI
 *
 * This software is free opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by 577fkj.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/577fkj/BlockMIUI/blob/main/LICENSE>.
 */

package cn.fkj233.ui.activity.view

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.fkj233.miui.R
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.LayoutPair
import cn.fkj233.ui.activity.dp2px

class TextSummaryV(private val text: String? = null, private val textId: Int? = null, private val tips: String? = null, private val textColor: Int? = R.color.whiteText, private val tipsId: Int? = null, private val showArrow: Boolean = true, private val dataBindingRecv: DataBinding.Binding.Recv? = null, val onClickListener: (() -> Unit)? = null): BaseView() {

    override fun getType(): BaseView {
        return this
    }

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return LinearContainerV(LinearContainerV.HORIZONTAL, arrayOf(
            LayoutPair(
                LinearContainerV(LinearContainerV.VERTICAL, arrayOf(
                    LayoutPair(
                        TextView(context).also { view ->
                            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (text == null && textId == null) 15f else 18f)
                            view.setTextColor(context.getColor(textColor!!))
                            text?.let { it1 -> view.text = it1 }
                            textId?.let { it1 -> view.setText(it1) }
                            view.paint.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        },
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                    ),
                    LayoutPair(
                        TextView(context).also {
                            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                            it.setTextColor(context.getColor(R.color.author_tips))
                            if (tips == null && tipsId == null) {
                                it.visibility = View.GONE
                            } else {
                                tips?.let { it1 -> it.text = it1 }
                                tipsId?.let { it1 -> it.setText(it1) }
                            }
                            it.paint.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        },
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                    )
                )).create(context, callBacks),
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            ),
            LayoutPair(
                ImageView(context).also {
                    it.background = context.getDrawable(R.drawable.ic_right_arrow)
                    it.visibility = if (showArrow) View.VISIBLE else View.GONE
                },
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.gravity = Gravity.CENTER_VERTICAL }
            )
        ), layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).also {
            it.setMargins(0, dp2px(context, 15f),0, dp2px(context, 15f))
        }).create(context, callBacks).also {
            dataBindingRecv?.setView(it)
        }
    }
}