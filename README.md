# Location Tracker
An android app that captures user's current location using phone GPS.

### Prerequisites
Android 4.4(KITKAT) or higher
   
### Installation &  Usage
1. To install, click on the .apk file and follow the instructions.
2. Make sure phone GPS is enabled while using the app.
3. Open the app, click on "Start Service" button. It will request user to grant permissions to access location services and to create        files in phone storage, especially if phone has Android 6.0 or higher, grant them.

### Location Tracking
Location tracking is implemented using a handler to set time intervals between two data points.
First, location request is made and this request returns multiple data points. Not all data points are valid.
Once a valid data point is received the requests are stopped and data is saved to a text file in phone storage. The handler is then used to delay the next request by a specific time interval.
The time interval depends on the current mode, either Active(2 minutes) or Idle(30 minutes).

Since updates provided by network provider are not valid, only GPS provider is used to request updates.

System status data is written to a separate file by using a timer, which repeats the task every 10 minutes.
   
### Libraries used
joda-time-2.2 : This library comes in very handy with date related operations, it provides a wide range of methods which the default android Date class lacks.
