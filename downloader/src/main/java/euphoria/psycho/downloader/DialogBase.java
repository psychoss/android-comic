package euphoria.psycho.downloader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2015/1/13.
 */
public class DialogBase extends AlertDialog implements DialogInterface.OnShowListener {

    protected void setVerticalMargin(View view, int top, int bottom) {
        final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        boolean isChanged = false;
        if (top > -1 && marginLayoutParams.topMargin != top) {
            marginLayoutParams.topMargin = top;
            isChanged = true;
        }
        if (bottom > -1 && marginLayoutParams.bottomMargin != bottom) {
            marginLayoutParams.bottomMargin = bottom;
            isChanged = true;
        }
        if (isChanged)
            view.setLayoutParams(marginLayoutParams);
    }

    protected DialogBase(Context context) {
        super(context);
    }

    protected void setViewInternal(View viewInternal) {
        super.setView(viewInternal);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {

    }
}
