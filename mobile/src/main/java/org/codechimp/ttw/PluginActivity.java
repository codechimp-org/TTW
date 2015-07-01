package org.codechimp.ttw;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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
public final class PluginActivity extends AbstractPluginActivity { //implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView patternsListView;
    private Button buttonCustom;

    private String[] patternNames;
    private String[] patternValues;

    private String patternName = "";
    private String patternValue = "";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_plugin);

        // Get references to UI widgets
        patternsListView = (ListView) findViewById(R.id.patternsListView);
        buttonCustom = (Button) findViewById(R.id.customButton);

        patternNames = getResources().getStringArray(R.array.patternNames);
        patternValues = getResources().getStringArray(R.array.patternValues);

        patternsListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, patternNames));

        patternsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                patternName = patternNames[position];
                patternValue = patternValues[position];
                finish();
            }
        });

        buttonCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO - display custom tap dialog
            }
        });
    }

    @Override
    public void finish() {
        if (!isCanceled()) {
            if (!patternName.isEmpty()) {
                final Intent resultIntent = new Intent();

                /*
                 * This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
                 * that anything placed in this Bundle must be available to Locale's class loader. So storing
                 * String, int, and other standard objects will work just fine. Parcelable objects are not
                 * acceptable, unless they also implement Serializable. Serializable objects must be standard
                 * Android platform objects (A Serializable class private to this plug-in's APK cannot be
                 * stored in the Bundle, as Locale's classloader will not recognize it).
                 */
                final Bundle resultBundle = PluginBundleManager.generateBundle(getApplicationContext(), patternName, patternValue);
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

                /*
                 * The blurb is concise status text to be displayed in the host's UI.
                 */
                final String blurb = generateBlurb(getApplicationContext(), patternName);
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);

                setResult(RESULT_OK, resultIntent);
            }
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
}