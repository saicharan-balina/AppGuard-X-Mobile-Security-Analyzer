# AppGuard X – Mobile Security Analyzer

🛡️ **AppGuard X** is a professional-grade Android application built with Kotlin and Jetpack Compose. It analyzes all installed applications on your device, performing a deep permission-based risk analysis to protect your privacy and security.

## ✨ Core Features

*   **🔍 Comprehensive App Scanning**: Fetches all installed apps using `PackageManager` with an optional toggle for system apps.
*   **🧠 Rule-Based Risk Engine**: Classifies apps into **High**, **Medium**, and **Low** risk levels based on sensitive permission usage and suspicious combinations.
*   **🔥 Smart Permission Insight**: Predicts essential permissions based on app categories (e.g., Messaging, Camera, Calculator) and flags unnecessary or suspicious permissions.
*   **📊 Security Dashboard**: Provides an overall device Security Score (0–100) and a breakdown of risk counts.
*   **📱 Modern UI**: A premium, state-of-the-art interface built with **Material Design 3**, featuring dark mode, smooth animations, and color-coded risk badges.
*   **⚡ Performance**: Leverages Kotlin Coroutines for efficient background scanning without UI lag.

## 🔴 Risk Classification Logic

### High Risk Permissions
- `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`
- `READ_CONTACTS`, `RECORD_AUDIO`, `CAMERA`
- `READ_CALL_LOG`, `WRITE_CALL_LOG`, `ACCESS_FINE_LOCATION`

### Suspicious Combinations (Auto-HIGH)
- **SMS + Contacts**: Potential data harvesting.
- **Mic + Location**: Possible surveillance.
- **SMS + Internet**: Possible data exfiltration.
- **Contacts + Internet**: Data leakage risk.

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Hedgehog 2023.1.1 or newer recommended)
- **Android Device or Emulator** (API 24 / Android 7.0 or higher)

### Installation
1.  **Clone/Open**: Open the project folder in Android Studio.
2.  **Sync**: Wait for Gradle to download dependencies and sync the project.
3.  **Run**: Click the **Run** button (▶) to install the app on your connected device or emulator.

## 🛠️ Technical Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Asynchronous**: Kotlin Coroutines & StateFlow
- **Navigation**: Jetpack Compose Navigation
- **Image Loading**: Coil

## 🔐 Permissions Required
- `QUERY_ALL_PACKAGES`: To list and analyze installed applications (Android 11+).

---
Developed as a high-performance security utility for the Android ecosystem.
