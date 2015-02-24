package com.example.miniui1;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;

/**
 * Created by erik on 2015-02-22.
 * This class is used to map data to resource via the pattern:
 * https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 *
 */
public class ProjectArrayAdapter extends ArrayAdapter<Project> {
        private final String CLASSTAG = "PROJECT_ARRAY_ADAPTER";

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
            TextView tvCreatorName = (TextView) convertView.findViewById(R.id.pCreatorName);
            TextView tvCreationTime = (TextView) convertView.findViewById(R.id.pCreationTime);
            TextView tvStatus = (TextView) convertView.findViewById(R.id.pStatus);
            TextView numObservations = (TextView) convertView.findViewById(R.id.pNumObservations);

            // Populate the data into the template view using the data object
            tvProjectName.setText(project.name);
            tvCreatorName.setText(project.operator);

            tvCreationTime.setText(DateFormat.
                    getDateTimeInstance(DateFormat.SHORT,
                                        DateFormat.SHORT).format(project.start_time));
            tvStatus.setText(project.status);
            if (project.status.equals("open")) {
                tvStatus.setTextColor(Color.RED);
            } else {
                tvStatus.setTextColor(Color.BLUE);
            }
            try {
                int nObs = ((ArrayList<Observation>)project.observations).size();
                numObservations.setText( String.valueOf(nObs) );
            } catch (Exception e) {
                Log.d(CLASSTAG, "Project contains nu observations");
                numObservations.setText("0");
            }

            // Return the completed view to render on screen
            return convertView;
        }
}
