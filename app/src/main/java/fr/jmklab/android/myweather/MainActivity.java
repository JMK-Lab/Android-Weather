package fr.jmklab.android.myweather;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.jmklab.android.myweather.utils.AsyncTaskLoadImage;

public class MainActivity extends AppCompatActivity implements LocationListener {

    // Define YOUR API KEY in gradle.properties
    private static final String API_KEY = BuildConfig.API_KEY;

    private TextView mWeatherDescription;
    private ImageView mWeatherIcon;

    private TextView mLocationText;
    private TextView mTemperatureText;
    private TextView mPressureText;
    private TextView mHumidityText;
    private TextView mWindSpeedText;

    private Double mLatGet;
    private Double mLonGet;

    private JSONObject mWeatherData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mWeatherDescription = (TextView) findViewById(R.id.text_view_weather_description);
        mWeatherIcon = (ImageView) findViewById(R.id.image_view_weather_icon);

        mLocationText = (TextView) findViewById(R.id.text_view_location_value);

        mTemperatureText = (TextView) findViewById(R.id.text_view_temperature_value);
        mPressureText = (TextView) findViewById(R.id.text_view_pressure_value);
        mHumidityText = (TextView) findViewById(R.id.text_view_humidity_value);
        mWindSpeedText = (TextView) findViewById(R.id.text_view_wind_speed_value);

        getLocation();

    }

    void getLocation() {

        try {

            LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mLatGet = location.getLatitude();
        mLonGet = location.getLongitude();

        getWeather();
    }


    public void getWeather() {

        try {

            new Thread(new Runnable() {

                @Override
                public void run() {

                    try {

                        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + mLatGet + "&lon=" + mLonGet + "&units=metric&lang=en&APPID=" + API_KEY);

                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                        StringBuilder json = new StringBuilder(1024);
                        String tmp;

                        while ((tmp = reader.readLine()) != null)
                            json.append(tmp).append("\n");
                        reader.close();

                        mWeatherData = new JSONObject(json.toString());

                        if (mWeatherData.getInt("cod") != 200) {

                            apiErrorWeather();

                        } else {

                            useWeather();
                        }

                    } catch (Exception e) {

                        e.printStackTrace();

                    }
                }
            }).start();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public void useWeather() throws JSONException {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                JSONObject mainObj, windObj, weatherObj = null;

                try {

                    JSONArray weatherArr = mWeatherData.getJSONArray("weather");
                    weatherObj = weatherArr.getJSONObject(0);

                    mainObj = new JSONObject(mWeatherData.getString("main"));
                    windObj = new JSONObject(mWeatherData.getString("wind"));

                    String weatherDescription = weatherObj.getString("description");
                    String weatherIcon = weatherObj.getString("icon");

                    Double dataTemperature = mainObj.getDouble("temp");
                    int dataPressure = mainObj.getInt("pressure");
                    int dataHumidity = mainObj.getInt("humidity");
                    Double dataWindSpeed = windObj.getDouble("speed");

                    mWeatherDescription.setText(ucfirst(weatherDescription));

                    new AsyncTaskLoadImage(mWeatherIcon).execute("http://openweathermap.org/img/w/" + weatherIcon + ".png");

                    mLocationText.setText(mWeatherData.getString("name"));
                    mTemperatureText.setText(dataTemperature + " °c");
                    mPressureText.setText(dataPressure + "Hpa");
                    mHumidityText.setText(dataHumidity + "%");
                    mWindSpeedText.setText(dataWindSpeed + "km/h");


                } catch (JSONException e) {

                    e.printStackTrace();

                }

            }
        });

    }

    public void apiErrorWeather() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Toast.makeText(MainActivity.this, "API Error", Toast.LENGTH_SHORT).show();

            }

        });

    }

    @Override
    public void onProviderDisabled(String provider) {

        Toast.makeText(MainActivity.this, "Please turn on GPS & Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    public static String ucfirst(String string) {

        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
