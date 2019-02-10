package com.tunjid.androidbootstrap.core.text;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import com.tunjid.androidbootstrap.functions.Consumer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Provides {@link String#format} style functions that work with {@link Spanned} strings and preserve formatting.
 *
 * @see <a href="https://github.com/george-steel/android-utils/blob/master/src/org/oshkimaadziig/george/androidutils/SpanFormatter.java">Original source</a>
 * @see <a href="http://developer.android.com/guide/topics/resources/string-resource.html">Modifications from Android Developers website</a>
 * <p>
 * Created by tj.dahunsi on 5/20/17.
 */

public class SpanBuilder {

    private static final Pattern FORMAT_SEQUENCE = Pattern.compile("%([0-9]+\\$|<?)([^a-zA-z%]*)([[a-zA-Z%]&&[^tT]]|[tT][a-zA-Z])");
    private static final String CONCATENATE_FORMATTER = "%1$s%2$s";
    private static final String BRACKETS = "(%1$s)";
    private static final String NEW_LINE = "\n";
    private static final String SPACE = " ";

    private CharSequence content;

    public static SpanBuilder of() { return new SpanBuilder(); }

    public static SpanBuilder of(CharSequence content) { return new SpanBuilder(content); }

    private SpanBuilder() { this.content = ""; }

    private SpanBuilder(CharSequence content) { this.content = content; }

    public SpanBuilder bold() {
        this.content = bold(content);
        return this;
    }

    public SpanBuilder italic() {
        this.content = italic(content);
        return this;
    }

    public SpanBuilder underline() {
        this.content = underline(content);
        return this;
    }

    public SpanBuilder resize(float relativeSize) {
        this.content = resize(relativeSize, content);
        return this;
    }

    public SpanBuilder color(@ColorInt int color) {
        this.content = color(color, content);
        return this;
    }

    public SpanBuilder color(Context context, @ColorRes int color) {
        this.content = color(ContextCompat.getColor(context, color), content);
        return this;
    }

    public SpanBuilder click(TextView textView,
                             Consumer<TextPaint> paintConsumer,
                             Runnable clickAction) {
        this.content = click(textView, paintConsumer, clickAction, content);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        return this;
    }

    public SpanBuilder prepend(Number number) {
        content = format(CONCATENATE_FORMATTER, String.valueOf(number), content);
        return this;
    }

    public SpanBuilder append(Number number) {
        content = format(CONCATENATE_FORMATTER, content, String.valueOf(number));
        return this;
    }

    public SpanBuilder prepend(CharSequence sequence) {
        content = format(CONCATENATE_FORMATTER, sequence, content);
        return this;
    }

    public SpanBuilder append(CharSequence sequence) {
        content = format(CONCATENATE_FORMATTER, content, sequence);
        return this;
    }

    public SpanBuilder prependNewLine() {
        content = format(CONCATENATE_FORMATTER, NEW_LINE, content);
        return this;
    }

    public SpanBuilder appendNewLine() {
        content = format(CONCATENATE_FORMATTER, content, NEW_LINE);
        return this;
    }

    public SpanBuilder prependSpace() {
        content = format(CONCATENATE_FORMATTER, SPACE, content);
        return this;
    }

    public SpanBuilder appendSpace() {
        content = format(CONCATENATE_FORMATTER, content, SPACE);
        return this;
    }

    @SuppressWarnings("unused")
    public SpanBuilder wrapInBrackets() {
        content = format(BRACKETS, content);
        return this;
    }

    public CharSequence build() {
        return content;
    }


    /**
     * Returns a CharSequence that concatenates the specified array of CharSequence
     * objects and then applies a list of zero or more tags to the entire range.
     *
     * @param content an array of character sequences to apply a style to
     * @param tags    the styled span objects to apply to the content
     *                such as android.text.style.StyleSpan
     */
    private static CharSequence apply(CharSequence[] content, Object... tags) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        openTags(text, tags);

        for (CharSequence item : content) text.append(item);

        closeTags(text, tags);
        return text;
    }

    /**
     * Iterates over an array of tags and applies them to the beginning of the specified
     * Spannable object so that future text appended to the text will have the styling
     * applied to it. Do not call this method directly.
     */
    private static void openTags(Spannable text, Object[] tags) {
        for (Object tag : tags) text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
    }

    /**
     * "Closes" the specified tags on a Spannable by updating the spans to be
     * endpoint-exclusive so that future text appended to the end will not take
     * on the same styling. Do not call this method directly.
     */
    private static void closeTags(Spannable text, Object[] tags) {
        int len = text.length();
        for (Object tag : tags) {
            if (len > 0) text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            else text.removeSpan(tag);
        }
    }

    /**
     * Returns a CharSequence that applies boldface to the concatenation
     * of the specified CharSequence objects.
     */
    private static CharSequence bold(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.BOLD));
    }

    /**
     * Returns a CharSequence that applies italics to the concatenation
     * of the specified CharSequence objects.
     */
    private static CharSequence italic(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.ITALIC));
    }

    /**
     * Returns a CharSequence that applies a foreground color to the
     * concatenation of the specified CharSequence objects.
     */
    private static CharSequence color(@ColorInt int color, CharSequence... content) {
        return apply(content, new ForegroundColorSpan(color));
    }

    private static CharSequence underline(CharSequence... content) {
        return apply(content, new UnderlineSpan());
    }

    private static CharSequence resize(float relativeSize, CharSequence... content) {
        return apply(content, new RelativeSizeSpan(relativeSize));
    }

    private static CharSequence click(TextView textView,
                                      Consumer<TextPaint> paintConsumer,
                                      Runnable clickAction,
                                      CharSequence... content) {
        return apply(content, new ClickableSpan() {
            public void onClick(@NonNull View widget) { clickAction.run(); }

            public void updateDrawState(@NonNull TextPaint paint) {
                paintConsumer.accept(paint);
            }
        });
    }

//    private static CharSequence drawable(Context context, @DrawableRes int drawable, CharSequence... content) {
//        return apply(content, new ImageSpan(context, drawable));
//    }

    /**
     * Version of {@link String#format(String, Object...)} that works on {@link Spanned} strings to preserve rich text formatting.
     * Both the {@code format} as well as any {@code %s args} can be Spanned and will have their formatting preserved.
     * Due to the way {@link Spannable}s work, any argument's spans will can only be included <b>once</b> in the result.
     * Any duplicates will appear as text only.
     *
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args   the list of arguments passed to the formatter. If there are
     *               more arguments than required by {@code format},
     *               additional arguments are ignored.
     * @return the formatted string (with spans).
     */
    @SuppressWarnings("WeakerAccess")
    public static SpannedString format(CharSequence format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }

    /**
     * Version of {@link String#format(Locale, String, Object...)} that works on {@link Spanned} strings to preserve rich text formatting.
     * Both the {@code format} as well as any {@code %s args} can be Spanned and will have their formatting preserved.
     * Due to the way {@link Spannable}s work, any argument's spans will can only be included <b>once</b> in the result.
     * Any duplicates will appear as text only.
     *
     * @param locale the locale to apply; {@code null} value means no localization.
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args   the list of arguments passed to the formatter.
     * @return the formatted string (with spans).
     * @see String#format(Locale, String, Object...)
     */
    @SuppressWarnings("WeakerAccess")
    public static SpannedString format(Locale locale, CharSequence format, Object... args) {
        SpannableStringBuilder out = new SpannableStringBuilder(format);

        int i = 0;
        int argAt = -1;

        while (i < out.length()) {
            Matcher m = FORMAT_SEQUENCE.matcher(out);
            if (!m.find(i)) break;
            i = m.start();
            int exprEnd = m.end();

            String argTerm = m.group(1);
            String modTerm = m.group(2);
            String typeTerm = m.group(3);

            CharSequence cookedArg;

            switch (typeTerm) {
                case "%":
                    cookedArg = "%";
                    break;
                case "n":
                    cookedArg = "\n";
                    break;
                default:
                    int argIdx;
                    switch (argTerm) {
                        case "":
                            argIdx = ++argAt;
                            break;
                        case "<":
                            argIdx = argAt;
                            break;
                        default:
                            argIdx = Integer.parseInt(argTerm.substring(0, argTerm.length() - 1)) - 1;
                            break;
                    }

                    Object argItem = args[argIdx];

                    if (typeTerm.equals("s") && argItem instanceof Spanned)
                        cookedArg = (Spanned) argItem;
                    else
                        cookedArg = String.format(locale, "%" + modTerm + typeTerm, argItem);
                    break;
            }

            out.replace(i, exprEnd, cookedArg);
            i += cookedArg.length();
        }

        return new SpannedString(out);
    }

//    /**
//     * Created by tj.dahunsi on 14.05.16.
//     * Image span centered in it's View
//     *
//     * @see <a href="http://stackoverflow.com/questions/25628258/align-text-around-imagespan-center-vertical">Stackoverflow</a>
//     */
    //    public static class CenteredImageSpan extends ImageSpan {
    //
    //         WeakReference<Drawable> mDrawableRef;
    //
    //        public CenteredImageSpan(Context context, final int drawableRes) {
    //            super(context, drawableRes);
    //        }
    //
    //        @Override
    //        public int getSize(Paint paint, CharSequence text,
    //                           int start, int end,
    //                           Paint.FontMetricsInt fm) {
    //            Drawable d = getCachedDrawable();
    //            Rect rect = d.getBounds();
    //
    //            if (fm != null) {
    //                Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
    //                // keep it the same as paint's fm
    //                fm.ascent = pfm.ascent;
    //                fm.descent = pfm.descent;
    //                fm.top = pfm.top;
    //                fm.bottom = pfm.bottom;
    //            }
    //
    //            return rect.right;
    //        }
    //
    //        @Override
    //        public void draw(@NonNull Canvas canvas, CharSequence text,
    //                         int start, int end, float x,
    //                         int top, int y, int bottom, @NonNull Paint paint) {
    //            Drawable b = getCachedDrawable();
    //            canvas.save();
    //
    //            int drawableHeight = b.getIntrinsicHeight();
    //            int fontAscent = paint.getFontMetricsInt().ascent;
    //            int fontDescent = paint.getFontMetricsInt().descent;
    //            int transY = bottom - b.getBounds().bottom +  // align bottom to bottom
    //                    (drawableHeight - fontDescent + fontAscent) / 2;  // align center to center
    //
    //            canvas.translate(x, transY);
    //            b.draw(canvas);
    //            canvas.restore();
    //        }
    //
    //        // Redefined locally because it is a  member from DynamicDrawableSpan
    //         Drawable getCachedDrawable() {
    //            WeakReference<Drawable> wr = mDrawableRef;
    //            Drawable d = null;
    //
    //            if (wr != null)
    //                d = wr.get();
    //
    //            if (d == null) {
    //                d = getDrawable();
    //                mDrawableRef = new WeakReference<>(d);
    //            }
    //
    //            return d;
    //        }
    //    }


    //    public SpanBuilder prependCenteredDrawable(@DrawableRes int drawableResource) {
//        CharSequence dummyString = "  ";
//        prepend(dummyString);
//
//        SpannableString result = new SpannableString(content);
//
//        ImageSpan centeredImageSpan = new CenteredImageSpan(context, drawableResource);
//
//        result.setSpan(centeredImageSpan, 1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//
//        content = result;
//
//        return this;
//    }
}
