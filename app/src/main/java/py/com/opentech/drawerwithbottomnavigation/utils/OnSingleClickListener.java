package py.com.opentech.drawerwithbottomnavigation.utils;

import android.os.SystemClock;
import android.view.View;

/**
 * Thay thế onClickListener để tránh double click.
 * Copy paste từ StackOverflow: https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button
 *
 */
public abstract class OnSingleClickListener implements View.OnClickListener {
    /**
     * Thời gian giữa các lần click
     */
    private static final long MIN_CLICK_INTERVAL=1000;
    /**
     * Lưu thời điểm click cuối cùng
     */
    private long mLastClickTime;

    /**
     * Hàm sử dụng thay onClickListener khi add vào view
     * @param v The view that was clicked.
     */
    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime=SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;
        mLastClickTime=currentClickTime;

        if(elapsedTime<=MIN_CLICK_INTERVAL)
            return;

        onSingleClick(v);
    }

}
