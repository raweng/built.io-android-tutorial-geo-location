package com.buitio.builtgeolocation;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.raweng.built.BuiltError;
import com.raweng.built.BuiltLocation;
import com.raweng.built.BuiltObject;
import com.raweng.built.BuiltQuery;
import com.raweng.built.BuiltResultCallBack;
import com.raweng.built.QueryResult;
import com.raweng.built.QueryResultsCallBack;

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
public class GeoLocationActivity extends Activity{

	GoogleMap map;
	SeekBar seekBar;

	public LatLng selectedLocation;
	String uid;
	Circle circle;
	public int selectedRadius = 1000;
	boolean isRadiusChanged;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Search Nearby Places");
		
		setContentView(R.layout.mapfragment_layout);
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		seekBar = (SeekBar) findViewById(R.id.seekBar);

		Intent intent = getIntent();

		if(intent.hasExtra("loc")){

			seekBar.setVisibility(View.GONE);
			isRadiusChanged = false; 
			((LinearLayout)findViewById(R.id.radiusSelecter)).setVisibility(View.GONE);

			double[] loc = intent.getDoubleArrayExtra("loc");
			uid = intent.getStringExtra("uid");
			addMapRadius(intent.getStringExtra("place_name"), loc);

		}else if(intent.hasExtra("newloc")){

			seekBar.setVisibility(View.VISIBLE);
			isRadiusChanged = true; 

			double[] loc = intent.getDoubleArrayExtra("newloc");
			addMapRadius("current :", loc);

			/*
			 * fetching near by location through querying.
			 */
			fetch_NearByPlaces(selectedRadius);
		};

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				circle.setRadius(progress*1000);
				selectedRadius = progress*1000;

				/*
				 * fetching near by location through querying.
				 */
				fetch_NearByPlaces(progress*1000);

				map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.geo_location, menu);
		
		MenuItem item = menu.findItem(R.id.newAdd);
		item.setVisible(false);

		MenuItem item1 = menu.findItem(R.id.update);
		if(!isRadiusChanged){
			item1.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.update: {
			/*
			 * Creating & Updating BuiltObject.
			 */
			saveBuiltObject();
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Creating & Updating BuiltObject.
	 */
	public void saveBuiltObject(){

		BuiltObject object = new BuiltObject("places");
		BuiltLocation location = new BuiltLocation();

		if(uid != null && selectedLocation != null){

			/*
			 * Updating the BuiltObject by setting object uid.
			 */
			object.setUid(uid);
			location.setLocation(selectedLocation.latitude, selectedLocation.longitude);
			object.setLocation(location);
			object.save(new BuiltResultCallBack() {

				@Override
				public void onSuccess() {

					/// object is updated successfully.

					Toast.makeText(GeoLocationActivity.this, "Refresh list...for latest", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onError(BuiltError builtErrorObject) {

					/// builtErrorObject contains more details of error.

					Toast.makeText(GeoLocationActivity.this, "Error : "+builtErrorObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onAlways() {

					/// write code here that user want to execute.
					/// regardless of success or failure of the operation.

					finish();
				}
			});

		}else if(uid == null && selectedLocation != null){

			/*
			 * Creating the BuiltObject.
			 */
			location.setLocation(selectedLocation.latitude, selectedLocation.longitude);
			object.set("place_name", ((EditText)findViewById(R.id.placeName)).getText().toString());
			object.set("place_name", ((EditText)findViewById(R.id.rating)).getText().toString());
			object.setLocation(location);
			object.save(new BuiltResultCallBack() {

				@Override
				public void onSuccess() {

					/// object is created successfully

					Toast.makeText(GeoLocationActivity.this, "Refresh list...for latest", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onError(BuiltError builtErrorObject) {

					/// builtErrorObject contains more details of error.

					Toast.makeText(GeoLocationActivity.this, "Error : "+builtErrorObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onAlways() {

					/// write code here that user want to execute.
					/// regardless of success or failure of the operation.

					finish();
				}
			});
		}
	}

	/**
	 * Fetching near by location through querying.
	 * 
	 */
	public void fetch_NearByPlaces(int radius){

		/*
		 * Creating queryObject by class uid.
		 */
		BuiltQuery query = new BuiltQuery("places");

		/*
		 * Creating BuiltLocation object with latitude and longitude.
		 */
		BuiltLocation location = new BuiltLocation();
		location.setLocation(selectedLocation.latitude, selectedLocation.longitude);

		/*
		 * adding location object with nearLocation filter.
		 */
		query.nearLocation(location, radius);

		/*
		 * Executing the query to fetch BuiltObject near location.
		 */
		query.exec(new QueryResultsCallBack() {

			@Override
			public void onSuccess(QueryResult queryResult) {

				/// query is executed successfully.

				ArrayList<BuiltObject> list = (ArrayList<BuiltObject>) queryResult.getResultObjects();

				for (int i = 0; i < list.size(); i++) {
					BuiltLocation loc = list.get(i).getLocation();
					LatLng near = new LatLng(loc.getLatitude(), loc.getLongitude());
					Marker nearplace = map.addMarker(new MarkerOptions().position(near).title(list.get(i).getString("place_name")));
					nearplace.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				}
			}

			@Override
			public void onError(BuiltError builtErrorObject) {

				/// builtErrorObject contains more details of error.

				Toast.makeText(GeoLocationActivity.this, "Error : "+builtErrorObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onAlways() {
				/// write code here that user want to execute.
				/// regardless of success or failure of the operation.
			}
		});
	}

	public void addMapRadius(String currentPlace, double[] loc){

		selectedLocation = new LatLng(loc[0], loc[1]);
		map.addMarker(new MarkerOptions().position(selectedLocation).title(currentPlace +"\n loc : "+loc[0]+" , "+ loc[1]));

		circle = map.addCircle(new CircleOptions()
		.center(new LatLng(loc[0], loc[1]))
		.radius(selectedRadius)
		.strokeColor(Color.RED)
		.fillColor(Color.parseColor("#E6A0A0")));

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 25));
		map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		if(isRadiusChanged){
			Intent locationIntent = new Intent(GeoLocationActivity.this, LocationListActivity.class);
			locationIntent.putExtra("radius", new int[]{selectedRadius});
			startActivity(locationIntent);
			finish();
		}
	}

}
