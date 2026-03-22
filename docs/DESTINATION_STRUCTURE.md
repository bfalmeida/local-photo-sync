# Destination Structure

## Folder Hierarchy

Files are organized in a year/month hierarchy with separate folders for photos and videos:

```
<destination>/
└── YYYY/                    # Year folders (4-digit)
    └── MM/                  # Month folders (2-digit)
        ├── Photos/
        │   ├── [media files]
        │   └── WhatsApp/
        │       └── [WhatsApp media files]
        └── Videos/
            ├── [media files]
            └── WhatsApp/
                └── [WhatsApp media files]
```

## Media Types

### Photos
Common image formats are supported and placed in the Photos folder.

### Videos
Video files are placed in the Videos folder.

## WhatsApp Detection

WhatsApp files are identified by characteristics in their filename after the date portion. These files are placed in a dedicated WhatsApp subfolder within Photos or Videos based on media type.

## Date Extraction

Dates are extracted from filenames using this priority order:

1. **Filename date**: Extracted from patterns in the filename
2. **EXIF metadata**: Used only if filename has no date (images only)
3. **Filesystem timestamp**: Used only if no other source available

The folder structure (YYYY/MM) is determined from the extracted date.

## Special Cases

### Duplicates
Files with the same name already in the destination are skipped silently without renaming.

### Undated Files
Files without an extractable date are either:
- Moved to a configurable undated folder, or
- Skipped entirely

### Folder Creation
Folders are created only when needed:
- Year/month folders created when files exist for that date
- WhatsApp subfolder created only when WhatsApp files exist for that month
- Undated folder created only when undated files are present
