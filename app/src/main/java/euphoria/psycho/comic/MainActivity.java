package euphoria.psycho.comic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.net.MalformedURLException;

import euphoria.psycho.comic.util.ContentView;
import euphoria.psycho.comic.util.InjectView;
import euphoria.psycho.comic.util.Injector;
import euphoria.psycho.comic.util.Utilities;

@ContentView(R.layout.activity_main)
public class MainActivity extends ActionBarActivity {


    @InjectView(R.id.ui_main_picture)
    private LinearLayout linearLayoutPicture_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        linearLayoutPicture_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PictureActivity.class);
               /* intent.putExtra(Constants.DOWNLOADER_DIRECTORY, "t");

                intent.putStringArrayListExtra(Constants.DOWNLOADER_LIST, Utilities.getNewArrayList("http://img01.ickeep.com/aot/ge65/00.png?12", "http://img01.ickeep.com/aot/ge65/01.png"));
*/
                startActivity(intent);
            }
        });
        initializeActionBar();
        initialize();

        //

    }


    private void initialize() {
        Utilities.setContext(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void initializeActionBar() {

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
