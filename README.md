# QR Mate

QR Mate is a versatile QR code generator and scanner application that allows users to easily create and manage QR codes for various purposes, including Wi-Fi connectivity and sharing messages. The app also provides a seamless experience for scanning existing QR codes.

## Features

### 1. QR Code Generation
- Generate QR codes for:
  - **Wi-Fi Credentials**: Easily create QR codes for connecting to Wi-Fi networks. Users can input the SSID and password, and the app generates a QR code that can be scanned to connect to the network.
  - **Text Messages**: Convert any text message into a QR code for quick sharing.
  - **SMS Sharing**: Users can select a message from their SMS inbox and generate a QR code for that message.

### 2. QR Code Scanning
- Scan existing QR codes using the device's camera to retrieve encoded information quickly.

### 3. QR Code Management
- **Local Storage**: Save generated QR codes in local storage, categorized by user.
- **Firebase Integration**: Sync QR codes with Firebase for cloud storage and easy access across devices.
- **Delete Functionality**: Remove QR codes from local storage and Firebase. If offline, the app queues deletions to be performed when internet access is restored.

### 4. User-Friendly Interface
- Light theme maintained regardless of the system's dark mode setting.
- Intuitive dialogs for inputting Wi-Fi credentials and other details.

## Technologies Used
- **Programming Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (for local storage)
- **Cloud Database**: Firebase Realtime Database
- **UI Framework**: Android Jetpack (RecyclerView, ViewModel, LiveData)
- **Image Loading**: Glide (for displaying QR code images)

## Firebase and Room Database Flow

The QR Mate app employs both Firebase and Room Database for managing QR codes. Here’s how data flows between the two:

### 1. **Data Generation**
- When a user generates a QR code (e.g., for Wi-Fi credentials), the app creates a `QRCode` object containing the relevant data.

### 2. **Local Storage (Room)**
- The generated QR code is stored locally using Room Database. 
  - The `QRCodeEntity` is created and inserted into the local Room database via the DAO.

### 3. **Synchronization with Firebase**
- After successfully storing the QR code in Room:
  - The app attempts to upload the QR code to Firebase.
  - If the upload is successful, the Firebase database reflects the latest QR code entry.
  - If the upload fails due to lack of internet connection, the QR code remains in the local Room database and is marked for upload once the internet connection is restored.

### 4. **Data Retrieval**
- When displaying QR codes:
  - The app first checks for internet connectivity.
  - If online, it fetches the latest QR codes from Firebase and merges them with the local Room database entries, avoiding duplicates.
  - If offline, it retrieves QR codes solely from Room Database.

### 5. **Deletion Handling**
- When a QR code is deleted:
  - It is removed from the Room database immediately.
  - If the app is offline, the deletion is queued to be executed in Firebase once the internet is available.

## Installation

To run the project locally, follow these steps:

1. Open the project in Android Studio.
2. Ensure the necessary permissions are set up in `AndroidManifest.xml` for:
   - Camera access
   - Reading SMS
3. Sync the project with Gradle files.
4. Run the app on an emulator or a physical device.

**OR You can download the app from the Release section**

## Usage Flows

### Generating a Wi-Fi QR Code
- Tap the **"Generate Wi-Fi QR Code"** button.
- Input the SSID and password (if required).
- Tap **"Generate QR Code"** to create the QR code.

### Sharing a Message as QR Code
- Select the **SMS** button.
- Choose a message from the SMS inbox.
- The app converts the selected message into a QR code.

### Scanning a QR Code
- Tap the **"Scan QR Code"** button.
- Point the camera at a QR code.
- The app decodes the QR code and displays the information.

### Managing QR Codes
- View saved QR codes in a list format.
- Delete or share QR codes as needed.

