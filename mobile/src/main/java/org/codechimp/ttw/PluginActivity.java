package org.codechimp.ttw;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

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

    private String patternName;
    private String patternValue;

    final Context context = this;
    private long lastTapStart = 0;
    ArrayList<Long> taps = new ArrayList<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_plugin);

        // Get references to UI widgets
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Throwable t) {
                // WTF SAMSUNG 4.2.2!
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_discard);
        }

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
                // display custom tap dialog
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_custom);
                dialog.setTitle(R.string.vibrate_pattern);

                ImageButton dialogButtonReset = (ImageButton) dialog.findViewById(R.id.dialogButtonReset);
                Button dialogButtonSave = (Button) dialog.findViewById(R.id.dialogButtonSave);
                Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
                Button dialogButtonTry = (Button) dialog.findViewById(R.id.dialogButtonTry);
                Button dialogButtonTap = (Button) dialog.findViewById(R.id.dialogButtonTap);

                dialogButtonTap.setOnTouchListener(new ImageButton.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_DOWN) {
                            Long now = System.currentTimeMillis();
                            if (lastTapStart > 0)
                                taps.add(now - lastTapStart);

                            lastTapStart = now;
                        }

                        return false;
                    }
                });

                dialogButtonReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ResetTapPattern();
                    }
                });

                dialogButtonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        patternName = getString(R.string.custom);
                        patternValue = TextUtils.join(",", taps);
                        dialog.dismiss();
                        finish();
                    }
                });

                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialogButtonTry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (taps.size() < 2) return;
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                        //Break into an array of longs
                        long[] patternLongs = new long[taps.size()];
                        for (int i = 0; i < taps.size(); i++) {
                            patternLongs[i] = taps.get(i);
                        }

                        vibrator.vibrate(patternLongs, -1);
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        ResetTapPattern();
    }

    /**
     * Clears and adds a starting 0 tap required for vibration patterns
     */
    private void ResetTapPattern() {
        taps.clear();
        taps.add(0l);
    }

    @Override
    public void finish() {
        if (!isCanceled()) {
            if (!patternName.isEmpty()) {
                final Intent resultIntent = new Intent();

                /*
                 * This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
                 * that anything placed in this Bundle must be available to Locale's class loader. So storing
                 * String, int, and other standard `objects will work just fine. Parcelable objects are not
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