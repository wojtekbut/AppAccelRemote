package com.butrym.wojciech.appaccelremote;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;


public class Page2 extends Fragment implements View.OnClickListener {

    Page1.communicateToActivity mCallBack;
    RadioGroup goradol, podniesobroc;
    Button b1,b2,b3,b4,b5,b6,b7,b8;
    Button[] buttons;
    double[] podnarr, obrarr, arr;
    int wspolczynnik;
    String rozkaz;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page2, container, false);
        allButtons((ViewGroup) v, "onclick");
        if (!mCallBack.isValue("polardena") && mCallBack.isValue("polmainena")) {
            allButtons((ViewGroup) v, "disable");
        } else {
            allButtons((ViewGroup) v, "enable");
        }
        b1 = (Button) v.findViewById(R.id.b1);
        b2 = (Button) v.findViewById(R.id.b2);
        b3 = (Button) v.findViewById(R.id.b3);
        b4 = (Button) v.findViewById(R.id.b4);
        b5 = (Button) v.findViewById(R.id.b5);
        b6 = (Button) v.findViewById(R.id.b6);
        b7 = (Button) v.findViewById(R.id.b7);
        b8 = (Button) v.findViewById(R.id.b8);
        buttons = new Button[]{b1,b2,b3,b4,b5,b6,b7,b8};
        podnarr = new double[]{0.02,0.05,0.1,0.2,0.5,1,1.5,2};
        obrarr = new double[]{1,5,10,30,45,60,90,180};
        for (int i=0;i<8;i++) {
            buttons[i].setText(String.valueOf(podnarr[i]));
        }
        podniesobroc = (RadioGroup) v.findViewById(R.id.radiogruppodnies);
        goradol = (RadioGroup) v.findViewById(R.id.radiogrupgora);
        podniesobroc.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId==R.id.obroc) {
                    for (int i=0;i<8;i++) {
                        buttons[i].setText(String.valueOf(obrarr[i]));
                    }
                } else {
                    for (int i=0;i<8;i++) {
                        buttons[i].setText(String.valueOf(podnarr[i]));
                    }
                }
            }
        });
        return v;
    }


    @Override
    public void onClick(View v) {
        switch (podniesobroc.getCheckedRadioButtonId()) {
            case R.id.podnies:
                rozkaz = "PODNIES:";
                arr = podnarr;
                break;
            case R.id.przechyl:
                rozkaz = "POCHYL:";
                arr = podnarr;
                break;
            case R.id.obroc:
                rozkaz = "OBROC:";
                arr = obrarr;
                break;
        }
        switch (goradol.getCheckedRadioButtonId()) {
            case R.id.gora:
                wspolczynnik = 1;
                break;
            case R.id.dol:
                wspolczynnik = -1;
                break;
        }
        switch (v.getId()) {
            case R.id.b1: //
                mCallBack.sendMessage(rozkaz, String.valueOf(wspolczynnik * arr[0]));
                break;
            case R.id.b2: //
                mCallBack.sendMessage(rozkaz, String.valueOf(wspolczynnik * arr[1]));
                break;
            case R.id.b3: //
                mCallBack.sendMessage(rozkaz, String.valueOf(wspolczynnik * arr[2]));
                break;
            case R.id.b4: //
                mCallBack.sendMessage(rozkaz, String.valueOf(wspolczynnik * arr[3]));
                break;
            case R.id.b5: //
                mCallBack.sendMessage(rozkaz, String.valueOf(wspolczynnik * arr[4]));
                break;
            case R.id.b6: //
                mCallBack.sendMessage(rozkaz, String.valueOf(wspolczynnik * arr[5]));
                break;
            case R.id.b7: //
                mCallBack.sendMessage(rozkaz, String.valueOf(wspolczynnik * arr[6]));
                break;
            case R.id.b8: //
                mCallBack.sendMessage(rozkaz, String.valueOf(wspolczynnik * arr[7]));
                break;
            case R.id.stop: //
                mCallBack.sendMessage("STOP:", "");
                break;
            case R.id.rownoleg: //
                mCallBack.sendMessage("ROWNOL:", "");
                break;
            case R.id.zeruj: //
                mCallBack.sendMessage("ZERUJ:", "");
                break;
        }

    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mCallBack = (Page1.communicateToActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " zaimplementuj communicateToActivity");
        }
    }

    public void allButtons(ViewGroup parent, String corobic) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                allButtons((ViewGroup) child, corobic);
            } else {
                if (child != null) {
                    if (child instanceof Button) {
                        switch (corobic) {
                            case "onclick":
                                child.setOnClickListener(this);
                                break;
                            case "enable":
                                child.setEnabled(true);
                                break;
                            case "disable":
                                child.setEnabled(false);
                                break;
                        }

                    }
                }
            }
        }
    }
}
