package org.codechimp.ttw;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
* This is the "Edit" activity for a Locale Plug-in.
* <p/>
* This Activity can be started in one of two states:
* <ul>
* <li>New plug-in instance: The Activity's Intent will not contain
* {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE}.</li>
* <li>Old plug-in instance: The Activity's Intent will contain
* {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} from a previously saved plug-in instance that the
* user is editing.</li>
* </ul>
*
* @see com.twofortyfouram.locale.Intent#ACTION_EDIT_SETTING
* @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
*/
public final class PluginActivity extends AbstractPluginActivity  { //implements LoaderManager.LoaderCallbacks<Cursor> {

    private Button buttonCustom;
//    private static final int LOADER_ID = 2;
//    private ListView myListView;
//    private PluginItemAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_plugin);

        // Get references to UI widgets
        buttonCustom = (Button) findViewById(R.id.custom);
//        myListView = (ListView) findViewById(R.id.chooserListView);
//        final TextView myEmptyView = (TextView) findViewById(R.id.emptyChooserView);
//        myListView.setEmptyView(myEmptyView);
//

        buttonCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO - display custom tap dialog
            }
        });
//        fillData(savedInstanceState);
    }

    @Override
    public void finish() {
        if (!isCanceled()) {
//            if (adapter.SelectedItemID > 0) {
                final Intent resultIntent = new Intent();

//                Uri itemUri = Uri.parse(ItemContentProvider.CONTENT_URI + "/" + adapter.SelectedItemID);
                Uri itemUri = Uri.parse("test");

                /*
                 * This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
                 * that anything placed in this Bundle must be available to Locale's class loader. So storing
                 * String, int, and other standard objects will work just fine. Parcelable objects are not
                 * acceptable, unless they also implement Serializable. Serializable objects must be standard
                 * Android platform objects (A Serializable class private to this plug-in's APK cannot be
                 * stored in the Bundle, as Locale's classloader will not recognize it).
                 */
//                final Bundle resultBundle = PluginBundleManager.generateBundle(getApplicationContext(), adapter.SelectedItemTitle, itemUri.toString());
                final Bundle resultBundle = PluginBundleManager.generateBundle(getApplicationContext(), "test", itemUri.toString());
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

                /*
                 * The blurb is concise status text to be displayed in the host's UI.
                 */
//                final String blurb = generateBlurb(getApplicationContext(), adapter.SelectedItemTitle);
                final String blurb = generateBlurb(getApplicationContext(), "test");
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);

                setResult(RESULT_OK, resultIntent);
//            }
        }

        super.finish();
    }

    /**
     * @param context Application context.
     * @param message The item to be displayed by the plug-in. Cannot be null.
     * @return A blurb for the plug-in.
     */
    /* package */
    static String generateBlurb(final Context context, final String message) {
        final int maxBlurbLength =
                context.getResources().getInteger(R.integer.twofortyfouram_locale_maximum_blurb_length);

        if (message.length() > maxBlurbLength) {
            return message.substring(0, maxBlurbLength);
        }

        return message;
    }

//    private void fillData(final Bundle savedInstanceState) {
//
//        // Fields from the database (projection)
//        // Must include the _id column for the adapter to work
//        String[] from = new String[]{ItemTable.COLUMN_TITLE, ItemTable.COLUMN_TYPE, ItemTable.COLUMN_DATA};
//        // Fields on the UI to which we map
//        int[] to = new int[]{R.id.itemtitle};
//
//        getLoaderManager().initLoader(LOADER_ID, null, this);
//
//        adapter = new PluginItemAdapter(this, R.layout.item_plugin, null, from, to, 0);
//
//        myListView.setAdapter(adapter);
//
//        if (null == savedInstanceState) {
//            BundleScrubber.scrub(getIntent());
//            final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
//            BundleScrubber.scrub(localeBundle);
//
//            if (PluginBundleManager.isBundleValid(localeBundle)) {
//                //Select current item
//                adapter.SelectedItemTitle = localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_TITLE);
//                Uri itemUri = Uri.parse(localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_URI));
//                adapter.SelectedItemID = Long.parseLong(itemUri.getLastPathSegment());
//            }
//        }
//    }
//
//    @Override
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        String[] projection = {ItemTable.COLUMN_ID, ItemTable.COLUMN_TITLE, ItemTable.COLUMN_TYPE, ItemTable.COLUMN_DATA};
//        return new CursorLoader(this,
//                ItemContentProvider.CONTENT_URI, projection, null, null, ItemTable.COLUMN_TITLE);
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        adapter.swapCursor(cursor);
//        selectItem();
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> cursorLoader) {
//        adapter.swapCursor(null);
//        selectItem();
//    }
//
//    void selectItem() {
//        try {
//            myListView.getItemAtPosition(getAdapterPositionById(myListView.getAdapter(), adapter.SelectedItemID));
//            myListView.setItemChecked(getAdapterPositionById(myListView.getAdapter(), adapter.SelectedItemID), true);
//        } catch (NoSuchElementException ex) {
//            //Item no longer exists, select nothing
//        }
//    }
//
//    int getAdapterPositionById(final Adapter adapter, final long id) throws NoSuchElementException {
//        final int count = adapter.getCount();
//
//        for (int pos = 0; pos < count; pos++) {
//            if (id == adapter.getItemId(pos)) {
//                return pos;
//            }
//        }
//
//        throw new NoSuchElementException();
//    }
}