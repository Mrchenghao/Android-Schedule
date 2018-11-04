package com.example.robert.schedule;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



 class Teacher {

    public String teacherID;
    public String teacherName;
    public String PY;

    public void setTeacherID(String teacherID) {
        this.teacherID = teacherID;
    }

    public String getTeacherID() {
        return teacherID;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setPY(){
        ChineseToEnglish c = new ChineseToEnglish();
        //Log.i("robert",str+"="+c.getPinYinFirst(str));
        this.PY = c.getPinYinFirst(teacherName);
    }

    public String getPY(){
        return PY;
    }

}


public class TeacherList {


    public List list=new ArrayList();
    public List getList(){
        return list;
    }

    public void creatList(String jsonStr) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i<jsonArray.length();i++){
            JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
            Teacher teacher = new Teacher();
            teacher.setTeacherName(jsonObject.getString("teacherName"));
            teacher.setTeacherID(jsonObject.getString("teacherID"));
            list.add(teacher);
        }
        threadProcessName();
    }

    public void addTeacher(String teacherID,String teacherName)  {
        Teacher teacher = new Teacher();
        teacher.setTeacherName(teacherName);
        teacher.setTeacherID(teacherID);
        teacher.setPY();
        list.add(teacher);
    }



    //获取教师数量
    public int getTeacherNum(){
        return list.size();
    }

    //获取教师ID
    public String getTeacherIDbyid(int i){
        return ((Teacher)list.get(i)).getTeacherID();
    }

    //获取教师Name
    public String getTeacherNamebyid(int i){
        return ((Teacher)list.get(i)).getTeacherName();
    }

    //获取教师PY
    public String getTeacherPYbyid(int i){
        return ((Teacher)list.get(i)).getPY();
    }

    public void threadProcessName(){
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0 ; i <list.size();i++){
                    ((Teacher)list.get(i)).setPY();
                }
            }
        }).start();
    }


    //模糊查询
    public List likeString(String likename){
        List resList = new ArrayList();
        for(int i=0;i<list.size();i++){
            if(((Teacher)(list.get(i))).getPY().indexOf(likename)>-1)
                resList.add(list.get(i));
        }
        return resList;

    }



}
