package il.co.woo.lotrjourneyseditor;


import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChapterData {

    protected int number;
    protected int chapterGameIndex;
    protected List<String> chapterScenes;


    public String toString() {

        return "" + number;
    }

    public Integer getCompletedChaptersIndexes(int savedGameChapter, ArrayList<Integer> completedChapters) {
        if (chapterScenes.size() == 1) {
            return new Integer(chapterGameIndex);
        }

        ArrayList<Integer> chapters = new ArrayList<>();
        for (int i = 0; i < chapterScenes.size(); i++) {
            chapters.add(i + chapterGameIndex);
        }
        chapters.remove(new Integer(savedGameChapter));

        for (Integer chapter : chapters) {
            if (!completedChapters.contains(chapter)) {
                return chapter;
            }
        }
        return null;
    }
}
