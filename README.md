# Profylish 🚀

**Profylish** is a modern, highly modular, and scalable Android application designed to bridge career development, professional language learning, and gamified skill enhancement. Built from the ground up using **Clean Architecture** principles, the project emphasizes separation of concerns, testability, and a robust multi-module codebase.

---

## 🏗️ Architectural Overview & Modular Design

Profylish follows a strict **Clean Architecture** and **Modularization** strategy to ensure high maintainability and independent feature scalability. The project is divided into core system layers and isolated feature modules:

- **`app`**: The central entry point orchestrating dependency injection, navigation graphs, and application-level configurations.
- **`core:*`**: Reusable infrastructure and domain logic modules:
  - `core:network`: API communication, client setups, and remote service interfaces.
  - `core:database` & `core:datastore`: Local persistence, caching, and secure preference storage.
  - `core:domain` & `core:model`: Shared business logic, use cases, and core entity models.
  - `core:ui` & `core:navigation`: Design system components, reusable widgets, and navigation contracts.
- **`feature:*`**: Independent, feature-specific presentation and logic layers:
  - `feature/auth`: User authentication and security pipelines.
  - `feature/onboarding`: Interactive user onboarding workflows.
  - `feature/home` & `feature/lesson-system`: Core gamified learning engines and structured content delivery.
  - `feature/shop` & `feature/leaderboard`: Monetization, rewards, and competitive gamification mechanics.
  - `feature/profile` & `feature/settings`: User state management and application configurations.

---

## ✨ Core Features

- **Gamified Learning System**: Interactive lessons and adaptive training modules designed for career-oriented professional development.
- **AI-Powered Integration**: Powered by **Gemini AI** pipelines to deliver dynamic content, personalized recommendations, and intelligent feedback.
- **Backend-as-a-Service (BaaS)**: Fully integrated with **Supabase** for secure authentication, real-time database sync, and cloud storage.
- **Robust State Management**: Built using modern reactive programming paradigms (Kotlin Coroutines & Flow) ensuring seamless UI synchronization.
- **AdMob Integration**: Prepared with modular monetization hooks for seamless advertisement management.

---

## 📊 Open Research & Associated Datasets

Profylish is backed by rigorous data engineering. The semantic frameworks, vocabulary systems, and occupational crosswalks utilized within the ecosystem are derived from open-source structured datasets published for Natural Language Processing (NLP) and Labor Market Analytics:

1. **English Career & Occupation Terminology Dictionary**
   - *Description:* Specialized professional vocabulary dataset tailored for ESP (English for Specific Purposes) and career-oriented AI models.
   - [Kaggle Dataset](https://www.kaggle.com/datasets/vedatturkkal/english-career-and-occupation-terminology-dictionary/data) | [Hugging Face Dataset](https://huggingface.co/datasets/VedatTurkkal/english_career_occupation_terminology_dictionary)

2. **ISCO-08 and O*NET Occupation Crosswalk Dataset**
   - *Description:* High-utility cross-reference mapping international employment standards with competency frameworks.
   - [Kaggle Dataset](https://www.kaggle.com/datasets/vedatturkkal/isco-08-and-onet-occupation-crosswalk-dataset/data) | [Hugging Face Dataset](https://huggingface.co/datasets/VedatTurkkal/isco-08_and_onet_occupation_crosswalk_dataset)

---

## 🛠️ Tech Stack & Libraries

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **Architecture**: Clean Architecture, Multi-Module MVVM
- **Backend & Auth**: [Supabase](https://supabase.com/)
- **Artificial Intelligence**: [Google Gemini AI](https://ai.google.dev/)
- **Asynchronous Programming**: Kotlin Coroutines & Flow
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 📄 License

This project is licensed under the terms of the [MIT License](LICENSE).
