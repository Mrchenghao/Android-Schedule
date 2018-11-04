# -*- coding: utf-8 -*-
# Generated by Django 1.11.16 on 2018-10-31 17:19
from __future__ import unicode_literals

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Course',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('MondayMorningOne', models.CharField(max_length=256)),
                ('MondayMorningTwo', models.CharField(max_length=256)),
                ('MondayAfternoonThree', models.CharField(max_length=256)),
                ('MondayAfternoonFour', models.CharField(max_length=256)),
                ('MondayNightFive', models.CharField(max_length=256)),
                ('MondayNightSix', models.CharField(max_length=256)),
                ('ThuesdayMorningOne', models.CharField(max_length=256)),
                ('ThuesdayMorningTwo', models.CharField(max_length=256)),
                ('ThuesdayAfternoonThree', models.CharField(max_length=256)),
                ('ThuesdayAfternoonFour', models.CharField(max_length=256)),
                ('ThuesdayNightFive', models.CharField(max_length=256)),
                ('ThuesdayNightSix', models.CharField(max_length=256)),
                ('WednesdayMorningOne', models.CharField(max_length=256)),
                ('WednesdayMorningTwo', models.CharField(max_length=256)),
                ('WednesdayAfternoonThree', models.CharField(max_length=256)),
                ('WednesdayAfternoonFour', models.CharField(max_length=256)),
                ('WednesdayNightFive', models.CharField(max_length=256)),
                ('WednesdayNightSix', models.CharField(max_length=256)),
                ('ThursdayMorningOne', models.CharField(max_length=256)),
                ('ThursdayMorningTwo', models.CharField(max_length=256)),
                ('ThursdayAfternoonThree', models.CharField(max_length=256)),
                ('ThursdayAfternoonFour', models.CharField(max_length=256)),
                ('ThursdayNightFive', models.CharField(max_length=256)),
                ('ThursdayNightSix', models.CharField(max_length=256)),
                ('FridayMorningOne', models.CharField(max_length=256)),
                ('FridayMorningTwo', models.CharField(max_length=256)),
                ('FridayAfternoonThree', models.CharField(max_length=256)),
                ('FridayAfternoonFour', models.CharField(max_length=256)),
                ('FridayNightFive', models.CharField(max_length=256)),
                ('FridayNightSix', models.CharField(max_length=256)),
                ('SaturdayMorningOne', models.CharField(max_length=256)),
                ('SaturdayMorningTwo', models.CharField(max_length=256)),
                ('SaturdayAfternoonThree', models.CharField(max_length=256)),
                ('SaturdayAfternoonFour', models.CharField(max_length=256)),
                ('SaturdayNightFive', models.CharField(max_length=256)),
                ('SaturdayNightSix', models.CharField(max_length=256)),
                ('SundayMorningOne', models.CharField(max_length=256)),
                ('SundayMorningTwo', models.CharField(max_length=256)),
                ('SundayAfternoonThree', models.CharField(max_length=256)),
                ('SundayAfternoonFour', models.CharField(max_length=256)),
                ('SundayNightFive', models.CharField(max_length=256)),
                ('SundayNightSix', models.CharField(max_length=256)),
            ],
        ),
        migrations.CreateModel(
            name='Teacher',
            fields=[
                ('teacher_id', models.CharField(max_length=11, primary_key=True, serialize=False)),
                ('teacher_name', models.CharField(max_length=10, unique=True)),
            ],
        ),
        migrations.AddField(
            model_name='course',
            name='teacher_id',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='courses.Teacher'),
        ),
    ]
