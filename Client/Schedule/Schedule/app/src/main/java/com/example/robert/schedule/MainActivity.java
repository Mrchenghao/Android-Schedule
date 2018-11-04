package com.example.robert.schedule;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity  {
    //数据变量
    private List<String> teacherNameList=new ArrayList<String>();
    private List resList;
    private EditText search_text;
    private DBHelper dbHelper;
    private SQLiteDatabase db_w;
    private SQLiteDatabase db_r;
    private TeacherList teachersList = new TeacherList();
    //控件
    private EditText editText;
    private Spinner spinner;
    private MyImageView imageView;
    /** 第一个无内容的格子 */
    protected TextView empty;
    /** 星期一的格子 */
    protected TextView monColum;
    /** 星期二的格子 */
    protected TextView tueColum;
    /** 星期三的格子 */
    protected TextView wedColum;
    /** 星期四的格子 */
    protected TextView thrusColum;
    /** 星期五的格子 */
    protected TextView friColum;
    /** 星期六的格子 */
    protected TextView satColum;
    /** 星期日的格子 */
    protected TextView sunColum;
    /** 课程表body部分布局 */
    protected RelativeLayout course_table_layout;
    /** 屏幕宽度 **/
    protected int screenWidth;
    /** 课程格子平均宽度 **/
    protected int aveWidth;
    protected int gridHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获得列头的控件
        empty = (TextView) this.findViewById(R.id.test_empty);
        monColum = (TextView) this.findViewById(R.id.test_monday_course);
        tueColum = (TextView) this.findViewById(R.id.test_tuesday_course);
        wedColum = (TextView) this.findViewById(R.id.test_wednesday_course);
        thrusColum = (TextView) this.findViewById(R.id.test_thursday_course);
        friColum = (TextView) this.findViewById(R.id.test_friday_course);
        satColum  = (TextView) this.findViewById(R.id.test_saturday_course);
        sunColum = (TextView) this.findViewById(R.id.test_sunday_course);
        course_table_layout = (RelativeLayout) this.findViewById(R.id.test_course_rl);
        spinner = findViewById(R.id.spinner_teacher);
        editText = findViewById(R.id.verify_code);
        search_text = findViewById(R.id.search_text);
        imageView = findViewById(R.id.verify_img);
        dbHelper = new DBHelper(this,"scheduleDB",null,3);
        db_w = dbHelper.getWritableDatabase();
        db_r = dbHelper.getReadableDatabase();
        createTable();
        getVerifyCode();
        getTeachers();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        db_w.close();
        db_r.close();
    }

    /**
     * 根据用户选择的老师来获取其课程表
     * @param teacherID
     */
    public void getSchedule(final String teacherID){
        final String verificationCode = editText.getText().toString();
        //Toast.makeText(MainActivity.this, teacherID+"--"+verificationCode, Toast.LENGTH_SHORT).show();
        course_table_layout.removeAllViews();
//        String sqlSchedule="select * from schedule where teacherID=?"+teacherID;
//        Log.i("robert",sqlSchedule);
//        Cursor cursor = db_r.rawQuery(sqlSchedule,null);
        Cursor cursor = db_r.query("schedule",null,"teacherID=?",new String[]{teacherID},null,null,null);
        Log.i("robert",cursor.getCount()+"");
        if (cursor.getCount()>1){
            createTable();
            while (cursor.moveToNext()){
                int weekday = cursor.getInt(cursor.getColumnIndex("weekday"));
                int fromClass = cursor.getInt(cursor.getColumnIndex("fromClass"));
                addClass(cursor.getString(cursor.getColumnIndex("classInfo")),weekday,fromClass);
            }
        }else{
            //开启线程来发起网络请求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String path = "http://www.ssrecord.cn/phpweb/index.php/home/android/post_verify_code";
                    HttpURLConnection conn = null;
                    BufferedReader reader = null;
                    try {
                        URL url = new URL(path);
                        String para = new String("teacherID="+teacherID+"&verificationCode="+verificationCode);
                        //1.得到HttpURLConnection实例化对象
                        conn = (HttpURLConnection) url.openConnection();
                        //2.设置请求方式
                        conn.setRequestMethod("POST");
                        //3.设置post提交内容的类型和长度
                        /*
                         * 只有设置contentType为application/x-www-form-urlencoded，
                         * servlet就可以直接使用request.getParameter("username");直接得到所需要信息
                         */
                        conn.setRequestProperty("contentType","application/x-www-form-urlencoded");
                        conn.setRequestProperty("Content-Length", String.valueOf(para.getBytes().length));
                        //默认为false
                        conn.setDoOutput(true);
                        //4.向服务器写入数据
                        conn.getOutputStream().write(para.getBytes());
                        //5.得到服务器相应
                        if (conn.getResponseCode() == 200) {
                            //Log.i("robert","服务器已经收到表单数据！");
                        } else {
                            Log.i("robert","请求失败！");
                        }
                        //process the inputstream
                        InputStream in = conn.getInputStream();
                        reader = new BufferedReader(new InputStreamReader((in)));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine())!=null){
                            response.append(line);
                        }
                        showSchedule(response.toString(),teacherID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        //6.释放资源
                        if (conn != null) {
                            //关闭连接 即设置 http.keepAlive = false;
                            conn.disconnect();
                        }
                    }

                }
            }).start();
        }


    }

    /**
     * 展示获取到的课程表
     * @param jsonData
     */
    public void showSchedule(final String jsonData,final String teacherID){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createTable();
                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    if (jsonObject.getString("successful")=="true"){
                        changeStringToClass(jsonObject.getString("data"),teacherID);
                    }else {
                        Toast.makeText(MainActivity.this,"验证码错误！", Toast.LENGTH_SHORT).show();
                        getVerifyCode();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 刷新校验码
     * @param view
     */
    public void update_img(View view) {
        Toast.makeText(this,"refresh",Toast.LENGTH_SHORT).show();
        getVerifyCode();
    }

    /**
     * @title 获取校验码
     */
    public void getVerifyCode() {
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("http://www.ccchenghao.cn:8000/courses/getVerificationCode");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    //process the inputstream
                    reader = new BufferedReader(new InputStreamReader((in)));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine())!=null){
                        response.append(line);
                    }
                    JSONObject ob = new JSONObject(response.toString());
                    if (ob.getString("successful")=="true")
                        setImageViewUrl(ob.getString("data"));
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (reader != null){
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * @title 设置imageview url
     */
    public void setImageViewUrl(final String url){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Log.i("robert",url);
                imageView.setImageURL(url);
            }
        });
    }

    public void searchTeacher(View view) {

        String search_key = search_text.getText().toString();
        Log.i("robert",search_key.length()+"");
        if (search_key.length()>0){
            //resList.clear();
            resList = teachersList.likeString(search_key);
            List l = new ArrayList<String>();
            l.add("教师列表");
            for (int i=0;i<resList.size();i++){
                //Log.i("robert",search_text.getText().toString()+" search res:"+((TeacherList.Teacher)(resList.get(i))).getTeacherName());
                l.add(((Teacher)(resList.get(i))).getTeacherName());
            }
            showTeachers(l);
        }else {
            Log.i("robert",search_key);
            showTeachers(teacherNameList);
        }

    }

    /**
     * @title 获取教师的名单
     *
     */
    private void getTeachers(){
        String sqlTeacher="select * from teacher";
        Cursor cursor = db_r.rawQuery(sqlTeacher,null);
        //Log.i("robert",cursor.getCount()+"");
        if (cursor.getCount()>1){
            teacherNameList.clear();
            teacherNameList.add("选择教师");
            while (cursor.moveToNext()){
                String temp = cursor.getString(cursor.getColumnIndex("teacherName"));
                teachersList.addTeacher(cursor.getString(cursor.getColumnIndex("teacherID")),temp);
                teacherNameList.add(temp);
            }showTeachers(teacherNameList);
        }else{
            //开启线程来发起网络请求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    try {
                        URL url = new URL("http://www.ccchenghao.cn:8000/courses/getTeachers");
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        InputStream in = connection.getInputStream();
                        //process the inputstream
                        reader = new BufferedReader(new InputStreamReader((in)));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine())!=null){
                            response.append(line);
                        }
                        parseJSONWithJSONObject(response.toString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        if (reader != null){
                            try{
                                reader.close();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                        if (connection != null){
                            connection.disconnect();
                        }
                    }
                }
            }).start();
        }

        showTeachers(teacherNameList);

    }

    /**
     * @title 将教师的名单在UI中展示
     * @param
     */
    private void showTeachers(final List l) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() { //Log.i("robert","showResponse");
                //UI operation
                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,l);
                TeacherAdapter adapter = new TeacherAdapter(MainActivity.this,R.layout.teacher_spinner,l,getSelectedTeacherList());
                spinner = findViewById(R.id.spinner_teacher);
                spinner.setAdapter(adapter);
            }
        });
        //给Spinner添加事件监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /*
             * parent接收的是被选择的数据项所属的 Spinner对象，
             * view参数接收的是显示被选择的数据项的TextView对象
             * position接收的是被选择的数据项在适配器中的位置
             * id被选择的数据项的行号
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                if (position!=0){
                    //view.setBackgroundColor(Color.parseColor("#22B8DD"));
                    if (editText.getText().toString().length()>0){
                        String search_key = search_text.getText().toString();Log.i("robert","showResponse");
                        if (search_key.length()==0){
                            Log.i("robert",teachersList.getTeacherPYbyid(position-1)+"="+teachersList.getTeacherNamebyid(position-1));
                            getSchedule(teachersList.getTeacherIDbyid(position-1));
                        }else {
                            getSchedule(((Teacher)(resList.get(position-1))).getTeacherID());
                        }
                    }else
                        Toast.makeText(MainActivity.this,"验证码不能为空！", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * @title 解析json
     * @param jsonData
     * @remark 服务端传过来的是一个对象，先对json解析成对象，其中"status"是状态返回码，“data”项
     * 是一个数组，即具体应用数据
     */
    private void parseJSONWithJSONObject(String jsonData){
        try{
            //先将json解析成对象
            JSONObject jsonObject = new JSONObject(jsonData);
            String status = jsonObject.getString("successful");
            if (status=="true"){
                //再将对象中的data解析成数组
                teachersList.creatList(jsonObject.getString("data"));
                teacherNameList.add("选择教师");
                for (int i = 0; i<teachersList.getTeacherNum();i++){
                    //最后将数组各项解析成对象，得到要的各项数据
                    teacherNameList.add(teachersList.getTeacherNamebyid(i));
                    ContentValues cv = new ContentValues();
                    cv.put("teacherID",teachersList.getTeacherIDbyid(i));
                    cv.put("teacherName",teachersList.getTeacherNamebyid(i));
                    db_w.insert("teacher",null,cv);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 绘出表格
     */

    public void createTable(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //屏幕宽度
        int width = dm.widthPixels;
        //平均宽度
        int aveWidth = width / 8;
        //第一个空白格子设置为25宽
        empty.setWidth(aveWidth * 3/4);
        monColum.setWidth(aveWidth * 33/32 + 1);
        tueColum.setWidth(aveWidth * 33/32 + 1);
        wedColum.setWidth(aveWidth * 33/32 + 1);
        thrusColum.setWidth(aveWidth * 33/32 + 1);
        friColum.setWidth(aveWidth * 33/32 + 1);
        satColum.setWidth(aveWidth * 33/32 + 1);
        sunColum.setWidth(aveWidth * 33/32 + 1);
        this.screenWidth = width;
        this.aveWidth = aveWidth;
        int height = dm.heightPixels;
        gridHeight = height / 4;
        //设置课表界面
        //动态生成4 * maxCourseNum个textview
        for(int i = 1; i <= 6; i ++){

            for(int j = 1; j <= 8; j ++){
                TextView tx = new TextView(MainActivity.this);
                tx.setId((i - 1) * 8  + j);
                //除了最后一列，都使用course_text_view_bg背景（最后一列没有右边框）
                if(j < 8)
                    tx.setBackgroundDrawable(MainActivity.this.
                            getResources().getDrawable(R.drawable.course_text_view_bg));
                else
                    tx.setBackgroundDrawable(MainActivity.this.
                            getResources().getDrawable(R.drawable.course_table_last_colum));
                //相对布局参数
                RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
                        aveWidth * 33 / 32 + 1,
                        gridHeight);
                //文字对齐方式
                tx.setGravity(Gravity.CENTER);
                //字体样式
                tx.setTextAppearance(this, R.style.courseTableText);
                //如果是第一列，需要设置课的序号（一 到 四）
                if(j == 1)
                {
                    tx.setText(String.valueOf(i));
                    rp.width = aveWidth * 3/4;
                    //设置他们的相对位置
                    if(i == 1)
                        rp.addRule(RelativeLayout.BELOW, empty.getId());
                    else
                        rp.addRule(RelativeLayout.BELOW, (i - 1) * 8);
                }
                else
                {
                    rp.addRule(RelativeLayout.RIGHT_OF, (i - 1) * 8  + j - 1);
                    rp.addRule(RelativeLayout.ALIGN_TOP, (i - 1) * 8  + j - 1);
                    tx.setText("");
                }

                tx.setLayoutParams(rp);
                course_table_layout.addView(tx);

            }
        }
    }

    /**
     * 将字符串格式的课程信息转换成具体的课程
     * @param str
     */
    public void changeStringToClass(final String str,final String teacherID){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("robert", str);
                try {
                    JSONObject ob = new JSONObject(str);
                    ContentValues cv = new ContentValues();
                    if (ob.getString("MondayMorningOne").length()>1) {
                        addClass(ob.getString("MondayMorningOne"), 1, 1);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("MondayMorningOne"));
                        cv.put("weekday", 1);
                        cv.put("fromClass", 1);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("MondayMorningTwo").length()>1) {
                        addClass(ob.getString("MondayMorningTwo"), 1, 2);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("MondayMorningTwo"));
                        cv.put("weekday", 1);
                        cv.put("fromClass", 2);
                        //Log.i("robert",db_w.insert("schedule", null, cv)+"");
                    }
                    if (ob.getString("MondayAfternoonThree").length()>1) {
                        addClass(ob.getString("MondayAfternoonThree"), 1, 3);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("MondayAfternoonThree"));
                        cv.put("weekday", 1);
                        cv.put("fromClass", 3);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("MondayAfternoonFour").length()>1) {
                        addClass(ob.getString("MondayAfternoonFour"), 1, 4);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("MondayAfternoonFour"));
                        cv.put("weekday", 1);
                        cv.put("fromClass", 4);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("MondayNightFive").length()>1) {
                        addClass(ob.getString("MondayNightFive"), 1, 5);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("MondayNightFive"));
                        cv.put("weekday", 1);
                        cv.put("fromClass", 5);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("MondayNightSix").length()>1) {
                        addClass(ob.getString("MondayNightSix"), 1, 6);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("MondayNightSix"));
                        cv.put("weekday", 1);
                        cv.put("fromClass", 6);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThuesdayMorningOne").length()>1) {
                        addClass(ob.getString("ThuesdayMorningOne"), 2, 1);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThuesdayMorningOne"));
                        cv.put("weekday", 2);
                        cv.put("fromClass", 1);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThuesdayMorningTwo").length()>1) {
                        addClass(ob.getString("ThuesdayMorningTwo"), 2, 2);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThuesdayMorningTwo"));
                        cv.put("weekday", 2);
                        cv.put("fromClass", 2);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThuesdayAfternoonThree").length()>1) {
                        addClass(ob.getString("ThuesdayAfternoonThree"), 2, 3);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThuesdayAfternoonThree"));
                        cv.put("weekday", 2);
                        cv.put("fromClass", 3);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThuesdayAfternoonFour").length()>1) {
                        addClass(ob.getString("ThuesdayAfternoonFour"), 2, 4);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThuesdayAfternoonFour"));
                        cv.put("weekday", 2);
                        cv.put("fromClass", 4);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThuesdayNightFive").length()>1) {
                        addClass(ob.getString("ThuesdayNightFive"), 2, 5);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThuesdayNightFive"));
                        cv.put("weekday", 2);
                        cv.put("fromClass", 5);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThuesdayNightSix").length()>1) {
                        addClass(ob.getString("ThuesdayNightSix"), 2, 6);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThuesdayNightSix"));
                        cv.put("weekday", 2);
                        cv.put("fromClass", 6);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("WednesdayMorningOne").length()>1) {
                        addClass(ob.getString("WednesdayMorningOne"), 3, 1);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("WednesdayMorningOne"));
                        cv.put("weekday", 3);
                        cv.put("fromClass", 1);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("WednesdayMorningTwo").length()>1) {
                        addClass(ob.getString("WednesdayMorningTwo"), 3, 2);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("WednesdayMorningTwo"));
                        cv.put("weekday", 3);
                        cv.put("fromClass", 2);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("WednesdayAfternoonThree").length()>1) {
                        addClass(ob.getString("WednesdayAfternoonThree"), 3, 3);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("WednesdayAfternoonThree"));
                        cv.put("weekday", 3);
                        cv.put("fromClass", 3);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("WednesdayAfternoonFour").length()>1) {
                        addClass(ob.getString("WednesdayAfternoonFour"), 3, 4);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("WednesdayAfternoonFour"));
                        cv.put("weekday", 3);
                        cv.put("fromClass", 4);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("WednesdayNightFive").length()>1) {
                        addClass(ob.getString("WednesdayNightFive"), 3, 5);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("WednesdayNightFive"));
                        cv.put("weekday", 3);
                        cv.put("fromClass", 5);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("WednesdayNightSix").length()>1) {
                        addClass(ob.getString("WednesdayNightSix"), 3, 6);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("WednesdayNightSix"));
                        cv.put("weekday", 3);
                        cv.put("fromClass", 6);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThursdayMorningOne").length()>1) {
                        addClass(ob.getString("ThursdayMorningOne"), 4, 1);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThursdayMorningOne"));
                        cv.put("weekday", 4);
                        cv.put("fromClass", 1);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThursdayMorningTwo").length()>1) {
                        addClass(ob.getString("ThursdayMorningTwo"), 4, 2);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThursdayMorningTwo"));
                        cv.put("weekday", 4);
                        cv.put("fromClass", 2);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThursdayAfternoonThree").length()>1) {
                        addClass(ob.getString("ThursdayAfternoonThree"), 4, 3);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThursdayAfternoonThree"));
                        cv.put("weekday", 4);
                        cv.put("fromClass", 3);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThursdayAfternoonFour").length()>1) {
                        addClass(ob.getString("ThursdayAfternoonFour"), 4, 4);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThursdayAfternoonFour"));
                        cv.put("weekday", 4);
                        cv.put("fromClass", 4);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThursdayNightFive").length()>1) {
                        addClass(ob.getString("ThursdayNightFive"), 4, 5);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThursdayNightFive"));
                        cv.put("weekday", 4);
                        cv.put("fromClass", 5);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("ThursdayNightSix").length()>1) {
                        addClass(ob.getString("ThursdayNightSix"), 4, 6);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("ThursdayNightSix"));
                        cv.put("weekday", 4);
                        cv.put("fromClass", 6);
                        db_w.insert("schedule", null, cv);
                    }

                    if (ob.getString("FridayMorningOne").length()>1) {
                        addClass(ob.getString("FridayMorningOne"), 5, 1);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("FridayMorningOne"));
                        cv.put("weekday", 5);
                        cv.put("fromClass", 1);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("FridayMorningTwo").length()>1) {
                        addClass(ob.getString("FridayMorningTwo"), 5, 2);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("FridayMorningTwo"));
                        cv.put("weekday", 5);
                        cv.put("fromClass", 2);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("FridayAfternoonThree").length()>1) {
                        addClass(ob.getString("FridayAfternoonThree"), 5, 3);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("FridayAfternoonThree"));
                        cv.put("weekday", 5);
                        cv.put("fromClass", 3);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("FridayAfternoonFour").length()>1) {
                        addClass(ob.getString("FridayAfternoonFour"), 5, 4);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("FridayAfternoonFour"));
                        cv.put("weekday", 5);
                        cv.put("fromClass", 4);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("FridayNightFive").length()>1) {
                        addClass(ob.getString("FridayNightFive"), 5, 5);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("FridayNightFive"));
                        cv.put("weekday", 5);
                        cv.put("fromClass", 5);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("FridayNightSix").length()>1) {
                        addClass(ob.getString("FridayNightSix"), 5, 6);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("FridayNightSix"));
                        cv.put("weekday", 5);
                        cv.put("fromClass", 6);
                        db_w.insert("schedule", null, cv);
                    }

                    if (ob.getString("SaturdayMorningOne").length()>1) {
                        addClass(ob.getString("SaturdayMorningOne"), 6, 1);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SaturdayMorningOne"));
                        cv.put("weekday", 6);
                        cv.put("fromClass", 1);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SaturdayMorningTwo").length()>1) {
                        addClass(ob.getString("SaturdayMorningTwo"), 6, 2);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SaturdayMorningTwo"));
                        cv.put("weekday", 6);
                        cv.put("fromClass", 2);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SaturdayAfternoonThree").length()>1) {
                        addClass(ob.getString("SaturdayAfternoonThree"), 6, 3);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SaturdayAfternoonThree"));
                        cv.put("weekday", 6);
                        cv.put("fromClass", 3);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SaturdayAfternoonFour").length()>1) {
                        addClass(ob.getString("SaturdayAfternoonFour"), 6, 4);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SaturdayAfternoonFour"));
                        cv.put("weekday", 6);
                        cv.put("fromClass", 4);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SaturdayNightFive").length()>1) {
                        addClass(ob.getString("SaturdayNightFive"), 6, 5);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SaturdayNightFive"));
                        cv.put("weekday", 6);
                        cv.put("fromClass", 5);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SaturdayNightSix").length()>1) {
                        addClass(ob.getString("SaturdayNightSix"), 6, 6);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SaturdayNightSix"));
                        cv.put("weekday", 6);
                        cv.put("fromClass", 6);
                        db_w.insert("schedule", null, cv);
                    }

                    if (ob.getString("SundayMorningOne").length()>1) {
                        addClass(ob.getString("SundayMorningOne"), 7, 1);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SundayMorningOne"));
                        cv.put("weekday", 7);
                        cv.put("fromClass", 1);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SundayMorningTwo").length()>1) {
                        addClass(ob.getString("SundayMorningTwo"), 7, 2);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SundayMorningTwo"));
                        cv.put("weekday", 7);
                        cv.put("fromClass", 2);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SundayAfternoonThree").length()>1) {
                        addClass(ob.getString("SundayAfternoonThree"), 7, 3);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SundayAfternoonThree"));
                        cv.put("weekday", 7);
                        cv.put("fromClass", 3);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SundayAfternoonFour").length()>1) {
                        addClass(ob.getString("SundayAfternoonFour"), 7, 4);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SundayAfternoonFour"));
                        cv.put("weekday", 7);
                        cv.put("fromClass", 4);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SundayNightFive").length()>1) {
                        addClass(ob.getString("SundayNightFive"), 7, 5);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SundayNightFive"));
                        cv.put("weekday", 7);
                        cv.put("fromClass", 5);
                        db_w.insert("schedule", null, cv);
                    }
                    if (ob.getString("SundayNightSix").length()>1) {
                        addClass(ob.getString("SundayNightSix"), 7, 6);
                        cv.clear();
                        cv.put("teacherID", teacherID);
                        cv.put("classInfo", ob.getString("SundayNightSix"));
                        cv.put("weekday", 7);
                        cv.put("fromClass", 6);
                        db_w.insert("schedule", null, cv);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    /**
     * 在表格中添加指定课程
     * @param classDetail
     * @param classWeekday
     * @param fromClass
     */
    public void addClass(String classDetail,int classWeekday,int fromClass){
        //五种颜色的背景
        int[] background = {R.drawable.course_info_blue, R.drawable.course_info_green,
                R.drawable.course_info_red, R.drawable.course_info_blue,
                R.drawable.course_info_yellow};
        // 添加课程信息
        TextView courseInfo = new TextView(this);
        courseInfo.setText(classDetail);
        //该textview的高度根据其节数的跨度来设置
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                aveWidth * 31 / 32,
                (gridHeight - 5) * 1 );
        //textview的位置由课程开始节数和上课的时间（day of week）确定
        rlp.topMargin = 5 + (fromClass - 1) * gridHeight;
        rlp.leftMargin = 1;
        // 偏移由这节课是星期几决定
        rlp.addRule(RelativeLayout.RIGHT_OF, classWeekday);
        //字体剧中
        courseInfo.setGravity(Gravity.CENTER);
        // 设置一种背景
        courseInfo.setBackgroundResource(background[(int)(1+Math.random()*(4-1+1))]);
        courseInfo.setTextSize(12);
        courseInfo.setLayoutParams(rlp);
        courseInfo.setTextColor(Color.WHITE);
        //设置不透明度
        courseInfo.getBackground().setAlpha(222);
        course_table_layout.addView(courseInfo);
    }




















    public List<String> getSelectedTeacherList(){
        List list = new ArrayList();
        //开启线程来发起网络请求
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                String sql="select DISTINCT(teacherID) from Schedule";
                Cursor cursor = db_r.rawQuery(sql,null);
                if (cursor.getCount()>1){
                    while (cursor.moveToNext()){
                        //list.add(cursor.getString(cursor.getColumnIndex("teacherID")));
                        //Log.i("robert",cursor.getString(cursor.getColumnIndex("teacherID")));
                        Cursor c = db_r.query("teacher",null,"teacherID=?",new String[]{cursor.getString(cursor.getColumnIndex("teacherID"))},null,null,null);
                        while (c.moveToNext()){
                            list.add(c.getString(c.getColumnIndex("teacherName")));
                        }
                    }
                }

//            }
//        }).start();
        return list;
    }






















































    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



}

