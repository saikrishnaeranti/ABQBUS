package edu.unm.albuquerquebus.live;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import edu.unm.albuquerquebus.live.fragments.DestinationRouteDirectionsFragment;
import edu.unm.albuquerquebus.live.fragments.DirectionsFragment;
import edu.unm.albuquerquebus.live.interfaces.RouteInfo;
import edu.unm.albuquerquebus.live.model.BusInfo;
import edu.unm.albuquerquebus.live.model.BusRoute;
import edu.unm.albuquerquebus.live.model.DirectionsTransitModel;
import edu.unm.albuquerquebus.live.model.WalkingRoute;
import edu.unm.albuquerquebus.live.utils.ApiCaller;
import edu.unm.albuquerquebus.live.utils.Constants;
import edu.unm.albuquerquebus.live.utils.DirectionParseJson;
import edu.unm.albuquerquebus.live.utils.KmlUtils;
import edu.unm.albuquerquebus.live.utils.LatLngInterpolator;
import edu.unm.albuquerquebus.live.utils.MarkerAnimation;
import edu.unm.albuquerquebus.live.utils.XMLPullParserHandler;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DestinationRouteDirectionsFragment.OnDestinationRouteDirectionsFragmentInteractionListener, DirectionsFragment.OnListFragmentInteractionListener {

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
    private Map<String, Marker> mMarkerMap;


    private int delay = 100; // delay for 100 microsec.
    private int period = 14000; // repeat every 14 sec.
    private Timer timer;

    private DirectionsTransitModel mPresentDirectionModelWithWalking;
    private HashMap<String, List<String>> mBusStopNameToStopCodeMap;
    private int counter = 0;
    private DirectionsFragment mDirectionsFragment;
    private Fragment fragment;
    private DirectionsTransitModel currentDirectionsTransitModel;

    private Map<String, String> busNumberToVehicleNumberMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_main);

        //To read the kml data file from assests folder
//        List<BusStop> busStopList = KmlUtils.readBusStopsFromKml(this);
//        List<BusStop> busStopList = KmlUtils.readBusStopsFromKml(MainActivity.this);
//        mBusStopNameToStopCodeMap = KmlUtils.getBusStopNameToStopCodeMapping(busStopList);

        //To read the csv and map stop id with stops
//        KmlUtils.readStopsFileToMapBusStopwithStopId(this);

        //To read the stops timing and map stops with stop_id in order
        //KmlUtils.readStopsTimingFileToMapBusStopId(this);

//        KmlUtils.readFromDatabaseGetTripsForEachRoute(busStopList,this);

        // Get start and end time of buses
        KmlUtils.findStartAndEndTimeOfEachBus();
        // Get the SupportMapFragment and request notification

        // when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //mDestinationRouteDirectionsFragment = (DestinationRouteDirectionsFragment) getSupportFragmentManager().findFragmentById(R.id.destination_route_directions_fragment);

        assignLastKnownLocation(mFusedLocationClient);

        //getData();
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        if (timer != null) {
            timer.cancel();
        }

        Timer timer1 = new Timer();
        timer1.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Log.d("counter of timer", String.valueOf(++counter));
                if (counter % 10 == 0) {
                    System.gc();
                }
                getAllRouteJson();
            }
        }, delay, period);

        final FloatingActionsMenu menuMultipleActions = findViewById(R.id.multiple_actions);

        FloatingActionButton searchButton = findViewById(R.id.action_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeCheckIfBusesAvailable()) {
                    showTripDetailsFragment();
                    searchPlaces();
                }

                menuMultipleActions.collapse();
            }
        });


    }

    private void showTripDetailsFragment() {

        //this is code for fragment
        mDestinationRouteDirectionsFragment = new DestinationRouteDirectionsFragment();
        fragment = mDestinationRouteDirectionsFragment;

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.activity_animation_push_down_in, R.anim.activity_animation_push_up_out)
                .replace(R.id.trip_details_frame_container, fragment).commit();
        fragmentManager.beginTransaction().addToBackStack(null);

        getUpdatedData();


    }

    private void getAllRouteJson() {

        ApiCaller apiCaller = new ApiCaller();
        apiCaller.setAfterApiCallResponse(new ApiCaller.AfterApiCallResponse() {
            @Override
            public void successResponse(String response, String url) {

                KmlUtils.readAllTripsAndBusLocationDataFromAllRouteJson(response);

                // Log.d("MAIN Class data", response);
            }

            @Override
            public void errorResponse(VolleyError error, String url) {

            }


        });


        apiCaller.makeStringRequest(MainActivity.this, Request.Method.GET, Constants.GET_ALL_ROUTE_JSON, null);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.mainmenu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            // action with ID action_refresh was selected
//            case R.id.action_search:
//
//
//                break;
//            /*// action with ID action_settings was selected
//            case R.id.action_settings:
//                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT)
//                        .show();
//                break;*/
//            default:
//                break;
//        }
//
//        return true;
//    }

    private boolean timeCheckIfBusesAvailable() {

        String startTime = "05:16:35";
        String endTime = "23:59:42";

        try {
            Date currentTime = new Date();
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
            String currentdate = DATE_FORMAT.format(currentTime);
            Date startTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentdate + " " + startTime);
            Date endTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentdate + " " + endTime);


            if (startTimeDate.compareTo(currentTime) == -1 && endTimeDate.compareTo(currentTime) == 1) {
                return true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "There are no buses running currently.", Toast.LENGTH_SHORT).show();
        return false;
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
//            //rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//            rlp.setMargins(0, 0, 30, 30);

            //for bottom left
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

            rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            rlp.addRule(RelativeLayout.ALIGN_END, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlp.setMargins(30, 0, 0, 40);

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
        mapInitialize();

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
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setCountry("US")
                    .build();
            mDestinationAddress = "";
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(typeFilter)
                            //North Latitude: 35.218054 South Latitude: 34.946766 East Longitude: -106.471163 West Longitude: -106.881796
                            .setBoundsBias(new LatLngBounds(new LatLng(34.946766, -106.471163), new LatLng(35.218054, -106.881796)))
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            e.fillInStackTrace();
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
                    currentDirectionsTransitModel = new DirectionParseJson().parseRoute(response, MainActivity.this);
                    /*if (currentDirectionsTransitModel.getEndAddress() == null || currentDirectionsTransitModel.getEndAddress().length() == 0) {
                        currentDirectionsTransitModel.setEndAddress(mDestinationAddress);
                    }*/
                    currentDirectionsTransitModel.setEndAddress(mDestinationAddress);
                    getTimeForBicycleForBusStop(currentDirectionsTransitModel);
                    if (mDestinationRouteDirectionsFragment != null)
                        mDestinationRouteDirectionsFragment.updateDestinationDetails(currentDirectionsTransitModel);


                    initiateTheTrip();
                    drawWalkingAndBusRoutePolylines(currentDirectionsTransitModel);
                    //getBusLocationsAndUpdateInDatabaseFromXML(currentDirectionsTransitModel);
                    getBusLocationsAndUpdateInDatabaseFromJSON(currentDirectionsTransitModel);

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

    public void initiateTheTrip() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        busNumberToVehicleNumberMap = new HashMap<>();
        removeAllPolylineAndMarkers();// This method removes the polylines that are drawn in last search and creates and new ArrayList


    }

    private void getBusLocationsAndUpdateInDatabaseFromXML(DirectionsTransitModel directionsTransitModel) {
        final List<BusRoute> busRouteList = new ArrayList<>();
        for (RouteInfo routeInfo :
                directionsTransitModel.getmListOfRoutes()) {
            if (routeInfo.transitMode().equalsIgnoreCase("TRANSIT")) {
                busRouteList.add((BusRoute) routeInfo);
            }
        }


        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {

                for (BusRoute busRoute :
                        busRouteList) {

                    makeRequestToGetBusLocation(busRoute);
                }
            }
        }, delay, period);


    }

    private void getBusLocationsAndUpdateInDatabaseFromJSON(DirectionsTransitModel directionsTransitModel) {
        final List<BusRoute> busRouteList = new ArrayList<>();
        for (RouteInfo routeInfo :
                directionsTransitModel.getmListOfRoutes()) {
            if (routeInfo.transitMode().equalsIgnoreCase("TRANSIT")) {
                busRouteList.add((BusRoute) routeInfo);
            }
        }

        if (busRouteList.size() > 0) {
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {


                    makeJSONRequestToGetBusLocation(busRouteList);

                }
            }, delay, period);
        } else {
            Toast.makeText(this, Constants.NO_BUSES_IN_THIS_ROUTE, Toast.LENGTH_SHORT).show();
        }


    }

    private void makeJSONRequestToGetBusLocation(final List<BusRoute> busRouteList) {
        ApiCaller apiCaller = new ApiCaller();
        apiCaller.setAfterApiCallResponse(new ApiCaller.AfterApiCallResponse() {
            @Override
            public void successResponse(String response, String url) {

                //TODO find the perfect trip and find the vehicle number (it should be done for once and update those buses regularly)
                List<BusInfo> busInfoList = KmlUtils.readAllTripsAndGetRequiredBusLocationDataFromAllRouteJson(response, busRouteList);

                for (BusInfo busInfo :
                        busInfoList) {
                    LatLng markerLatLng = new LatLng(busInfo.getLatitude(), busInfo.getLongitude());
                    if (mMap != null) {
                        Context context = MainActivity.this;
                        int id = context.getResources().getIdentifier("ic_bus_marker_" + busInfo.getBusShortName(), "drawable", context.getPackageName());

                        if (!mMarkerMap.containsKey(busInfo.getVehicleNumber())) {


                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(markerLatLng)
                                    .title(busInfo.getVehicleNumber() + "," + busInfo.getBusShortName().trim())
                                    .snippet("TripNumber - " + busInfo.getTripId() + "\n"
                                            + "Next Stop - " + busInfo.getNextStop() + "\n"
                                            + " At " + busInfo.getNextStopScheduleTime().toString())
                                    .icon(BitmapDescriptorFactory.fromResource(id)));
                            marker.setTag(busInfo);
                            mMarkerMap.put(busInfo.getVehicleNumber(), marker);


                        } else {
                            Marker marker = mMarkerMap.get(busInfo.getVehicleNumber());
                            marker.setTitle(busInfo.getVehicleNumber() + "," + busInfo.getBusShortName().trim());
                            marker.setSnippet("TripNumber - " + busInfo.getTripId() + "\n"
                                    + "Next Stop - " + busInfo.getNextStop() + "\n"
                                    + " At " + busInfo.getNextStopScheduleTime().toString());
                            marker.setIcon(BitmapDescriptorFactory.fromResource(id));
                            MarkerAnimation.animateMarkerToICS(marker, markerLatLng, new LatLngInterpolator.Spherical());
                            marker.setTag(busInfo);


                        }
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng));
                    }
                }

                if (busNumberToVehicleNumberMap.size() != busRouteList.size()) {
                    findTripAndBusNumbersToTrack(busInfoList, busRouteList);
                } else {

                    Iterator<Map.Entry<String, Marker>> iterator = mMarkerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Marker> pair = iterator.next();
                        Marker marker = pair.getValue();
                        BusInfo busInfo = (BusInfo) marker.getTag();
                        String vehicleNumber = busNumberToVehicleNumberMap.get(busInfo.getBusShortName());
                        if (!vehicleNumber.equalsIgnoreCase(busInfo.getVehicleNumber())) {
                            marker.remove();
                        }

                    }
                }

                // Log.d("MAIN Class data", response);
            }

            @Override
            public void errorResponse(VolleyError error, String url) {

                Log.d(TAG, error.getMessage());
            }


        });


        apiCaller.makeStringRequest(MainActivity.this, Request.Method.GET, Constants.GET_ALL_ROUTE_JSON, null);
    }


    private Bitmap changeBitmapColor(int color) {

        Bitmap ob = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_bus_marker);
        Bitmap obm = Bitmap.createBitmap(ob.getWidth(), ob.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap overlay = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_bus_marker);
        Bitmap overlaym = Bitmap.createBitmap(overlay.getWidth(), overlay.getHeight(), Bitmap.Config.ARGB_8888);


        Canvas canvas = new Canvas(overlaym);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(ob, 0f, 0f, paint);
        canvas.drawBitmap(overlay, 0f, 0f, null);
        return overlaym;
    }

    private void findTripAndBusNumbersToTrack(final List<BusInfo> busInfoList, List<BusRoute> busRouteList) {


        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        for (final BusRoute busRoute : busRouteList
                ) {
            if (!busNumberToVehicleNumberMap.containsKey(busRoute.getIndividualBusSteps().getBusShortName())) {
                Query query = database.child("busNumber-with-trips2").child(busRoute.getIndividualBusSteps().getBusShortName().trim());
                long arrivalTime = busRoute.getIndividualBusSteps().getArrivalTime();
                long departTime = busRoute.getIndividualBusSteps().getDepartureTime();
                Date date = new Date(departTime * 1000);
                final String departStationName = busRoute.getIndividualBusSteps().getDepartureStopName();


                Log.d("departTime", date.toString());

                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("MST"));
                final String departTimeString = df.format(date).trim();
                Log.d("departTime", busRoute.getIndividualBusSteps().getBusShortName() + "-" + departTimeString);

                query.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Map<String, HashMap<String, HashMap<String, String>>> mapofBusNumberAndTripDetails = (Map<String, HashMap<String, HashMap<String, String>>>) dataSnapshot.getValue();

                        Log.d("tripss", String.valueOf(mapofBusNumberAndTripDetails.size()));

                        Iterator<Map.Entry<String, HashMap<String, HashMap<String, String>>>> it = mapofBusNumberAndTripDetails.entrySet().iterator();

                        String finalTrip = "";
                        while (it.hasNext()) {
                            Map.Entry<String, HashMap<String, HashMap<String, String>>> pair = it.next();
                            //Log.d("tripss", pair.getKey() + " = " + pair.getValue());
                            HashMap<String, HashMap<String, String>> singleBusTripDetailsHashMap = pair.getValue();

                            if (singleBusTripDetailsHashMap.containsKey(departTimeString)) {
                                HashMap<String, String> busMap = singleBusTripDetailsHashMap.get(departTimeString);
                                String stopName = busMap.get("stop_name");
                                if (stopName.equalsIgnoreCase(departStationName)) {
                                    finalTrip = pair.getKey();
                                    break;
                                }

                            }
                            it.remove(); // avoids a ConcurrentModificationException
                        }

                        Log.d("finalTrip", finalTrip);

                        for (BusInfo busInfo :
                                busInfoList) {
                            if (busInfo.getTripId().equalsIgnoreCase(finalTrip)) {
                                busNumberToVehicleNumberMap.put(busRoute.getIndividualBusSteps().getBusShortName(), busInfo.getVehicleNumber());
                            }
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
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
                //getAllTripDetails(busRoute);
                for (BusInfo busInfo :
                        busInfoArrayList) {
                    LatLng markerLatLng = new LatLng(busInfo.getLatitude(), busInfo.getLongitude());
                    if (mMap != null) {

                        if (!mMarkerMap.containsKey(busInfo.getVehicleNumber())) {
                            mMarkerMap.put(busInfo.getVehicleNumber(), mMap.addMarker(new MarkerOptions()
                                    .position(markerLatLng)
                                    .title(busInfo.getVehicleNumber() + "-" + busInfo.getBusShortName().trim())
                                    .snippet("TripNumber - " + busInfo.getTripId() + "\n"
                                            + "Next Stop - " + busInfo.getNextStop() + "\n"
                                            + " At " + busInfo.getNextStopScheduleTime().toString())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker))));
                        } else {
                            Marker marker = mMarkerMap.get(busInfo.getVehicleNumber());
                            marker.setTitle(busInfo.getVehicleNumber() + "-" + busInfo.getBusShortName().trim());
                            marker.setSnippet("TripNumber - " + busInfo.getTripId() + "\n"
                                    + "Next Stop - " + busInfo.getNextStop() + "\n"
                                    + " At " + busInfo.getNextStopScheduleTime().toString());
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker));
                            MarkerAnimation.animateMarkerToICS(marker, markerLatLng, new LatLngInterpolator.Spherical());
                        }

                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng));
                    }
                }
                Log.d("MAIN Class data", response);
            }

            private void getAllTripDetails(final BusRoute busRoute) {

                final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                Query query = database.child("busNumber-with-trips1").child(busRoute.getIndividualBusSteps().getBusShortName().trim());
                long arrivalTime = busRoute.getIndividualBusSteps().getArrivalTime();
                long departTime = busRoute.getIndividualBusSteps().getDepartureTime();
                Date date = new Date(departTime * 1000);

                Log.d("departTime", date.toString());

                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("MST"));
                final String departTimeString = df.format(date);
                Log.d("departTime", busRoute.getIndividualBusSteps().getBusShortName() + "-" + departTimeString);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Map<String, ArrayList<HashMap<String, String>>> mapofBusNumberAndTripDetails = (Map<String, ArrayList<HashMap<String, String>>>) dataSnapshot.getValue();

                        Log.d("tripss", String.valueOf(mapofBusNumberAndTripDetails.size()));

                        Iterator<Map.Entry<String, ArrayList<HashMap<String, String>>>> it = mapofBusNumberAndTripDetails.entrySet().iterator();

                        String finalTrip = "";
                        while (it.hasNext()) {
                            Map.Entry<String, ArrayList<HashMap<String, String>>> pair = it.next();
                            //Log.d("tripss", pair.getKey() + " = " + pair.getValue());
                            ArrayList<HashMap<String, String>> singleBusTripDetailsArrayList = pair.getValue();

                            for (HashMap<String, String> eachBusTripStopsDetails :
                                    singleBusTripDetailsArrayList) {

                                if (eachBusTripStopsDetails.get("departTime").equalsIgnoreCase(departTimeString)) {
                                    finalTrip = pair.getKey();
                                    break;
                                }
                            }
                            it.remove(); // avoids a ConcurrentModificationException
                        }

                        Log.d("finalTrip", finalTrip);
                        ArrayList<HashMap<String, String>> finalBusTripDetailsArrayList = mapofBusNumberAndTripDetails.get(finalTrip);
                        if (busRoute.getIndividualBusSteps().getBusInfoList().size() > 0)
                            Log.d("finalTrip", busRoute.getIndividualBusSteps().getBusInfoList().get(0).getNextStop());

                        for (BusInfo busInfo :
                                busRoute.getIndividualBusSteps().getBusInfoList()) {

                            // String stopCode = mBusStopNameToStopCodeMap.get(busInfo.getNextStop());
                            for (HashMap<String, String> eachBusDetails :
                                    finalBusTripDetailsArrayList) {
//                                if(busInfo.)


                            }
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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

        removeAllPolyline();
        directionsTransitModel.setTypeOfRoute(DirectionsTransitModel.RouteType.WALKING);
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

    private void drawBikeAndBusRoutePolylines(DirectionsTransitModel directionsTransitModel) {

        boolean firstBikeRouteDrawn = false;
        directionsTransitModel.setTypeOfRoute(DirectionsTransitModel.RouteType.BIKE);
        removeAllPolyline();
        for (RouteInfo routeInfo :
                directionsTransitModel.getmListOfRoutes()) {
            if (routeInfo.transitMode().equalsIgnoreCase("TRANSIT")) {
                BusRoute busRoute = ((BusRoute) routeInfo);
                ArrayList<LatLng> latLngs = busRoute.getPolylineLatLngPoints();
                Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(latLngs).width(7).color(Color.parseColor(busRoute.getIndividualBusSteps().getBusColor())));
                mListOfPolyLines.add(polyline);
            } else {
                if (firstBikeRouteDrawn) {
                    ArrayList<LatLng> latLngs = ((WalkingRoute) routeInfo).getPolylineLatLngPoints();
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(latLngs).width(7).color(Color.BLUE).pattern(Arrays.<PatternItem>asList(
                            new Dot(), new Gap(20))));
                    polyline.setJointType(JointType.ROUND);
                    mListOfPolyLines.add(polyline);
                } else {
                    firstBikeRouteDrawn = true;
                    ArrayList<LatLng> latLngs = directionsTransitModel.getFirstBicycleRoute().getPolylineLatLngPoints();
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(latLngs).width(7).color(Color.BLUE).pattern(Arrays.<PatternItem>asList(
                            new Dot(), new Gap(20))));
                    polyline.setJointType(JointType.ROUND);
                    mListOfPolyLines.add(polyline);
                }

            }
        }
    }

    private void removeAllPolylineAndMarkers() {

        removeAllPolyline();


        if (mMarkerMap != null) {

            Iterator<Map.Entry<String, Marker>> it = mMarkerMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Marker> pair = it.next();
                Marker marker = pair.getValue();
                marker.remove();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
            mMarkerMap = null;
        }
        mMarkerMap = new HashMap<>();


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


    private void getTimeForBicycleForBusStop(final DirectionsTransitModel directionsTransitModel) {
        ApiCaller apiCaller = new ApiCaller();
        apiCaller.setAfterApiCallResponse(new ApiCaller.AfterApiCallResponse() {
            @Override
            public void successResponse(String response, String url) {

                try {
                    DirectionsTransitModel bikeDirectionsTransitModel = new DirectionParseJson().parseRoute(response, MainActivity.this);
                    if (directionsTransitModel.getmListOfRoutes().size() > 0) {
                        directionsTransitModel.setFirstBicycleRoute(bikeDirectionsTransitModel);
                    }
                    if (mDestinationRouteDirectionsFragment != null)
                        mDestinationRouteDirectionsFragment.updateBicycleTimeDetails(bikeDirectionsTransitModel);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("MAIN Class data", response);
            }

            @Override
            public void errorResponse(VolleyError error, String url) {
                Log.d(TAG, error.getMessage());

            }


        });
        ArrayList<RouteInfo> routeInfoArrayList = directionsTransitModel.getmListOfRoutes();
        WalkingRoute walkingRoute = null;
        for (int i = 0; i < routeInfoArrayList.size(); i++) {
            if (routeInfoArrayList.get(i).transitMode().equalsIgnoreCase("WALKING")) {
                walkingRoute = (WalkingRoute) routeInfoArrayList.get(i);
                directionsTransitModel.setFirstWalkingRoute(walkingRoute);
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
    public void showDirections() {

        if (currentDirectionsTransitModel != null) {
            //this is code for fragment
            mDirectionsFragment = new DirectionsFragment();
            fragment = mDirectionsFragment;

            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.activity_animation_push_down_in, R.anim.activity_animation_push_up_out)
                    .replace(R.id.directions_frame_container, fragment).commit();
            fragmentManager.beginTransaction().addToBackStack(null);

            getUpdatedData();
        }
    }

    @Override
    public void showBicyclePolyLines() {
        if (currentDirectionsTransitModel != null) {
            drawBikeAndBusRoutePolylines(currentDirectionsTransitModel);
        }
    }

    @Override
    public void showWalkPolyLines() {
        if (currentDirectionsTransitModel != null) {
            drawWalkingAndBusRoutePolylines(currentDirectionsTransitModel);
        }
    }

    @Override
    public void closeTripDetailsFragment() {
        if (mDestinationRouteDirectionsFragment != null) {
            if (getSupportFragmentManager().findFragmentById(R.id.trip_details_frame_container) != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.activity_animation_push_down_in, R.anim.activity_animation_push_up_out);


                fragmentTransaction.remove(getSupportFragmentManager().findFragmentById(R.id.trip_details_frame_container)).commit();
            }
            mDestinationRouteDirectionsFragment = null;
            currentDirectionsTransitModel = null;
            removeAllPolylineAndMarkers();

        }
    }


    public void mapInitialize() {
        final Context mContext = this;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mContext);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }


    @Override
    public void closeTheDirectionFragment() {
        if (getSupportFragmentManager().findFragmentById(R.id.directions_frame_container) != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.activity_animation_push_down_in, R.anim.activity_animation_push_up_out);


            fragmentTransaction.remove(getSupportFragmentManager().findFragmentById(R.id.directions_frame_container)).commit();
        }


    }

    @Override
    public void getUpdatedData() {
        if (mDirectionsFragment != null && currentDirectionsTransitModel != null) {
            mDirectionsFragment.updateDataInAdapter(currentDirectionsTransitModel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
