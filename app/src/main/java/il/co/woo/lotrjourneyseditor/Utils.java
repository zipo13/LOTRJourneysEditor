package il.co.woo.lotrjourneyseditor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

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
    public static final int PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 5487;
    private static int MAX_SVAED_GAMES = 5;

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
    /*
    static final int FFG_HERO_ID_ARAGORN = 1;
    static final int FFG_HERO_ID_BERAVOR = 2;
    static final int FFG_HERO_ID_BILBO = 3;
    static final int FFG_HERO_ID_ELENA = 4;
    static final int FFG_HERO_ID_GIMLI = 5;
    static final int FFG_HERO_ID_LEGOLAS = 6;
    */

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

    //get the XP of a specific hero
    static int getSaveGameHeroXP(int savedGameId,int heroIdx) {
        Log.d(TAG, "getSaveGameHeroXP: Enter");
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

    //set the XP of a hero
    static void setSaveGameHeroXP(int savedGameId,int heroIdx,int xp) {
        Log.d(TAG, "setSaveGameHeroXP: Enter");
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

    //get the JSON OBJECT that holds the wanted hero XP value
    private static JSONObject getSaveGameHeroXPObj(int savedGameId,int heroIdx) {
        Log.d(TAG, "getSaveGameHeroXPObj: Enter");
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if ((savedGame == null) || (heroIdx < 0)) {
            Log.d(TAG, "getSaveGameHeroXPObj: invalid save game or hero index");
            return null;
        }

        try {
            //get the heros array
            JSONArray jsonArr = savedGame.getJSONArray(FFG_HEROINFO_ARRAY);
            if (heroIdx >= jsonArr.length())
                return null;

            //get the correct hero
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


    //get the number of last stands saved in the save game
    static int getSavedGameLastStands(int savedGameId) {
        Log.d(TAG, "getSavedGameLastStands: Enter");
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

    //set the last stands of the party
    static void setSavedGameLastStands(int savedGameId, int lastStands) {
        Log.d(TAG, "setSavedGameLastStands: Enter");
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

    //get the saved game JSON object that holds the last stand
    private static JSONObject getSavedGameLastStandsObj(int savedGameId) {
        Log.d(TAG, "getSavedGameLastStandsObj: Enter");
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return null;

        return savedGame;
    }

    //get the lore of the saved game
    static int getSavedGameLore(int savedGameId) {
        Log.d(TAG, "getSavedGameLore: Enter");

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

    //set the lore saved in the saved game
    static void setSavedGameLore(int savedGameId, int lore) {
        Log.d(TAG, "setSavedGameLore: Enter");
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

    //get the JSON object that holds the lore
    private static JSONObject getSavedGameLoreObj(int savedGameId) {
        Log.d(TAG, "getSavedGameLoreObj: Enter");
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return null;

        try {

            //the lore is held in global vars in int vars
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

    //get the hero type from the saved game
    //the hero types (gimly, legolas ...) are actually just numbered from 1-6 making it easy to math a picture to them
    static int getSavedGameHeroType(int savedGameId, int heroIdx) {
        Log.d(TAG, "getSavedGameHeroType: Enter");
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if ((savedGame == null) || (heroIdx < 0))
            return FFG_HERO_ID_INVALID;

        try {

            //check that the wanted hero exist
            JSONArray jsonArr = savedGame.getJSONArray(FFG_HEROINFO_ARRAY);
            if (heroIdx >= jsonArr.length())
                return FFG_HERO_ID_INVALID;


            //get the ID
            JSONObject jsonHero = jsonArr.getJSONObject(heroIdx);
            if (jsonHero != null) {
                return jsonHero.getInt(FFG_HEROINFO_ID);
            }

        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameHeroType: JSON error. Could not get '" + FFG_HEROINFO_ARRAY + "' from saved game");
        }

        return 0;
    }

    //get the number of heros in this saved game
    static int getSavedGameNumOfHeroes(int savedGameId) {
        Log.d(TAG, "getSavedGameNumOfHeroes: Enter");
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return 0;

        try {

            //the heroes are save in hero array. So the length of it is actually the number of heroes
            JSONArray jsonArr = savedGame.getJSONArray(FFG_HEROINFO_ARRAY);
            return jsonArr.length();

        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameNumOfHeroes: JSON error. Could not get '" + FFG_HEROINFO_ARRAY + "' from saved game");
        }

        return 0;
    }

    //get the current chapter from the saved game
    static int getSavedGameChapter(int savedGameId) {
        Log.d(TAG, "getSavedGameChapter: Enter");
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

    //change the saved game chapter
    static void setSavedGameChapter(int savedGameId, int newChapter) {
        Log.d(TAG, "setSavedGameChapter: Enter");
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return;

        try {
            //set the needed parameters
            savedGame.put(FFG_CHAPTER,newChapter);//the new chapter
            savedGame.put(FFG_CURR_SCENE,FFG_SCENE_START);//the current SCENE in the chapter - it seems that this returns to the camp scene
            int[] completedChapters = new int[newChapter-1];//now prepare an array to indicate that all the previous scenes were completed
            for (int i = 0; i < completedChapters.length; i++) {
                completedChapters[i] = i+1;
            }
            savedGame.put(FFG_COMPLETED_CHAPTERS, new JSONArray(completedChapters));

        } catch (JSONException e) {
            Log.d(TAG, "setSavedGameChapter: JSON error. Could not put '" + FFG_CHAPTER + "' from saved game");
        }


    }

    //get the party name from the saved game object
    static String getSavedGamePartyName(int savedGameId) {
        Log.d(TAG, "getSavedGamePartyName: Enter");
        JSONObject jsonSavedGame = getSavedGamePartyNameObj(savedGameId);
        try {
            return jsonSavedGame.getString(FFG_PARTY_NAME);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGamePartyName: JSON error. Could not get '" + FFG_PARTY_NAME + "' from saved game");
        }

        return "";
    }

    //set the new party name
    static void setSavedGamePartyName(int savedGameId,String partyName) {
        Log.d(TAG, "setSavedGamePartyName: Enter");
        JSONObject jsonSavedGame = getSavedGamePartyNameObj(savedGameId);
        try {
            jsonSavedGame.put(FFG_PARTY_NAME,partyName);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGamePartyName: JSON error. Could not get '" + FFG_PARTY_NAME + "' from saved game");
        }
    }

    //get the JSON object where the party name is stored
    private static JSONObject getSavedGamePartyNameObj(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return null;

        return savedGame;
    }

    //get the difficulty of the saved game
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

    //set the new difficulty
    static void setSavedGameDifficulty(int savedGameId, int difficulty) {
        Log.d(TAG, "setSavedGameDifficulty: Enter");
        JSONObject jsonObj = getSavedGameDifficultyObj(savedGameId);
        if (jsonObj == null)
            return;

        try {
            jsonObj.put(FFG_DIFFICULTY,difficulty);
        } catch (JSONException e) {
            Log.d(TAG, "setSavedGameDifficulty: JSON error. Could not get '" + FFG_DIFFICULTY + "' from saved game");
        }
    }

    //get the JSON object where the difficulty is stored
    private static JSONObject getSavedGameDifficultyObj(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return null;

        return getSavedGame(savedGameId);
    }


    //get the date and date of the saved game
    //save game is save in .NET format meaning 0 is the year 0
    //this mean that we need to convert it to ANDROID time which begins in 1970
    static long getSavedGameDate(int savedGameId) {
        Log.d(TAG, "getSavedGameDate: Enter");

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

        //convert to a BigInteger since the number is VERY big
        BigInteger dateBig = new BigInteger(epoch);
        BigInteger base = new BigInteger(JAN1ST1970);
        BigInteger divider = new BigInteger(TIME_DIV);//the time is represented in a millionth of a second
        return dateBig.subtract(base).divide(divider).longValue();
    }


    //get a save game JSON object from the cached array
    private static JSONObject getSavedGame(int savedGameNum) {
        Log.d(TAG, "getSavedGame: Enter");
        if (savedGameNum >= getNumberOfSavedGames()) {
            Log.d(TAG, "getSavedGame: wanted save game index out of range of saved game array");
            return null;
        }


        //if this is the first time then init the array
        if (mSavedGames == null)
            initSavedGameArray();

        //return the the right save game
        return mSavedGames.get(savedGameNum);

    }

    //clear the save game cache array and create anew one
    private static void initSavedGameArray() {
        Log.d(TAG, "initSavedGameArray: Enter");
        if (mSavedGames != null)
            mSavedGames.clear();

        mSavedGames = new ArrayList<>();
        String[] filePaths = getValidSavedGamePaths();
        for (String filePath : filePaths) {
            mSavedGames.add(readSavedGame(new File(filePath + "/" + SAVE_FILE_A_NAME)));
        }
    }

    //simple clear of the save game chache data to force the app to reload next time its accessed
    static void clearSavedGameData() {
        initSavedGameArray();
    }

    //check for the LOTR JIME package
    static boolean lotrAppInstalled(Context context) {
        Log.d(TAG, "lotrAppInstalled: Enter");
        try {
            ApplicationInfo info = context.getPackageManager().
                    getPackageInfo(LOTR_PKG_NAME, 0).
                    applicationInfo;

            Log.d(TAG, "lotrAppInstalled: LOTR package located");
            return true;

        } catch (PackageManager.NameNotFoundException e) {
            //App was not found
        }
        Log.d(TAG, "lotrAppInstalled: LOTR package not located");
        return false;
    }

    //get the save game path
    private static String getSavedGamePath() {
        Log.d(TAG, "getSavedGamePath: Enter");

        //simply construct it from the environment and the location of the package
        String internalFilesPath = Environment.getExternalStorageDirectory().getPath();
        return internalFilesPath + "/" + LOTR_SAVED_GAMES_PATH;
    }


    //check if a saved game exists in the given location
    private static boolean savedFilesExist(File folder) {
        Log.d(TAG, "savedFilesExist: Enter");

        //check that the folder exsts
        if ((folder == null) || (!folder.exists()))
            return false;

        //perform 2 tests.
        //The first is for the save game file and the second is for the log file
        boolean logALocated = false;
        boolean savedGameALocated = false;
        File[] files = folder.listFiles();
        if (files == null)
            return false;
        for (File file : files) {
            if (file.getName().compareTo(LOG_FILE_A_NAME) == 0)
                logALocated = true;
            else if (file.getName().compareTo(SAVE_FILE_A_NAME) == 0)
                savedGameALocated = true;
        }

        //if both OK the we found a save game location
        return logALocated && savedGameALocated;
    }

    //scan to see how many saved game valid paths are on the device
    private static String[] getValidSavedGamePaths() {
        Log.d(TAG, "getValidSavedGamePaths: Enter");
        List<String> savedGamesPaths = new ArrayList<>();
        //chec
        for (int i = 0; i < MAX_SVAED_GAMES; i++) {
            String savedGamesPath = getSavedGamePath() + "/" + i;
            if (savedFilesExist(new File(savedGamesPath)))
                savedGamesPaths.add(savedGamesPath);
        }
        //return array fo Strings
        return savedGamesPaths.toArray(new String[0]);
    }

    //simply get the number of saved game located on the device
    static int getNumberOfSavedGames() {
        return getValidSavedGamePaths().length;
    }

    //helper function to read the save game data from a file
    private static JSONObject readSavedGame(File file) {
        Log.d(TAG, "readSavedGame: Enter");

        //check that the file is present and ready
        if ((file == null) || (!file.exists()) || (!file.canRead()))
            return null;

        String fileContent;
        try {

            String charset = null;//to avoid ambiguity
            fileContent = FileUtils.readFileToString(file,charset);
        }
        catch (IOException e) {
            Log.d(TAG, "readSavedGame: failed to read file contents with error: " + e.getMessage());
            return null;
        }

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(fileContent);
        } catch (org.json.JSONException e) {
            Log.d(TAG, "readSavedGame: failed to convert file content to JSON with error: " + e.getMessage());
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

    //Backup all the files in a savegame folder
    //this is done simply by copying the files located there to a .bak files
    private static boolean backupSavedGameFiles(Context context, int savedGameNum) {
        Log.d(TAG, "backupSavedGameFiles: Enter");
        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length < savedGameNum) {
            Log.d(TAG, "backupSavedGameFiles: Saved game number too big than the current saved games - exiting");
            return false;
        }

        String pathToSavedGame = savedGamesPaths[savedGameNum];
        String[] fileNames = {SAVE_FILE_A_NAME,SAVE_FILE_B_NAME,LOG_FILE_A_NAME,LOG_FILE_B_NAME};
        try {
            for (String fileName:fileNames) {
                //check if a backup already exists and if so delete it
                File backupFile = new File(pathToSavedGame + "/" + fileName + SAVE_FILE_BACKUP_EXT);
                if (backupFile.exists()) {
                    if (!backupFile.delete()) {
                        Log.d(TAG, "backupSavedGameFiles: failed to delete backup file.");
                        return false;
                    }
                }
                //now backup the save game file
                File srcFile = new File(pathToSavedGame + "/" + fileName);
                if ((srcFile.exists()) && (backupFile.createNewFile())) {
                    FileUtils.copyFile(srcFile,backupFile);
                }
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "backupSavedGameFiles: failed to backup saved game: " + savedGameNum + ". With error: " + e.getMessage());
        }
        return false;
    }

    //restore backup files to the save game files
    static boolean restoreSavedGameFiles(Context context, int savedGameNum) {
        Log.d(TAG, "restoreSavedGameFiles: Enter");
        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length < savedGameNum) {
            Log.d(TAG, "restoreSavedGameFiles: Saved game number too big than the current saved games - exiting");
            return false;
        }

        //go over the files and check if there is a .bak file
        String pathToSavedGame = savedGamesPaths[savedGameNum];
        String[] fileNames = {SAVE_FILE_A_NAME,SAVE_FILE_B_NAME,LOG_FILE_A_NAME,LOG_FILE_B_NAME};
        for (String fileName:fileNames) {
            File buFile = new File(pathToSavedGame + "/" + fileName + SAVE_FILE_BACKUP_EXT);
            File gameFile = new File(pathToSavedGame + "/" + fileName);
            if (buFile.exists()) {

                //try to restore the game file
                if ((gameFile.exists()) && (!gameFile.delete())) {
                    Log.d(TAG, "restoreSavedGameFiles: failed to delete game file.");
                } else {
                    buFile.renameTo(gameFile);
                    buFile.delete();//delete the backup file after restoring it
                }
            }

        }
        return true;
    }

    //Save the JSON save game sturcture that is currently in memory to a file
    static boolean saveSavedGameToFile(Context context, int savedGameNum,boolean export) {
        Log.d(TAG, "saveSavedGameToFile: Enter");
        //check to see if the save game exists
        JSONObject savedGame = getSavedGame(savedGameNum);
        if (savedGame == null) {
            return false;
        }

        //try to make a back up - if you fail do not change the actual files
        if (!backupSavedGameFiles(context, savedGameNum)) {
            return false;
        }

        //check if the save game number is valid
        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length < savedGameNum) {
            return false;
        }

        //delete the current save game files
        String[] fileNames = {SAVE_FILE_A_NAME,SAVE_FILE_B_NAME};
        String pathToSavedGame = savedGamesPaths[savedGameNum];
        for (String fileName:fileNames) {
            File file = new File(pathToSavedGame + "/" + fileName);
            if ((file.exists()) && (!file.delete())) {
                return false;
            }
        }

        //create a new file
        File newSavedGame = new File(pathToSavedGame + "/" + SAVE_FILE_A_NAME);
        try {
            String encoding  = null;//to avoid ambiguity
            FileUtils.writeStringToFile(newSavedGame, savedGame.toString(),encoding);
            //duplicate firl A to fie B - not sure why but all the ave game have it so....
            FileUtils.copyFile(newSavedGame,new File(pathToSavedGame + "/" + SAVE_FILE_B_NAME) );
            //check if this is also an export operation
            if (export) {
                //locate the downloads folder and copy the file there
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File exportFile = new File(downloadsDir.getAbsolutePath() + "/" + SAVE_FILE_A_NAME);
                //check for an older file and delete it if one exists
                if (exportFile.exists()) {
                    exportFile.delete();
                }
                FileUtils.copyFile(newSavedGame,exportFile);
            }
        } catch (IOException e) {
            Log.d(TAG, "saveSavedGameToFile: failed to write to saved game: " + savedGameNum + ". With error: " + e.getMessage());
        }
        return true;
    }


    //check if the Read/Write permission has been already given to us
    static boolean arePermissionsNeeded(Activity requester) {
        Log.d(TAG, "arePermissionsNeeded: Enter");
        if (requester.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    static void checkReadWritePermissions(final Activity requester) {
        Log.d(TAG, "checkReadWritePermissions: Enter");
        if (requester.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (requester.shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //show a message explaining to the user why the Read/Write permission is needed
                new AlertDialog.Builder(requester)
                        .setTitle(requester.getString(R.string.permission_request))
                        .setMessage(requester.getString(R.string.permission_request_msg))
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requester.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }


        }
    }


}
