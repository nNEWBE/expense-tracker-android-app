---
description: Complete Implementation Plan for Expense Tracker App
---

# Expense Tracker App - Implementation Plan

## Overview
A modern, Material Design 3 Android expense tracking application with Firebase integration, guest mode support, and offline-first architecture.

---

## Phase 1: Project Setup & Dependencies

### 1.1 Update build.gradle.kts with Required Dependencies
- Room Database (offline storage)
- Firebase Firestore (cloud sync)
- MPAndroidChart (analytics graphs)
- Lottie (animations)
- Biometric authentication
- WorkManager (notifications)
- ViewBinding/DataBinding
- Lifecycle components
- Navigation component
- Glide/Coil (image loading)

### 1.2 Configure Firebase
- Add Firestore dependency
- Enable offline persistence
- Configure security rules

---

## Phase 2: Core Data Layer

### 2.1 Room Database (Local Storage)
- **Entities:**
  - `Expense` (id, amount, category, date, notes, type, userId, synced)
  - `Income` (id, amount, source, date, notes, userId, synced)
  - `UserProfile` (id, name, email, monthlyBudget, currency, photoUri)
  - `Category` (id, name, icon, color)

- **DAOs:**
  - `ExpenseDao`
  - `IncomeDao`
  - `UserProfileDao`

- **Database:**
  - `AppDatabase` with migrations

### 2.2 Firebase Firestore Structure
```
/users/{userId}/
  ├── profile/
  ├── expenses/
  └── income/
```

### 2.3 Repository Pattern
- `ExpenseRepository` (abstracts local + cloud)
- `IncomeRepository`
- `UserRepository`
- Sync logic for guest-to-cloud migration

---

## Phase 3: Authentication System

### 3.1 Firebase Authentication
- `AuthManager` singleton class
- Email/password sign-up
- Email/password login
- Password reset via email
- Auth state listener for session persistence
- Guest mode detection

### 3.2 Activities/Fragments
- `SplashActivity` (with Lottie animation)
- `LoginActivity`
- `RegisterActivity`
- `ForgotPasswordActivity`

---

## Phase 4: Main App Architecture

### 4.1 Navigation Structure
- `MainActivity` (hosts navigation + bottom bar)
- Bottom Navigation Items:
  - Home/Dashboard
  - Transactions (expenses + income)
  - Analytics
  - Profile

### 4.2 Fragments
- `DashboardFragment` - Overview, summaries, quick actions
- `TransactionsFragment` - List of all transactions with filters
- `AddExpenseFragment` - Form to add/edit expense
- `AddIncomeFragment` - Form to add/edit income
- `AnalyticsFragment` - Charts and graphs
- `ProfileFragment` - User settings and data
- `SettingsFragment` - App settings, theme, export

---

## Phase 5: Expense Management

### 5.1 Add/Edit Expense Feature
- Material TextFields for:
  - Amount (currency formatted)
  - Category dropdown (Material Exposed Dropdown)
  - Date picker (Material DatePicker)
  - Notes (optional)
- Type toggle (Expense/Income)
- FAB for quick add

### 5.2 Categories
Default categories with icons:
- 🍕 Food
- 🚗 Transport
- 🛒 Shopping
- 💡 Bills
- 🎬 Entertainment
- 📦 Others

### 5.3 Transaction List
- RecyclerView with Material animations
- Swipe to delete
- Click to edit
- Filter chips: Daily/Weekly/Monthly/All
- Category filter
- Search functionality

### 5.4 Summaries
- Total expenses this period
- Total income this period
- Net balance
- Category-wise breakdown

---

## Phase 6: Analytics & Charts

### 6.1 MPAndroidChart Integration
- **Pie Chart:** Category distribution
- **Bar Chart:** Monthly comparison
- **Line Chart:** Weekly spending trends

### 6.2 Budget Tracking
- Monthly budget setting
- Progress bar visualization
- Warning when exceeding 80%
- Alert when budget exceeded

---

## Phase 7: User Profile

### 7.1 Profile Features
- Profile photo (camera/gallery)
- Display name
- Email (view only)
- Monthly budget setting
- Preferred currency selection

### 7.2 Account Actions
- Logout
- Delete account (with confirmation)
- Sync guest data (if applicable)

---

## Phase 8: Notifications & Reminders

### 8.1 WorkManager Tasks
- Daily expense reminder (configurable time)
- Weekly summary notification
- Budget exceeded alert

### 8.2 Notification Channels
- Reminders
- Alerts
- General

---

## Phase 9: Theme System

### 9.1 Light/Dark Mode
- System default
- Light mode
- Dark mode
- Save preference in SharedPreferences

### 9.2 Material You (Dynamic Colors)
- Use Material 3 dynamic color when available
- Fallback to custom theme

---

## Phase 10: Additional Features

### 10.1 Export Options
- Export to CSV
- Export to PDF (using iText or similar)

### 10.2 Multi-Currency
- Supported currencies: BDT, INR, USD, EUR, GBP
- Save preference
- Format amounts accordingly

### 10.3 App Lock
- Biometric authentication
- PIN fallback
- Enable/disable in settings

### 10.4 Advanced Search
- Search by amount range
- Search by note content
- Search by category
- Search by date range

---

## File Structure
```
app/src/main/java/com/example/expensetrackerapp/
├── MainActivity.java
├── auth/
│   ├── AuthManager.java
│   ├── SplashActivity.java
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   └── ForgotPasswordActivity.java
├── data/
│   ├── local/
│   │   ├── AppDatabase.java
│   │   ├── dao/
│   │   │   ├── ExpenseDao.java
│   │   │   ├── IncomeDao.java
│   │   │   └── UserProfileDao.java
│   │   └── entity/
│   │       ├── Expense.java
│   │       ├── Income.java
│   │       └── UserProfile.java
│   ├── remote/
│   │   └── FirestoreManager.java
│   └── repository/
│       ├── ExpenseRepository.java
│       ├── IncomeRepository.java
│       └── UserRepository.java
├── ui/
│   ├── dashboard/
│   │   └── DashboardFragment.java
│   ├── transactions/
│   │   ├── TransactionsFragment.java
│   │   ├── AddExpenseFragment.java
│   │   ├── AddIncomeFragment.java
│   │   └── TransactionAdapter.java
│   ├── analytics/
│   │   └── AnalyticsFragment.java
│   ├── profile/
│   │   ├── ProfileFragment.java
│   │   └── SettingsFragment.java
│   └── common/
│       ├── DateUtils.java
│       └── CurrencyUtils.java
├── utils/
│   ├── Constants.java
│   ├── PreferenceManager.java
│   └── ThemeUtils.java
├── notifications/
│   ├── NotificationHelper.java
│   └── ReminderWorker.java
└── security/
    └── AppLockManager.java
```

---

## Implementation Order
1. ✅ Dependencies & configuration
2. ✅ Data layer (Room entities, DAOs, database)
3. ✅ Authentication system
4. ✅ Splash screen with animation
5. ✅ Main navigation structure
6. ✅ Dashboard with summaries
7. ✅ Transaction management (CRUD)
8. ✅ Analytics with charts
9. ✅ Profile & settings
10. ✅ Notifications
11. ✅ Theme system
12. ✅ Additional features (export, search, app lock)

---

// turbo-all
