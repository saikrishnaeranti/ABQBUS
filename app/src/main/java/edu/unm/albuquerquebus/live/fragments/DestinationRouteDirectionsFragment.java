package edu.unm.albuquerquebus.live.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import edu.unm.albuquerquebus.live.R;
import edu.unm.albuquerquebus.live.RouteInfo;
import edu.unm.albuquerquebus.live.model.BusRoute;
import edu.unm.albuquerquebus.live.model.DirectionsTransitModel;

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
        /*mOrders = new ArrayList<>();
        mProgressBarLayout = (FrameLayout) getActivity().findViewById(R.id.progress_bar_main_layout);
        mContentResolver = getActivity().getContentResolver();
        ProcessRestaurantOrders processRestaurantOrders = new ProcessRestaurantOrders(mActivity, mContentResolver);
        processRestaurantOrders.execute();
        mProgressBarLayout = (FrameLayout) getActivity().findViewById(R.id.progress_bar_main_layout);
        mAdapter = new OrdersCustomAdapter(getActivity(), getChildFragmentManager(), mOrders, mTypeOfList, this);
        mOrdersListView = (ListView) view.findViewById(R.id.orders_list);
        mOrdersListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);*/
        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

        mDestinationTextView.setText(directionsTransitModel.getEndAddress());
        addBusesInLayout(directionsTransitModel);
        addWalkingDistanceToBusStopInLayout(directionsTransitModel);
    }

    private void addWalkingDistanceToBusStopInLayout(DirectionsTransitModel directionsTransitModel) {


    }

    private void addBusesInLayout(DirectionsTransitModel directionsTransitModel) {

        mBusLinearLayout.removeAllViews();
        ArrayList<RouteInfo> listOfRoutes = directionsTransitModel.getmListOfRoutes();

        for (int i = 0; i < listOfRoutes.size(); i++) {
            if(listOfRoutes.get(i).transitMode() == "TRANSIT"){
                BusRoute busRoute = (BusRoute) listOfRoutes.get(i);
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.each_bus_layout, null);
                TextView busNoTextView = view.findViewById(R.id.bus_no);
                busNoTextView.setText(busRoute.getIndividualBusSteps().getBusShortName());
                mBusLinearLayout.addView(view);
            }
        }
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
        void onFragmentInteraction(Uri uri);

    }
}