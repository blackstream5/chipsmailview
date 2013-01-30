package com.cmv.example;

import android.app.Activity;
import android.os.Bundle;
import ui.widgets.ChipsMailAddressesView;

public class MainActivity extends Activity {
    private static final String TAG = "{MainActivity}";

    ChipsMailAddressesView cmaView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cmaView = (ChipsMailAddressesView)findViewById(R.id.chips_mail_addresses_view);
        cmaView.add("jhonie.walker@gmail.com");
        cmaView.add("jason12455@hotmail.com");
        cmaView.add("alice_in_wonderland@japanmail.com");
    }
}
