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
// Settings screen Activity

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Toast;
import org.anasthase.androidseekbarpreference.SeekBarPreference;

public class SettingsPrefActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingsPrefActivity.class.getSimpleName();
    public static int current_font_size = 18;
    private static Toast last_toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("key_dark_mode", false)) {
            setTheme(R.style.CustomTheme2_Night);
        } else{
            setTheme(R.style.CustomTheme2_Day);
        }
        super.onCreate(savedInstanceState);

        //display back button. Fragments will handle its behavior
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_settings));
        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment(), "main").commit();
    }

    @Override
    public void switchToHeader(Header header) {
        super.switchToHeader(header);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return MainPreferenceFragment.class.getName().equals(fragmentName) ||
                SubscreenFragment.class.getName().equals(fragmentName);
    }

    public static class MainPreferenceFragment extends PreferenceFragment {

        SharedPreferences.OnSharedPreferenceChangeListener mListener;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_main);

            //let the fragment intercept the ActionBar buttons:
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("key_myname"));

            //pre-save sentences subscreen setting handling
            findPreference("pref_presave_subscreen").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction().replace(android.R.id.content, new SubscreenFragment(), "sub").commit();
                    return true;
                }
            });

            // feedback preference click listener
            Preference myPref = findPreference(getString(R.string.key_send_feedback));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    sendFeedback(getActivity());
                    return true;
                }
            });

            // Font size listeners
            //last_toast = new Toast(getActivity());
            SeekBarPreference seek_bar =  (SeekBarPreference) findPreference(getString(R.string.key_font_seekbar));
            seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    current_font_size = progress;
//                    last_toast.cancel();
//                    last_toast = Toast.makeText(getActivity(),getResources().getString(R.string.increase_font)+ " " + current_font_size, Toast.LENGTH_SHORT);
//                    last_toast.show();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

//            Preference increase = findPreference(getString(R.string.key_increase_font_size));
//            increase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    current_font_size = current_font_size + 2;
//                    last_toast.cancel();
//                    last_toast = Toast.makeText(getActivity(),getResources().getString(R.string.increase_font)+ " " + current_font_size, Toast.LENGTH_SHORT);
//                    last_toast.show();
//                    return true;
//                }
//            });
//            Preference decrease = findPreference(getString(R.string.key_decrease_font_size));
//            decrease.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    last_toast.cancel();
//                    if (current_font_size > 4){
//                        current_font_size = current_font_size - 2;
//                        last_toast = Toast.makeText(getActivity(),getResources().getString(R.string.decrease_font)+ " " + current_font_size, Toast.LENGTH_SHORT);
//                        last_toast.show();
//                    } else{
//                        last_toast = Toast.makeText(getActivity(),getResources().getString(R.string.warning_font), Toast.LENGTH_SHORT);
//                        last_toast.show();
//                    }
//                    return true;
//                }
//            });

            SwitchPreference darkMode = (SwitchPreference) findPreference("key_dark_mode");
            darkMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Intent intent = new Intent(getActivity(), SettingsPrefActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                    return true;
                }
            });
        }

        private static void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String stringValue = newValue.toString();
                preference.setSummary(stringValue);
                return true;
            }
        };
    }

    public static class SubscreenFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //let the fragment intercept the ActionBar buttons:
            setHasOptionsMenu(true);
            addPreferencesFromResource(R.xml.subscreen_pref);

            bindPreferenceSummaryToValue(findPreference("pref_presave_1"));
            bindPreferenceSummaryToValue(findPreference("pref_presave_2"));
            bindPreferenceSummaryToValue(findPreference("pref_presave_3"));
            bindPreferenceSummaryToValue(findPreference("pref_presave_4"));
            bindPreferenceSummaryToValue(findPreference("pref_presave_5"));
            bindPreferenceSummaryToValue(findPreference("key_presave"));
        }

        private static void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String stringValue = newValue.toString();
                preference.setSummary(stringValue);
                return true;
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"HawkingBGU@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }

    public static int getCurrent_font_size() {
        return current_font_size;
    }

    public static void set_font_size(int font_size){
        current_font_size = font_size;
    }
}