package edu.unm.albuquerquebus.live.utils;


/**
 * Copyright (C) PickQuick, Inc - All Rights Reserve
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Created by pruthviraj on 01/02/16.
 * Last Updated by pruthviraj on 01/02/16.
 * Copyright
 */
public class Constants {

    static final String ERROR = "error";
    static final String TOAST_CHECK_CONNECTIVITY = "Please check your internet connectivity.";
    public static final String DATA_RETURNED = "Data returned : ";
    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    public static final String NEW_LINE = "\n";
    public static final String ERROR_CLOSING_STREAM = "Error closing stream ";
    public static final String NOTIFICATION_DETAILS = "Notification details";
    public static final String SEND_GCM_TOKEN = "Sent Gcm Token";
    public static final String DIDNT_SEND_GCM_TOKEN = "Didn't send Gcm Token ";
    public static final String SENT_GCM_TOKEN_TO_SERVER = "sent_gcm_token_to_server";
    public static final String GCM_TOKEN = "gcm_token";
    public static final String GET_ROUTE_URL = "http://data.cabq.gov/transit/realtime/route/route%s.kml";
    public static final String GET_ALL_ROUTE_JSON = "http://data.cabq.gov/transit/realtime/route/allroutes.json";
    public static final String GET_MAP_URL = "https://maps.googleapis.com/maps/api/directions/json";
    static final String GET_SNAP_TO_ROAD_URL = "https://roads.googleapis.com/v1/snapToRoads";
//    public static final String GET_MAP_TEMP_URL = "https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=";

    //Map Parameters
    public static final String ORIGIN = "origin";
    public static final String DESTINATION = "destination";
    public static final String KEY = "key";
    public static final String MODE = "mode";

    public static final String CONNECTION_FAILED_AND_CONNECTION_RESULT_CODE = "Connection failed: ConnectionResult.getErrorCode() = ";


    public static final String DEPART_TIME = "departure_time";

    static final String INTERPOLATE = "interpolate";
    static final String PATH = "path";


    static final String ALL_ROUTES = "allroutes";
    static final String VEHICLE_ID = "vehicle_id";
    static final String MESSAGE_TIME = "msg_time";
    static final String ROUTE_SHORT_NAME = "route_short_name";
    static final String TRIP_ID = "trip_id";
    static final String NEXT_STOP_ID = "next_stop_id";
    static final String NEXT_STOP_NAME = "next_stop_name";
    static final String NEXT_STOP_SCHEDULE_TIME = "next_stop_sched_time";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String HEADING = "heading";
    static final String SPEED_IN_MPH = "speed_mph";
    static final String STOP_ID = "stop_id";
    static final String STOP_NAME = "stop_name";
    static final String STOP_TIME = "stop_time";
    public static final String ERROR_IN_CREATING_FRAGMENT = "Error in creating fragment";
    ;

    public static final String WALKING = "WALKING";
    public static final String BIKE = "DRIVING";

    public static final String TRANSIT = "TRANSIT";
    public static final String NO_BUSES_IN_THIS_ROUTE = "There are no buses available for this route.";
    public static final String SHARED_PREFERENCE_KEY = "MyPref";
}