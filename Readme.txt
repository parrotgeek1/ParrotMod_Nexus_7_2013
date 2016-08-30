What is ParrotMod?
==================

This is a mod to greatly improve the touchscreen and multitasking performance of the 2013 Nexus 7, on ANY rooted ROM, and work around a hardware problem with the gyroscope/accelerometer (auto rotate).

(Got a 2012 Nexus 7? Use this version: http://forum.xda-developers.com/nexus-7/orig-development/parrotmod-speed-2012-nexus-7-emmc-fix-t3300416)

XDA thread: http://forum.xda-developers.com/nexus-7-2013/orig-development/beta-1-parrotmod-improve-2013-nexus-7-t3375928

Download: https://github.com/parrotgeek1/ParrotModFloApp/blob/master/app/app-release.apk?raw=true

GitHub: https://github.com/parrotgeek1/ParrotModFloApp

IMPORTANT NOTE
--------------

In order to recalibrate the screen when you switch from holding the tablet (your body acts as electrical ground) to not (ungrounded), try turning the screen off and on again.

Please disable IO scheduler tweaks in kernel apps. They override ParrotMod's meticulously optimized settings.

If ParrotMod does not improve the touchscreen at all, it could be that the touchscreen cable is loose inside the tablet. There are YouTube videos on how to fix this, like this popular one: https://www.youtube.com/watch?v=cOnccu6Nbl0

The feature fixing the auto rotate bug might not work for everyone. If it doesn't, here is a real hardware fix: https://www.youtube.com/watch?v=LLCAeBe2hVU

If you use multiple users on your tablet, ParrotMod's app icon will only show in the primary user. Enable ParrotMod in the primary user, and it will run even when you switch to another user.

Features:
=========

EXCLUSIVE: TOUCHSCREEN CALIBRATION FIX! You have to see it to believe it. No more ghost touches or missed taps! 
Noticeably better flash memory/filesystem speed, especially on ext4.
Fix for auto rotate failing after long standby periods
Slightly improved battery life
Fully compatible with all F2FS partitions
More apps open at once (low memory killer tweak, zram optimizations)
Enables 512MB of zram, with optimized settings (if the kernel supports it - stock does not)
Speed up full disk encryption

Thanks
------

Thanks to @nereis for showing me a zram tweak in the Nexus 7 2012 thread.

How ParrotMod Works
-------------------

The touchscreen is fixed by enabling a mode that is normally only used when HDMI is plugged in, that is more resistant to electrical interference, and calibrating the EMI detection at every boot and when the screen turns off.
I counteract the slow flash storage speed by decreasing unnecessary reads and writes, and also optimizing how well processes share the bandwidth.

"HELP! MY TOUCHSCREEN HAS STOPPED WORKING AFTER I INSTALLED THIS! YOU SUCK!!!one!1!!1eleven!!!1"
---------------------------------

Apparently, there is a VERY RARE incompatibility with some touchscreen firmwares which requires you to reset the firmware before installing ParrotMod. ONLY do this if your touchscreen stops working after installing it.

(You need fastboot & adb on your computer, Google it)

1) Plug the tablet into the computer
2) Turn on the tablet with power & volume down buttons pressed, to enter the bootloader
3) download the boot-ts.7z from http://forum.xda-developers.com/show....php?t=2428133 and extract it to a folder, open command prompt/Terminal, cd to that folder
4) run: fastboot boot boot-ts10-lock.img
5) your device will bootloop. just run "adb reboot bootloader" after about 30 seconds to get back to bootloader
4) run: fastboot boot boot-ts-unlock.img
5) your device will bootloop. just run "adb reboot" after about 30 seconds and Android should boot up with touch working