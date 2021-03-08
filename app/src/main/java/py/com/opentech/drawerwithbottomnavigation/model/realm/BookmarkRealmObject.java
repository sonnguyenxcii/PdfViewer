package py.com.opentech.drawerwithbottomnavigation.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BookmarkRealmObject extends RealmObject {
    @PrimaryKey
    public long id;
    public String path;
}
