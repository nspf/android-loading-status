package com.udacity


sealed class ButtonState {
    object Default : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}