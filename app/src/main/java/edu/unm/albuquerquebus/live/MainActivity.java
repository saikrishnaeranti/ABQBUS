package edu.unm.albuquerquebus.live;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.unm.albuquerquebus.live.fragments.DestinationRouteDirectionsFragment;
import edu.unm.albuquerquebus.live.interfaces.RouteInfo;
import edu.unm.albuquerquebus.live.model.BusInfo;
import edu.unm.albuquerquebus.live.model.BusRoute;
import edu.unm.albuquerquebus.live.model.BusStop;
import edu.unm.albuquerquebus.live.model.DirectionsTransitModel;
import edu.unm.albuquerquebus.live.model.WalkingRoute;
import edu.unm.albuquerquebus.live.utils.ApiCaller;
import edu.unm.albuquerquebus.live.utils.Constants;
import edu.unm.albuquerquebus.live.utils.DirectionParseJson;
import edu.unm.albuquerquebus.live.utils.KmlUtils;
import edu.unm.albuquerquebus.live.utils.XMLPullParserHandler;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DestinationRouteDirectionsFragment.OnDestinationRouteDirectionsFragmentInteractionListener {

    private static final int MY_LOCATION_REQUEST_CODE = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap mMap;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mLastKnownLocation;
    private View mLocationButton;
    private SupportMapFragment mMapFragment;
    private String mDestinationAddress;
    private DestinationRouteDirectionsFragment mDestinationRouteDirectionsFragment;
    private List<Polyline> mListOfPolyLines;



    private DirectionsTransitModel mPresentDirectionModelWithWalking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference().child("busRoute").getDatabase().getReference("message");

        myRef.setValue("Hello, World!");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        //To read the kml data file from assests folder
        //List<BusStop> busStopList = KmlUtils.readBusStopsFromKml(this);

        //To read the csv and map stop id with stops
//        KmlUtils.readStopsFileToMapBusStopwithStopId(this);

        //To read the stops timing and map stops with stop_id in order
        //KmlUtils.readStopsTimingFileToMapBusStopId(this);

        //KmlUtils.readFromDatabaseGetTripsForEachRoute(busStopList);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDestinationRouteDirectionsFragment = (DestinationRouteDirectionsFragment) getSupportFragmentManager().findFragmentById(R.id.destination_route_directions_fragment);

        assignLastKnownLocation(mFusedLocationClient);

        //getData();
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

    }


    private void assignLastKnownLocation(FusedLocationProviderClient mFusedLocationClient) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("MAIN Class data", "Location known ---aaa-");


                            //Move the camera to the user's location and zoom in!
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));

                            // Logic to handle location object
                            mLastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        }

                    }
                });

    }


    public boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_search:
                Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                        .show();
                searchPlaces();
                break;
            /*// action with ID action_settings was selected
            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT)
                        .show();
                break;*/
            default:
                break;
        }

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);

        if (mMapFragment.getView() != null) {
            mLocationButton = ((View) mMapFragment.getView().findViewById(Integer.parseInt("1")).
                    getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, for exemple, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mLocationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 30);

        }


        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showMessageOKCancel("You need to allow access to Location",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_LOCATION_REQUEST_CODE);
                            }
                        });
                return;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_LOCATION_REQUEST_CODE);

            }
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            Log.d("MyApp", String.valueOf(permissions.length));
            Log.d("MyApp", String.valueOf(permissions[0]));
            Log.d("MyApp", String.valueOf(grantResults[0]));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    private void searchPlaces() {
        try {
            mDestinationAddress = "";
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            //  .setBoundsBias(new LatLngBounds(new LatLng(34.927097, -106.476673),new LatLng(35.232219, -106.869434) ))
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                mDestinationAddress = place.getName().toString() + ", " + place.getAddress().toString();
                String name = place.getName().toString();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lastKnowLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient);
                if (mLastKnownLocation == null) {
                    mLastKnownLocation = new LatLng(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());

                }

                //here we are getting the bus route from google
                getRouteOfBus(mLastKnownLocation, place.getLatLng());

                Log.i(TAG, "Place: " + place.getName());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void getRouteOfBus(LatLng originLatLng, LatLng destinationLatLng) {
        ApiCaller apiCaller = new ApiCaller();
        apiCaller.setAfterApiCallResponse(new ApiCaller.AfterApiCallResponse() {
            @Override
            public void successResponse(String response, String url) {

                try {
                    DirectionsTransitModel directionsTransitModel = new DirectionParseJson().parseRoute(response, MainActivity.this);
                    /*if (directionsTransitModel.getEndAddress() == null || directionsTransitModel.getEndAddress().length() == 0) {
                        directionsTransitModel.setEndAddress(mDestinationAddress);
                    }*/
                    directionsTransitModel.setEndAddress(mDestinationAddress);
                    getTimeForBicycleForBusStop(directionsTransitModel);
                    if (mDestinationRouteDirectionsFragment != null)
                        mDestinationRouteDirectionsFragment.updateDestinationDetails(directionsTransitModel);

                    removeAllPolyline();// This method removes the polylines that are drawn in last search and creates and new ArrayList
                    drawWalkingAndBusRoutePolylines(directionsTransitModel);
                    getBusLocationsAndUpdateInDatabase(directionsTransitModel);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("MAIN Class data", response);
            }


            @Override
            public void errorResponse(VolleyError error, String url) {

            }


        });
        Map<String, String> parameters = new HashMap<>();
        parameters.put(Constants.ORIGIN, originLatLng.latitude + "," + originLatLng.longitude);
        parameters.put(Constants.DESTINATION, destinationLatLng.latitude + "," + destinationLatLng.longitude);
        parameters.put(Constants.KEY, getResources().getString(R.string.google_maps_key));
        parameters.put(Constants.MODE, "transit");
        //parameters.put(Constants.DEPART_TIME,"1507129200000");
        apiCaller.makeStringRequest(MainActivity.this, Request.Method.GET, Constants.GET_MAP_URL, parameters);
    }

    private void getBusLocationsAndUpdateInDatabase(DirectionsTransitModel directionsTransitModel) {
        List<BusRoute> busRouteList = new ArrayList<>();
        for (RouteInfo routeInfo :
                directionsTransitModel.getmListOfRoutes()) {
            if (routeInfo.transitMode().equalsIgnoreCase("TRANSIT")) {
                busRouteList.add((BusRoute) routeInfo);
            }
        }

        for (BusRoute busRoute :
                busRouteList) {

            makeRequestToGetBusLocation(busRoute);
        }

    }

    private void makeRequestToGetBusLocation(final BusRoute busRoute) {
        ApiCaller apiCaller = new ApiCaller();
        apiCaller.setAfterApiCallResponse(new ApiCaller.AfterApiCallResponse() {
            @Override
            public void successResponse(String response, String url) {

                XMLPullParserHandler parserHandler = new XMLPullParserHandler();
                List<BusInfo> busInfoArrayList = parserHandler.parseSingleRouteKml(response);
                busRoute.getIndividualBusSteps().setBusInfoList(parserHandler.parseSingleRouteKml(response));
                for (BusInfo busInfo :
                        busInfoArrayList) {
                    if (mMap != null) {
                        LatLng markerLatLng = new LatLng(busInfo.getLatitude(), busInfo.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(markerLatLng).title(busInfo.getVehicleNumber()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng));
                    }
                }
                Log.d("MAIN Class data", response);
            }


            @Override
            public void errorResponse(VolleyError error, String url) {

            }


        });
        apiCaller.makeStringRequest(MainActivity.this, Request.Method.GET,
                String.format(Constants.GET_ROUTE_URL,
                        busRoute.getIndividualBusSteps().getBusShortName().trim()), null);
    }

    private void drawWalkingAndBusRoutePolylines(DirectionsTransitModel directionsTransitModel) {

        for (RouteInfo routeInfo :
                directionsTransitModel.getmListOfRoutes()) {
            if (routeInfo.transitMode().equalsIgnoreCase("TRANSIT")) {
                BusRoute busRoute = ((BusRoute) routeInfo);
                ArrayList<LatLng> latLngs = busRoute.getPolylineLatLngPoints();
                Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(latLngs).width(7).color(Color.parseColor(busRoute.getIndividualBusSteps().getBusColor())));
                mListOfPolyLines.add(polyline);
            } else {
                ArrayList<LatLng> latLngs = ((WalkingRoute) routeInfo).getPolylineLatLngPoints();
                Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(latLngs).width(7).color(Color.BLUE).pattern(Arrays.<PatternItem>asList(
                        new Dot(), new Gap(20))));
                polyline.setJointType(JointType.ROUND);
                mListOfPolyLines.add(polyline);

            }
        }
    }

    private void removeAllPolyline() {

        if (mListOfPolyLines != null) {

            for (Polyline polyline :
                    mListOfPolyLines) {
                polyline.remove();
            }
            mListOfPolyLines = null;
        }
        mListOfPolyLines = new ArrayList<>();
    }


    private void getTimeForBicycleForBusStop(DirectionsTransitModel directionsTransitModel) {
        ApiCaller apiCaller = new ApiCaller();
        apiCaller.setAfterApiCallResponse(new ApiCaller.AfterApiCallResponse() {
            @Override
            public void successResponse(String response, String url) {

                try {
                    DirectionsTransitModel directionsTransitModel = new DirectionParseJson().parseRoute(response, MainActivity.this);
                    if (mDestinationRouteDirectionsFragment != null)
                        mDestinationRouteDirectionsFragment.updateBicycleTimeDetails(directionsTransitModel);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("MAIN Class data", response);
            }

            @Override
            public void errorResponse(VolleyError error, String url) {

            }


        });
        ArrayList<RouteInfo> routeInfoArrayList = directionsTransitModel.getmListOfRoutes();
        WalkingRoute walkingRoute = null;
        for (int i = 0; i < routeInfoArrayList.size(); i++) {
            if (routeInfoArrayList.get(i).transitMode().equalsIgnoreCase("WALKING")) {
                walkingRoute = (WalkingRoute) routeInfoArrayList.get(i);
                break;
            }
        }
        Map<String, String> parameters = new HashMap<>();
        parameters.put(Constants.ORIGIN, mLastKnownLocation.latitude + "," + mLastKnownLocation.longitude);
        parameters.put(Constants.DESTINATION, walkingRoute.getEndLocation().latitude + "," + walkingRoute.getEndLocation().longitude);
        parameters.put(Constants.KEY, getResources().getString(R.string.google_maps_key));
        parameters.put(Constants.MODE, "BICYCLING");
        //parameters.put(Constants.DEPART_TIME,"1507129200000");
        apiCaller.makeStringRequest(MainActivity.this, Request.Method.GET, Constants.GET_MAP_URL, parameters);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //displayLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, Constants.CONNECTION_FAILED_AND_CONNECTION_RESULT_CODE
                + connectionResult.getErrorCode());
    }

    @Override
    public void onFragmentInteraction(Uri uri) {


    }


}
