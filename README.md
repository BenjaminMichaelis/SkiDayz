# SkiDayz

## Authors

- [Benjamin Michaelis](https://github.com/BenjaminMichaelis)
- [Anna Ueti](https://github.com/aueti)

## Goal

The goal of this app is likely only helpful to a limited number of people including myself, but I wanted to quickly be able to tell what darkness of the goggle lenses I need when skiing (I have a few options) so I can choose the correct one for when I am heading out, and if I need to wear sunscreen and how strong (snow is deceivingly bright and causes sunburns I have learned the hard way).

## Demo

Here is a video demoing the application. (One video in the folder is webm format directly from the phone, one I converted to mp4). [https://michaelises-my.sharepoint.com/:f:/g/personal/benjamin_michaelis_net/EqyWLDhcOkdBj_oOIIQEdCcBAo8Pnl4lnEHV7fMi6n0KrA?e=zYGqDz](https://michaelises-my.sharepoint.com/:f:/g/personal/benjamin_michaelis_net/EqyWLDhcOkdBj_oOIIQEdCcBAo8Pnl4lnEHV7fMi6n0KrA?e=zYGqDz)

## Features

- Gets the current location of the device using the onboard sensors
- Displays a dialog to request permissions if they are not already granted to the application
- Uses google map to show the current location, or you can select a location on the map to drop a marker to have a new location selected.
- Utilizes the Weatherbit.io api calls to get the current weather at a given location
- Gives recommendations of the goggle lenses to use based off of the weather, as well as a quick current weather overview with applicable fields for weather, and a weather icon determined by the api.

## What You Will Need

- [Android Studio](https://developer.android.com/studio)
- An emulator with an API of 33 or at least above 30 (otherwise there are play store issues with Google Maps).
- Location services enabled on your emulator (a permission dialog should pop up to ask, but if it's not accepted, you'll have to just use the google map to select your location)
- Api key from [Weatherbit](https://www.weatherbit.io/)
- Api key for Google Maps

## Startup Steps

1. Clone Repo/Unzip project
2. Make sure that there is an api key for Weather Bit in the `weatherBitApiKey` under MainActivity.kt and an api key for google maps in android:value under `<meta-data` in the AndroidManifest.xml
3. Make sure all Google Play SDK tools enabled (On Android Studio: Tools -> SDK Manager -> SDK Tools), there should be ~4 of them to select and install.
4. Select your Emulator (We have found some of the experimental emulators like the freeform are sometimes a bit weird with current location, but the pixel ones work better)
5. Launch the App!

## Issues

- I have recommended a fresh emulator device as depending on location permission on the emulator, sometimes the current location in the emulator isn't being obtained properly. You can use the google map still however to select your location in this case.
- As shown in the demo video, the location may show unknown for a few seconds as system obtains the current location. The map is still useable in this case, and current location will work when it is obtained from the system. The default location for android emulators is Mountain View, CA, but this can be adjusted in the emulator settings.
- If you find other issues, please open an issue or reach out to me.
