What is ParrotMod?
==================

This is a mod to greatly improve the touchscreen and multitasking performance of the 2013 Nexus 7, on ANY rooted ROM, and work around a hardware problem with the gyroscope/accelerometer (auto rotate).

(Got a 2012 Nexus 7? Use this version: http://forum.xda-developers.com/nexus-7/orig-development/parrotmod-speed-2012-nexus-7-emmc-fix-t3300416)

XDA thread: http://forum.xda-developers.com/nexus-7-2013/orig-development/beta-1-parrotmod-improve-2013-nexus-7-t3375928

Download: http://download.parrotgeek.com/android/ParrotMod_Flo

GitHub: https://github.com/parrotgeek1/ParrotModFloApp

IMPORTANT NOTE
--------------

To properly calibrate the touchscreen after you plug in or unplug a charger, you need to turn off the screen for 2 seconds and turn it on again. I am going to fix this.

In order to recalibrate the screen when you switch from holding the tablet (your body acts as electrical ground) to not (ungrounded), try turning the screen off and on again.

Please disable IO scheduler tweaks in kernel apps. They override ParrotMod's meticulously optimized settings.

If ParrotMod does not improve the touchscreen at all, it could be that the actual touchscreen cable is loose inside the tablet. There are YouTube videos on how to fix this, like this popular one: https://www.youtube.com/watch?v=cOnccu6Nbl0

The feature fixing the auto rotate bug might not work for everyone. If it doesn't, here is a real hardware fix: https://www.youtube.com/watch?v=LLCAeBe2hVU

Features:
=========

EXCLUSIVE: TOUCHSCREEN CALIBRATION FIX! You have to see it to believe it. No more ghost touches or missed taps! 
Noticeably better flash memory/filesystem speed, especially on ext4.
Slightly improved battery life
Fully compatible with all F2FS partitions
More apps open at once (low memory killer tweak)
Speed up full disk encryption

Known Bugs
----------

F2FS optimizations are missing, mostly because I don't want to wipe data to test them. Performance should still be fine though.

Thanks
------

Thanks to @nereis for showing me a zram tweak in the Nexus 7 2012 thread.

How ParrotMod Works
-------------------

The touchscreen is fixed by enabling a mode that is normally only used when HDMI is plugged in, that is more resistant to electrical interference, and calibrating the EMI detection at every boot and when the screen turns off.
I counteract the slow flash storage speed by decreasing unnecessary reads and writes, and also optimizing how well processes share the bandwidth.