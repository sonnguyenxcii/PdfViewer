package py.com.opentech.drawerwithbottomnavigation.ui.fileexplorer;

import android.app.Application;
import android.content.Context;

public class ApplicationLoader extends Application{
   
	 public static volatile Context applicationContext = null;
	
	
	@Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
    }
}
