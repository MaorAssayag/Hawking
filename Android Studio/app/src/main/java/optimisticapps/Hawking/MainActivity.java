package optimisticapps.Hawking;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gigamole.library.PulseView;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;
import android.speech.tts.TextToSpeech;
import android.os.Vibrator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *   _   _                      _      _
 *  | | | |   __ _  __      __ | | __ (_)  _ __     __ _
 *  | |_| |  / _` | \ \ /\ / / | |/ / | | | '_ \   / _` |
 *  |  _  | | (_| |  \ V  V /  |   <  | | | | | | | (_| |
 *  |_| |_|  \__,_|   \_/\_/   |_|\_\ |_| |_| |_|  \__, |
 *                                                 |___/   ;)
 *
 * Hawking - A real-time communication system for deaf and blind people
 *
 *   Hawking was created to help people with struggle to communicate via a braille keyboard.
 *   This app enabling using TTS & STT calibrated with standard braille keyboard.
 *   This project is part of a final computer engineering project in ben-gurion university Israel,
 *   in collaboration with the deaf-blind center in Israel.
 *
 * @author Maor Assayag
 *         Computer Engineer, Ben-gurion University, Israel
 *
 * @author Refhael Shetrit
 *         Computer Engineer, Ben-gurion University, Israel
 *
 * Supervisors: Prof. Guterman Hugo
 * 		        Dr. Luzzatto Ariel
 *
 * @version 1.0
 *
 * Main Activity
 */

public class MainActivity extends AppCompatActivity {

    /**
     * General
     */
    private EditText myMessageText; //chat editText view
    private String LOG_TAG = "Hawking"; //tag for debug
    private MemberData data;//data object represent a new message
    private MessageAdapter messageAdapter; //Message adapter that handle the chat
    private ListView messagesView; //ListView of the message views in chat
    private SpeechRecognizer speechRecognizer; //SpeechRecognizer object, will get an intent to start listening in some sections
    private Intent intent_recognize; //Intent to be used in STT services
    private static final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 1;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 2;
    private RecognitionProgressView recognitionProgressView; //Custom speech-wave view from : com.github.zagum.speechrecognitionview
    private TextToSpeech tts; //Text-To-Speech object. handles all tts operations
    private final int THRESH_HOLD_RECOGNITION = 1;
    private PulseView pulseView_train;

    /**
     * User settings
     */
    public SharedPreferences sharedPref; // SharedPreferences object for all the user settings to be saved on
    private int current_font_size = 20; // tracking the current desire font size of the chat messages UI
    private String stt_language = "en-US"; // desired STT language, default : english
    private String tts_language = "en-US";// desired TTS language, default : english
    private String shortcut_template; // current shortcut for pre-save sentences - default "a"
    private String myName = ""; // User name to be displayed on the app title
    private boolean max_volume_enable = true; // does the user want the device to operate TTS with max-volume(always)
    private boolean allow_to_speak = true;// allow to operate tts services
    private boolean allow_to_vibrate = true; // allow to vibrate when tts operate
    private boolean enable_bold = false; // enable bold text in chat
    private boolean repeat_on_blank = false; // repeat speaking the last message sent by the user when the user send a blank message
    private boolean repeat_on_longpress = true; // repeat the current long-pressed message
    private boolean dark_mode = false;

    /**
     * Progress-boolean
     */
    public boolean recognizeSpeech = false;// Currently a switch to block/non-block the stt operations. Could be the user firing the app or press a button.
    private boolean doneReading = true; // does the user read the last x messages ? (default x = 2). block/non-block stt operations.
    private boolean clear_speech_recognition = false; // does the user want to speak right now (so the stt operation will be stopped & cleaned)
    public int recognitionMessagesCount = 0; // how many un-read messages for the user ?
    private boolean isListening = false; // doest the stt currently operating (actively listening)
    private boolean unavailable_stt = false; // if the current desired language of speech to be detected is unavailable
    private boolean first_time_after_closed_app = false;  // used to edit the pref-shared setting file and add the current font to it

    /**
     * Vibration
     */
    private Vibrator vibrator;  // Vibrator object, used to notify the user on on-going events (currently speaks, wrong settings etc)
    long[] pattern_heart_beat = {0, 100, 200, 125, 900, 100, 200, 100}; //Pattern of vibration 1
    long[] pattern_wrong = {50, 100, 50, 100}; //Pattern of vibration 2
    long[] pattern_received = {50, 100, 200, 125, 200, 125, 200}; //Pattern of vibration 2

    /**
     * POP UP messages
     */
    RelativeLayout mRelativeLayout;
    public static final String PACKAGE_NAME_GOOGLE_NOW = "com.google.android.googlequicksearchbox";
    public static final String ACTIVITY_INSTALL_OFFLINE_FILES = "com.google.android.voicesearch.greco3.languagepack.InstallActivity";

    /**
     * GPS - Location service
     */
    private boolean train_mode_status = false;
    private boolean requestingLocationUpdates = false;
    private String currentCity;

    /**
     * Service communication
     */
    public static final int UPDATE_CURRENT_CITY = 1;
    public static final int LOCATION_FAILED = 2;
    public Handler messageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Preference Settings object
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        boolean new_dark_mode = sharedPref.getBoolean("key_dark_mode", false);
        boolean update_dark_mode = sharedPref.getBoolean("updatedDarkMode",true);
        if (update_dark_mode) {
            this.dark_mode = new_dark_mode;
            prefEditor.putBoolean("updatedDarkMode", false);
            prefEditor.apply();
            if (new_dark_mode) {
                setTheme(R.style.CustomTheme2_Night);
            } else {
                setTheme(R.style.CustomTheme2_Day);
            }
        }
        super.onCreate(savedInstanceState);

        // Check if we need to display our Onboarding Fragment - introduction activity
        if (sharedPref.getBoolean("PREF_USER_FIRST_TIME",true)){
            // start introduction
            startActivity(new Intent(this, IntroductionActivity.class));
            return;
        }

        messageHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                int state = message.arg1;
                Bundle bundle = message.getData();
                String msg = bundle.getString("msg1");
                switch (state) {
                    case UPDATE_CURRENT_CITY:
                        onMessage(msg,3);
                        vibrator.vibrate(pattern_received, -1);
                        break;
                    case LOCATION_FAILED:
                        onMessage(msg,3);
                        vibrator.vibrate(pattern_wrong, -1);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        // General
        setContentView(R.layout.main_layout);
        myMessageText = findViewById(R.id.MessageInputText);
        myMessageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // if the user press the 'ok' button on the in-display keyboard, send the message
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendMessage(findViewById(R.id.sendImageButton));
                }
                return false;
            }
        });
        myMessageText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        // Message adapter init
        data = new MemberData("Recognition");
        messageAdapter = new MessageAdapter(this, current_font_size, enable_bold);
        messagesView = findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);
        // when the user long-pressed on a message, repeat the message
        messagesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                if (repeat_on_longpress){
                    CustomMessage msg = (CustomMessage) messageAdapter.getItem(pos);
                    sendMessage(msg.getText());
                }
                return true;
            }
        });

        // Voice wave initialize
        intent_recognize = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent_recognize.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent_recognize.putExtra(RecognizerIntent.EXTRA_LANGUAGE, this.stt_language);// default en_US
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognitionProgressView =  findViewById(R.id.recognition_view);
        recognitionProgressView.setSpeechRecognizer(speechRecognizer);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {
                Log.i(LOG_TAG, "onResults");
                showResults(results);
            }

            @Override
            public void onError(int error) {
                String errorMessage = getErrorText(error);
                Log.d(LOG_TAG, "FAILED " + errorMessage);
                if (error != SpeechRecognizer.ERROR_CLIENT){
                    Toast.makeText(MainActivity.this, "FAILED " + errorMessage, Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(pattern_wrong, -1);
                    setRecognizeSpeech();
                }
                super.onError(error);
            }
        });
        int[] colors = {
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };
        recognitionProgressView.setColors(colors);
        recognitionProgressView.setCircleRadiusInDp(4);
        recognitionProgressView.play();
        int multi = 3;
        int[] heights = { multi * 16 , multi * 23 , multi * 16, multi * 22, multi * 18 };
        recognitionProgressView.setBarMaxHeightsInDp(heights);
        //recognitionProgressView.setSpacingInDp(2);
        //recognitionProgressView.setIdleStateAmplitudeInDp(2);
        //recognitionProgressView.setRotationRadiusInDp(10);

        // Action bar initialize
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_layout);

        // Text To Speech init
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        // Get instance of Vibrator from current Context
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (allow_to_vibrate) {
                    vibrator.vibrate(pattern_heart_beat, 0);
                }
            }
            @Override
            public void onDone(String utteranceId) {
                // Speaking stopped.
                vibrator.cancel();
                if (isListening){
                    startRecognition();
                }
            }
            @Override
            public void onError(String utteranceId) {
                Log.i(LOG_TAG, "onError: ");
                // Error section
            }
        });

        //pref = getApplicationContext().getSharedPreferences("pref_main", 0); // 0 - for private mode
        if (!sharedPref.contains("font_size")){
            prefEditor.putInt("font_size", 20);
            prefEditor.putBoolean("requestingLocationUpdates",false);
            prefEditor.putBoolean("updatedDarkMode",false);
            prefEditor.apply();
        }

        this.first_time_after_closed_app = true;
        mRelativeLayout = findViewById(R.id.main_layout);
        pulseView_train = findViewById(R.id.pv_train);
        pulseView_train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrainMode(v);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        Intent stopIntent = new Intent(MainActivity.this, myServiceTrainMode.class);
        stopService(stopIntent);
    }

    /**
     * This method will start actively speech recognition if available.
     *
     * First the function make sure that we have the permission to record audio, notice
     * that we ask those permissions in AndroidManifest.xml (app installation)
     * and in the first time that app is running.
     *
     * If the current desired language of speech to be detected is unavailable,
     * this.unavailable_stt will be false and the user will get a warning Toast
     * with a 'wrong' vibrate.
     *
     * In addition, this method responsible for the speech-wave view animation to be started.
     *
     * this.recognizeSpeech : if the user allow speech recognition (e.g. a button)
     * this.unavailable_stt : if the current desired language of speech to be detected is unavailable
     */
    private void startRecognition(){
        if (recognizeSpeech) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                // This will make sure that the user allowing voice recording permissions.
                // When the user has been giving the permission this method will be called again to perform speechRecognition
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.RECORD_AUDIO },
                        REQUEST_RECORD_AUDIO_PERMISSION_CODE);

            } else {
                this.unavailable_stt =  this.stt_language.equals("he-IL") && !isNetworkAvailable();

                if (this.unavailable_stt){
                    vibrator.vibrate(pattern_wrong, -1);
                    offlinePopUpWindows();
                } else {
                    recognitionProgressView.stop();
                    recognitionProgressView.play();
                    recognitionProgressView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            speechRecognizer.startListening(intent_recognize);
                        }
                    }, 50);
                }
            }
        }
    }

    /**
     * Used only in onError in recognitionProgressView (animation of voice-wave)
     */
    public void setRecognizeSpeech(){
        this.recognizeSpeech = false;
    }

    /**
     * switchRecognition
     * This method switch on&off this.recognizeSpeech which is a
     * Progress-boolean that switch to block/non-block the stt operations.
     * Could be the user firing the app or press a button.
     *
     * @param view - speech-wave view on the UI
     */
    public void switchRecognition(View view){
        this.recognizeSpeech = !this.recognizeSpeech;
        if (this.recognizeSpeech && this.doneReading) {
            this.isListening = true;
            startRecognition();
        }
    }

    /**
     * showResults
     * This method is called on SpeechRecognizer results (done listening).
     * If the user send a message to be TTS in the middle of recognition process,
     * the results will be dismiss.
     * After sending the message as the recognition speech, we continue to listen if there is no
     * more than 2 un-read message to been read by the user.
     *
     * @param results - voice recognition results (Currently Google SpeechRecognition API)
     */
    private void showResults(Bundle results) {
        this.isListening = false;
        if (clear_speech_recognition){
            // just ignore captured voices
            clear_speech_recognition = false;
        }else {
            // Voice recognition is done
            speechRecognizer.stopListening();
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            // Add the message to the chat
            onMessage(matches.get(0), 2);
            recognitionMessagesCount++;
            if (recognitionMessagesCount < THRESH_HOLD_RECOGNITION) {
                // if the user done read at lease before the THRESH_HOLD_RECOGNITION fresh messages - enable speech recognition again
                this.doneReading = true;
                this.isListening = true;
                startRecognition();
                // place for voice notation
            }else{
                this.doneReading = false;
            }
        }
    }

    /**
     * startSettingActivity
     * This method will start the Setting screen Activity when clicking the Setting icon in the
     * Action-bar.
     *
     * @param view - Setting icon on the Action-Bar
     */
    public void startSettingActivity(View view){
        startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
    }

    /**
     * updateCheckRead
     * This method currently will be called from pressing one-time on Recognition message.
     * The main idea is to give the user control of the input flow of the conversation,
     * and continue it only when he is ready - a fundamental principle when we talk about
     * streaming the recognize input to a braille keyboard.
     *
     * @param view - onClickListener to any Recognize message from the chat
     */
    public void updateCheckRead(View view) {
        View parentRow = (View) view.getParent();
        ListView listView = (ListView) parentRow.getParent();
        int position = listView.getPositionForView(parentRow);
        // here we will replace this with just increasing position
        boolean fromService = messageAdapter.updateReadOnMessage(position); // update check mark on this message
        if (recognitionMessagesCount > 0){
            recognitionMessagesCount--;
        }
        if (!fromService && recognitionMessagesCount == THRESH_HOLD_RECOGNITION - 1){
            // if the user done read at lease before the 3 fresh messages - enable speech recognition again
            this.doneReading = true;
            this.isListening = true;
            this.recognizeSpeech = true; // do we want the user to fully control? currently the conversation is fluid
            startRecognition();
        }
    }

    /**
     * onResume is part of the app life-cycle.
     * We used this method to make adjustments after the user came back from Setting Activity
     * or start the app again (we are using SharedPreferences to save user-settings).
     *
     * This method covers Dark mode, app title, STT Language, TTS Language, Font size in chat
     * and many more User-Settings for the app features.
     */
    @Override
    protected void onResume() {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        boolean new_dark_mode = sharedPref.getBoolean("key_dark_mode", false);
        if (new_dark_mode != this.dark_mode){
            this.dark_mode = new_dark_mode;
            prefEditor.putBoolean("updatedDarkMode",true);
            prefEditor.commit();
            MainActivity.this.recreate();
        }

        allow_to_vibrate = sharedPref.getBoolean("key_vibrate", true);
        allow_to_speak = sharedPref.getBoolean("key_tts_enable", true);
        repeat_on_blank = sharedPref.getBoolean("key_repeat_enable", false);
        repeat_on_longpress = sharedPref.getBoolean("key_repeat_longpress", true);
        shortcut_template = sharedPref.getString("key_presave", "a");
        max_volume_enable = sharedPref.getBoolean("key_max_volume",true);
        currentCity = sharedPref.getString("currentCity", "");
        requestingLocationUpdates = sharedPref.getBoolean("requestingLocationUpdates", false);
        updateAppTitle(sharedPref.getString("key_myname", getString(R.string.app_name_title)));
        updateSTTLanguage(sharedPref.getString("key_language_stt", "en-US"));
        updateTTSLanguage(sharedPref.getString("key_language_tts", "en-US"));
        updateFontSize();
        // settings opened = false
        super.onResume();
    }

    /**
     * updateAppTitle
     * A simple method to adjust the app title in the Custom Action Bar to the desired @param name.
     * @param name - the desired new name in the Action Bar
     */
    private void updateAppTitle(String name){
        if (name!=null && !this.myName.equals(name) && findViewById(R.id.HawkingLogoText) != null){
            this.myName = name;
            TextView app_title = findViewById(R.id.HawkingLogoText);
            app_title.setText(name);
        }
    }

    /**
     * updateSTTLanguage
     * After the user has chose another Speak-To-Text language we need to create a new RecognizerIntent
     * to be fed to the RecognizeListener.
     *
     * @param language - language code parameter consists of a BCP-47 identifier for
     *                   e.g. 'he-IL', 'iw-IL', 'en-US'
     *
     */
    private void updateSTTLanguage(String language){
        // if the desired language is different from the current stt language
        if (!this.stt_language.equals(language)) {
            // if the desired language is hebrew, and there is not an internet connection
            this.stt_language = language;
            intent_recognize = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent_recognize.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent_recognize.putExtra(RecognizerIntent.EXTRA_LANGUAGE, this.stt_language);
            intent_recognize.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,true);
            //intent_recognize.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 30000);
        }
    }

    /**
     * updateTTSLanguage
     * After the user has chose another Text-To-Speech language we need to initialize tts object with
     * a new TextToSpeech object after using setLanguage with a Local object.
     * To avoid errors first we check if the chosen @param language is available in the device chosen TTS engine,
     * later on if this check failed we give the user a pop-up message with solutions.
     *
     * @param language - language code parameter consists of a BCP-47 identifier for
     *                   e.g. 'he-IL', 'iw-IL', 'en-US'
     */
    private void updateTTSLanguage(String language){
        final Locale new_locale = Locale.forLanguageTag(language);
        if (!this.tts_language.equals(language) || (tts != null && tts.isLanguageAvailable(new_locale) < 0)) {
            this.tts_language = language;
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(new_locale);
                    }
                }
            });
        }
    }

    /**
     * isNetworkAvailable
     * Simply check with the ConnectivityManager if there is an active connection to the Network
     * (internet) - used for some features required an active connection to the Network.
     * In current use this method is always inverted.
     *
     * @return if the device have an active connection to the internet
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * isLocationServiceAvailable
     * Simply check with the LocationManager if there is an Location Service available
     * (internet connection or GPS enable) - used for Train-mode feature that required an active Location Service.
     *
     * @return if the device have an Location Service available
     */
    private boolean isLocationServiceAvailable(){
        //TODO check for LocationManager.NETWORK_PROVIDER
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try { gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}
        return gps_enabled;
    }

    /**
     * getErrorText
     * A Method used to identify the error that occurred during Speech Recognition.
     *
     * @param errorCode int returned from Speech Recognition service
     * @return String to identify the error code for LOG.i purpose
     */
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    /**
     * sendMessage(View)
     * If the user press the SEND button (R.id = sendImageButton) and simply forward
     * the container text to sendMessage(String).
     *
     * @param view - R.id = sendImageButton
     */
    public void sendMessage(View view) {
        String message = myMessageText.getText().toString();
        sendMessage(message);
    }

    /**
     * sendMessage
     * This method is the main function for represent the User text (on-display keyboard or braille keyboard).
     * in the conversation flow.
     *
     * Supports pre-saved sentences activated with the shortcut template set in User-Settings.
     * Supports repeat the last message when the user send an empty message
     * Supports adjusting the output volume for TTS according to the User-Settings.
     *
     * If the current chosen TTS language is unavailable a POP-UP message will appear on
     * screen with solutions.
     *
     * @param message - the message to be spoken
     */
    public void sendMessage(String message) {
        String message_to_speak = "";
        int length = this.shortcut_template.length();
        if (message.length() == length + 1){
            if (message.substring(0,length).equals(this.shortcut_template) && Character.isDigit(message.charAt(length))){
                message =  sharedPref.getString("pref_presave_" + message.charAt(length) ,"");
                if (message.length() == 0){
                    Toast.makeText(this, "No such shortcut", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Add the message to the chat list-view
        if (message.length() > 0) {
            onMessage(message, 1);
            message_to_speak = message;
        }else if (repeat_on_blank){
            // the user pressed send on a blank message, repeat the prev message
            // need to be a setting !
            message_to_speak = messageAdapter.getLastUserMessage();
        }

        // if allowed to TTS, speak the Text entered in chat
        if (allow_to_speak && message_to_speak.length() > 0){
            if (this.isListening){
                clear_speech_recognition = true;
                speechRecognizer.stopListening();
            }
            // Check if the language is unavailable
            final Locale new_locale = Locale.forLanguageTag(this.tts_language);
            if (tts.isLanguageAvailable(new_locale) < 0){
                vibrator.vibrate(pattern_wrong, -1);
                TTSPopUpWindows();
            } else {
                // User-settings : if max volume is enabled in app settings - speak with the max volume
                if (max_volume_enable){
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                }
                tts.speak(message_to_speak, TextToSpeech.QUEUE_FLUSH, null, message_to_speak);
                Log.i(LOG_TAG, "spoke :" + message_to_speak);
            }
        }
        myMessageText.getText().clear();
    }

    /**
     * onMessage
     * This method is the main function for updating the conversation flow - from the Recognize speech
     * (or from Train-mode notification) to the User text (on-display keyboard or braille keyboard).
     * Using messageAdapter, we add the message to the ListView on the UI.
     *
     * We do NOT save the message in a DB for maximum security
     * (only in temp private vars in messageAdapter cleaned when the app is cleaned from memory).
     *
     * Its important to update the list with an runOnUiThread to not block other operations.
     * After adding a new message to the list we scroll down to keep up.
     *
     * @param InputMessage - the message itself
     * @param userID - {1,2,3} : '1' for the USER, '2' for Recognition, '3' for Train-Mode notification
     */
    public void onMessage(String InputMessage, Integer userID) {
        boolean belongsToCurrentUser = (userID == 1); // userID=1 is us, userID=2 is recognition
        MemberData data2Pass;
        // this is a message from train mode service
        if (userID == 3){
            data2Pass = new MemberData("TrainMode");
        } else{
            data2Pass = data;
        }
        final CustomMessage customMessage = new CustomMessage(InputMessage, data2Pass, belongsToCurrentUser, false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // add new message to the adapter
                messageAdapter.add(customMessage);
                // scroll the ListView to the last added element
                messagesView.setSelection(messagesView.getCount() - 1);
            }
        });
    }

    /**
     *
     */
    public void updateFontSize(){
        // increase the font size in chat
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean new_enable_bold = sharedPref.getBoolean("key_bold",false);
        int new_font_size = SettingsPrefActivity.getCurrent_font_size(); // this is relevant only to current session changes

        // if the app was closed (clean from memory)
        if (first_time_after_closed_app){
            first_time_after_closed_app = false;
            // restore the last font size
            new_font_size = sharedPref.getInt("font_size",20);
            SettingsPrefActivity.set_font_size(new_font_size);
            current_font_size = 20;
        }

        if(messageAdapter == null){
            return;
        }

        if (current_font_size != new_font_size || new_enable_bold != enable_bold) {
            current_font_size = new_font_size;
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putInt("font_size", current_font_size);
            prefEditor.commit();

            enable_bold = new_enable_bold;
            List<CustomMessage> old = new ArrayList<>();

            // is there messages in chat ?
            if (messageAdapter.getCount() > 0) {
                old = messageAdapter.getMessagesList();
            }

            // update the MessageAdapter with the new font size
            messageAdapter = new MessageAdapter(this, current_font_size, enable_bold);
            messagesView.setAdapter(messageAdapter);

            // add the previous messages with the new font size
            for (int i = 0; i < old.size(); i++){
                messageAdapter.add(old.get(i));
            }
            myMessageText.setTextSize(current_font_size);
        }
    }

    public void introduction_speech(){
        // incomplete function
        String message_to_speak = "m";
        tts.speak(message_to_speak, TextToSpeech.QUEUE_FLUSH, null, message_to_speak);
    }

    public void offlinePopUpWindows() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_stt, null);
        hideKeyboard(mRelativeLayout);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER, 0, 0);

        // Get a reference for the custom view close button
        View closeButton = popupView.findViewById(R.id.popup_btn_close);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });

        // Get a reference for the custom view close button
        View openSettingsButton = popupView.findViewById(R.id.popup_btn_settings_STT);

        // Set a click listener for the popup window close button
        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                final Intent intent = new Intent();
                intent.setComponent(new ComponentName(PACKAGE_NAME_GOOGLE_NOW, ACTIVITY_INSTALL_OFFLINE_FILES));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, 0);
            }
        });
    }

    public void TTSPopUpWindows() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_tts, null);
        hideKeyboard(mRelativeLayout);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER, 0, 0);

        // Get a reference for the custom view close button
        View closeButton = popupView.findViewById(R.id.popup_btn_close_TTS);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });

        // Get a reference for the custom view setting TTS button
        View openSettingsButton = popupView.findViewById(R.id.popup_btn_settings_TTS);

        // Set a click listener for the popup window setting TTS button
        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // Get a reference for the custom view TTS Engine download button
        View downloadTTSEngineButton = popupView.findViewById(R.id.popup_btn_download_TTS_engine);

        // Set a click listener for the popup window TTS Engine download button
        downloadTTSEngineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String appPackageName = "es.codefactory.vocalizertts"; // TTS engine
                try {
                    Intent appStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                    appStoreIntent.setPackage("com.android.vending");
                    startActivity(appStoreIntent);
                } catch (android.content.ActivityNotFoundException exception) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
    }

    public void trainModePopUpWindows() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_train_mode, null);
        hideKeyboard(mRelativeLayout);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER, 0, 0);

        // Get a reference for the custom view close button
        View closeButton = popupView.findViewById(R.id.popup_btn_close_train_mode);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });

        // Get a reference for the custom view close button
        View openSettingsButton = popupView.findViewById(R.id.popup_btn_settings_train_mode);

        // Set a click listener for the popup window close button
        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 0);
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void startTrainMode(View view){
        this.train_mode_status = !this.train_mode_status;

        if (this.train_mode_status){ // turn train mode on
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                        REQUEST_LOCATION_PERMISSION_CODE);
                // train mode will be activated when the user gave permission in on onRequestPermissionsResult method
                return;
            }
            if (!isLocationServiceAvailable() && !isNetworkAvailable()){
                vibrator.vibrate(pattern_wrong,-1);
                trainModePopUpWindows();
                return;
            }
            this.requestingLocationUpdates = true;
            Intent startIntent = new Intent(MainActivity.this, myServiceTrainMode.class);
            startIntent.putExtra("MESSENGER", new Messenger(messageHandler));// Start the Service with Handler object as an extra data
            startService(startIntent);
            pulseView_train.setIconRes(R.drawable.ic_train_on);
            pulseView_train.startPulse();

       }else{ // turn train mode off
            this.requestingLocationUpdates = false;
            Intent stopIntent = new Intent(MainActivity.this, myServiceTrainMode.class);
            stopService(stopIntent);
            pulseView_train.setIconRes(R.drawable.ic_train);
            pulseView_train.finishPulse();
        }
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putBoolean("requestingLocationUpdates",this.requestingLocationUpdates);
        prefEditor.apply();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.requestingLocationUpdates = true;
                    //startLocationUpdates();
                    Intent startIntent = new Intent(MainActivity.this, myServiceTrainMode.class);
                    startIntent.putExtra("MESSENGER", new Messenger(messageHandler));// Start the Service with Handler object as an extra data
                    startService(startIntent);
                    pulseView_train.setIconRes(R.drawable.ic_train_on);
                    pulseView_train.startPulse();
                } else {
                    // permission denied
                    this.train_mode_status = false;
                    vibrator.vibrate(pattern_wrong,-1);
                    trainModePopUpWindows();
                }
                break;

            case REQUEST_RECORD_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecognition();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.audio_needed_title)
                            .setMessage(R.string.audio_needed_summary) // Want to enable?
                            .setNegativeButton(R.string.audio_needed_ok, null)
                            .show();
                }
                break;

            default:
                break;
        }
    }
}