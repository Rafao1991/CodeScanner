package com.rafao.codescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rafao.codescanner.codescan.CodeScanActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, CodeScanActivity.class)
                        .putExtra("width", 2500).putExtra("height", 640), 0);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, CodeScanActivity.class)
                        .putExtra("width", 800).putExtra("height", 800), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0)
            ((Button) findViewById(R.id.button)).setText(data.getStringExtra("item"));
        else if (requestCode == 1)
            ((Button) findViewById(R.id.button2)).setText(data.getStringExtra("item"));
    }
}
