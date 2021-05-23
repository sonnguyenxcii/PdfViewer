package py.com.opentech.drawerwithbottomnavigation.ui.imagetopdf;

import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import py.com.opentech.drawerwithbottomnavigation.R;


public class DataOptionViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private TextView mNameView;
    private RadioButton mCheckBoxView;

    public DataOptionViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mCheckBoxView = itemView.findViewById(R.id.item_checkbox_view);
    }

    public void bindView(int position, String nameOption, boolean isSelected, OnDataOptionClickListener listener) {
        mNameView.setText(nameOption);

        if (isSelected) {
            mCheckBoxView.setChecked(true);
        } else {
            mCheckBoxView.setChecked(false);
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });

        mCheckBoxView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
