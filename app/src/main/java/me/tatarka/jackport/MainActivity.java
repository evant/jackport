package me.tatarka.jackport;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Objects;
import java.util.function.Predicate;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Predicate<String> equals2 = str -> Objects.equals(str, "2");
        boolean equal = equals2.test("1");
        Toast.makeText(this, "equals = " + equal, Toast.LENGTH_SHORT).show();
    }
}
