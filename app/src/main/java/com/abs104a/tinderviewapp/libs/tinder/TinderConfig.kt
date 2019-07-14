package com.abs104a.tinderview.tinder

open class TinderConfig protected constructor(val duration: Long, val rotate: Float) {

    class ConfigBuilder {

        companion object{
            private const val DEFAULT_DURATION: Long = 400
            private const val DEFAULT_ROTATE = 90f
        }

        private var duration : Long = DEFAULT_DURATION
        private var rotate : Float = DEFAULT_ROTATE

        fun setDurtation(duration: Long): ConfigBuilder {
            this.duration = duration
            return this
        }

        fun setRotate(rotate: Float): ConfigBuilder {
            this.rotate = rotate
            return this
        }

        fun build(): TinderConfig = TinderConfig(duration,rotate)
    }
}