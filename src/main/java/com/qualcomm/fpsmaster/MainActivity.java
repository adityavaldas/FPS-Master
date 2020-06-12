package com.qualcomm.fpsmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button fbutton, sbutton, abutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        abutton = ( Button ) findViewById(R.id.abutton);
        sbutton = ( Button ) findViewById(R.id.sbutton);
        fbutton = ( Button ) findViewById(R.id.fbutton);

        abutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runAutoFPS();
            }
        });
        fbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runForceFPS();
            }
        });
        sbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runStability();
            }
        });

    }

    public void runForceFPS() {
        Intent intent = new Intent(this, RunForceFPS.class);
        startActivity(intent);
    }

    public void runStability() {
        Intent intent = new Intent(this, RunStability.class);
        startActivity(intent);
    }

    public void runAutoFPS() {
        Intent intent = new Intent(this, AutoFPS.class);
        startActivity(intent);
    }
}
