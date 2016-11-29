package sqrtstudio.com.fitwalker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
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

import java.io.ByteArrayOutputStream;

import static sqrtstudio.com.fitwalker.AppConfig.SP;

public class InfoWindow extends AppCompatActivity {
    private static final int CAMERA_PIC_REQUEST = 88;//int bebas, maks 1 byte
    ImageView img;
    ImageView imageView;
    Bitmap bitm;
    Intent i;
    boolean isImageFitToScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_window);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent i2 = getIntent();
        TextView judul = (TextView) findViewById(R.id.judul);
        judul.setText(i2.getStringExtra("namap"));
        TextView alamat = (TextView) findViewById(R.id.address);
        alamat.setText(i2.getStringExtra("namaa"));
        ImageView ico = (ImageView)findViewById(R.id.imageView5);
        TextView tIco = (TextView) findViewById(R.id.count);
        if(i2.getStringExtra("visited").equals("yes")){
            ico.setImageResource(R.drawable.ceklis);
            tIco.setText("Visited 1 Times");
        }else{
            ico.setImageResource(R.drawable.ceklis);
            tIco.setText("Not Visited");
        }
        img = (ImageView) findViewById(R.id.imageView1);
        i = new Intent(this,FullScreen.class);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
        });
        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return true;
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PIC_REQUEST) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ImageView imageview = (ImageView) findViewById(R.id.imageView2);
            imageview.setImageBitmap(image);
        }
    }
}
