# Cool Todo App - Project Structure

## Package Structure

```
com.example.todoapp/
├── data/
│   ├── local/
│   │   ├── TodoDatabase.kt          # Room database configuration
│   │   ├── TaskEntity.kt            # Room entity for tasks
│   │   ├── TaskDao.kt               # Data access object
│   │   └── Converters.kt            # Room type converters
│   ├── repository/
│   │   └── TodoRepositoryImpl.kt    # Repository implementation
│   └── mapper/
│       └── TaskMapper.kt            # Entity-Domain model mappers
├── domain/
│   ├── model/
│   │   ├── TodoTask.kt              # Domain model for tasks
│   │   ├── Priority.kt              # Priority enum
│   │   └── Category.kt              # Category enum
│   └── repository/
│       └── TodoRepository.kt        # Repository interface
├── presentation/
│   ├── TodoViewModel.kt             # Main ViewModel
│   ├── TodoUiState.kt               # UI state data class
│   ├── screen/
│   │   └── TaskListScreen.kt        # Main task list screen
│   └── navigation/
│       └── TodoNavigation.kt        # Navigation setup
├── di/
│   ├── DatabaseModule.kt            # Database dependency injection
│   └── RepositoryModule.kt          # Repository dependency injection
└── TodoApplication.kt               # Application class with Hilt
```

## Dependencies Added

- **Room**: Database persistence
- **Hilt**: Dependency injection
- **Navigation Compose**: Screen navigation
- **ViewModel**: State management
- **Coroutines**: Asynchronous programming
- **WorkManager**: Background tasks (for notifications)
- **Core Library Desugaring**: Java 8+ time APIs support

## Architecture

The project follows MVVM + Repository pattern with clean architecture principles:

- **Data Layer**: Room database, entities, DAOs, repository implementation
- **Domain Layer**: Business models, repository interfaces
- **Presentation Layer**: ViewModels, UI state, Compose screens

## Build Configuration

- Minimum SDK: 24
- Target SDK: 36
- Kotlin version: 2.0.21
- Compose BOM: 2024.09.00
- Core library desugaring enabled for Java 8+ time APIs