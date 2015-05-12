package com.buitio.builtgeolocation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.raweng.built.Built;
import com.raweng.built.BuiltApplication;
import com.raweng.built.BuiltError;
import com.raweng.built.BuiltLocation;
import com.raweng.built.BuiltLocationCallback;
import com.raweng.built.BuiltObject;
import com.raweng.built.BuiltResultCallBack;
import com.raweng.built.BuiltUser;
import com.raweng.built.userInterface.BuiltListViewResultCallBack;
import com.raweng.built.userInterface.BuiltUIListViewController;
import com.raweng.built.utilities.BuiltConstant;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is built.io android tutorial.
 *
 * Short introduction of some classes with some methods.
 * Contain classes: 
 * 1. BuiltUILoginController 
 * 2. BuiltUISignUpController 
 * 3. BuiltUIListViewController 
 * 4. BuiltLocation
 * 5. BuiltObject
 * 6. BuiltQuery
 *
 * For quick start with built.io refer "http://docs.built.io/quickstart/index.html#android"
 *
 * @author raw engineering, Inc
 *
 */
public class LocationListActivity extends Activity{

    int radius = 600;
    double[] currentLocation;
    ArrayList<BuiltObject> builtObjectList;

    /*
     * Declaration of BuiltUIListViewController.
     */
    BuiltUIListViewController builtListView;
    private BuiltApplication builtApplication;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Geo Locations");
		/*
		 * Initialization of BuiltUIListViewController object and BuiltApplication.
		 */
        builtListView = new BuiltUIListViewController(LocationListActivity.this, "blt9f2f3c1d77c907e0","places");

        try {
            builtApplication = Built.application(LocationListActivity.this , "blt9f2f3c1d77c907e0");
        } catch (Exception e) {
            e.printStackTrace();
        }
		/*
		 * Setting the BuiltUIListViewController layout to activity (Initialization of layout to activity).
		 */
        setContentView(builtListView.getLayout());

        initProgressBar();
        builtListView.setProgressDialog(progressDialog);

        Intent intent = getIntent();
        if(intent.hasExtra("radius")){
            int[] selectedRadius = intent.getIntArrayExtra("radius");
            radius = selectedRadius[0];
        }

        try {
			
			/*
			 * getting current location using BuiltLocation class.
			 */
            BuiltLocation.getCurrentLocation(LocationListActivity.this, LocationListActivity.this, new BuiltLocationCallback() {

                @Override
                public void onCompletion(BuiltConstant.ResponseType responseType, BuiltLocation builtLocation, BuiltError builtError) {

                    if (builtError == null){
                        /// onSuccess() callback provides a BuiltLocation object.

                        showToast("CurrentLocation : " + builtLocation.getLatitude() + " , " + builtLocation.getLongitude());

                        currentLocation = new double[]{builtLocation.getLatitude(), builtLocation.getLongitude()};

					/*
					 * Updating logged in user with location.
					 */
                        update_currentUser(builtLocation);

					/*
					 * fetching and listing BuiltObject on basis of near location.
					 */
                        fetchPlaces_NearLocation(builtLocation.getLatitude(), builtLocation.getLongitude(), radius);

                    }else {
                        showToast("Error : "+builtError.getErrorMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        builtListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BuiltObject builtObject = builtListView.getDataSourceObject().getItem(position-1);

                BuiltLocation locationObject = builtObject.getLocation();

                if(locationObject != null){

                    Intent geoLocationIntent = new Intent(LocationListActivity.this, GeoLocationActivity.class);
                    geoLocationIntent.putExtra("loc", new double[]{locationObject.getLatitude(),locationObject.getLongitude()});
                    geoLocationIntent.putExtra("uid", builtObject.getUid());
                    geoLocationIntent.putExtra("place_name", builtObject.getString("place_name"));
                    startActivity(geoLocationIntent);
                }

            }
        });
    }

    private void initProgressBar() {
        progressDialog = new ProgressDialog(LocationListActivity.this);
        progressDialog.setMessage("Loading Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
    }


    /**
     *
     * Fetching and listing BuiltObject on basis of near location.
     *
     */
    public void fetchPlaces_NearLocation(double latitude, double longitude, int radius){

		/*
		 * Setting BuiltLocation with latitude and longitude.
		 */
        BuiltLocation locationObject = new BuiltLocation();
        locationObject.setLocation(latitude, longitude);
		
		/*
		 * Adding nearLocation filter to BuiltUIListViewController for fetching data.
		 */
        builtListView.getBuiltQueryInstance().nearLocation(locationObject, radius);
		
		/*
		 * Calling load data 
		 */
        builtListView.loadData(new BuiltListViewResultCallBack() {

            @Override
            public void onError(BuiltError error) {

                /// builtErrorObject contains more details of error.

                Toast.makeText(LocationListActivity.this, "Error : "+error.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAlways() {
                /// write code here that user want to execute.
                /// regardless of success or failure of the operation.
            }

            @Override
            public int getViewTypeCount() {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent, BuiltObject builtObject) {

                ///Inflating the row layout in list view.

                LayoutInflater inflater     = LayoutInflater.from(LocationListActivity.this);
                convertView                 = inflater.inflate(R.layout.row_list_layout, parent, false);
                TextView updateTextView     = (TextView) convertView.findViewById(R.id.updatedAt);
                TextView latlongTextView	= (TextView)convertView.findViewById(R.id.location);

				
				/*
				 * Extracting the data from builtObject instance.
				 */
                BuiltLocation locationObject = builtObject.getLocation();

                updateTextView.setText(builtObject.getString("place_name")+"");
                latlongTextView.setText("Created At : "+builtObject.getUpdateAt().getTime()+"\nLocation :-"+locationObject.getLatitude()+","+locationObject.getLongitude());

                return convertView;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }
        });

    }

    /**
     * Updating logged in user with location.
     */
    private void update_currentUser(BuiltLocation builtLocation) {

        BuiltUser user = builtApplication.getCurrentUser();
        if(user != null){
			
			/*
			 * Updating BuiltUser with latest location. 
			 */
            //BuiltUser user = BuiltUser.getSession();
            user.setLocation(builtLocation);
            user.updateUserInfoInBackground(new BuiltResultCallBack() {

                @Override
                public void onCompletion(BuiltConstant.ResponseType responseType, BuiltError builtError) {

                    if (builtError == null) {
                        showToast("Current user updated with current location");

                    } else {
                        showToast("Error : " + builtError.getErrorMessage());
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.geo_location, menu);
        MenuItem item = menu.findItem(R.id.update);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newAdd: {

                if(currentLocation != null){
                    Intent geoLocationIntent = new Intent(LocationListActivity.this, GeoLocationActivity.class);
                    geoLocationIntent.putExtra("newloc", currentLocation);
                    startActivity(geoLocationIntent);
                }

                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showToast(String msg){
        Toast.makeText(this , msg , Toast.LENGTH_SHORT).show();
    }


}
