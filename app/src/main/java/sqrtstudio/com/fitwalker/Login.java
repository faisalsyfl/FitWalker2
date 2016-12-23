package sqrtstudio.com.fitwalker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static sqrtstudio.com.fitwalker.AppConfig.*;

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
            Intent i = new Intent(this,LoadingScreen.class);
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
        ed.putString("count","0");
        ed.putBoolean("b1",false);
        ed.putBoolean("b2",false);
        ed.putBoolean("b3",false);
        ed.putBoolean("b4",false);
        ed.commit();

        //ADD INTO DB
        StringRequest postRequest = new StringRequest(Request.Method.POST, ADD_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // menampilkan respone
                        Log.d("Response ADD", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                // Menambahkan parameters post
                Map<String, String>  params = new HashMap<String, String>();
                params.put("name",((EditText)findViewById(R.id.editText)).getText().toString());
                params.put("lat", "0");
                params.put("long", "0");
                params.put("done", "0");
                params.put("meter", "0");
                params.put("stats","0");
                params.put("photo", "");
                params.put("stats", "1");

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest);

        Intent i = new Intent(this,Main.class);
        startActivity(i);
        finish();

    }

}
