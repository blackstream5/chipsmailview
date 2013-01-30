package ui.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: BlackStream
 * Date: 30.01.13
 * Time: 17:06
  */
public class ChipsMailAddressesView extends TextView {
    private static final String TAG = "{ChipsMailAddressesView}";

    private int currentSelected = -1;
    private PopupWindow popupWindow = null;
    private TextView popupTextView;

    private ArrayList<String> mails;

    private static final int SELECTED_COLOR_BACKGROUND = Color.parseColor("#feb70f");

    public ChipsMailAddressesView(Context context) {
        super(context);
        init();
    }

    public ChipsMailAddressesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChipsMailAddressesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.mails = new ArrayList<String>();
        this.setMovementMethod(new LinkTouchMovementMethod());
        this.setFocusable(false);
    }

    public boolean add(final String email) {
        if (mails.contains(email)) return false;
        mails.add(email);
        currentSelected = -1;
        notifyMailListChanged();
        return true;
    }

    public ArrayList<String> getMailAdresses() {
        return mails;
    }

    public String getMailAdressesString() {
        return TextUtils.join(",", mails);
    }

    public void notifyMailListChanged() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i=0; i<mails.size(); i++) {
            if (i>0) builder.append(" ");
            builder.append(getSpannableObject(i, mails.get(i)));
            builder.append(" ");
        }
        this.setText(builder);
    }

    private SpannableStringBuilder getSpannableObject(final int index, final String email) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        boolean selected = (currentSelected == index) ? true : false;
        builder.append(getRawSpanText(email, selected));
        MailHintedSpan spanMail = new MailHintedSpan(index, email);
        builder.setSpan(spanMail, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private SpannableStringBuilder getRawSpanText(String text, boolean isSelected) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" "+text+" ");
        Bitmap drawable = getSpannedText(text, isSelected);
        builder.setSpan(new ImageSpan(getContext(), drawable), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private Bitmap getSpannedText(String text, boolean selected){
        String txt = strTruncateEmail(text, 12);
        TextView textview = new TextView(getContext());
        textview.setSingleLine();
        textview.setText(txt);
        textview.setTextSize(18);
        textview.setTextColor(Color.BLACK);
        int rBackground = selected ? R.drawable.background_span_mail_selected : R.drawable.background_span_mail_normal;
        textview.setBackgroundResource(rBackground);
        return convertViewToBitmap(textview);
    }

    private void showPopupHintAtPosition(View view, int line, int xpos) {
        Rect rect = new Rect();
        this.getLayout().getLineBounds(line, rect);
        int pos = -(view.getHeight()-line*rect.height())+rect.height();
        showPopupAlignTo(view, xpos, pos);
    }

    private void showPopupAlignTo(View view, int x, int y) {
        if (popupWindow == null) {
            LinearLayout popupLayout = new LinearLayout(getContext());
            popupLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            popupTextView = new TextView(getContext());
            popupTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT));
            popupTextView.setBackground(new ColorDrawable(SELECTED_COLOR_BACKGROUND));
            popupTextView.setTextColor(Color.BLACK);
            popupTextView.setText("error");
            popupTextView.setPadding(5, 3, 5, 3);
            popupLayout.addView(popupTextView);
            popupWindow = new PopupWindow( popupLayout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setTouchInterceptor(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        popupWindow.dismiss();
                        return true;
                    }
                    return false;
                }
            });
        }
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        popupWindow.showAsDropDown(view, x, y);
    }

    public void dismissPopupHint() {
        if (popupWindow != null &&  popupWindow.isShowing())
            popupWindow.dismiss();
    }

    protected Bitmap convertViewToBitmap(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(canvas);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return viewBmp;
    }

    private String strTruncateEmail(String email, int len) {
        String str = email;
        int apos = str.indexOf('@');
        if (apos>1) {
            str = str.substring(0, apos);
        }
        if (len > 3) {
            if (str.length() > len) {
                return str.substring(0, (len - 3)) + "...";
            } else {
                return str;
            }
        }
        return str;
    }

    private class LinkTouchMovementMethod extends LinkMovementMethod {

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                MailHintedSpan[] link = buffer.getSpans(off, off, MailHintedSpan.class);
                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        link[0].onClick(widget);              //////// ADDED THIS
                        link[0].onShowAtPos(widget, line, x); //////// ADDED THIS
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                    currentSelected = -1;
                    notifyMailListChanged();
                    dismissPopupHint();
                }
            }
            return super.onTouchEvent(widget, buffer, event);
        }

    }

    private class MailHintedSpan extends ClickableSpan {
        protected String email;
        protected int index;
        protected boolean flagToDelete;

        public MailHintedSpan(int index, String email) {
            this.index = index;
            this.email = email;
            this.flagToDelete = false;
        }

        @Override
        public void onClick(View view) {
            if (currentSelected != -1 && currentSelected == index) {
                mails.remove(index);
                mails.trimToSize();
                currentSelected = -1;
                flagToDelete = true;
                notifyMailListChanged();
                return;
            }
            currentSelected = index;
            notifyMailListChanged();
        }

        public void onShowAtPos(View view, int line, int xpos) {
            if (!flagToDelete) {
                try{
                    showPopupHintAtPosition(view, line, xpos);
                    popupTextView.setText(email);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            } else {
                dismissPopupHint();
            }
        };
    }
}
