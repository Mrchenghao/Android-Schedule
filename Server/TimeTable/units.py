# -*- coding: utf-8 -*-
import os,django
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "TimeTable.settings")
django.setup()
import requests
import re
import codecs
import chardet
from bs4 import BeautifulSoup
from courses.models import Teacher,Course
from PIL import Image
from io import BytesIO

def getVerificationCodeAndCookie():
    try:
        headers = {
            'Host': 'jw.zj-art.com',
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2',
            'Accept-Encoding': 'gzip, deflate',
            'Connection': 'keep-alive',
            'Referer': 'http://jw.zj-art.com/ZNPK/TeacherKBFB.aspx',
            'Upgrade-Insecure-Requests': '1',
            'Pragma': 'no-cache',
            'Cache-Control': 'no-cache',
        }
        r = requests.get('http://jw.zj-art.com/sys/ValidateCode.aspx', headers=headers)
        i = Image.open(BytesIO(r.content))
        i.save('static/code.jpg')
        Cookie = re.findall(r'ASP.*;', r.headers['Set-Cookie'])[0][:-1]
        print Cookie
        f = open('setting.py', 'w+')
        f.write(Cookie)
        f.close()
    except Exception, e:
        raise e
    finally:
        pass

def getAllTeachers():
    teachers = []
    Cookie = open('setting.py', 'r').read()
    headers = {
        'Host': 'jw.zj-art.com',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2',
        'Accept-Encoding': 'gzip, deflate',
        'Connection': 'keep-alive',
        'Referer': 'http://jw.zj-art.com/ZNPK/TeacherKBFB.aspx',
        'Cookie': Cookie,
        'Upgrade-Insecure-Requests': '1',
        'Pragma': 'no-cache',
        'Cache-Control': 'no-cache',
    }
    r = requests.get('http://jw.zj-art.com/ZNPK/Private/List_JS.aspx?xnxq=20180&t=174', headers=headers)
    html = '<html><body>' + re.findall(r'<select.*</select>', r.text.encode('utf-8'))[0] + '</body></html>'
    soup = BeautifulSoup(html, features="html.parser")
    teachersElements = soup.find_all('option')
    for teacherElement in teachersElements:
        if(teacherElement.text):
            teacher = {
                'teacherID': teacherElement['value'],
                'teacherName': teacherElement.text
            }
            teacherObj = Teacher(teacher_id = teacher['teacherID'], teacher_name = teacher['teacherName'])
            teacherObj.save()
            teachers.append(teacher)
    return teachers

def getCourseHtml(teacherID, verificationCode):
    courseHtml = ''
    try:
        data = {
            'Sel_XNXQ': '20180',
            'Sel_JS': teacherID,
            'type': '1',
            'txt_yzm': verificationCode
        }
        Cookie = open('setting.py', 'r').read()
        headers = {
            'Host': 'jw.zj-art.com',
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2',
            'Accept-Encoding': 'gzip, deflate',
            'Referer': 'http://jw.zj-art.com/ZNPK/TeacherKBFB.aspx',
            'Content-Type': 'application/x-www-form-urlencoded',
            'Content-Length': '49',
            'Connection': 'keep-alive',
            'Cookie': Cookie,
            'Upgrade-Insecure-Requests': '1',
            'Pragma': 'no-cache',
            'Cache-Control': 'no-cache',
        }
        url = 'http://jw.zj-art.com/ZNPK/TeacherKBFB_rpt.aspx'
        r = requests.post(url, data=data, headers=headers)
        courseHtml = r.text
    except Exception, e:
        raise e
    finally:
        return courseHtml

def analysisAndSaveCourse(courseHtml, teacherID):
    html = courseHtml
    teacherID = teacherID
    soup = BeautifulSoup(html, 'html.parser')
    courseTable = soup.find(bordercolorlight="#000000")
    if courseTable:
        morningOneTr = courseTable.contents[1]
        morningTwoTr = courseTable.contents[2]
        afternoonThreeTr = courseTable.contents[3]
        afternoonFourTr = courseTable.contents[4]
        nightFiveTr = courseTable.contents[5]
        nightSixTr = courseTable.contents[6]
        MondayMorningOne = morningOneTr.contents[2].text
        MondayMorningTwo = morningTwoTr.contents[1].text
        MondayAfternoonThree = afternoonThreeTr.contents[2].text
        MondayAfternoonFour = afternoonFourTr.contents[1].text
        MondayNightFive = nightFiveTr.contents[2].text
        MondayNightSix = nightSixTr.contents[1].text
        ThuesdayMorningOne = morningOneTr.contents[3].text
        ThuesdayMorningTwo = morningTwoTr.contents[2].text
        ThuesdayAfternoonThree = afternoonThreeTr.contents[3].text
        ThuesdayAfternoonFour = afternoonFourTr.contents[2].text
        ThuesdayNightFive = nightFiveTr.contents[3].text
        ThuesdayNightSix = nightSixTr.contents[2].text
        WednesdayMorningOne = morningOneTr.contents[4].text
        WednesdayMorningTwo = morningTwoTr.contents[3].text
        WednesdayAfternoonThree = afternoonThreeTr.contents[4].text
        WednesdayAfternoonFour = afternoonFourTr.contents[3].text
        WednesdayNightFive = nightFiveTr.contents[4].text
        WednesdayNightSix = nightSixTr.contents[3].text
        ThursdayMorningOne = morningOneTr.contents[5].text
        ThursdayMorningTwo = morningTwoTr.contents[4].text
        ThursdayAfternoonThree = afternoonThreeTr.contents[5].text
        ThursdayAfternoonFour = afternoonFourTr.contents[4].text
        ThursdayNightFive = nightFiveTr.contents[5].text
        ThursdayNightSix = nightSixTr.contents[4].text
        FridayMorningOne = morningOneTr.contents[6].text
        FridayMorningTwo = morningTwoTr.contents[5].text
        FridayAfternoonThree = afternoonThreeTr.contents[6].text
        FridayAfternoonFour = afternoonFourTr.contents[5].text
        FridayNightFive = nightFiveTr.contents[6].text
        FridayNightSix = nightSixTr.contents[5].text
        SaturdayMorningOne = morningOneTr.contents[7].text
        SaturdayMorningTwo = morningTwoTr.contents[6].text
        SaturdayAfternoonThree = afternoonThreeTr.contents[7].text
        SaturdayAfternoonFour = afternoonFourTr.contents[6].text
        SaturdayNightFive = nightFiveTr.contents[7].text
        SaturdayNightSix = nightSixTr.contents[6].text
        SundayMorningOne = morningOneTr.contents[8].text
        SundayMorningTwo = morningTwoTr.contents[7].text
        SundayAfternoonThree = afternoonThreeTr.contents[8].text
        SundayAfternoonFour = afternoonFourTr.contents[7].text
        SundayNightFive = nightFiveTr.contents[8].text
        SundayNightSix = nightSixTr.contents[7].text
    else:
        morningOneTr = ''
        morningTwoTr = ''
        afternoonThreeTr = ''
        afternoonFourTr = ''
        nightFiveTr = ''
        nightSixTr = ''
        MondayMorningOne = ''
        MondayMorningTwo = ''
        MondayAfternoonThree = ''
        MondayAfternoonFour = ''
        MondayNightFive = ''
        MondayNightSix = ''
        ThuesdayMorningOne = ''
        ThuesdayMorningTwo = ''
        ThuesdayAfternoonThree = ''
        ThuesdayAfternoonFour = ''
        ThuesdayNightFive = ''
        ThuesdayNightSix = ''
        WednesdayMorningOne = ''
        WednesdayMorningTwo = ''
        WednesdayAfternoonThree = ''
        WednesdayAfternoonFour = ''
        WednesdayNightFive = ''
        WednesdayNightSix = ''
        ThursdayMorningOne = ''
        ThursdayMorningTwo = ''
        ThursdayAfternoonThree = ''
        ThursdayAfternoonFour = ''
        ThursdayNightFive = ''
        ThursdayNightSix = ''
        FridayMorningOne = ''
        FridayMorningTwo = ''
        FridayAfternoonThree = ''
        FridayAfternoonFour = ''
        FridayNightFive = ''
        FridayNightSix = ''
        SaturdayMorningOne = ''
        SaturdayMorningTwo = ''
        SaturdayAfternoonThree = ''
        SaturdayAfternoonFour = ''
        SaturdayNightFive = ''
        SaturdayNightSix = ''
        SundayMorningOne = ''
        SundayMorningTwo = ''
        SundayAfternoonThree = ''
        SundayAfternoonFour = ''
        SundayNightFive = ''
        SundayNightSix = ''
    course = Course(
        teacher_id=Teacher.objects.filter(teacher_id=teacherID)[0],
        MondayMorningOne = MondayMorningOne,
        MondayMorningTwo = MondayMorningTwo,
        MondayAfternoonThree = MondayAfternoonThree,
        MondayAfternoonFour = MondayAfternoonFour,
        MondayNightFive = MondayNightFive,
        MondayNightSix = MondayNightSix,
        ThuesdayMorningOne = ThuesdayMorningOne,
        ThuesdayMorningTwo = ThuesdayMorningTwo,
        ThuesdayAfternoonThree = ThuesdayAfternoonThree,
        ThuesdayAfternoonFour = ThuesdayAfternoonFour,
        ThuesdayNightFive = ThuesdayNightFive,
        ThuesdayNightSix = ThuesdayNightSix,
        WednesdayMorningOne = WednesdayMorningOne,
        WednesdayMorningTwo = WednesdayMorningTwo,
        WednesdayAfternoonThree = WednesdayAfternoonThree,
        WednesdayAfternoonFour = WednesdayAfternoonFour,
        WednesdayNightFive = WednesdayNightFive,
        WednesdayNightSix = WednesdayNightSix,
        ThursdayMorningOne = ThursdayMorningOne,
        ThursdayMorningTwo = ThursdayMorningTwo,
        ThursdayAfternoonThree = ThursdayAfternoonThree,
        ThursdayAfternoonFour = ThursdayAfternoonFour,
        ThursdayNightFive = ThursdayNightFive,
        ThursdayNightSix = ThursdayNightSix,
        FridayMorningOne = FridayMorningOne,
        FridayMorningTwo = FridayMorningTwo,
        FridayAfternoonThree = FridayAfternoonThree,
        FridayAfternoonFour = FridayAfternoonFour,
        FridayNightFive = FridayNightFive,
        FridayNightSix = FridayNightSix,
        SaturdayMorningOne = SaturdayMorningOne,
        SaturdayMorningTwo = SaturdayMorningTwo,
        SaturdayAfternoonThree = SaturdayAfternoonThree,
        SaturdayAfternoonFour = SaturdayAfternoonFour,
        SaturdayNightFive = SaturdayNightFive,
        SaturdayNightSix = SaturdayNightSix,
        SundayMorningOne = SundayMorningOne,
        SundayMorningTwo = SundayMorningTwo,
        SundayAfternoonThree = SundayAfternoonThree,
        SundayAfternoonFour = SundayAfternoonFour,
        SundayNightFive = SundayNightFive,
        SundayNightSix = SundayNightSix,
    )
    course.save()

def getAndSaveCourse(teacherID, verificationCode):
    courseHtml = getCourseHtml(teacherID, verificationCode)
    if re.findall(r'验证码错误', courseHtml.encode('utf-8')):
        return False
    analysisAndSaveCourse(courseHtml, teacherID)
    return True

if __name__ == '__main__':
    # getTeachers()
    html = getAndSaveCourse('0000016', 'zxcv')
    print html