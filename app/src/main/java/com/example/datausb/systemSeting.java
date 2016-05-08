package com.example.datausb;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Arrays;

/**
 * Created by wang on 2016/2/23.
 */
public class systemSeting extends android.app.Fragment {
    private EditText data;
    private Button sentdata;
    private TextView showbyte;
    private EditText oplong;
    private Button setopl;
    private Switch sw;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.systemseting, container, false);

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        data = (EditText) getActivity().findViewById(R.id.editText3);
        sentdata = (Button) getActivity().findViewById(R.id.button10);
        showbyte = (TextView) getActivity().findViewById(R.id.textView17);
        sw=(Switch)getActivity().findViewById(R.id.switch1);
        if(((main1) getActivity()).STOREDATA==1){
            sw.setChecked(true);
        }
        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file=new File("mnt/external_sd/LOST.DIR");
              //  Environment.getExternalStorageState();
               // Log.e("ee",Boolean.valueOf(file.exists()).toString());
                //Log.e("ee",Boolean.valueOf(sw.isChecked()).toString());
                int flag=0;
                if(file.exists())
                {
                    flag=1;
                }
                if(sw.isChecked()&& (file.exists())){
                     ((main1) getActivity()).STOREDATA=1;
                }
                else {
                    sw.setChecked(false);
                    ((main1) getActivity()).STOREDATA=0;
                    if (flag==0){
                        Toast.makeText(((main1) getActivity()).getApplication(),"没有检测到SD卡,或重启系统",Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
        sentdata.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String s = data.getText().toString();//String变量
                byte[] b = s.getBytes();//String转换为byte[]
                // String res = new String(b);
                String hex = "0";
                String hh = "";
                for (int i = 0; i < b.length; i++) {
                    hex = Integer.toHexString(b[i] & 0xFF);

                    if (hex.length() == 1) {
                        hex = '0' + hex;
                    }
                    hh = hh + hex;
                }
                showbyte.setText(hh);
                ((main1) getActivity()).sentdata(b);
                //在这里使用getActivity

            }
        });

        oplong = (EditText) getActivity().findViewById(R.id.editText5);
        int ll = ((main1) getActivity()).preferences.getInt("long", 0);
        if(ll!=0)
        {

            oplong.setText(Integer.toString(ll));
        }

        setopl = (Button) getActivity().findViewById(R.id.button11);
        setopl.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          int ol = Integer.valueOf(oplong.getText().toString());
                                          ((main1) getActivity()).setpref(ol);
                                      }
                                  }
        );
    }
}
