package cn.fkj233.ui.activity.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.LayoutPair
import cn.fkj233.ui.activity.dp2px
import cn.fkj233.ui.activity.fragment.MIUIFragment

/**
 * @Author: Liuyi
 * @Date: 2023/05/15/23:51:15
 * @Description:
 */
class EditTextWithSpinnerV(
    private val text: String = "",
    private val hint: String = "",
    private val isSingleLine: Boolean = true,
    private val editTextWeight: Float = 1f,
    private val spinnerV: SpinnerV,
    private val dataBindingRecv: DataBinding.Binding.Recv? = null,
    private val editCallBacks: ((String) -> Unit)? = null
) : BaseView {
    override fun getType(): BaseView = this

    lateinit var spinnerView: View

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        spinnerView = spinnerV.create(context, callBacks)
        return LinearContainerV(
            LinearContainerV.HORIZONTAL, arrayOf(
                LayoutPair(
                    MIUIEditText(context).apply {
                        this@EditTextWithSpinnerV.text.let { setText(it.toCharArray(), 0, it.length) }
                        this.hint = this@EditTextWithSpinnerV.hint
                        this.isSingleLine = this@EditTextWithSpinnerV.isSingleLine
                        visibility = View.VISIBLE
                        editCallBacks?.let {
                            addTextChangedListener(object : TextWatcher {
                                override fun afterTextChanged(var1: Editable?) {
                                    it(var1.toString())
                                }

                                override fun beforeTextChanged(var1: CharSequence?, var2: Int, var3: Int, var4: Int) {}
                                override fun onTextChanged(var1: CharSequence?, var2: Int, var3: Int, var4: Int) {}
                            })
                        }
                    },
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, editTextWeight
                    )
                ),
                LayoutPair(
                    spinnerView,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f
                    ).also {
                        it.gravity = Gravity.CENTER_VERTICAL
                    }
                )
            ),
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).also {
                it.setMargins(0, dp2px(context, 17.75f), 0, dp2px(context, 17.75f))
            }
        ).create(context, callBacks).also {
            dataBindingRecv?.setView(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onDraw(thiz: MIUIFragment, group: LinearLayout, view: View) {
        thiz.apply {
            group.apply {
                addView(view)
                spinnerView.isClickable = true
                spinnerView.setOnTouchListener { view, motionEvent ->
                    if (motionEvent.action == MotionEvent.ACTION_UP) {
                        val popup = MIUIPopup(context, view, spinnerV.currentValue, spinnerV.dropDownWidth, {
                            spinnerV.select.text = it
                            spinnerV.currentValue = it
                            callBacks?.let { it1 -> it1() }
                            spinnerV.dataBindingSend?.send(it)
                        }, SpinnerV.SpinnerData().apply(spinnerV.data).arrayList)
                        popup.apply {
                            horizontalOffset = dp2px(context, 24F)
                            setDropDownGravity(Gravity.LEFT)
                        }
                        popup.show()
                    }
                    false
                }
            }
        }
    }
}