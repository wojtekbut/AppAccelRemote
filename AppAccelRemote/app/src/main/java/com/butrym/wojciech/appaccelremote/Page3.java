package com.butrym.wojciech.appaccelremote;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class Page3 extends Fragment implements View.OnClickListener {

    Page1.communicateToActivity mCallBack;
    Button kalibracja, resetkalibracji;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page3, container, false);
        kalibracja = (Button) v.findViewById(R.id.kalibruj);
        resetkalibracji = (Button) v.findViewById(R.id.resetkalibracji);
        kalibracja.setEnabled(mCallBack.isValue("kalibena"));
        resetkalibracji.setEnabled(mCallBack.isValue("reskalibena"));
        kalibracja.setOnClickListener(this);
        resetkalibracji.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.kalibruj:
                kalibracja();
                break;
            case R.id.resetkalibracji:
                resetkalibracji();
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

    void kalibracja() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.zapisz, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String roz = "KALIBRACJA:";
                    mCallBack.sendMessage(roz, "");
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            builder.setMessage("Aby skalibrować zdalne urządzenie,\npołóż je na poziomej powierzchni.\n" +
                    "a następnie kliknij przycisk \"Kalibruj\".\n" +
                    "Zdalne urządzenie odczeka 3 sekundy, aby zniwelować drgania,\n" +
                    "a następnie zapisze dane kalibracyjne.");
            AlertDialog dialog = builder.create();
            dialog.show();

        }

        void resetkalibracji() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.wyczysc, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String roz = "RESETKALIBRACJI:";
                    mCallBack.sendMessage(roz, "");
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            builder.setMessage("Usunąć kalibrację?\nAby ponownie skalibrować zdalne urządzenie,\npotrzebna jest idealnie pozioma powierzchnia.");
            AlertDialog dialog = builder.create();
            dialog.show();

        }
}