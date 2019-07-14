package com.abs104a.tinderviewapp

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.abs104a.tinderview.tinder.TinderConfig
import com.abs104a.tinderview.tinder.TinderFragment

class MainActivity : AppCompatActivity() {

    data class SampleData(val string: String, val color: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataList = mutableListOf<SampleData>().also {
            it.add(SampleData("test1", ContextCompat.getColor(this, R.color.turquoise)))
            it.add(SampleData("test2", ContextCompat.getColor(this, R.color.emerland)))
            it.add(SampleData("test3", ContextCompat.getColor(this, R.color.peterrier)))
            it.add(SampleData("test4", ContextCompat.getColor(this, R.color.amethyst)))
            it.add(SampleData("test5", ContextCompat.getColor(this, R.color.wetasphalt)))
        }.toList()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.constraint_layout, TinderFragment<SampleData>().apply {
                addItem(dataList)
                setTinderConfig(TinderConfig.ConfigBuilder().setDurtation(400).setRotate(90f).build())
                getCardView = { data, viewGroup ->
                    viewGroup.addView(TextView(context))
                    (viewGroup.getChildAt(0) as? TextView)?.apply {
                        text = data.string
                        setBackgroundColor(data.color)
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT)
                        gravity = Gravity.CENTER
                    }
                }
                onSwipeSelected = { state, data, _ ->
                    val pos = dataList.indexOf(data)
                    Log.v("onSwipeSelected", "state: ${state.name}, string: ${data.string}, pos: $pos")
                    // replace items
                    if(state != TinderFragment.Companion.STATE.NONE) addItem(data)
                }
                onSwipeCompleted = { state, data, _ ->
                    val pos = dataList.indexOf(data)
                    Log.v("onSwipeCompleted", "state: ${state.name}, string: ${data.string}, pos: $pos")
                }
                onChangeSwipingStatus = { state, data, _ ->
                    val pos = dataList.indexOf(data)
                    Log.v("onChangeSwipingStatus", "state: ${state.name}, string: ${data.string}, pos: $pos")
                }
            }, "TinderFragment")
            .commit()
    }
}
