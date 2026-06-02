# Currency Converter

A native Android app that converts between currencies using live exchange rates.
Enter an amount, pick two currencies, and get the converted value from a real REST API.

Built as a learning project to practice modern Android development with Kotlin and Jetpack Compose.

## Screenshots

<!-- Add your screenshot here. Drag the PNG into this file on GitHub, or commit it
     to the repo and reference it like below. -->
<img src="screenshots/app.png" alt="Currency Converter screen" width="300"/>

## Features

- Convert between 8 currencies (USD, EUR, GBP, TRY, JPY, CHF, CAD, AUD)
- Live exchange rates from the [Frankfurter API](https://frankfurter.dev) (ECB data, no API key)
- Swap source and target currencies with one tap
- Loading and error states so the UI stays responsive
- Clean, single-screen Material 3 interface

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** ViewModel with a single immutable UI-state model
- **Concurrency:** Kotlin Coroutines (network calls run off the main thread)
- **Networking:** REST call to the Frankfurter API, JSON parsed with `org.json`
- **Min SDK:** 30 (Android 11)

## How it works

The app keeps all screen state in one `UiState` data class held by `ConverterViewModel`.
When the user taps **Convert**, the ViewModel launches a coroutine on a background
dispatcher, calls the Frankfurter `/latest` endpoint, reads the rate, and multiplies it
by the entered amount. Compose observes the state and redraws automatically — the UI
itself holds no logic.

## Running it locally

1. Clone the repo and open it in Android Studio.
2. Let Gradle sync.
3. Run on an emulator or a device (API 30+).

No API key or configuration needed.

## Possible improvements

Things I'd add next to take this from a learning project toward production quality:

- Replace `URL().readText()` + `org.json` with **Retrofit + Moshi**
- Move networking into a **Repository** for cleaner separation (full MVVM)
- Cache the latest rates with **Room** so the app works offline
- Add unit tests for the conversion logic

## License

Free to use for learning purposes.