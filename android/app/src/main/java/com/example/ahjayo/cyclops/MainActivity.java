package com.example.ahjayo.cyclops;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback {

    enum MODE {MAP, PROFILE, ENROLLMENT};
    UserInfo userinfo;
    private GoogleMap mMap = null;
    Boolean is_first = true;
    MapFragment mapFragment;
    List<Marker> markers;

    Spinner bike_list_spinner;
    Button ride_button;
    Button leave_button;
    TextView name_header;
    TextView name_content;
    TextView address_header;
    TextView address_content;
    EditText device_register_content;
    Button device_register_submit;

    /*
    For navigation tap that changes page between home, profile, register
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    set_home_visibility(View.VISIBLE);
                    set_profile_visibility(View.GONE);
                    set_device_register_visibility(View.GONE);
                    return true;
                case R.id.navigation_profile:
                    set_home_visibility(View.GONE);
                    set_profile_visibility(View.VISIBLE);
                    set_device_register_visibility(View.GONE);
                    return true;
                case R.id.navigation_device_register:
                    set_home_visibility(View.GONE);
                    set_profile_visibility(View.GONE);
                    set_device_register_visibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bike_list_spinner = (Spinner) findViewById(R.id.bike_list_spinner);
        ride_button = (Button) findViewById(R.id.ride_button);
        leave_button = (Button) findViewById(R.id.leave_button);
        ride_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_state(1);
            }
        });
        leave_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_state(0);
            }
        });


        name_header = (TextView) findViewById(R.id.name_header);
        name_content = (TextView) findViewById(R.id.name_content);
        address_header = (TextView) findViewById(R.id.address_header);
        address_content = (TextView) findViewById(R.id.address_content);
        device_register_content = (EditText) findViewById(R.id.device_register_content);
        device_register_submit = (Button) findViewById(R.id.device_register_submit);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        userinfo = ((UserInfo)this.getApplication()).get_user_info();
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ArrayList<String> items = new ArrayList<String>();
        for(int i=0; i<userinfo.get_keys().length(); i++) {
            try {
                items.add(userinfo.get_keys().getString(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, items);
        bike_list_spinner.setAdapter(adapter);

        name_content.setText(userinfo.get_name());
        address_content.setText(userinfo.get_address());

        set_profile_visibility(View.INVISIBLE);
        set_device_register_visibility(View.INVISIBLE);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        markers = new ArrayList<Marker>();

        final Handler handler = new Handler();
        Timer timer = new Timer();
        BikeLocationRequester bike_task = new BikeLocationRequester(handler);
        timer.schedule(bike_task, 0, 1000*100);
    }

    /**
     * Represents an asynchronous task used to change state of bike.
     */
    public class BikeStateChanger extends AsyncTask<Void, Void, Boolean> {
        private int bike_id;
        private int bike_state;

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean success = false;
            JSONArray keys = userinfo.get_keys();
            HttpClient http_client = new DefaultHttpClient();
            try {
                String url = Constants.SERVER_URL + Constants.BIKE_STATE_METHOD;
                HttpPut http_put = new HttpPut(url);
                JSONObject json = new JSONObject();
                json.put("key", userinfo.get_keys().getString(bike_id));
                json.put("state", bike_state);
                String json_str = json.toString();
                StringEntity put_params = new StringEntity(json_str, "UTF-8");
                http_put.setEntity(put_params);
                HttpResponse response = http_client.execute(http_put);
                if (response.getStatusLine().getStatusCode() == 200) {
                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                http_client.getConnectionManager().shutdown();
            }
            return success;
        }
    }

    private void set_home_visibility(int visibility) {
        ride_button.setVisibility(visibility);
        leave_button.setVisibility(visibility);
        mapFragment.getView().setVisibility(visibility);
        bike_list_spinner.setVisibility(visibility);
    }

    private void set_profile_visibility(int visibility) {
        name_header.setVisibility(visibility);
        name_content.setVisibility(visibility);
        address_header.setVisibility(visibility);
        address_content.setVisibility(visibility);
    }

    private void set_device_register_visibility(int visibility) {
        device_register_content.setVisibility(visibility);
        device_register_submit.setVisibility(visibility);
    }

    private void send_state(int state) {
        BikeStateChanger task = new BikeStateChanger();
        task.bike_state = state;
        task.execute();
    }

    /**
     * Represents an asynchronous task used to request location of bike.
     */
    public class BikeLocationRequester extends TimerTask {
        private Handler mHandler;

        public BikeLocationRequester(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    if (mMap == null)
                        return;
                    JSONArray keys = userinfo.get_keys();
                    double lat_avg = 0; double lon_avg = 0; double count = 0;
                    for (int i = 0; i < keys.length(); i++) {
                        HttpClient http_client = new DefaultHttpClient();
                        try {
                            String url = Constants.SERVER_URL + Constants.BIKE_LOC_METHOD + "?"
                                    + "key=" + keys.get(i).toString();
                            HttpGet get = new HttpGet(url);
                            HttpResponse response = http_client.execute(get);
                            String jsonstr = EntityUtils.toString(response.getEntity(), "UTF-8");
                            JSONObject json = new JSONObject(jsonstr);
                            JSONObject loc = new JSONObject(json.getJSONObject("location").toString());
                            if (response.getStatusLine().getStatusCode() == 200) {
                                if (markers.size() > i) {
                                    markers.remove(i);
                                }
                                double lat = loc.getDouble("lat");
                                double lon = loc.getDouble("long");
                                Marker m = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat, lon))
                                        .title("bike " + i));
                                markers.add(m);
                                lat_avg += lat; lon_avg += lon; count += 1;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            http_client.getConnectionManager().shutdown();
                        }

                    }
                    lat_avg /= count; lon_avg /= count;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lat_avg, lon_avg), 18));
                    is_first = false;
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
    }
}
