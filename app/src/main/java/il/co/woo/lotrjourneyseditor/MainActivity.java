package il.co.woo.lotrjourneyseditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 55151848;
    static final int INFLATED_PANELS_BASE_ID = 5478126;
    private static String HERO_IMG_NAME_PREFIX = "hero_";
    private static final String DRAWABLE_TYPE = "drawable";

    private static final int HEROES_IMAGE_ID_BASE = 716865;

/*
    BaseConfig mBaseConfig =  BaseConfig.newInstance(this)
    public static String trimEnd( String s,  String suffix) {

        if (s.endsWith(suffix)) {

            return s.substring(0, s.length() - suffix.length());

        }
        return s;
    }

    String getInternalStoragePath() {
        return trimEnd(Environment.getExternalStorageDirectory().getAbsolutePath(),"/");
    }

    boolean isPathOnSD(String path) {
        sdCardPath.isNotEmpty() && path.startsWith(sdCardPath)
    }

    fun Context.isPathOnOTG(path: String) = otgPath.isNotEmpty() && path.startsWith(otgPath)

    fun Context.needsStupidWritePermissions(path: String) = isPathOnSD(path) || isPathOnOTG(path)
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique

            return;
        }


        TextView tvAppInstalled = findViewById(R.id.app_installed_status);
        if (Utils.lotrAppInstalled(this)) {
            tvAppInstalled.setText(getString(R.string.app_located_on_device));

        } else {
            tvAppInstalled.setText(getString(R.string.app_not_located_on_device));
            return;
        }

        int iNumberOfSavedGames = Utils.getNumberOfSavedGames();
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
        dateFormat.setTimeZone(TimeZone.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        ConstraintLayout mainView = findViewById(R.id.main_container);
        int lastId = R.id.app_installed_status;
        for (int i = 0; i < iNumberOfSavedGames; i++) {

            LayoutInflater inflater = LayoutInflater.from(this);

            //to get the MainLayout
            //View view = inflater.inflate(R.layout.save_game_panel, null);
            View inflatedLayout = inflater.inflate(R.layout.save_game_panel, mainView,false);
            inflatedLayout.setId(INFLATED_PANELS_BASE_ID + i);
            mainView.addView(inflatedLayout);
            ConstraintSet set = new ConstraintSet();
            set.clone(mainView);
            set.connect(inflatedLayout.getId(),ConstraintSet.LEFT,mainView.getId(),ConstraintSet.LEFT,Utils.convertDpToPixel(8,this));
            set.connect(inflatedLayout.getId(),ConstraintSet.RIGHT,mainView.getId(),ConstraintSet.RIGHT,Utils.convertDpToPixel(8,this));
            set.connect(inflatedLayout.getId(),ConstraintSet.TOP,lastId,ConstraintSet.BOTTOM,Utils.convertDpToPixel(8,this));
            set.applyTo(mainView);

            lastId = inflatedLayout.getId();

            TextView tvDate = inflatedLayout.findViewById(R.id.save_date);
            TextView tvTime = inflatedLayout.findViewById(R.id.save_time);
            TextView tvDifficulty = inflatedLayout.findViewById(R.id.save_difficulty);
            TextView tvPartyName = inflatedLayout.findViewById(R.id.party_name);
            TextView tvChapterName = inflatedLayout.findViewById(R.id.chapter);

            long lDate = Utils.getSavedGameDate(i);
            Date date = new Date(lDate);
            String timeOutput = timeFormat.format(date);
            tvTime.setText(timeOutput);
            String dateOutput = dateFormat.format(date);
            tvDate.setText(dateOutput);


            int gameDifficulty = Utils.getSavedGameDifficulty(i);
            if (gameDifficulty == Utils.GAME_HARD)
                tvDifficulty.setText(R.string.game_difficulty_hard);
            else
                tvDifficulty.setText(R.string.game_difficulty_normal);

            tvPartyName.setText(Utils.getSavedGamePartyName(i));


            tvChapterName.setText( String.format(getString(R.string.game_chapter), Utils.getSavedGameChapter(i)));

            int numberOfHeros = Utils.getSavedGameNumOfHeroes(i);
            LinearLayout heroslayout = inflatedLayout.findViewById(R.id.heroes_img_container);
            for (int j = 0; j < numberOfHeros; j++) {
                int imgResId = getHeroResIDFromHeroIdx(this,Utils.getSavedGameHeroType(i,j));
                ImageView ivHero = createImageView(HEROES_IMAGE_ID_BASE + i*10+j,80,80,imgResId);
                heroslayout.addView(ivHero);
            }

            final int currentSavedGameIdx = i;
            inflatedLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent savedGameIntent = new Intent(MainActivity.this,SavedGame.class);
                    savedGameIntent.putExtra(Utils.INTENT_EXTRA_SAVE_GAME_ID_KEY,currentSavedGameIdx);
                    startActivity(savedGameIntent);
                }
            });

        }
    }

    public static int getHeroResIDFromHeroIdx(Context context, int heroIdx) {
        //Generate the resource name
        String tileFileName = HERO_IMG_NAME_PREFIX + heroIdx;

        //locate the id
        return context.getResources().getIdentifier(tileFileName, DRAWABLE_TYPE,context.getPackageName());
    }


    //a helper function to create images and set a scaled image in them
    private ImageView createImageView(int newID, int width, int height, int resID) {
        //inflate an image view
        @SuppressLint("InflateParams") ImageView iv = (ImageView)LayoutInflater.from(this).inflate(R.layout.hero_image_view, null);
        //generate a new unique ID
        iv.setId(newID);

        //iv.setX(x);
       // iv.setY(y);
        scaleResIntoImageView(width,height,resID,iv);

        //the width and height should also be exactly the same
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
        iv.setLayoutParams(layoutParams);
        return iv;
    }

    //this method is used to take a resource image and scale it to the needed size on the device
    private void scaleResIntoImageView(int reqWidth, int reqHeight, int resID, ImageView imageView) {
        Log.d(TAG, "scaleResIntoImageView: Enter");
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), resID);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, reqWidth, reqHeight, true);
        // Loads the resized Bitmap into an ImageView
        imageView.setImageBitmap(bMapScaled);
    }
}
