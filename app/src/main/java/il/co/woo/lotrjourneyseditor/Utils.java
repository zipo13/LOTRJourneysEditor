package il.co.woo.lotrjourneyseditor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Utils {

    private static final String TAG = "Utils";
    private static final String LOTR_PKG_NAME = "com.fantasyflightgames.jime";
    private static final String LOTR_FILE_PATH = "Android/data/" + LOTR_PKG_NAME;
    private static final String LOTR_SAVED_GAMES_PATH = LOTR_FILE_PATH + "/files/SavedGames";
    private static final String LOG_FILE_A_NAME = "LogA.txt";
    private static final String LOG_FILE_B_NAME = "LogB.txt";
    private static final String SAVE_FILE_A_NAME = "SavedGameA";
    private static final String SAVE_FILE_B_NAME = "SavedGameB";

    private static final String FFG_TIMESTAMP = "Timestamp";
    private static final String JAN1ST1970 = "621355968000000000";
    private static final String TIME_DIV = "10000";

    private static final String FFG_DIFFICULTY = "CampaignDifficulty";
    static final int GAME_NORMAL = 0;
    static final int GAME_HARD = 1;

    private static final String FFG_PRTY_NAME = "PartyName";

    private static final String FFG_CHAPTER = "CurrentAdventureId";

    private static final String FFG_HEROSINFO_ARRAY = "HeroInfo";
    private static final String FFG_HEROINFO_ID = "Id";
    static final int FFG_HERO_ID_INVALID = -1;
    static final int FFG_HERO_ID_ARAGORN = 1;
    static final int FFG_HERO_ID_BERAVOR = 2;
    static final int FFG_HERO_ID_BILBO = 3;
    static final int FFG_HERO_ID_ELENA = 4;
    static final int FFG_HERO_ID_GIMLI = 5;
    static final int FFG_HERO_ID_LEGOLAS = 6;



    private static ArrayList<JSONObject> mSavedGames = null;

    static int getSavedGameHeroType(int savedGameId, int heroIdx) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if ((savedGame == null) || (heroIdx < 0))
            return FFG_HERO_ID_INVALID;

        int numOfHeros = 0;
        try {

            JSONArray jsonArr = savedGame.getJSONArray(FFG_HEROSINFO_ARRAY);
            if (heroIdx >= jsonArr.length())
                return FFG_HERO_ID_INVALID;


            JSONObject jsonHero = jsonArr.getJSONObject(heroIdx);
            if (jsonHero != null) {
                return jsonHero.getInt(FFG_HEROINFO_ID);
            }

        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameHeroType: JSON error. Could not get '" + FFG_HEROSINFO_ARRAY + "' from saved game");
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

            JSONArray jsonArr = savedGame.getJSONArray(FFG_HEROSINFO_ARRAY);
            return jsonArr.length();

        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameNumOfHeroes: JSON error. Could not get '" + FFG_HEROSINFO_ARRAY + "' from saved game");
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


    static String getSavedGamePartyName(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return "";

        try {
            return savedGame.getString(FFG_PRTY_NAME);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGamePartyName: JSON error. Could not get '" + FFG_PRTY_NAME + "' from saved game");
        }

        return "";
    }

    static int getSavedGameDifficulty(int savedGameId) {
        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return 0;

        int gameDifficulty = GAME_NORMAL;
        try {
            gameDifficulty = savedGame.getInt(FFG_DIFFICULTY);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameDifficulty: JSON error. Could not get '" + FFG_DIFFICULTY + "' from saved game");
        }

        return gameDifficulty;
    }


    static long getSavedGameDate(int savedGameId) {

        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return 0;

        String epoch = "";
        try {
            epoch = savedGame.getString(FFG_TIMESTAMP);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameDate: JSON error. Could not get '" + FFG_TIMESTAMP + "' from saved game");
        }

        BigInteger datebig = new BigInteger(epoch);
        BigInteger base = new BigInteger(JAN1ST1970);
        BigInteger divider = new BigInteger(TIME_DIV);
        return datebig.subtract(base).divide(divider).longValue();
    }


    static String getSavedGameTime(int savedGameId, Context context) {

        //get the saved game
        JSONObject savedGame = getSavedGame(savedGameId);
        if (savedGame == null)
            return null;

        int epoch = 0;
        try {
            epoch = savedGame.getInt(FFG_TIMESTAMP);
        } catch (JSONException e) {
            Log.d(TAG, "getSavedGameDate: JSON error. Could not get '" + FFG_TIMESTAMP + "' from saved game");
        }

        Date date = new Date(epoch);
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        return dateFormat.format(date);
    }


    private static JSONObject getSavedGame(int savedGameNum) {
        if (savedGameNum >= getNumberOfSavedGames())
            return null;

        if (mSavedGames == null)
            initSavedGameArray();

        return mSavedGames.get(savedGameNum);

    }

    private static void initSavedGameArray() {
        mSavedGames = new ArrayList<>();
        String[] filePaths = getValidSavedGamePaths();
        for (int i = 0; i < filePaths.length; i++) {
           mSavedGames.add(readSavedGame(new File(filePaths[i] + "/" + SAVE_FILE_A_NAME)));
        }
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
            if (file.getName().compareTo("LogA.txt") == 0)
                logALocated = true;
            else if (file.getName().compareTo("SavedGameA") == 0)
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





}
