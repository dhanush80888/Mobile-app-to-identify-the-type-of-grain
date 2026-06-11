# Grain Classifier Android App

## Team Details

### Project Title
Grain Classification Using Machine Learning on Android

### Team Leader
| Name | USN |
|--------|--------|
| K Dhanush kumar | KUB23CSE055 |

### Team Members
| Name | USN |
|--------|--------|
| Abdul khuddus khazi | KUB23CSE002 |
| Mohammed Junaid | KUB23CSE086 |

---

## Project Description

Grain Classifier is an Android application that uses Machine Learning to identify different types of grains from images. Users can capture a photo using the device camera or upload an image from the gallery. The application processes the image using a TensorFlow Lite model trained with Google Teachable Machine and predicts the grain type along with a confidence score.

The project demonstrates the integration of Artificial Intelligence and Android development for real-world agricultural applications.

---

## Features & Capabilities

- **On-Device Machine Learning Classification**: Fast, offline grain type prediction using a TensorFlow Lite (`.tflite`) model trained with Google Teachable Machine.
- **Real-Time Camera Scanner**: Real-time image analyzer (`GrainImageAnalyzer`) utilizing CameraX. Smooth UI updates with a 10-sample majority voting stabilization filter that minimizes flickering and ignores predictions under 50% confidence.
- **Single Capture & Upload**: Capture high-quality images via camera or select existing files from the gallery for detailed diagnostic analysis.
- **Local History Database**: Room Database integration to persistently save all classification results (including confidence scores, timestamps, and image paths).
- **History Management**: Browse and delete past classification records directly in-app, complete with a reliable "Undo" option via SnackBar alerts.
- **Interactive Grains Dashboard**: Direct home screen horizontal list showing supported grains with advanced metrics:
  - Classification standards (e.g. Long/Medium/Short Grain, Hard Red Winter, Yellow Dent)
  - Moisture thresholds (e.g. <14.0% for Rice, <12.0% for Bajra)
  - Average kernel dimensions (e.g. 5.5mm - 7.5mm)
  - Quality evaluation parameters (e.g. Chalkiness index, Gluten index, Aflatoxin ratio)
- **Premium Material 3 UI**: Modern visual layout featuring:
  - Sidebar Navigation Drawer for quick access to all screens.
  - Dynamic HSL color states that adapt the interface layout (tags, progress bars) to the matched grain type.
  - Interactive Settings Panel allowing toggles for **Dark Mode** and notifications.
  - Inline instructional menus and exit confirmation overlays.

---

## Technologies & Architecture

- **Language**: Kotlin
- **Development Tooling**: Android Studio (Gradle build environment configured with Kotlin DSL)
- **Machine Learning**: TensorFlow Lite Interpreter
- **Camera APIs**: Jetpack CameraX (Preview & ImageAnalysis components)
- **Storage & Database**: Jetpack Room Database, DAO patterns, and Repository architecture
- **State Management**: Android Architecture Components (ViewModel & LiveData)
- **Asynchronous Tasks**: Kotlin Coroutines & Executors
- **UI Framework**: Material Design 3, ViewBinding, Navigation Drawer, Custom Adapters (RecyclerView)

---

## Supported Grains & Standards

| Grain | Botanical Name | Target Moisture Threshold | Average Length/Diameter |
| :--- | :--- | :--- | :--- |
| **Rice** | *Oryza sativa* | < 14.0% | 5.5mm - 7.5mm |
| **Wheat** | *Triticum aestivum* | 12.0% - 13.5% | 6.0mm - 8.0mm |
| **Maize** | *Zea mays* | < 15.0% | 8.0mm - 10.0mm |
| **Bajra** | *Pennisetum glaucum* | < 12.0% | 2.0mm - 3.0mm |
| **Ragi** | *Eleusine coracana* | < 11.0% | 1.5mm - 2.0mm |

---



