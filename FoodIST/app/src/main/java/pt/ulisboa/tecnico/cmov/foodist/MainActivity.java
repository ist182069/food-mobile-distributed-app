package pt.ulisboa.tecnico.cmov.foodist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.foodist.async.campus.FoodServiceParsingTask;
import pt.ulisboa.tecnico.cmov.foodist.async.campus.GuessCampusTask;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.parse.FoodServiceResource;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;


public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;

    private static final String TAG = "TAG_MainActivity";
    private static final int PHONE_LOCATION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        Button userProfileButton = findViewById(R.id.userProfile);
        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        Button changeCampusButton = findViewById(R.id.changeCampus);
        changeCampusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChooseCampusActivity.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        String campus = intent.getStringExtra(ChooseCampusActivity.CAMPUS);

        if (campus != null) {
            //Know Current Location from previous user choice

            setCampus(campus);
        } else {
            askPhonePermission();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(((GlobalStatus)getApplicationContext()).getProfileActivity()){
            filterServices();
            ((GlobalStatus)getApplicationContext()).setProfileActivity(true);
        }
    }


    private void askPhonePermission() {
        int hasPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasPhonePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PHONE_LOCATION_REQUEST_CODE);
        } else {
            guessCampusFromLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PHONE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "phone location permission granted");
                guessCampusFromLocation();
            } else {
                Toast.makeText(getApplicationContext(), "Location not granted, enter location manually", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "phone location permission NOT granted");
                askCampus();
            }
        }
    }


    private void guessCampusFromLocation() {
        final double[] coords = new double[2];

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            coords[0] = location.getLatitude();
                            coords[1] = location.getLongitude();
                        } else {
                            Toast.makeText(getApplicationContext(), "Could not get location, please select campus", Toast.LENGTH_SHORT).show();
                            askCampus();
                        }
                        String common = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=";
                        String myCoords = String.format(Locale.ENGLISH, "%f,%f", coords[0], coords[1]);
                        Log.d("LOCATION", "Coords: " + myCoords);
                        String destination = "Instituto+Superior+Técnico";
                        String apiKey = getString(R.string.map_api_key);

                        String urlAlameda = common + myCoords + "&destinations=" + destination + "&key=" + apiKey;
                        destination = "Instituto+Superior+Técnico+-+Taguspark";
                        String urlTagus = common + myCoords + "&destinations=" + destination + "&key=" + apiKey;

                        new GuessCampusTask(MainActivity.this).execute(urlAlameda, urlTagus);
                    }
                });
    }

    public void setCampus(String campus) {
        //Update Interface
        TextView textView = findViewById(R.id.currentCampus);
        textView.setText(campus);

        loadServices(campus);

        //Save preferences for later
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.global_preferences_file), 0).edit();
        editor.putString(getString(R.string.global_preferences_location), campus);
        editor.apply();
    }

    public void writeServicesToUI(List<FoodService> services) {
        ViewGroup foodServiceList = findViewById(R.id.foodServices);
        foodServiceList.removeAllViews();

        for (FoodService service : services) {
            //number of info
            String[] info = new String[]{service.getName(), service.getDistance(), service.getTime()};

            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.food_service, null);

            TextView name = v.findViewById(R.id.foodServiceName);
            TextView distance = v.findViewById(R.id.distance);
            TextView queue = v.findViewById(R.id.queueTime);

            name.setText(info[0]);
            distance.setText(String.format("Distance: %s", info[1]));
            queue.setText(String.format("Waiting: %s", info[2]));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, FoodServiceActivity.class);
                    TextView name = v.findViewById(R.id.foodServiceName);

                    intent.putExtra("Service Name", name.getText());
                    startActivity(intent);
                }
            });

            foodServiceList = findViewById(R.id.foodServices);
            foodServiceList.addView(v);
        }
    }


    public void askCampus() {
        Intent intent = new Intent(MainActivity.this, ChooseCampusActivity.class);
        finish();
        startActivity(intent);
    }

    public void loadServices(String campus) {
        InputStream is = getResources().openRawResource(R.raw.services);
        FoodServiceResource resource = new FoodServiceResource(is, campus);
        new FoodServiceParsingTask(MainActivity.this).execute(resource);
    }

    public void filterServices(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        String position_name = pref.getString(getString(R.string.position_name), null);
        ArrayList<FoodService> filteredServices = new ArrayList<>();

        if(position_name != null) {
            for (FoodService service : ((GlobalStatus)getApplicationContext()).getServices()) {
                //If restriction
                List<String> restrictions = service.getRestrictions();
                if (!restrictions.contains(position_name)) {
                    filteredServices.add(service);
                }
            }
        }
        writeServicesToUI(filteredServices);
    }

}
