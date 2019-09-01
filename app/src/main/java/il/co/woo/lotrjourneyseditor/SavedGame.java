package il.co.woo.lotrjourneyseditor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SavedGame extends AppCompatActivity implements View.OnClickListener, android.text.TextWatcher {

    private static final String TAG = "SavedGame";
    private final int INFLATED_PANELS_BASE_ID = 128754;
    private int mSaveGameIdx = -1;
    //it is possible to edit a saved game before the first chapter was started
    //in that case the hero data is not saved yet and is empty in the save game file
    //in this case do not let the user to edit it.
    private boolean[] mHeroDataReady = new boolean[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_game);
        setTitle(R.string.edit_save_game_title);


        //the extras should hold the save game id
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }

        Spinner spChapters = findViewById(R.id.current_chapter);
        Spinner spDifficulty = findViewById(R.id.game_difficulty);
        for (int i = 0; i < mHeroDataReady.length; i++) {
            mHeroDataReady[i] = false;
        }

        //get the save game id
        mSaveGameIdx = extras.getInt(Utils.INTENT_EXTRA_SAVE_GAME_ID_KEY);
        if (mSaveGameIdx < 0)
            return;

        EditText etPartyName = findViewById(R.id.party_name);
        EditText etLore = findViewById(R.id.party_lore);
        EditText etLastStands = findViewById(R.id.last_stands);

        etPartyName.addTextChangedListener(this);
        etLore.addTextChangedListener(this);
        etLastStands.addTextChangedListener(this);

        //listen to cliks on the save button
        FloatingActionButton fabSaveButton = findViewById(R.id.save_game_button);
        fabSaveButton.setOnClickListener(this);


        etPartyName.setText(Utils.getSavedGamePartyName(mSaveGameIdx));
        etLore.setText(String.valueOf(Utils.getSavedGameLore(mSaveGameIdx)));
        etLastStands.setText(String.valueOf(Utils.getSavedGameLastStands(mSaveGameIdx)));

        //create the chapters spinner
        ArrayAdapter<CharSequence> chaptersAdapter = ArrayAdapter.createFromResource(this,
                R.array.chapter_names,
                R.layout.support_simple_spinner_dropdown_item);

        chaptersAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spChapters.setAdapter(chaptersAdapter);
        //set the the correct chapter
        int currentChapter = Utils.getSavedGameChapter(mSaveGameIdx);
        spChapters.setSelection(currentChapter-1);


        //create the difficulty spinner
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_names,
                R.layout.support_simple_spinner_dropdown_item);

        difficultyAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);

        //set the current difficulty
        int savedGameDifficulty = Utils.getSavedGameDifficulty(mSaveGameIdx);
        spDifficulty.setSelection(savedGameDifficulty);


        //for each hero create a panel to hold its image and XP
        int numOfHeroes = Utils.getSavedGameNumOfHeroes(mSaveGameIdx);
        ConstraintLayout mainView = findViewById(R.id.saved_game_layout);
        int lastId = R.id.difficulty_label;
        for (int i=  0; i < numOfHeroes; i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View inflatedLayout = inflater.inflate(R.layout.hero_details, mainView,false);
            inflatedLayout.setId(INFLATED_PANELS_BASE_ID + i);
            mainView.addView(inflatedLayout);


            //set the constraints for the panel to create one under the other
            ConstraintSet set = new ConstraintSet();
            set.clone(mainView);
            set.connect(inflatedLayout.getId(),ConstraintSet.LEFT,mainView.getId(),ConstraintSet.LEFT,Utils.convertDpToPixel(8,this));
            set.connect(inflatedLayout.getId(),ConstraintSet.RIGHT,mainView.getId(),ConstraintSet.RIGHT,Utils.convertDpToPixel(8,this));
            set.connect(inflatedLayout.getId(),ConstraintSet.TOP,lastId,ConstraintSet.BOTTOM,Utils.convertDpToPixel(8,this));
            set.applyTo(mainView);

            lastId = inflatedLayout.getId();

            //get the hero ID
            int heroType = Utils.getSavedGameHeroType(mSaveGameIdx,i);
            //get the image resource
            int resID = MainActivity.getHeroResIDFromHeroIdx(this,heroType);
            ImageView ivHeroImage = inflatedLayout.findViewById(R.id.hero_image);
            ivHeroImage.setImageResource(resID);


            //set the hero name
            FontTextView tvHeroName = inflatedLayout.findViewById(R.id.hero_name);
            String heroName = Utils.getHeroNameFromType(this,heroType);
            tvHeroName.setText(heroName);

            //get the hero XP
            EditText etXP = inflatedLayout.findViewById(R.id.hero_xp);
            etXP.addTextChangedListener(this);
            int xp = Utils.getSaveGameHeroXP(mSaveGameIdx,i);
            //if the XP is invalid put a message instead and make the control disabled
            if (xp != Utils.FFG_INVALID_XP) {
                etXP.setText(String.valueOf(xp));
                mHeroDataReady[i] = true;
            } else {
                etXP.setText(this.getString(R.string.hero_not_init));
                etXP.setEnabled(false);
                etXP.setTextColor(Color.RED);
            }
        }
    }

    //onclick the FAB
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: Enter");
        saveToFile(false);
        invalidateOptionsMenu();
    }

    //save the save game data to the physical file
    private void saveToFile(boolean export) {
        Log.d(TAG, "saveToFile: Enter");
        //first set the data to memory and only then save to file

        if (!isDataValid()) {
            return;
        }

        //save the game data
        //lore
        EditText etLore = findViewById(R.id.party_lore);
        int lore = Integer.parseInt(etLore.getText().toString());

        //party name
        EditText etPartyName = findViewById(R.id.party_name);
        String partyName = etPartyName.getText().toString();

        //last stands
        EditText etLastStandFails = findViewById(R.id.last_stands);
        int lastStandFails = Integer.parseInt(etLastStandFails.getText().toString());

        Utils.setSavedGamePartyName(mSaveGameIdx,partyName);
        Utils.setSavedGameLore(mSaveGameIdx,lore);
        Utils.setSavedGameLastStands(mSaveGameIdx,lastStandFails);

        //difficulty
        Spinner spDifficulty = findViewById(R.id.game_difficulty);
        int difficultyID = spDifficulty.getSelectedItemPosition();
        Utils.setSavedGameDifficulty(mSaveGameIdx,difficultyID);

        //the new chapter
        Spinner spChapter = findViewById(R.id.current_chapter);
        int chapter = spChapter.getSelectedItemPosition();
        chapter++;//the chapter in the spinner is 0 based and in the file 1 based
        Utils.setSavedGameChapter(mSaveGameIdx,chapter);

        //go over the heroes and update the XP
        int numOfHeroes = Utils.getSavedGameNumOfHeroes(mSaveGameIdx);
        for (int i=  0; i < numOfHeroes; i++) {
            View heroView = findViewById(INFLATED_PANELS_BASE_ID + i);
            if ((heroView != null) && (mHeroDataReady[i])){
                EditText etHeroXP = heroView.findViewById(R.id.hero_xp);
                int xp = Integer.parseInt(etHeroXP.getText().toString());
                Utils.setSaveGameHeroXP(mSaveGameIdx,i,xp);
            }
        }

        //try and save the game data
        //alert the user on operation failure
        if (!Utils.saveSavedGameToFile(this, mSaveGameIdx,export)) {
            new AlertDialog.Builder(new ContextThemeWrapper(SavedGame.this,R.style.AlertDialogTheme))
                    .setTitle(getString(R.string.save_failed_msg_title))
                    .setMessage(getString(R.string.save_failed_msg))
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.ok, null)
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
    }

    //create the option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saved_game_options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (!isDataValid()) {
            menu.findItem(R.id.export).setEnabled(false);
            menu.findItem(R.id.save).setEnabled(false);
        }

        if (!Utils.restoreSavedGameFiles(this,mSaveGameIdx,true)) {
            menu.findItem(R.id.restore).setEnabled(false);
        }
        return true;
    }

    //handler of the options menu selections
    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.export://export option
                //save and pass the export true parameter to the method, indicating to export the saved file
                //one the save operation is done
                saveToFile(true);
                new AlertDialog.Builder(new ContextThemeWrapper(SavedGame.this,R.style.AlertDialogTheme))
                        .setTitle(getString(R.string.export_dialog_title))
                        .setMessage(getString(R.string.export_message))
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, null)
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                return true;
            case R.id.restore://restore save game from backup
                Utils.restoreSavedGameFiles(this,mSaveGameIdx,false);
                sendDataBackToPreviousActivity();
                finish();
                return true;
            case R.id.save://save the current data
                saveToFile(false);
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }

    //handle the back button pressed to inform the main activity that it needs to refresh it data
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: Enter");
        sendDataBackToPreviousActivity();
        super.onBackPressed();
    }

    /**
     * Send data back to previous activity which start this one, you can call this method when users press on back key
     * or when users press on a view (button, image, etc) on this activity.
     */
    private void sendDataBackToPreviousActivity() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.RELOAD_GAME_DATA,true);
        setResult(Activity.RESULT_OK, intent);
    }

    //check if these is invalid data like empty strings or missing numbers
    private boolean isDataValid() {
        EditText etLore = findViewById(R.id.party_lore);
        try {
            Integer.parseInt(etLore.getText().toString());
        } catch (NumberFormatException e) {
            Log.d(TAG, "isDataValid: Lore number is not valid");
            return false;
        }

        //party name
        EditText etPartyName = findViewById(R.id.party_name);
        String partyName = etPartyName.getText().toString();
        if (partyName.isEmpty()) {
            Log.d(TAG, "isDataValid: Party name is not valid");
            return false;
        }

        //last stands
        EditText etLastStandFails = findViewById(R.id.last_stands);
        try {
            Integer.parseInt(etLastStandFails.getText().toString());
        } catch (NumberFormatException e) {
            Log.d(TAG, "isDataValid: Last stands number is not valid");
            return false;
        }

        //go over the heroes and update the XP
        int numOfHeroes = Utils.getSavedGameNumOfHeroes(mSaveGameIdx);
        for (int i=  0; i < numOfHeroes; i++) {
            View heroView = findViewById(INFLATED_PANELS_BASE_ID + i);
            if ((heroView != null) && (mHeroDataReady[i])){
                EditText etHeroXP = heroView.findViewById(R.id.hero_xp);
                try {
                    Integer.parseInt(etHeroXP.getText().toString());
                } catch (NumberFormatException e) {
                    Log.d(TAG, "isDataValid: Hero XP invalid");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    //after each text change check if the FAB needs to be enabled or disabled
    @Override
    public void afterTextChanged(Editable s) {

        FloatingActionButton fab = findViewById(R.id.save_game_button);
        fab.setEnabled(isDataValid());
        invalidateOptionsMenu();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
}
