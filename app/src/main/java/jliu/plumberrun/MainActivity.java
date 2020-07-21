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
        TransitionManager.go(Scene.getSceneForLayout((ViewGroup) findViewById(R.id.root_container), R.layout.recycler_view, context));
        initRecyclerView();
    }

    private void initRecyclerView() {
        //set level names
        ArrayList<String> levelNames = new ArrayList<>();
        levelNames.add("Level 1");
        levelNames.add("Level 2");
        levelNames.add("Level 3");
        levelNames.add("Level 4");
        levelNames.add("Level 5");

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, levelNames);
        recyclerView.setAdapter(adapter);

        LinearSnapHelper snapper = new LinearSnapHelper();
        snapper.attachToRecyclerView(recyclerView);

        ImageButton start = findViewById(R.id.start);
        applyButtonEffect(start);

        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                TransitionManager.go(Scene.getSceneForLayout((ViewGroup) findViewById(R.id.root_container), R.layout.game_view, context));
                startGame();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startGame() {
        final Game currentGame = findViewById(R.id.game_view);
        final ViewGroup loadScreen = findViewById(R.id.game_load_screen);
        final LoadLock loadLock = new LoadLock();
        final ImageButton replayButton = findViewById(R.id.replay_button);
        final ImageButton menuButton = findViewById(R.id.menu_button);
        final ImageButton pauseButton = findViewById(R.id.pause_button);
        final ImageButton settingsButton = findViewById(R.id.settings_button);

        runWithLoadAnim(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                fadeAnim(loadScreen, false, 300);  //fade out load transition
                currentGame.startGameLoop();
                startGameThread(currentGame, replayButton, menuButton); //wait for game to complete
            }
        }, loadLock);

        applyButtonEffect(replayButton);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scaleAnim(findViewById(R.id.level_complete), false);
                currentGame.resetLevel();
                startGameThread(currentGame, replayButton, menuButton); //restart thread
            }
        });

        applyButtonEffect(menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                replayButton.setClickable(false);
                menuButton.setClickable(false);
                currentGame.endGameLoop();
                loadMenu(loadScreen);
            }
        });

        //pause button
        applyButtonEffect(pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                scaleAnim(findViewById(R.id.pause_screen), true);
                currentGame.pauseGame();
                final ImageButton menuButton = findViewById(R.id.pause_menu_button);
                final ImageButton resumeButton = findViewById(R.id.resume_button);

                applyButtonEffect(menuButton);
                menuButton.clearColorFilter();
                menuButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menuButton.setClickable(false);
                        resumeButton.setClickable(false);
                        currentGame.endGameLoop();
                        loadMenu(loadScreen);
                    }
                });

                applyButtonEffect(resumeButton);
                resumeButton.clearColorFilter();
                resumeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //automatically set as clickable when instantiated
                        menuButton.setClickable(false);
                        resumeButton.setClickable(false);
                        scaleAnim(findViewById(R.id.pause_screen), false);
                        currentGame.resumeGame();
                    }
                });
            }
        });

        //update load lock
        currentGame.loadLevel(0);
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
                                TransitionManager.go(Scene.getSceneForLayout((ViewGroup) findViewById(R.id.root_container), R.layout.recycler_view, context));
                                initRecyclerView();
                            }
                        }, null);
                    }
                });
            }
        }).start();
    }

    private void startGameThread(final Game currentGame, final ImageButton replay, final ImageButton menu) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                replay.setClickable(false);
                menu.setClickable(false);
                synchronized (currentGame) {
                    while (currentGame.gameInProgress()) {
                        try {
                            currentGame.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //level complete
                replay.setClickable(true);
                menu.setClickable(true);
                replay.clearColorFilter();
                menu.clearColorFilter();
                scaleAnim(findViewById(R.id.level_complete), true);
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
