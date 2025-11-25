# Tablón v1.0: Collaborative Shopping List

**Tablón** is a native Android application designed to help household members manage a shared shopping list in real-time. It streamlines family shopping, enhances coordination, and ensures nothing is forgotten.

This project is currently at the **Minimum Viable Product (MVP)** stage, with a fully functional and secure collaborative experience.

---

## 📋 Table of Contents

- [✨ Key Features](#-key-features)
- [🛠️ Tech Stack & Architecture](#️-tech-stack--architecture)
- [🚀 Getting Started](#-getting-started)
- [갤 Gallery](#-gallery)
- [🛣️ Roadmap](#️-roadmap)
- [🤝 How to Contribute](#-how-to-contribute)
- [📄 License](#-license)

---

## ✨ Key Features

### Version 1.0 - Functional Collaborative MVP

#### 🔐 Secure & Modern Authentication
- **Email & Password:** Users can register and sign in using their email.
- **Biometric Login:** Quick and secure access using fingerprint authentication for subsequent logins, powered by encrypted credentials.

#### 🏡 Household Management
- **Seamless Onboarding:** The first user can easily create a new household.
- **Unique Invite Codes:** A unique code is generated to allow other members to join an existing household, ensuring privacy and control.

#### 🛒 Real-Time Shopping List
- **Live Sync:** The shopping list is automatically synchronized in real-time across all devices within a household.
- **Add Products:** Easily add items with details like name, quantity, unit, and an optional description.
- **Intuitive Interactions:**
  - **Single Tap:** Marks/unmarks an item as "purchased," moving it between the "To Buy" and "In Cart" sections.
  - **Long Press:** Opens a menu to permanently delete an item from the list.
- **List Lifecycle:** Archive the entire shopping list with a long press on the title, saving a historical record of the purchase.

#### 🧠 Smart UX Features
- **Product Autocomplete:** Suggests product names based on purchase history to speed up item entry.
- **Unit Memory:** Remembers and pre-selects the last unit used for a known product, reducing repetitive input.

#### 🎨 Visual Identity
- The app features a custom launcher icon and a fingerprint icon, enhancing brand identity and user experience.

---

## 🛠️ Tech Stack & Architecture

This project is built on a modern, scalable, and decoupled architecture, following Google's recommended best practices for Android development.

- **Backend & Synchronization:** Google Firebase Platform
  - **Firebase Authentication:** Manages user authentication (Email/Password).
  - **Cloud Firestore:** A real-time NoSQL database for synchronizing households and lists.
  - **Firestore Security Rules:** Robust rules are implemented to protect data, ensuring only household members can access and modify their lists.

- **Local Database:** Room Database
  - Manages "smart" features locally and efficiently, such as product history for autocomplete and unit memory.

- **User Interface (UI):** Jetpack Compose
  - A declarative, modern, and efficient UI toolkit for building native Android interfaces.

- **Client Architecture:** MVVM (Model-View-ViewModel)
  - A clear separation of concerns between the UI (View), presentation logic (ViewModel), and data logic (Repository).

- **Local Security:** EncryptedSharedPreferences
  - Ensures the secure and encrypted storage of user credentials for biometric login.

- **Language & Concurrency:** Kotlin
  - Includes **Coroutines** and **Flow (StateFlow)** for managing asynchronous operations and reactive state.

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- Android Studio (latest stable version recommended)
- A Firebase project set up with Authentication and Cloud Firestore enabled.

### Installation

1. **Clone the repo**
   ```sh
   git clone https://github.com/gustavor29/Tablon
    ```
2. **Set up Firebase**
Place your google-services.json file in the app/ directory of the project.
3. Open in Android Studio
Open the project in Android Studio and let it sync.
4. Run the app
Build and run the app on an Android emulator or a physical device.

---

## Gallery


| Shopping List Screen                                | Adding a Product Screen                           |
| --------------------------------------------------- | ------------------------------------------------- |
| ![Shopping List Screen](https://github.com/gustavor29/Tablon/blob/main/ShopingList.jpeg) | ![Adding a Product Screen](https://github.com/gustavor29/Tablon/blob/main/ProductAdd.jpeg) |
---

## 🛣️ Roadmap
The solid foundation of v1.0 allows for easy expansion. Here are the logical next steps:
### v1.1 (Immediate Enhancements)
- **Implement Google Sign-In:** Add "Sign in with Google" as an alternative to email registration.
- **Build Archived Lists Screen:** Develop the UI to view the historical shopping lists that are already being saved.
- **Implement Navigation Menu (Hamburger):** Activate the menu button to navigate between the active list and the archives.
### v2.0 (Advanced Features)
- **Push Notifications:** Notify household members when a new item is added to the list.
- **Product Categorization:** Allow users to assign categories to products and view a summary of expenses by category.
- **Multiple Lists:** Enable a household to manage multiple active lists simultaneously (e.g., "Weekly Groceries," "Hardware Store").
## 🤝 How to Contribute
Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated.**
If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
1. Fork the Project
2- Create your Feature Branch (```sh git checkout -b feature/AmazingFeature```)
3. Commit your Changes (```shgit commit -m 'Add some AmazingFeature'```)
4. Push to the Branch (```shgit push origin feature/AmazingFeature```)
5. Open a Pull Request
## 📄 License
Distributed under the MIT License. See LICENSE for more information.
