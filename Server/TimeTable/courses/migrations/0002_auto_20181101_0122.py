# -*- coding: utf-8 -*-
# Generated by Django 1.11.16 on 2018-10-31 17:22
from __future__ import unicode_literals

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('courses', '0001_initial'),
    ]

    operations = [
        migrations.AlterModelTable(
            name='course',
            table='course',
        ),
        migrations.AlterModelTable(
            name='teacher',
            table='teacher',
        ),
    ]
