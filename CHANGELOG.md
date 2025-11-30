# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.0](https://github.com/yshrsmz/historian/compare/historian-v0.4.0...historian-v0.5.0) (2025-11-30)


### ⚠ BREAKING CHANGES

* Minimum SDK raised from 15 to 21

### Features

* Modernize Gradle build configuration ([c8c190d](https://github.com/yshrsmz/historian/commit/c8c190d))
* Migrate build scripts from Groovy to Kotlin DSL ([baebc9b](https://github.com/yshrsmz/historian/commit/baebc9b))
* Migrate dependency management to Gradle Version Catalog ([0a6c130](https://github.com/yshrsmz/historian/commit/0a6c130))

### Changed

* **Breaking**: Minimum SDK raised from 15 to 21
* Target SDK updated from 30 to 35
* Compile SDK updated to 35
* Android Gradle Plugin updated from 4.1.3 to 8.13.1
* Gradle wrapper updated from 6.8.3 to 8.14.3
* Java compatibility set to Java 11

### Removed

* Removed deprecated jcenter() repository
* Removed AutoService dependency (replaced with robolectric.properties for tests)

### Bug Fixes

* **deps:** update dependency androidx.appcompat:appcompat to v1.7.1 ([6b53233](https://github.com/yshrsmz/historian/commit/6b5323354e1e0d61248a392ee3d73997c75a6ec5))
* **deps:** update dependency com.google.android.material:material to v1.13.0 ([#24](https://github.com/yshrsmz/historian/issues/24)) ([8d669b6](https://github.com/yshrsmz/historian/commit/8d669b695218821cb8c739736406198b9ec18c68))
* **deps:** update dependency org.robolectric:robolectric to v4.16 ([#25](https://github.com/yshrsmz/historian/issues/25)) ([dde3856](https://github.com/yshrsmz/historian/commit/dde3856dff6fd66c2e58390ac49fc634863b92fe))
* make signing conditional on credential availability ([2168b9f](https://github.com/yshrsmz/historian/commit/2168b9ff5c10df6f55b4a246f8340db95adc8691))


### Documentation

* add conventional commits section to CLAUDE.md ([57aca18](https://github.com/yshrsmz/historian/commit/57aca18fdc076198b3d1486702e42534b0836a9d))

## [0.4.0] - 2021/03/21

### Changed
- Target SDK version is now 30
- AGP 4.1.3
- androidx
- remove appcompat dependency

## [0.3.1] - 2017/02/20

### Changed
- Historian now automatically skip logs whose message is null or empty

### Added
- add tag column


## v0.2.0 - 2017/02/14

### Changed

- change column name `timestamp` -> `created_at`


## [0.1.0] - 2017/02/10

Initial Release.
