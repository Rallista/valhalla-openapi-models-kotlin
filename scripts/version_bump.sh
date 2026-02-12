#!/bin/bash
set -e

BUMP_TYPE="${1:-patch}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SETTINGS_FILE="$SCRIPT_DIR/../settings.gradle.kts"

CURRENT_VERSION=$(grep 'libraryVersion' "$SETTINGS_FILE" | sed 's/.*"\([0-9]*\.[0-9]*\.[0-9]*\)".*/\1/')

if [ -z "$CURRENT_VERSION" ]; then
  echo "Error: Could not find libraryVersion in settings.gradle.kts" >&2
  exit 1
fi

IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

case "$BUMP_TYPE" in
  major) MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0 ;;
  minor) MINOR=$((MINOR + 1)); PATCH=0 ;;
  patch) PATCH=$((PATCH + 1)) ;;
  *)
    echo "Error: Invalid bump type '$BUMP_TYPE'. Use 'major', 'minor', or 'patch'." >&2
    exit 1
    ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"

if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' "s/libraryVersion.*=.*\"$CURRENT_VERSION\"/libraryVersion\"] = \"$NEW_VERSION\"/" "$SETTINGS_FILE"
else
  sed -i "s/libraryVersion.*=.*\"$CURRENT_VERSION\"/libraryVersion\"] = \"$NEW_VERSION\"/" "$SETTINGS_FILE"
fi

echo "Bumped version: $CURRENT_VERSION -> $NEW_VERSION"

# Output for GitHub Actions
if [ -n "$GITHUB_OUTPUT" ]; then
  echo "version=$NEW_VERSION" >> "$GITHUB_OUTPUT"
fi