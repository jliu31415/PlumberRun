package jliu.plumberrun;

import android.os.Build;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
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
    private ArrayList<String> levelNames;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    private void setLevelNames() {
        levelNames = new ArrayList<>();
        levelNames.add("Level 1");
        levelNames.add("Level 2");
        levelNames.add("Level 3");
        levelNames.add("Level 4");
        levelNames.add("Level 5");
    }

    private void initRecyclerView() {
        setLevelNames();

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
                TransitionManager.go(Scene.getSceneForLayout(root, R.layout.loading_transition, context));

                new Thread(new Runnable() {
                    private final LoadAnim load = new LoadAnim();

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
                        beginGame();
                    }
                }).start();

            }
        });
    }

    private void beginGame() {
        context.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                setContentView(new Game(context, 0));
            }
        });
    }

    class LoadAnim extends Thread {
        private int offset = 100, numCycles = 1;

        @Override
        public void run() {
            ImageView image = null;

            while (numCycles-- > 0) {
                for (int i = 1; i <= 6; i++) {
                    switch (i % 6) {
                        case 0:
                            image = findViewById(R.id.plunger1);
                            break;
                        case 1:
                            image = findViewById(R.id.plunger2);
                            break;
                        case 2:
                            image = findViewById(R.id.plunger3);
                            break;
                        case 3:
                            image = findViewById(R.id.plunger4);
                            break;
                        case 4:
                            image = findViewById(R.id.plunger5);
                            break;
                        case 5:
                            image = findViewById(R.id.plunger6);
                            break;
                    }

                    image.startAnimation(getFadeAnim(i + 3));
                }

                while (!image.getAnimation().hasEnded()) {
                    try {
                        Thread.sleep(offset);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (this) {
                notify();
            }
        }

        private AnimationSet getFadeAnim(int index) {
            //duration of animation is 2 * offset
            AnimationSet s = new AnimationSet(false);
            s.addAnimation(new AlphaAnimation(0, 1));
            s.addAnimation(new AlphaAnimation(1, 0));
            s.getAnimations().get(0).setStartOffset(index * offset);
            s.getAnimations().get(1).setStartOffset((index + 2) * offset);
            s.setDuration(2 * offset);
            s.setFillAfter(true);
            return s;
        }
    }
}
