package com.example.gurcuff.hackathon;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.EngineSpeed;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by Gurcuff on 5.11.2017.
 */

public class SecondFragment extends Fragment {
    private VehicleManager mVehicleManager;
    private TextView mEngineSpeedView;
    private TextView mAcceleratorPedalPosition; //percentage
    private TextView mBrakePedalStatus; //bool
    private TextView mFuelConsumed; //since vehicle start
    private TextView mFuelLevel; //total fuel leveltextView
    private TextView mLat; //latitude
    private TextView mLon; //longitude
    private TextView mOdometer; //odometer
    private TextView mIgnitionStatus;
    private TextView mWheelAngle;
    private TextView mActualGear;
    private TextView mVehicleSpeed;
    private WebView webview;
    private TelephonyManager telephonyManager;
    private String uuid;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private Runnable mTicker;
    private Handler mHandler;
    private String dat;



    private ArrayList<String> engineSpeedArray = new ArrayList<String>();
    private ArrayList<String> pedalPosArray = new ArrayList<String>();
    private ArrayList<String> brakePosArray = new ArrayList<String>();
    private ArrayList<String> fuelConsumedArray = new ArrayList<String>();
    private ArrayList<String> fuelLevelArray = new ArrayList<String>();
    private ArrayList<String> latArray = new ArrayList<String>();
    private ArrayList<String> lonArray = new ArrayList<String>();
    private ArrayList<String> odometerArray = new ArrayList<String>();
    private ArrayList<String> ignitionArray = new ArrayList<String>();
    private ArrayList<String> wheelAngleArray = new ArrayList<String>();
    private ArrayList<String> actualGearArray = new ArrayList<String>();
    private ArrayList<String> vehicleSpeedArray = new ArrayList<String>();
    
    


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        mHandler = new Handler();
        return inflater.inflate(R.layout.second_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Live View");
        mEngineSpeedView = (TextView)  view.findViewById(R.id.engine_speed);
        mFuelConsumed = (TextView)  view.findViewById(R.id.fuelConsumed);
        mFuelLevel = (TextView)  view.findViewById(R.id.fuelLevel);
        mWheelAngle = (TextView)  view.findViewById(R.id.wheelAngle);
        mVehicleSpeed = (TextView)  view.findViewById(R.id.vehicleSpeed);
        mIgnitionStatus = (TextView)  view.findViewById(R.id.ignitionStatus);
        mBrakePedalStatus = (TextView)  view.findViewById(R.id.brakePedalStatus);
        mOdometer = (TextView)  view.findViewById(R.id.odometer);
        mLat = (TextView)  view.findViewById(R.id.lat);
        mLon = (TextView)  view.findViewById(R.id.lon);
        mActualGear = (TextView)  view.findViewById(R.id.gear);
        mAcceleratorPedalPosition = (TextView)  view.findViewById(R.id.accPedalPos);
        webview = (WebView) view.findViewById(R.id.webView1);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://google.com/maps/@");

        Random ran = new Random();
        int top = 10;
        char data = ' ';
        dat = "";

        for (int i=0; i<=top; i++) {
            data = (char)(ran.nextInt(25)+97);
            dat = data + dat;
        }

        telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_PHONE_STATE}, 1002);
        } else {
            return;
        }


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1002:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                    uuid = telephonyManager.getDeviceId();

                }
                break;

            default:
                break;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            //Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.
            mVehicleManager.removeListener(EngineSpeed.class,mSpeedListener);
            mVehicleManager.removeListener(FuelConsumed.class, mFuelConsumedListener);
            mVehicleManager.removeListener(FuelConsumed.class, mFuelLevelListener);
            // mVehicleManager.r
            getActivity().unbindService(mConnection);
            mVehicleManager = null;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if(mVehicleManager == null) {
            Intent intent = new Intent(getActivity(), VehicleManager.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }
    EngineSpeed.Listener mSpeedListener = new EngineSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            
            // When we receive a new EngineSpeed value from the car, we want to
            // update the UI to display the new value. First we cast the generic
            // Measurement back to the type we know it to be, an EngineSpeed.
            final EngineSpeed speed = (EngineSpeed) measurement;
            if((speed != null))
            engineSpeedArray.add(speed.getValue().toString());
            // In order to modify the UI, we have to make sure the code is
            // running on the "UI thread" - Google around for this, it's an
            // important concept in Android.
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    // Finally, we've got a new value and we're running on the
                    // UI thread - we set the text of the EngineSpeed view to
                    // the latest value
                    mEngineSpeedView.setText("Engine speed (RPM): "
                            + speed.getValue().doubleValue());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final FuelConsumed fuelConsumedLevel = (FuelConsumed) measurement;
            if((fuelConsumedLevel != null))
            fuelConsumedArray.add(fuelConsumedLevel.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mFuelConsumed.setText("Fuel Consumed: " + fuelConsumedLevel.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    FuelLevel.Listener mFuelLevelListener = new FuelLevel.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final FuelLevel fuelLevel = (FuelLevel) measurement;
            if((fuelLevel != null))
            fuelLevelArray.add(fuelLevel.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mFuelLevel.setText("Fuel Level (%): " + fuelLevel.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    SteeringWheelAngle.Listener mWheelAngleListener = new SteeringWheelAngle.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final SteeringWheelAngle wheelAngle = (SteeringWheelAngle) measurement;
            if((wheelAngle != null))
            wheelAngleArray.add(wheelAngle.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mWheelAngle.setText("Wheel Angle: " + wheelAngle.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    VehicleSpeed.Listener mVehicleSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed vehicleSpeed = (VehicleSpeed) measurement;
            if((vehicleSpeed != null))
            vehicleSpeedArray.add(vehicleSpeed.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mVehicleSpeed.setText("Vehicle Speed:" + vehicleSpeed.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    IgnitionStatus.Listener mIgnitionStatusListener = new IgnitionStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final IgnitionStatus ignitionStatus = (IgnitionStatus) measurement;
            if((ignitionStatus != null))
            ignitionArray.add(ignitionStatus.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mIgnitionStatus.setText("Ignition Status: " + ignitionStatus.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    BrakePedalStatus.Listener mBrakePedalStatusListener = new BrakePedalStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final BrakePedalStatus brakePedalStatus = (BrakePedalStatus) measurement;
            if((brakePedalStatus != null))
            brakePosArray.add(brakePedalStatus.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mBrakePedalStatus.setText("Brake Pedal Status: " + brakePedalStatus.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    Odometer.Listener mOdometerListener = new Odometer.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Odometer odometer = (Odometer) measurement;
            if((odometer != null))
            odometerArray.add(odometer.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mOdometer.setText("Trip:\n" + odometer.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }

    };

    Latitude.Listener mLatListener = new Latitude.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Latitude latitude = (Latitude) measurement;
            if((latitude != null))
            latArray.add(latitude.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mLat.setText("Lat:\n" + latitude.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    Longitude.Listener mLonListener = new Longitude.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Longitude longitude = (Longitude) measurement;
            if((longitude != null))
            lonArray.add(longitude.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mLon.setText("Lon:\n" + longitude.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    TransmissionGearPosition.Listener mGearListener = new TransmissionGearPosition.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final TransmissionGearPosition gear = (TransmissionGearPosition) measurement;
            if((gear != null))
            actualGearArray.add(gear.getValue().toString());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mActualGear.setText("Gear:\n" + gear.getValue().toString());
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("message");

                }
            });
        }
    };

    AcceleratorPedalPosition.Listener mAccelListener = new AcceleratorPedalPosition.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final AcceleratorPedalPosition pedalPosition = (AcceleratorPedalPosition) measurement;
            if((pedalPosition != null))
            pedalPosArray.add(pedalPosition.getValue().toString());
            if(pedalPosArray.size() > 200){
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("RPM: " + engineSpeedArray.get(engineSpeedArray.size() - 1));
                engineSpeedArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("PedalPos: " + pedalPosArray.get(pedalPosArray.size() - 1));
                pedalPosArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("BrakeStatus: " + brakePosArray.get(brakePosArray.size() - 1));
                brakePosArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("FuelConsumed: " + fuelConsumedArray.get(fuelConsumedArray.size() - 1));
                fuelConsumedArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("FuelLevel: " + fuelLevelArray.get(fuelLevelArray.size() - 1));
                fuelLevelArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("Lat: " + latArray.get(latArray.size() - 1));
                latArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("Lon: " + lonArray.get(lonArray.size() - 1));
                lonArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("Odometer: " + odometerArray.get(odometerArray.size() - 1));
                odometerArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("IgnitionStatus: " + ignitionArray.get(ignitionArray.size() - 1));
                ignitionArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("WheelAngle: " + wheelAngleArray.get(wheelAngleArray.size() - 1));
                wheelAngleArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("Gear: " + actualGearArray.get(actualGearArray.size() - 1));
                actualGearArray.clear();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("message");
                myRef.child(dat).push().setValue("SPeed: " + vehicleSpeedArray.get(vehicleSpeedArray.size() - 1));
                vehicleSpeedArray.clear();



            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mAcceleratorPedalPosition.setText("AccelPos: " + pedalPosition.getValue().toString());
                   // myRef.child(dat).push().setValue(pedalPosition.getValue().toString());
                }
            });
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            mVehicleManager.addListener(EngineSpeed.class, mSpeedListener);
            mVehicleManager.addListener(FuelLevel.class, mFuelLevelListener);
            mVehicleManager.addListener(FuelConsumed.class, mFuelConsumedListener);
            mVehicleManager.addListener(SteeringWheelAngle.class, mWheelAngleListener);
            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.addListener(IgnitionStatus.class, mIgnitionStatusListener);
            mVehicleManager.addListener(BrakePedalStatus.class, mBrakePedalStatusListener);
            mVehicleManager.addListener(Odometer.class, mOdometerListener);
            mVehicleManager.addListener(Latitude.class, mLatListener);
            mVehicleManager.addListener(Longitude.class, mLonListener);
            mVehicleManager.addListener(TransmissionGearPosition.class, mGearListener);
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAccelListener);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };


}



