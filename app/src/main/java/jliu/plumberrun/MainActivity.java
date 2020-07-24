package jliu.plumberrun;

import android.annotation.SuppressLint;
import android.graphics.LightingColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private AppCompatActivity context;

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_transition);
        new Thread(new Runnable() {
            @Override
            public void run() {
                delayThread(1000);
                runWithLoadAnim(new Runnable() {
                    @Override
                    public void run() {
                        goToMain();
                    }
                }, null);
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void goToMain() {
        TransitionManager.go(Scene.getSceneForLayout((ViewGroup) findViewById(R.id.root_container), R.layout.main, context));

        ImageButton playButton = findViewById(R.id.play_button);
        applyButtonEffect(playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                goToGame();
            }
        });
    }

    private void initRecyclerView() {
        ArrayList<String> cardNames = new ArrayList<>();
        cardNames.add("1");
        cardNames.add("2");
        cardNames.add("3");
        cardNames.add("4");
        cardNames.add("5");

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, cardNames);
        recyclerView.setAdapter(adapter);

        LinearSnapHelper snapper = new LinearSnapHelper();
        snapper.attachToRecyclerView(recyclerView);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void goToGame() {
        TransitionManager.go(Scene.getSceneForLayout((ViewGroup) findViewById(R.id.root_container), R.layout.game_view, context));
        final Game currentGame = findViewById(R.id.game_view);
        final ViewGroup loadScreen = findViewById(R.id.load_screen);
        loadScreen.setVisibility(View.VISIBLE);
        final LoadLock loadLock = new LoadLock();
        final ImageButton pauseButton = findViewById(R.id.pause_button);
        final ImageButton menuButton = findViewById(R.id.menu_button);
        final ImageButton continueButton = findViewById(R.id.play_button);
        final ImageButton replayButton = findViewById(R.id.replay_button);

        new Thread(new Runnable() {
            @Override
            public void run() {
                delayThread(500);
                runWithLoadAnim(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        delayThread(500);
                        fadeAnim(loadScreen, false, 300);  //fade out load transition
                        currentGame.startGameLoop();
                        startGameThread(currentGame); //wait for game to complete
                    }
                }, loadLock);
            }
        }).start();

        //pause button
        applyButtonEffect(pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                if (currentGame.hasStarted()) {
                    menuButton.setClickable(true);
                    continueButton.setClickable(true);
                    menuButton.clearColorFilter();
                    continueButton.clearColorFilter();
                    replayButton.setVisibility(View.INVISIBLE);
                    currentGame.pauseGame();
                    scaleAnim(findViewById(R.id.pop_up_container), true);
                }
            }
        });

        applyButtonEffect(menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                menuButton.setClickable(false);
                replayButton.setClickable(false);
                continueButton.setClickable(false);
                currentGame.endGameLoop();
                loadMenu(loadScreen);
            }
        });

        applyButtonEffect(continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuButton.setClickable(false);
                continueButton.setClickable(false);
                currentGame.resumeGame();
                scaleAnim(findViewById(R.id.pop_up_container), false);
            }
        });

        applyButtonEffect(replayButton);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuButton.setClickable(false);
                replayButton.setClickable(false);
                scaleAnim(findViewById(R.id.pop_up_container), false);
                currentGame.resetLevel();
                startGameThread(currentGame); //restart thread
            }
        });

        //update load lock
        currentGame.loadLevel();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (currentGame.levelLoading()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                loadLock.unlock();
            }
        }).start();
    }

    private void delayThread(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void loadMenu(final View loadScreen) {
        final int fadeDuration = 200;
        fadeAnim(loadScreen, true, fadeDuration);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(fadeDuration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.loading_transition);
                        runWithLoadAnim(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void run() {
                                goToMain();
                            }
                        }, null);
                    }
                });
            }
        }).start();
    }

    private void startGameThread(final Game currentGame) {
        final ImageButton menuButton = findViewById(R.id.menu_button);
        final ImageButton replayButton = findViewById(R.id.replay_button);
        final ImageButton continueButton = findViewById(R.id.play_button);

        new Thread(new Runnable() {
            @Override
            public void run() {
                currentGame.resumeGame();

                menuButton.setClickable(false);
                replayButton.setClickable(false);
                continueButton.setClickable(false);

                synchronized (currentGame) {
                    while (currentGame.gameInProgress()) {
                        try {
                            currentGame.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //player death
                menuButton.setClickable(true);
                replayButton.setClickable(true);
                menuButton.clearColorFilter();
                replayButton.clearColorFilter();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        replayButton.setVisibility(View.VISIBLE);
                    }
                });
                scaleAnim(findViewById(R.id.pop_up_container), true);
                currentGame.pauseGame();
            }
        }).start();
    }

    private void scaleAnim(final View v, final boolean scaleMode) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(View.VISIBLE);
                int temp = scaleMode ? 0 : 1;
                ScaleAnimation scale = new ScaleAnimation(temp, 1 - temp, temp, 1 - temp,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scale.setInterpolator(new DecelerateInterpolator());
                scale.setDuration(300);
                scale.setFillAfter(true);
                v.startAnimation(scale);
            }
        });
    }

    private void fadeAnim(final View view, boolean fadeMode, int duration) {
        AlphaAnimation alpha = fadeMode ? new AlphaAnimation(0, 1) : new AlphaAnimation(1, 0);
        alpha.setDuration(duration);
        alpha.setFillAfter(true);
        alpha.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(alpha);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void runWithLoadAnim(@Nullable final Runnable run, @Nullable final LoadLock loadLock) {
        new Thread(new Runnable() {
            private final LoadAnim load = new LoadAnim(loadLock);

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                load.start();
                synchronized (load) {
                    try {
                        load.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (run != null) context.runOnUiThread(run);
            }
        }).start();
    }

    static class LoadLock {
        private boolean locked;

        LoadLock() {
            locked = true;
        }

        void unlock() {
            this.locked = false;
        }

        boolean isLocked() {
            return locked;
        }
    }

    class LoadAnim extends Thread {
        private LoadLock loadLock;

        LoadAnim(@Nullable LoadLock loadLock) {
            this.loadLock = loadLock;
        }

        @Override
        public void run() {
            int duration = 500;
            ImageView image = findViewById(R.id.plunger);

            while (image.getWidth() * image.getHeight() == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            AnimationSet animSet = new AnimationSet(false);
            ScaleAnimation s1 = new ScaleAnimation(-1, 1, 1, 1,
                    (float) Math.ceil(image.getWidth() / 2.0), (float) Math.ceil(image.getHeight() / 2.0));
            ScaleAnimation s2 = new ScaleAnimation(-1, 1, 1, 1,
                    (float) Math.ceil(image.getWidth() / 2.0), (float) Math.ceil(image.getHeight() / 2.0));
            s1.setStartOffset(duration);
            s2.setStartOffset(2 * duration);
            animSet.addAnimation(s1);
            animSet.addAnimation(s2);
            animSet.setDuration(duration);
            animSet.setFillAfter(true);

            do {
                image.startAnimation(animSet);

                while (!image.getAnimation().hasEnded()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (loadLock != null && loadLock.isLocked());

            synchronized (this) {
                notify();
            }
        }
    }

    private void applyButtonEffect(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ((ImageView) v).setColorFilter(new LightingColorFilter(0xffcccccc, 0x000000));
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        ((ImageView) v).clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }
}
