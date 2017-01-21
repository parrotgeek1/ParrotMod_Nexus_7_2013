What is ParrotMod?
==================

This is a mod to greatly improve the touchscreen performance of the 2013 Nexus 7, on ANY rooted ROM, and work around a hardware problem with the gyroscope/accelerometer (auto rotate).

Updated 1/21/2017 to remove the problematic kernel tweaks.

Download: https://github.com/parrotgeek1/ParrotModFloApp/blob/master/app/app-release.apk?raw=true

IMPORTANT NOTE
--------------

In order to recalibrate the screen when you switch from holding the tablet (your body acts as electrical ground) to not (ungrounded), try turning the screen off and on again.

If ParrotMod does not improve the touchscreen at all, it could be that the touchscreen cable is loose inside the tablet. There are YouTube videos on how to fix this, like this popular one: https://www.youtube.com/watch?v=cOnccu6Nbl0

The feature fixing the auto rotate bug might not work for everyone. If it doesn't, here is a real hardware fix: https://www.youtube.com/watch?v=LLCAeBe2hVU

If you use multiple users on your tablet, ParrotMod's app icon will only show in the primary user. Enable ParrotMod in the primary user, and it will run even when you switch to another user.

How ParrotMod Works
-------------------

The touchscreen is fixed by enabling a mode that is normally only used when HDMI is plugged in, that is more resistant to electrical interference, and calibrating the EMI detection at every boot and when the screen turns off.

The auto rotate is fixed by restarting the sensor daemon every time it loses connection with the i2c device (due to a loose cable).