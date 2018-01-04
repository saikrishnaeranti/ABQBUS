package edu.unm.albuquerquebus.live.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import edu.unm.albuquerquebus.live.R;
import edu.unm.albuquerquebus.live.adapters.DirectionsRecyclerViewAdapter;
import edu.unm.albuquerquebus.live.interfaces.RouteInfo;
import edu.unm.albuquerquebus.live.model.DirectionsTransitModel;
import edu.unm.albuquerquebus.live.model.WalkingRoute;
import edu.unm.albuquerquebus.live.utils.Constants;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DirectionsFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private List<RouteInfo> mRouteInfoList;
    private DirectionsRecyclerViewAdapter directionsRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DirectionsFragment() {
        mRouteInfoList = new ArrayList<>();
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DirectionsFragment newInstance(int columnCount) {
        DirectionsFragment fragment = new DirectionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_directions_list, container, false);

        ImageView closeImageView = view.findViewById(R.id.close_directions);

        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.closeTheDirectionFragment();
            }
        });
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        directionsRecyclerViewAdapter = new DirectionsRecyclerViewAdapter(mRouteInfoList, mListener);
        recyclerView.setAdapter(directionsRecyclerViewAdapter);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mListener.getUpdatedData();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void closeTheDirectionFragment();

        void getUpdatedData();
    }

    public void updateDataInAdapter(DirectionsTransitModel currentDirectionsTransitModel) {
        List<RouteInfo> routeInfoList = new ArrayList<>();
        if (currentDirectionsTransitModel.getTypeOfRoute() == DirectionsTransitModel.RouteType.BIKE) {

            routeInfoList.addAll(currentDirectionsTransitModel.getFirstBicycleRoute().getmListOfRoutes());
            for (int i = 1; i < currentDirectionsTransitModel.getmListOfRoutes().size(); i++) {

                if (currentDirectionsTransitModel.getmListOfRoutes().get(i).transitMode().equalsIgnoreCase(Constants.WALKING)) {
                    routeInfoList.addAll(((WalkingRoute) currentDirectionsTransitModel.getmListOfRoutes().get(i)).getListOfSubRoute());
                } else {
                    routeInfoList.add(currentDirectionsTransitModel.getmListOfRoutes().get(i));
                }
                routeInfoList.add(currentDirectionsTransitModel.getmListOfRoutes().get(i));
            }
        } else if (currentDirectionsTransitModel.getTypeOfRoute() == DirectionsTransitModel.RouteType.WALKING) {
            {
                for (RouteInfo routeInfo :
                        currentDirectionsTransitModel.getmListOfRoutes()) {
                    if (routeInfo.transitMode().equalsIgnoreCase(Constants.WALKING)) {
                        routeInfoList.addAll(((WalkingRoute) routeInfo).getListOfSubRoute());
                    } else {
                        routeInfoList.add(routeInfo);
                    }
                }
            }


        }
        if (directionsRecyclerViewAdapter != null) {
            directionsRecyclerViewAdapter.updateData(routeInfoList);
        }
    }
}
