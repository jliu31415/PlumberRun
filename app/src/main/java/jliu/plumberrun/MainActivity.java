package jliu.plumberrun;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private AppCompatActivity context;
    private ArrayList<String> levelNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button play = findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        setContentView(R.layout.recycler_view);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, levelNames);
        recyclerView.setAdapter(adapter);

        LinearSnapHelper snapper = new LinearSnapHelper();
        snapper.attachToRecyclerView(recyclerView);

        Button start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(new Game(context, 0));
            }
        });
    }
}
