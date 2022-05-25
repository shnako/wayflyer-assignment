# Wayflyer Billing

This is Vlad Schnakovszki's submission for the Wayflyer take-home assignment.

The assignment task is available [here](TASK.md).

## Running

You need to have Java >= 11 installed in order to run this application. Details on how to install it are available [here](https://www.wikihow.com/Install-the-Java-Software-Development-Kit).

### Terminal

A [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) is bundled with the application, so you don't need to have it installed.

You can run the application by running the following command from your favourite terminal:

`./gradlew bootRun`

or

`gradlew.bat bootRun`

if running via Windows' cmd.

### IntelliJ

#### Lombok
This project uses Lombok to handle POJO boilerplate.

In order to compile from an IDE, you'll need to install the Lombok plugin otherwise you'll get errors in the model classes. Details on how to do this are available [here](https://www.baeldung.com/lombok-ide).

#### Running

Open the Gradle side menu and double click assessment > Tasks > application > bootRun. No other configuration needed.


## Improvements
Below are some suggestions to improve the API we interact with during this assessment.

### Wrong Content-Type
The endpoints return JSON but set the Content-Type to `text/html; charset=utf-8`.
This is interfering with REST frameworks (e.g. need to read as String and then parse it to object in Java, no formatting in Postman).

The endpoints should return Content-Type `application/json`.

This might have been introduced to support error messages not being sent as JSON but rather as text.
These should also either be sent as JSON objects or the Content-Type specified as `text/plain`.

### Use of unofficial response codes
The endpoints return `530` on failures. This is not an official HTTP status code so REST frameworks can't handle it properly out of the box.

Since a response code matching the errors doesn't exist, it would be better to return `500 Internal Server Error` instead. 