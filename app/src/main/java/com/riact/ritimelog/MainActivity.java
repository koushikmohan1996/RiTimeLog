package com.riact.ritimelog;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hyttetech.library.utils.AppSingleton;
import com.riact.ritimelog.models.SiteModel;
import com.riact.ritimelog.utils.Constants;
import com.riact.ritimelog.utils.DbHandler;
import com.riact.ritimelog.utils.GPSTracker;
import com.riact.ritimelog.utils.ModelUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;

public class MainActivity extends AppCompatActivity {
    String resp;

    EditText siteCode,pinCode,OECode,OEPassword;
    Button submitBtn ;
    String siteCodeTxt,OECodeTxt,OEPasswordTxt,pinCodeTxt;
    DbHandler db;
    GPSTracker gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        siteCode = (EditText)findViewById(R.id.site_code);
        pinCode = (EditText)findViewById(R.id.pin_code);
        OECode =  (EditText)findViewById(R.id.oe_code);
        OEPassword =  (EditText)findViewById(R.id.password);
        submitBtn = (Button)findViewById(R.id.submit);

        OECode.setFilters(new InputFilter[]{new InputFilter.AllCaps()});


        db = new DbHandler(getApplicationContext());
        List item = db.getCurrentSite();
        if(!item.isEmpty())
        {
            Constants.currentSite=ModelUtil.getSite(item.get(0).toString());
            volleyStringRequst(Constants.WEB_ADDRESS+"get_site_emp.php?site_code="+item.get(0).toString(),item.get(0).toString());
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("Hiii");

                siteCodeTxt = siteCode.getText().toString();
                pinCodeTxt = pinCode.getText().toString();
                OECodeTxt = OECode.getText().toString();
                OEPasswordTxt = OEPassword.getText().toString();
                long pincodeLng = 0 ;
                if(!"".equals(pinCodeTxt))
                    pincodeLng = new Long(pinCodeTxt);
                if("".equals(siteCodeTxt)&&("".equals(pinCodeTxt)))
                {
                    Toast.makeText(getApplicationContext(),"Enter OE Code or PIN code",Toast.LENGTH_LONG).show();
                }
                else {
                    if("".equals(OECodeTxt)||("".equals(OEPasswordTxt)))
                    {
                        Toast.makeText(getApplicationContext(),"OE code and Password cannot be empty",Toast.LENGTH_LONG).show();
                    }
                    else {

                        if(ModelUtil.isValidSite(siteCodeTxt,pincodeLng,OECodeTxt,OEPasswordTxt))
                        {
                            gps = new GPSTracker(MainActivity.this);
                            if (gps.canGetLocation()) {
                                SiteModel model = ModelUtil.getSite(siteCodeTxt);
                                double deviceLatitude = gps.getLatitude();
                                double deviceLongitude = gps.getLongitude();
                                double siteLatitude = model.getSiteLatitude();
                                double siteLongitude = model.getSiteLongitude();
                                double tollerance = model.getTollerance();
                                AttendanceRegister register = new AttendanceRegister();

                                if (register.distance(siteLatitude, siteLongitude, deviceLatitude, deviceLongitude) < tollerance ) {
                                    volleyStringRequst(Constants.WEB_ADDRESS+"get_site_emp.php?site_code="+siteCodeTxt,siteCodeTxt);
                                }
                                else {
                                    Toast.makeText(getApplicationContext(),"Invalid Location",Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                gps.showSettingsAlert();
                            }

                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Invalid Credentials",Toast.LENGTH_LONG).show();

                        }

                    }
                }
            }
        });

    }

    public void  volleyStringRequst(String url, final String siteCodeTxt){
        String  REQUEST_TAG = "com.androidtutorialpoint.volleyStringRequest";
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();


        StringRequest strReq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    Constants.employeeList=ModelUtil.getEmployeeList(new JSONArray(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                db.addEmployeeDetails(siteCodeTxt,response);
                db.deleteCurrentSite();
                db.addCurrentSite(siteCodeTxt);
                Constants.currentSite=ModelUtil.getSite(db.getCurrentSite().get(0).toString());
                updateAttenanceDetails();
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent);
                finish();
                progressDialog.hide();


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                resp="Failed to Login";
                progressDialog.hide();
                List<String> employeeData=db.getEmployeeDtails(siteCodeTxt);
                if(!employeeData.isEmpty())
                {
                    try {
                        Constants.employeeList=ModelUtil.getEmployeeList(new JSONArray(employeeData.get(0)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updateAttenanceDetails();
                    Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), resp, Toast.LENGTH_LONG).show();
                }


            }
        });
        // Adding String request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, REQUEST_TAG);

    }

    public void updateAttenanceDetails()
    {
        String startDateTxt,endateTxt;
        DateFormat currentDate =  new SimpleDateFormat("yyyy/MM/dd");
        Date startDate = new Date();
        Date endDate = new Date();
        startDateTxt = currentDate.format(startDate)+" 00:00:00";
        endateTxt = currentDate.format(endDate)+ " 23:59:59";
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try{

            startDate = dateFormat.parse(startDateTxt);
            endDate = dateFormat.parse(endateTxt);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        List<List> attendanceList=db.getSyncedEmpDetails(startDate.getTime(),endDate.getTime());
        ModelUtil.setEmployeeListFromEmpCode(attendanceList.get(0));

    }


}
