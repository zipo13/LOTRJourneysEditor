package il.co.woo.lotrjourneyseditor;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CampaignData {
    private String name;
    private int campaignIndex;
    ArrayList<ChapterData> chapters;

    public String toString() {
        return name;
    }
}
