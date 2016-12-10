# vexscouter
A drafting tool for the Vex Robotics Competition.

When building for release, be sure to add a `keystore.properties` file to the root of the project containing the following information in order to sign the release APK:
```
storePassword=YourKeyStorePassword
keyPassword=YourKeyPassword
keyAlias=YourKeyAlias
storeFile=PathToYourStoreFile
```
The build is already configured to sign the APK automatically using this configuration. For more information, see [Sign Your App](https://developer.android.com/studio/publish/app-signing.html).
