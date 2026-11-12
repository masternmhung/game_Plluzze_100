#!/usr/bin/env python3
"""
Script to package PUZZLE 100 into a pristine, complete Android Native project ZIP archive.
Ensures gradle-wrapper.jar, gradlew, gradlew.bat, KSP, Room, resources, and tests are packed.
"""

import os
import zipfile

ZIP_FILENAME = "PUZZLE_100_Android_Project.zip"

INCLUDE_FILES = [
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties",
    "gradlew",
    "gradlew.bat",
    "PROJECT_STRUCTURE.md"
]

INCLUDE_DIRS = [
    "gradle",
    "app"
]

def create_zip():
    print(f"Creating {ZIP_FILENAME}...")
    with zipfile.ZipFile(ZIP_FILENAME, "w", zipfile.ZIP_DEFLATED) as zipf:
        for f in INCLUDE_FILES:
            if os.path.exists(f):
                info = zipfile.ZipInfo(f)
                if f == "gradlew":
                    info.external_attr = 0o755 << 16  # preserve executable permissions
                else:
                    info.external_attr = 0o644 << 16
                with open(f, "rb") as fp:
                    zipf.writestr(info, fp.read())
                print(f"  + {f}")

        for d in INCLUDE_DIRS:
            for root, dirs, files in os.walk(d):
                for f in files:
                    path = os.path.join(root, f)
                    info = zipfile.ZipInfo(path)
                    if path.endswith("gradlew"):
                        info.external_attr = 0o755 << 16
                    else:
                        info.external_attr = 0o644 << 16
                    with open(path, "rb") as fp:
                        zipf.writestr(info, fp.read())
                    print(f"  + {path}")

    size_kb = os.path.getsize(ZIP_FILENAME) / 1024
    print(f"\nDone! Generated {ZIP_FILENAME} ({size_kb:.1f} KB)")

if __name__ == "__main__":
    create_zip()
