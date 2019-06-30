package optimisticapps.Hawking;
import java.util.HashMap;

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
 * @version 1.2
 *
 * KeyCodeEng2Heb - Braille English to Hebrew layout convertor
 */
public class BrailleEng2Heb {
    protected HashMap<Character, Character> map;

    public BrailleEng2Heb() {
        map = new HashMap<>();
        put('a','א');
        put('b','ב');
        put('v','ב');
        put('g','ג');
        put('d','ד');
        put('h','ה');
        put('w','ו');
        put('u','ו');
        put('o','ו');
        put('z','ז');
        put('x','ח');
        put('t','ט');
        put('j','י');
        put('i','י');
        put('k','כ');
        put('*','כ');
        put('l','ל');
        put('m','מ');
        put('n','נ');
        put('s','ס');
        put('$','ע');
        put('p','p');
        put('f','פ');
        put('!','צ');
        put('&','צ');
        put('q','ק');
        put('r','ר');
        put('%','ש');
        put('?','ת');
    }

    private void put(char from, char to) {
        map.put(new Character(from), new Character(to));
    }

    public Character convert(Character from) {
        Character to = map.get(from);
        if(to == null) {
            return from;
        } else {
            return to;
        }
    }

    public String convert(String input) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < input.length(); ++i) {
            Character c = input.charAt(i);
            result.append(convert(c));
        }
        return result.toString();
    }
}