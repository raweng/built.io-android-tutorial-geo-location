package com.buitio.builtgeolocation;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
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

import com.raweng.built.BuiltError;
import com.raweng.built.BuiltLocation;
import com.raweng.built.BuiltLocationCallback;
import com.raweng.built.BuiltObject;
import com.raweng.built.BuiltResultCallBack;
import com.raweng.built.BuiltUser;
import com.raweng.built.userInterface.BuiltListViewResultCallBack;
import com.raweng.built.userInterface.BuiltUIListViewController;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setTitle("Geo Locations");
		/*
		 * Initialization of BuiltUIListViewController object.
		 */
		builtListView = new BuiltUIListViewController(LocationListActivity.this, "places");
		
		/*
		 * Setting the BuiltUIListViewController layout to activity (Initialization of layout to activity).
		 */
		setContentView(builtListView.getLayout());
		
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
				public void onSuccess(BuiltLocation builtLocation) {
					
					/// onSuccess() callback provides a BuiltLocation object.
					
					Toast.makeText(LocationListActivity.this, "CurrentLocation : "+builtLocation.getLatitude()+" , "+builtLocation.getLongitude(), Toast.LENGTH_SHORT).show();

					currentLocation = new double[]{builtLocation.getLatitude(), builtLocation.getLongitude()};
					
					/*
					 * Updating logged in user with location.
					 */
					update_currentUser(builtLocation);
					
					/*
					 * fetching and listing BuiltObject on basis of near location.
					 */
					fetchPlaces_NearLocation(builtLocation.getLatitude(), builtLocation.getLongitude(), radius);
				}

				@Override
				public void onError(BuiltError builtErrorObject) {
					
					/// builtErrorObject contains more details of error.
					
					Toast.makeText(LocationListActivity.this, "Error : "+builtErrorObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onAlways() {
					/// write code here that user want to execute.
					/// regardless of success or failure of the operation.
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
		
		if(BuiltUser.getSession() != null){
			
			/*
			 * Updating BuiltUser with latest location. 
			 */
			BuiltUser user = BuiltUser.getSession();
			user.setLocation(builtLocation);
			user.updateUserInfo(new HashMap<String, Object>(), new BuiltResultCallBack() {
				
				@Override
				public void onSuccess() {
					
					Toast.makeText(LocationListActivity.this, "Current user updated with current location", Toast.LENGTH_SHORT).show();
					
					///User updated successfully.
					///Saving current updated user to disc.
					try {
						BuiltUser.getCurrentUser().saveSession();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void onError(BuiltError builtErrorObject) {
					
					/// builtErrorObject contains more details of error.
					Toast.makeText(LocationListActivity.this, "Error : "+builtErrorObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onAlways() {
					
					/// write code here that user want to execute.
					/// regardless of success or failure of the operation.
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
				finish();
			}

			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

}
