package il.co.woo.lotrjourneyseditor.campaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import il.co.woo.lotrjourneyseditor.ChapterData;
import il.co.woo.lotrjourneyseditor.JIMEApp;
import il.co.woo.lotrjourneyseditor.R;
import il.co.woo.lotrjourneyseditor.campaign.embercrown.Chapter8;

public class CampaignManager {

    private static ArrayList<CampaignData> campaigns;

    public static List<CampaignData> getCampaigns() {
        if (campaigns == null) {
            campaigns = new ArrayList<>();
            campaigns.add(createBonesOfArnorCampaign());
            campaigns.add(createEmberCrownCampaign());
        }
        return campaigns;
    }

    public static CampaignData getCampaignByIndex(int campaignIndex) {
        for (CampaignData campaign : getCampaigns()) {
            if (campaign.getCampaignIndex() == campaignIndex) {
                return campaign;
            }
        }
        return null;
    }

    public static int savedGameChapterToChapterNumber(int savedGameChapter) {
        for (CampaignData campaign : getCampaigns()) {
            ArrayList<ChapterData> chapters = campaign.getChapters();
            for (ChapterData chapter : chapters) {
                if ((savedGameChapter >= chapter.getChapterGameIndex()) && (savedGameChapter < chapter.getChapterGameIndex() + chapter.getChapterScenes().size())) {
                    return chapter.getNumber();
                }
            }
        }
        return 1;
    }


    public static String savedGameChapterToChapterName(int savedGameChapter) {
        for (CampaignData campaign : getCampaigns()) {
            ArrayList<ChapterData> chapters = campaign.getChapters();
            for (ChapterData chapter : chapters) {
                if ((savedGameChapter >= chapter.getChapterGameIndex()) && (savedGameChapter < chapter.getChapterGameIndex() + chapter.getChapterScenes().size())) {
                    return chapter.getChapterScenes().get(savedGameChapter - chapter.getChapterGameIndex());
                }
            }
        }
        return "";
    }

    public static ArrayList<Integer> getCompletedChapters(int campaign, int userChapter, int saveGameChapter) {
        ArrayList<Integer> completedChapters = new ArrayList<>();
        CampaignData campaignData = getCampaignByIndex(campaign);
        for (int i = 0; i < userChapter - 1; i++) {
            completedChapters.add(campaignData.getChapters().get(i).getCompletedChaptersIndexes(saveGameChapter, completedChapters));
        }
        return completedChapters;
    }

    public static void doChapterSpecificActions(int saveGameId, int campaign, int userChapter) {
        CampaignData campaignData = getCampaignByIndex(campaign);
        for (int i = 0; i < userChapter; i++) {
            campaignData.getChapters().get(i).doChapterActions(saveGameId);
        }
    }

    private static CampaignData createBonesOfArnorCampaign() {
        ArrayList<ChapterData> chapters = new ArrayList<>();
        chapters.add(new ChapterData(1, 1, Collections.singletonList("The Shadow in Eriador")));
        chapters.add(new ChapterData(2, 2, Collections.singletonList("The Thieve's Lair")));
        chapters.add(new ChapterData(3, 3, Collections.singletonList("Dark Eglantine")));
        chapters.add(new ChapterData(4, 4, Collections.singletonList("The Forsaken Inn")));
        chapters.add(new ChapterData(5, 5, Collections.singletonList("The Silent Scepter")));
        chapters.add(new ChapterData(6, 6, Collections.singletonList("Flight from Fornost")));
        chapters.add(new ChapterData(7, 7, Collections.singletonList("Uluk's Trail")));
        chapters.add(new ChapterData(8, 8, Collections.singletonList("Run to the Brandywine")));
        chapters.add(new ChapterData(9, 9, Collections.singletonList("Barrels out of Bond")));
        chapters.add(new ChapterData(10, 10, Collections.singletonList("The Gates of Annuminas")));
        chapters.add(new ChapterData(11, 11, Collections.singletonList("The Mirror of Spirits")));
        chapters.add(new ChapterData(12, 12, Collections.singletonList("The Aid of Evensong")));
        chapters.add(new ChapterData(13, 13, Collections.singletonList("Gulgotar")));
        chapters.add(new ChapterData(14, 14, Collections.singletonList("Bones of Arnor")));

        int campaignIndex = 0;

        String[] campaignNames = JIMEApp.getRes().getStringArray(R.array.campaign_names);
        return new CampaignData(campaignNames[campaignIndex], campaignIndex + 1, chapters);
    }

    private static CampaignData createEmberCrownCampaign() {
        ArrayList<ChapterData> chapters = new ArrayList<>();
        chapters.add(new ChapterData(1, 16, Collections.singletonList("The Russet Warg")));
        chapters.add(new ChapterData(2, 17, Arrays.asList("Cold Be Hand and Heart and Bone", "Willow's Wrath")));
        chapters.add(new ChapterData(3, 19, Arrays.asList("A Misplaced Gift", "Farmer Maggot's Mushrooms", "The Trading Post")));
        chapters.add(new ChapterData(4, 19, Arrays.asList("A Misplaced Gift", "Farmer Maggot's Mushrooms", "The Trading Post")));
        chapters.add(new ChapterData(5, 19, Arrays.asList("A Misplaced Gift", "Farmer Maggot's Mushrooms", "The Trading Post")));
        chapters.add(new ChapterData(6, 22, Collections.singletonList("A Rift in the Blue Mountains")));
        chapters.add(new ChapterData(7, 23, Arrays.asList("In the Halls of Broadbeams", "Keepers of the Vally")));
        chapters.add(new Chapter8(8, 27, Collections.singletonList("The Ember Crown")));
        chapters.add(new ChapterData(9, 28, Collections.singletonList("Coalfang")));

        int campaignIndex = 1;
        String[] campaignNames = JIMEApp.getRes().getStringArray(R.array.campaign_names);
        return new CampaignData(campaignNames[campaignIndex], campaignIndex + 1, chapters);
    }
}
