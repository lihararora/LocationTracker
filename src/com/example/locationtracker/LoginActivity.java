package com.example.locationtracker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_login);
	    
	    final SharedPreferences sp = this.getSharedPreferences("com.example.locationtracker", Context.MODE_PRIVATE);
	    final String tokenKey = "com.example.locationtracker.token";
	    String token = sp.getString(tokenKey, null);
	    
	    if(token == null)
	    {
	    	LinearLayout loginProgress = (LinearLayout) findViewById(R.id.LoginProgress);
	    	loginProgress.setVisibility(LinearLayout.GONE);
	    	
	    	LinearLayout loginForm = (LinearLayout) findViewById(R.id.LoginForm);
	    	loginForm.setVisibility(LinearLayout.VISIBLE);
	    	
		    final Button button = (Button) findViewById(R.id.LogIn);
	        button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	String username = ((EditText)findViewById(R.id.Username)).getText().toString();
	            	String password = ((EditText)findViewById(R.id.Password)).getText().toString();
	            	String data = "username="+username+"&password="+password;
	           
	        		HttpConnection con = null;
	        		String token = null;
	        		try {
						con = new HttpConnection(Utils.API_URL+"get-auth-token/", HttpConnection.POST_REQUEST);
						token = con.POST(data, null, true);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		con.close();
	        		try {
						JSONObject jsonObj = new JSONObject(token);
						token = jsonObj.getString("token");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						token = null;
					}
	        		if(token != null)
	        		{
	        			sp.edit().putString(tokenKey, token).commit();
	        			Intent intent = new Intent (getApplicationContext(), MainActivity.class);
	        		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        		    startActivity(intent);
	        		    finish(); // Call once you redirect to another activity
	        		}
	        		else {
	        			Context context = getApplicationContext();
	        			CharSequence text = "Login Failed!";
	        			int duration = Toast.LENGTH_SHORT;

	        			Toast toast = Toast.makeText(context, text, duration);
	        			toast.show();
	        		}
	        		
	        		//String token = con.POST(params);
	        		
	        		/*String token = "";
	        		
	        		if (httpResponse != null) {
	                  try {
	                      JSONObject jsonObj = new JSONObject(httpResponse);
	                       
	                      // Getting JSON Array node
	                      token = jsonObj.getString("token");
	
	                  } catch (JSONException e) {
	                      e.printStackTrace();
	                  }
	              } else {
	                  Log.e("ServiceHandler", "Couldn't get any data from the url");
	              }*/
	            	
	            }
	        });
	    }
	    else
	    {
	    	Intent intent = new Intent (getApplicationContext(), MainActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intent);
		    finish(); // Call once you redirect to another activity
	    }
	}

}
