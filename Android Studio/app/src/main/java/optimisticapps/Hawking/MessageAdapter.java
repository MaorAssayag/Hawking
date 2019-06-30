package optimisticapps.Hawking;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

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
 * Message Adapter class
 */

public class MessageAdapter extends BaseAdapter {

    /**
     * General
     */
    private List<CustomMessage> customMessages = new ArrayList<CustomMessage>(); // Message array
    private Context context; // context of Main Activity
    private int font_size; // User-Setting : current font size
    private boolean enable_bold; // User-Setting : does the text need to be bold ?
    private String last_user_message; // last message sent by the user

    /**
     * MessageAdapter(Context, int, boolean)
     *
     * Main constructor of this class.
     * @param context - the app context, used to access UI elements on the Main Activity
     * @param font_size - the default font_size of the chat messages
     * @param enable_bold - the default style of the chat messages (bold or not)
     */
    public MessageAdapter(Context context, int font_size, boolean enable_bold) {
        this.context = context;
        this.font_size = font_size;
        this.enable_bold = enable_bold;
        this.last_user_message = "";
    }

    /**
     * add(CustomMessage)
     *
     * Add a new message to the chat view. Using CustomMessage class containing additional
     * information about the message.
     * After adding to the local object, call notifyDataSetChanged to render the list-view again.
     *
     * @param customMessage - the new message to be added to the chat
     */
    public void add(CustomMessage customMessage) {
        this.customMessages.add(customMessage);
        // limit the message view
        if (getCount() > MainActivity.chat_messages_limit){
            customMessages.remove(0);
        }
        notifyDataSetChanged(); // to render the list we need to notify
    }

    /**
     * updateReadOnMessage(int)
     *
     * Update the message in @position that its been read by the User.
     * This method will be called only on messages that belong to the Recognition part of
     * the conversation.
     *
     * @param position - the position of the message view in the List-view (the chat)
     * @return fromService - if the message is from the Train mode - to not start Speech Recognition
     *                       in updateCheckRead.
     */
    public boolean updateReadOnMessage(int position){
        boolean fromService = customMessages.get(position).getData().getName().equals("TrainMode");
        customMessages.get(position).setReadCheck(); // set to true
        notifyDataSetChanged(); // update view with check mark
        return fromService;
    }

    public void saveFromIndex(int index){
        customMessages = customMessages.subList(index-1, getCount());
        notifyDataSetChanged(); // to render the list we need to notify
    }

    /**
     * getCount()
     *
     * @return the number of messages in the list view
     */
    @Override
    public int getCount() {
        return customMessages.size();
    }

    /**
     * getLastUserMessage()
     *
     * @return the last message from the User
     */
    public String getLastUserMessage(){
        return this.last_user_message;
    }

    /**
     * getItem(int)
     *
     * @param i - message position in the local object Message Array
     * @return the message in position @i
     */
    @Override
    public Object getItem(int i) {
        return customMessages.get(i);
    }

    /**
     * getMessagesList()
     *
     * @return the temp local List of CustomMessage (for updating the font size via settings)
     */
    public List<CustomMessage> getMessagesList(){
        return customMessages;
    }

    /**
     * getItemId(int)
     *
     * @param i position
     * @return the item ID, which happens to be the temp position in the Array
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * getView(int, View, ViewGroup)
     * The main method to update the UI of the chat. We can add message to both side of the
     * conversation and keep the read/unread mark.
     * This is the backbone of the class,
     * it handles the creation of single ListView row (chat bubble).
     *
     * @param i - view position (or new position to be added)
     * @param convertView - the ListView in Main Activity holding the chat messages View
     * @param viewGroup - Override, not our business
     * @return the new message view (with all the required information)
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        CustomMessage customMessage = customMessages.get(i);

        if (customMessage.isBelongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            holder.messageBody.setText(customMessage.getText());
            holder.messageBody.setContentDescription(context.getString(R.string.my_message_disc) + customMessage.getText());
            //holder.messageBody.setContentDescription(message.getText()); // Label for Accessibility
            holder.messageBody.setTextSize(font_size);
            last_user_message = customMessage.getText();
            if (enable_bold)
                holder.messageBody.setTypeface(null, Typeface.BOLD);

        } else if (customMessage.getData().getName().equals("System")){ // system messages when braille keyboard is connected
            convertView = messageInflater.inflate(R.layout.their_message, null);
            //holder.name =  convertView.findViewById(R.id.name);
            holder.messageBody = convertView.findViewById(R.id.message_body);
            holder.readCheck = convertView.findViewById(R.id.read_check);
            convertView.setTag(holder);
            //holder.name.setText(customMessage.getData().getName());
            holder.messageBody.setText(customMessage.getText());
            holder.messageBody.setContentDescription(context.getString(R.string.system) + customMessage.getText());
            holder.messageBody.setTextSize(font_size);
            //holder.messageBody.setContentDescription(message.getText()); // Label for Accessibility
            if (enable_bold)
                holder.messageBody.setTypeface(null, Typeface.BOLD);

        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.their_message, null);
            //holder.name =  convertView.findViewById(R.id.name);
            holder.messageBody = convertView.findViewById(R.id.message_body);
            holder.readCheck = convertView.findViewById(R.id.read_check);
            convertView.setTag(holder);
            //holder.name.setText(customMessage.getData().getName());
            holder.messageBody.setText(customMessage.getText());
            holder.messageBody.setContentDescription(context.getString(R.string.thier_message_disc) + customMessage.getText());
            holder.messageBody.setTextSize(font_size);
            //holder.messageBody.setContentDescription(message.getText()); // Label for Accessibility
            if (enable_bold)
                holder.messageBody.setTypeface(null, Typeface.BOLD);

            if(customMessage.isReadCheck())
                holder.readCheck.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
}

/**
 * MessageViewHolder class
 * Simple class to hold the required message data
 */
class MessageViewHolder {
    public TextView name;
    public TextView messageBody;
    public ImageView readCheck;
}