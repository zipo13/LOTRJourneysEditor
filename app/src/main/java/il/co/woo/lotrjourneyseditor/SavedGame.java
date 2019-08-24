package il.co.woo.lotrjourneyseditor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SavedGame extends AppCompatActivity implements View.OnClickListener {

    private final int INFLATED_PANELS_BASE_ID = 128754;
    private int mSaveGameIdx = -1;
    private boolean[] mHeroReady = new boolean[5];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_game);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        Spinner spChapters = findViewById(R.id.current_chapter);
        Spinner spDifficulty = findViewById(R.id.game_difficulty);
        for (int i = 0; i < mHeroReady.length; i++) {
            mHeroReady[i] = false;
        }

        mSaveGameIdx = extras.getInt(Utils.INTENT_EXTRA_SAVE_GAME_ID_KEY);
        if (mSaveGameIdx < 0)
            return;



        EditText etPartyName = findViewById(R.id.party_name);
        EditText etLore = findViewById(R.id.party_lore);
        EditText etLastStands = findViewById(R.id.last_stands);
        FloatingActionButton fabSaveButton = findViewById(R.id.save_game_button);
        fabSaveButton.setOnClickListener(this);


        etPartyName.setText(Utils.getSavedGamePartyName(mSaveGameIdx));
        etLore.setText(String.valueOf(Utils.getSavedGameLore(mSaveGameIdx)));
        etLastStands.setText(String.valueOf(Utils.getSavedGameLastStands(mSaveGameIdx)));


        ArrayAdapter<CharSequence> chaptersAdapter = ArrayAdapter.createFromResource(this,
                R.array.chapter_names,
                R.layout.support_simple_spinner_dropdown_item);

        chaptersAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spChapters.setAdapter(chaptersAdapter);

        int currentChapter = Utils.getSavedGameChapter(mSaveGameIdx);
        spChapters.setSelection(currentChapter-1);


        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_names,
                R.layout.support_simple_spinner_dropdown_item);

        difficultyAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);

        int savedGameDifficulty = Utils.getSavedGameDifficulty(mSaveGameIdx);
        spDifficulty.setSelection(savedGameDifficulty);


        int numOfHeroes = Utils.getSavedGameNumOfHeroes(mSaveGameIdx);
        ConstraintLayout mainView = findViewById(R.id.saved_game_layout);
        int lastId = R.id.difficulty_label;
        for (int i=  0; i < numOfHeroes; i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View inflatedLayout = inflater.inflate(R.layout.hero_details, mainView,false);
            inflatedLayout.setId(INFLATED_PANELS_BASE_ID + i);
            mainView.addView(inflatedLayout);



            ConstraintSet set = new ConstraintSet();
            set.clone(mainView);
            set.connect(inflatedLayout.getId(),ConstraintSet.LEFT,mainView.getId(),ConstraintSet.LEFT,Utils.convertDpToPixel(8,this));
            set.connect(inflatedLayout.getId(),ConstraintSet.RIGHT,mainView.getId(),ConstraintSet.RIGHT,Utils.convertDpToPixel(8,this));
            set.connect(inflatedLayout.getId(),ConstraintSet.TOP,lastId,ConstraintSet.BOTTOM,Utils.convertDpToPixel(8,this));
            set.applyTo(mainView);

            lastId = inflatedLayout.getId();

            int heroType = Utils.getSavedGameHeroType(mSaveGameIdx,i);
            int resID = MainActivity.getHeroResIDFromHeroIdx(this,heroType);
            ImageView ivHeroImage = inflatedLayout.findViewById(R.id.hero_image);
            ivHeroImage.setImageResource(resID);

            EditText etXP = inflatedLayout.findViewById(R.id.hero_xp);
            int xp = Utils.getSaveGameHeroXP(mSaveGameIdx,i);
            if (xp != Utils.FFG_INVALID_XP) {
                etXP.setText(String.valueOf(xp));
                mHeroReady[i] = true;
            } else {
                etXP.setText(this.getString(R.string.hero_not_init));
                etXP.setEnabled(false);
                etXP.setTextColor(Color.RED);
            }

        }
    }


    private void requestForPermission() {

        if (ContextCompat.checkSelfPermission(SavedGame.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(SavedGame.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                promptForPermissionsDialog("Error requesting permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(SavedGame.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                100);
                    }
                });

            } else {

                ActivityCompat.requestPermissions(SavedGame.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }
        }
    }

    private void promptForPermissionsDialog(String message, DialogInterface.OnClickListener onClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SavedGame.this);

        builder.setMessage(message)
                .setPositiveButton("OK", onClickListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }


    @Override
    public void onClick(View v) {

        requestForPermission();
        //save the game data
        EditText etLore = findViewById(R.id.party_lore);
        int lore = Integer.parseInt(etLore.getText().toString());
        Utils.setSavedGameLore(mSaveGameIdx,lore);


        EditText etPartyName = findViewById(R.id.party_name);
        String partyName = etPartyName.getText().toString();
        Utils.setSavedGamePartyName(mSaveGameIdx,partyName);


        EditText etLastStandFails = findViewById(R.id.last_stands);
        int lastStandFails = Integer.parseInt(etLastStandFails.getText().toString());
        Utils.setSavedGameLastStands(mSaveGameIdx,lastStandFails);

        Spinner spDifficulty = findViewById(R.id.game_difficulty);
        int difficultyID = spDifficulty.getSelectedItemPosition();
        Utils.setSavedGameDifficulty(mSaveGameIdx,difficultyID);

        Spinner spChapter = findViewById(R.id.current_chapter);
        int chapter = spChapter.getSelectedItemPosition();
        chapter++;
        Utils.setSavedGameChapter(mSaveGameIdx,chapter);

        int numOfHeroes = Utils.getSavedGameNumOfHeroes(mSaveGameIdx);
        for (int i=  0; i < numOfHeroes; i++) {
            View heroView = findViewById(INFLATED_PANELS_BASE_ID + i);
            if ((heroView != null) && (mHeroReady[i])){
                EditText etHeroXP = heroView.findViewById(R.id.hero_xp);
                int xp = Integer.parseInt(etHeroXP.getText().toString());
                Utils.setSaveGameHeroXP(mSaveGameIdx,i,xp);
            }
        }

        Utils.saveSavedGameToFile(this, mSaveGameIdx);
    }
}
