package jliu.plumberrun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> levelNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final AppCompatActivity context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button play = findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.recycler_view);
                setLevelNames();
                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(layoutManager);
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, levelNames);
                recyclerView.setAdapter(adapter);
                //setContentView(new Game(context, 0));
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
}
