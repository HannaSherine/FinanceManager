# Finance Manager App

A simple and efficient Android application to track your daily income and expenses. This app helps users manage their personal finances by providing a clear view of their balance, total income, and total expenses.

## 🚀 Features

*   **Add Transactions**: Easily record income and expense entries with descriptions, categories, and amounts.
*   **Real-time Balance**: Automatically calculates and displays your current balance based on recorded transactions.
*   **Transaction History**: View a list of all your past transactions sorted by date.
*   **Persistent Storage**: Uses Room Database to ensure your data is saved locally on your device.
*   **Efficient UI**: Built with RecyclerView and ListAdapter for smooth performance.

## 🛠️ Tech Stack

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
*   **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
*   **UI Components**: RecyclerView, ViewModel, LiveData, and ViewBinding.

*   ## 📸 Screenshots

*(Tip: Once you take screenshots of your app, you can upload them to the `screenshots/` folder and link them here!)*

| Dashboard | Add Transaction |
| :---: | :---: |
| ![](screenshots/Dashboard.png) | ![](screenshots/AddTransaction.png) |

## 🏗️ Project Structure

*   `data/`: Contains the Transaction entity.
*   `database/`: Room Database configuration and DAOs.
*   `repository/`: Handles data operations and business logic.
*   `viewmodel/`: Manages UI-related data in a lifecycle-conscious way.
*   `adapter/`: Custom RecyclerView adapter for transaction lists.
*   `ui/`: Activities and Fragments.

## 🏁 Getting Started

1.  Clone the repository:
    ```bash
    git clone https://github.com/HannaSherine/FinanceManager.git
    ```
2.  Open the project in **Android Studio**.
3.  Build and run the app on an emulator or physical device.

---
Developed with ❤️ using Kotlin and Jetpack.
