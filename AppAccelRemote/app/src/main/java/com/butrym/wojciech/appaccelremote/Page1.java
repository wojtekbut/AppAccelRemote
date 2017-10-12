package com.butrym.wojciech.appaccelremote;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class Page1 extends Fragment implements View.OnClickListener {

    communicateToActivity mCallBack;
    Button polzmain, polzard, rozmain, rozard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page1, container, false);
        polzmain = (Button) v.findViewById(R.id.polzmain);
        polzmain.setOnClickListener(this);
        polzard = (Button) v.findViewById(R.id.polzard);
        rozmain = (Button) v.findViewById(R.id.rozmain);
        rozard = (Button) v.findViewById(R.id.rozard);
        polzard.setEnabled(mCallBack.isValue("polardena"));
        rozard.setEnabled(mCallBack.isValue("rozardena"));
        polzmain.setEnabled(mCallBack.isValue("polmainena"));
        rozmain.setEnabled(mCallBack.isValue("rozmainena"));
        polzard.setOnClickListener(this);
        rozmain.setOnClickListener(this);
        rozard.setOnClickListener(this);
        if (mCallBack.isValue("polaczenie")) {
            polzmain.setEnabled(false);
        }
        return v;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.polzmain:
                mCallBack.sendMessage("Łączę z Main");
                break;
            case R.id.polzard:
                if (mCallBack.isValue("polaczenie")) {
                    String roz = "lista:";
                    String msg = "getBonded";
                    mCallBack.sendMessage(roz, msg);
                }
                break;
            case R.id.rozmain:
                mCallBack.sendMessage("cancel");
                break;
            case R.id.rozard:
                mCallBack.sendMessage("ARDUINO:", "cancel");
                break;
        }
    }

    interface communicateToActivity {
        void sendMessage(String... msg);
        boolean isValue(String string);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mCallBack = (communicateToActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " zaimplementuj communicateToActivity");
        }
    }
}