package il.co.woo.lotrjourneyseditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import il.co.woo.lotrjourneyseditor.campaign.CampaignManager;
import il.co.woo.lotrjourneyseditor.campaign.data.Utils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int INFLATED_PANELS_BASE_ID = 5478126;
    private static final String HERO_IMG_NAME_PREFIX = "hero_";
    private static final String DRAWABLE_TYPE = "drawable";

    private static final int HEROES_IMAGE_ID_BASE = 716865;

    private static final int SAVE_GAME_EDIT_REQ_CODE = 456;
    public static final String RELOAD_GAME_DATA = "reload_game_data";

    private static final String PREFS_NAME = "LOTR_prefs_file";
    private static final String PREF_SHOW_WARNING_KEY = "show_warning_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show an alert with some credits
                new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme))
                        .setTitle(getString(R.string.credits_title))
                        .setMessage(getString(R.string.credits_msg))
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, null)
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        });

        //check if we need permissions to read/write on this device
        //if we cannot obtain them close the app as there is nothing to be done without them
        if (Utils.arePermissionsNeeded(this)) {
            Utils.checkReadWritePermissions(this);
        } else {
            loadSavedGamesDetails();
        }

        showWarningMsgDlg();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //The dynamic panels overlap the info button so bring it to front when the activity is loaded
        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.bringToFront();
    }

    //show a warning message to the user about this app being experimental
    private void showWarningMsgDlg() {
        //Show a warning message only if the user has asked for it
        final SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (preferences.getBoolean(PREF_SHOW_WARNING_KEY, getResources().getBoolean(R.bool.show_warning_def_value))) {

            View checkBoxView = View.inflate(this, R.layout.alert_checkbox, null);
            CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
            checkBox.setText(R.string.dont_show_again);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                //check if the user doesn't want to see ths message again
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PREF_SHOW_WARNING_KEY, !isChecked);
                    editor.apply();
                }
            });

            //show an alert dialog to warn the user about this app being experimental
            new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme))
                    .setTitle(getString(R.string.warning_title))
                    .setMessage(getString(R.string.app_warning_msg))
                    .setView(checkBoxView)
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(R.string.i_understand, null)
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    //Check for external storage write permissions.
    //If non are given do not bother with data loading as it will fail
    //just close the app with an error message
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Enter");
        // If request is cancelled, the result arrays are empty.
        if (requestCode == Utils.PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                loadSavedGamesDetails();
            } else {
                // permission denied as the user did not give us permission to read the saved games
                //give an alert explaining why the app will close now.
                new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme))
                        .setTitle(getString(R.string.permission_request))
                        .setMessage(getString(R.string.permission_denied_msg))
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        }
    }

    //Load all the saved game details from the device.
    private void loadSavedGamesDetails() {
        Log.d(TAG, "loadSavedGamesDetails: Enter");

        //populate some text views with information about locating the LOTR JIME app
        Button ffgLinkButton = findViewById(R.id.ffg_jime_link_button);
        if (Utils.lotrAppInstalled(this)) {
            findViewById(R.id.app_installed_status).setVisibility(View.GONE);
            ffgLinkButton.setVisibility(View.GONE);

        } else {
            //the app was not located so there is nothing to load.
            findViewById(R.id.edit_save_game).setVisibility(View.GONE);
            ffgLinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Utils.LOTR_PKG_NAME)));
                }
            });
            return;
        }


        //calculate the time
        int iNumberOfSavedGames = Utils.getNumberOfSavedGames();
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
        dateFormat.setTimeZone(TimeZone.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        //add saved game panels to the main layout as they are loaded dynamically
        ConstraintLayout mainView = findViewById(R.id.main_container);
        int lastId = R.id.edit_save_game;
        for (int i = 0; i < iNumberOfSavedGames; i++) {

            LayoutInflater inflater = LayoutInflater.from(this);

            //to get the MainLayout
            //View view = inflater.inflate(R.layout.save_game_panel, null);
            View inflatedLayout = inflater.inflate(R.layout.save_game_panel, mainView, false);
            inflatedLayout.setId(INFLATED_PANELS_BASE_ID + i);
            mainView.addView(inflatedLayout);
            //add constraints to each generated panel to put one under the other
            ConstraintSet set = new ConstraintSet();
            set.clone(mainView);
            set.connect(inflatedLayout.getId(), ConstraintSet.LEFT, mainView.getId(), ConstraintSet.LEFT, Utils.convertDpToPixel(8, this));
            set.connect(inflatedLayout.getId(), ConstraintSet.RIGHT, mainView.getId(), ConstraintSet.RIGHT, Utils.convertDpToPixel(8, this));
            set.connect(inflatedLayout.getId(), ConstraintSet.TOP, lastId, ConstraintSet.BOTTOM, Utils.convertDpToPixel(8, this));
            set.applyTo(mainView);

            lastId = inflatedLayout.getId();

            TextView tvDate = inflatedLayout.findViewById(R.id.save_date);
            TextView tvTime = inflatedLayout.findViewById(R.id.save_time);
            TextView tvDifficulty = inflatedLayout.findViewById(R.id.save_difficulty);
            TextView tvPartyName = inflatedLayout.findViewById(R.id.party_name);
            TextView tvChapterName = inflatedLayout.findViewById(R.id.chapter);
            TextView tvCampaignName = inflatedLayout.findViewById(R.id.campaign_name);

            //populate the filelds with loaded data
            long lDate = Utils.getSavedGameDate(i);
            Date date = new Date(lDate);
            String timeOutput = timeFormat.format(date);
            tvTime.setText(timeOutput);
            String dateOutput = dateFormat.format(date);
            tvDate.setText(dateOutput);


            GameDifficulty gameDifficulty = Utils.getSavedGameDifficulty(i);
            switch (gameDifficulty) {
                case HARD:
                    tvDifficulty.setText(R.string.game_difficulty_hard);
                    break;
                case NORMAL:
                    tvDifficulty.setText(R.string.game_difficulty_normal);
                    break;
                case ADVENTURE:
                    tvDifficulty.setText(R.string.game_difficulty_adventure);
                    break;
            }

            tvPartyName.setText(Utils.getSavedGamePartyName(i));

            int currentCampaign = Utils.getSavedGameCampaign(i);
            tvCampaignName.setText(getResources().getStringArray(R.array.campaign_names)[currentCampaign - 1]);//zero based
            int chapterNumber = CampaignManager.savedGameChapterToChapterNumber(Utils.getSavedGameChapter(i));
            String chapterName = CampaignManager.savedGameChapterToChapterName(Utils.getSavedGameChapter(i));
            tvChapterName.setText(String.format(getString(R.string.game_chapter), chapterNumber, chapterName));

            //for the heroes generate images dynamically
            int numberOfHeroes = Utils.getSavedGameNumOfHeroes(i);
            LinearLayout heroesLayout = inflatedLayout.findViewById(R.id.heroes_img_container);
            for (int j = 0; j < numberOfHeroes; j++) {
                int imgResId = getHeroResIDFromHeroIdx(this, Utils.getSavedGameHeroType(i, j));
                ImageView ivHero = createImageView(HEROES_IMAGE_ID_BASE + i * 10 + j, Utils.convertDpToPixel(40, this), Utils.convertDpToPixel(40, this), imgResId);
                heroesLayout.addView(ivHero);
                if (ivHero.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) ivHero.getLayoutParams();
                    p.setMargins(Utils.convertDpToPixel(1, this), 0, 0, 0);
                    ivHero.requestLayout();
                }
            }

            //finally add a listener to move to the editor if the user clicks on a saved game panel
            final int currentSavedGameIdx = i;
            inflatedLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent savedGameIntent = new Intent(MainActivity.this, SavedGame.class);
                    savedGameIntent.putExtra(Utils.INTENT_EXTRA_SAVE_GAME_ID_KEY, currentSavedGameIdx);
                    startActivityForResult(savedGameIntent, SAVE_GAME_EDIT_REQ_CODE);
                }
            });

        }
    }

    //when the user returns from a saved game we need to update the list with the new data.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: enter");
        if (requestCode == SAVE_GAME_EDIT_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {

                    //check to see if we need to reload the data
                    if (data.getBooleanExtra(RELOAD_GAME_DATA, false)) {
                        reloadSavedGames();
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //remove all the saved game panels from the layout
    private void clearSavedGamePanels() {
        Log.d(TAG, "clearSavedGamePanels: Enter");
        ConstraintLayout mainView = findViewById(R.id.main_container);
        int iNumberOfSavedGames = Utils.getNumberOfSavedGames();
        for (int i = 0; i < iNumberOfSavedGames; i++) {
            View view = mainView.findViewById(INFLATED_PANELS_BASE_ID + i);
            if (view != null) {
                mainView.removeView(view);
            }
        }

    }

    //reload the saved games data
    private void reloadSavedGames() {
        Log.d(TAG, "reloadSavedGames: Enter");
        Utils.clearSavedGameData();
        clearSavedGamePanels();
        loadSavedGamesDetails();
    }

    //helper function to generate a image resource id from the hero ID
    public static int getHeroResIDFromHeroIdx(Context context, int heroIdx) {
        Log.d(TAG, "getHeroResIDFromHeroIdx: Enter");
        //Generate the resource name
        String tileFileName = HERO_IMG_NAME_PREFIX + heroIdx;

        //locate the id
        return context.getResources().getIdentifier(tileFileName, DRAWABLE_TYPE, context.getPackageName());
    }

    //a helper function to create images and set a scaled image in them
    private ImageView createImageView(int newID, int width, int height, int resID) {
        //inflate an image view
        @SuppressLint("InflateParams") ImageView iv = (ImageView) LayoutInflater.from(this).inflate(R.layout.hero_image_view, null);
        //generate a new unique ID
        iv.setId(newID);

        //iv.setX(x);
        // iv.setY(y);
        scaleResIntoImageView(width, height, resID, iv);

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
