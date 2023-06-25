package com.linhphuong.englishconsonant;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadmeActivity extends ActionBarActivity {
    /** StartAppAd object declaration */
    //private StartAppAd startAppAd = new StartAppAd(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //StartAppSDK.init(this, "200153260", "100717203", false);

        setContentView(R.layout.activity_readme);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")
                .build();
        mAdView.loadAd(adRequest);

        String data;

        EditText editdata= (EditText) findViewById(R.id.editdata);



        InputStream in= getResources().openRawResource(+ R.drawable.readme);
        InputStreamReader inreader=new InputStreamReader(in);
        BufferedReader bufreader=new BufferedReader(inreader);
        StringBuilder builder=new StringBuilder();
        if(in!=null)
        {
            try
            {
                while((data=bufreader.readLine())!=null)
                {
                    builder.append(data);
                    builder.append("\n");
                }
                in.close();
                editdata.setText(builder.toString());
            }
            catch(IOException ex){
                //Log.e("ERROR", ex.getMessage());
            }
        }

    }

    @Override
    public void onBackPressed() {
        //startAppAd.onBackPressed();
        super.onBackPressed();
        finish();

    }



}
