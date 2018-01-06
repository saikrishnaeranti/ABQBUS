package edu.unm.albuquerquebus.live.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.unm.albuquerquebus.live.R;
import edu.unm.albuquerquebus.live.interfaces.RouteInfo;
import edu.unm.albuquerquebus.live.model.BusInfo;
import edu.unm.albuquerquebus.live.model.BusRoute;
import edu.unm.albuquerquebus.live.model.DirectionsTransitModel;
import edu.unm.albuquerquebus.live.model.WalkingRoute;
import edu.unm.albuquerquebus.live.utils.KmlUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnDestinationRouteDirectionsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DestinationRouteDirectionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DestinationRouteDirectionsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnDestinationRouteDirectionsFragmentInteractionListener mListener;
    private TextView mDestinationTextView;
    private LinearLayout mBusLinearLayout;
    private TextView mWalkTimeTextView;
    private TextView mBicycleTimeTextView;

    private Map<String, TextView> mMapOfBusNumberToExtraTimeTextView = new HashMap<>();
    private FloatingActionButton directionsFabButton;
    private TextView mArriveAtTextView;
    private TextView mDepartAtTextView;
    private TextView mTripCompletedTextView;

    public DestinationRouteDirectionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DestinationRouteDirectionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DestinationRouteDirectionsFragment newInstance(String param1, String param2) {
        DestinationRouteDirectionsFragment fragment = new DestinationRouteDirectionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.route_fragment_layout, container, false);

        mDestinationTextView = view.findViewById(R.id.destination_text);
        mDestinationTextView.setSelected(true);

        mBusLinearLayout = view.findViewById(R.id.bus_layout);

        mWalkTimeTextView = view.findViewById(R.id.walk_time);


        mBicycleTimeTextView = view.findViewById(R.id.bicycle_time);

        mDepartAtTextView = view.findViewById(R.id.depart_at);
        mArriveAtTextView = view.findViewById(R.id.arrive_at);

        mTripCompletedTextView = view.findViewById(R.id.trip_completed);

        final FloatingActionButton bicycleFabButton = view.findViewById(R.id.bike_fab);


        final FloatingActionButton walkFabButton = view.findViewById(R.id.walk_fab);
        final FloatingActionButton closeFabButton = view.findViewById(R.id.close_fab);


        walkFabButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.littleBlue)));

        directionsFabButton = view.findViewById(R.id.direction_fab);
        directionsFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.showDirections();
                    directionsFabButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.littleBlue)));

                }
            }
        });
        bicycleFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    bicycleFabButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.littleBlue)));
                    walkFabButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    mListener.showBicyclePolyLines();
                }
            }
        });
        walkFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    bicycleFabButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    walkFabButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.littleBlue)));
                    mListener.showWalkPolyLines();
                }
            }
        });
        closeFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.closeTripDetailsFragment();
                    directionsFabButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                }
            }
        });
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mListener != null) {
            mListener.loadFragmentElements();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDestinationRouteDirectionsFragmentInteractionListener) {
            mListener = (OnDestinationRouteDirectionsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDestinationRouteDirectionsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void updateDestinationDetails(DirectionsTransitModel directionsTransitModel) {

        mDepartAtTextView.setText(KmlUtils.convertLongtoDateString(directionsTransitModel.getDepartTime()));
        mArriveAtTextView.setText(KmlUtils.convertLongtoDateString(directionsTransitModel.getArrivalTime()));
        mDestinationTextView.setText(directionsTransitModel.getEndAddress());
        addBusesInLayout(directionsTransitModel);
        addWalkingDistanceToBusStopInLayout(directionsTransitModel);
    }

    public void updateBicycleTimeDetails(DirectionsTransitModel directionsTransitModel) {

        long temp = directionsTransitModel.getDuration();
        int minutes = (int) (temp / 60);
        minutes++;
        mBicycleTimeTextView.setText(String.format("%d Min", minutes));

    }

    private void addWalkingDistanceToBusStopInLayout(DirectionsTransitModel directionsTransitModel) {

        ArrayList<RouteInfo> routeInfoArrayList = directionsTransitModel.getmListOfRoutes();
        WalkingRoute walkingRoute = null;
        for (int i = 0; i < routeInfoArrayList.size(); i++) {
            if (routeInfoArrayList.get(i).transitMode().equalsIgnoreCase("WALKING")) {
                walkingRoute = (WalkingRoute) routeInfoArrayList.get(i);
                break;
            }
        }
        if (walkingRoute != null) {
            long temp = walkingRoute.getDuration();
            int minutes = (int) (temp / 60);
            minutes++;
            mWalkTimeTextView.setText(String.format("%d Min", minutes));
        }


    }

    private void addBusesInLayout(DirectionsTransitModel directionsTransitModel) {

        mBusLinearLayout.removeAllViews();
        ArrayList<RouteInfo> listOfRoutes = directionsTransitModel.getmListOfRoutes();
        int busesAdded = 0;
        for (int i = 0; i < listOfRoutes.size(); i++) {
            if (listOfRoutes.get(i).transitMode() == "TRANSIT") {
                busesAdded++;

                BusRoute busRoute = (BusRoute) listOfRoutes.get(i);
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.each_bus_layout, null);
                TextView busNoTextView = view.findViewById(R.id.bus_no);
                TextView busTimingTextView = view.findViewById(R.id.bus_timing);
                TextView busExtraTimingTextView = view.findViewById(R.id.bus_extra_time);
                busNoTextView.setText(busRoute.getIndividualBusSteps().getBusShortName());
                busTimingTextView.setText(busRoute.getIndividualBusSteps().getDepartureTimeString());
                busTimingTextView.setTag(busRoute);
                busExtraTimingTextView.setVisibility(View.GONE);
                mMapOfBusNumberToExtraTimeTextView.put(busRoute.getIndividualBusSteps().getBusShortName(), busExtraTimingTextView);
                mBusLinearLayout.addView(view);
                if (busesAdded < directionsTransitModel.getTotalNumberOfBuses()) {
                    View arrowView = inflater.inflate(R.layout.bus_to_bus_layout, null);
                    mBusLinearLayout.addView(arrowView);

                }

            }
        }

    }

    public void updateDelayTime(BusInfo busInfo) {

        if (mMapOfBusNumberToExtraTimeTextView.containsKey(busInfo.getBusShortName())) {
            TextView busExtraTimingTextView = mMapOfBusNumberToExtraTimeTextView.get(busInfo.getBusShortName());
            Date currentTime = new Date();
            // Date busBoardedTime
            Date scheduledTime = busInfo.getNextStopScheduleTime();

            long diff = currentTime.getTime() - scheduledTime.getTime();
            if (diff > 0) {
                long diffSeconds = diff / 1000 % 60;
                long diffMinutes = diff / (60 * 1000) % 60;
                if (diffMinutes > 0) {
                    busExtraTimingTextView.setText("(+" + diffMinutes + ")");
                    busExtraTimingTextView.setVisibility(View.VISIBLE);
                    Log.d("DifferenceTime", String.valueOf(diffMinutes));
                } else {
                    busExtraTimingTextView.setVisibility(View.GONE);
                }
            }


        }
    }

    public void resetColorOfDirectionFab() {
        directionsFabButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
    }

    public void showTripCompletedTextView() {
        mTripCompletedTextView.setVisibility(View.VISIBLE);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDestinationRouteDirectionsFragmentInteractionListener {
        // TODO: Update argument type and name
        void showDirections();

        void showBicyclePolyLines();

        void showWalkPolyLines();

        void closeTripDetailsFragment();

        void loadFragmentElements();
    }
}
