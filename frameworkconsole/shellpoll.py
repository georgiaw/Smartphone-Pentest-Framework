#!/usr/bin/python
version = "0.1.7"
import os
from os import system
import sys
import re
import serial
from time import sleep
import struct
import select
from lib.serial import read_modem
from lib.config import Config
from lib.db import DB, DBException
config = Config('config')

ipaddress = config.get('IPADDRESS')
webserver = config.get('WEBSERVER')
path = sys.argv[1]
port1 = sys.argv[2]
comm = sys.argv[3];
modem = sys.argv[4];
agentkey = sys.argv[5];
agentnumber = sys.argv[6];
sqlserver = config.get('MYSQLSERVER')

while True:
    fullpath5 = webserver + path + "/" + port1 + ".txt"
    PUTFILE = open(fullpath5, 'r+')
    line = PUTFILE.readline()
    PUTFILE.close()
    PUTFILE2 = open(fullpath5, 'w')
    PUTFILE2.close()
    catcommand = "cat " + fullpath5 + " | sed '1d' > hold1"
    system(catcommand)
    catcommand2 = "mv hold1 " + fullpath5
    system(catcommand)

    if  line == "" :
        pass    
    else :
        print line + "\n"
    
    if  select.select([sys.stdin,], [], [], 0.5)[0] :
        foo = sys.stdin().readline().strip()
        if  foo == "exit" :
            break
        
        elif  comm.lower == "sms" :
            command  = agentkey + " " + "PORT" + " " + port1 + " " + foo
            db = DB(config=config)

            db.query("SELECT path from modems where id=" + modem)
            path2     = db.fetchone()[0]
            db.query("SELECT controlkey from modems where id=" + modem)
            key2      = db.fetchone()[0]
            number2   = agentnumber
            db.query("SELECT type from modems where id=" + modem)
            modemtype2     = db.fetchone()[0]

            if  modemtype2 == "usb" :
                usb = serial.serialposix(port='/dev/ttyUSB2', baudrate=115200, bytesize=8, parity='N', stopbits=1)
                usb.write("ATZ\r\n")
                sleep(1)
                line = read_modem(usb)
                print line
                sleep(1)
                usb.write("AT+CMGF=1\r\n")
                line = read_modem(usb)
                print line
                sleep(1)
                numberline = "AT+CMGS=\"" + number2 + "\"\r\n"
                usb.write(numberline)
                line = read_modem(usb)
                print line
                sleep(1)
                usb.write( command + struct.pack('b', 26) )
                sleep(10)
                line = read_modem(usb)
                print line
                sleep(1)
                usb.close()

            
            elif  modemtype2 == "app" :
                command2 = key2 + " " + "SEND" + " " + number2 + " " + command
                control = webserver + path2 + "/getfunc"
                CONTROLFILE = open(control, 'w')
                CONTROLFILE.write(command2)
                CONTROLFILE.close()
            
        
        elif  comm.lower() == "http" :
            control = webserver + path + "/" + port1 + "control"
            CONTROLFILE = open(control, 'w')
            CONTROLFILE.write(foo)
            CONTROLFILE.close()
