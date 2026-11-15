# Ve-Stock (VE AGRO HUB)

> **"VE AGRO HUB — THE FARMER'S SOLUTION"**
>
> A modern, offline-first Android inventory management, multi-store stock tracking, and point-of-sale (POS) application built with **Jetpack Compose** and **Material 3**.

---

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Database-Room%20(SQLite)-00599C)
![Firebase](https://img.shields.io/badge/Backend-Firebase%20(Firestore%2C%20Auth%2C%20FCM)-FFCA28?logo=firebase&logoColor=black)
![Compile SDK](https://img.shields.io/badge/compileSdk-37-blue)
![Min SDK](https://img.shields.io/badge/minSdk-24-blue)
![Gradle](https://img.shields.io/badge/Gradle-9.9.1-02303A?logo=gradle&logoColor=white)

---

## 📌 Overview

**Ve-Stock** is designed for agro-dealers, farm input suppliers, and agricultural retail hubs in East Africa (with native support for Tanzanian Shilling — **TZS** currency formatting). It empowers store managers and field staff to manage product catalogs (fertilizers, seeds, chemicals, pesticides, farm tools), track inventory across multiple physical stores, record point-of-sale customer transactions, and maintain an immutable audit trail of all stock movements.

Built with an **offline-first** architecture, Ve-Stock ensures uninterrupted store operations even in areas with poor or intermittent connectivity, automatically synchronizing changes with **Firebase Cloud Firestore** once connectivity is restored.

---

## ✨ Key Features

- **⚡ Fast Point of Sale (POS)**
  - Quickly record sales transactions with store selection, product catalog picker, and quantity controls.
  - Auto-suggests repeat customer names.
  - Supports multiple transaction types: `SALE`, `REFILL`, and `PENDING_PAYMENT`.
  - Automatic total price computation in Tanzanian Shillings (**TZS**).

- **🏬 Multi-Store Inventory Tracking**
  - Track stock levels across different branches and warehouse locations.
  - Configurable minimum stock alert thresholds per inventory item.
  - Visual badges for low stock warnings (`Low Stock` alerts).
  - Track image captures / receipt attachments per item using CameraX.

- **📦 Product Catalog & Category Management**
  - Group products by agricultural categories (Fertilizers, Seeds, Pesticides, Equipment, Animal Feeds, etc.).
  - Standardized packaging unit specifications (e.g., `50 kg Bag`, `1 L Bottle`, `25 kg Packet`).
  - Unit pricing with automated valuation across all inventory items.

- **🛡️ 3-Tier Role-Based Access Control (RBAC)**
  - **ADMIN**: Full access to store management, product catalog additions/deletions, user authorization, and system configuration.
  - **STAFF**: Fast sales recording, refill management, and inventory viewing.
  - **NONE / PENDING**: Restricts unauthorized users until approved by an administrator.
  - Initial seed administrator whitelist bootstrapped via JSON asset (`admin_whitelist.json`).

- **📜 System Events & Audit Log**
  - Immutable audit trail recording every inventory alteration (`SALE`, `REFILL`, `PENDING_PAYMENT`).
  - Records user email, customer name, notes, timestamp, quantity delta, and monetary total.

- **🔄 Offline-First & Cloud Synchronization**
  - Local Room SQLite database serves as the single source of truth for snappy UI response times.
  - `FirestoreSyncEngine` syncs changes bidirectionally with Firebase Cloud Firestore.
  - Firebase Cloud Messaging (`VeStockFirebaseMessagingService`) handles remote alerts.

---

## 🏗️ Architecture & Technology Stack

Ve-Stock follows modern Android development practices and Google's recommended architecture guide (MVVM + Clean Architecture + Repository Pattern).

```mermaid
flowchart TD
    subgraph UI ["UI Layer (Jetpack Compose + Material 3)"]
        Activity[MainActivity]
        MainScrn[MainScreen / NavDrawer / BottomBar]
        Screens[RecordSalesScreen | DashboardScreen | InventoryScreen | StockLogScreen | AdminSettingsScreen]
        VM[MainViewModel]
    end

    subgraph Domain ["Domain & State Layer"]
        Models[VeStockModels: Store, ProductType, InventoryItem, StockLog, AllowedUser]
        Flows[Kotlin Coroutines StateFlow / SharedFlow]
    end

    subgraph Data ["Data Layer (Repository Pattern)"]
        AuthRepo[AuthRepository]
        VeRepo[VeStockRepository]
        SyncEngine[FirestoreSyncEngine]
    end

    subgraph Storage ["Storage & Remote Sources"]
        RoomDB[(VeStockDatabase / Room SQLite)]
        DAOs[StoreDao | ProductTypeDao | InventoryItemDao | StockLogDao | AllowedUserDao]
        Firestore[(Firebase Cloud Firestore)]
        FCM[Firebase Cloud Messaging]
    end

    Activity --> MainScrn
    MainScrn --> Screens
    Screens <--> VM
    VM <--> Flows
    Flows <--> AuthRepo
    Flows <--> VeRepo
    VeRepo --> DAOs
    AuthRepo --> DAOs
    SyncEngine <--> DAOs
    DAOs --> RoomDB
    SyncEngine <--> Firestore
    FCM --> SyncEngine
```

### Core Libraries & Tools
- **Language**: Kotlin 2.4.x (with Coroutines & Flow)
- **UI Toolkit**: Jetpack Compose, Material 3, Edge-to-Edge display
- **Navigation**: Jetpack Compose Navigation 3 (`androidx.navigation3`) & Adaptive Navigation
- **Local Persistence**: Android Jetpack Room (2.7+) with SQLite & KSP annotation processing
- **Cloud & Auth**: Firebase BOM (Firestore, Authentication, Cloud Messaging)
- **Hardware Integration**: CameraX (Camera2, Lifecycle, View) for stock/receipt captures
- **Networking & Serialization**: Retrofit, OkHttp, Moshi Kotlin, Kotlinx Serialization
- **Local Preferences**: Android Jetpack DataStore Preferences
- **Build System**: Gradle 9.9.1 with Kotlin DSL (`build.gradle.kts`) and version catalog (`libs.versions.toml`)

---

## 📂 Project Structure

```text
ve-agrohub-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── assets/
│   │   │   │   └── admin_whitelist.json       # Initial seed admin emails
│   │   │   ├── java/com/tabaapps/ve_agrohub_app/
│   │   │   │   ├── MainActivity.kt            # Main activity entry point
│   │   │   │   ├── QuestionAnswersApplication.kt # Application container
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/                 # Room database, entities, DAOs, converters
│   │   │   │   │   ├── model/                 # Domain & presentation models
│   │   │   │   │   ├── remote/                # Network & streaming clients
│   │   │   │   │   ├── repository/            # VeStockRepository, AuthRepository
│   │   │   │   │   └── sync/                  # FirestoreSyncEngine
│   │   │   │   ├── service/                   # VeStockFirebaseMessagingService
│   │   │   │   ├── ui/
│   │   │   │   │   ├── MainScreen.kt          # Drawer + Bottom navigation shell
│   │   │   │   │   ├── MainViewModel.kt       # Primary screen state holder
│   │   │   │   │   ├── screens/               # RecordSales, Dashboard, Inventory, AdminSettings, Auth
│   │   │   │   │   └── theme/                 # Material 3 Color, Type, Theme
│   │   │   │   └── util/                      # CurrencyUtils (TZS), StringUtils
│   │   │   └── res/                           # Drawables, mipmaps, strings, XML rules
│   │   └── test/                              # Unit tests (VeStockUnitTest)
│   └── build.gradle.kts                       # App module configuration
├── gradle/
│   ├── libs.versions.toml                     # Dependency version catalog
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties          # Gradle distribution & sha256 checksum
├── build.gradle.kts                           # Root build configuration
├── settings.gradle.kts                        # Plugin & repository settings
├── .gitignore                                 # Git ignore rules for Android
└── README.md                                  # Project documentation
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer
- **JDK**: Java 17 or Java 21+
- **Android SDK**:
  - `compileSdk`: 37 (Android 15+)
  - `minSdk`: 24 (Android 7.0+)

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/vmataba/ve-agrohub-app.git
   cd ve-agrohub-app
   ```

2. **Add Firebase configuration:**
   - Place your `google-services.json` file inside the `app/` directory (if configuring custom Firebase backend).

3. **Verify Gradle distribution:**
   ```bash
   ./gradlew --version
   ```

4. **Build the Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Run Unit Tests:**
   ```bash
   ./gradlew test
   ```

---

## 🧪 Testing & Code Quality

The project includes unit test suites in `app/src/test/java/` verifying:
- **Currency formatting**: Precise conversion and comma formatting for Tanzanian Shillings (`TZS 50,000`, etc.).
- **Inventory valuation**: Real-time total value calculations and low-stock threshold triggers.
- **RBAC validation**: Role validation logic (`ADMIN` vs `STAFF`).
- **Room DAO Query compliance**: Reflection-based validation ensuring all SQL DAO queries adhere strictly to lowercase SQL syntax standards.

Run all tests via:
```bash
./gradlew test
```

---

## 🔒 Security & Privacy

- All sensitive credentials, build outputs (`build/`, `.gradle/`), local paths (`local.properties`), IDE caches (`.idea/`), and signing keystores (`*.jks`, `*.keystore`) are excluded from version control via [`.gitignore`](.gitignore).
- Access control is strictly enforced on-device and synchronized with Firebase Firestore rules.

---

## 📄 License

Copyright &copy; 2026 **VE AGRO HUB**. All rights reserved.
Licensed under the Apache License, Version 2.0.
