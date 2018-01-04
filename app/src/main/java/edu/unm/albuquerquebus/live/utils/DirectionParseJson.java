package edu.unm.albuquerquebus.live.utils;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.unm.albuquerquebus.live.R;
import edu.unm.albuquerquebus.live.interfaces.RouteInfo;
import edu.unm.albuquerquebus.live.model.BikeRoute;
import edu.unm.albuquerquebus.live.model.BusRoute;
import edu.unm.albuquerquebus.live.model.DirectionsTransitModel;
import edu.unm.albuquerquebus.live.model.IndividualBusSteps;
import edu.unm.albuquerquebus.live.model.WalkingRoute;

/**
 * Created by saikrishna on 10/28/17.
 */

public class DirectionParseJson {

    private static final String TAG = DirectionParseJson.class.getSimpleName();

    // Parse route from Google Directions API response
    // Return route object corresponding to response provided by Google Directions API
    /*To complete this task you must implement the parseRoute method specified in the GoogleDirectionsParser class.
     This method must produce a BusRoute object that contains a WalkingRoute object for each leg found in the JSON response.
     Each WalkingRoute object must contain a LatLng point for each point in each step of the leg.
      */
    public DirectionsTransitModel parseRoute(java.lang.String response, Activity activity)
            throws org.json.JSONException {
        JSONObject route = new JSONObject(response);
        DirectionsTransitModel directionsTransitModel = new DirectionsTransitModel();

        JSONObject legs = route.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
        JSONObject overviewPolyline = route.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline");

        if (legs.has("arrival_time"))
            directionsTransitModel.setArrivalTime(legs.getJSONObject("arrival_time").getLong("value"));

        if (legs.has("departure_time"))
            directionsTransitModel.setDepartTime(legs.getJSONObject("departure_time").getLong("value"));

        if (legs.has("distance"))
            directionsTransitModel.setDistance(legs.getJSONObject("distance").getLong("value"));

        if (legs.has("duration"))
            directionsTransitModel.setDuration(legs.getJSONObject("duration").getLong("value"));

        if (legs.has("end_address"))
            directionsTransitModel.setEndAddress(legs.getString("end_address"));

        if (legs.has("end_location"))
            directionsTransitModel.setEndLocation(new LatLng(legs.getJSONObject("end_location").getDouble("lat"),
                legs.getJSONObject("end_location").getDouble("lng")));

        if (legs.has("start_address"))
            directionsTransitModel.setStartAddress(legs.getString("start_address"));

        if (legs.has("start_location"))
            directionsTransitModel.setStartLocation(new LatLng(legs.getJSONObject("start_location").getDouble("lat"),
                legs.getJSONObject("start_location").getDouble("lng")));

        if (overviewPolyline.has("points")) {
            directionsTransitModel.setPolylinePointsString(overviewPolyline.getString("points"));
            directionsTransitModel.setPolylineLatLngPoints(decodePoly(directionsTransitModel.getPolylinePointsString()));
        }
        if(legs.has("steps")) {
            JSONArray steps = legs.getJSONArray("steps");
            int noOfBuses = 0;
            for (int j = 0; j < steps.length(); j++) {

                JSONObject step = steps.getJSONObject(j);
                if (step.getString("travel_mode").equalsIgnoreCase("WALKING")) {

                    directionsTransitModel.getmListOfRoutes().add(getDetailsOfWalkingRoute(step, false));

                } else if (step.getString("travel_mode").equalsIgnoreCase("TRANSIT")) {
                    noOfBuses++;
                    directionsTransitModel.getmListOfRoutes().add(getDetailsOfBusRoute(step, activity));
                } else if (step.getString("travel_mode").equalsIgnoreCase("DRIVING")) {
                    directionsTransitModel.getmListOfRoutes().add(getDetailsOfBikeRoute(step, false));
                }



            }
            directionsTransitModel.setTotalNumberOfBuses(noOfBuses);
            Log.i(TAG, "noOfBuses: " + noOfBuses);


        }
        return directionsTransitModel;
    }

    /**
     * Method Courtesy :
     * jeffreysambells.com/2010/05/27
     * /decoding-polylines-from-google-maps-direction-api-with-java
     */
    private ArrayList<LatLng> decodePoly(String encoded) {

        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void getSnapPointsOfRoute(final Activity activity, final BusRoute busRoute) {
        ArrayList<LatLng> latLngs = busRoute.getPolylineLatLngPoints();
        Log.i(TAG, "latLngs size: " + latLngs.size());
        ApiCaller apiCaller = new ApiCaller();
        apiCaller.setAfterApiCallResponse(new ApiCaller.AfterApiCallResponse() {

            @Override
            public void successResponse(String response, String url) {
                //Log.i(TAG, "SNAP TO Road Response: " +response);
                try {
                    ArrayList<LatLng> tempLatLngsList = new DirectionParseJson().getListOfLatLngs(response);
                    busRoute.setPolylineLatLngPoints(tempLatLngsList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void errorResponse(VolleyError error, String url) {

                error.printStackTrace();
            }
        });


        String latLngStringParameter = "";

        for (int i = 0; i < latLngs.size() && i < latLngs.size(); i++) {

            LatLng latLng = latLngs.get(i);
            latLngStringParameter = latLngStringParameter.concat(String.valueOf(latLng.latitude));
            latLngStringParameter = latLngStringParameter.concat(",");
            latLngStringParameter = latLngStringParameter.concat(String.valueOf(latLng.longitude));
            if (i != latLngs.size() - 1) {
                latLngStringParameter = latLngStringParameter.concat("|");
            }

        }
        String finalUrl = Constants.GET_SNAP_TO_ROAD_URL;
        finalUrl += "?";
        finalUrl += Constants.INTERPOLATE + "=true";
        finalUrl += "&";
        finalUrl += Constants.KEY + "=" + activity.getResources().getString(R.string.google_maps_key);
        finalUrl += "&";
        finalUrl += Constants.PATH + "=" + latLngStringParameter;


        apiCaller.makeStringRequest(activity, Request.Method.GET, finalUrl, null);

    }


    private RouteInfo getDetailsOfBusRoute(JSONObject step, Activity activity) throws JSONException {
        BusRoute busRoute = new BusRoute();

        if (step.has("distance"))
        busRoute.setDistance(step.getJSONObject("distance").getLong("value"));

        if (step.has("duration"))
        busRoute.setDuration(step.getJSONObject("duration").getLong("value"));

        if (step.has("start_location"))
        busRoute.setStartLocation(new LatLng(step.getJSONObject("start_location").getDouble("lat"),
                step.getJSONObject("start_location").getDouble("lng")));

        if (step.has("end_location"))
        busRoute.setEndLocation(new LatLng(step.getJSONObject("end_location").getDouble("lat"),
                step.getJSONObject("end_location").getDouble("lng")));

        if (step.has("html_instructions"))
        busRoute.setHtmlInstructions(step.getString("html_instructions"));
        if (step.has("polyline")) {
            busRoute.setPolylinePoints(step.getJSONObject("polyline").getString("points"));
            busRoute.setPolylineLatLngPoints(decodePoly(busRoute.getPolylinePoints()));
            //getSnapPointsOfRoute(activity,busRoute);
        }


        if (step.has("transit_details")) {
            JSONObject transitDetails = step.getJSONObject("transit_details");
            IndividualBusSteps individualBusSteps = new IndividualBusSteps();


            if (transitDetails.has("arrival_stop"))
                individualBusSteps.setArrivalStopLocation(new LatLng(transitDetails.getJSONObject("arrival_stop").getJSONObject("location").getDouble("lat"),
                    transitDetails.getJSONObject("arrival_stop").getJSONObject("location").getDouble("lng")));
            if (transitDetails.has("departure_stop"))
            individualBusSteps.setDepartureStopLocation(new LatLng(transitDetails.getJSONObject("departure_stop").getJSONObject("location").getDouble("lat"),
                    transitDetails.getJSONObject("departure_stop").getJSONObject("location").getDouble("lng")));

            if (transitDetails.has("arrival_stop"))
            individualBusSteps.setArrivalStopName(transitDetails.getJSONObject("arrival_stop").getString("name"));
            if (transitDetails.has("departure_stop"))
            individualBusSteps.setDepartureStopName(transitDetails.getJSONObject("departure_stop").getString("name"));

            if (transitDetails.has("departure_time")) {
                individualBusSteps.setDepartureTime(transitDetails.getJSONObject("departure_time").getLong("value"));
                individualBusSteps.setDepartureTimeString(transitDetails.getJSONObject("departure_time").getString("text"));
            }
            if (transitDetails.has("arrival_time")) {
                individualBusSteps.setArrivalTime(transitDetails.getJSONObject("arrival_time").getLong("value"));
                individualBusSteps.setArrivalTimeString(transitDetails.getJSONObject("arrival_time").getString("text"));
            }


            if (transitDetails.has("headsign"))
            individualBusSteps.setHeadSign(transitDetails.getString("headsign"));
            if (transitDetails.has("line")) {
                if (transitDetails.getJSONObject("line").has("name"))
                    individualBusSteps.setBusName(transitDetails.getJSONObject("line").getString("name"));
                if (transitDetails.getJSONObject("line").has("short_name"))
                    individualBusSteps.setBusShortName(transitDetails.getJSONObject("line").getString("short_name"));
                if (transitDetails.getJSONObject("line").has("color"))
                    individualBusSteps.setBusColor(transitDetails.getJSONObject("line").getString("color"));


            }

            if (transitDetails.has("num_stops"))
            individualBusSteps.setNoOfBusStops(transitDetails.getInt("num_stops"));
            busRoute.setIndividualBusSteps(individualBusSteps);


        }


        return busRoute;
    }

    private RouteInfo getDetailsOfWalkingRoute(JSONObject step, boolean individualRoute) throws JSONException {

        Log.d("MAIN Class data", step.toString());
        WalkingRoute walkingRoute = new WalkingRoute();

        if (step.has("distance"))
            walkingRoute.setDistance(step.getJSONObject("distance").getLong("value"));
        if (step.has("duration"))
            walkingRoute.setDuration(step.getJSONObject("duration").getLong("value"));
        if (step.has("start_location"))
            walkingRoute.setStartLocation(new LatLng(step.getJSONObject("start_location").getDouble("lat"),
                step.getJSONObject("start_location").getDouble("lng")));
        if (step.has("end_location"))
            walkingRoute.setEndLocation(new LatLng(step.getJSONObject("end_location").getDouble("lat"),
                step.getJSONObject("end_location").getDouble("lng")));

        if (step.has("html_instructions"))
            walkingRoute.setHtmlInstructions(step.getString("html_instructions"));
        if (step.has("polyline")) {
            walkingRoute.setPolylinePoints(step.getJSONObject("polyline").getString("points"));
            walkingRoute.setPolylineLatLngPoints(decodePoly(walkingRoute.getPolylinePoints()));
        }

        walkingRoute.setIndividualRoute(individualRoute);

        if (!individualRoute) {
            if (step.has("steps")) {
                JSONArray tempSteps = step.getJSONArray("steps");
                for (int j = 0; j < tempSteps.length(); j++) {
                    JSONObject tempStep = tempSteps.getJSONObject(j);
                    walkingRoute.getListOfSubRoute().add(getDetailsOfWalkingRoute(tempStep, true));
                }
            }
        }

        return walkingRoute;
    }

    private RouteInfo getDetailsOfBikeRoute(JSONObject step, boolean individualRoute) throws JSONException {

        Log.d("MAIN Class data", step.toString());
        BikeRoute bikeRoute = new BikeRoute();

        if (step.has("distance"))
            bikeRoute.setDistance(step.getJSONObject("distance").getLong("value"));
        if (step.has("duration"))
            bikeRoute.setDuration(step.getJSONObject("duration").getLong("value"));
        if (step.has("start_location"))
            bikeRoute.setStartLocation(new LatLng(step.getJSONObject("start_location").getDouble("lat"),
                    step.getJSONObject("start_location").getDouble("lng")));
        if (step.has("end_location"))
            bikeRoute.setEndLocation(new LatLng(step.getJSONObject("end_location").getDouble("lat"),
                    step.getJSONObject("end_location").getDouble("lng")));

        if (step.has("html_instructions"))
            bikeRoute.setHtmlInstructions(step.getString("html_instructions"));
        if (step.has("polyline")) {
            bikeRoute.setPolylinePoints(step.getJSONObject("polyline").getString("points"));
            bikeRoute.setPolylineLatLngPoints(decodePoly(bikeRoute.getPolylinePoints()));
        }

        bikeRoute.setIndividualRoute(individualRoute);

        if (!individualRoute) {
            if (step.has("steps")) {
                JSONArray tempSteps = step.getJSONArray("steps");
                for (int j = 0; j < tempSteps.length(); j++) {
                    JSONObject tempStep = tempSteps.getJSONObject(j);
                    bikeRoute.getListOfSubRoute().add(getDetailsOfBikeRoute(tempStep, true));
                }
            }
        }

        return bikeRoute;
    }

    private ArrayList<LatLng> getListOfLatLngs(String response) throws JSONException {
        ArrayList<LatLng> latLngsList = new ArrayList<>();
        JSONObject responseJsonObject = new JSONObject(response);

        if (responseJsonObject.has("snappedPoints")) {

            JSONArray snapPointJsonArray = responseJsonObject.getJSONArray("snappedPoints");
            for (int j = 0; j < snapPointJsonArray.length(); j++) {
                JSONObject pointJsonObject = snapPointJsonArray.getJSONObject(j);
                JSONObject pointLocationJsonObject = pointJsonObject.getJSONObject("location");
                latLngsList.add(new LatLng(pointLocationJsonObject.getDouble("latitude"), pointLocationJsonObject.getDouble("longitude")));

            }
        }

        return latLngsList;
    }
}
