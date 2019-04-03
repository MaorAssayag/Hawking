package optimisticapps.Hawking;
/**
 *   _   _                      _      _
 *  | | | |   __ _  __      __ | | __ (_)  _ __     __ _
 *  | |_| |  / _` | \ \ /\ / / | |/ / | | | '_ \   / _` |
 *  |  _  | | (_| |  \ V  V /  |   <  | | | | | | | (_| |
 *  |_| |_|  \__,_|   \_/\_/   |_|\_\ |_| |_| |_|  \__, |
 *                                                 |___/   ;)
 *
 * Hawking - A real-time communication system for deaf and blind people
 *
 *   Hawking was created to help people with struggle to communicate via a braille keyboard.
 *   This app enabling using TTS & STT calibrated with standard braille keyboard.
 *   This project is part of a final computer engineering project in ben-gurion university Israel,
 *   in collaboration with the deaf-blind center in Israel.
 *
 * @author Maor Assayag
 *         Computer Engineer, Ben-gurion University, Israel
 *
 * @author Refhael Shetrit
 *         Computer Engineer, Ben-gurion University, Israel
 *
 * Supervisors: Prof. Guterman Hugo
 * 		        Dr. Luzzatto Ariel
 *
 * @version 1.0
 *
 * Simple Message object class - describes messages in chat
 */

public class CustomMessage {

    /**
     * General
     */
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