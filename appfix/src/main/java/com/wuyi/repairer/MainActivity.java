package com.wuyi.repairer;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
    /**
     * This is the method to be fixed
     */
    public void showToast(View view) {
        Toast.makeText(this, "Fixed!", Toast.LENGTH_SHORT).show();
    }
}
