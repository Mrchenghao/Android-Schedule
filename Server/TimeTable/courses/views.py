# -*- coding: utf-8 -*-
from __future__ import unicode_literals
import sys
sys.path.append('../')
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from models import Teacher, Course
from units import *
import os
import json
import string

def getTeachers(request):
    try:
        teachersList = []
        allTeachers = None
        allTeachers = Teacher.objects.all()
        if not allTeachers:
            allTeachers = getAllTeachers()
        for teacher in allTeachers:
            teachersList.append({
                    'teacherID': teacher.teacher_id,
                    'teacherName': teacher.teacher_name
                })
        result = {
            'successful': True,
            'data': teachersList
        }
    except Exception, e:
        result = {
            'successful': False
        }
    finally:
        return HttpResponse(json.dumps(result), content_type='application/json')

def getVerificationCode(request):
    try:
        path = os.getcwd()
        codeSrc = path + '/code.jpg'
        result = {
            'successful': True,
            'data': codeSrc
        }
    except Exception, e:
        result = {
        'successful': False
        }
    finally:
        return HttpResponse(json.dumps(result), content_type='application/json')

def getCourseByTeacher(request):
    try:
        data = json.loads(request.body)
        teacherID = data['teacherID']
        verificationCode = data['verificationCode']
        teacher = Teacher.objects.get(teacher_id=teacherID)
        courseSet = Course.objects.filter(teacher_id=teacher)
        if courseSet:
            course = courseSet[0]
            result = {
                'successful': True,
                'data': {
                    'teacherID': teacherID,
                    'MondayMorningOne': course.MondayMorningOne,
                    'MondayMorningTwo': course.MondayMorningTwo,
                    'MondayAfternoonThree': course.MondayAfternoonThree,
                    'MondayAfternoonFour': course.MondayAfternoonFour,
                    'MondayNightFive': course.MondayNightFive,
                    'MondayNightSix': course.MondayNightSix,
                    'ThuesdayMorningOne': course.ThuesdayMorningOne,
                    'ThuesdayMorningTwo': course.ThuesdayMorningTwo,
                    'ThuesdayAfternoonThree': course.ThuesdayAfternoonThree,
                    'ThuesdayAfternoonFour': course.ThuesdayAfternoonFour,
                    'ThuesdayNightFive': course.ThuesdayNightFive,
                    'ThuesdayNightSix': course.ThuesdayNightSix,
                    'WednesdayMorningOne': course.WednesdayMorningOne,
                    'WednesdayMorningTwo': course.WednesdayMorningTwo,
                    'WednesdayAfternoonThree': course.WednesdayAfternoonThree,
                    'WednesdayAfternoonFour': course.WednesdayAfternoonFour,
                    'WednesdayNightFive': course.WednesdayNightFive,
                    'WednesdayNightSix': course.WednesdayNightSix,
                    'ThursdayMorningOne': course.ThursdayMorningOne,
                    'ThursdayMorningTwo': course.ThursdayMorningTwo,
                    'ThursdayAfternoonThree': course.ThursdayAfternoonThree,
                    'ThursdayAfternoonFour': course.ThursdayAfternoonFour,
                    'ThursdayNightFive': course.ThursdayNightFive,
                    'ThursdayNightSix': course.ThursdayNightSix,
                    'FridayMorningOne': course.FridayMorningOne,
                    'FridayMorningTwo': course.FridayMorningTwo,
                    'FridayAfternoonThree': course.FridayAfternoonThree,
                    'FridayAfternoonFour': course.FridayAfternoonFour,
                    'FridayNightFive': course.FridayNightFive,
                    'FridayNightSix': course.FridayNightSix,
                    'SaturdayMorningOne': course.SaturdayMorningOne,
                    'SaturdayMorningTwo': course.SaturdayMorningTwo,
                    'SaturdayAfternoonThree': course.SaturdayAfternoonThree,
                    'SaturdayAfternoonFour': course.SaturdayAfternoonFour,
                    'SaturdayNightFive': course.SaturdayNightFive,
                    'SaturdayNightSix': course.SaturdayNightSix,
                    'SundayMorningOne': course.SundayMorningOne,
                    'SundayMorningTwo': course.SundayMorningTwo,
                    'SundayAfternoonThree': course.SundayAfternoonThree,
                    'SundayAfternoonFour': course.SundayAfternoonFour,
                    'SundayNightFive': course.SundayNightFive,
                    'SundayNightSix': course.SundayNightSix,
                }
            }
        else:
            status = getAndSaveCourse(teacherID, verificationCode)
            if status:
                course = Course.objects.filter(teacher_id=teacherID)[0]
                result = {
                    'successful': True,
                    'data': {
                        'teacherID': teacherID,
                        'MondayMorningOne': course.MondayMorningOne,
                        'MondayMorningTwo': course.MondayMorningTwo,
                        'MondayAfternoonThree': course.MondayAfternoonThree,
                        'MondayAfternoonFour': course.MondayAfternoonFour,
                        'MondayNightFive': course.MondayNightFive,
                        'MondayNightSix': course.MondayNightSix,
                        'ThuesdayMorningOne': course.ThuesdayMorningOne,
                        'ThuesdayMorningTwo': course.ThuesdayMorningTwo,
                        'ThuesdayAfternoonThree': course.ThuesdayAfternoonThree,
                        'ThuesdayAfternoonFour': course.ThuesdayAfternoonFour,
                        'ThuesdayNightFive': course.ThuesdayNightFive,
                        'ThuesdayNightSix': course.ThuesdayNightSix,
                        'WednesdayMorningOne': course.WednesdayMorningOne,
                        'WednesdayMorningTwo': course.WednesdayMorningTwo,
                        'WednesdayAfternoonThree': course.WednesdayAfternoonThree,
                        'WednesdayAfternoonFour': course.WednesdayAfternoonFour,
                        'WednesdayNightFive': course.WednesdayNightFive,
                        'WednesdayNightSix': course.WednesdayNightSix,
                        'ThursdayMorningOne': course.ThursdayMorningOne,
                        'ThursdayMorningTwo': course.ThursdayMorningTwo,
                        'ThursdayAfternoonThree': course.ThursdayAfternoonThree,
                        'ThursdayAfternoonFour': course.ThursdayAfternoonFour,
                        'ThursdayNightFive': course.ThursdayNightFive,
                        'ThursdayNightSix': course.ThursdayNightSix,
                        'FridayMorningOne': course.FridayMorningOne,
                        'FridayMorningTwo': course.FridayMorningTwo,
                        'FridayAfternoonThree': course.FridayAfternoonThree,
                        'FridayAfternoonFour': course.FridayAfternoonFour,
                        'FridayNightFive': course.FridayNightFive,
                        'FridayNightSix': course.FridayNightSix,
                        'SaturdayMorningOne': course.SaturdayMorningOne,
                        'SaturdayMorningTwo': course.SaturdayMorningTwo,
                        'SaturdayAfternoonThree': course.SaturdayAfternoonThree,
                        'SaturdayAfternoonFour': course.SaturdayAfternoonFour,
                        'SaturdayNightFive': course.SaturdayNightFive,
                        'SaturdayNightSix': course.SaturdayNightSix,
                        'SundayMorningOne': course.SundayMorningOne,
                        'SundayMorningTwo': course.SundayMorningTwo,
                        'SundayAfternoonThree': course.SundayAfternoonThree,
                        'SundayAfternoonFour': course.SundayAfternoonFour,
                        'SundayNightFive': course.SundayNightFive,
                        'SundayNightSix': course.SundayNightSix,
                    }
                }
            else:
                getVerificationCodeAndCookie()
                result = {
                    'successful':False
                }
    except Exception, e:
        getVerificationCodeAndCookie()
        result = {
            'successful':False
        }
    finally:
        return HttpResponse(json.dumps(result), content_type='application/json')
