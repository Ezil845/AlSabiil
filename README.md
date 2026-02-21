# AlSabiil - Digital Quran Reader

**AlSabiil** is a beautifully designed, feature-rich digital Quran reader application built with modern Android technologies. The app provides a seamless and immersive experience for reading and studying the Holy Quran with advanced features for enhanced spiritual practice.

## 🌟 Key Features

### 📖 Complete Quran Reading Experience
- Access to all 604 pages of the Quran with high-quality Arabic text
- Smooth scrolling and pagination optimized for reading
- Page restoration - the app remembers your last reading position

### 🔖 Advanced Bookmark System
- Create bookmarks for specific Surahs and Ayahs
- Add custom names to bookmarks for easy identification
- Organized bookmark management with quick navigation
- Bookmarks persist across app sessions using DataStore

### 🕋 Comprehensive Index System
- Easy navigation through Surahs (chapters) and Juz (sections)
- Quick access to any part of the Quran via the index modal
- Tabbed interface for Surah, Juz, and Bookmarks navigation

### 🎨 Customizable Themes
- Multiple color palettes to choose from for comfortable reading
- Elegant green-themed interface (Emarald palette) as default
- Theme preferences saved and applied across sessions

### ⏰ Prayer Time Notifications
- Configurable prayer time notifications for Fajr, Dhuhr, Asr, Maghrib, and Isha
- Sunrise notification for additional spiritual awareness
- **Exact Alarm Delivery:** Bypasses battery optimization (Doze mode) to ensure Adhan plays precisely on time
- **Lock-Screen Alerts:** Interactive full-screen popup dialogs that wake the device, allowing you to stop the Adhan even when locked
- Automatic location detection for accurate prayer times
- Calculation methods configurable based on geographic location

### 📚 Spiritual Reminders
- Morning (Sabah) and Evening (Masaa) Adhkar standard notifications
- Nightly Qiyam reminders with dedicated interactive full-system alarm
- Fully customizable audio routing using isolated channels (guarantees no overlapping system sounds)

### 📝 Tafseer (Exegesis) Support
- Detailed explanations for individual Ayahs (verses)
- Contextual Tafseer accessible via Ayah selection
- Enhanced understanding of Quranic verses

### 🌙 Night Mode & Accessibility
- Optimized reading experience with appropriate contrast
- Clean, distraction-free interface
- Responsive design for various screen sizes

## 🛠️ Technical Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Modern Android UI toolkit)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Data Persistence**: DataStore (preferences and protocol buffers)
- **Coroutines**: For asynchronous programming
- **Serialization**: KotlinX Serialization for data handling

### Key Components
- **Repository Pattern**: For data management and business logic
- **ViewModel**: For UI-related data management
- **State Management**: Using Compose's state management features
- **Navigation**: Compose-based navigation system
- **Location Services**: For accurate prayer time calculations

### Data Sources
- Quran text and metadata stored locally
- Prayer time calculations based on geographic coordinates
- Settings and preferences stored using DataStore

## 📱 User Interface

### Main Screens
1. **Mushaf Screen** - Primary Quran reading interface
2. **Index Modal** - Navigation hub for Surahs, Juz, and Bookmarks
3. **Tafseer Modal** - Detailed verse explanations
4. **Settings Screen** - App customization options

### UI Components
- Custom `MushafPage` component for Quran page rendering
- `BookmarkManager` for bookmark management
- `QuranIndexModal` for navigation
- `TafseerModal` for verse explanations

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK API level 21 or higher
- Gradle 8.13 or compatible version

### Setup Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/Ezil845/AlSabiil.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build and run the application on an emulator or physical device

### Configuration
- The app uses automatic location detection for prayer times
- Customize themes and notification preferences in the Settings screen
- Manage bookmarks directly from the reading interface

## 📋 Permissions
- **Location Access**: For accurate prayer time calculations
- **Notification Permission**: For prayer time and Adhkar reminders

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request. For major changes, open an issue first to discuss what you would like to change.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments
- The Quran text and translations used in this application
- Islamic resources and scholarly contributions
- The open-source community for libraries and tools used in this project

---

*AlSabiil - Making the Quran accessible and engaging for the modern Muslim*

For support or inquiries, please contact the development team.