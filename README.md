# SmartContacts - 💫 Your Smart Calling Partner
A brief description of what this project does and who it's for.

## 🌟 Overview
Smart Contacts is a modern Android contact management application developed using Java in Android Studio. It provides an intuitive interface for managing contacts, making phone calls, sending messages, viewing call history, and organizing favorite contacts. The application supports multiple Indian languages, dark/light themes, AI-powered spam detection, blocked numbers management, and a customizable user experience. It is designed to simplify everyday communication while offering a clean, responsive, and secure interface.

## 🚀 Features
### 📞 Contact Management
- View all device contacts
- Search contacts instantly
- Add new contacts
- Edit existing contacts
- Delete contacts
- Share contacts
- Block/Unblock contacts
- View detailed contact information
- Support for multiple phone numbers
- Contact profile picture support

### ⭐ Favorites
- Add contacts to favorites
- Remove contacts from favorites
- Search favorite contacts

### 📜 Call History
- Incoming calls
- Outgoing calls
- Missed calls
- Call duration
- Clear call history
- Search call history
- One-tap callback
  
### ☎ Dial Pad
- Modern numeric keypad
- Real-time number display
- Call button
- Add contact button
- Video call button
- Delete digits
- Launch default dialer

### 💬 Communication
- Make phone calls
- Send SMS using default messaging app
- Launch default video calling application
- Share contacts

### ⚙ Settings
- Blocked Numbers
- Theme Appearance (Light/Dark)
- AI Spam Detection
- App Language Selection
- About Application

### 🌐 Multilingual Support
- English
- Hindi
- Kannada
- Telugu
- Tamil
- Malayalam
- Marathi
- Gujarati
- Bengali
- Punjabi
- Odia
- Assamese

### 🎨 User Experience
- Material Design UI
- Responsive layouts
- Dark Mode
- Light Mode
- Smooth navigation
- Runtime permission handling

## 🛠 Technology Stack
- **Programming Language:** Java 
- **Development Environment (IDE):** Android Studio
- **User Interface:** XML (Material Design 3)
- **Architecture Pattern:** MVVM (Model–View–ViewModel)
- **Database:** Android Contacts Provider (ContactsContract)
- **Call Log Management:** Android CallLog API
- **Local Storage:** SharedPreferences
- **Image Handling:** MediaStore API
- **Permissions:** Android Runtime Permissions
- **Intents:** Android Implicit & Explicit Intents
- **RecyclerView:** Display Contacts, Favorites, and Call History
- **Navigation:** Bottom Navigation View
- **UI Components:** Material Design 3 Components
- **Localization:** Android Resource Localization (strings.xml)
- **Theme Management:** AppCompatDelegate (Light/Dark Mode)
- **App Language Management:** Locale & Configuration APIs
- **Version Control:** Git & GitHub
- **Build System:** Gradle
- **Minimum SDK:** Android API 24 (Android 7.0 Nougat)
- **Target SDK:** Latest Android SDK (API 35/36 as configured)
- **Testing:** Manual Functional Testing & UI Testing
- **Operating System:** Android

## 📂 Architecture
The application follows the MVVM (Model–View–ViewModel) architecture to separate UI, business logic, and data handling.
### Layers
- **Presentation Layer:** Activities,Fragments,XML Layouts,Adapters
- **ViewModel Layer:** Business Logic,Search Logic,Language Management,Theme Management
- **Repository Layer:** Contacts Repository,Call History Repository,Favorites Repository
- **Android Framework:** Contacts Provider,Call Log Provider,SharedPreferences,Intents,MediaStore
<img src="https://github.com/ritish18089/SmartContacts/blob/main/smaw.png" width="1000px" >

## 🎯  Application Work Flow
<img src="https://github.com/ritish18089/SaiketTask2/blob/main/aaga.png" height="1000px" width="1000px" >

## 🧪 Testing Strategy
### Functional Testing
- User Registration
- Login
- Logout
- Gallery Loading
- Camera Functionality
- Image Editing
- Favorites
- Theme Switching

### UI Testing
- Responsive Layout
- Dark & Light Theme
- Navigation Testing
- Screen Rotation

### Integration Testing
- Firebase Authentication
- Firestore Database
- CameraX
- MediaStore

### Performance Testing
- Gallery Loading Speed
- Image Preview Performance
- RecyclerView Scrolling
- Memory Usage

### User Acceptance Testing
- Easy Navigation
- Editing Workflow
- Camera Operations
- Profile Management

## 📸 Screenshots
### 🚀 Splash Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/1.jpeg"  height="500px"></p>

### 🔐 Login Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/2.jpeg"  height="500px"></p>

### 📝 Signup Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/3.jpeg"  height="500px"></p>

### 🏠 Home Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/4.jpeg"  height="500px"></p>

### 🖼️ Gallery Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/5.jpeg"  height="500px"></p>

### 🔍 Image Preview
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/6.jpeg"  height="500px"></p>

### ✂️ Crop Tool
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/7.jpeg"  height="500px"></p>

### 🎚️ Adjust Tool
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/8.jpeg"  height="500px"></p>

### 🖼️ Frame Tool
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/9.jpeg"  height="500px"></p>

### ✏️ Markup Tool
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/10.jpeg"  height="500px"></p>

### ⭐ Favorites Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/11.jpeg"  height="500px"></p>

### 📷 Camera Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/12.jpeg"  height="500px"></p>

### 👤 Profile Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/13.jpeg"  height="500px"></p>

### ℹ️ About App Screen
<p align="center"><img src="https://github.com/ritish18089/SaiketTask2/blob/main/14.jpeg"  height="500px"></p>

## ⚙ Installation
### Prerequisites
- Android Studio Hedgehog or above
- JDK 17
- Android SDK 34+
- Firebase Project
- google-services.json

### Steps
- **Clone the repository:** git clone https://github.com/ritish18089/SaiketTask2.git
- Open the project in Android Studio.
- Add google-services.json to the app/ folder.
- Sync Gradle.
- Build the project.
- Run the application on an emulator or Android device.

## 📱Download APK
Click below to download the latest version of GalleryEditor
[APK File](https://github.com/ritish18089/SaiketTask2/releases/download/v1.0.0/GalleryEditor.apk)
