#!/system/bin/sh

# stop this script from being killed

mypid=$$
echo "-1000" > /proc/$mypid/oom_score_adj

olddir="$(pwd)"

# ram tuning

# these are from Intel's recommendation for 2GB/xhdpi tablet devices
# https://01.org/android-ia/user-guides/android-memory-tuning-android-5.0-and-5.1
setprop dalvik.vm.heapstartsize 16m
setprop dalvik.vm.heapgrowthlimit 200m
setprop dalvik.vm.heapsize 348m
setprop dalvik.vm.heaptargetutilization 0.75
setprop dalvik.vm.heapminfree 512k
setprop dalvik.vm.heapmaxfree 8m

echo 48 > /sys/module/lowmemorykiller/parameters/cost # default 32

echo 1 > /proc/sys/vm/highmem_is_dirtyable # allow LMK to free more ram

settings put global fstrim_mandatory_interval 86400000 # 1 day
settings put global storage_benchmark_interval 9223372036854775807 # effectively, never

cd /sys/block/mmcblk0/queue
echo 512 > nr_requests # don't clog the pipes
echo 0 > add_random # don't contribute to entropy, it reads randomly in background
echo 2 > rq_affinity # moving cpus is "expensive"

grep -Fq 'row' scheduler && echo row > scheduler # prefer row

if grep -Fq '[cfq]' scheduler ; then
# https://www.kernel.org/doc/Documentation/block/cfq-iosched.txt
	echo 0 > iosched/slice_idle # never idle WITHIN groups
	echo 10 > iosched/group_idle # BUT make sure there is differentiation between cgroups
	echo 1 > iosched/back_seek_penalty # no penalty
	echo 16 > iosched/quantum # default 8. Removes bottleneck
	echo 4 > iosched/slice_async_rq # default 2. See above
	echo 2147483647 > iosched/back_seek_max # i.e. the whole disk
fi

cd "$olddir"

# fs tune

for m in /data /realdata /cache /system ; do
	test ! -e $m && continue
	mount | grep "$m" | grep -q ext4 && mount -t ext4 -o remount,noauto_da_alloc,journal_async_commit,journal_ioprio=7,barrier=0,dioread_nolock "$m" "$m"
	mount | grep "$m" | grep -q f2fs && mount -t f2fs -o remount,nobarrier,flush_merge,inline_xattr,inline_data,inline_dentry "$m" "$m"
done

(for f in /sys/fs/ext4/*; do
	test "$f" = "/sys/fs/ext4/features" && continue
	echo 8 > ${f}/max_writeback_mb_bump # don't spend too long writing ONE file if multiple need to write
done) 2>/dev/null

for f in /sys/block/*; do
	echo 0 > "${f}/queue/add_random" 2>/dev/null # don't contribute to entropy
done

if test -e /sys/block/dm-0; then
    for f in /sys/block/dm-*; do # encrypted filesystems
    	echo 2 > "${f}/queue/rq_affinity" # moving cpus is "expensive"
    	echo 0 > "${f}/queue/rotational"
    done
fi

if test -e /sys/block/loop0; then
    for f in /sys/block/loop*; do # loopback, like multirom USB boot
	    echo none > "${f}/queue/scheduler"
    	echo 0 > "${f}/queue/read_ahead_kb" # because there is already readahead on the USB device
	    echo 0 > "${f}/queue/rotational"
    done
fi

echo 40 > /proc/sys/vm/swappiness # 60 default is too high especially with lmk cost changed
echo 0 > /proc/sys/vm/page-cluster # zram is not a disk with a sector size, can swap 1 page at once

if test -e /sys/block/zram0; then
	swapoff /dev/block/zram0 >/dev/null 2>&1
	echo 1 > /sys/block/zram0/reset
	test -e /sys/block/zram0/max_comp_streams && echo 2 > /sys/block/zram0/max_comp_streams # half the number of cores
	test -e /sys/block/zram0/comp_algorithm && echo lz4 > /sys/block/zram0/comp_algorithm # it's faster than lzo but some kernels don't have it
	echo 536870912 > /sys/block/zram0/disksize # 512mb
	mkswap /dev/block/zram0
	swapon -p 32767 /dev/block/zram0 # highest possible priority
fi

