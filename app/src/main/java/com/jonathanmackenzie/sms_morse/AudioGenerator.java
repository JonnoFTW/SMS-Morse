package com.jonathanmackenzie.sms_morse;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioGenerator {

    private int sampleRate;
    private AudioTrack audioTrack;
    private static final String TAG = AudioGenerator.class.getSimpleName();

    public AudioGenerator(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public double[] getSineWave(int samples, double frequencyOfTone) {
        double[] sample = new double[samples];
        for (int i = 0; i < samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i
                    / (sampleRate / frequencyOfTone));
        }
        return sample;
    }

    public byte[] get16BitPcm(double[] samples) {
        byte[] generatedSound = new byte[2 * samples.length];
        int index = 0;
        for (double sample : samples) {
            // scale to maximum amplitude
            short maxSample = (short) ((sample * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[index++] = (byte) (maxSample & 0x00ff);
            generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);
        }
        return generatedSound;
    }

    public boolean hasPlayer() {
        return (audioTrack == null);
    }

    public void createPlayer() {
        audioTrack = new AudioTrack(AudioManager.STREAM_NOTIFICATION, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, sampleRate,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    public void writeSound(double[] samples) {
        byte[] generatedSnd = get16BitPcm(samples);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
    }

    public void destroyAudioTrack() {
        Log.d(TAG, "Audio track destroyed");
        if (audioTrack == null || audioTrack.getState() == AudioTrack.STATE_UNINITIALIZED)
            return;
        audioTrack.stop();
        audioTrack.release();
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }
}