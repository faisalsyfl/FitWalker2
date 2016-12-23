package sqrtstudio.com.fitwalker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;

import static sqrtstudio.com.fitwalker.AppConfig.SP;

public class OtherProfile extends AppCompatActivity {

    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(myToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent i = getIntent();
        String[] data = i.getStringArrayExtra("data");
        TextView oT = (TextView) findViewById(R.id.ownerText);
        oT.setText(data[1]);

        TextView mcT = (TextView) findViewById(R.id.countMarkerText);
        mcT.setText(data[2]);

        TextView  dT = (TextView) findViewById(R.id.distance);
//        Float f1 = sp.getFloat("dist",0.0f);
//        BigDecimal roundfinalPrice = new BigDecimal(f1.floatValue()).setScale(2,BigDecimal.ROUND_HALF_UP);
//
        int b1 = 0,b2=0;
        dT.setText(data[3]);
        if((Float.valueOf(data[3]) >= 3)) {
            b1= 1;
        }
        if((Integer.valueOf(data[2]) >= 2)){
            b2 = 1;
        }

        if(b1==1){
            ImageView iv = (ImageView) findViewById(R.id.imageView);
            iv.setImageResource(R.drawable.satu);
        }
        if(b2==1){
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
