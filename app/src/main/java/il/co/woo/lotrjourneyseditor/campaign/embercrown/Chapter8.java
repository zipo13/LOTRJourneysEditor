package il.co.woo.lotrjourneyseditor.campaign.embercrown;


import java.util.List;

import il.co.woo.lotrjourneyseditor.ChapterData;
import il.co.woo.lotrjourneyseditor.campaign.data.Utils;

public class Chapter8 extends ChapterData {
    public Chapter8(int number, int chapterGameIndex, List<String> chapterScenes) {
        super(number, chapterGameIndex, chapterScenes);
    }

    private static final String COALFNAG_DEFETED_PATH = "$.GlobalVarData.IntVars[?(@.Name == 'Adventure/Ember Crown 2/Coalfang Defeated?')].Value";
    private static final String COALFNAG_KILLED_PATH = "$.GlobalVarData.IntVars[?(@.Name == 'Adventure/Ember Crown 2/Killed Coalfang')].Value";
    private static final String BATTLE_OVER_PATH = "$.GlobalVarData.IntVars[?(@.Name == 'Adventure/Ember Crown/Battle Over?')].Value";
    private static final String BATTLE_WON_PATH = "$.GlobalVarData.IntVars[?(@.Name == 'Adventure/Ember Crown/Battle Won?')].Value";


    @Override
    public void doChapterActions(int savedGameId) {
        super.doChapterActions(savedGameId);

        Utils.setValueAtPath(savedGameId, COALFNAG_DEFETED_PATH, false);
        Utils.setValueAtPath(savedGameId, COALFNAG_KILLED_PATH, false);
        Utils.setValueAtPath(savedGameId, BATTLE_OVER_PATH, false);
        Utils.setValueAtPath(savedGameId, BATTLE_WON_PATH, false);
    }
}
