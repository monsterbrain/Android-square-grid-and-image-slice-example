package com.monsterbrain.squaregridsample

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * A layout that arranges views into a grid of same-sized squares.
 *
 * This source code contained in this file is in the Public Domain.
 *
 * @author Tom Gibara
 */
class SquareGridLayout : ViewGroup {
    // fields
    /**
     * Records the number of views on each side of the square (ie. the number of rows and columns)
     */
    private var mSize = 1

    /**
     * Records the size of the square in pixels (excluding padding).
     * This is set during [.onMeasure]
     */
    private var mSquareDimensions = 0
    // constructors
    /**
     * Constructor used to create layout programatically.
     */
    constructor(context: Context?) : super(context) {}

    /**
     * Constructor used to inflate layout from XML. It extracts the size from
     * the attributes and sets it.
     */
    /* This requires a resource to be defined like this:
	 *
	 * <resources>
	 *   <declare-styleable name="SquareGridLayout">
	 *     <attr name="size" format="integer"/>
	 *   </declare-styleable>
	 * </resources>
	 *
	 * So that the attribute can be set like this:
	 *
	 * <com.tomgibara.android.util.SquareGridLayout
	 *   xmlns:android="http://schemas.android.com/apk/res/android"
	 *   xmlns:util="http://schemas.android.com/apk/res/com.tomgibara.android.background"
	 *   util:size="3"
	 *   />
	 */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.SquareGridLayout)
        setSize(a.getInt(R.styleable.SquareGridLayout_size, 1))
        a.recycle()
    }
    // accessors
    /**
     * Sets the number of views on each side of the square.
     *
     * @param size the size of grid (at least 1)
     */
    fun setSize(size: Int) {
        require(size >= 1) { "size must be positive" }
        if (mSize != size) {
            mSize = size
            requestLayout()
        }
    }

    // View methods
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // breakdown specs
        val mw = MeasureSpec.getMode(widthMeasureSpec)
        val mh = MeasureSpec.getMode(heightMeasureSpec)
        val sw = MeasureSpec.getSize(widthMeasureSpec)
        val sh = MeasureSpec.getSize(heightMeasureSpec)

        // compute padding
        val pw = paddingLeft + paddingRight
        val ph = paddingTop + paddingBottom

        // compute largest size of square (both with and without padding)
        val s: Int
        val sp: Int
        require(!(mw == MeasureSpec.UNSPECIFIED && mh == MeasureSpec.UNSPECIFIED)) { "Layout must be constrained on at least one axis" }
        if (mw == MeasureSpec.UNSPECIFIED) {
            s = sh
            sp = s - ph
        } else if (mh == MeasureSpec.UNSPECIFIED) {
            s = sw
            sp = s - pw
        } else {
            if (sw - pw < sh - ph) {
                s = sw
                sp = s - pw
            } else {
                s = sh
                sp = s - ph
            }
        }

        // guard against giving the children a -ve measure spec due to excessive padding
        val spp = Math.max(sp, 0)

        // pass on our rigid dimensions to our children
        val size = mSize
        for (y in 0 until size) {
            for (x in 0 until size) {
                val child = getChildAt(y * size + x) ?: continue
                // measure each child
                // we could try to accommodate oversized children, but we don't
                measureChildWithMargins(
                    child,
                    MeasureSpec.makeMeasureSpec((spp + x) / size, MeasureSpec.EXACTLY), 0,
                    MeasureSpec.makeMeasureSpec((spp + y) / size, MeasureSpec.EXACTLY), 0
                )
            }
        }

        //record our dimensions
        setMeasuredDimension(
            if (mw == MeasureSpec.EXACTLY) sw else sp + pw,
            if (mh == MeasureSpec.EXACTLY) sh else sp + ph
        )
        mSquareDimensions = sp
    }

    // ViewGroup methods
    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        )
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        // recover the previously computed square dimensions for efficiency
        var l = l
        var t = t
        val s = mSquareDimensions
        run {

            // adjust for our padding
            val pl = paddingLeft
            val pt = paddingTop
            val pr = paddingRight
            val pb = paddingBottom

            // allocate any extra spare space evenly
            l = pl + (r - pr - l - pl - s) / 2
            t = pt + (b - pb - t - pb - s) / 2
        }
        val size = mSize
        for (y in 0 until size) {
            for (x in 0 until size) {
                val child = getChildAt(y * mSize + x) ?: return
                // optimization: we are moving through the children in order
                // when we hit null, there are no more children to layout so return
                // get the child's layout parameters so that we can honour their margins
                val lps = child.layoutParams as MarginLayoutParams
                // we don't support gravity, so the arithmetic is simplified
                child.layout(
                    l + s * x / size + lps.leftMargin,
                    t + s * y / size + lps.topMargin,
                    l + s * (x + 1) / size - lps.rightMargin,
                    t + s * (y + 1) / size - lps.bottomMargin
                )
            }
        }
    }
}