package me.tatarka.jackport;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Objects.compare(1, 2, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return 0;
            }
        });
        Objects.deepEquals(1, 2);
        Objects.hash(1, 2, 3);
        Objects.hashCode(1);
        Objects.isNull(null);
        Objects.nonNull(null);
        Objects.requireNonNull("non null");
        Objects.requireNonNull("non null", "message");
//        Objects.requireNonNull("non null", new Supplier<String>() {
//            @Override
//            public String get() {
//                return null;
//            }
//        });
        Objects.toString(1);
        Objects.toString(null, "default");

        boolean equal = Objects.equals("1", "2");
        Toast.makeText(this, "equals = " + equal, Toast.LENGTH_SHORT).show();
    }
}
