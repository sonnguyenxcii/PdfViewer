package py.com.opentech.drawerwithbottomnavigation.ui.fileexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ads.control.Admod;

import java.util.ArrayList;

import py.com.opentech.drawerwithbottomnavigation.R;
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity;
import py.com.opentech.drawerwithbottomnavigation.utils.Constants;

public class FileExplorerActivity extends AppCompatActivity {

    //    private Toolbar toolbar;
    private FragmentManager fragmentManager = null;
    private FragmentTransaction fragmentTransaction = null;
    private DirectoryFragment mDirectoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("File Management");

        Admod.getInstance().loadSmallNative(this, Constants.ADMOB_Native_File_Management);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        mDirectoryFragment = new DirectoryFragment();
        mDirectoryFragment.setDelegate(new DirectoryFragment.DocumentSelectActivityDelegate() {

            @Override
            public void startDocumentSelectActivity() {

            }

            @Override
            public void didSelectFiles(DirectoryFragment activity,
                                       ArrayList<String> files) {
                gotoViewPdf(files.get(0));
            }

            @Override
            public void updateToolBarName(String name) {

            }
        });
        fragmentTransaction.add(R.id.fragment_container, mDirectoryFragment, "" + mDirectoryFragment.toString());
        fragmentTransaction.commit();

    }

    void gotoViewPdf(String path) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("url", path);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        mDirectoryFragment.onFragmentDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDirectoryFragment.onBackPressed_()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
