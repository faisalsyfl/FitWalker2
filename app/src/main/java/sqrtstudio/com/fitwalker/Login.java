package sqrtstudio.com.fitwalker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static sqrtstudio.com.fitwalker.AppConfig.SP;
public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Typeface fontStyle01 = Typeface.createFromAsset(getAssets(),"fonts/AAUX.ttf");
        Typeface fontStyle02 = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Regular.ttf");

        TextView textView1 = (TextView)findViewById(R.id.textView1);
        TextView textView2 = (TextView)findViewById(R.id.textView2);
        TextView textView3 = (TextView)findViewById(R.id.textView);

        Button goButton = (Button)findViewById(R.id.button);

        textView1.setTypeface(fontStyle01);
        textView2.setTypeface(fontStyle01);
        textView3.setTypeface(fontStyle02);
        goButton.setTypeface(fontStyle02);
        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
        if(!sp.getString("owner","empty").equals("empty")){
            Intent i = new Intent(this,Main.class);
            startActivity(i);
            finish();
        }
    }

    public void readyToGo(View v){
        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        ed.putString("owner",((EditText)findViewById(R.id.editText)).getText().toString());
        ed.putFloat("dist",0);
        ed.putBoolean("b1",false);
        ed.putBoolean("b2",false);
        ed.putBoolean("b3",false);
        ed.putBoolean("b4",false);
        ed.commit();
        Intent i = new Intent(this,Main.class);
        startActivity(i);
        finish();

    }
}
