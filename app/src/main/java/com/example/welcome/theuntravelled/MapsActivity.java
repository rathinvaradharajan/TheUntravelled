package com.example.welcome.theuntravelled;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.test.mock.MockPackageManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.welcome.theuntravelled.R.id.time;
import static java.lang.Thread.sleep;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    EditText locEd;
    LatLng source;
    bg b;
    String[] a;

    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public double[] getGps() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        /* Loop over the array backwards, and if you get an accurate location, then break                 out the loop*/
        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                double[] d = {7,76};
                return d;
            }
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //----------------------------------------------------------------------------------------------------------
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //-----------------------------------------------------------------------------------------------------------
        double[] pos = this.getGps();
        if (pos[1] == 0){
            pos[0]=10;
            pos[1] = 78;
        }
        //--------------------------------------------------------------------------------------------------------------------
        LatLng de;

        de = new LatLng(pos[0],pos[1]);
        Geocoder geo = new Geocoder(this,Locale.getDefault());
        List<Address> addresses = null;
        String cityName="h";
        try {
            addresses = geo.getFromLocation(pos[0], pos[1], 1);
            cityName = addresses.get(0).getSubAdminArea();
            List<Address> ad = geo.getFromLocationName(cityName,1);
            String ab[] = null;
            ab = String.valueOf(ad.get(0).getLatitude()).split("\\.");
            String s = ab[0]+"|"+ab[1];
            ab = null;
            ab = String.valueOf(ad.get(0).getLongitude()).split("\\.");
            s = s+"&"+ab[0]+"|"+ab[1];
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            ab = null;
            bg ba = new bg();
            ba.execute(cityName,s);
        } catch (IOException e) {
            Toast.makeText(this, "Cant get this location's address", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(this,"Can't get this location's address. Please search a city", Toast.LENGTH_SHORT).show();
        }




        mMap.addMarker(new MarkerOptions().position(de).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ptr)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(de,9.0f));



        locEd = (EditText)findViewById(R.id.SearchBar);
        locEd.setOnKeyListener(new View.OnKeyListener()  {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            try {
                                MapsActivity.this.OnSearch(locEd);
                            } catch (IOException e) {
                                Toast.makeText(MapsActivity.this, "IO Exception", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng dest = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                Toast.makeText(MapsActivity.this, dest.toString(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        //mMap.setMyLocationEnabled(true);
    }



    public void OnSearch(View view) throws IOException {
        locEd= (EditText)findViewById(R.id.SearchBar);
        String loc = locEd.getText().toString();
        locEd.setText("");
        try {

            if (!loc.equalsIgnoreCase("")){
                mMap.clear();
                Geocoder g = new Geocoder(this);
                List<Address> ad = g.getFromLocationName(loc,1);
                android.location.Address pl = ad.get(0);
                LatLng Search = new LatLng(pl.getLatitude(),pl.getLongitude());
                source = Search;
               // Toast.makeText(MapsActivity.this, Search.latitude+"&"+Search.longitude, Toast.LENGTH_LONG).show();
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Search,13.0f));
                mMap.addMarker(new MarkerOptions().position(Search).title(ad.get(0).getSubAdminArea()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ptr)));
                String lat = String.valueOf(pl.getLatitude());
                String lon = String.valueOf(pl.getLongitude());
                a = lat.split("\\.");
                String send = a[0]+"|"+a[1];
                a = null;
                a = lon.split("\\.");
                send = send+"&"+a[0]+"|"+a[1];
                a = null;
                //Toast.makeText(MapsActivity.this, send, Toast.LENGTH_SHORT).show();

                b = new bg();
                b.execute(loc,send);
            }
            else {
                Toast.makeText(this, "Enter a city", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            Toast.makeText(MapsActivity.this,"Something went wrong! Please re-enter", Toast.LENGTH_SHORT).show();
        }


    }

//--------------------------------------------------------------------------------------------------

//--------------------------------------------------------------------------------------------------

    class bg extends AsyncTask<String,Void,Void>{
        Geocoder g = new Geocoder(MapsActivity.this);
        private ProgressDialog bar;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        @Override
        protected Void doInBackground(final String...s) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Toast.makeText(MapsActivity.this, "Sleeping problem", Toast.LENGTH_SHORT).show();
            }

            DatabaseReference ref = database.getReference().child("Place").child(s[1]);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> c = dataSnapshot.getChildren();
                    for (DataSnapshot d:c) {
                        String tag = d.getKey();
                        String aClass = d.getKey()+","+s[0];
                        String cat = d.getValue().toString();
                        List<Address> ad = null;
                        try {
                            ad = g.getFromLocationName(aClass,1);
                        } catch (IOException e) {
                            Toast.makeText(MapsActivity.this, "Something Went wrong", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            android.location.Address pl = ad.get(0);
                            LatLng de = new LatLng(pl.getLatitude(),pl.getLongitude());
                            if(tag.equalsIgnoreCase("Beach")){
                                mMap.addMarker(new MarkerOptions().position(de).title(tag).snippet("Category: "+cat).icon(BitmapDescriptorFactory.fromResource(R.drawable.beach)));
                            }
                            mMap.addMarker(new MarkerOptions().position(de).title(tag).snippet("Category: "+cat)/*.icon(BitmapDescriptorFactory.fromResource(R.drawable.pt))*/);
                        }
                        catch (Exception e){
                            //Toast.makeText(MapsActivity.this,"", Toast.LENGTH_SHORT).show();
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            bar.dismiss();
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //database.goOffline();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar = new ProgressDialog(MapsActivity.this);
            bar.setMessage("Waiting to Wander");
            bar.show();
            database.goOnline();
        }
    }

    public void Displaydialog(View view){
        try {
            AlertDialog.Builder dia = new AlertDialog.Builder(this);
            dia.setTitle("Add places");
            dia.setMessage("To add place, Please visit www.googleMaps.com");
            dia.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog s = dia.create();
            s.show();
        }
        catch (Exception e){
            Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
        }


    }

}




