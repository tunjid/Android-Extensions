# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Core [Unreleased]

### Added

- Added `doOnEveryEvent` as shorthand for adding a `LifeCycleObserver`

## ViewPager2 [Unreleased]

### Added

- `FragmentListAdapter` for easy and efficient management of `Fragment` instances in a `FragmentStateAdapter`

## Core [1.3.1]

### Fixed

- Fixed callback of service being bound in `HardServiceConnection` firing before the `Service` has been assigned to it's `var`

## Navigation [1.2.1]

### Added

- Allowed `MultiStackNavigator` to have different back stack types as defined by
``` kotlin
    sealed class BackStackType {
        object UniqueEntries : BackStackType()
        object Unlimited : BackStackType()
        data class Restricted(val count: Int) : BackStackType()
    }
```

## View [1.2.0]

### Changed

- Maintenance update, bumped core androidx versions

## RecyclerView [1.2.0]

### Added

- `RecyclerViewMultiScroller` to synchronize scrolling of multiple `RecyclerView` instances

## Material [1.0.4]

### Changed

- Maintenance update, bumped core androidx versions

## Communications [1.0.0]

### Changed

- Initial release

## SavedState [1.0.1]

### Changed

- Maintenance update, bumped core androidx versions