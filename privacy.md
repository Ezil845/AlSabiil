# Privacy Policy

**Effective Date:** 21 February 2026

Welcome to the **AlSabiil App**. We are committed to protecting your privacy and ensuring your data is handled securely and responsibly. This Privacy Policy explains how we collect, use, and safeguard your information.

## 1. Information We Collect

### A. Location Data
- **Why we need it:** AlSabiil requires access to your device's location (latitude and longitude) to accurately calculate prayer times (Adhan) based on your geographic position and to automatically display your current city name.
- **How it's used:** 
    - **Prayer Calculation:** Your location coordinates are processed locally on your device to determine correct solar positions.
    - **City Lookup (Reverse Geocoding):** To display your city name, the app sends your latitude and longitude coordinates via a secure HTTPS request to the OpenStreetMap (Nominatim) service. This is the only instance where location data is transmitted off your device.
- **Data Retention:** We cache your last known location and city name locally on your device to ensure the app remains functional offline. We do not store this data on our own servers.

### B. App Settings & Preferences
- **Why we need it:** We save your preferences such as configured prayer calculation methods, preferred notification sounds, bookmarked Quran ayahs, and last-read statuses.
- **How it's used:** This data is stored securely on your device's local storage (e.g., Android DataStore) to provide a personalized, uninterrupted experience natively on your device.

## 2. How Your Data Is Processed

- **Localized Core Processing:** The vast majority of data—including alarm configurations, prayer time calculations, and bookmarks—is generated and kept locally on your phone.
- **Minimal External Transmission:** We do not maintain a central database or user accounts. The only external transmission of data occurs during the city name lookup described above, which is sent directly to the OpenStreetMap service via encrypted HTTPS. No personally identifiable information (PII) is attached to these requests.

## 3. Data Sharing and Third Parties

We value your privacy. Your data is strictly yours.
- **We do not sell, rent, or trade your personal information to any third parties whatsoever.**
- **We do not integrate third-party tracking or advertising SDKs** that collect data for marketing purposes or metric building.
- Your information remains completely isolated from external entities.

## 4. App Permissions Explained

Our application requests specific system privileges solely to fulfill its core responsibilities:
- **Location Services (Fine/Coarse):** Essential for capturing geographic coordinates to calculate accurate local prayer times according to standard astronomical calculation methods (ISNA, MWL, Makkah, etc.).
- **Notifications & Alarms:** We require permission to schedule exact alarms (`SCHEDULE_EXACT_ALARM`) and bypass battery optimization modes (Doze mode) so that adhan and qiyam notifications arrive exactly on time. We also use full-screen intents and alert system window layers to display the prayer time overlay dependably when the phone is locked.
- **Internet Permission:** Used exclusively and sparingly for fetching strictly necessary non-identifying public resources, although the vast majority of our core features are specifically designed to operate completely offline.

## 5. Security of Your Information

Your preferences and calculated information are handled by the standard, secure local storage solutions enforced natively by the Android Operating System's isolated sandboxed environments. While no environment is entirely immune to flaws or unauthorized system-level manipulation, keeping your data entirely offline ensures the strongest possible baseline protection against external data breaches. We actively adhere to the principle of zero-transmission design.

## 6. Changes to This Policy

We may periodically formulate minor updates to this Privacy Policy. Since our application deliberately omits a centralized backend service connecting to user accounts, we simply rely on keeping this document synchronized alongside application bundle repository updates. We strongly encourage you to review this document occasionally to stay comprehensively informed regarding our privacy-centric data practices.

## 7. Contact Us

If you harbor any questions, concerns, or feedback regarding this Privacy Policy or our localized data handling practices, please feel free to raise an issue within the official project repository or direct inquiries strictly towards verified development team communication endpoints.
