package edu.unm.albuquerquebus.live.adapters;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.unm.albuquerquebus.live.R;
import edu.unm.albuquerquebus.live.fragments.DirectionsFragment.OnListFragmentInteractionListener;
import edu.unm.albuquerquebus.live.interfaces.RouteInfo;
import edu.unm.albuquerquebus.live.model.Route;

/**
 * {@link RecyclerView.Adapter} that can display a {@link RouteInfo} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class DirectionsRecyclerViewAdapter extends RecyclerView.Adapter<DirectionsRecyclerViewAdapter.ViewHolder> {

    private final List<RouteInfo> mRouteInfoList;
    private final OnListFragmentInteractionListener mListener;

    public DirectionsRecyclerViewAdapter(List<RouteInfo> items, OnListFragmentInteractionListener listener) {
        mRouteInfoList = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_directions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mRouteInfo = mRouteInfoList.get(position);
        Route route = (Route) mRouteInfoList.get(position);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.mDirectionTextView.setText(Html.fromHtml(route.getHtmlInstructions(), Html.FROM_HTML_MODE_COMPACT));

        } else {
            holder.mDirectionTextView.setText(Html.fromHtml(route.getHtmlInstructions()));
        }

        if (route.transitMode().equalsIgnoreCase("TRANSIT")) {
            holder.mDirectionTypeImageView.setImageResource(R.drawable.ic_action_directions_bus);
        } else if (route.transitMode().equalsIgnoreCase("WALKING")) {
            holder.mDirectionTypeImageView.setImageResource(R.drawable.ic_action_directions_walk);
        } else {
            holder.mDirectionTypeImageView.setImageResource(R.drawable.ic_action_directions_bike);
        }

    }

    @Override
    public int getItemCount() {
        return mRouteInfoList.size();

    }

    public void updateData(List<RouteInfo> routeInfoList) {
        mRouteInfoList.clear();
        mRouteInfoList.addAll(routeInfoList);
        this.notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mDirectionTextView;
        final ImageView mDirectionTypeImageView;
        RouteInfo mRouteInfo;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mDirectionTextView = (TextView) view.findViewById(R.id.direction_text);
            mDirectionTypeImageView = (ImageView) view.findViewById(R.id.directions_type_image);
        }


    }
}
