# DigiSignageApp

DigiSignageApp is an Android application designed for digital signage, ensuring seamless and uninterrupted video playback. It is ideal for displaying advertisements, information, or other digital content in public spaces.

## Features

- Plays video from a specified URL or local storage.
- Ensures continuous playback even if the app is moved to the background.
- Allows configuration of video source via shared preferences.
- Keeps the device screen on during playback, making it perfect for digital signage purposes.

## Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/omeryildiz/dukatvapplication.git
    ```
2. Open the project in Android Studio.
3. Build and run the project on your Android device or emulator.

## Usage

1. Launch the application.
2. The video will start playing automatically from the specified URL or local storage.
3. Use the media controller to control playback.
4. Configure the video source via the settings menu.

## Code Structure

- `MainActivity.java`: Handles the main video playback functionality.
- `SettingsActivity.java`: Allows users to set preferences for video source and filename.
- `res/`: Contains the layout XML files.

## Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.


## Acknowledgements

- Media playback functionality is based on the Android VideoView class.
