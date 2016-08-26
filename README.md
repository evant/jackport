# Jackport

Backporting java 8 api's to older versions of android using the jack compiler.

## Work In Progress!

1. Build in install a fork of the android gradle pluigin https://github.com/evant/android-gradle-jack-plugin.
This should not be needed in the future when this is offically implemented.
2. Set android.gradle.plugin.path to where you installed the plugin
3. Run
```
./gradlew jar
./gradlew installDebug
```
4. Profit!
