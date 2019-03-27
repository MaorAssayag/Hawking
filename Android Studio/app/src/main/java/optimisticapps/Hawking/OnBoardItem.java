package optimisticapps.Hawking;
//  _   _                      _      _
// | | | |   __ _  __      __ | | __ (_)  _ __     __ _
// | |_| |  / _` | \ \ /\ / / | |/ / | | | '_ \   / _` |
// |  _  | | (_| |  \ V  V /  |   <  | | | | | | | (_| |
// |_| |_|  \__,_|   \_/\_/   |_|\_\ |_| |_| |_|  \__, |
//                                                |___/
// Creators : Maor Assayag
//            Refhael Shetrit
//
// onboarding holder used in IntroductionActivity

public class OnBoardItem {
    int imageID;
    String title;
    String description;

    public OnBoardItem() {
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}