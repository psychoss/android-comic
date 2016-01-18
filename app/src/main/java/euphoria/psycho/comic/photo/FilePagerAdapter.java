package euphoria.psycho.comic.photo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import java.util.List;

public class FilePagerAdapter extends BasePagerAdapter {


    public FilePagerAdapter(Context context, List<String> resources) {
        super(context, resources);

    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        ((GalleryViewPager) container).mCurrentView = ((FileTouchImageView) object).getImageView();
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        final FileTouchImageView iv = new FileTouchImageView(mContext);
        iv.setUrl(mResources.get(position));
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        collection.addView(iv, 0);
        return iv;
    }

    public void removeView(int index) {
        mResources.remove(index);

    }

    @Override
    public int getItemPosition(Object object) {
// TODO Auto-generated method stub
        return PagerAdapter.POSITION_NONE;
    }
}