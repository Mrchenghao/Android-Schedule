package com.example.robert.schedule;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


public class ChineseToEnglish {


    // 将汉字转换为全拼，,其他字符不变
    public static String getPinYin(String src) {
        src = src.replaceAll("（", "(").replaceAll("）", ")");
        char[] t1 = null;
        t1 = src.toCharArray();
        String[] t2 = new String[t1.length];
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        String t4 = "";
        int t0 = t1.length;
        try {
            for (int i = 0; i < t0; i++) {
// 判断是否为汉字字符
                if (java.lang.Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
                    t4 += t2[0];
                } else {
                    t4 += java.lang.Character.toString(t1[i]);
                }
            }
            return t4;
        } catch (BadHanyuPinyinOutputFormatCombination e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        return t4;
    }


    // 返回中文的首字母，英文字符不变
    public  String getPinYinFirst(String src) {
        String temp = "";
        String demo = "";
        String convert = "";
        for (int j = 0; j < src.length(); j++) {
            char word = src.charAt(j);
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert += pinyinArray[0].charAt(0);
            } else {
                convert += word;
            }
        }
        return convert;
// convert目前为小写首字母,下面是将小写首字母转化为大写
// for(int i=0;i<convert.length();i++){
// if(convert.charAt(i)>='a'&&convert.charAt(i)<='z'){
// temp=convert.substring(i, i+1).toUpperCase();
// demo+=temp;
// }
// }
    }


    // 将字符串转移为ASCII码
    public static String getCnASCII(String str) {
        StringBuffer sb = new StringBuffer();
        byte[] bGBK = str.getBytes();
        for (int i = 0; i < bGBK.length; i++) {
            sb.append(Integer.toHexString(bGBK[i] & 0xff));
        }
        return sb.toString();
    }



}
