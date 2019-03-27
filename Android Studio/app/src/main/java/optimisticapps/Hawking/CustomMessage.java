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
// Simple Message object class - describes messages in chat

public class CustomMessage {
    private String text; // message body
    private MemberData data; // data of the user that sent this message
    private boolean belongsToCurrentUser; // is this message sent by us?
    private boolean readCheck; // does the user read the message in braille ?

    public CustomMessage(String text, MemberData data, boolean belongsToCurrentUser, boolean readCheck) {
        this.text = text;
        this.data = data;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.readCheck = readCheck;
    }

    public String getText() {
        return text;
    }

    public MemberData getData() {
        return data;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public boolean isReadCheck(){return readCheck;}

    public void setReadCheck(){this.readCheck = true;}
}