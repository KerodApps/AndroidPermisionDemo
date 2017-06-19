package net.kerod.android.permissiondemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {
    private LocationManager mLocationManager;
    private TextView mTxtvLatLng;
    boolean finishActivityIfGpsPermissionDenied=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTxtvLatLng = (TextView) findViewById(R.id.txtv_lat_lng);
        setUpGps();
    }



    private void setUpGps() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!isGpsFeatureAvailable(this)) {
            return;
        } else if (!PermissionUtil.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            PermissionUtil.requestPermission(this, PermissionUtil.REQUEST_PERMISSION_ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_rationale_location, finishActivityIfGpsPermissionDenied);
            //
        } else if (!isGpsEnabled(mLocationManager)) {
            showEnableLocationDialog(this);
        } else {
            startUpdatingLocation();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.REQUEST_PERMISSION_ACCESS_FINE_LOCATION) {

            if (PermissionUtil.isPermissionGranted(permissions, grantResults, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                startUpdatingLocation();
            } else {
                Toast.makeText(this, getString(R.string.permission_required_toast), Toast.LENGTH_LONG).show();
                if(finishActivityIfGpsPermissionDenied) {
                    finish();
                }
            }
        }
    }


    void showEnableLocationDialog(final Activity context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(getString(R.string.enable_location))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.settings),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(callGPSSettingIntent);
//                                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);

                            }
                        }
                );
        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void startUpdatingLocation() {
        //already checked for permission by the calling function, just add this if clause to stop ide yelling
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mTxtvLatLng.setText("LatLng(" + location.getLatitude() + "," + location.getLongitude() + ")");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }
    };

    public static boolean isGpsFeatureAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    public static boolean isGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
