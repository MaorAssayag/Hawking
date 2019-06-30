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
 * KeyCodeEng2Heb - QWERTY English to Hebrew layout convertor
 */
public class KeyCodeEng2Heb {
    protected HashMap<Character, Character> map;

    public KeyCodeEng2Heb() {
        map = new HashMap<>();
        put('a','ש');
        put('b','נ');
        put('c','ב');
        put('d','ג');
        put('e','ק');
        put('f','כ');
        put('g','ע');
        put('h','י');
        put('i','ן');
        put('j','ח');
        put('k','ל');
        put('l','ך');
        put('m','צ');
        put('n','מ');
        put('o','ם');
        put('p','פ');
        put('q','/');
        put('r','ר');
        put('s','ד');
        put('t','א');
        put('u','ו');
        put('v','ה');
        put('x','ס');
        put('y','ט');
        put('z','ז');
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