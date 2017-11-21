package edu.unm.albuquerquebus.live.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import edu.unm.albuquerquebus.live.interfaces.RouteInfo;

/**
 * Created by saikrishna on 10/5/17.
 */

public class DirectionsTransitModel {


    public enum RouteType {
        WALKING,
        TRANSITBUS
    }

    private RouteType typeOfRoute;

    private ArrayList<RouteInfo> mListOfRoutes;

    // Short Details of the route
    private long arrivalTime;
    private long departTime;
    // distance is in meters
    private long distance;
    //duration of route
    private long duration;
    private String startAddress;
    private LatLng startLocation;
    private String endAddress;
    private LatLng endLocation;


    //polyline
    private String polylinePointsString;
    private ArrayList<LatLng> polylineLatLngPoints;

    //Number of Buses
    private int mTotalNumberOfBuses;

    public int getTotalNumberOfBuses() {
        return mTotalNumberOfBuses;
    }

    public void setTotalNumberOfBuses(int totalNumberOfBuses) {
        mTotalNumberOfBuses = totalNumberOfBuses;
    }

    public DirectionsTransitModel() {
        mListOfRoutes = new ArrayList<>();
    }

    public RouteType getTypeOfRoute() {
        return typeOfRoute;
    }

    public void setTypeOfRoute(RouteType typeOfRoute) {
        this.typeOfRoute = typeOfRoute;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    public ArrayList<RouteInfo> getmListOfRoutes() {
        return mListOfRoutes;
    }

    public void setmListOfRoutes(ArrayList<RouteInfo> mListOfRoutes) {
        this.mListOfRoutes = mListOfRoutes;
    }

    public String getPolylinePointsString() {
        return polylinePointsString;
    }

    public void setPolylinePointsString(String polylinePointsString) {
        this.polylinePointsString = polylinePointsString;
    }

    public long getDepartTime() {
        return departTime;
    }

    public void setDepartTime(long departTime) {
        this.departTime = departTime;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }


    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }


    public ArrayList<LatLng> getPolylineLatLngPoints() {
        return polylineLatLngPoints;
    }

    public void setPolylineLatLngPoints(ArrayList<LatLng> polylineLatLngPoints) {
        this.polylineLatLngPoints = polylineLatLngPoints;
    }
}
