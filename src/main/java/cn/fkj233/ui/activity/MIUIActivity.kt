/*
 * BlockMIUI
 * Copyright (C) 2022 fkj@fkj233.cn
 * https://github.com/577fkj/BlockMIUI
 *
 * This software is free opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License v2.1
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by 577fkj.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * GNU Lesser General Public License v2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License v2.1
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/577fkj/BlockMIUI/blob/main/LICENSE>.
 */

@file:Suppress("DEPRECATION")

package cn.fkj233.ui.activity

import android.animation.ObjectAnimator
import java.util.Random
import android.annotation.SuppressLint
import android.app.Activity
import android.app.FragmentManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import cn.fkj233.miui.R
import cn.fkj233.ui.activity.annotation.BMMainPage
import cn.fkj233.ui.activity.annotation.BMMenuPage
import cn.fkj233.ui.activity.annotation.BMPage
import cn.fkj233.ui.activity.data.AsyncInit
import cn.fkj233.ui.activity.data.BasePage
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.InitView
import cn.fkj233.ui.activity.data.SafeSharedPreferences
import cn.fkj233.ui.activity.fragment.MIUIFragment
import cn.fkj233.ui.activity.view.BaseView
import cn.fkj233.ui.activity.view.TitleTextV

/**
 * @version: V1.0
 * @author: 577fkj
 * @className: MIUIActivity
 * @packageName: MIUIActivity
 * @description: BaseActivity / 基本Activity
 * @data: 2022-02-05 18:30
 **/
@Keep
open class MIUIActivity : Activity() {

    private var callbacks: (() -> Unit)? = null

    private var thisName: ArrayList<String> = arrayListOf()

    private lateinit var viewData: InitView

    private val dataList: HashMap<String, InitView.ItemData> = hashMapOf()

    private lateinit var initViewData: InitView.() -> Unit

    companion object {

        var safeSP: SafeSharedPreferences = SafeSharedPreferences()

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        @SuppressLint("StaticFieldLeak")
        lateinit var activity: MIUIActivity
    }

    private val backButton by lazy {
        ImageView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
                it.gravity = Gravity.CENTER_VERTICAL
                if (isRtl(context))
                    it.setMargins(dp2px(activity, 5f), 0, 0, 0)
                else
                    it.setMargins(0, 0, dp2px(activity, 5f), 0)
            }
            background = getDrawable(R.drawable.abc_ic_ab_back_material)
            visibility = View.GONE
            setOnClickListener {
                this@MIUIActivity.onBackPressed()
            }
        }
    }

    private val menuButton by lazy {
        ImageView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.gravity = Gravity.CENTER_VERTICAL }
            background = getDrawable(R.drawable.abc_ic_menu_overflow_material)
            visibility = View.GONE
            if (isRtl(context))
                setPadding(dp2px(activity, 25f), 0, 0, 0)
            else
                setPadding(0, 0, dp2px(activity, 25f), 0)
            setOnClickListener {
                showFragment(if (this@MIUIActivity::initViewData.isInitialized) "Menu" else "__menu__")
            }
        }
    }

    private val titleView by lazy {

        TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also {
                it.gravity = Gravity.CENTER_VERTICAL
            }
            gravity = if (isRtl(context)) Gravity.RIGHT else Gravity.LEFT
            setTextColor(getColor(R.color.whiteText))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)

        }
    }

    private val titleText by lazy {
        TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also {
                it.gravity = Gravity.CENTER_VERTICAL
            }
            gravity = if (isRtl(context)) Gravity.RIGHT else Gravity.LEFT
            setTextColor(getColor(R.color.whiteText))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.5f)
            alpha = 0.5f // 设置透明度为 0.5（半透明）
        }
    }

    private fun TitleText(text: String? = null, textId: Int? = null,colorInt: Int? = null, colorId: Int? = null, dataBindingRecv: DataBinding.Binding.Recv? = null, onClickListener: (() -> Unit)? = null) {
        val itemList: ArrayList<BaseView> = arrayListOf()
        itemList.add(TitleTextV(text, textId,colorInt, colorId,dataBindingRecv, onClickListener))
    }

    private var frameLayoutId: Int = -1
    private val frameLayout by lazy {
        val mFrameLayout = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        frameLayoutId = View.generateViewId()
        mFrameLayout.id = frameLayoutId
        mFrameLayout
    }

    /**
     *  是否继续加载 / Continue loading
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var isLoad = true

    /**
     *  退出时是否保留后台 / Retaining the background
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var isExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // 假设您正在操作的是当前的 Activity 实例
        val yourActivity: Activity = this

////// 获取当前窗口的属性
//        val window: Window = yourActivity.window
//
////// 将窗口内容调整到适合屏幕的尺寸
//        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
//
////// 设置导航栏背景色为透明
//        window.navigationBarColor = Color.TRANSPARENT

        super.onCreate(savedInstanceState)

        context = this
        activity = this
        actionBar?.hide()
        setContentView(LinearLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            background = getDrawable(R.color.foreground)
            orientation = LinearLayout.VERTICAL
            addView(LinearLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setPadding(dp2px(activity, 25f), dp2px(activity, 20f), dp2px(activity, 25f), dp2px(activity, 15f))
                orientation = LinearLayout.HORIZONTAL
                addView(backButton)
                addView(titleView)
                addView(titleText)
                addView(menuButton)
            })
            addView(frameLayout)
        })
        if (savedInstanceState != null) {
            if (this::initViewData.isInitialized) {
                viewData = InitView(dataList).apply(initViewData)
                setMenuShow(viewData.isMenu)
                val list = savedInstanceState.getStringArrayList("this")!!
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                for (name: String in list) {
                    showFragment(name)
                }
                if (list.size == 1) {
                    setBackupShow(viewData.mainShowBack)
                }
                return
            }
            val list = savedInstanceState.getStringArrayList("this")!!
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            initAllPage()
            if (pageInfo.containsKey("__menu__")) setMenuShow(list.size == 1)
            for (name: String in list) {
                showFragment(name)
            }
        } else {
            if (isLoad) {
                if (this::initViewData.isInitialized) {
                    viewData = InitView(dataList).apply(initViewData)
                    setBackupShow(!viewData.mainShowBack)
                    setMenuShow(viewData.isMenu)
                    showFragment("Main")
                    return
                }
                initAllPage()
                showFragment("__main__")
            }
        }
        val showFragmentName = intent.getStringExtra("showFragment").toString()
        if (showFragmentName != "null" && showFragmentName.isNotEmpty()) {
            if (pageInfo.containsKey(showFragmentName)) {
                showFragment(showFragmentName)
                return
            }
        }

    }

    private val pageInfo: HashMap<String, BasePage> = hashMapOf()
    private val pageList: ArrayList<Class<out BasePage>> = arrayListOf()

    fun registerPage(basePage: Class<out BasePage>) {
        pageList.add(basePage)
    }

    fun initAllPage() {
        pageList.forEach { basePage ->
            if (basePage.getAnnotation(BMMainPage::class.java) != null) {
                val mainPage = basePage.newInstance()
                mainPage.activity = this
                pageInfo["__main__"] = mainPage
            } else if (basePage.getAnnotation(BMMenuPage::class.java) != null) {
                val menuPage = basePage.newInstance()
                menuPage.activity = this
                menuButton.visibility = View.VISIBLE
                pageInfo["__menu__"] = menuPage
            } else if (basePage.getAnnotation(BMPage::class.java) != null) {
                val menuPage = basePage.newInstance()
                menuPage.activity = this
                pageInfo[basePage.getAnnotation(BMPage::class.java)!!.key] = menuPage
            } else {
                throw Exception("Page must be annotated with BMMainPage or BMMenuPage or BMPage")
            }
        }

    }

    @Deprecated("This method is obsolete")
    fun initView(iView: InitView.() -> Unit) {
        initViewData = iView

    }
    fun animateTitleView(titleView: View, isBack: Boolean = false, backButtonTranslationX: Float = 50f, titleButtonTranslationX: Float = 50f) {
        // 创建 x 轴平移动画
        val translationXValue = if (isBack) 0f else backButtonTranslationX
        val translationX = ObjectAnimator.ofFloat(titleView, "translationX", translationXValue, 0f)
        translationX.duration = 350 // 设置动画时长
        translationX.interpolator = DecelerateInterpolator() // 设置动画插值器为减速插值器

        // 创建透明度渐变动画
        val alpha = ObjectAnimator.ofFloat(titleView, "alpha", 0f, 1f)
        alpha.duration = 350 // 设置动画时长
        alpha.interpolator = DecelerateInterpolator() // 设置动画插值器为减速插值器

        // 启动动画
        translationX.start()
        alpha.start()
    }

    override fun setTitle(title: CharSequence?) {
        titleView.text = title
//        val texts = mutableListOf("Tip：这么多功能里面，烟语觉得自己做的最完美的功能就是捐赠\uD83D\uDE0B", "Tip： 烟语开发的时候，曾因为乱加了一行代码导致状态栏上移，\n排查了一个下午的错误", "Tip： 这个App似乎有一个奇怪的页面？", "…", "Tip： Cemiuiler？真不熟", "Tip: 热知识：世界上功能最多的温控 Seto温控", "Tip: 烟语原本只是想做一个锁定均衡性能模式的功能，不知不觉中就开发了App", "Tip: 菜卡玩机是我们自己开发的论坛", "Tip: 关于模块里面的logo的颜色，是和知名软件粉色创可贴一样的颜色", "Tip： 冷知识：A12以下的系统\n因为没有莫奈取色,所以App的颜色是黑白的", "Tip: Seto出生在一个苹果发布了第一款手机的年代", "Tip: Sev到底是男生还是女生？", "Tip: Seto和烟语目前就读于山河大学三江市校区信息工程技术学院", "Tip: SetoSkins的名字来自于一个喜欢的作曲人和一张专辑", "Tip: Seto不精通C++、Kotlin、Shell\n这些都是烟语帮忙写的，Seto只负责写Tips", "Tip: 大\uD83D\uDC37\uD83D\uDC37是Mly，小\uD83D\uDC37\uD83D\uDC37是Shadow3", "Tip: Seto接触Java的契机，源于徕卡相机", "Tip: 想要尽快联系Seto？请去哔哩哔哩搜索SetoSKins并给他发私信！", "Tip: Seto有点强迫症，每个选项的上下布局都要纠结半天", "Tip: Seto曾经在Scene第一天更新Cpu温度的时候\n把Cpu温度干到150°以上,以至于让嘟嘟都发了动态", "Tip: 笨蛋Ray说他也要Tip，所以就有了这一条（）", "Tip: 笨蛋Ray是既危险又卡哇伊的食智力生物")
//        val random = Random()
//        val randomText = texts[random.nextInt(texts.size)] // 从列表中随机选择一个文本
//        titleText.text = randomText // 替换标题文本
    }


    /**
     *  设置 SharedPreferences / Set SharedPreferences
     *  @param: SharedPreferences
     */
    fun setSP(sharedPreferences: SharedPreferences) {
        safeSP.mSP = sharedPreferences
    }

    /**
     *  获取 SharedPreferences / Get SharedPreferences
     *  @return: SharedPreferences
     */
    @Suppress("unused")
    fun getSP(): SharedPreferences? {
        return safeSP.mSP
    }

    /**
     *  显示 Fragment / Show fragment
     *  @param: key 注册的key / Register key
     */
    fun showFragment(key: String) {
        title = dataList[key]?.title

        // 添加标题动画
        title = dataList[key]?.title

        // 添加标题动画


        val backButtonTranslationX = when {
            key == "Main" -> 0f
            key == "__menu__" -> -50f
            else -> 50f
        }
        val titleButtonTranslationX = when {
            key == "Main" -> 50f
            key == "__menu__" -> -50f
            else -> 50f
        }
        animateTitleView(titleView, isBack = key == "Main", backButtonTranslationX)
        animateTitleView(backButton, isBack = key == "Main", backButtonTranslationX)

        if (this::initViewData.isInitialized) {
            title = dataList[key]?.title
            thisName.add(key)
            val frame = MIUIFragment(key)
            if (key != "Main" && fragmentManager.backStackEntryCount != 0) {
                fragmentManager.beginTransaction().let {
                    if (key != "Menu") {

                        if (isRtl(activity)) it.setCustomAnimations(R.animator.slide_left_in, R.animator.slide_right_out, R.animator.slide_right_in, R.animator.slide_left_out)
                        else it.setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)


                    } else {

                        if (isRtl(activity)) it.setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                        else it.setCustomAnimations(R.animator.slide_left_in, R.animator.slide_right_out, R.animator.slide_right_in, R.animator.slide_left_out)
                    }
                }.replace(frameLayoutId, frame).addToBackStack(key).commit()

                backButton.visibility = View.VISIBLE
                setMenuShow(dataList[key]?.hideMenu == false)
            } else {
                setBackupShow(viewData.mainShowBack)
                fragmentManager.beginTransaction().replace(frameLayoutId, frame).addToBackStack(key).commit()
            }
            return
        }
        if (!pageInfo.containsKey(key)) {
            throw Exception("No page found")
        }
        val thisPage = pageInfo[key]!!
        title = getPageTitle(thisPage)
        thisName.add(key)
        val frame = MIUIFragment(key)
        if (key != "__main__" && fragmentManager.backStackEntryCount != 0) {
            fragmentManager.beginTransaction().let {
                if (key != "__menu__") {
                    if (isRtl(activity)) it.setCustomAnimations(R.animator.slide_left_in, R.animator.slide_right_out, R.animator.slide_right_in, R.animator.slide_left_out)
                    else it.setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                } else {
                    if (isRtl(activity)) it.setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                    else it.setCustomAnimations(R.animator.slide_left_in, R.animator.slide_right_out, R.animator.slide_right_in, R.animator.slide_left_out)
                }
            }.replace(frameLayoutId, frame).addToBackStack(key).commit()
            setBackupShow(true)
            if (key !in arrayOf("__main__", "__menu__")) setMenuShow(!getPageHideMenu(thisPage))
            if (key == "__menu__") setMenuShow(false)
        } else {
            setMenuShow(pageInfo.containsKey("__menu__"))
            setBackupShow(pageInfo["__main__"]!!.javaClass.getAnnotation(BMMainPage::class.java)!!.showBack)
            fragmentManager.beginTransaction().replace(frameLayoutId, frame).addToBackStack(key).commit()
        }
    }

    fun setMenuShow(show: Boolean) {
        if (this::initViewData.isInitialized) {
            if (!dataList.containsKey("Menu")) return
            if (show) menuButton.visibility = View.VISIBLE
            else menuButton.visibility = View.GONE
            return
        }
        if (pageInfo.containsKey("__menu__")) {
            if (show) {
                menuButton.visibility = View.VISIBLE
            } else {
                menuButton.visibility = View.GONE
            }
        }
    }

    fun setBackupShow(show: Boolean) {

        if (show) backButton.visibility = View.VISIBLE else backButton.visibility = View.GONE
    }

    private fun getPageHideMenu(basePage: BasePage): Boolean {
        return basePage.javaClass.getAnnotation(BMPage::class.java)?.hideMenu == true
    }

    private fun getPageTitle(basePage: BasePage): String {
        basePage.javaClass.getAnnotation(BMPage::class.java)?.let {
            return it.title.ifEmpty { if (it.titleId != 0) activity.getString(it.titleId) else basePage.getTitle() }
        }
        basePage.javaClass.getAnnotation(BMMainPage::class.java)?.let {
            return it.title.ifEmpty { if (it.titleId != 0) activity.getString(it.titleId) else basePage.getTitle() }
        }
        basePage.javaClass.getAnnotation(BMMenuPage::class.java)?.let {
            return it.title.ifEmpty { if (it.titleId != 0) activity.getString(it.titleId) else basePage.getTitle() }
        }
        throw Exception("No title found")
    }

    fun getTopPage(): String {
        return thisName[thisName.lastSize()]
    }

    fun getThisItems(key: String): List<BaseView> {
        if (this::initViewData.isInitialized) {
            return dataList[key]?.itemList ?: arrayListOf()
        }
        val currentPage = pageInfo[key]!!
        if (currentPage.itemList.size == 0) {
            currentPage.onCreate()
        }
        return currentPage.itemList
    }

    fun getThisAsync(key: String): AsyncInit? {
        if (this::initViewData.isInitialized) {
            return dataList[key]?.async
        }
        val currentPage = pageInfo[key]!!
        if (currentPage.itemList.size == 0) {
            currentPage.onCreate()
        }
        return object : AsyncInit {
            override val skipLoadItem: Boolean
                get() = currentPage.skipLoadItem

            override fun onInit(fragment: MIUIFragment) {
                currentPage.asyncInit(fragment)
            }
        }
    }

    fun getAllCallBacks(): (() -> Unit)? {
        return callbacks
    }

    /**
     * 设置全局返回调用 / Set global return call methods
     * @param: Unit
     */
    @Suppress("unused")
    fun setAllCallBacks(callbacks: () -> Unit) {
        this.callbacks = callbacks
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount <= 1) {
            if (isExit) {
                finishAndRemoveTask()
            } else {
                finish()
            }
        } else {
            thisName.removeAt(thisName.lastSize())
            val name = fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 2).name
            when (name) {
                "Main" -> {
                    if (!viewData.mainShowBack) backButton.visibility = View.GONE
                    if (viewData.isMenu) menuButton.visibility = View.VISIBLE
                    animateTitleView(titleView, true)  // 返回操作的动画
                    animateTitleView(backButton, true)  // 返回操作的动画
                }

                "__main__" -> {
                    if (!pageInfo[name]!!.javaClass.getAnnotation(BMMainPage::class.java)!!.showBack) backButton.visibility = View.GONE
                    setMenuShow(pageInfo.containsKey("__menu__"))
                    animateTitleView(titleView, true)  // 返回操作的动画
                    animateTitleView(backButton, true)  // 返回操作的动画
                }

                else -> {
                    if (this::initViewData.isInitialized) {
                        setMenuShow(dataList[name]?.hideMenu == false)
                    } else {
                        setMenuShow(!getPageHideMenu(pageInfo[name]!!))
                    }
                    animateTitleView(titleView, true)  // 返回操作的动画
                    animateTitleView(backButton, true)  // 返回操作的动画
                }
            }
            title = if (this::initViewData.isInitialized) {
                dataList[name]?.title
            } else {
                getPageTitle(pageInfo[name]!!)
            }
            fragmentManager.popBackStack()
        }
    }


    private fun ArrayList<*>.lastSize(): Int = this.size - 1

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("this", thisName)
    }

}