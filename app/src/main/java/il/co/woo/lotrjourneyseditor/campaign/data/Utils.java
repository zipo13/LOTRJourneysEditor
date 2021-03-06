package il.co.woo.lotrjourneyseditor.campaign.data;

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
import androidx.appcompat.view.ContextThemeWrapper;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import il.co.woo.lotrjourneyseditor.GameDifficulty;
import il.co.woo.lotrjourneyseditor.R;
import il.co.woo.lotrjourneyseditor.campaign.CampaignManager;

public class Utils {

    public static final String INTENT_EXTRA_SAVE_GAME_ID_KEY = "SAVED_GAME_ID";
    public static final int PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 5487;
    private final static int MAX_SAVED_GAMES = 5;

    private static final String TAG = "Utils";
    public static final String LOTR_PKG_NAME = "com.fantasyflightgames.jime";
    private static final String LOTR_FILE_PATH = "Android/data/" + LOTR_PKG_NAME;
    private static final String LOTR_SAVED_GAMES_PATH = LOTR_FILE_PATH + "/files/SavedGames";
    private static final String LOG_FILE_A_NAME = "LogA.txt";
    private static final String LOG_FILE_B_NAME = "LogB.txt";
    private static final String SAVE_FILE_A_NAME = "SavedGameA";
    private static final String SAVE_FILE_B_NAME = "SavedGameB";
    private static final String SAVE_FILE_BACKUP_EXT = ".bak";

    private static final String FFG_TIMESTAMP = "$.Timestamp";
    private static final String JAN1ST1970 = "621355968000000000";
    private static final String TIME_DIV = "10000";

    private static final String FFG_DIFFICULTY = "$.CampaignDifficulty";

    private static final String FFG_PARTY_NAME = "$.PartyName";

    private static final String FFG_CAMPAIGN = "$.CampaignId";
    private static final String FFG_CHAPTER = "$.CurrentAdventureId";
    private static final String FFG_COMPLETED_CHAPTERS = "$.CompletedAdventureIds";
    private static final String FFG_CURR_SCENE = "$.CurrentScene";
    private static final int FFG_SCENE_START = 10;


    private static final String FFG_HEROINFO_ID = "$.HeroInfo[%s].Id";
    private static final String FFG_HEROINFO = "$.HeroInfo";
    private static final int INVALID = -1;

    /*
    static final int FFG_HERO_ID_ARAGORN = 1;
    static final int FFG_HERO_ID_BERAVOR = 2;
    static final int FFG_HERO_ID_BILBO = 3;
    static final int FFG_HERO_ID_ELENA = 4;
    static final int FFG_HERO_ID_GIMLI = 5;
    static final int FFG_HERO_ID_LEGOLAS = 6;
    */

    private static final String FFS_LAST_STAND = "$.LastStandsFailed";
    private static final String FFG_CAMP_LORE = "$.GlobalVarData.IntVars[?(@.Name == 'Campaign/Lore')].Value";


    private static final String FFG_AVAIL_XP = "$.HeroInfo[%s].AvailableXP[0].XP";
    public static final int FFG_INVALID_XP = -1;


    private static ArrayList<DocumentContext> mSavedGames = null;


    private static String getStringValueAtPath(int savedGameId, String path) {
        Log.d(TAG, "getStringValueAtPath: Enter");

        DocumentContext savedGame = getSavedGame(savedGameId);
        if (savedGame == null) {
            Log.d(TAG, "getStringValueAtPath: invalid save game or hero index");
        }

        try {
            return savedGame.read(path).toString();

        } catch (Exception e) {
            Log.d(TAG, "getStringValueAtPath: JSON error. Could not get '" + path + "' from saved game");
        }

        return null;
    }

    public static void setValueAtPath(int savedGameId, String path, Object value) {
        Log.d(TAG, "setValueAtPath: Enter");

        DocumentContext savedGame = getSavedGame(savedGameId);
        if (savedGame == null) {
            Log.d(TAG, "setValueAtPath: invalid save game or hero index");
        }

        try {
            savedGame.set(path, value);

        } catch (Exception e) {
            Log.d(TAG, "setValueAtPath: JSON error. Could not get '" + path + "' from saved game");
        }
    }

    private static int getIntValueAtPath(int savedGameId, String path) {
        Log.d(TAG, "getIntValueAtPath: Enter");

        DocumentContext savedGame = getSavedGame(savedGameId);
        if (savedGame == null) {
            Log.d(TAG, "getIntValueAtPath: invalid save game or hero index");
        }

        try {
            Object result = savedGame.read(path);
            if (result.getClass() == Integer.class) {
                return ((Integer) result);
            } else if (result.getClass() == JSONArray.class) {
                return (int) ((JSONArray) result).get(0);
            }
        } catch (Exception e) {
            Log.d(TAG, "getIntValueAtPath: JSON error. Could not get '" + path + "' from saved game");
        }

        return INVALID;
    }

    //get the XP of a specific hero
    public static int getSaveGameHeroXP(int savedGameId, int heroIdx) {
        Log.d(TAG, "getSaveGameHeroXP: Enter");
        return getIntValueAtPath(savedGameId, String.format(FFG_AVAIL_XP, heroIdx));
    }


    //set the XP of a hero
    public static void setSaveGameHeroXP(int savedGameId, int heroIdx, int xp) {
        Log.d(TAG, "setSaveGameHeroXP: Enter");
        setValueAtPath(savedGameId, String.format(FFG_AVAIL_XP, heroIdx), xp);
    }

    //get the number of last stands saved in the save game
    public static int getSavedGameLastStands(int savedGameId) {
        Log.d(TAG, "getSavedGameLastStands: Enter");
        return getIntValueAtPath(savedGameId, FFS_LAST_STAND);
    }

    //set the last stands of the party
    public static void setSavedGameLastStands(int savedGameId, int lastStands) {
        Log.d(TAG, "setSavedGameLastStands: Enter");
        setValueAtPath(savedGameId, FFS_LAST_STAND, lastStands);
    }

    //get the lore of the saved game
    public static int getSavedGameLore(int savedGameId) {
        Log.d(TAG, "getSavedGameLore: Enter");
        return getIntValueAtPath(savedGameId, FFG_CAMP_LORE);
    }

    //set the lore saved in the saved game
    public static void setSavedGameLore(int savedGameId, int lore) {
        Log.d(TAG, "setSavedGameLore: Enter");
        setValueAtPath(savedGameId, FFG_CAMP_LORE, lore);
    }

    //get the hero name from its type
    public static String getHeroNameFromType(Context context, int heroType) {
        String[] heroNames = context.getResources().getStringArray(R.array.hero_names);
        heroType--;//the heroType is 1 based and the array is 0 based
        if (heroType < heroNames.length) {
            return heroNames[heroType];
        }
        return "";
    }

    //get the hero type from the saved game
    //the hero types (gimly, legolas ...) are actually just numbered from 1-6 making it easy to math a picture to them
    public static int getSavedGameHeroType(int savedGameId, int heroIdx) {
        Log.d(TAG, "getSavedGameHeroType: Enter");
        return getIntValueAtPath(savedGameId, String.format(FFG_HEROINFO_ID, heroIdx));
    }

    //get the number of heroes in this saved game
    public static int getSavedGameNumOfHeroes(int savedGameId) {
        Log.d(TAG, "getSavedGameNumOfHeroes: Enter");
        DocumentContext savedGame = getSavedGame(savedGameId);
        if (savedGame == null) {
            Log.d(TAG, "getSavedGameNumOfHeroes: invalid save game or hero index");
        }

        try {
            JSONArray heroes = savedGame.read(FFG_HEROINFO);
            return heroes.size();

        } catch (Exception e) {
            Log.d(TAG, "getSavedGameNumOfHeroes: JSON error. Could not get '" + FFG_HEROINFO + "' from saved game");
        }
        return 0;
    }

    //get the current chapter from the saved game
    public static int getSavedGameCampaign(int savedGameId) {
        Log.d(TAG, "getSavedGameCampaign: Enter");
        return getIntValueAtPath(savedGameId, FFG_CAMPAIGN);
    }

    //get the current chapter from the saved game
    public static int getSavedGameChapter(int savedGameId) {
        Log.d(TAG, "getSavedGameChapter: Enter");
        return getIntValueAtPath(savedGameId, FFG_CHAPTER);
    }

    //change the saved game chapter
    public static void setSavedGameCampaignAndChapterData(int savedGameId, int newCampaign, int userChapter, int chapterIndexToSave) {
        Log.d(TAG, "setSavedGameChapter: Enter");
        //get the saved game
        DocumentContext savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return;

        try {
            //set the needed parameters
            savedGame.set(FFG_CAMPAIGN, newCampaign);//the new chapter
            savedGame.set(FFG_CHAPTER, chapterIndexToSave);//the new chapter
            savedGame.set(FFG_CURR_SCENE, FFG_SCENE_START);//the current SCENE in the chapter - it seems that this returns to the camp scene

            //now prepare an array to indicate that all the previous scenes were completed
            ArrayList<Integer> completedChapters = CampaignManager.getCompletedChapters(newCampaign, userChapter, chapterIndexToSave);
            CampaignManager.doChapterSpecificActions(savedGameId, newCampaign, userChapter);
            savedGame.set(FFG_COMPLETED_CHAPTERS, completedChapters);

        } catch (Exception e) {
            Log.d(TAG, "setSavedGameChapter: JSON error. Could not set save game details");
        }
    }

    //get the party name from the saved game object
    public static String getSavedGamePartyName(int savedGameId) {
        Log.d(TAG, "getSavedGamePartyName: Enter");
        return getStringValueAtPath(savedGameId, FFG_PARTY_NAME);
    }

    //set the new party name
    public static void setSavedGamePartyName(int savedGameId, String partyName) {
        Log.d(TAG, "setSavedGamePartyName: Enter");
        setValueAtPath(savedGameId, FFG_PARTY_NAME, partyName);
    }

    //get the difficulty of the saved game
    public static GameDifficulty getSavedGameDifficulty(int savedGameId) {
        int difficulty = getIntValueAtPath(savedGameId, FFG_DIFFICULTY);
        return GameDifficulty.values()[difficulty];
    }

    //set the new difficulty
    public static void setSavedGameDifficulty(int savedGameId, int difficulty) {
        Log.d(TAG, "setSavedGameDifficulty: Enter");
        setValueAtPath(savedGameId, FFG_DIFFICULTY, difficulty);
    }

    //get the date and date of the saved game
    //save game is save in .NET format meaning 0 is the year 0
    //this mean that we need to convert it to ANDROID time which begins in 1970
    public static long getSavedGameDate(int savedGameId) {
        Log.d(TAG, "getSavedGameDate: Enter");
        String epoch = getStringValueAtPath(savedGameId, FFG_TIMESTAMP);
        //convert to a BigInteger since the number is VERY big
        BigInteger dateBig = new BigInteger(epoch);
        BigInteger base = new BigInteger(JAN1ST1970);
        BigInteger divider = new BigInteger(TIME_DIV);//the time is represented in a millionth of a second
        return dateBig.subtract(base).divide(divider).longValue();
    }


    //get a save game JSON object from the cached array
    private static DocumentContext getSavedGame(int savedGameNum) {
        Log.d(TAG, "getSavedGame: Enter");
        if (savedGameNum >= getNumberOfSavedGames()) {
            Log.d(TAG, "getSavedGame: wanted save game index out of range of saved game array");
            return null;
        }


        //if this is the first time then init the array
        if (mSavedGames == null) {
            initSavedGameArray();
        }

        //return the the right save game
        return mSavedGames.get(savedGameNum);

    }

    //clear the save game cache array and create anew one
    private static void initSavedGameArray() {
        Log.d(TAG, "initSavedGameArray: Enter");
        if (mSavedGames != null) {
            mSavedGames.clear();
        }

        mSavedGames = new ArrayList<>();
        String[] filePaths = getValidSavedGamePaths();
        for (String filePath : filePaths) {
            mSavedGames.add(readSavedGame(new File(filePath + "/" + SAVE_FILE_A_NAME)));
        }
    }

    //simple clear of the save game cache data to force the app to reload next time its accessed
    public static void clearSavedGameData() {
        initSavedGameArray();
    }

    //check for the LOTR JIME package
    public static boolean lotrAppInstalled(Context context) {
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

        //check that the folder exists
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
        //check
        for (int i = 0; i < MAX_SAVED_GAMES; i++) {
            String savedGamesPath = getSavedGamePath() + "/" + i;
            if (savedFilesExist(new File(savedGamesPath)))
                savedGamesPaths.add(savedGamesPath);
        }
        //return array fo Strings
        return savedGamesPaths.toArray(new String[0]);
    }

    //simply get the number of saved game located on the device
    public static int getNumberOfSavedGames() {
        return getValidSavedGamePaths().length;
    }

    //helper function to read the save game data from a file
    private static DocumentContext readSavedGame(File file) {
        Log.d(TAG, "readSavedGame: Enter");

        //check that the file is present and ready
        if ((file == null) || (!file.exists()) || (!file.canRead()))
            return null;

        String fileContent;
        try {

            fileContent = FileUtils.readFileToString(file, (String) null);
        } catch (IOException e) {
            Log.d(TAG, "readSavedGame: failed to read file contents with error: " + e.getMessage());
            return null;
        }

        DocumentContext jsonObj = null;
        try {
            jsonObj = JsonPath.parse(fileContent);
        } catch (Exception e) {
            Log.d(TAG, "readSavedGame: failed to convert file content to JSON with error: " + e.getMessage());
        }
        return jsonObj;

    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    //Backup all the files in a save game folder
    //this is done simply by copying the files located there to a .bak files
    private static boolean backupSavedGameFiles(int savedGameNum) {
        Log.d(TAG, "backupSavedGameFiles: Enter");
        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length < savedGameNum) {
            Log.d(TAG, "backupSavedGameFiles: Saved game number too big than the current saved games - exiting");
            return false;
        }

        String pathToSavedGame = savedGamesPaths[savedGameNum];
        String[] fileNames = {SAVE_FILE_A_NAME, SAVE_FILE_B_NAME, LOG_FILE_A_NAME, LOG_FILE_B_NAME};
        try {
            for (String fileName : fileNames) {
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
                    FileUtils.copyFile(srcFile, backupFile);
                }
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "backupSavedGameFiles: failed to backup saved game: " + savedGameNum + ". With error: " + e.getMessage());
        }
        return false;
    }

    //restore backup files to the save game files
    public static boolean restoreSavedGameFiles(int savedGameNum, boolean checkOnly) {
        Log.d(TAG, "restoreSavedGameFiles: Enter");
        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length <= savedGameNum) {
            Log.d(TAG, "restoreSavedGameFiles: Saved game number too big than the current saved games - exiting");
            return false;
        }

        boolean restoreSuccess = false;
        //go over the files and check if there is a .bak file
        String pathToSavedGame = savedGamesPaths[savedGameNum];
        String[] fileNames = {SAVE_FILE_A_NAME, SAVE_FILE_B_NAME, LOG_FILE_A_NAME, LOG_FILE_B_NAME};
        for (String fileName : fileNames) {
            File buFile = new File(pathToSavedGame + "/" + fileName + SAVE_FILE_BACKUP_EXT);
            File gameFile = new File(pathToSavedGame + "/" + fileName);
            if (buFile.exists()) {
                //if this was only to check the existence of backup files
                //end the method here
                if (checkOnly) {
                    return true;
                }

                //try to restore the game file
                if ((gameFile.exists()) && (!gameFile.delete())) {
                    Log.d(TAG, "restoreSavedGameFiles: failed to delete game file.");
                } else {
                    //delete the backup file after restoring it
                    if ((buFile.renameTo(gameFile)) && (buFile.delete())) {
                        restoreSuccess = true;
                    }
                }
            }
        }
        return restoreSuccess;
    }

    //Save the JSON save game structure that is currently in memory to a file
    public static boolean saveSavedGameToFile(Context context, int savedGameNum, boolean export) {
        Log.d(TAG, "saveSavedGameToFile: Enter");
        //check to see if the save game exists
        DocumentContext savedGame = getSavedGame(savedGameNum);
        if (savedGame == null) {
            return false;
        }

        //try to make a back up - if you fail do not change the actual files
        if (!backupSavedGameFiles(savedGameNum)) {
            return false;
        }

        //check if the save game number is valid
        String[] savedGamesPaths = getValidSavedGamePaths();
        if (savedGamesPaths.length < savedGameNum) {
            return false;
        }

        //delete the current save game files
        String[] fileNames = {SAVE_FILE_A_NAME, SAVE_FILE_B_NAME};
        String pathToSavedGame = savedGamesPaths[savedGameNum];
        for (String fileName : fileNames) {
            File file = new File(pathToSavedGame + "/" + fileName);
            if ((file.exists()) && (!file.delete())) {
                return false;
            }
        }

        //create a new file
        File newSavedGame = new File(pathToSavedGame + "/" + SAVE_FILE_A_NAME);
        try {
            FileUtils.writeStringToFile(newSavedGame, savedGame.jsonString(), (String) null);
            //duplicate file A to file B - not sure why but all the ave game have it so....
            FileUtils.copyFile(newSavedGame, new File(pathToSavedGame + "/" + SAVE_FILE_B_NAME));
            //check if this is also an export operation
            if (export) {
                //locate the downloads folder and copy the file there
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File exportFile = new File(downloadsDir.getAbsolutePath() + "/" + SAVE_FILE_A_NAME);
                //check for an older file and delete it if one exists
                if (exportFile.exists()) {
                    exportFile.delete();
                }
                FileUtils.copyFile(newSavedGame, exportFile);
            }
        } catch (IOException e) {
            Log.d(TAG, "saveSavedGameToFile: failed to write to saved game: " + savedGameNum + ". With error: " + e.getMessage());
        }
        return true;
    }


    //check if the Read/Write permission has been already given to us
    public static boolean arePermissionsNeeded(Activity requester) {
        Log.d(TAG, "arePermissionsNeeded: Enter");
        return requester.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    public static void checkReadWritePermissions(final Activity requester) {
        Log.d(TAG, "checkReadWritePermissions: Enter");
        if (requester.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (requester.shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //show a message explaining to the user why the Read/Write permission is needed
                new AlertDialog.Builder(new ContextThemeWrapper(requester, R.style.AlertDialogTheme))
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
            } else {
                //if no rational is needed then just request the permission.
                requester.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE);
            }


        }
    }


}
