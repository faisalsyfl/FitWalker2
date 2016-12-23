package sqrtstudio.com.fitwalker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.BigDecimal;

import static sqrtstudio.com.fitwalker.AppConfig.SP;

public class Profile extends AppCompatActivity {

    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        Intent i = getIntent();

        TextView oT = (TextView) findViewById(R.id.ownerText);
        oT.setText(sp.getString("owner","#UNDEFINED"));

        TextView mcT = (TextView) findViewById(R.id.countMarkerText);
        mcT.setText(i.getStringExtra("count"));
        ed.putString("count",i.getStringExtra("count"));
        ed.commit();

        TextView  dT = (TextView) findViewById(R.id.distance);
        Float f1 = sp.getFloat("dist",0.0f);
        BigDecimal roundfinalPrice = new BigDecimal(f1.floatValue()).setScale(2,BigDecimal.ROUND_HALF_UP);

        dT.setText(roundfinalPrice.toPlainString()+" m");
        if((sp.getFloat("dist",0.0f) >= 3)&& (!sp.getBoolean("b1", false))) {
            ed.putBoolean("b1", true);
            ed.commit();
        }
        if((Integer.valueOf(i.getStringExtra("count")) >= 2)&&(!sp.getBoolean("b2", false))){
            ed.putBoolean("b2",true);
            ed.commit();
        }
        if(sp.getBoolean("b1",false)){
            ImageView iv = (ImageView) findViewById(R.id.imageView);
            iv.setImageResource(R.drawable.satu);
        }
        if(sp.getBoolean("b2",false)){
            ImageView iv1 = (ImageView) findViewById(R.id.imageView1);
            iv1.setImageResource(R.drawable.dua);

        }


        imageView = (ImageView) findViewById(R.id.imageView4);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }else if(item.getItemId() ==  R.id.mAdd){
            Intent xyz = new Intent(this,AddShake.class);
            startActivity(xyz);
        }

        return super.onOptionsItemSelected(item);
    }

}
