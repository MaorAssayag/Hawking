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
// onBoard adapter object used in IntroductionActivity

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

class OnBoard_Adapter extends PagerAdapter {

    private Context mContext;
    ArrayList<OnBoardItem> onBoardItems;

    public OnBoard_Adapter(Context mContext, ArrayList<OnBoardItem> items) {
        this.mContext = mContext;
        this.onBoardItems = items;
    }

    @Override
    public int getCount() {
        return onBoardItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.onboard_item, container, false);

        OnBoardItem item=onBoardItems.get(position);

        ImageView imageView = itemView.findViewById(R.id.iv_onboard);
        imageView.setImageResource(item.getImageID());

        TextView tv_title=itemView.findViewById(R.id.tv_header);
        tv_title.setText(item.getTitle());

        TextView tv_content=itemView.findViewById(R.id.tv_desc);
        tv_content.setText(item.getDescription());

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}