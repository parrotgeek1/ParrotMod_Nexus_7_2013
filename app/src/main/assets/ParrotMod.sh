#!/system/bin/sh

# stop this script from being killed

mypid=$$
echo "-1000" > /proc/$mypid/oom_score_adj

olddir="$(pwd)"
emicb="$(dirname "$0")/emi_config.bin"

# ram optimize atlas service

read buildfp type width height count flags < /data/system/framework_atlas.config

echo "$buildfp" > /data/system/framework_atlas.config
echo "$type" >> /data/system/framework_atlas.config
echo 768 >> /data/system/framework_atlas.config
echo 768 >> /data/system/framework_atlas.config
echo "$count" >> /data/system/framework_atlas.config
echo "$flags" >> /data/system/framework_atlas.config

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
rm /data/system/last_fstrim # force trim at next idle

cd /sys/block/mmcblk0/queue
echo 512 > nr_requests # don't clog the pipes
echo 0 > add_random # don't contribute to entropy, it reads randomly in background
echo 2 > rq_affinity # moving cpus is "expensive"

# https://www.kernel.org/doc/Documentation/block/cfq-iosched.txt

echo cfq > scheduler
echo 0 > iosched/slice_idle # never idle WITHIN groups
echo 10 > iosched/group_idle # BUT make sure there is differentiation between cgroups
echo 1 > iosched/back_seek_penalty # no penalty
echo 16 > iosched/quantum # default 8. Removes bottleneck
echo 4 > iosched/slice_async_rq # default 2. See above
echo 2147483647 > iosched/back_seek_max # i.e. the whole disk

# fs tune

for m in /data /realdata /cache /system ; do
	test ! -e $m && continue
	mount | grep "$m" | grep -q ext4 && mount -t ext4 -o remount,noauto_da_alloc,journal_async_commit,journal_ioprio=7,barrier=0,dioread_nolock "$m" "$m"
	mount | grep "$m" | grep -q f2fs && mount -t f2fs -o remount,nobarrier,flush_merge,inline_xattr,inline_data,inline_dentry "$m" "$m"
done

for f in /sys/fs/ext4/*; do
	test "$f" = "/sys/fs/ext4/features" && continue
	echo 8 > ${f}/max_writeback_mb_bump # don't spend too long writing ONE file if multiple need to write
done

if test -e "/sys/block/dm-0/queue"; then # encrypted
	cd /sys/block/dm-0/queue
	echo 0 > add_random # don't contribute to entropy
	echo 2 > rq_affinity # moving cpus is "expensive"
fi

echo 60 > /proc/sys/vm/swappiness # for some reason, 0 is default on flo, which messes up zram
echo 0 > /proc/sys/vm/page-cluster # zram is not a disk with a sector size, can swap 1 page at once

cd "$olddir"

# postboot calibration

calib() {
    pwr=$(cat /sys/devices/i2c-3/3-0010/power/control)
    echo on > /sys/devices/i2c-3/3-0010/power/control
    echo ff > /proc/ektf_dbg
    sleep 1
    echo $pwr > /sys/devices/i2c-3/3-0010/power/control
}

while true; do
    cat /sys/power/wait_for_fb_wake
    cat "$emicb" > /dev/elan-iap
    cat /sys/power/wait_for_fb_sleep
    sleep 2
    test "$(cat /sys/power/wait_for_fb_status)" != "on" && calib
done
