package edu.unm.albuquerquebus.live.model;

import com.google.android.gms.maps.model.LatLng;

import edu.unm.albuquerquebus.live.RouteInfo;

/**
 * Created by saikrishna on 10/28/17.
 */

public class BusRoute extends Route {


    private IndividualBusSteps mIndividualBusSteps;

    public IndividualBusSteps getIndividualBusSteps() {
        return mIndividualBusSteps;
    }

    public void setIndividualBusSteps(IndividualBusSteps individualBusSteps) {
        mIndividualBusSteps = individualBusSteps;
    }

    @Override
    public String transitMode() {
        return "TRANSIT";
    }
}
