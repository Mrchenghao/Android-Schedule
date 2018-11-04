package com.example.robert.schedule;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TeacherAdapter extends ArrayAdapter<String> {
    private List<String> teachers;
    private int res;
    private List<String> selectedList;

    public TeacherAdapter(Context context, int res, List<String> teachers,List<String> selectedList){
        super(context,res,teachers);
        this.teachers = teachers;
        this.res = res;
        //this.selectedList = selectedList;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final String teacher = teachers.get(position);
        View view = LayoutInflater.from(getContext()).inflate(res,parent,false);
        TextView teacher_name = view.findViewById(R.id.teacher_name);
        teacher_name.setText(teacher);
        //if (position==16)Log.i("robert",isSelected(teacher)+teacher+selectedList.get(0));
//        if (isSelected(teacher)){
//            Log.i("robert",teacher+"is selected");
//        }
        return view;

    }


    public boolean isSelected(String name){
        for (int i = 0;i<selectedList.size();i++ ){
            if (name.equals(selectedList.get(i))){
                return true;
            }
        }
        return false;
    }











}
