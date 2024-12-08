# Geofication

An Android app that lets you create custom notifications triggered by your location to keep you organized wherever you go.

## Development

Android Studio has been used for development, with the SonarLint plugin installed.

If you have SonarQube installed, you can run a local scan with the command blow. You need to specify the ```sonar.token```. For more insights, you can run ```./gradlew lint``` first.

```
./gradlew sonar \
  -D "sonar.projectKey=Geofication-local" \
  -D "sonar.projectName='Geofication-local'" \
  -D "sonar.host.url=http://localhost:9000" \
  -D "sonar.token=token"
```

## Website

The website is hosted on Google Firebase. In order to use it, you have to login first (```firebase login```) and then deploy it (```firebase deploy```).

## Future

Features that are planned for the future:

- Widget that shows active Geofications
- Custom high precision mode
- Optional account feature for e.g. shared Geofications
- Set polygonial Geofences
- Sharable Geofications
- More settings for Geofications, e.g. active days  

## Current version

Version 1.0
