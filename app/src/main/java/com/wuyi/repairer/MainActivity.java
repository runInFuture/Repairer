package com.wuyi.repairer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wuyi.repairer.patch.Repairer;
import com.wuyi.repairer.runtime.AndroidInstantRuntime;
import com.wuyi.repairer.runtime.IncrementalChange;

import java.io.File;

public class MainActivity extends Activity {
    public static final long serialVersionUID = 4273359790103098749L;
    public static IncrementalChange $change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showToast2(View view) {
        IncrementalChange var1 = (IncrementalChange) AndroidInstantRuntime.getPrivateField(this, MainActivity.class, "$change");
        if (var1 != null) {
            var1.access$dispatch("showToast.()V", new Object[]{this});
        } else {
            Toast.makeText(this, "  Something must be wrong...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This is the method to be fixed
     */
    public void showToast(View view) {
        Toast.makeText(this, "  Something must be wrong...", Toast.LENGTH_SHORT).show();
    }

    public void patch(View view) {
        Repairer.Patch patch = new Repairer.Patch();
        patch.optDir = new File(MainActivity.this.getFilesDir(), "fix_opt");
        patch.optDir.mkdirs();
        new Repairer().apply(patch);
    }
}
