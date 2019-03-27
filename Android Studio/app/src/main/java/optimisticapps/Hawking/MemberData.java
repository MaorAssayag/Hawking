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
// Member Data class - used in Message class

class MemberData {
    private String name;

    public MemberData(String name) {
        this.name = name;
    }

    // Add an empty constructor so we can later parse JSON into MemberData using Jackson
    public MemberData() {
    }

    public String getName() {
        return name;
    }
}