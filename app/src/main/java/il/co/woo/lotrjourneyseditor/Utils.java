package il.co.woo.lotrjourneyseditor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

class Utils {

    public static final String INTENT_EXTRA_SAVE_GAME_ID_KEY = "SAVED_GAME_ID";

    private static final String TAG = "Utils";
    private static final String LOTR_PKG_NAME = "com.fantasyflightgames.jime";
    private static final String LOTR_FILE_PATH = "Android/data/" + LOTR_PKG_NAME;
    private static final String LOTR_SAVED_GAMES_PATH = LOTR_FILE_PATH + "/files/SavedGames";
    private static final String LOG_FILE_A_NAME = "LogA.txt";
    private static final String LOG_FILE_B_NAME = "LogB.txt";
    private static final String SAVE_FILE_A_NAME = "SavedGameA";
    private static final String SAVE_FILE_B_NAME = "SavedGameB";
    private static final String SAVE_FILE_BACKUP_EXT = ".bak";

    private static final String FFG_TIMESTAMP = "Timestamp";
    private static final String JAN1ST1970 = "621355968000000000";
    private static final String TIME_DIV = "10000";

    private static final String FFG_DIFFICULTY = "CampaignDifficulty";
    private static final int GAME_NORMAL = 0;
    static final int GAME_HARD = 1;

    private static final String FFG_PARTY_NAME = "PartyName";

    private static final String FFG_CHAPTER = "CurrentAdventureId";
    private static final String FFG_COMPLETED_CHAPTERS = "CompletedAdventureIds";
    private static final String FFG_CURR_SCENE = "CurrentScene";
    private static final int FFG_SCENE_START = 10;

    private static final String FFG_HEROINFO_ARRAY = "HeroInfo";
    private static final String FFG_HEROINFO_ID = "Id";
    static final int FFG_HERO_ID_INVALID = -1;
    static final int FFG_HERO_ID_ARAGORN = 1;
    static final int FFG_HERO_ID_BERAVOR = 2;
    static final int FFG_HERO_ID_BILBO = 3;
    static final int FFG_HERO_ID_ELENA = 4;
    static final int FFG_HERO_ID_GIMLI = 5;
    static final int FFG_HERO_ID_LEGOLAS = 6;

    private static final String FFG_GLOBAL_VARS = "GlobalVarData";
    private static final String FFG_INT_VARS = "IntVars";

    private static final String FFS_LAST_STAND = "LastStandsFailed";

    private static final String FFG_NAME  = "Name";
    private static final String FFG_VALUE = "Value";
    private static final String FFG_CAMP_LORE = "Campaign/Lore";

    private static final String FFG_AVAIL_XP = "AvailableXP";
    private static final String FFG_XP = "XP";
    static final int FFG_INVALID_XP = -1;


    private static ArrayList<JSONObject> mSavedGames = null;

    static int getSaveGameHeroXP(int savedGameId,int heroIdx) {
        //get the saved game
        JSONObject heroXpObj = getSaveGameHeroXPObj(savedGameId,heroIdx);
        if (heroXpObj == null)
            return FFG_INVALID_XP;

        try {
                return heroXpObj.getInt(FFG_XP);

        } catch (JSONException e) {
            Log.d(TAG, "getSaveGameHeroXP: JSON error. Could not get '" + FFG_XP + "' from saved game");
        }

        return FFG_INVALID_XP;
    }

    static void setSaveGameHeroXP(int savedGameId,int heroIdx,int xp) {
        //get the saved game
        JSONObject heroXpObj = getSaveGameHeroXPObj(savedGameId,heroIdx);
        if (heroXpObj == null)
            return;

        try {
            heroXpObj.put(FFG_XP,xp);

        } catch (JSONException e) {
            Log.d(TAG, "setSaveGameHeroXP: JSON error. Could not set '" + FFG_XP + "' from saved game");
        }
    }

    private static JSONObject getSaveGameHeroXPObj(int savedGameId,int heroIdx) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if ((savedGame == null) || (heroIdx < 0))
            return null;

        int numOfHeros = 0;
        try {

            JSONArray jsonArr = savedGame.getJSONArray(FFG_HEROINFO_ARRAY);
            if (heroIdx >= jsonArr.length())
                return null;


            JSONObject jsonHero = jsonArr.getJSONObject(heroIdx);
            if (jsonHero != null) {
                JSONArray availXP = jsonHero.getJSONArray(FFG_AVAIL_XP);
                return availXP.getJSONObject(0);

            }

        } catch (JSONException e) {
            Log.d(TAG, "getSaveGameHeroXP: JSON error. Could not get '" + FFG_HEROINFO_ARRAY + "' from saved game");
        }

        return null;
    }


    static int getSavedGameLastStands(int savedGameId) {
        //get the saved game
        JSONObject lastStandObj = getSavedGameLastStandsObj(savedGameId);
        if (lastStandObj == null)
            return 0;

        int numberOfLastStandsFailed = 0;
        try {
            numberOfLastStandsFailed = lastStandObj.getInt(FFS_LAST_STAND);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameLastStands: JSON error. Could not get '" + FFS_LAST_STAND + "' from saved game");
        }

        return numberOfLastStandsFailed;

    }

    static void setSavedGameLastStands(int savedGameId, int lastStands) {
        //get the saved game
        JSONObject lastStandObj = getSavedGameLastStandsObj(savedGameId);
        if (lastStandObj == null)
            return;

        try {
            lastStandObj.put(FFS_LAST_STAND,lastStands);
        } catch (JSONException e) {
            Log.d(TAG, "setSavedGameLastStands: JSON error. Could not get '" + FFS_LAST_STAND + "' from saved game");
        }
    }

    private static JSONObject getSavedGameLastStandsObj(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return null;

        return savedGame;
    }

    static int getSavedGameLore(int savedGameId) {

        try {
            JSONObject jsonObj = getSavedGameLoreObj(savedGameId);
            if (jsonObj != null) {
                return jsonObj.getInt(FFG_VALUE);
            }
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameLore: JSON error. Could not get '" + FFG_VALUE + "' from saved game");
        }
        return 0;
    }

    static void setSavedGameLore(int savedGameId, int lore) {
        //get the saved game
        try {
            JSONObject jsonObj = getSavedGameLoreObj(savedGameId);
            if (jsonObj != null) {
                jsonObj.put(FFG_VALUE,lore);
        }
        } catch (JSONException e) {
            Log.d(TAG, "setSavedGameLore: JSON error. Could not get '" + FFG_VALUE + "' from saved game");
        }
    }

    private static JSONObject getSavedGameLoreObj(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return null;

        try {

            JSONObject jsonObjGlobalVars = savedGame.getJSONObject(FFG_GLOBAL_VARS);
            JSONArray jsonArr = jsonObjGlobalVars.getJSONArray(FFG_INT_VARS);

            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                if (jsonObj.getString(FFG_NAME).compareTo(FFG_CAMP_LORE) == 0) {
                    return jsonObj;
                }

            }

        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameHeroType: JSON error. Could not get '" + FFG_GLOBAL_VARS + "' from saved game");
        }
        return null;
    }

    static int getSavedGameHeroType(int savedGameId, int heroIdx) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if ((savedGame == null) || (heroIdx < 0))
            return FFG_HERO_ID_INVALID;

        int numOfHeros = 0;
        try {

            JSONArray jsonArr = savedGame.getJSONArray(FFG_HEROINFO_ARRAY);
            if (heroIdx >= jsonArr.length())
                return FFG_HERO_ID_INVALID;


            JSONObject jsonHero = jsonArr.getJSONObject(heroIdx);
            if (jsonHero != null) {
                return jsonHero.getInt(FFG_HEROINFO_ID);
            }

        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameHeroType: JSON error. Could not get '" + FFG_HEROINFO_ARRAY + "' from saved game");
        }

        return 0;
    }

    static int getSavedGameNumOfHeroes(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return 0;

        int numOfHeros = 0;
        try {

            JSONArray jsonArr = savedGame.getJSONArray(FFG_HEROINFO_ARRAY);
            return jsonArr.length();

        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameNumOfHeroes: JSON error. Could not get '" + FFG_HEROINFO_ARRAY + "' from saved game");
        }

        return 0;
    }

    static int getSavedGameChapter(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return 0;

        int chapter = 0;
        try {
            chapter = savedGame.getInt(FFG_CHAPTER);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameChapter: JSON error. Could not get '" + FFG_CHAPTER + "' from saved game");
        }

        return chapter;
    }

    static void setSavedGameChapter(int savedGameId, int newChapter) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return;

        try {
            savedGame.put(FFG_CHAPTER,newChapter);
            savedGame.put(FFG_CURR_SCENE,FFG_SCENE_START);
            int[] compleatedChapters = new int[newChapter-1];
            for (int i = 0; i < compleatedChapters.length; i++) {
                compleatedChapters[i] = i+1;
            }
            savedGame.put(FFG_COMPLETED_CHAPTERS, new JSONArray(compleatedChapters));

        } catch (JSONException e) {
            Log.d(TAG, "setSavedGameChapter: JSON error. Could not put '" + FFG_CHAPTER + "' from saved game");
        }


    }


    static String getSavedGamePartyName(int savedGameId) {

        JSONObject jsonSavedGame = getSavedGamePartyNameObj(savedGameId);
        try {
            return jsonSavedGame.getString(FFG_PARTY_NAME);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGamePartyName: JSON error. Could not get '" + FFG_PARTY_NAME + "' from saved game");
        }

        return "";
    }

    static void setSavedGamePartyName(int savedGameId,String partyName) {

        JSONObject jsonSavedGame = getSavedGamePartyNameObj(savedGameId);
        try {
            jsonSavedGame.put(FFG_PARTY_NAME,partyName);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGamePartyName: JSON error. Could not get '" + FFG_PARTY_NAME + "' from saved game");
        }
    }

    private static JSONObject getSavedGamePartyNameObj(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return null;

        return savedGame;
    }

    static int getSavedGameDifficulty(int savedGameId) {
        JSONObject jsonObj = getSavedGameDifficultyObj(savedGameId);
        if (jsonObj == null)
            return GAME_NORMAL;

        int gameDifficulty = GAME_NORMAL;
        try {
            return jsonObj.getInt(FFG_DIFFICULTY);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameDifficulty: JSON error. Could not get '" + FFG_DIFFICULTY + "' from saved game");
        }

        return gameDifficulty;
    }

    static void setSavedGameDifficulty(int savedGameId, int difficulty) {
        JSONObject jsonObj = getSavedGameDifficultyObj(savedGameId);
        if (jsonObj == null)
            return;

        try {
            jsonObj.put(FFG_DIFFICULTY,difficulty);
        } catch (JSONException e) {
            Log.d(TAG, "setSavedGameDifficulty: JSON error. Could not get '" + FFG_DIFFICULTY + "' from saved game");
        }
    }

    private static JSONObject getSavedGameDifficultyObj(int savedGameId) {
        //get the saved game
        return getSavedGame(savedGameId);
    }


    static long getSavedGameDate(int savedGameId) {

        //get the saved game
        JSONObject jsonObj = getSavedGame(savedGameId);
        if (jsonObj == null)
            return 0;

        String epoch = "";
        try {
            epoch = jsonObj.getString(FFG_TIMESTAMP);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameDate: JSON error. Could not get '" + FFG_TIMESTAMP + "' from saved game");
        }

        BigInteger datebig = new BigInteger(epoch);
        BigInteger base = new BigInteger(JAN1ST1970);
        BigInteger divider = new BigInteger(TIME_DIV);
        return datebig.subtract(base).divide(divider).longValue();
    }


    private static JSONObject getSavedGame(int savedGameNum) {
        if (savedGameNum >= getNumberOfSavedGames())
            return null;

        if (mSavedGames == null)
            initSavedGameArray();

        return mSavedGames.get(savedGameNum);

    }

    private static void initSavedGameArray() {
        if (mSavedGames != null)
            mSavedGames.clear();

        mSavedGames = new ArrayList<>();
        String[] filePaths = getValidSavedGamePaths();
        for (String filePath : filePaths) {
            mSavedGames.add(readSavedGame(new File(filePath + "/" + SAVE_FILE_A_NAME)));
        }
    }

    static void clearSavedGameData() {
        initSavedGameArray();
    }

    static boolean lotrAppInstalled(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager().
                    getPackageInfo(LOTR_PKG_NAME, 0).
                    applicationInfo;

            return true;

        } catch (PackageManager.NameNotFoundException e) {
            //App was not found
        }
        return false;
    }

    private static String getSavedGamePath() {
        String internalFilesPath = Environment.getExternalStorageDirectory().getPath();
        return internalFilesPath + "/" + LOTR_SAVED_GAMES_PATH;
    }


    private static boolean savedFilesExist(File dir) {
        if ((dir == null) || (!dir.exists()))
            return false;

        boolean logALocated = false;
        boolean savedGameALocated = false;
        File[] files = dir.listFiles();
        if (files == null)
            return false;
        for (File file : files) {
            if (file.getName().compareTo(LOG_FILE_A_NAME) == 0)
                logALocated = true;
            else if (file.getName().compareTo(SAVE_FILE_A_NAME) == 0)
                savedGameALocated = true;
        }

        return logALocated && savedGameALocated;
    }

    private static String[] getValidSavedGamePaths() {
        List<String> savedGamesPaths = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            String savedGamespath = getSavedGamePath() + "/" + i;
            if (savedFilesExist(new File(savedGamespath)))
                savedGamesPaths.add(savedGamespath);
        }
        return savedGamesPaths.toArray(new String[0]);
    }

    static int getNumberOfSavedGames() {
        return getValidSavedGamePaths().length;
    }

    private static JSONObject readSavedGame(File file) {

        if ((file == null) || (!file.exists()) || (!file.canRead()))
            return null;


        StringBuilder json = new StringBuilder();
        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json.toString());
        } catch (org.json.JSONException e)
        {
            e.printStackTrace();
        }
        return jsonObj;

    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    static int convertDpToPixel(float dp, Context context){
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    static int convertPixelsToDp(float px, Context context){
        return Math.round(px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private static boolean backupSavedGameFiles(Context context, int savedGameNum) {
        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length < savedGameNum)
            return false;

        String pathToSavedGame = savedGamesPaths[savedGameNum];
        String[] fileNames = {SAVE_FILE_A_NAME,SAVE_FILE_B_NAME,LOG_FILE_A_NAME,LOG_FILE_B_NAME};
        try {
            for (String fileName:fileNames) {
                File file = new File(pathToSavedGame + "/" + fileName + SAVE_FILE_BACKUP_EXT);
                if (file.exists()) {
                    if (file.delete() == false) {
                        Log.d(TAG, "backupSavedGameFiles: failed to delete backup file.");
                        return false;
                    }
                }

                File srcFile = new File(pathToSavedGame + "/" + fileName);
                if (srcFile.exists()) {
                    file.createNewFile();
                    FileUtils.copyFile(srcFile,file);
                }
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "backupSavedGameFiles: failed to backup saved game: " + savedGameNum + ". With error: " + e.getMessage());
        }
        return false;
    }

    static boolean restoreSavedGameFiles(Context context, int savedGameNum) {
        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length < savedGameNum)
            return false;

        String pathToSavedGame = savedGamesPaths[savedGameNum];
        String[] fileNames = {SAVE_FILE_A_NAME,SAVE_FILE_B_NAME,LOG_FILE_A_NAME,LOG_FILE_B_NAME};
        for (String fileName:fileNames) {
            File buFile = new File(pathToSavedGame + "/" + fileName + SAVE_FILE_BACKUP_EXT);
            File gameFile = new File(pathToSavedGame + "/" + fileName);
            if (buFile.exists()) {

                if ((gameFile.exists()) && !gameFile.delete()) {
                    Log.d(TAG, "restoreSavedGameFiles: failed to delete game file.");
                }

                buFile.renameTo(gameFile);
                buFile.delete();
            }

        }
        return true;
    }

    static boolean saveSavedGameToFile(Context context, int savedGameNum) {
        JSONObject savedGame = getSavedGame(savedGameNum);
        if (savedGame == null) {
            return false;
        }

        if (!backupSavedGameFiles(context, savedGameNum))
            return false;

        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length < savedGameNum)
            return false;

        String[] fileNames = {SAVE_FILE_A_NAME,SAVE_FILE_B_NAME};
        String pathToSavedGame = savedGamesPaths[savedGameNum];
        for (String fileName:fileNames) {
            File file = new File(pathToSavedGame + "/" + fileName);
            if (file.exists())
                file.delete();
        }

        File newSavedGame = new File(pathToSavedGame + "/" + SAVE_FILE_A_NAME);
        try {
            FileUtils.writeStringToFile(newSavedGame, savedGame.toString());
            FileUtils.copyFile(newSavedGame,new File(pathToSavedGame + "/" + SAVE_FILE_B_NAME) );

        } catch (IOException e) {
            Log.d(TAG, "saveSavedGameToFile: failed to write to saved game: " + savedGameNum + ". With error: " + e.getMessage());
        }
        return true;
    }
}
