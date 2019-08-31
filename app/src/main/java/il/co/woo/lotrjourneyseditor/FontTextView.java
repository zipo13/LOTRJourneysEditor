package il.co.woo.lotrjourneyseditor;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class FontTextView extends AppCompatTextView {

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FontTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/MorrisRoman.ttf");
            setTypeface(tf);
        }
    }
}
