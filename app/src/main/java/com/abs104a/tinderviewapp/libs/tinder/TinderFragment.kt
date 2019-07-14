package com.abs104a.tinderview.tinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.abs104a.tinderview.R

class TinderFragment<T> : Fragment() {

    companion object {
        // 画面外に行くアニメーション時間
        private const val MOVE_TIME_TH: Int = 16
        private const val MOVE_TH: Int = 5
        private const val JUDGE_TH = 0.25

        enum class STATE{
            NONE,
            TOP,
            BOTTOM,
            RIGHT,
            LEFT
        }
    }

    var getCardView: (obj: T, viewGroup: ViewGroup) -> Unit = { _, _ -> }
    var onSwipeSelected: (state: STATE, obj: T, view: View) -> Unit = { _, _, _ -> }
    var onSwipeCompleted: (state: STATE, obj: T, view: View) -> Unit = { _, _, _ -> }
    var onChangeSwipingStatus: (state: STATE, obj: T, view: View) -> Unit = {_, _, _ -> }

    private var frameLayout: FrameLayout? = null
    private var items = mutableListOf<T>()

    // デフォルトのConfigを追加
    private var config : TinderConfig = TinderConfig.ConfigBuilder().build()
    private var moveFlag = 0
    private var oldState = STATE.NONE
    private val touchListener = object : View.OnTouchListener {

        private var targetLocalX: Int = 0
        private var targetLocalY: Int = 0

        private var screenX: Float = 0f
        private var screenY: Float = 0f

        override fun onTouch(target: View?, event: MotionEvent?): Boolean {
            // onTouch
            if (event == null || target == null) return true
            val x = event.rawX
            val y = event.rawY

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    targetLocalX = target.left
                    targetLocalY = target.top

                    screenX = x
                    screenY = y

                    moveFlag = 0

                    frameLayout?.apply {
                        when {
                            items.size == 0 -> {
                                getChildAt(0).visibility = View.GONE
                                getChildAt(1).visibility = View.GONE
                            }
                            items.size == 1 -> {
                                getChildAt(0).visibility = View.GONE
                                getChildAt(1).visibility = View.VISIBLE
                            }
                            else -> {
                                getChildAt(0).visibility = View.VISIBLE
                                getChildAt(1).visibility = View.VISIBLE
                            }
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    // タッチ中
                    val diffX = screenX - x
                    val diffY = screenY - y

                    targetLocalX -= diffX.toInt()
                    targetLocalY -= diffY.toInt()

                    target.translationX = targetLocalX.toFloat()
                    target.translationY = targetLocalY.toFloat()
                    target.rotation = ((targetLocalX.toFloat() / target.width.toFloat()) + (targetLocalY.toFloat() / target.height.toFloat())) * config.rotate

                    screenX = x
                    screenY = y

                    if (moveFlag < MOVE_TH) moveFlag += 1

                    // ステート
                    if (targetLocalX > target.width * JUDGE_TH) {
                        if (oldState != STATE.RIGHT){
                            onChangeSwipingStatus(STATE.RIGHT, items[0],target)
                            oldState = STATE.RIGHT
                        }
                    } else if (0 - targetLocalX > target.width * JUDGE_TH) {
                        if (oldState != STATE.LEFT){
                            onChangeSwipingStatus(STATE.LEFT, items[0],target)
                            oldState = STATE.LEFT
                        }
                    } else if (targetLocalY > target.height * JUDGE_TH) {
                        if (oldState != STATE.BOTTOM){
                            onChangeSwipingStatus(STATE.BOTTOM, items[0],target)
                            oldState = STATE.BOTTOM
                        }
                    } else if (0 - targetLocalY > target.height * JUDGE_TH) {
                        if (oldState != STATE.TOP) {
                            onChangeSwipingStatus(STATE.TOP, items[0],target)
                            oldState = STATE.TOP
                        }
                    } else if (oldState != STATE.NONE){
                        onChangeSwipingStatus(STATE.NONE, items[0],target)
                        oldState = STATE.NONE
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_OUTSIDE -> {

                    if (event.action == MotionEvent.ACTION_UP &&
                            moveFlag < MOVE_TIME_TH &&
                            Math.abs(targetLocalX + targetLocalY) < MOVE_TH) {
                        // クリックイベントの処理
                        target.performClick()
                    }
                    moveFlag = 0

                    // 動作判定（Viewの1/3以上動いたらそれぞれの動作として見なす）
                    when {
                        getTopView() != target -> {
                            // キャンセル
                            viewAnimation(target, 0f - target.left, 0f - target.top, 0f ,Runnable{
                                onSwipeCompleted(STATE.NONE, items[0],target)
                            })
                            onSwipeSelected(STATE.NONE, items[0],target)
                        }
                        targetLocalX > target.width * JUDGE_TH -> {
                            // 右
                            val item = items.removeAt(0)
                            onSwipeSelected(STATE.RIGHT,item,target)
                            val margin = if (target.width < target.height) target.height - target.width else 0
                            viewAnimation(target, (target.width + margin).toFloat(), ((target.width * targetLocalY) / targetLocalX).toFloat(), config.rotate,Runnable {
                                onSwipeCompleted(STATE.RIGHT,item,target)
                                changeCard()
                            })
                        }
                        0 - targetLocalX > target.width * JUDGE_TH -> {
                            // 左
                            val item = items.removeAt(0)
                            onSwipeSelected(STATE.LEFT,item,target)
                            val margin = if (target.width < target.height) target.height - target.width else 0
                            viewAnimation(target, (0 - target.width - margin).toFloat(), ((target.width * targetLocalY) / (0 - targetLocalX)).toFloat(),0 - config.rotate, Runnable {
                                onSwipeCompleted(STATE.LEFT,item,target)
                                changeCard()
                            })
                        }
                        targetLocalY > target.height * JUDGE_TH -> {
                            // 下
                            val item = items.removeAt(0)
                            onSwipeSelected(STATE.BOTTOM,item,target)
                            val margin = if (target.width < target.height) 0 else target.width - target.height
                            viewAnimation(target, ((target.height * targetLocalX) / targetLocalY).toFloat(), (target.height + margin).toFloat(),config.rotate, Runnable {
                                onSwipeCompleted(STATE.BOTTOM,item,target)
                                changeCard()
                            })
                        }
                        0 - targetLocalY > target.height * JUDGE_TH -> {
                            // 上
                            val item = items.removeAt(0)
                            onSwipeSelected(STATE.TOP,item,target)
                            val margin = if (target.width < target.height) 0 else target.width - target.height
                            viewAnimation(target, ((target.height * targetLocalX) / (0 - targetLocalY)).toFloat(), (0 - target.height - margin).toFloat(),0 - config.rotate, Runnable {
                                onSwipeCompleted(STATE.TOP,item,target)
                                changeCard()
                            })
                        }
                        else -> {
                            // キャンセルされた時
                            viewAnimation(target, 0f - target.left, 0f - target.top,0f, Runnable{
                                onSwipeCompleted(STATE.NONE, items[0],target)
                            })
                            onSwipeSelected(STATE.NONE, items[0],target)
                        }
                    }
                    // リセットする．
                    screenX = 0f
                    screenY = 0f
                    targetLocalX = 0
                    targetLocalY = 0
                    oldState = STATE.NONE
                }
            }
            return true
        }
    }

    private fun changeCard() {
        swapView()
        frameLayout?.apply {
            if (items.size >= 2) {
                getCardView(items[1], getChildAt(0) as ViewGroup)
            } else if (items.size == 0) {
                getChildAt(0).visibility = View.GONE
                getChildAt(1).visibility = View.GONE
            }
        }
    }


    /**
     * ViewをAnimationさせる
     */
    private fun viewAnimation(target: View, toX: Float, toY: Float,rotate: Float, callback: Runnable?) =
        target.animate()
                .translationX(toX)
                .translationY(toY)
                .rotation(rotate)
                .setDuration(config.duration)
                .withEndAction(callback)
                .start()

    /**
     * 設定の追加
     */
    fun setTinderConfig(config: TinderConfig){ this.config = config }

    fun addItem(obj: T) { items.add(obj)}

    fun addItem(obj: List<T>) {items.addAll(obj)}

    fun addItem(pos: Int, obj: T) {
        items.add(pos, obj)

        frameLayout?.also {
            if(items.size == 1) {
                it.getChildAt(0).visibility = View.VISIBLE
            } else if (items.size == 2) {
                it.getChildAt(1).visibility = View.VISIBLE
                it.getChildAt(0).visibility = View.VISIBLE
            }

            if (pos == 0) {
                swapView()
                val target = it.getChildAt(1)
                getCardView(items[pos], target as ViewGroup)
                val margin = if (target.width > target.height) target.width - target.height else 0
                target.translationY = (target.height + margin).toFloat()
                target.rotation = config.rotate
                viewAnimation(target, 0f, 0f, 0f,null)
            } else if (pos == 1) {
                getCardView(items[pos], it.getChildAt(0) as ViewGroup)
            }
        }
    }

    private fun swapView() {
        frameLayout?.also {
            it.getChildAt(1)?.apply {
                translationX = 0f
                translationY = 0f
                rotation = 0f
                it.removeViewAt(1)
                it.addView(this, 0)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        frameLayout = inflater.inflate(R.layout.fragment_tinder, container, false) as FrameLayout
        initView(frameLayout as ViewGroup)
        return frameLayout
    }

    private fun initView(viewGroup: ViewGroup) {
        when {
            items.size == 0 -> {
                viewGroup.getChildAt(1).visibility = View.INVISIBLE
                viewGroup.getChildAt(0).visibility = View.INVISIBLE
            }
            items.size == 1 -> {
                viewGroup.getChildAt(1).visibility = View.VISIBLE
                viewGroup.getChildAt(0).visibility = View.INVISIBLE
                getCardView(items[0],viewGroup.getChildAt(0) as ViewGroup)
            }
            else -> {
                viewGroup.getChildAt(1).visibility = View.VISIBLE
                viewGroup.getChildAt(0).visibility = View.VISIBLE

                getCardView(items[0], viewGroup.getChildAt(1) as ViewGroup)
                getCardView(items[1], viewGroup.getChildAt(0) as ViewGroup)

            }
        }
        viewGroup.getChildAt(0).setOnTouchListener(touchListener)
        viewGroup.getChildAt(1).setOnTouchListener(touchListener)
    }

    private fun getTopView(): ViewGroup {
        frameLayout?.also {
            return it.getChildAt(1) as ViewGroup
        }
        throw IllegalStateException()
    }

    fun notifiyDataSetChanged() = initView(frameLayout as ViewGroup)

    fun getTopItem(): T = getItem(0)

    fun getSize(): Int = items.size

    fun getItem(pos: Int): T = items[pos]
}
