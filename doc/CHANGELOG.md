# Changelog

## 0.1.0 (2026-02-06)
### Fixed
- Fixed rust path resolution in ReflectionJVM and ReflectionNative to resolve relative to project root instead of Gradle daemon directory

### Added
- Pass CC_<target> and AR_<target> environment variables to cargo build, enabling cross-compilation of Rust crates that depend on C code via cc-rs (e.g. ring, openssl-sys, libsqlite3-sys)
- New REST API sample (`sample/restapi`) demonstrating HTTP client in Rust using Tokio, reqwest, and serde with the Dog API

## 0.0.27 (2025-08-07)
### Fixed
- Fixed the support for Cargo checking on Windows

## 0.0.26 (2025-08-06)
### Added
- Added support for a custom Rust path via
e.g. `cargo.dir=/Users/<user>/cargo_tmp/.cargo/bin`

### Changed
- Moved Rust version checking from initialization to Rust compilation time

## 0.0.25 (2025-07-20)
### Changed
Improved Rust code generation: when importing a single JNI primitive type, the import now uses the form ```use jni::sys::<primitive_type>;``` instead of ```use jni::sys::{<primitive_type>};```.
This change aligns better with Rust best practices and helps reduce lint warnings.

## 0.0.24 (2025-04-17)
### Added
- Support for Rust version constraints in `rustJni` configuration.  
  You can now define which Rust version is acceptable to compile your project using the `rustVersion` property.  
  Supported patterns include:
  - Exact version: `1.86.0`
  - Minimum version: `>=1.64.0`
  - Wildcards: `1.86.*`, `1.*.*`

## 0.0.23 (2025-03-31)
### Added

- Manual changes to the `config.toml` file are now preserved.  
  The plugin no longer overrides existing content, allowing developers to customize their configuration safely.

## 0.0.22 (2024-11-05)

### Added
- [Support configurable visibility for generated Java/Kotlin methods](https://github.com/andrefigas/RustJNI/issues/19)
- [Support kotlin implicit return](https://github.com/andrefigas/RustJNI/issues/18)
- Game sample


