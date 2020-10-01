package com.udacity

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Button size
    private var widthSize = 0
    private var heightSize = 0

    // Current text
    private var text = TEXT_DEFAULT

    // Values used to animate progress and loading indicator
    private var currentWidth = 0
    private var currentDegree = 0

    // Values used for the loading indicator
    private val circleIndicatorWidth = LOADING_INDICATOR_WIDTH
    private val circleIndicatorStrokeWidth = LOADING_INDICATOR_STROKE_WIDTH
    private val circleIndicatorLeft = LOADING_INDICATOR_LEFT
    private val circleIndicatorStrokeColor = LOADING_INDICATOR_COLOR
    private val circleIndicatorStartAngle = LOADING_INDICATOR_START_ANGLE

    // Custom attributes. These can be specified via XML
    private var background = DEFAULT_BUTTON_COLOR
    private var textColor = DEFAULT_TEXT_COLOR
    private var progressColor = DEFAULT_PROGRESS_COLOR

    private val r = Rect()
    private val rectF = RectF()

    private var valueAnimator = ValueAnimator()

    /*
    Style and color info for the rectangle that defines the button
    */
    private val paintButton = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
    }

    /*
    Style and color info for the button text
    */
    private val paintText = Paint().apply {
        style = Paint.Style.FILL
        textAlign = Align.CENTER
        textSize = 60f
    }

    /*
    Style and color info for the progress animation
    */
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /*
    Style and color info for the circle animation
    */
    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    /*
    Read more:
    https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-delegates/observable.html
     */
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when(new) {
            ButtonState.Default -> {
                text = TEXT_DEFAULT
            }
            ButtonState.Loading -> {
                text = TEXT_LOADING
                loadingAnimation()
            }
            ButtonState.Completed -> {
                text = TEXT_COMPLETED
                valueAnimator.end()
            }
        }

        /*
        No matter which state, always redraw the button
         */
        invalidate()
    }

    init {
        /*
        Read more:
        https://android.github.io/android-ktx/core-ktx/androidx.content/android.content.-context/with-styled-attributes.html
         */
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            textColor = getColor(R.styleable.LoadingButton_lb_textColor, DEFAULT_TEXT_COLOR)
            background = getColor(R.styleable.LoadingButton_lb_background, DEFAULT_BUTTON_COLOR)
            progressColor = getColor(R.styleable.LoadingButton_lb_progressColor, DEFAULT_PROGRESS_COLOR)

            // TODO: add more attributes here
        }

        setState(ButtonState.Default)
    }

    /*
    Set the current status of the button (Default, Loading, Completed)
    */
    fun setState(state: ButtonState) {
        buttonState = state
    }

    /*
    Utility method to draw text centered on the button

    See more: https://stackoverflow.com/a/32081250
     */
    private fun drawCenter(canvas: Canvas, paint: Paint, text: String) {
        canvas.getClipBounds(r)
        val cHeight: Int = r.height()
        val cWidth: Int = r.width()
        paint.textAlign = Align.LEFT
        paint.getTextBounds(text, 0, text.length, r)
        val x: Float = cWidth / 2f - r.width() / 2f - r.left
        val y: Float = cHeight / 2f + r.height() / 2f - r.bottom
        canvas.drawText(text, x, y, paint)
    }

    /*
    Called whenever Android thinks that the view should be redrawn

    Read more: https://stackoverflow.com/a/11912495
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        /*
        Draw the rectangle that defines the button, with its background color
         */
        paintButton.color = background
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paintButton)

        /*
        We show animations only if the button is in the Loading state
         */
        if (buttonState == ButtonState.Loading) {

            /*
            Show the progress bar using a custom color, and according the currentWidth value.
             */
            paintProgress.color = progressColor
            canvas?.drawRect(
                0f,
                0f,
                currentWidth.toFloat(),
                heightSize.toFloat(),
                paintProgress
            )

            /*
            RectF holds four float coordinates for a rectangle.
            Read more: https://developer.android.com/reference/android/graphics/RectF
             */
            rectF.set(
                circleIndicatorLeft - circleIndicatorWidth,
                (height/2) - circleIndicatorWidth,
                circleIndicatorLeft + circleIndicatorWidth,
                (height/2) + circleIndicatorWidth)

            paintCircle.apply {
                color = circleIndicatorStrokeColor
                strokeWidth = circleIndicatorStrokeWidth.toFloat()
            }

            /*
            Show the circle indicator using a custom color
             */
            canvas?.drawArc(
                rectF,
                currentDegree.toFloat() - circleIndicatorStartAngle,
                currentDegree.toFloat(),
                false,
                paintCircle)

        }

        /*
        Set the text color and place the text centered on the button.
        */
        paintText.color = textColor
        canvas?.let { drawCenter(it, paintText, text) }
    }

    /*
    Measure the view and its content to determine the measured width and the measured height
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        /*
        getSuggestedMinimumWidth()

        Returns the suggested minimum width that the view should use.
         */
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth

        /*
        resolveSizeAndState()

        Utility to reconcile a desired size and state, with constraints imposed by a MeasureSpec.
        Will take the desired size, unless a different size is imposed by the constraints.

        The returned value is a compound integer, with the resolved size in the MEASURED_SIZE_MASK bits
        and optionally the bit MEASURED_STATE_TOO_SMALL set
        if the resulting size is smaller than the size the view wants to be.
         */
        val width: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val height: Int = resolveSizeAndState(MeasureSpec.getSize(width), heightMeasureSpec, 0)

        /*
        We store width and height in variables to use later
         */
        widthSize = width
        heightSize = height

        /*
        This method must be called by onMeasure(int, int)
        to store the measured width and measured height.
        Failing to do so will trigger an exception at measurement time.
         */
        setMeasuredDimension(width, height)
    }

    /*
    Animator for both the loading indicator and the progress bar through the button
     */
    private fun loadingAnimation() {
        valueAnimator.setValues(
            PropertyValuesHolder.ofInt(PROPERTY_RECT, 0, widthSize),
            PropertyValuesHolder.ofInt(PROPERTY_ARC, 0, 180)
        )
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 1800
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.addUpdateListener {
            currentWidth = it.getAnimatedValue(PROPERTY_RECT) as Int
            currentDegree = it.getAnimatedValue(PROPERTY_ARC) as Int
            invalidate()
        }
        valueAnimator.start()
    }

    /*
    Constants and default values
     */
    companion object {

        // Default texts
        const val TEXT_DEFAULT = "Download"
        const val TEXT_LOADING = "Loading..."
        const val TEXT_COMPLETED = "Done"

        // Default colors
        const val DEFAULT_BUTTON_COLOR = R.attr.colorPrimary
        const val DEFAULT_PROGRESS_COLOR = R.attr.colorPrimaryDark
        const val DEFAULT_TEXT_COLOR = Color.WHITE

        // Animator properties
        const val PROPERTY_RECT = "rect"
        const val PROPERTY_ARC = "arc"

        // Default values for the loading circle
        const val LOADING_INDICATOR_WIDTH = 32f
        const val LOADING_INDICATOR_LEFT = 72f
        const val LOADING_INDICATOR_STROKE_WIDTH = 10
        const val LOADING_INDICATOR_START_ANGLE = 45
        const val LOADING_INDICATOR_COLOR = Color.YELLOW
   }

}