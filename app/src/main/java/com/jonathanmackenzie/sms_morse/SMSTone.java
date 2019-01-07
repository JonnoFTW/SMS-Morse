package com.jonathanmackenzie.sms_morse;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

/**
 * The SMSTone
 *
 * @author Jonathan Mackenzie
 * @email jonmac1@gmail.com
 */
public class SMSTone {

    private static AudioGenerator audioGenerator = new AudioGenerator(8000);
    private static final String TAG = SMSTone.class.getSimpleName();
    public static final SparseArray<String> morseTable = new SparseArray<String>(64);
    private int unit = 800;
    private int frequency = 800;

    /**
     * The table to convert from char to
     * morse code dots
     */
    static {
        morseTable.put('a', ".-");
        morseTable.put('b', "-...");
        morseTable.put('c', "-.-.");
        morseTable.put('d', "-..");
        morseTable.put('e', ".");
        morseTable.put('f', "..-.");
        morseTable.put('g', "--.");
        morseTable.put('h', "....");
        morseTable.put('i', "..");
        morseTable.put('j', ".---");
        morseTable.put('k', "-.-");
        morseTable.put('l', ".-..");
        morseTable.put('m', "--");
        morseTable.put('n', "-.");
        morseTable.put('o', "---");
        morseTable.put('p', ".--.");
        morseTable.put('q', "--.-");
        morseTable.put('r', ".-.");
        morseTable.put('s', "...");
        morseTable.put('t', "-");
        morseTable.put('u', "..-");
        morseTable.put('v', "...-");
        morseTable.put('w', ".--");
        morseTable.put('x', "-..-");
        morseTable.put('y', "-.--");
        morseTable.put('z', "--..");
        morseTable.put('1', ".----");
        morseTable.put('2', "..---");
        morseTable.put('3', "...--");
        morseTable.put('4', "....-");
        morseTable.put('5', ".....");
        morseTable.put('6', "-....");
        morseTable.put('7', "--...");
        morseTable.put('8', "---..");
        morseTable.put('9', "----.");
        morseTable.put('0', "-----");
        morseTable.put('.', ".-.-.-");
        morseTable.put(',', "-..--");
        morseTable.put('?', "..--..");
        morseTable.put('/', "-..-.");
        morseTable.put('\'', ".----.");
        morseTable.put('!', "-.-.--");
        morseTable.put('(', "-.--.");
        morseTable.put(')', "-.--.-");
        morseTable.put('&', ".-...");
        morseTable.put(':', "---...");
        morseTable.put(';', "-.-.-.");
        morseTable.put('=', "-...-");
        morseTable.put('+', ".-.-.");
        morseTable.put('-', "-....-");
        morseTable.put('_', "..--.-");
        morseTable.put('\"', ".-..-.");
        morseTable.put('$', "...-..-");
        morseTable.put('@', ".--.-.");
        morseTable.put('à', ".--.-");
        morseTable.put('ä', ".-.-");
        morseTable.put('å', ".--.-");
        morseTable.put('æ', ".-.-");
        morseTable.put('ć', "-.-..");
        morseTable.put('ĉ', "-.-..");
        morseTable.put('ç', "-.-..");
        morseTable.put('đ', "..-..");
        morseTable.put('ð', "..--.");
        morseTable.put('é', "..-..");
        morseTable.put('è', ".-..-");
        morseTable.put('ę', "..-..");
        morseTable.put('ĝ', "--.-.");
        morseTable.put('ĥ', "----");
        morseTable.put('ĵ', ".---.");
        morseTable.put('ł', ".-..-");
        morseTable.put('ń', "--.--");
        morseTable.put('ñ', "--.--");
        morseTable.put('ó', "---.");
        morseTable.put('ö', "---.");
        morseTable.put('ø', "---.");
        morseTable.put('ś', "...-...");
        morseTable.put('ŝ', "...-.");
        morseTable.put('š', "----");
        morseTable.put('þ', ".--..");
        morseTable.put('ü', "..--");
        morseTable.put('ŭ', "..--");
        morseTable.put('ź', "--..-.");
        morseTable.put('ż', "--..-");

    }

    /**
     * Create an SMS tone, the length and frequency of each tone
     * is taken from SharedPreferences
     *
     * @param context
     */
    public SMSTone(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        unit = Integer.parseInt(settings.getString("morse_code_speed", "800"));
        if (unit <= 0) {
            unit = 800;
        }
        frequency = Integer.parseInt(settings.getString("morse_code_frequency", "800"));
        if (frequency <= 0) {
            frequency = 800;
        }
        Log.i(TAG, "Frequency is: " + frequency + " tick is:" + unit);
    }

    /**
     * Play silence for u units
     *
     * @param u the number of units to play the silence for
     */
    private void silenceUnit(int u) {
        audioGenerator.writeSound(new double[unit * u]);
    }

    /**
     * Play a sound for u units,
     *
     * @param u the number of units to play the tone for
     */
    private void ditUnit(int u) {
        audioGenerator.writeSound(audioGenerator.getSineWave(unit * u, frequency));
    }

    /**
     * Convert a character string to morse code format
     *
     * @param message
     * @return the encoded string
     */
    public String convertToDots(String message) {
        StringBuilder sb = new StringBuilder();
        for (char i : message.toLowerCase().toCharArray()) {
            String morsed = morseTable.get(i);
            if (morsed != null) {
                sb.append(morsed).append("|");
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    /**
     * Plays a morse encoded string with | indicating end of
     * character. Unknown characters will not be played
     *
     * @param dots the dots to play
     */
    public void play(String dots) {
        dots = dots.replace("| ", " ");
        dots = "|" + dots + "|";
        Log.i(TAG, "Playing sequence: " + dots);
        audioGenerator.createPlayer();
        audioGenerator.getAudioTrack().setNotificationMarkerPosition(countFrames(dots));
        audioGenerator.getAudioTrack().setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {

            @Override
            public void onPeriodicNotification(AudioTrack track) {
            }

            @Override
            public void onMarkerReached(AudioTrack track) {
                stopTone();
            }
        });

        char[] chars = dots.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '.' || c == '•') {
                //Play a dit
                ditUnit(1);
                silenceUnit(1);
            } else if (c == '-') {
                // play a dash
                ditUnit(3);
                silenceUnit(1);
            } else if (c == '|') {
                // end of character
                silenceUnit(2);
            } else if (c == ' ') {
                // end of word
                silenceUnit(6);
            }
        }


    }

    private int countFrames(String dots) {
        int count = 0;
        for (int i = 0; i < dots.length(); i++) {
            switch (dots.charAt(i)) {
                case '-':
                    count += unit * 2;
                case '.':
                case '•':
                case '|':
                    count += unit * 2;
                    break;
                case ' ':
                    count += unit * 6;
                default:
            }
        }
        return count;
    }

    /**
     * Stop the tone from being played
     */
    public void stopTone() {
        audioGenerator.destroyAudioTrack();
    }

}
