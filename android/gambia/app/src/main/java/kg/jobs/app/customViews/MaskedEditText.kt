package kg.jobs.app.customViews

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import kg.jobs.app.R

open class MaskedEditText : AppCompatEditText, TextWatcher {
    var charRepresentation: Char = ' '
    private var keepHint: Boolean = false
    private var rawToMask: IntArray? = null
    private var rawText: RawText? = null
    private var editingBefore: Boolean = false
    private var editingOnChanged: Boolean = false
    private var editingAfter: Boolean = false
    private var maskToRaw: IntArray? = null
    private var selector: Int = 0
    private var initialized: Boolean = false
    private var ignore: Boolean = false
    private var maxRawLength: Int = 0
    private var lastValidMaskPosition: Int = 0
    private var selectionChanged: Boolean = false
    private var focusChangeListener: View.OnFocusChangeListener? = null
    private var allowedChars: String = ""
    private var deniedChars: String? = ""

    private var masked = false
    var mask: String = ""
        set(value) {
            masked = value.isNotEmpty()
            field = value
            cleanUp()
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MaskedEditText)
        val mask = attributes.getString(R.styleable.MaskedEditText_mask)

        this.mask = mask ?: "---------------"

        val allowedChars = attributes.getString(R.styleable.MaskedEditText_allowed_chars)
        this.allowedChars = allowedChars ?: "1234567890"

        deniedChars = attributes.getString(R.styleable.MaskedEditText_denied_chars)

        val representation = attributes.getString(R.styleable.MaskedEditText_char_representation)

        charRepresentation = if (representation == null) '-' else representation[0]

        keepHint = attributes.getBoolean(R.styleable.MaskedEditText_keep_hint, true)

        cleanUp()
        attributes.recycle()
    }

    /** @param listener - its onFocusChange() method will be called before performing MaskedEditText operations,
     * related to this event.
     */
    override fun setOnFocusChangeListener(listener: View.OnFocusChangeListener) {
        focusChangeListener = listener
    }

    private fun cleanUp() {
        if (masked) {
            initialized = false

            generatePositionArrays()

            rawText = RawText()
            selector = rawToMask!![0]

            editingBefore = true
            editingOnChanged = true
            editingAfter = true
            if (rawText!!.length() == 0) {
                this.setText(makeMaskedTextWithHint())
            } else {
                this.setText(makeMaskedText())
            }
            editingBefore = false
            editingOnChanged = false
            editingAfter = false

            maxRawLength = maskToRaw!![previousValidPosition(mask.length - 1)] + 1
            lastValidMaskPosition = findLastValidMaskPosition()
            initialized = true

            super.setOnFocusChangeListener { v, hasFocus ->
                if (focusChangeListener != null) {
                    focusChangeListener!!.onFocusChange(v, hasFocus)
                }

                if (hasFocus()) {
                    selectionChanged = false
                    this@MaskedEditText.setSelection(lastValidPosition())
                }
            }
        } else {
            super.setOnFocusChangeListener { _, _ ->

            }
            initialized = false
            setText("")
        }
    }

    private fun findLastValidMaskPosition(): Int {
        maskToRaw!!.indices.reversed()
                .filter { maskToRaw!![it] != -1 }
                .forEach { return it }
        throw RuntimeException("Mask must contain at least one representation char")
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    /**
     * Generates positions for values characters. For instance:
     * Input data: mask = "+7(###)###-##-##
     * After method execution:
     * rawToMask = [3, 4, 5, 6, 8, 9, 11, 12, 14, 15]
     * maskToRaw = [-1, -1, -1, 0, 1, 2, -1, 3, 4, 5, -1, 6, 7, -1, 8, 9]
     * charsInMask = "+7()- " (and space, yes)
     */
    private fun generatePositionArrays() {
        val aux = IntArray(mask.length)
        maskToRaw = IntArray(mask.length)
        var charsInMaskAux = ""

        var charIndex = 0
        for (i in 0 until mask.length) {
            val currentChar = mask[i]
            if (currentChar == charRepresentation) {
                aux[charIndex] = i
                maskToRaw!![i] = charIndex++
            } else {
                val charAsString = Character.toString(currentChar)
                if (!charsInMaskAux.contains(charAsString)) {
                    charsInMaskAux += charAsString
                }
                maskToRaw!![i] = -1
            }
        }
        if (charsInMaskAux.indexOf(' ') < 0) {
            charsInMaskAux += SPACE
        }

        charsInMaskAux.toCharArray()

        rawToMask = IntArray(charIndex)
        for (i in 0 until charIndex) {
            rawToMask!![i] = aux[i]
        }
    }

    private fun init() {
        addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                   after: Int) {
        if (!editingBefore && masked) {
            editingBefore = true
            if (start > lastValidMaskPosition) {
                ignore = true
            }
            var rangeStart = start
            if (after == 0) {
                rangeStart = erasingStart(start)
            }
            val range = calculateRange(rangeStart, start + count)
            if (range.start != -1) {
                rawText!!.subtractFromString(range)
            }
            if (count > 0) {
                selector = previousValidPosition(start)
            }
        }
    }

    private fun erasingStart(start: Int): Int {
        var start = start
        while (start > 0 && maskToRaw!![start] == -1) {
            start--
        }
        return start
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (masked) {
            var count = count
            if (!editingOnChanged && editingBefore) {
                editingOnChanged = true
                if (ignore) {
                    return
                }
                if (count > 0) {
                    val startingPosition = maskToRaw!![nextValidPosition(start)]
                    val addedString = s.subSequence(start, start + count).toString()
                    count = rawText!!.addToString(clear(addedString), startingPosition, maxRawLength)
                    if (initialized) {
                        val currentPosition: Int = if (startingPosition + count < rawToMask!!.size)
                            rawToMask!![startingPosition + count]
                        else lastValidMaskPosition + 1
                        selector = nextValidPosition(currentPosition)
                    }
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable) {
        if (!editingAfter && editingBefore && editingOnChanged && masked) {
            editingAfter = true
            if (keepHint || rawText!!.length() == 0) {
                setText(makeMaskedTextWithHint())
            } else {
                setText(makeMaskedText())
            }

            selectionChanged = false
            setSelection(selector)

            editingBefore = false
            editingOnChanged = false
            editingAfter = false
            ignore = false
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        var selStart = selStart
        var selEnd = selEnd
        // On Android 4+ this method is being called more than 1 time if there is a hint in the EditText, what moves the cursor to left
        // Using the boolean var selectionChanged to limit to one execution

        if (initialized) {
            if (!selectionChanged) {
                selStart = fixSelection(selStart)
                selEnd = fixSelection(selEnd)

                // exactly in this order. If getText.length() == 0 then selStart will be -1
                if (selStart > text?.length ?: 0) selStart = text?.length ?: 0
                if (selStart < 0) selStart = 0

                // exactly in this order. If getText.length() == 0 then selEnd will be -1
                if (selEnd > text?.length ?: 0) selEnd = text?.length ?: 0
                if (selEnd < 0) selEnd = 0

                setSelection(selStart, selEnd)
                selectionChanged = true
            } else {
                //check to see if the current bnv_selector is outside the already entered text
                if (selStart > rawText!!.length() - 1) {
                    if (fixSelection(selStart) >= 0 && fixSelection(selEnd) < text?.length ?: 0) {
                        setSelection(fixSelection(selStart), fixSelection(selEnd))
                    }
                }
            }
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    private fun fixSelection(selection: Int): Int {
        return if (selection > lastValidPosition()) {
            lastValidPosition()
        } else {
            nextValidPosition(selection)
        }
    }

    private fun nextValidPosition(position: Int): Int {
        var currentPosition = position
        while (currentPosition < lastValidMaskPosition && maskToRaw!![currentPosition] == -1) {
            currentPosition++
        }
        return if (currentPosition > lastValidMaskPosition) lastValidMaskPosition + 1 else currentPosition
    }

    private fun previousValidPosition(position: Int): Int {
        var currentPosition = position
        while (currentPosition >= 0 && maskToRaw!![currentPosition] == -1) {
            currentPosition--
            if (currentPosition < 0) {
                return nextValidPosition(0)
            }
        }
        return currentPosition
    }

    private fun lastValidPosition(): Int {
        return if (rawText!!.length() == maxRawLength) {
            rawToMask!![rawText!!.length() - 1] + 1
        } else nextValidPosition(rawToMask!![rawText!!.length()])
    }


    private fun makeMaskedText(): String {
        val maskedTextLength: Int = if (rawText!!.length() < rawToMask!!.size) {
            rawToMask!![rawText!!.length()]
        } else {
            mask.length
        }
        val maskedText = CharArray(maskedTextLength) //mask.replace(charRepresentation, ' ').toCharArray();
        for (i in maskedText.indices) {
            val rawIndex = maskToRaw!![i]
            if (rawIndex == -1) {
                maskedText[i] = mask[i]
            } else {
                maskedText[i] = rawText!!.charAt(rawIndex)
            }
        }
        return String(maskedText)
    }

    private fun makeMaskedTextWithHint(): CharSequence {
        val ssb = SpannableStringBuilder()
        var mtrv: Int
        val maskFirstChunkEnd = rawToMask!![0]
        for (i in 0 until mask.length) {
            mtrv = maskToRaw!![i]
            if (mtrv != -1) {
                if (mtrv < rawText!!.length()) {
                    ssb.append(rawText!!.charAt(mtrv))
                } else {
                    ssb.append(mask[i])
                }
            } else {
                ssb.append(mask[i])
            }
            if (keepHint && rawText!!.length() < rawToMask!!.size && i >= rawToMask!![rawText!!.length()] ||
                    !keepHint && i >= maskFirstChunkEnd) {
                ssb.setSpan(ForegroundColorSpan(currentHintTextColor), i, i + 1, 0)
            }
        }
        return ssb
    }

    private fun calculateRange(start: Int, end: Int): Range {
        val range = Range()
        var i = start
        while (i <= end && i < mask.length) {
            if (maskToRaw!![i] != -1) {
                if (range.start == -1) {
                    range.start = maskToRaw!![i]
                }
                range.end = maskToRaw!![i]
            }
            i++
        }
        if (end == mask.length) {
            range.end = rawText!!.length()
        }
        if (range.start == range.end && start < end) {
            val newStart = previousValidPosition(range.start - 1)
            if (newStart < range.start) {
                range.start = newStart
            }
        }
        return range
    }

    private fun clear(data: String): String {
        var text = data
        if (deniedChars?.isNotEmpty() == true) {
            for (c in deniedChars!!.toCharArray()) {
                text = text.replace(Character.toString(c), "")
            }
        }

        if (allowedChars.isNotEmpty()) {
            val builder = StringBuilder(text.length)

            text.toCharArray()
                    .filter { allowedChars.contains(it.toString()) }
                    .forEach { builder.append(it) }

            text = builder.toString()
        }

        return text
    }

    companion object {
        val SPACE = " "
    }

    inner class Range {
        var start: Int = -1
        var end: Int = -1
    }

    inner class RawText {
        var text: String = ""
            private set

        /**
         * text = 012345678, range = 123 =&gt; text = 0456789
         * @param range given range
         */
        fun subtractFromString(range: Range) {
            var firstPart = ""
            var lastPart = ""

            if (range.start > 0 && range.start <= text.length) {
                firstPart = text.substring(0, range.start)
            }
            if (range.end >= 0 && range.end < text.length) {
                lastPart = text.substring(range.end, text.length)
            }
            text = firstPart + lastPart
        }

        /**
         *
         * @param newString New String to be added
         * @param start Position to insert newString
         * @param maxLength Maximum raw text length
         * @return Number of added characters
         */
        fun addToString(newString: String?, start: Int, maxLength: Int): Int {
            var newString = newString
            var firstPart = ""
            var lastPart = ""

            if (newString == null || newString == "") {
                return 0
            } else if (start < 0) {
                throw IllegalArgumentException("Start position must be non-negative")
            } else if (start > text.length) {
                throw IllegalArgumentException("Start position must be less than the actual text length")
            }

            var count = newString.length

            if (start > 0) {
                firstPart = text.substring(0, start)
            }
            if (start >= 0 && start < text.length) {
                lastPart = text.substring(start, text.length)
            }
            if (text.length + newString.length > maxLength) {
                count = maxLength - text.length
                newString = newString.substring(0, count)
            }
            text = firstPart + newString + lastPart
            return count
        }

        fun length(): Int {
            return text.length
        }

        fun charAt(position: Int): Char {
            return text[position]
        }
    }
}
