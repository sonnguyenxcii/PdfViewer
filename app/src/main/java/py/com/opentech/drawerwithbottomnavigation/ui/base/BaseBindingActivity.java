package py.com.opentech.drawerwithbottomnavigation.ui.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.IntegerRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.ads.control.Admod;
import com.ads.control.funtion.AdCallback;
import com.google.android.gms.ads.InterstitialAd;


public abstract class BaseBindingActivity<T extends ViewDataBinding, V extends BaseViewModel>
        extends AppCompatActivity implements BaseFragment.Callback {

    protected static final int PREVIEW_FILE_REQUEST = 2369;
    protected static final int PERMISSION_WRITE = 2368;
    protected static final int PICK_IMAGE_REQUEST = 2367;
    protected static final int CAMERA_REQUEST = 2366;
    protected static final int TAKE_FILE_REQUEST = 2365;
    protected static final int ADD_FILE_REQUEST = 2364;
    protected static final int SCAN_REQUEST = 2363;
    protected static final int CREATE_PDF_FROM_SELECT_FILE = 2362;

    public static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";
    public static final String EXTRA_FILE_EXTENSION = "EXTRA_FILE_EXTENSION";
    public static final String EXTRA_FILE_TYPE = "EXTRA_FILE_TYPE";
    protected static final String EXTRA_PASSWORD = "EXTRA_PASSWORD";
    protected static final String EXTRA_IS_PREVIEW = "EXTRA_IS_PREVIEW";
    protected static final String EXTRA_NEED_SCAN = "EXTRA_NEED_SCAN";
    protected static final String EXTRA_DATA_CREATE_PDF = "EXTRA_DATA_CREATE_PDF";
    protected static final String EXTRA_FROM_FIRST_OPEN = "EXTRA_FROM_FIRST_OPEN";

    protected static final int RESULT_FILE_DELETED = -1111;
    public static final int RESULT_NEED_FINISH = -1112;

    protected boolean isNeedToSetTheme = true;

    private static final String TAG = "BaseBindingActivity";
    private V mViewModel;
    private T mViewDataBinding;
    protected String mCurrentPhotoPath;

//    private SweetAlertDialog mDownloadFromGgDriveDialog;
//    protected SweetAlertDialog mLoadFromLocalDialog;

    private InterstitialAd mHomeInterstitialAd;
    private InterstitialAd mDoneInterstitialAd;
    private InterstitialAd mMyPdfInterstitialAd;

    protected boolean mIsRequestFullPermission = false;
    protected int mRequestFullPermissionCode = -1000;

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    public abstract V getViewModel();

    @Override
    public void onFragmentAttached() {
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setNoActionBar();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        performDataBinding();
    }


    public void preloadViewPdfAdsIfInit() {
    }

    public void showTapFunctionAdsBeforeAction(Runnable callback) {

    }

    public void showOnePerTwoTapFunctionAdsBeforeAction(Runnable callback) {
        callback.run();
    }

    public void showHomeAdsBeforeAction(Runnable callback) {
        Admod.getInstance().forceShowInterstitial(this, mHomeInterstitialAd, new AdCallback() {
            @Override
            public void onAdClosed() {
                callback.run();
            }
        });
    }

    public void showDoneAdsBeforeAction(Runnable callback) {
        Admod.getInstance().forceShowInterstitial(this, mDoneInterstitialAd, new AdCallback() {
            @Override
            public void onAdClosed() {
                callback.run();
            }
        });
    }

    public void showMyPdfAdsBeforeAction(Runnable callback) {
        Admod.getInstance().forceShowInterstitial(this, mMyPdfInterstitialAd, new AdCallback() {
            @Override
            public void onAdClosed() {
                callback.run();
            }
        });
    }

    public void showViewPdfAdsBeforeAction(Runnable callback) {
        callback.run();
    }

    public void showBackHomeAdsBeforeAction(Runnable callback) {
        callback.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * create view component
     */
    protected abstract void initView();

    /**
     * set on-click listener for view component
     */
    protected abstract void setClick();

    public T getViewDataBinding() {
        return mViewDataBinding;
    }

    private void performDataBinding() {
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        this.mViewModel = mViewModel == null ? getViewModel() : mViewModel;
        mViewDataBinding.setLifecycleOwner(this);
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.executePendingBindings();
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void requestReadStoragePermissionsSafely(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            mIsRequestFullPermission = true;
            mRequestFullPermissionCode = requestCode;

            startActivity(intent);
        }
    }

    public void requestFullStoragePermission() {

    }

    public boolean notHaveStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
        } else {
            return (!Environment.isExternalStorageManager());
        }
    }

    @SuppressLint("RestrictedApi")
    public void setNoActionBar() {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.hide();
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setActionBar(String title, boolean isShowBackButton) {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.show();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);

            actionBar.setHomeButtonEnabled(isShowBackButton);
            actionBar.setDisplayHomeAsUpEnabled(isShowBackButton);
        }
    }


    public int getIntegerByResource(@IntegerRes int integer) {
        return getResources().getInteger(integer);
    }

    /**
     * for set full screen without action bar and navigation bar()
     */
    protected void setActivityFullScreen() {

    }

    protected void setActivityWithActionBar() {

    }

//    protected void showConverterPopup(int currentConvert) {
//        ConverterSelectDialog converterSelectDialog = new ConverterSelectDialog(this, currentConvert, new ConverterSelectDialog.ConverterSelectSubmit() {
//            @Override
//            public void updateSelection(int newSelect) {
//                if (currentConvert != newSelect) {
//                    if (newSelect == 0) {
//                        gotoActivityWithFlag(AppConstants.FLAG_IMAGE_TO_PDF);
//                        finish();
//                    } else if (newSelect == 1) {
//                        gotoActivityWithFlag(AppConstants.FLAG_WORD_TO_PDF);
//                        finish();
//                    } else {
//                        gotoActivityWithFlag(AppConstants.FLAG_EXCEL_TO_PDF);
//                        finish();
//                    }
//                }
//            }
//        });
//        converterSelectDialog.setCanceledOnTouchOutside(true);
//        converterSelectDialog.setCancelable(true);
//        converterSelectDialog.show();
//    }
}
