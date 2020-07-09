package jliu.plumberrun;

import android.os.Build;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
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
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                runWithLoadAnim(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        TransitionManager.go(Scene.getSceneForLayout(root, R.layout.game_view, context));
                        final Game currentGame = findViewById(R.id.game_view);
                        currentGame.setLevel(0);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (currentGame) {
                                    while (!currentGame.isDone()) {
                                        try {
                                            currentGame.wait();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                currentGame.endGameLoop();
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TransitionManager.go(Scene.getSceneForLayout(root, R.layout.recycler_view, context));
                                        initRecyclerView();
                                    }
                                });
                            }
                        }).start();
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void runWithLoadAnim(final Runnable changeLayout) {
        TransitionManager.go(Scene.getSceneForLayout(root, R.layout.loading_transition, context));

        new Thread(new Runnable() {
            private final LoadAnim load = new LoadAnim();

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

                context.runOnUiThread(changeLayout);
            }
        }).start();
    }

    class LoadAnim extends Thread {
        @Override
        public void run() {
            int duration = 500, numCycles = 1;
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

            for (int i = 0; i < numCycles; i++) {
                image.startAnimation(animSet);

                while (!image.getAnimation().hasEnded()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (this) {
                notify();
            }
        }
    }
}
