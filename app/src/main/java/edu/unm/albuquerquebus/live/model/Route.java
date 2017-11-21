package edu.unm.albuquerquebus.live.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import edu.unm.albuquerquebus.live.interfaces.RouteInfo;

/**
 * Created by saikrishna on 10/28/17.
 */

public abstract class Route implements RouteInfo {
    private long distance = 0;
    private long duration = 0;
    private LatLng endLocation = null;
    private LatLng startLocation = null;
    private String htmlInstructions = null;
    private String polylinePoints = null;
    private ArrayList<LatLng> polylineLatLngPoints;



    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public String getHtmlInstructions() {
        return htmlInstructions;
    }

    public void setHtmlInstructions(String htmlInstructions) {
        this.htmlInstructions = htmlInstructions;
    }

    public String getPolylinePoints() {
        return polylinePoints;
    }

    public void setPolylinePoints(String polylinePoints) {
        this.polylinePoints = polylinePoints;
    }

    public ArrayList<LatLng> getPolylineLatLngPoints() {
        return polylineLatLngPoints;
    }

    public void setPolylineLatLngPoints(ArrayList<LatLng> polylineLatLngPoints) {
        this.polylineLatLngPoints = polylineLatLngPoints;
    }
}
