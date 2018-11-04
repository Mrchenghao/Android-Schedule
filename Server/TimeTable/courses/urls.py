from django.contrib import admin
from django.conf.urls import *
import views

urlpatterns = [
    url(r'^getTeachers$', views.getTeachers),
    url(r'^getVerificationCode$', views.getVerificationCode),
    url(r'^getCourseByTeacher$', views.getCourseByTeacher),
]