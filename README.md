# Tablón v1.0: Collaborative Shopping List

**Tablón** is a native Android application designed to help household members manage a shared shopping list in real-time. It streamlines family shopping, enhances coordination, and ensures nothing is forgotten.

This project is currently at the **Minimum Viable Product (MVP)** stage, with a fully functional and secure collaborative experience.

---

## 📋 Table of Contents

- [✨ Key Features](#-key-features)
- [🛠️ Tech Stack & Architecture](#️-tech-stack--architecture)
- [🚀 Getting Started](#-getting-started)
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
   git clone https://github.com/your_username/your_repository_name.git