package com.wuyi.repairer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wuyi.repairer.annotation.Fix;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Click Me! hehe");
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast();
            }
        });
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(30);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(tv);
    }

    /**
     * This is the method to be fixed
     */
    private void showToast() {
        Toast.makeText(this, "Something must be wrong...", Toast.LENGTH_SHORT).show();
    }

    /**
     * The fixed version method
     */
//    @Fix
//    private void showToast() {
//        Toast.makeText(this, "Aha! We fixed anything!", Toast.LENGTH_SHORT).show();
//    }
}
