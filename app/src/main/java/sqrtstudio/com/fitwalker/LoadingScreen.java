package sqrtstudio.com.fitwalker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

public class LoadingScreen extends AppCompatActivity {
    private ProgressBar progress;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        progress = (ProgressBar) findViewById(R.id.progressBar5);
        progress.setIndeterminate(true);
        Thread background = new Thread() {
            public void run() {

                try {
                    // Thread will sleep for 5 seconds
                    sleep(5*1000);

                    // After 5 seconds redirect to another intent
                    Intent i=new Intent(getBaseContext(),Main.class);
                    startActivity(i);

                    //Remove activity
                    finish();

                } catch (Exception e) {

                }
            }
        };
        // start thread
        background.start();
        Log.d("pb",String.valueOf(progress.getProgress()));
        if(progress.getProgress() == 100){
            Intent i = new Intent(this,Main.class);
            startActivity(i);
            finish();
        }
    }
}
