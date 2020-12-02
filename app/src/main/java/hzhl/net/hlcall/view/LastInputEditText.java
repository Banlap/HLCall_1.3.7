package hzhl.net.hlcall.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by guang on 2019/8/15.
 */

public class LastInputEditText extends AppCompatEditText {
    public LastInputEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSelection(getText().length());
    }

    public LastInputEditText(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }

    public LastInputEditText(Context context) {
        this(context, null);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //光标首次获取焦点是在最后面，之后操作就是按照点击的位置移动光标
        if (isEnabled() && hasFocus() && hasFocusable()) {
            setSelection(selEnd);
        } else {
            setSelection(getText().length());
        }

    }
}
