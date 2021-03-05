//package py.com.opentech.drawerwithbottomnavigation.ui.base;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
//import android.graphics.drawable.Drawable;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.SystemClock;
//import android.text.TextUtils;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.CallSuper;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.app.AppCompatDelegate;
//import androidx.appcompat.widget.AppCompatImageView;
//import androidx.appcompat.widget.AppCompatTextView;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Hashtable;
//
//import butterknife.ButterKnife;
//import io.reactivex.annotations.NonNull;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.disposables.Disposable;
//import vn.finhay.finhay.PersionalInformationActivity;
//import vn.finhay.finhay.R;
//import vn.finhay.finhay.api.ApiService;
//import vn.finhay.finhay.api.ApiService2;
//import vn.finhay.finhay.api.GatewayApiService;
//import vn.finhay.finhay.application.FinhayApplication;
//import vn.finhay.finhay.database.ApplicationDataBase;
//import vn.finhay.finhay.screens.pinCode.PinActivity;
//import vn.finhay.finhay.screens.splashScreen.SplashScreen;
//import vn.finhay.finhay.screens.userInfoInput.ImagePickerActivity;
//import vn.finhay.finhay.screens.userInfoInput.UpdateIdentifyInfoNewActivity;
//import vn.finhay.finhay.utilsApp.Debug;
//import vn.finhay.finhay.utilsApp.OnSingleClickListener;
//import vn.finhay.finhay.utilsApp.SharedPrefsUtils;
//
//import static vn.finhay.finhay.utilsApp.Constants.LAST_TIME_CHEESEPARINGS;
//import static vn.finhay.finhay.utilsApp.Constants.LAST_TIME_CUULONG;
//import static vn.finhay.finhay.utilsApp.Constants.LAST_TIME_INVESTENT;
//import static vn.finhay.finhay.utilsApp.Constants.LAST_TIME_PROTECT;
//
////import com.uxcam.UXCam;
//
///**
// * Created by huypd on 5/6/2018.
// */
//
//public abstract class BaseActivity extends AppCompatActivity {
//
//    static {
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//    }
//
//    public long mLastClickTime = 0;
//    public Hashtable<String, String> calendarIdTable;
//    public String TAG = this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().length() > 23 ? 22 : this.getClass().getSimpleName().length() - 1);
//    public GatewayApiService gatewayApiService;
//    public ApiService apiService;
//    public ApiService2 apiService2;
//    public SharedPreferences mSecurePrefs;
//    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
//    protected FinhayApplication application;
//    private Handler mhandler;
//    private Runnable runnable;
//    private ProgressDialog mProgressDialog;
//    public ApplicationDataBase applicationDataBase;
//
//    public static void hideSoftKeyboard(Activity activity) {
//        try {
//            InputMethodManager inputMethodManager =
//                    (InputMethodManager) activity.getSystemService(
//                            Activity.INPUT_METHOD_SERVICE);
//
//            inputMethodManager.hideSoftInputFromWindow(
//                    activity.getCurrentFocus().getWindowToken(), 0);
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//
//    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        application = FinhayApplication.create(this);
//        applicationDataBase = ApplicationDataBase.Companion.getInstance(this);
////        System.out.println("application.getGlobal().isRegistedEventBus()-------------"+);
////        if (!application.getGlobal().isRegistedEventBus()) {
////
////            EventBus.getDefault().register(this);
////            application.getGlobal().setRegistedEventBus(true);
////        }
//        application.wasInBackground.observe(this, aBoolean -> {
//            if (!aBoolean) {
//                if (application.getGlobal().persionalInformation.getValue() == null) {
//                    Intent intent = new Intent(this, SplashScreen.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    return;
//                }
//                if (!(this instanceof PinActivity) && !(this instanceof SplashScreen) && !(this instanceof ImagePickerActivity) && !(this instanceof UpdateIdentifyInfoNewActivity)) {
//                    Debug.e("---  pinCodeTime " + (SystemClock.elapsedRealtime() - application.pinCodeTime));
//                    if (application.getGlobal().isHasPin() && (SystemClock.elapsedRealtime() - application.pinCodeTime > 30000)) {
//                        Intent intent = new Intent(this, PinActivity.class);
//                        intent.putExtra("caller", "application");
//                        startActivity(intent);
//                    }
//                }
//            }
//        });
//        gatewayApiService = application.getGatewayApi();
//        apiService2 = application.getApiServiceV2();
//        apiService = application.getApiService();
//        mSecurePrefs = application.getSharedPreferences();
//
//        adjustFontScale(getResources().getConfiguration());
//        setContentView(getContentView());
//        ButterKnife.bind(this);
//        onViewReady(savedInstanceState, getIntent());
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
////        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
////                WindowManager.LayoutParams.FLAG_SECURE);
////        UXCam.startWithKey("5k8tol8m98n39or");
//
//
//    }
//
//    @CallSuper
//    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
//        //To be used by child activities
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    public void setupUI(View view) {
//
//        // Set up touch listener for non-text box views to hide keyboard.
//        if (!(view instanceof EditText)) {
//            view.setOnTouchListener((v, event) -> {
//                hideSoftKeyboard(vn.finhay.finhay.Base.customView.BaseActivity.this);
//                return false;
//            });
//        }
//
//        //If a layout container, iterate over children and seed recursion.
//        if (view instanceof ViewGroup) {
//            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
//                View innerView = ((ViewGroup) view).getChildAt(i);
//                setupUI(innerView);
//            }
//        }
//    }
//
//    public String getPortfolioFromIntent() {
//        return getIntent().getStringExtra("portfolio") == null ? "0" : getIntent().getStringExtra("portfolio");
//    }
//
//    public boolean isHdtg() {
//        return getIntent().getBooleanExtra("hdtg", false);
//    }
//
//    public boolean isCd3m() {
//        return getIntent().getBooleanExtra("cd3m", false);
//    }
//
//
//    public String getCode() {
//        return getIntent().getStringExtra("code") == null ? "" : getIntent().getStringExtra("code");
//    }
//
//    @Override
//    protected void onDestroy() {
//        compositeDisposable.clear();
////        if (application.getGlobal().isRegistedEventBus()) {
////            application.getGlobal().setRegistedEventBus(false);
////            EventBus.getDefault().unregister(this);
////        }
//        super.onDestroy();
//    }
//
////    @Subscribe
////    public void onEvent(NotificationEvent event) {
////        Debug.e("---  event.getType() " + event.getType());
////        System.out.println("---BaseActivity--------------onEvent-------------" + event.getType());
////
////        if (event.getType().equals("12")) {
////            System.out.println("---BaseActivity--------------onEvent------------1-" + event.getType());
////
////            application.showInvitedSuccessDialog();
////        }
////
////    }
//
//    protected void hideKeyboard() {
//        try {
//            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            if (getCurrentFocus() != null)
//                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        } catch (Exception e) {
//            Log.e("MultiBackStack", "Failed to add fragment to back stack", e);
//        }
//    }
//
//    public void setTitleCus(String title2) {
//        setTitle(title2);
//
//    }
//
//    protected void showBackArrow() {
//        ActionBar supportActionBar = getSupportActionBar();
//        if (supportActionBar != null) {
//            supportActionBar.setDisplayHomeAsUpEnabled(true);
//            supportActionBar.setDisplayShowHomeEnabled(true);
//            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_gray);
//        }
//        greenStatusBar(false);
//    }
//
//    public void showProgressDialog(String title, @NonNull String message) {
//        if (mhandler == null) {
//            mhandler = new Handler();
//        }
//        if (runnable == null) {
//            runnable = () -> {
//                try {
//                    if (isFinishing()) {
//                        return;
//                    }
//                    if (mProgressDialog == null) {
//                        mProgressDialog = CustomProgressDialog.ctor(this, "Loading");
//                        mProgressDialog.setCancelable(false);
//                        mProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                    }
//                    if (!mProgressDialog.isShowing()) {
//                        mProgressDialog.setMessage(message);
//                        mProgressDialog.show();
//                    }
//                } catch (Exception e) {
//                    Debug.e("---  Exception BaseActivity " + e.getMessage());
//                }
//            };
//        }
//        mhandler.postDelayed(runnable, 0);
//    }
//
//    public void showProgressDialog() {
//        if (mhandler == null) {
//            mhandler = new Handler();
//        }
//        if (runnable == null) {
//            runnable = () -> {
//                try {
//                    if (isFinishing()) {
//                        return;
//                    }
//                    if (mProgressDialog == null) {
//                        mProgressDialog = CustomProgressDialog.ctor(this, "Loading");
//                        mProgressDialog.setCancelable(false);
//                        mProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                    }
//                    if (!mProgressDialog.isShowing()) {
//                        mProgressDialog.setMessage("");
//                        mProgressDialog.show();
//                    }
//                } catch (Exception e) {
//                    Debug.e("---  Exception BaseActivity " + e.getMessage());
//                }
//            };
//        }
//        mhandler.postDelayed(runnable, 0);
//    }
//
//
//    public void hideDialog() {
//
////        if (isDestroyed()) { // or call isFinishing() if min sdk version < 17
////            return;
////        }
//
////        if (mProgressDialog != null && mProgressDialog.isShowing()) {
//        try { // tam de the nay vi 1 so th điều kiện trên không work
//            if (mProgressDialog != null) {
//                mProgressDialog.dismiss();
//            }
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
////        }
//    }
//
//    public void showAlertDialog(String msg, final Runnable runnable) {
//        if (isFinishing())
//            return;
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//            dialogBuilder.setTitle(null);
//            dialogBuilder.setIcon(R.mipmap.ic_launcher);
//            dialogBuilder.setMessage(msg);
//            dialogBuilder.setPositiveButton("OK", (dialog, which) -> runnable.run());
//
//            dialogBuilder.setCancelable(false);
//            dialogBuilder.show();
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//    }
//
//    public void showAlertDialog(String title, String msg, String positive, String negative, final Runnable runnable) {
//        if (isFinishing()) {
//            return;
//        }
//
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//        dialogBuilder.setTitle(title);
//        dialogBuilder.setIcon(null);
//        dialogBuilder.setMessage(msg);
//        dialogBuilder.setPositiveButton(TextUtils.isEmpty(positive) ? "OK" : positive, (dialog, which) -> runnable.run()
//        );
//        dialogBuilder.setNegativeButton(TextUtils.isEmpty(negative) ? "HỦY" : negative, (dialog, which) -> dialog.cancel()
//        );
//
//
//        dialogBuilder.setCancelable(false);
//        dialogBuilder.show();
//
//    }
//
//    protected void showDeleteAllAlertDialog(String tittle, String msg, final Runnable runnable) {
//
//        if (isFinishing())
//            return;
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//            dialogBuilder.setTitle(tittle);
//            dialogBuilder.setIcon(R.mipmap.ic_launcher);
//            dialogBuilder.setMessage(msg);
//            dialogBuilder.setPositiveButton("XÁC NHẬN", (dialog, which) -> runnable.run());
//            dialogBuilder.setNegativeButton("HỦY", null);
//
//            dialogBuilder.setCancelable(false);
//
//            AlertDialog dialog = dialogBuilder.create();
//
//            //2. now setup to change color of the button
//            dialog.setOnShowListener(arg0 -> {
//                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorHint));
//                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
//            });
//            dialog.show();
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//    }
//
//    protected void showConfirmDialog(String title, String msg, final Runnable runnable) {
//        if (isFinishing())
//            return;
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//            dialogBuilder.setTitle(title);
//            dialogBuilder.setIcon(R.mipmap.ic_launcher);
//            dialogBuilder.setMessage(msg);
//            dialogBuilder.setPositiveButton("Xác nhận", (dialog, which) -> {
//                runnable.run();
//                dialog.dismiss();
//            });
//
//            dialogBuilder.setNegativeButton("Hủy", null);
//            dialogBuilder.show();
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//    }
//
//    protected void showDialog(String title, String msg, final Runnable runnablePositive, final Runnable runnableNegative) {
//        if (isFinishing())
//            return;
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//            dialogBuilder.setTitle(title);
//            dialogBuilder.setIcon(R.mipmap.ic_launcher);
//            dialogBuilder.setMessage(msg);
//            dialogBuilder.setPositiveButton("Xác nhận", (dialog, which) -> {
//                runnablePositive.run();
//                dialog.dismiss();
//            });
//
//            dialogBuilder.setNegativeButton("Hủy", (dialog, which) -> {
//                runnableNegative.run();
//                dialog.dismiss();
//            });
//            dialogBuilder.show();
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//    }
//
//    protected void showConfirmDialogHdtg(String title, String msg, final Runnable runnable) {
//        if (isFinishing())
//            return;
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//            dialogBuilder.setTitle(title);
//            dialogBuilder.setIcon(R.mipmap.ic_launcher);
//            dialogBuilder.setMessage(msg);
//            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
//                runnable.run();
//                dialog.dismiss();
//            });
//
//            dialogBuilder.setNegativeButton(android.R.string.no, null);
//            dialogBuilder.show();
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//    }
//
//    public void showAlertDialog(String msg) {
//        if (isFinishing())
//            return;
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//            dialogBuilder.setTitle(null);
//            dialogBuilder.setIcon(R.mipmap.ic_launcher);
//            dialogBuilder.setMessage(msg);
//            dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
//
//            dialogBuilder.setCancelable(false);
//            dialogBuilder.show();
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//    }
//
//    protected void showAlertDialogPass(String msg) {
//        if (isFinishing())
//            return;
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//            dialogBuilder.setTitle(null);
//            dialogBuilder.setIcon(R.mipmap.ic_launcher);
//            dialogBuilder.setMessage(msg);
//            dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
//
//            dialogBuilder.setCancelable(false);
//            dialogBuilder.show();
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//    }
//
//    protected void showToast(String mToastMsg) {
//        Toast.makeText(this, mToastMsg, Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {// app icon in action bar clicked; go home
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    protected abstract int getContentView();
//
//    /**
//     * onResume override for all activities.
//     * Check if the application have been put into the background or not.
//     * If it is then throw up PIN screen if have PIN enabled.
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    public void greenStatusBar(boolean flag) {
//        greenStatusBar(flag, R.color.colorPrimary);
//    }
//
//    public void greenStatusBar(boolean flag, int color) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Window window = getWindow();
//            // finally change the color
//            if (flag) {
//                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
//                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
////                window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
//                window.setStatusBarColor(ContextCompat.getColor(this, color));
//            } else {
//                window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            }
//        }
//    }
//
//    public void setStatusBarGradiant(int gradient) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            Drawable background = getResources().getDrawable(gradient);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
////            window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
//            window.setBackgroundDrawable(background);
//        }
//    }
//
//    /**
//     * Set the color of the status bar
//     *
//     * @param lightDark true for white icons, false for black icons
//     * @param color
//     */
//    public void setStatusBarColor(boolean lightDark, int color) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Window window = getWindow();
//            // finally change the color
//            window.setStatusBarColor(ContextCompat.getColor(this, color));
//            if (lightDark) {
//                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            } else {
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            }
//        }
//    }
//
//    public void addDisposible(Disposable disposable) {
//        compositeDisposable.add(disposable);
//    }
//
//    public CompositeDisposable getCompositeDisposable() {
//        return compositeDisposable;
//    }
//
//    public void adjustFontScale(Configuration configuration) {
////        if (configuration.fontScale > 1.0) {
////            LogUtil.log(LogUtil.WARN, TAG, "fontScale=" + configuration.fontScale); //Custom Log class, you can use Log.w
////            LogUtil.log(LogUtil.WARN, TAG, "font too big. scale down..."); //Custom Log class, you can use Log.w
//        configuration.fontScale = (float) 1.0;
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//        wm.getDefaultDisplay().getMetrics(metrics);
//        metrics.scaledDensity = configuration.fontScale * metrics.density;
//        getBaseContext().getResources().updateConfiguration(configuration, metrics);
////        }
//    }
//
//    public void onChangeFragment(Fragment fragment, String tag) {
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (!TextUtils.isEmpty(tag)) {
//            transaction.replace(R.id.container, fragment, tag).addToBackStack(tag);
//        } else {
//            transaction.replace(R.id.container, fragment);
//        }
//        try {
//            transaction.commit();
//        } catch (IllegalStateException ex) {
//            transaction.commitAllowingStateLoss();
//        }
//    }
//
//    public void getCurrentTimeInvestment() {
//        String pattern = "yyyy-MM-dd HH:mm:ss";
//        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(pattern);
//        Date today = Calendar.getInstance().getTime();
//        String todayAsString = df.format(today);
//        SharedPrefsUtils.setStringPreference(this, LAST_TIME_INVESTENT, todayAsString);
//    }
//
//    public void getCurrentTimeCuulong() {
//        String pattern = "yyyy-MM-dd HH:mm:ss";
//        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(pattern);
//        Date today = Calendar.getInstance().getTime();
//        String todayAsString = df.format(today);
//        SharedPrefsUtils.setStringPreference(this, LAST_TIME_CUULONG, todayAsString);
//    }
//
//    public void getCurrentTimeCheeseparing() {
//        String pattern = "yyyy-MM-dd HH:mm:ss";
//        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(pattern);
//        Date today = Calendar.getInstance().getTime();
//        String todayAsString = df.format(today);
//        SharedPrefsUtils.setStringPreference(this, LAST_TIME_CHEESEPARINGS, todayAsString);
//    }
//
//    public void getCurrentTimeProtect() {
//        String pattern = "yyyy-MM-dd HH:mm:ss";
//        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(pattern);
//        Date today = Calendar.getInstance().getTime();
//        String todayAsString = df.format(today);
//        SharedPrefsUtils.setStringPreference(this, LAST_TIME_PROTECT, todayAsString);
//    }
//
//    public void onclickIconBack(AppCompatImageView appCompatImageView) {
//        appCompatImageView.setOnClickListener(view -> finish());
//    }
//
//    public void saveValue(String key, String value) {
//        if (mSecurePrefs == null || value == null) {
//            return;
//        }
//
//        try {
//            final SharedPreferences.Editor secureEditor = mSecurePrefs.edit();
//            secureEditor.putString(key, value);
//            secureEditor.apply();
//        } catch (Exception e) {
//            Debug.e("---  Exception BaseActivity " + e.getMessage());
//        }
//
//    }
//
//    public void logException(Exception exception) {
//        Log.e("---  Exception: ", exception.getMessage());
//    }
//
//    protected void showVerifyRequireDialog(String title, String content) {
//        androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
//        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_hdtg_verify_require, null);
//        dialogBuilder.setView(dialogView);
//        AppCompatTextView txtXacthuc = (dialogView).findViewById(R.id.txt_xacthucngay);
//        AppCompatTextView header = (dialogView).findViewById(R.id.infoHeader1);
//        AppCompatTextView txtContent = (dialogView).findViewById(R.id.infoBody1);
//        header.setText(title);
//        txtContent.setText(content);
//        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
//        alertDialog.setCancelable(true);
//        alertDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
//        alertDialog.show();
//
//        txtXacthuc.setOnClickListener(new OnSingleClickListener() {
//            @Override
//            public void onSingleClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), PersionalInformationActivity.class);
//                startActivity(intent);
//                alertDialog.dismiss();
//            }
//        });
//    }
//}
