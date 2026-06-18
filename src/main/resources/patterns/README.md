# Patterns Directory

Place your screenshot patterns here:

1. **mention_icon.png** - Screenshot of the @ mention icon from Telegram Web chat list
2. **unread_badge.png** - Screenshot of the unread message badge (number indicator)
3. **dm_icon.png** - Screenshot of direct message icon

## How to capture patterns

1. Open Telegram Web (https://web.telegram.org)
2. Use screenshot tool (Win+Shift+S on Windows)
3. Capture the specific icon/element
4. Save as PNG in this directory
5. Rebuild the project: `mvn clean package`

## Tips

- Keep patterns small (50x50 to 100x100 pixels)
- Capture in the same resolution you'll use for monitoring
- Update patterns when Telegram UI changes
- Use image editor to crop tightly around the icon
