package com.example.miniui1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by erik on 2015-02-22.
 * This class is used to map data to resource via the pattern:
 * https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 *
 */
public class ProjectArrayAdapter extends ArrayAdapter<Project> {
        public ProjectArrayAdapter(Context context, ArrayList<Project> projects) {
            super(context, 0, projects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Project project = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).
                        inflate(R.layout.project_row_layout, parent, false);
            }
            // Lookup view for data population
            TextView tvProjectName = (TextView) convertView.findViewById(R.id.pName);
            TextView tvCreatorName = (TextView) convertView.findViewById(R.id.creatorName);
            TextView tvCreationTime = (TextView) convertView.findViewById(R.id.creationTime);
            TextView tvStatus = (TextView) convertView.findViewById(R.id.projectStatus);
            TextView numObservations = (TextView) convertView.findViewById(R.id.numObservations);

            // Populate the data into the template view using the data object
            tvProjectName.setText(project.name);
            tvCreatorName.setText(project.operator);
            tvCreationTime.setText(project.start_time.toString());
            tvStatus.setText(project.status);
            try {
                numObservations.setText( project.observations.size() );
            } catch (Exception e) {
                numObservations.setText("0");
            }

            // Return the completed view to render on screen
            return convertView;
        }
}
