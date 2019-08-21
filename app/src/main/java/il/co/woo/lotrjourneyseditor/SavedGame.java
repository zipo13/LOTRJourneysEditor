package il.co.woo.lotrjourneyseditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class SavedGame extends AppCompatActivity {

    private final int INFLATED_PANELS_BASE_ID = 128754;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_game);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        TextView tvSavedGameIdx = findViewById(R.id.saved_game_idx);
        Spinner spChapters = findViewById(R.id.current_chapter);
        Spinner spDifficulty = findViewById(R.id.game_difficulty);

        int savedGameIdx = extras.getInt(Utils.INTENT_EXTRA_SAVE_GAME_ID_KEY);
       // tvSavedGameIdx.setText("The current game idx is: " + savedGameIdx);


        EditText etPartyName = findViewById(R.id.party_name);
        EditText etLore = findViewById(R.id.party_lore);
        EditText etLastStands = findViewById(R.id.last_stands);


        etPartyName.setText(Utils.getSavedGamePartyName(savedGameIdx));
        etLore.setText(String.valueOf(Utils.getSavedGameLore(savedGameIdx)));
        etLastStands.setText(String.valueOf(Utils.getSavedGameLastStands(savedGameIdx)));


        ArrayAdapter<CharSequence> chaptersAdapter = ArrayAdapter.createFromResource(this,
                R.array.chapter_names,
                R.layout.support_simple_spinner_dropdown_item);

        chaptersAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spChapters.setAdapter(chaptersAdapter);

        int currentChapter = Utils.getSavedGameChapter(savedGameIdx);
        spChapters.setSelection(currentChapter-1);


        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_names,
                R.layout.support_simple_spinner_dropdown_item);

        difficultyAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);

        int savedGameDifficulty = Utils.getSavedGameDifficulty(savedGameIdx);
        spDifficulty.setSelection(savedGameDifficulty);


        int numOfHeroes = Utils.getSavedGameNumOfHeroes(savedGameIdx);
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

            int heroType = Utils.getSavedGameHeroType(savedGameIdx,i);
            int resID = MainActivity.getHeroResIDFromHeroIdx(this,heroType);
            ImageView ivHeroImage = inflatedLayout.findViewById(R.id.hero_image);
            ivHeroImage.setImageResource(resID);

            EditText etXP = inflatedLayout.findViewById(R.id.hero_xp);
            int xp = Utils.getSaveGameHeroXP(savedGameIdx,i);
            etXP.setText(String.valueOf(xp));

        }

    }
}
