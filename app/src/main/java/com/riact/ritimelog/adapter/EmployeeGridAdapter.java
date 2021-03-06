package com.riact.ritimelog.adapter;

/**
 * Created by koushik on 15/8/17.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


import com.riact.ritimelog.AttendanceRegister;
import com.riact.ritimelog.R;
import com.riact.ritimelog.models.EmployeeModel;


public class EmployeeGridAdapter extends ArrayAdapter<EmployeeModel> {

    private ArrayList<EmployeeModel> dataSet;
    Context mContext;
    EmployeeModel dataModel;
    View myView;



    // View lookup cache
    private static class ViewHolder {
        TextView txtEmpName;
        TextView txtEmpCode;
        TextView txtSiteCode;
        LinearLayout linearLayout;
        RelativeLayout relativeLayout;
    }

    public EmployeeGridAdapter(ArrayList<EmployeeModel> data, Context context , View myview) {
        super(context, R.layout.employee_grid_item_layout, data);
        this.dataSet = data;
        this.mContext=context;
        this.myView = myview;

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
         dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        EmployeeGridAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new EmployeeGridAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.employee_grid_item_layout, parent, false);
            viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.carview);
            viewHolder.relativeLayout = (RelativeLayout)  convertView.findViewById(R.id.layout_background);




            viewHolder.txtEmpName = (TextView) convertView.findViewById(R.id.emp_name);
            viewHolder.txtEmpCode = (TextView) convertView.findViewById(R.id.emp_code);

            if(dataModel.getAttendance())
            {
                viewHolder.relativeLayout.setBackgroundColor(Color.parseColor("#FF7CEF7C"));

            }
            viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  new AttendanceRegister().showAttendanceDialog(position,getContext(),myView);
                }
            });
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (EmployeeGridAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }
        viewHolder.txtEmpName.setText(dataModel.getEmp_name());
        viewHolder.txtEmpCode.setText(dataModel.getEmp_code());
        return convertView;
    }
}
