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
// Message Adapter class
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

public class MessageAdapter extends BaseAdapter {

    private List<CustomMessage> customMessages = new ArrayList<CustomMessage>();
    private Context context;
    private int font_size;
    private boolean enable_bold;
    private String last_user_message;

    public MessageAdapter(Context context, int font_size, boolean enable_bold) {
        this.context = context;
        this.font_size = font_size;
        this.enable_bold = enable_bold;
        this.last_user_message = "";
    }

    public void add(CustomMessage customMessage) {
        this.customMessages.add(customMessage);
        notifyDataSetChanged(); // to render the list we need to notify
    }

    public boolean updateReadOnMessage(int position){
        boolean fromService = customMessages.get(position).getData().getName().equals("TrainMode");
        customMessages.get(position).setReadCheck(); // set to true
        notifyDataSetChanged(); // update view with check mark
        return fromService;
    }


    @Override
    public int getCount() {
        return customMessages.size();
    }

    public String getLastUserMessage(){return this.last_user_message;}

    @Override
    public Object getItem(int i) {
        return customMessages.get(i);
    }

    public List<CustomMessage> getMessagesList(){return customMessages;}

    @Override
    public long getItemId(int i) {
        return i;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
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
            //holder.messageBody.setContentDescription(message.getText()); // Label for Accessibility
            holder.messageBody.setTextSize(font_size);
            last_user_message = customMessage.getText();
            if (enable_bold){
                holder.messageBody.setTypeface(null, Typeface.BOLD);
            }
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.their_message, null);
            //holder.name =  convertView.findViewById(R.id.name);
            holder.messageBody = convertView.findViewById(R.id.message_body);
            holder.readCheck = convertView.findViewById(R.id.read_check);
            convertView.setTag(holder);
            //holder.name.setText(customMessage.getData().getName());
            holder.messageBody.setText(customMessage.getText());
            holder.messageBody.setTextSize(font_size);
            //holder.messageBody.setContentDescription(message.getText()); // Label for Accessibility
            if (enable_bold){
                holder.messageBody.setTypeface(null, Typeface.BOLD);
            }
            if(customMessage.isReadCheck()){
                holder.readCheck.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

}

class MessageViewHolder {
    public TextView name;
    public TextView messageBody;
    public ImageView readCheck;
}