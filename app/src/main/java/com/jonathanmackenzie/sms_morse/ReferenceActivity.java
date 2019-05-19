package com.jonathanmackenzie.sms_morse;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ReferenceActivity extends Activity {
    Typeface monospace;
    private static final String TAG = ReferenceActivity.class.getSimpleName();
    private SMSTone mTone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView lv = new ListView(this);
        monospace = Typeface.createFromAsset(getAssets(), "DroidSansMono.ttf");

        lv.setAdapter(new SparseStringsAdapter(this, SMSTone.morseTable));

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                (new Thread(new Runnable() {

                    @Override
                    public void run() {
                        String key = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                        String message = ((TextView) view.findViewById(android.R.id.text2)).getText().toString();
                        Log.i(TAG, "Playing: " + key + " " + message);
                        try {
                            mTone.play(message);
                        } catch (IllegalStateException e) {

                        }
                    }
                })).start();
            }
        });
        setContentView(lv);
    }

    public abstract class SparseArrayAdapter<E> extends BaseAdapter {

        private SparseArray<E> mData;

        public void setData(SparseArray<E> data) {
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public E getItem(int position) {
            return mData.valueAt(position);
        }

        @Override
        public long getItemId(int position) {
            return mData.keyAt(position);
        }

        public int getKey(int position) {
            return mData.keyAt(position);
        }
    }

    public class SparseStringsAdapter extends SparseArrayAdapter<String> {
        private final LayoutInflater mInflater;

        public SparseStringsAdapter(Context context, SparseArray<String> data) {
            mInflater = LayoutInflater.from(context);
            setData(data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = convertView;
            if (result == null) {
                result = mInflater.inflate(R.layout.morse_list_item, null);
            }
            char key = (char) getKey(position);
            TextView tvLeft = (TextView) result.findViewById(android.R.id.text1);
            TextView tvRight = (TextView) result.findViewById(android.R.id.text2);
            tvLeft.setText(Character.toString(key));
            tvRight.setTypeface(monospace);
            tvRight.setText(getItem(position).replace(".", "•"));
            return result;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mTone = new SMSTone(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTone.stopTone();
    }
}
