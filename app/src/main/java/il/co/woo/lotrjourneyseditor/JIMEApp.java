package il.co.woo.lotrjourneyseditor;

import android.app.Application;
import android.content.res.Resources;

public class JIMEApp extends Application {
    private static Resources res;


    @Override
    public void onCreate() {
        super.onCreate();
        res = getResources();
    }

    public static Resources getRes() {
        return res;
    }

}