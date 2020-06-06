package il.co.woo.lotrjourneyseditor;

import android.app.Activity;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import il.co.woo.lotrjourneyseditor.campaign.CampaignData;
import il.co.woo.lotrjourneyseditor.campaign.CampaignManager;
import il.co.woo.lotrjourneyseditor.campaign.data.Utils;

public class SavedGame extends AppCompatActivity implements View.OnClickListener, android.text.TextWatcher {

    private static final String TAG = "SavedGame";
    private final int INFLATED_PANELS_BASE_ID = 128754;
    private int saveGameIdx = -1;
    //it is possible to edit a saved game before the first chapter was started
    //in that case the hero data is not saved yet and is empty in the save game file
    //in this case do not let the user to edit it.
    private final boolean[] heroDataReady = new boolean[5];
    private static final String CHAPTERS_ARRAY_NAME_PREFIX = "chapter_names_campaign_";
    private static final String STRING_ARRAY_TYPE = "array";
    private boolean firstTimeLoading = true;

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

        Arrays.fill(heroDataReady, false);

        //get the save game id
        saveGameIdx = extras.getInt(Utils.INTENT_EXTRA_SAVE_GAME_ID_KEY);
        if (saveGameIdx < 0)
            return;

        setupPartyDetails();
        //listen to clicks on the save button
        FloatingActionButton fabSaveButton = findViewById(R.id.save_game_button);
        fabSaveButton.setOnClickListener(this);

        updateHandleCampaignSelectionChanged();
        updateHandleChapterSelectionChanged();

        populateCampaignSelector();
        createDifficultySpinner();
        createHeroPanels();
    }

    private void createHeroPanels() {
        //for each hero create a panel to hold its image and XP
        int numOfHeroes = Utils.getSavedGameNumOfHeroes(saveGameIdx);
        ConstraintLayout mainView = findViewById(R.id.saved_game_layout);
        int lastId = R.id.difficulty_label;
        for (int i = 0; i < numOfHeroes; i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View inflatedLayout = inflater.inflate(R.layout.hero_details, mainView, false);
            inflatedLayout.setId(INFLATED_PANELS_BASE_ID + i);
            mainView.addView(inflatedLayout);

            //set the constraints for the panel to create one under the other
            ConstraintSet set = new ConstraintSet();
            set.clone(mainView);
            set.connect(inflatedLayout.getId(), ConstraintSet.LEFT, mainView.getId(), ConstraintSet.LEFT, Utils.convertDpToPixel(8, this));
            set.connect(inflatedLayout.getId(), ConstraintSet.RIGHT, mainView.getId(), ConstraintSet.RIGHT, Utils.convertDpToPixel(8, this));
            set.connect(inflatedLayout.getId(), ConstraintSet.TOP, lastId, ConstraintSet.BOTTOM, Utils.convertDpToPixel(8, this));
            set.applyTo(mainView);

            lastId = inflatedLayout.getId();

            //get the hero ID
            int heroType = Utils.getSavedGameHeroType(saveGameIdx, i);
            //get the image resource
            int resID = MainActivity.getHeroResIDFromHeroIdx(this, heroType);
            ImageView ivHeroImage = inflatedLayout.findViewById(R.id.hero_image);
            ivHeroImage.setImageResource(resID);


            //set the hero name
            FontTextView tvHeroName = inflatedLayout.findViewById(R.id.hero_name);
            String heroName = Utils.getHeroNameFromType(this, heroType);
            tvHeroName.setText(heroName);

            //get the hero XP
            EditText etXP = inflatedLayout.findViewById(R.id.hero_xp);
            etXP.addTextChangedListener(this);
            int xp = Utils.getSaveGameHeroXP(saveGameIdx, i);
            //if the XP is invalid put a message instead and make the control disabled
            if (xp != Utils.FFG_INVALID_XP) {
                etXP.setText(String.valueOf(xp));
                heroDataReady[i] = true;
            } else {
                etXP.setText(this.getString(R.string.hero_not_init));
                etXP.setEnabled(false);
                etXP.setTextColor(Color.RED);
            }
        }
    }

    private void populateCampaignSelector() {
        Spinner spCampaigns = findViewById(R.id.current_campaign);
        int currentCampaign = Utils.getSavedGameCampaign(saveGameIdx);
        //create the chapters spinner
        ArrayAdapter<CampaignData> campaignAdapter = new ArrayAdapter<CampaignData>(this,
                R.layout.support_simple_spinner_dropdown_item,
                CampaignManager.getCampaigns());
        campaignAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spCampaigns.setAdapter(campaignAdapter);
        spCampaigns.setSelection(currentCampaign - 1);//zero based
    }

    private void setupPartyDetails() {
        EditText etPartyName = findViewById(R.id.party_name);
        EditText etLore = findViewById(R.id.party_lore);
        EditText etLastStands = findViewById(R.id.last_stands);

        etPartyName.addTextChangedListener(this);
        etLore.addTextChangedListener(this);
        etLastStands.addTextChangedListener(this);

        etPartyName.setText(Utils.getSavedGamePartyName(saveGameIdx));
        etLore.setText(String.valueOf(Utils.getSavedGameLore(saveGameIdx)));
        etLastStands.setText(String.valueOf(Utils.getSavedGameLastStands(saveGameIdx)));
    }

    private void createDifficultySpinner() {
        Spinner spDifficulty = findViewById(R.id.game_difficulty);
        //create the difficulty spinner
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_names,
                R.layout.support_simple_spinner_dropdown_item);

        difficultyAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);

        //set the current difficulty
        GameDifficulty savedGameDifficulty = Utils.getSavedGameDifficulty(saveGameIdx);
        if (spDifficulty.getAdapter().getCount() > savedGameDifficulty.ordinal()) {
            spDifficulty.setSelection(savedGameDifficulty.ordinal());
        }
    }

    private void updateHandleCampaignSelectionChanged() {
        final Spinner spCampaigns = findViewById(R.id.current_campaign);
        spCampaigns.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                CampaignData campaignData = (CampaignData) spCampaigns.getItemAtPosition(position);
                ArrayAdapter<ChapterData> chaptersAdapter = new ArrayAdapter<ChapterData>(SavedGame.this,
                        R.layout.support_simple_spinner_dropdown_item,
                        campaignData.getChapters());

                Spinner spChapters = findViewById(R.id.current_chapter);
                chaptersAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spChapters.setAdapter(chaptersAdapter);

                if (firstTimeLoading) {
                    //only set the the correct chapter on first time this screen loads
                    //after that each time the campaign changes just reset it to 1
                    int savedGameChapter = Utils.getSavedGameChapter(saveGameIdx);
                    int chapterNumber = CampaignManager.savedGameChapterToChapterNumber(savedGameChapter);
                    spChapters.setSelection(chapterNumber - 1);//zero based
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateHandleChapterSelectionChanged() {
        final Spinner spChapters = findViewById(R.id.current_chapter);
        spChapters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ChapterData chapterData = (ChapterData) spChapters.getItemAtPosition(position);
                ArrayAdapter<String> sceneAdapter = new ArrayAdapter<String>(SavedGame.this,
                        R.layout.support_simple_spinner_dropdown_item,
                        chapterData.getChapterScenes());

                Spinner spScene = findViewById(R.id.current_scene);
                sceneAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spScene.setAdapter(sceneAdapter);

                if (firstTimeLoading) {
                    //only set the the correct chapter on first time this screen loads
                    //after that each time the campaign changes just reset it to 1
                    int savedGameChapter = Utils.getSavedGameChapter(saveGameIdx);
                    int sceneNumber = savedGameChapterToSceneNumber(savedGameChapter, chapterData);
                    spScene.setSelection(sceneNumber);
                    firstTimeLoading = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private int savedGameChapterToSceneNumber(int savedGameChapter, ChapterData chapterData) {
        return savedGameChapter - chapterData.getChapterGameIndex();
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

        Utils.setSavedGamePartyName(saveGameIdx, partyName);
        Utils.setSavedGameLore(saveGameIdx, lore);
        Utils.setSavedGameLastStands(saveGameIdx, lastStandFails);

        //difficulty
        Spinner spDifficulty = findViewById(R.id.game_difficulty);
        int difficultyID = spDifficulty.getSelectedItemPosition();
        Utils.setSavedGameDifficulty(saveGameIdx, difficultyID);

        //the new chapter
        Spinner spChapter = findViewById(R.id.current_chapter);
        Spinner spCampaign = findViewById(R.id.current_campaign);
        Spinner spScene = findViewById(R.id.current_scene);
        int userChapter = spChapter.getSelectedItemPosition();
        int campaign = spCampaign.getSelectedItemPosition();
        List<CampaignData> campaigns = CampaignManager.getCampaigns();
        CampaignData campaignData = campaigns.get(campaign);
        ChapterData chapterData = campaignData.getChapters().get(userChapter);

        campaign++;//the campaign in the spinner is 0 based and in the file 1 based
        int savedChapter = chapterData.getChapterGameIndex() + spScene.getSelectedItemPosition();
        Utils.setSavedGameCampaignAndChapterData(saveGameIdx, campaign, chapterData.getNumber(), savedChapter);

        //go over the heroes and update the XP
        int numOfHeroes = Utils.getSavedGameNumOfHeroes(saveGameIdx);
        for (int i = 0; i < numOfHeroes; i++) {
            View heroView = findViewById(INFLATED_PANELS_BASE_ID + i);
            if ((heroView != null) && (heroDataReady[i])) {
                EditText etHeroXP = heroView.findViewById(R.id.hero_xp);
                int xp = Integer.parseInt(etHeroXP.getText().toString());
                Utils.setSaveGameHeroXP(saveGameIdx, i, xp);
            }
        }

        //try and save the game data
        //alert the user on operation failure
        if (!Utils.saveSavedGameToFile(this, saveGameIdx, export)) {
            new AlertDialog.Builder(new ContextThemeWrapper(SavedGame.this, R.style.AlertDialogTheme))
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isDataValid()) {
            menu.findItem(R.id.export).setEnabled(false);
            menu.findItem(R.id.save).setEnabled(false);
        }

        if (!Utils.restoreSavedGameFiles(saveGameIdx, true)) {
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
                new AlertDialog.Builder(new ContextThemeWrapper(SavedGame.this, R.style.AlertDialogTheme))
                        .setTitle(getString(R.string.export_dialog_title))
                        .setMessage(getString(R.string.export_message))
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, null)
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                return true;
            case R.id.restore://restore save game from backup
                Utils.restoreSavedGameFiles(saveGameIdx, false);
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

    //helper function to generate the chapters array resource id from the campaign ID
    public static int getChaptersArrayResIDFromCampaignIdx(Context context, int campaignIdx) {
        Log.d(TAG, "getChaptersArrayResIDFromCampaignIdx: Enter");
        //Generate the resource name
        String tileFileName = CHAPTERS_ARRAY_NAME_PREFIX + campaignIdx;

        //locate the id
        return context.getResources().getIdentifier(tileFileName, STRING_ARRAY_TYPE, context.getPackageName());
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
        intent.putExtra(MainActivity.RELOAD_GAME_DATA, true);
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
        int numOfHeroes = Utils.getSavedGameNumOfHeroes(saveGameIdx);
        for (int i = 0; i < numOfHeroes; i++) {
            View heroView = findViewById(INFLATED_PANELS_BASE_ID + i);
            if ((heroView != null) && (heroDataReady[i])) {
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
