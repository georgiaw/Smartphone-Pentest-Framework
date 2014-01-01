#!/usr/bin/perl
use DBI;
use Cwd;
use Expect;
use IO::Socket;
use Device::SerialPort;
use IO::Select;
$configfile = "config";
 open(CONFIG, "+<$configfile");
while (<CONFIG>)
{
        chomp;
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        ($var, $value) = split(/\s*=\s*/, $_, 2);
        $Variables{$var} = ${value};
}
$Variables{"OS"} = $^O;
$ipaddress = $Variables{"IPADDRESS"};
$webserver = $Variables{"WEBSERVER"};
$path = $ARGV[0];
$port1 = $ARGV[1];
$comm = $ARGV[2];
$modem = $ARGV[3];
$agentkey = $ARGV[4];
$agentnumber = $ARGV[5];
$sqlserver = $Variables{"MYSQLSERVER"};
$s = IO::Select->new();
$s->add(\*STDIN);
while(1)
{
$fullpath5 = $webserver . $path . "/" . $port1 . ".txt";
        open(PUTFILE, "+<$fullpath5");
        $line= <PUTFILE>;
        close(PUTFILE);
        open(PUTFILE2, ">$fullpath5");
        print PUTFILE2;
        close(PUTFILE2);
        $catcommand = "cat " . $fullpath5 . " | sed '1d' > hold1";
        system($catcommand);
        $catcommand2 = "mv hold1 " . $fullpath;
        system($catcommand);
        if ($line eq "")
        {
        }
        else {
        print $line . "\n";
        }
        if ($s->can_read(.5)) {
       chomp($foo = <STDIN>);
        if ($foo eq "exit")
        {
                last;
        }
        elsif (lc($comm) eq "sms")
        {                
                        $command = $agentkey . " " . "PORT" . " " . $port1 . " " . $foo;
                                $username = $Variables{"MYSQLUSER"};
                                $password = $Variables{"MYSQLPASS"};
                                $port = $Variables{"MYSQLPORT"};
                                $type = $Variables{"DATABASETYPE"};
                                if ($type eq "postgres")
                                {
                                        $dbh = DBI->connect("DBI:Pg:dbname=framework;host=$sqlserver;port=$port",$username,$password);
                                }
                                  elsif ($type eq "mysql")
                                {     
                                          $dbh = DBI->connect("dbi:mysql:database=framework;host=$sqlserver;port=$port", $username,$password);
                                }
                                       $pathquery = "SELECT path from modems where id=" . $modem;
                                       $sql = $dbh->prepare($pathquery);
                                $results = $sql->execute;
                                @rows = $sql->fetchrow_array();
                                $path2 = @rows[0];
                                $keyquery = "SELECT controlkey from modems where id=" . $modem;
                                $sql = $dbh->prepare($keyquery);
                                $results2 = $sql->execute;
                                @rows = $sql->fetchrow_array();
                                $key2 = @rows[0];
                                $number2 = $agentnumber;
                        $modemtypequery = "SELECT type from modems where id=" . $modem;
                                $sql = $dbh->prepare($modemtypequery);
                                $results3 = $sql->execute;
                                @rows = $sql->fetchrow_array();
                                $modemtype2 = @rows[0];
                                if ($modemtype2 eq "usb")
                                {
                                        $usb = Device::SerialPort->new("/dev/ttyUSB2");
                                        $usb->databits(8);
                                        $usb->baudrate(115200);
                                        $usb->parity("none");
                                        $usb->stopbits(1);
                                        $usb->write("ATZ\r\n");
                                        sleep(1);
                                        $line = read_modem($usb);
                                        print $line;
                                        sleep(1);
                                        $usb->write("AT+CMGF=1\r\n");
                                        $line = read_modem($usb);
                                        print $line;
                                        sleep(1);
                                        $numberline = "AT+CMGS=\"" . $number2 . "\"\r\n";
                                        $usb->write($numberline);
                                        $line = read_modem($usb);
                                        print $line;
                                        sleep(1);        
                                        $usb->write($command.pack('c',26));
                                        sleep(10);
                                        $line = read_modem($usb);
                                        print $line;
                                        sleep(1);
                                        $usb->close();

                                }
                                elsif ($modemtype2 eq "app") 
                                {
                                        $command2 = $key2 . " " . "SEND" . " " . $number2 . " " . $command;
                                        $control = $webserver . $path2 . "/getfunc";
                                        open(CONTROLFILE, ">$control");
                                        print CONTROLFILE $command2;
                                        close(CONTROLFILE);
                                }        
        }
        elsif(lc($comm) eq "http")
        {
             $control = $webserver . $path . "/" . $port1 . "control";
        open(CONTROLFILE, ">$control");
        print CONTROLFILE $foo;
        close(CONTROLFILE);
        }
        
  }
}
        
