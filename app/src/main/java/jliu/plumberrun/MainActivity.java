package jliu.plumberrun;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
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
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private AppCompatActivity context;
    private ViewGroup root;

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root_container);

        TransitionManager.go(Scene.getSceneForLayout(root, R.layout.title_screen, context));

        Button play = findViewById(R.id.play);
        applyButtonEffect(play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLevelSelect();
            }
        });
    }

    private void goToLevelSelect() {
        context.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                TransitionManager.go(Scene.getSceneForLayout(root, R.layout.recycler_view, context));
                initRecyclerView();
            }
        });
    }

    private void initRecyclerView() {
        //set level names
        ArrayList<String> levelNames = new ArrayList<>();
        levelNames.add("Level 1");
        levelNames.add("Level 2");
        levelNames.add("Level 3");
        levelNames.add("Level 4");
        levelNames.add("Level 5");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, levelNames);
        recyclerView.setAdapter(adapter);

        LinearSnapHelper snapper = new LinearSnapHelper();
        snapper.attachToRecyclerView(recyclerView);

        Button start = findViewById(R.id.start);
        applyButtonEffect(start);
        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                TransitionManager.go(Scene.getSceneForLayout(root, R.layout.game_view, context));
                final Game currentGame = findViewById(R.id.game_view);
                final ViewGroup loadScreen = findViewById(R.id.load_screen);
                loadScreen.setVisibility(View.VISIBLE);
                currentGame.loadLevel(0);

                final LoadLock loadLock = new LoadLock();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadLock.lock(true);
                        while (currentGame.levelLoading()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        loadLock.lock(false);
                    }
                }).start();

                runWithLoadAnim(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                currentGame.startGameLoop();

                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlphaAnimation alpha = new AlphaAnimation(1, 0);
                                        alpha.setDuration(500);
                                        alpha.setFillAfter(true);
                                        loadScreen.startAnimation(alpha);
                                    }
                                });

                                synchronized (currentGame) {
                                    while (currentGame.gameInProgress()) {
                                        try {
                                            currentGame.wait();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1,
                                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                        scale.setInterpolator(new BounceInterpolator());
                                        scale.setDuration(1000);
                                        scale.setFillAfter(true);
                                        ImageView levelComplete = findViewById(R.id.level_complete);
                                        levelComplete.setVisibility(View.VISIBLE);
                                        levelComplete.startAnimation(scale);
                                    }
                                });

                                //goToLevelSelect();
                            }
                        }).start();
                    }
                }, loadLock);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void runWithLoadAnim(final Runnable run, final LoadLock loadLock) {
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

                context.runOnUiThread(run);
            }
        }).start();
    }

    static class LoadLock {
        private boolean locked;

        void lock(boolean locked) {
            this.locked = locked;
        }

        boolean isLocked() {
            return locked;
        }
    }

    class LoadAnim extends Thread {
        private LoadLock loadLock;

        LoadAnim(LoadLock loadLock) {
            this.loadLock = loadLock;
        }

        @Override
        public void run() {
            int duration = 500;
            ImageView image = findViewById(R.id.plunger);

            AnimationSet animSet = new AnimationSet(false);
            while (image.getWidth() * image.getHeight() == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

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
            } while (loadLock.isLocked());

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
                        v.getBackground().setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }
}
