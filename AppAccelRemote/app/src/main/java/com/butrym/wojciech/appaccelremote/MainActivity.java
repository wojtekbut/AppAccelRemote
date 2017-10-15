package com.butrym.wojciech.appaccelremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Page1.communicateToActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    TextView xval;
    TextView yval;
    TextView macadres, polmainval, polardval;
    ToggleButton onoff;
    int count;
    ListView listaview;
    ArrayList listaArray;
    ArrayAdapter<String> arrayAdapter;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice device;
    String adresMac;
    ConnectThread polaczenie;
    boolean on;
    String text, nazwa;
    Context context;
    Page1 pg1;
    Page2 pg2;
    Page3 pg3;
    boolean polmainena, rozmainena;
    boolean polardena, rozardena;
    boolean kalibena, reskalibena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        count = 2;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        xval = (TextView) findViewById(R.id.xval);
        yval = (TextView) findViewById(R.id.yval);
        macadres = (TextView) findViewById(R.id.conmaintext);
        polmainval = (TextView) findViewById(R.id.polmainval);
        polardval = (TextView) findViewById(R.id.polardval);
        listaview = (ListView) findViewById(R.id.lista);
        listaview.setVisibility(View.INVISIBLE);
        onoff = (ToggleButton) findViewById(R.id.onoff);
        on = false;
        polardena = false;
        rozardena = false;
        polmainena = true;
        rozmainena = false;
        kalibena = false;
        reskalibena = false;
        device = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }
        onoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onoff.isChecked()) {
                    String roz = "run:";
                    String msg = "on";
                    wyslijDoMain(roz, msg);
                    Log.d("on check change", "wysyłam on");
                } else {
                    String roz = "run:";
                    String msg = "off";
                    wyslijDoMain(roz, msg);
                    Log.d("on check change", "wysyłam off");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (polaczenie != null) {
            polaczenie.cancel();
        }
    }


    @Override
    public void onBackPressed() {
        if (listaview.getVisibility() == View.VISIBLE) {
            listaview.setVisibility(View.INVISIBLE);
            findViewById(R.id.container).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.exit) {
            finish();
        }
        if (id == R.id.Page3) {
            count = 3;
            mSectionsPagerAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(3);
            count = 2;
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void sendMessage(String... msg) {
        if (msg.length == 1) {
            switch (msg[0]) {
                case "Łączę z Main":
                    Intent devlisti = new Intent(getApplicationContext(), ListaUrzadzen.class);
                    startActivityForResult(devlisti, 1);
                    break;
                case "cancel":
                    polaczenie.cancel();
            }
        } else if (msg.length == 2) {
            String rozkaz = msg[0];
            String wiadomosc = msg[1];
            if (rozkaz.endsWith(":")) {
                wyslijDoMain(rozkaz, wiadomosc);
            }
        }
    }

    @Override
    public boolean isValue(String str) {
        Field coto = null;
        try {
            try {
                coto = this.getClass().getField(str);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            if (coto.getType().isAssignableFrom(ConnectThread.class)) {
                return coto.get(this) != null;
            } else if (coto.getType().isAssignableFrom(boolean.class)) {
                return (boolean) coto.get(this);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            adresMac = data.getStringExtra("adres");
            nazwa = data.getStringExtra(("nazwa"));
            device = mBluetoothAdapter.getRemoteDevice(adresMac);
            polaczenie = new ConnectThread(device, mHandler);
            polaczenie.start();
            polmainval.setText(String.format("Łączę z %1$s", nazwa.substring(0, nazwa.indexOf("\n"))));
        } else {
            polmainval.setText("Nie wybrano adresu.");
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    pg1 = new Page1();
                    return pg1;
                case 1:
                    pg2 = new Page2();
                    return pg2;
                case 2:
                    pg3 = new Page3();
                    return pg3;
            }
            return null;
        }

        @Override
        public int getCount() {
            return count;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Połączenia";
                case 1:
                    return "Sterowanie";
                case 2:
                    return "Kalibracja";
            }
            return null;
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            text = "";
            if (msg.what != 5 && msg.what != 4) {
                text = msg.obj.toString();
            }
            if (text.length() == 5 && text.equals("- - -")) {
                if (onoff.isChecked()) {
                    onoff.setChecked(false);
                }
            } else if ((msg.what == 1 || msg.what == 2) && !text.equals("- - -") && !onoff.isChecked()) {
                on = true;
                onoff.setChecked(true);
            }
            switch (msg.what) {
                case 1:
                    xval.setText(text);
                    break;
                case 2:
                    yval.setText(text);
                    break;
                case 3:
                    polmainval.setText(text);
                    polardval.setText(text);
                    polaczenie.cancel();
                    polaczenie = null;
                    onoff.setChecked(false);
                    onoff.setEnabled(false);
                    xval.setText("- - -");
                    yval.setText("- - -");
                    if (pg1 != null) {
                        pg1.polzmain.setEnabled(true);
                        pg1.rozmain.setEnabled(false);
                        pg1.polzard.setEnabled(false);
                        pg1.rozard.setEnabled(false);
                    }
                    if (pg3 != null) {
                        pg3.kalibracja.setEnabled(false);
                        pg3.resetkalibracji.setEnabled(false);
                    }
                    polmainena = true;
                    rozmainena = false;
                    polardena = false;
                    rozardena = false;
                    kalibena = false;
                    reskalibena = false;
                    if (pg2 != null) {
                        pg2.allButtons((ViewGroup) pg2.getView(),"disable" );
                    }
                    break;
                case 4:
                    onoff.setEnabled(true);
                    onoff.setChecked(false);
                    polmainval.setText("Połączony z " + nazwa.substring(0, nazwa.indexOf("\n")));
                    if (pg1 != null) {
                        pg1.polzmain.setEnabled(false);
                        pg1.rozmain.setEnabled(true);
                        if (polardval.getText().equals("nie połączono")) {
                            pg1.polzard.setEnabled(true);
                            pg1.rozard.setEnabled(false);
                            polardena = true;
                            rozardena = false;
                        }
                    }
                    polmainena = false;
                    rozmainena = true;
                    break;
                case 5:
                    listaArray = (ArrayList) msg.obj;
                    arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.lista, listaArray);
                    listaview.setAdapter(arrayAdapter);
                    listaview.setVisibility(View.VISIBLE);
                    listaview.bringToFront();
                    findViewById(R.id.container).setVisibility(View.INVISIBLE);
                    listaview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String info = ((TextView) view).getText().toString();
                            String address = info.substring(0, 17);
                            String name = info.substring(17);
                            listaview.setVisibility(View.INVISIBLE);
                            findViewById(R.id.container).setVisibility(View.VISIBLE);
                            wyslijDoMain("polArd", address);
                            polardval.setText("łączę z " + name);
                        }
                    });
                    break;
                case 6:
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                    break;
                case 7:
                    if (text.equals("rozlaczony")) {
                        polardval.setText("Rozłączony.");
                        if (pg1 != null) {
                            pg1.polzard.setEnabled(true);
                            pg1.rozard.setEnabled(false);
                        }
                        polardena = true;
                        rozardena = false;
                        if (pg2 != null) {
                            pg2.allButtons((ViewGroup) pg2.getView(),"disable" );
                        }
                    } else if (text.startsWith("Polaczony")) {
                        polardval.setText(text.replace("Polaczony", "Połączony"));
                        if (pg1 != null) {
                            pg1.polzard.setEnabled(false);
                            pg1.rozard.setEnabled(true);
                        }
                        polardena = false;
                        rozardena = true;
                        if (pg2 != null) {
                            pg2.allButtons((ViewGroup) pg2.getView(),"enable" );
                        }
                    } else if (text.startsWith("failed")) {
                        polardval.setText("Nieudane poł. z " + text.substring(text.indexOf(" ")));
                    }
                    break;
                case 8:
                    if (text.equals("on")) {
                        kalibena = false;
                        reskalibena = true;
                    } else if (text.equals("off")) {
                        kalibena = true;
                        reskalibena = false;
                    }
                    if (pg3 != null) {
                        pg3.kalibracja.setEnabled(kalibena);
                        pg3.resetkalibracji.setEnabled(reskalibena);
                    }
            }
        }
    };

    void wyslijDoMain(String rozkaz, String wiadomosc) {
        if (polaczenie != null) {
            polaczenie.writes(rozkaz, wiadomosc);
        }
    }
}
