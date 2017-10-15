package com.butrym.wojciech.appaccelremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by wojtek on 04.10.17.
 */

class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final Handler mHandler;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    ConnectThread(BluetoothDevice device, Handler handler) {

        mHandler = handler;
        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try {
            mmSocket.connect();
            mHandler.obtainMessage(4).sendToTarget();
        } catch (IOException connectException) {
            mHandler.obtainMessage(3, "Rozłączony").sendToTarget();
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();

            }
            return;
        }
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = mmSocket.getInputStream();
        } catch (IOException e) {
            Log.e("InStream", "InputStream nie podłączony.");
        }
        try {
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("OutStream", "OutputStream nie podłączony.");
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        czytaj();
    }


    private void czytaj() {
        byte[] buffer = new byte[2];
        byte[] poczatek = new byte[6];
        byte dlrozkazu;
        byte[] rozkaz;
        byte[] objetosc = new byte[2];
        byte[] koniec = new byte[7];
        int dlugosc;
        while (mmSocket.isConnected()) {
            try {
                poczatek[0] = (byte) mmInStream.read();
                if ((char) poczatek[0] == 's'){
                    for (int i=1;i<6;i++){
                        poczatek[i] = (byte) mmInStream.read();
                    }
                    String spocz = new String(poczatek);
                    if (spocz.equals("stArt:")){
                        dlrozkazu = (byte) mmInStream.read();
                        rozkaz = new byte[dlrozkazu];
                        for (int i = 0;i<2;i++) {
                            objetosc[i] = (byte) mmInStream.read();
                        }
                        dlugosc = objetosc[0]*256+objetosc[1];
                        buffer = new byte[dlugosc];
                        for (int i = 0; i< dlrozkazu; i++){
                            rozkaz[i] = (byte) mmInStream.read();
                        }
                        for (int i = 0; i< dlugosc; i++){
                            buffer[i] = (byte) mmInStream.read();
                        }
                        for (int i = 0; i<7; i++) {
                            koniec[i] = (byte) mmInStream.read();
                        }
                        String skoniec = new String(koniec);
                        if (skoniec.equals(":koNiec")){
                            wiadomosc(new String(rozkaz), buffer);
                            Arrays.fill(buffer, (byte) 0);
                            Arrays.fill(poczatek, (byte) 0);
                            Arrays.fill(koniec, (byte) 0);
                            Arrays.fill(objetosc, (byte) 0);
                        }else {
                            Arrays.fill(buffer, (byte) 0);
                            Arrays.fill(poczatek, (byte) 0);
                            Arrays.fill(koniec, (byte) 0);
                            Arrays.fill(objetosc, (byte) 0);
                        }
                    }else {
                        Arrays.fill(buffer, (byte) 0);
                        Arrays.fill(poczatek, (byte) 0);
                        Arrays.fill(koniec, (byte) 0);
                        Arrays.fill(objetosc, (byte) 0);
                    }
                }
            } catch (IOException e) {
                mHandler.obtainMessage(3, "Rozłączony").sendToTarget();
                break;
            }
        }

    }

    private void wiadomosc(String rozkaz, byte[] wiadomosc) {
        Log.d("wiadomość", rozkaz + " " + new String(wiadomosc));
        if (rozkaz.startsWith("x:")) {
            mHandler.obtainMessage(1, new String(wiadomosc)).sendToTarget();
        } else if (rozkaz.startsWith("y:")) {
            mHandler.obtainMessage(2, new String(wiadomosc)).sendToTarget();
        } else if (rozkaz.startsWith("lista:")) {
            ArrayList<String> listaArray = new ArrayList<>();
            ByteArrayInputStream bais = new ByteArrayInputStream(wiadomosc);
            ObjectInputStream objectIn = null;
            try {
                objectIn = new ObjectInputStream(bais);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                listaArray = (ArrayList<String>) objectIn.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            mHandler.obtainMessage(5,listaArray ).sendToTarget();
        } else if (rozkaz.startsWith("TOAST:")) {
            mHandler.obtainMessage(6, new String(wiadomosc)).sendToTarget();
        } else if (rozkaz.startsWith("ARDUINO:")) {
            mHandler.obtainMessage(7, new String(wiadomosc)).sendToTarget();
        } else if (rozkaz.startsWith("KALIBRACJA:")) {
            mHandler.obtainMessage(8, new String(wiadomosc)).sendToTarget();
        }
    }

    void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(byte[] buffer) {
        try {
            if (mmSocket.getOutputStream() == null) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (!mmSocket.isConnected()) {
                return;
            } else {
                mmOutStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    void writes(String rozkaz, String string) {
        String poczatek, koniec;
        byte[] bpoczatek, bkoniec, brozkaz, bstring, wiadomosc;
        poczatek = "stArt:";
        koniec = ":koNiec";
        bpoczatek = poczatek.getBytes();
        bkoniec = koniec.getBytes();
        brozkaz = rozkaz.getBytes();
        bstring = string.getBytes();
        byte[] objetosc = new byte[2];
        byte dlrozkazu = (byte) brozkaz.length;
        int dlugosc = bstring.length;
        if (dlugosc < 256) {
            objetosc[0] = 0;
            objetosc[1] = (byte) dlugosc;
        } else if (dlugosc > 255 && dlugosc < 65535) {
            objetosc[0] = (byte) (dlugosc / 256);
            objetosc[1] = (byte) (dlugosc - ((int) objetosc[0] * 256));
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(bpoczatek);
            outputStream.write(dlrozkazu);
            outputStream.write(objetosc);
            outputStream.write(brozkaz);
            outputStream.write(bstring);
            outputStream.write(bkoniec);

        } catch (IOException e) {
            e.printStackTrace();
        }
        wiadomosc = outputStream.toByteArray();
        write(wiadomosc);
    }
}
