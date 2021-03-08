package py.com.opentech.drawerwithbottomnavigation;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import py.com.opentech.drawerwithbottomnavigation.model.realm.BookmarkRealmObject;
import py.com.opentech.drawerwithbottomnavigation.model.realm.RecentRealmObject;
import py.com.opentech.drawerwithbottomnavigation.utils.Globals;

public class PdfApplication extends Application {
    private Globals instance;
    public static AtomicLong bookmarkPrimaryKey;
    public static AtomicLong recentPrimaryKey;

    @Override
    public void onCreate() {
        super.onCreate();
        initRealm();
    }

    public synchronized Globals getGlobal() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }

    public static PdfApplication create(Context context) {

        return PdfApplication.get(context);
    }

    private static PdfApplication get(Context context) {
        return (PdfApplication) context.getApplicationContext();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("pdfviewer.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        Realm realm = Realm.getInstance(config);

        try {
            //Attempt to get the last id of the last entry in the Quote class and use that as the
            //Starting point of your primary key. If your Quote table is not created yet, then this
            //attempt will fail, and then in the catch clause you want to create a table
            bookmarkPrimaryKey = new AtomicLong(realm.where(BookmarkRealmObject.class).max("id").longValue() + 1);
        } catch (Exception e) {
            //All write transaction should happen within a transaction, this code block
            //Should only be called the first time your app runs
            realm.beginTransaction();
            //Create temp Quote so as to create the table
            BookmarkRealmObject quote = realm.createObject(BookmarkRealmObject.class, 0);
            //Now set the primary key again
            bookmarkPrimaryKey = new AtomicLong(realm.where(BookmarkRealmObject.class).max("id").longValue() + 1);
            //remove temp quote
            RealmResults<BookmarkRealmObject> results = realm.where(BookmarkRealmObject.class).equalTo("id", 0).findAll();
            results.deleteAllFromRealm();
            realm.commitTransaction();
        }
        try {
            //Attempt to get the last id of the last entry in the Quote class and use that as the
            //Starting point of your primary key. If your Quote table is not created yet, then this
            //attempt will fail, and then in the catch clause you want to create a table
            recentPrimaryKey = new AtomicLong(realm.where(RecentRealmObject.class).max("id").longValue() + 1);
        } catch (Exception e) {
            //All write transaction should happen within a transaction, this code block
            //Should only be called the first time your app runs
            realm.beginTransaction();
            //Create temp Quote so as to create the table
            RecentRealmObject quote = realm.createObject(RecentRealmObject.class, 0);
            //Now set the primary key again
            recentPrimaryKey = new AtomicLong(realm.where(RecentRealmObject.class).max("id").longValue() + 1);
            //remove temp quote
            RealmResults<RecentRealmObject> results = realm.where(RecentRealmObject.class).equalTo("id", 0).findAll();
            results.deleteAllFromRealm();
            realm.commitTransaction();
        }
    }
}
