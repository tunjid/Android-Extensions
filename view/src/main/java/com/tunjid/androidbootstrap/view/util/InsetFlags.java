package com.tunjid.androidbootstrap.view.util;

@SuppressWarnings("unused")
public class InsetFlags {

    public static final InsetFlags ALL = new InsetFlags(true, true, true, true);
    public static final InsetFlags NO_TOP = new InsetFlags(true, false, true, true);
    public static final InsetFlags NONE = new InsetFlags(false, false, false, false);
    public static final InsetFlags BOTTOM = new InsetFlags(false, false, false, true);
    public static final InsetFlags VERTICAL = new InsetFlags(false, true, false, true);
    public static final InsetFlags HORIZONTAL = new InsetFlags(true, false, true, true);

    private boolean topInset;
    private boolean leftInset;
    private boolean rightInset;
    private boolean bottomInset;

    @SuppressWarnings("unused")
    public static InsetFlags create(boolean hasLeftInset, boolean hasTopInset, boolean hasRightInset, boolean hasBottomInset) {
        return new InsetFlags(hasLeftInset, hasTopInset, hasRightInset, hasBottomInset);
    }

    private InsetFlags(boolean leftInset, boolean topInset, boolean rightInset, boolean bottomInset) {
        this.topInset = topInset;
        this.leftInset = leftInset;
        this.rightInset = rightInset;
        this.bottomInset = bottomInset;
    }

    public boolean hasTopInset() { return topInset; }

    public boolean hasLeftInset() { return leftInset; }

    public boolean hasRightInset() { return rightInset; }

    public boolean hasBottomInset() { return bottomInset; }
}
