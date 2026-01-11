# Context Basket

IntelliJ plugin for collecting files and copying their contents to clipboard — built for AI prompting workflows.

## Features

- **Add files** — Right-click files/folders → "Add to Context Basket"
- **View selection** — Tool window shows all files in basket
- **Copy to clipboard** — One click to copy all file contents

## Quick Start

### 1. Build and Run

```bash
./gradlew runIde
```

This launches a sandbox IDE with the plugin installed.

### 2. Test the Plugin

1. Open any project in the sandbox IDE
2. Right-click a file → **Add to Context Basket**
3. Open **Context Basket** tool window (right sidebar)
4. Click **Copy to Clipboard**

## Commands

```bash
./gradlew runIde           # Run plugin in sandbox IDE
./gradlew build            # Build plugin
./gradlew buildPlugin      # Build distributable ZIP
```

## Project Structure

```
src/main/kotlin/com/example/contextbasket/
├── service/
│   └── BasketService.kt           # In-memory file storage (per-project)
├── action/
│   ├── AddToBasketAction.kt       # Right-click "Add to Context Basket"
│   └── RemoveFromBasketAction.kt  # Right-click "Remove from Context Basket"
└── ui/
    ├── BasketToolWindowFactory.kt # Tool window factory
    ├── BasketToolWindowPanel.kt   # Main UI (file list + buttons)
    └── ProjectViewHighlighter.kt  # File highlighting in Project View
```

## Clipboard Output Format

```
==== /path/to/file1.kt ====
<file contents>

==== /path/to/file2.kt ====
<file contents>
```
