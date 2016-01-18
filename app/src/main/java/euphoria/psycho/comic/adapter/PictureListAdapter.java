package euphoria.psycho.comic.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import euphoria.psycho.comic.R;

/**
 * Created by Administrator on 2015/1/12.
 */
public class PictureListAdapter extends ArrayAdapter<Pair<String, String>> {
    private final LayoutInflater layoutInflater_;

    public PictureListAdapter(Context context,List<Pair<String, String>> objects) {
        super(context,0,objects);

        layoutInflater_ = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {

            convertView = layoutInflater_.inflate(R.layout.picture_list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.textView_ = (TextView) convertView.findViewById(R.id.ui_picture_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }
        final Pair<String, String> pair = (Pair<String, String>) getItem(position);
        viewHolder.textView_.setText(pair.first);
        return convertView;
    }

    static class ViewHolder {
        public TextView textView_;
    }
}
