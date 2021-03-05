package py.com.opentech.drawerwithbottomnavigation.ui.pdf;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import py.com.opentech.drawerwithbottomnavigation.R;


/**
 * Shows the terms and agreements. Simply calls a webview.
 */
public class PdfViewerActivity extends AppCompatActivity {

    @BindView(R.id.pdfView)
    PDFView pdfView;

    @BindView(R.id.progress)
    ProgressBar progress;

//    @BindView(R.id.toolbar)
//    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String url = getIntent().getExtras().getString("url");

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
//                    InputStream input = new URL(url).openStream();
                    pdfView.fromUri(Uri.parse(url))
//                    pdfView.fromStream(input)
                            .enableSwipe(true) // allows to block changing pages using swipe
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .defaultPage(0)
                            .onPageChange((page, pageCount) -> {
                                Toast.makeText(PdfViewerActivity.this, "Trang " + (page + 1) + "/" + pageCount, Toast.LENGTH_SHORT).show();
                            })
                            // allows to draw something on the current page, usually visible in the middle of the screen
                            // allows to draw something on all pages, separately for every page. Called only for visible pages
                            .onLoad(new OnLoadCompleteListener() {
                                @Override
                                public void loadComplete(int nbPages) {
                                    progress.setVisibility(View.GONE);
                                }
                            }) // called after document is loaded and starts to be rendered
//                            .nightMode(true)
                            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                            .password(null)
                            .scrollHandle(null)
                            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                            .spacing(10)
                            .load();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        thread.start();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
