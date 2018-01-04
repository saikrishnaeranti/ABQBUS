package edu.unm.albuquerquebus.live.model;

import java.util.ArrayList;

import edu.unm.albuquerquebus.live.interfaces.RouteInfo;

/**
 * Created by saikrishna on 10/28/17.
 */

public class BikeRoute extends Route {

    private boolean individualRoute = false;
    private ArrayList<RouteInfo> mListOfSubRoute;

    public BikeRoute() {
        mListOfSubRoute = new ArrayList<>();
    }

    public ArrayList<RouteInfo> getListOfSubRoute() {
        return mListOfSubRoute;
    }

    public void setListOfSubRoute(ArrayList<RouteInfo> listOfSubRoute) {
        mListOfSubRoute = listOfSubRoute;
    }

    public boolean isIndividualRoute() {
        return individualRoute;
    }

    public void setIndividualRoute(boolean individualRoute) {
        this.individualRoute = individualRoute;
    }

    @Override
    public String transitMode() {
        return "DRIVING";
    }
}
