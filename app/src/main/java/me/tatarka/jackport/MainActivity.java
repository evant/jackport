package me.tatarka.jackport;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

//import java.util.Objects;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

//        boolean equal = Objects.equals("1", "2");
//        Toast.makeText(this, "equals = " + equal, Toast.LENGTH_SHORT).show();
    }
}
