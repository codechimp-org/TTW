package org.codechimp.ttw;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Superclass for plug-in Activities. This class takes care of initializing aspects of the plug-in's UI to
 * look more integrated with the plug-in host.
 */
public abstract class AbstractPluginActivity extends AppCompatActivity
{
    /**
     * Flag boolean that can only be set to true via the "Don't Save" menu item in
     * {@link #onMenuItemSelected(int, MenuItem)}.
     */
    /*
     * There is no need to save/restore this field's state.
     */
    private boolean mIsCancelled = false;
    private Toolbar toolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_plugin, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (android.R.id.home == id)
        {
            mIsCancelled = true;
            finish();
            return true;
        }
        else if (R.id.twofortyfouram_locale_menu_save == id)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * During {@link #finish()}, subclasses can call this method to determine whether the Activity was
     * canceled.
     *
     * @return True if the Activity was canceled. False if the Activity was not canceled.
     */
    protected boolean isCanceled()
    {
        return mIsCancelled;
    }
}
