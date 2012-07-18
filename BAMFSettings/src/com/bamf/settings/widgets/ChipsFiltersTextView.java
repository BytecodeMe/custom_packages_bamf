package com.bamf.settings.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.util.Rfc822Tokenizer;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView.Validator;

import com.android.ex.chips.RecipientEditTextView;
import com.bamf.settings.R;

public class ChipsFiltersTextView extends RecipientEditTextView {
    /** A noop validator that does not munge invalid texts. */
    private class ForwardValidator implements Validator {
        private Validator mValidator = null;

        public CharSequence fixText(CharSequence invalidText) {
            return invalidText;
        }

        public boolean isValid(CharSequence text) {
            return mValidator != null ? mValidator.isValid(text) : true;
        }

        public void setValidator(Validator validator) {
            mValidator = validator;
        }
    }

    private final ForwardValidator mInternalValidator = new ForwardValidator();

    public ChipsFiltersTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setValidator(mInternalValidator);
        setTokenizer(new Rfc822Tokenizer());
        Resources r = context.getResources();
        Bitmap def = BitmapFactory.decodeResource(r, R.drawable.chip_delete);
//        setChipDimensions(
//                r.getDrawable(R.drawable.chip_bamf_edit),
//                r.getDrawable(R.drawable.chip_background_selected),
//                r.getDrawable(R.drawable.chip_background_invalid),
//                r.getDrawable(R.drawable.chip_delete), def, R.layout.more_item,
//                R.layout.chips_alternate_item,
//                        r.getDimension(R.dimen.chip_height),
//                        r.getDimension(R.dimen.chip_padding),
//                        r.getDimension(R.dimen.chip_text_size),
//                        R.layout.copy_chip_dialog_layout);
    }

    @Override
    public void setValidator(Validator validator) {
        mInternalValidator.setValidator(validator);
    }
}
