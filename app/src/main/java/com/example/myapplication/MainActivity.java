package com.example.myapplication;
import android.os.Handler;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Balloon.BalloonListener {

    private static final int MIN_ANIMATION_DELAY = 500;
    private static final int MAX_ANIMATION_DELAY = 1500;
    private static final int MIN_ANIMATION_DURATION = 1000;
    private static final int MAX_ANIMATION_DURATION = 8000;
    private static final int NUMBER_OF_PINS = 5;
    private static final int BALLOONS_PER_LEVEL = 10;

    private ViewGroup mContentView;
    private int[] mBalloonColors = new int[4]; // Updated for seven colors

    private int mNextColor, mScreenWidth, mScreenHeight;
    TextView mScoreDisplay, mLevelDisplay;
    private List<ImageView> mPinImages = new ArrayList<>();
    private List<Balloon> mBalloons = new ArrayList<>();
    Button mGoButton;

    private boolean mPlaying;
    private boolean mGameStopped = true;
    private int mLevel, mScore, mPinsUsed;
    private int mBalloonsPopped;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private SoundHelper mSoundHelper;

    private String[] balloonMessages = {"Blue Balloon - Burst it!", "Red Balloon - Burst it!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize balloon colors
        mBalloonColors[0] = Color.argb(255, 255, 0, 0);  // RED
        mBalloonColors[1] = Color.argb(255, 165, 42, 42);  // Brown
        mBalloonColors[2] = Color.argb(255, 0, 0, 255);   // BLUE
        mBalloonColors[3] = Color.argb(255, 255, 165, 0);  // Orange

        getWindow().setBackgroundDrawableResource(R.drawable.modern_background);

        mContentView = findViewById(R.id.activity_main);
        setToFullScreen();

        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenHeight = mContentView.getHeight();
                    mScreenWidth = mContentView.getWidth();
                }
            });
        }

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setToFullScreen();
            }
        });

        mScoreDisplay = findViewById(R.id.score_display);
        mLevelDisplay = findViewById(R.id.level_display);
        mPinImages.add(findViewById(R.id.pushpin1));
        mPinImages.add(findViewById(R.id.pushpin2));
        mPinImages.add(findViewById(R.id.pushpin3));
        mPinImages.add(findViewById(R.id.pushpin4));
        mPinImages.add(findViewById(R.id.pushpin5));
        mGoButton = findViewById(R.id.go_button);

        updateDisplay();

        mSoundHelper = new SoundHelper(this);
        mSoundHelper.prepareMusicPlayer(this);
    }

    private void setToFullScreen() {
        mContentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
    }

    private void startGame() {
        setToFullScreen();
        mScore = 0;
        mLevel = 0;
        mPinsUsed = 0;

        for (ImageView pin : mPinImages) {
            pin.setImageResource(R.drawable.pin);
        }

        mGameStopped = false;
        startLevel();
        mSoundHelper.playMusic();
    }

    private void startLevel() {
        mLevel++;
        updateDisplay();
        BalloonLauncher balloonLauncher = new BalloonLauncher();
        balloonLauncher.execute(mLevel);
        mPlaying = true;
        mBalloonsPopped = 0;
        mGoButton.setText("Stop Game");
    }

    private void finishLevel() {
        Toast.makeText(this, String.format("You finished level %d", mLevel), Toast.LENGTH_SHORT)
                .show();
        mPlaying = false;
        mGoButton.setText(String.format("Start level %d", mLevel + 1));
    }

    public void goButtonClickHandler(View view) {
        if (mPlaying) {
            gameOver(false);
        } else if (mGameStopped) {
            startGame();
        } else {
            startLevel();
        }
    }

    @Override
    public void popBalloon(Balloon balloon, boolean userTouch) {
        mSoundHelper.playSound();
        mBalloonsPopped++;

        mContentView.removeView(balloon);
        mBalloons.remove(balloon);

        if (userTouch) {
            mScore++;
        } else {
            mPinsUsed++;
            if (mPinsUsed <= mPinImages.size()) {
                mPinImages.get(mPinsUsed - 1).setImageResource(R.drawable.pin_off);
            }
            if (mPinsUsed == NUMBER_OF_PINS) {
                gameOver(true);
                return;
            } else {
                Toast.makeText(this, "Missed that one!", Toast.LENGTH_SHORT).show();
            }
        }
        updateDisplay();

        if (mBalloonsPopped == BALLOONS_PER_LEVEL) {
            finishLevel();
        }
    }

    private void gameOver(boolean allPinsUsed) {
        mSoundHelper.pauseMusic();
        Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show();

        for (Balloon balloon : mBalloons) {
            mContentView.removeView(balloon);
            balloon.setPopped(true);
        }

        mBalloons.clear();
        mPlaying = false;
        mGameStopped = true;
        mGoButton.setText("Start Game");

        if (allPinsUsed) {
            // Check for high score and display a dialog if achieved
            if (HighScoreHelper.isTopScore(this, mScore)) {
                HighScoreHelper.setTopScore(this, mScore);

                SimpleAlertDialog dialog = SimpleAlertDialog.newInstance("New High Score!",
                        String.format("Your new high score is %d", mScore));
                dialog.show(getSupportFragmentManager(), null);
            }
        }
    }

    private void updateDisplay() {
        mScoreDisplay.setText(String.valueOf(mScore));
        mLevelDisplay.setText(String.valueOf(mLevel));
    }

    @Override
    protected void onStop() {
        super.onStop();
        gameOver(false);
    }

    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            if (params.length != 1) {
                throw new AssertionError("Expected 1 param for current level");
            }

            int level = params[0];
            int maxDelay = Math.max(MIN_ANIMATION_DELAY,
                    (MAX_ANIMATION_DELAY - ((level - 1) * 500)));
            int minDelay = Math.max(maxDelay / 2, 1); // Ensure minDelay is positive

            int balloonsLaunched = 0;
            while (mPlaying && balloonsLaunched < BALLOONS_PER_LEVEL) {
                Random random = new Random();
                int xPosition = random.nextInt(mScreenWidth - 200);
                publishProgress(xPosition);
                balloonsLaunched++;

                int delay = Math.max(random.nextInt(minDelay) + minDelay, 1); // Ensure delay is positive
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int xPosition = values[0];
            launchBalloon(xPosition);
        }
    }


    private void launchBalloon(int x) {
        Balloon balloon = new Balloon(this, mBalloonColors[mNextColor], 150);
        mBalloons.add(balloon);

        if (mNextColor + 1 == mBalloonColors.length) {
            mNextColor = 0;
        } else {
            mNextColor++;
        }

        balloon.setX(x);
        balloon.setY(mScreenHeight + balloon.getHeight());
        mContentView.addView(balloon);

        int duration = Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel * 1000));

        // Assign the color to the balloonColor variable
        int balloonColor = mBalloonColors[mNextColor];

        // Set OnClickListener for the balloon after adding it to the mContentView
        balloon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (balloonColor == Color.RED) {
                    Toast.makeText(MainActivity.this, "You popped the red balloon!", Toast.LENGTH_SHORT).show();
                } else if (balloonColor == Color.BLUE) {
                    Toast.makeText(MainActivity.this, "You popped the blue balloon!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Balloon clicked!", Toast.LENGTH_SHORT).show();
                }
                popBalloon(balloon, true);
            }
        });

        balloon.releaseBalloon(mScreenHeight, duration);
    }
}
