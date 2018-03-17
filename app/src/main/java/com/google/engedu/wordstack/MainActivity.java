/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private int c1, c2, c3;
    private Stack<LetterTile> placedTiles = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if (WORD_LENGTH<=word.length())
                {
                    words.add(word);
                }

            }
            Log.i("tset", words.size()+"");
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                    placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }
                    else {
                        placedTiles.push(tile);
                    }
                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        ViewGroup word1linearlayout = (ViewGroup) findViewById(R.id.word1);
        ViewGroup word2linearlayout = (ViewGroup) findViewById(R.id.word2);
        word1linearlayout.removeAllViews();
        word2linearlayout.removeAllViews();
        stackedLayout.clear();
//        messageBox.setText("Game started");
        c1 = 0;
        c2 = 0;
        c3 = 0;

        word1 = words.get((int) (Math.random() * words.size()));
        word2 = words.get((int) (Math.random() * words.size()));
        Log.i("ans", word1 + "  " + word2);
        char w1[] = word1.toCharArray();
        char w2[] = word2.toCharArray();
        int l1 = w1.length;
        int l2 = w2.length;
        char shuffled[] = new char[l1+l2];
        while(c1!=l1 && c2!=l2)
        {
            int counter = random.nextInt(2);
            if (counter == 0)
            {
                shuffled[c3] = w1[c1];
                c1++;
                c3++;
            }
            else
            {
                shuffled[c3] = w2[c2];
                c2++;
                c3++;
            }
        }
        if (c1==l1)
        {
            while (c2!=l2)
            {
                shuffled[c3] = w2[c2];
                c2++;
                c3++;
            }
        }
        else if (c2 == l2)
        {
            while (c1!=l1)
            {
                shuffled[c3] = w1[c1];
                c1++;
                c3++;
            }
        }
        String shuffle = new String(shuffled);
        for (int index = shuffle.length()-1; index>=0; index--)
        {
            stackedLayout.push(new LetterTile(this, shuffle.charAt(index)));
        }
        messageBox.setText(shuffle);
        return true;
    }

    public boolean onUndo(View view) {

        if (!placedTiles.empty()){
            LetterTile letter = placedTiles.pop();
            letter.moveToViewGroup(stackedLayout);
        }

        return true;
    }
}
