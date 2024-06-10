# TdB Android Application - README

## I - Introduction

The "TdB" Android application is developed using Android Studio in Java and XML. This application consists of several screens (activities), each serving a unique purpose. This app connects to a specific website, enabling us to track the car, monitor our performance, and replay races to analyze our energy consumption.

## I.a. - Main Activity

The main activity comprises two files: `MainActivity.java` and `activity_main.xml`. The Java files handle the application’s logic, such as triggering actions upon button presses, while the XML files are used for layout design.

The Main Activity serves as the main menu where users land upon opening the app. It contains various buttons that lead to other activities. These buttons are primarily `ImageView` elements, which launch new intents based on their IDs.

### Key Buttons:
- **Top Left ImageView**: Accesses the Parameters Activity.
- **Top Right ImageView**: Accesses the Info Activity.
- **ImageView above "Pilote" TextView**: Accesses the Dashboard Activity.
- **ImageView above "Equipe" TextView**: Accesses the Staff Activity.

## I.b. - Info Activity

This activity includes a ScrollView to optimize space usage on the user interface. It provides detailed explanations about each button’s purpose, credits to graphic designers, and the context in which the application was created.

### Features:
- Enable/Disable driving assistance.
- Show/Hide coordinates and consumption information (voltage, current, and energy).
- Modify the visibility of communication speed and the button to change it.
- Display data from the web database, with selected data shown on a TextView.
- Show/Hide satellite count and output TextView for debugging purposes.
- Adjust communication speed depending on the microcontroller used (Arduino typically at 9600 baud, ESP32 at 115200 baud).

## I.c. - Parameters Activity

Accessible from the Main Activity, this activity allows pre-configuring the dashboard settings. A ScrollView is used to enhance visibility and space efficiency.

### Settings:
- **Language Selection**: Change the app language. Managed by the `strings` folder, containing translations in various languages. Languages are selectable via buttons, each invoking the `setLangue` function.
- **Total Laps Configuration**: Set the total number of laps for the pilot using a `NumberPicker`. Data is saved using `SharedPreferences` and retrieved in the Dashboard Activity.
- **Average Speed Configuration**: Configure the average speed indicator on the dashboard.
- **Average Deviation**: Set the deviation threshold for driving assistance to activate.
- **Website Address**: Define the website address to which PolyWatt data is sent. This is accessed in the Staff Activity via WebView.

## I.d. - Dashboard Activity

This activity’s purpose and functionality are explained in detail below.

### I.d.i. - UI Orientation:
Initially designed for landscape mode, it was changed to reverse portrait due to hardware constraints. The XML code for landscape mode is commented out in `activity_tdb.xml`.

### I.d.ii. - USB Connection Management:
To use the phone’s USB-C port, user permission is required. The app retrieves the microcontroller’s Vendor ID and processes data accordingly. Data reception differs between ESP32 and Arduino Nano. The preferred data format is a series of numbers with start and end symbols. Data is displayed on the UI if the "Hide Coordinates" button is deactivated. We used the felHR85 library from GitHub for this.

### I.d.iii. - GPS Data Retrieval:
The app connects to up to 50 satellites simultaneously to obtain the vehicle's location and GPS speed. A feature to automatically increment lap counts based on GPS coordinates is being developed.

### I.d.iv. - Website Connection:
The app requires internet access permissions (found in the app’s manifest) and a network connection (WiFi or 4G) to retrieve data from the web database and send data to the website.

## I.e. - Staff Activity

This activity primarily consists of a WebView for direct access to the EMT website from within the mobile app.
