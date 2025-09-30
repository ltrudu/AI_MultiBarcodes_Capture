# Common Regex Expressions for Barcode Filtering

This comprehensive guide provides robust regular expressions for filtering barcodes based on common data patterns. These expressions are optimized to minimize false positives and false negatives.

## 🔧 How to Combine Multiple Patterns

### Understanding Pattern Combination

When you need to match **multiple formats** with a single regex pattern, you can combine them using the **OR operator** (`|`). This allows you to create one pattern that matches any of several different formats.

### Basic Syntax

**Format**: `^(pattern1|pattern2|pattern3)$`

**Explanation**:
- `^` - Start of string
- `(` - Begin group
- `pattern1|pattern2|pattern3` - Match pattern1 OR pattern2 OR pattern3
- `)` - End group
- `$` - End of string

### Practical Example: Web Protocols

Let's combine HTTP, HTTPS, and FTP patterns from this document:

#### Individual Patterns:
- **HTTP**: `^http://[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(\:[0-9]{1,5})?(\/[^\s]*)?$`
- **HTTPS**: `^https://[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(\:[0-9]{1,5})?(\/[^\s]*)?$`
- **FTP**: `^ftp://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?$`

#### Combined Pattern:
```regex
^(http://[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(\:[0-9]{1,5})?(\/[^\s]*)?|https://[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(\:[0-9]{1,5})?(\/[^\s]*)?|ftp://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?)$
```

#### Simplified Alternative:
For web protocols, you can use this more efficient pattern:
```regex
^(https?|ftp)://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?$
```

**Explanation**:
- `https?` - Matches "http" or "https" (the `?` makes the 's' optional)
- `|ftp` - OR matches "ftp"

### More Examples

#### Phone Numbers (International + Local):
```regex
^(\+33[67][0-9]{8}|0[67][0-9]{8})$
```
**Matches**: `+33650203370` OR `0650203370`

#### Product Codes (Multiple Formats):
```regex
^([0-9]{12}|[0-9]{13}|[0-9]{8})$
```
**Matches**: UPC-A (12 digits) OR EAN-13 (13 digits) OR EAN-8 (8 digits)

#### Email + Phone:
```regex
^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}|\+?[0-9\s\-\(\)\.]{10,15})$
```
**Matches**: Email addresses OR phone numbers

### Quick Tips

1. **Keep it Simple**: Start with 2-3 patterns, then expand as needed
2. **Test Your Patterns**: Use online regex testers before implementing
3. **Order Matters**: Put more specific patterns before general ones
4. **Use Parentheses**: Always wrap your OR groups in parentheses
5. **Common Prefixes**: Look for shared parts to simplify (like `https?` for HTTP/HTTPS)

### Pattern Testing

**Test your combined patterns with these examples**:
- ✅ `http://example.com` - Should match
- ✅ `https://secure.example.com:443/path` - Should match
- ✅ `ftp://files.example.com/download` - Should match
- ❌ `invalid://bad.url` - Should NOT match

## 📋 Table of Contents

### 🌐 [Digital & Web Identifiers](#-digital--web-identifiers)
- [Web URLs (HTTP/HTTPS)](#web-urls)
- [IP Addresses (IPv4/IPv6)](#ip-addresses)
- [MAC Addresses](#mac-addresses)
- [Protocol-Specific URIs](#protocol-specific-uris)
- [Android System Schemes](#android-system-schemes)
- [Popular App Schemes](#popular-app-schemes)
- [Digital Object Identifiers (DOI, ORCID)](#digital-object-identifiers)

### 📱 [Device & Technology](#-device--technology)
- [Device Identifiers (IMEI, MEID)](#device-identifiers)
- [Mobile Phone Numbers by Country](#mobile-phone-numbers-by-country)

### 🆔 [Government & Legal Identifiers](#-government--legal-identifiers)
- [National Identity Cards by Country](#national-identity-cards-by-country)
- [Driver's License Numbers by Country](#drivers-license-numbers-by-country)
- [Social Security Numbers by Country](#social-security-numbers-by-country)
- [License Plate Numbers by Country](#license-plate-numbers-by-country)
- [Postal/ZIP Codes by Country](#postalzip-codes-by-country)
- [Financial & Legal Codes (ISIN, LEI)](#financial--legal-codes)

### 📚 [Publishing & Media Standards](#-publishing--media-standards)
- [Book & Publication Numbers (ISBN, ISSN)](#book--publication-numbers)
- [Media & Entertainment Codes (ISRC, ISAN)](#media--entertainment-codes)

### 🛒 [Retail & Product Identification](#-retail--product-identification)
- [Product Barcodes (UPC, EAN, GTIN)](#product-barcodes)
- [Global Trade & Location Numbers](#global-trade--location-numbers)

### 🏭 [Manufacturing & Industry](#-manufacturing--industry)
- [Industrial Identifiers](#industrial-identifiers)
- [Supply Chain & Logistics](#supply-chain--logistics)

### 🎯 [Usage Guidelines](#-usage-guidelines)
- [Combining Patterns](#combining-patterns)
- [Testing & Best Practices](#testing--best-practices)

---

## 🌐 Digital & Web Identifiers

### Web URLs

| URL Type | Regular Expression | Description |
|----------|-------------------|-------------|
| **HTTP URLs** | `^http://[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(\:[0-9]{1,5})?(\/[^\s]*)?$` | Complete HTTP URLs with optional ports and paths |
| **HTTPS URLs** | `^https://[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(\:[0-9]{1,5})?(\/[^\s]*)?$` | Complete HTTPS URLs with optional ports and paths |
| **Any Web URL** | `^https?://[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(\:[0-9]{1,5})?(\/[^\s]*)?$` | Both HTTP and HTTPS URLs |

**Examples:**
- `https://example.com`
- `https://api.company.com:8080/api/v1/data?param=value`
- `http://192.168.1.100:3500/endpoint`

### IP Addresses

| IP Type | Regular Expression | Description |
|---------|-------------------|-------------|
| **IPv4** | `^((25[0-5]\|2[0-4][0-9]\|[01]?[0-9][0-9]?)\.){3}(25[0-5]\|2[0-4][0-9]\|[01]?[0-9][0-9]?)$` | Standard IPv4 addresses (0.0.0.0 to 255.255.255.255) |
| **IPv4 with Port** | `^((25[0-5]\|2[0-4][0-9]\|[01]?[0-9][0-9]?)\.){3}(25[0-5]\|2[0-4][0-9]\|[01]?[0-9][0-9]?):[0-9]{1,5}$` | IPv4 addresses with port numbers |
| **IPv4 Private** | `^(10\.([0-9]\|[1-9][0-9]\|1[0-9][0-9]\|2[0-4][0-9]\|25[0-5])\.([0-9]\|[1-9][0-9]\|1[0-9][0-9]\|2[0-4][0-9]\|25[0-5])\.([0-9]\|[1-9][0-9]\|1[0-9][0-9]\|2[0-4][0-9]\|25[0-5])\|172\.(1[6-9]\|2[0-9]\|3[01])\.([0-9]\|[1-9][0-9]\|1[0-9][0-9]\|2[0-4][0-9]\|25[0-5])\.([0-9]\|[1-9][0-9]\|1[0-9][0-9]\|2[0-4][0-9]\|25[0-5])\|192\.168\.([0-9]\|[1-9][0-9]\|1[0-9][0-9]\|2[0-4][0-9]\|25[0-5])\.([0-9]\|[1-9][0-9]\|1[0-9][0-9]\|2[0-4][0-9]\|25[0-5]))$` | Private IPv4 ranges (10.x.x.x, 172.16-31.x.x, 192.168.x.x) |
| **IPv6** | `^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$` | Full IPv6 addresses (no compression) |
| **IPv6 Compressed** | `^(([0-9a-fA-F]{1,4}:){1,7}:\|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}\|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}\|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}\|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}\|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}\|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})\|:((:[0-9a-fA-F]{1,4}){1,7}\|:))$` | IPv6 addresses with zero compression (::) |
| **IPv6 Full Pattern** | `^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\|(([0-9a-fA-F]{1,4}:){1,7}:\|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}\|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}\|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}\|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}\|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}\|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})\|:((:[0-9a-fA-F]{1,4}){1,7}\|:)))$` | Complete IPv6 pattern (full and compressed) |

**Examples:**
- IPv4: `192.168.1.100`, `10.0.0.1`, `203.0.113.195`
- IPv4 with Port: `192.168.1.100:8080`, `10.0.0.1:443`
- IPv6: `2001:0db8:85a3:0000:0000:8a2e:0370:7334`
- IPv6 Compressed: `2001:db8:85a3::8a2e:370:7334`, `::1`

### MAC Addresses

| MAC Type | Regular Expression | Description | Example |
|----------|-------------------|-------------|---------|
| **Standard MAC (Colon)** | `^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$` | Standard MAC address with colon separators | 00:1B:44:11:3A:B7 |
| **Standard MAC (Hyphen)** | `^([0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}$` | Standard MAC address with hyphen separators | 00-1B-44-11-3A-B7 |
| **Cisco Format (Dot)** | `^([0-9A-Fa-f]{4}\.){2}[0-9A-Fa-f]{4}$` | Cisco format with dot separators every 4 digits | 001B.4411.3AB7 |
| **No Separators** | `^[0-9A-Fa-f]{12}$` | MAC address without any separators | 001B44113AB7 |
| **Any MAC Format** | `^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$\|^([0-9A-Fa-f]{4}\.){2}[0-9A-Fa-f]{4}$\|^[0-9A-Fa-f]{12}$` | Matches any common MAC address format | 00:1B:44:11:3A:B7 |

### Protocol-Specific URIs

| Protocol | Regular Expression | Description | Example |
|----------|-------------------|-------------|---------|
| **Email (mailto)** | `^mailto:[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(\?[a-zA-Z0-9=&%+-]*)?$` | Email addresses with mailto protocol | mailto:user@example.com |
| **FTP** | `^ftp://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?$` | File Transfer Protocol URLs | ftp://files.example.com/path |
| **SFTP** | `^sftp://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?$` | Secure File Transfer Protocol URLs | sftp://secure.example.com/file |
| **SSH** | `^ssh://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?$` | Secure Shell protocol | ssh://server.example.com:22 |
| **Telnet** | `^telnet://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?$` | Telnet protocol | telnet://host.example.com:23 |
| **LDAP** | `^ldaps?://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?$` | LDAP directory protocol | ldap://directory.example.com |
| **File Protocol** | `^file:///[^\s]*$` | Local file system access | file:///C:/Users/file.txt |
| **SMB/CIFS** | `^smb://[a-zA-Z0-9.-]+(/[^\s]*)?$` | Windows file sharing protocol | smb://server/share/file |
| **NFS** | `^nfs://[a-zA-Z0-9.-]+(/[^\s]*)?$` | Network File System protocol | nfs://server/export/path |
| **RDP** | `^rdp://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?$` | Remote Desktop Protocol | rdp://remote.example.com:3389 |
| **VNC** | `^vnc://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?$` | Virtual Network Computing | vnc://remote.example.com:5900 |
| **SIP** | `^sips?:[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(\:[0-9]{1,5})?$` | Session Initiation Protocol | sip:user@voip.example.com |
| **IRC** | `^ircs?://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?$` | Internet Relay Chat | irc://irc.example.com:6667 |
| **RTSP** | `^rtsp://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?$` | Real Time Streaming Protocol | rtsp://stream.example.com/video |
| **RTMP** | `^rtmps?://[a-zA-Z0-9.-]+(\:[0-9]{1,5})?(/[^\s]*)?$` | Real Time Messaging Protocol | rtmp://live.example.com/stream |

### Android System Schemes

| Scheme | Regular Expression | Description | Example |
|--------|-------------------|-------------|---------|
| **Intent** | `^intent://[^\s]*#Intent;[^\s]*end$` | Android intent URLs | intent://scan/#Intent;scheme=zxing;end |
| **Android App** | `^android-app://[a-zA-Z0-9._-]+(/[^\s]*)?$` | Android app deep links | android-app://com.example.app |
| **Market** | `^market://[^\s]*$` | Google Play Store links | market://details?id=com.app |
| **Content** | `^content://[a-zA-Z0-9._/-]+$` | Android content provider | content://media/external/images |
| **File** | `^file:///android_asset/[^\s]*$` | Android asset files | file:///android_asset/file.html |
| **Tel** | `^tel:\+?[0-9\s\-\(\)\.]+$` | Phone number dialing | tel:+1234567890 |
| **SMS** | `^sms:\+?[0-9\s\-\(\)\.]+(\?[^\s]*)?$` | SMS messaging | sms:+1234567890?body=Hello |
| **Geo** | `^geo:[0-9\-\.]+,[0-9\-\.]+(\?[^\s]*)?$` | Geographic coordinates | geo:37.7749,-122.4194 |
| **Maps** | `^maps:\?[^\s]*$` | Maps application | maps:?q=San+Francisco,CA |

### Popular App Schemes

| App | Regular Expression | Description | Example |
|-----|-------------------|-------------|---------|
| **Facebook** | `^fb://[^\s]*$` | Facebook app deep links | fb://profile/123456789 |
| **Instagram** | `^instagram://[^\s]*$` | Instagram app links | instagram://user?username=example |
| **Twitter/X** | `^twitter://[^\s]*$` | Twitter app deep links | twitter://user?screen_name=example |
| **WhatsApp** | `^whatsapp://[^\s]*$` | WhatsApp messaging | whatsapp://send?phone=1234567890 |
| **YouTube** | `^(youtube://\|vnd\.youtube://)[^\s]*$` | YouTube app links | youtube://watch?v=dQw4w9WgXcQ |
| **TikTok** | `^(tiktok://\|snssdk1233://)[^\s]*$` | TikTok app deep links | tiktok://user/@username |
| **Snapchat** | `^snapchat://[^\s]*$` | Snapchat app links | snapchat://add/username |
| **LinkedIn** | `^linkedin://[^\s]*$` | LinkedIn app deep links | linkedin://profile/12345 |
| **Telegram** | `^(tg://\|telegram://)[^\s]*$` | Telegram messaging | tg://resolve?domain=username |
| **Discord** | `^discord://[^\s]*$` | Discord app links | discord://channels/server/channel |
| **Slack** | `^slack://[^\s]*$` | Slack workspace links | slack://channel?team=T123&id=C456 |
| **Zoom** | `^(zoom://\|zoomus://)[^\s]*$` | Zoom meeting links | zoom://zoom.us/join?confno=123 |
| **Skype** | `^skype:[^\s]*$` | Skype calling/messaging | skype:username?call |
| **Signal** | `^sgnl://[^\s]*$` | Signal messenger | sgnl://linkdevice?uuid=123 |
| **Viber** | `^viber://[^\s]*$` | Viber messaging | viber://chat?number=1234567890 |
| **WeChat** | `^weixin://[^\s]*$` | WeChat messaging | weixin://dl/chat?username |
| **Line** | `^line://[^\s]*$` | Line messaging app | line://ti/p/~username |
| **Spotify** | `^spotify:[^\s]*$` | Spotify music streaming | spotify:track:4iV5W9uYEdYUVa79Axb7Rh |
| **Apple Music** | `^music://[^\s]*$` | Apple Music app | music://album/123456789 |
| **Netflix** | `^nflx://[^\s]*$` | Netflix streaming | nflx://www.netflix.com/title/123 |
| **Uber** | `^uber://[^\s]*$` | Uber ride sharing | uber://?action=setPickup&pickup=my_location |
| **Lyft** | `^lyft://[^\s]*$` | Lyft ride sharing | lyft://ridetype?id=lyft |
| **Airbnb** | `^airbnb://[^\s]*$` | Airbnb accommodation | airbnb://rooms/12345 |
| **Amazon** | `^(amazon://\|amzn://)[^\s]*$` | Amazon shopping | amazon://www.amazon.com/dp/B123 |
| **eBay** | `^ebay://[^\s]*$` | eBay marketplace | ebay://item/123456789 |
| **PayPal** | `^paypal://[^\s]*$` | PayPal payment | paypal://paypalme/username |
| **Venmo** | `^venmo://[^\s]*$` | Venmo payment | venmo://users/username |
| **CashApp** | `^cashme://[^\s]*$` | Cash App payment | cashme://$username |
| **Pinterest** | `^pinterest://[^\s]*$` | Pinterest social | pinterest://pin/123456789 |
| **Reddit** | `^reddit://[^\s]*$` | Reddit social platform | reddit://r/subreddit |
| **Twitch** | `^twitch://[^\s]*$` | Twitch streaming | twitch://stream/username |
| **GitHub** | `^github://[^\s]*$` | GitHub code repository | github://user/repo |
| **Dropbox** | `^dbapi-[0-9]+://[^\s]*$` | Dropbox cloud storage | dbapi-1://connect |
| **Google Drive** | `^googledrive://[^\s]*$` | Google Drive storage | googledrive://file/123abc |
| **OneDrive** | `^ms-onedrive://[^\s]*$` | Microsoft OneDrive | ms-onedrive://open?file=123 |
| **Teams** | `^msteams://[^\s]*$` | Microsoft Teams | msteams://l/meetup-join/123 |
| **Chrome** | `^googlechrome://[^\s]*$` | Google Chrome browser | googlechrome://navigate?url=example.com |
| **Firefox** | `^firefox://[^\s]*$` | Firefox browser | firefox://open-url?url=example.com |
| **Opera** | `^opera://[^\s]*$` | Opera browser | opera://open-url?url=example.com |
| **Safari** | `^x-web-search://[^\s]*$` | Safari browser search | x-web-search://?query=example |
| **Google Maps** | `^comgooglemaps://[^\s]*$` | Google Maps navigation | comgooglemaps://?q=San+Francisco |
| **Waze** | `^waze://[^\s]*$` | Waze navigation | waze://?q=San+Francisco |
| **Apple Maps** | `^maps://[^\s]*$` | Apple Maps navigation | maps://?q=San+Francisco |
| **TikTok** | `^musically://[^\s]*$` | TikTok (Musical.ly legacy) | musically://user/@username |
| **VSCO** | `^vsco://[^\s]*$` | VSCO photo editing | vsco://user/username |
| **Snapchat** | `^snapchat://[^\s]*$` | Snapchat camera app | snapchat://camera |
| **Clubhouse** | `^clubhouse://[^\s]*$` | Clubhouse audio chat | clubhouse://room/123 |
| **BeReal** | `^bereal://[^\s]*$` | BeReal social app | bereal://post/123 |

### Digital Object Identifiers

| Standard | Format | Regular Expression | Description | Example |
|----------|--------|-------------------|-------------|---------|
| **DOI** | Digital Object Identifier | `^10\.[0-9]{4,}/[-._;()/:\[\]a-zA-Z0-9]+$` | Digital Object Identifier | 10.1000/123456 |
| **ORCID** | 16 digits with hyphens | `^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{3}[0-9X]$` | Open Researcher Contributor ID | 0000-0000-0000-0000 |

---

## 📱 Device & Technology

### Device Identifiers

| Device Type | Regular Expression | Description |
|-------------|-------------------|-------------|
| **Standard IMEI** | `^[0-9]{15}$` | 15-digit IMEI number |
| **IMEI with Check Digit** | `^[0-9]{14}[0-9]$` | IMEI with Luhn algorithm validation |
| **IMEI/MEID Format** | `^[0-9A-F]{14}[0-9A-F]?$` | Supports both IMEI and MEID formats |

**Examples:**
- `123456789012345`
- `A1000012345678F`

### Mobile Phone Numbers by Country

| Country | Country Code | Regular Expression | Example |
|---------|-------------|-------------------|---------|
| **Algeria** | +213 | `^(\+213\|0)[567][0-9]{8}$` | +213555123456 / 0555123456 |
| **Argentina** | +54 | `^(\+54\|0)(11\|[2-9][0-9])[0-9]{7,8}$` | +541112345678 / 01112345678 |
| **Bangladesh** | +880 | `^(\+880\|0)[13-9][0-9]{8}$` | +8801712345678 / 01712345678 |
| **Brazil** | +55 | `^(\+55\|0)[1-9][1-9][0-9]{8}$` | +5511987654321 / 011987654321 |
| **Canada** | +1 | `^(\+1\|1)?[2-9][0-9]{2}[2-9][0-9]{6}$` | +14165551234 / 4165551234 |
| **China** | +86 | `^(\+86\|0)?(13[0-9]\|14[57]\|15[0-35-9]\|17[678]\|18[0-9])[0-9]{8}$` | +8613812345678 / 13812345678 |
| **Colombia** | +57 | `^(\+57\|0)?[13][0-9]{9}$` | +573123456789 / 3123456789 |
| **DR Congo** | +243 | `^(\+243\|0)[89][0-9]{8}$` | +243812345678 / 0812345678 |
| **Egypt** | +20 | `^(\+20\|0)[12][0-9]{8}$` | +201234567890 / 01234567890 |
| **Ethiopia** | +251 | `^(\+251\|0)[79][0-9]{8}$` | +251912345678 / 0912345678 |
| **France** | +33 | `^(\+33\|0)[67][0-9]{8}$` | +33650203370 / 0650203370 |
| **Germany** | +49 | `^(\+49\|0)[0-9]{10,11}$` | +4915123456789 / 015123456789 |
| **Ghana** | +233 | `^(\+233\|0)[2-5][0-9]{8}$` | +233241234567 / 0241234567 |
| **India** | +91 | `^(\+91\|0)?[6-9][0-9]{9}$` | +919876543210 / 9876543210 |
| **Indonesia** | +62 | `^(\+62\|0)[8][1-9][0-9]{7,9}$` | +62812345678 / 0812345678 |
| **Iran** | +98 | `^(\+98\|0)[9][0-9]{9}$` | +989123456789 / 09123456789 |
| **Iraq** | +964 | `^(\+964\|0)[7][0-9]{9}$` | +9647812345678 / 07812345678 |
| **Italy** | +39 | `^(\+39\|0)?[3][0-9]{9}$` | +393123456789 / 3123456789 |
| **Japan** | +81 | `^(\+81\|0)[7-9][0-9]{8}$` | +818012345678 / 08012345678 |
| **Kenya** | +254 | `^(\+254\|0)[7][0-9]{8}$` | +254712345678 / 0712345678 |
| **Madagascar** | +261 | `^(\+261\|0)[23][0-9]{7}$` | +26132123456 / 032123456 |
| **Malaysia** | +60 | `^(\+60\|0)[1][0-9]{8,9}$` | +60123456789 / 0123456789 |
| **Mexico** | +52 | `^(\+52\|0)?[1]?[0-9]{10}$` | +5215512345678 / 5512345678 |
| **Morocco** | +212 | `^(\+212\|0)[67][0-9]{8}$` | +212612345678 / 0612345678 |
| **Myanmar** | +95 | `^(\+95\|0)[9][0-9]{8,9}$` | +959123456789 / 09123456789 |
| **Nepal** | +977 | `^(\+977\|0)?[9][8][0-9]{8}$` | +9779812345678 / 9812345678 |
| **Nigeria** | +234 | `^(\+234\|0)[78][0-9]{9}$` | +2348123456789 / 08123456789 |
| **Pakistan** | +92 | `^(\+92\|0)[3][0-9]{9}$` | +923123456789 / 03123456789 |
| **Peru** | +51 | `^(\+51\|0)?[9][0-9]{8}$` | +51987654321 / 987654321 |
| **Philippines** | +63 | `^(\+63\|0)[9][0-9]{9}$` | +639123456789 / 09123456789 |
| **Poland** | +48 | `^(\+48\|0)?[5-9][0-9]{8}$` | +48512345678 / 512345678 |
| **Russia** | +7 | `^(\+7\|8)[9][0-9]{9}$` | +79123456789 / 89123456789 |
| **Saudi Arabia** | +966 | `^(\+966\|0)[5][0-9]{8}$` | +966512345678 / 0512345678 |
| **South Africa** | +27 | `^(\+27\|0)[6-8][0-9]{8}$` | +27821234567 / 0821234567 |
| **South Korea** | +82 | `^(\+82\|0)[1][0-9]{8,9}$` | +821012345678 / 01012345678 |
| **Spain** | +34 | `^(\+34\|0)?[67][0-9]{8}$` | +34612345678 / 612345678 |
| **Sri Lanka** | +94 | `^(\+94\|0)[7][0-9]{8}$` | +94712345678 / 0712345678 |
| **Tanzania** | +255 | `^(\+255\|0)[67][0-9]{8}$` | +255712345678 / 0712345678 |
| **Thailand** | +66 | `^(\+66\|0)[689][0-9]{8}$` | +66812345678 / 0812345678 |
| **Turkey** | +90 | `^(\+90\|0)[5][0-9]{9}$` | +905123456789 / 05123456789 |
| **Ukraine** | +380 | `^(\+380\|0)[6-9][0-9]{8}$` | +380671234567 / 0671234567 |
| **United Kingdom** | +44 | `^(\+44\|0)[7][0-9]{9}$` | +447712345678 / 07712345678 |
| **United States** | +1 | `^(\+1\|1)?[2-9][0-9]{2}[2-9][0-9]{6}$` | +14155551234 / 4155551234 |
| **Uzbekistan** | +998 | `^(\+998\|0)?[9][0-9]{8}$` | +998901234567 / 901234567 |
| **Venezuela** | +58 | `^(\+58\|0)[4][0-9]{9}$` | +584123456789 / 04123456789 |
| **Vietnam** | +84 | `^(\+84\|0)[3-9][0-9]{8}$` | +84912345678 / 0912345678 |

---

## 🆔 Government & Legal Identifiers

### National Identity Cards by Country

| Country | ID Format | Regular Expression | Example |
|---------|-----------|-------------------|---------|
| **Algeria** | 18 digits | `^[0-9]{18}$` | 123456789012345678 |
| **Argentina** | DNI: 8 digits | `^[0-9]{8}$` | 12345678 |
| **Bangladesh** | NID: 10 or 13 digits | `^[0-9]{10}([0-9]{3})?$` | 1234567890 |
| **Brazil** | CPF: 11 digits | `^[0-9]{3}\.[0-9]{3}\.[0-9]{3}-[0-9]{2}$` | 123.456.789-01 |
| **Canada** | SIN: 9 digits | `^[0-9]{3}-[0-9]{3}-[0-9]{3}$` | 123-456-789 |
| **China** | 18 digits | `^[1-9][0-9]{5}(19\|20)[0-9]{2}(0[1-9]\|1[0-2])(0[1-9]\|[12][0-9]\|3[01])[0-9]{3}[0-9X]$` | 110101199001011234 |
| **Colombia** | Cédula: 8-10 digits | `^[0-9]{8,10}$` | 12345678 |
| **DR Congo** | Variable format | `^[A-Z0-9]{8,15}$` | ABC123456789 |
| **Egypt** | 14 digits | `^[0-9]{14}$` | 12345678901234 |
| **Ethiopia** | Variable format | `^[A-Z0-9]{8,12}$` | AB1234567890 |
| **France** | INSEE: 15 digits | `^[12][0-9]{2}(0[1-9]\|1[0-2])[0-9AB][0-9]{4}[0-9]{3}[0-9]{2}$` | 123456789012345 |
| **Germany** | 11 digits | `^[0-9]{11}$` | 12345678901 |
| **Ghana** | 15 characters | `^GHA-[0-9]{9}-[0-9]$` | GHA-123456789-1 |
| **India** | Aadhaar: 12 digits | `^[2-9][0-9]{11}$` | 234567890123 |
| **Indonesia** | NIK: 16 digits | `^[0-9]{16}$` | 1234567890123456 |
| **Iran** | 10 digits | `^[0-9]{10}$` | 1234567890 |
| **Iraq** | Variable format | `^[0-9]{8,12}$` | 123456789012 |
| **Italy** | Codice Fiscale | `^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$` | RSSMRA80A01H501A |
| **Japan** | My Number: 12 digits | `^[0-9]{12}$` | 123456789012 |
| **Kenya** | 8 digits | `^[0-9]{8}$` | 12345678 |
| **Madagascar** | 12 digits | `^[0-9]{12}$` | 123456789012 |
| **Malaysia** | MyKad: 12 digits | `^[0-9]{6}-[0-9]{2}-[0-9]{4}$` | 123456-78-9012 |
| **Mexico** | CURP: 18 characters | `^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[0-9A-Z][0-9]$` | ABCD123456HDFRNN09 |
| **Morocco** | CIN: 8 characters | `^[A-Z]{1,2}[0-9]{6}$` | A123456 |
| **Myanmar** | NRC: Variable | `^[0-9]{1,2}/[A-Z]{6}\([A-Z]\)[0-9]{6}$` | 12/ABCDEF(N)123456 |
| **Nepal** | 10 digits | `^[0-9]{10}$` | 1234567890 |
| **Nigeria** | NIN: 11 digits | `^[0-9]{11}$` | 12345678901 |
| **Pakistan** | CNIC: 13 digits | `^[0-9]{5}-[0-9]{7}-[0-9]$` | 12345-1234567-1 |
| **Peru** | DNI: 8 digits | `^[0-9]{8}$` | 12345678 |
| **Philippines** | 12 digits | `^[0-9]{4}-[0-9]{4}-[0-9]{4}$` | 1234-5678-9012 |
| **Poland** | PESEL: 11 digits | `^[0-9]{11}$` | 12345678901 |
| **Russia** | 10 digits | `^[0-9]{10}$` | 1234567890 |
| **Saudi Arabia** | 10 digits | `^[12][0-9]{9}$` | 1234567890 |
| **South Africa** | 13 digits | `^[0-9]{13}$` | 1234567890123 |
| **South Korea** | 13 digits | `^[0-9]{6}-[1-4][0-9]{6}$` | 123456-1234567 |
| **Spain** | DNI: 8 digits + letter | `^[0-9]{8}[A-Z]$` | 12345678A |
| **Sri Lanka** | NIC: 10 or 12 characters | `^([0-9]{9}[VX]\|[0-9]{12})$` | 123456789V |
| **Tanzania** | Variable format | `^[0-9]{8,12}$` | 123456789012 |
| **Thailand** | 13 digits | `^[0-9]{13}$` | 1234567890123 |
| **Turkey** | T.C. No: 11 digits | `^[1-9][0-9]{10}$` | 12345678901 |
| **Ukraine** | 10 digits | `^[0-9]{10}$` | 1234567890 |
| **United Kingdom** | Variable formats | `^[A-Z]{2}[0-9]{6}[A-Z]$` | AB123456C |
| **United States** | SSN: 9 digits | `^[0-9]{3}-[0-9]{2}-[0-9]{4}$` | 123-45-6789 |
| **Uzbekistan** | 9 digits | `^[0-9]{9}$` | 123456789 |
| **Venezuela** | Cédula: 7-8 digits | `^[VE]-[0-9]{7,8}$` | V-12345678 |
| **Vietnam** | CCCD: 12 digits | `^[0-9]{12}$` | 123456789012 |

### Driver's License Numbers by Country

| Country | License Format | Regular Expression | Example |
|---------|----------------|-------------------|---------|
| **Algeria** | 12 digits | `^[0-9]{12}$` | 123456789012 |
| **Argentina** | 8 digits | `^[0-9]{8}$` | 12345678 |
| **Bangladesh** | 13 characters | `^[A-Z]{2}-[0-9]{2}-[0-9]{7}$` | DH-85-1234567 |
| **Brazil** | 11 digits | `^[0-9]{11}$` | 12345678901 |
| **Canada** | Varies by province | `^[A-Z0-9]{5,12}$` | A1234567890 |
| **China** | 18 characters | `^[0-9]{17}[0-9X]$` | 123456789012345678 |
| **Colombia** | 8-10 digits | `^[0-9]{8,10}$` | 12345678 |
| **DR Congo** | Variable format | `^[A-Z0-9]{8,12}$` | ABC12345678 |
| **Egypt** | 14 digits | `^[0-9]{14}$` | 12345678901234 |
| **Ethiopia** | Variable format | `^[A-Z0-9]{8,12}$` | ET12345678 |
| **France** | 12 characters | `^[0-9]{2}[A-Z]{2}[0-9]{6}$` | 12AB345678 |
| **Germany** | 11 characters | `^[A-Z0-9]{11}$` | B123456789C |
| **Ghana** | 10 characters | `^GH-[0-9]{7}$` | GH-1234567 |
| **India** | 16 characters | `^[A-Z]{2}[0-9]{2}[0-9]{11}$` | MH021234567890 |
| **Indonesia** | 12 digits | `^[0-9]{12}$` | 123456789012 |
| **Iran** | 10 digits | `^[0-9]{10}$` | 1234567890 |
| **Iraq** | Variable format | `^[A-Z0-9]{8,12}$` | IQ12345678 |
| **Italy** | 10 characters | `^[A-Z]{2}[0-9]{7}[A-Z]$` | MI123456A |
| **Japan** | 12 digits | `^[0-9]{12}$` | 123456789012 |
| **Kenya** | 8 characters | `^[A-Z]{2}[0-9]{6}$` | KE123456 |
| **Madagascar** | 9 characters | `^[0-9]{9}$` | 123456789 |
| **Malaysia** | 13 characters | `^[A-Z][0-9]{12}$` | A123456789012 |
| **Mexico** | 18 characters | `^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[0-9A-Z][0-9]$` | ABCD123456HDFRNN09 |
| **Morocco** | 8 characters | `^[A-Z][0-9]{7}$` | A1234567 |
| **Myanmar** | Variable format | `^[A-Z0-9]{8,12}$` | MM12345678 |
| **Nepal** | 16 characters | `^[0-9]{16}$` | 1234567890123456 |
| **Nigeria** | 11 characters | `^[A-Z]{3}[0-9]{8}$` | ABC12345678 |
| **Pakistan** | 13 characters | `^[0-9]{5}-[0-9]{7}-[0-9]$` | 12345-1234567-1 |
| **Peru** | 9 characters | `^[A-Z][0-9]{8}$` | Q12345678 |
| **Philippines** | 13 characters | `^[A-Z][0-9]{2}-[0-9]{2}-[0-9]{6}$` | A01-23-456789 |
| **Poland** | 13 characters | `^[A-Z]{3}[0-9]{6}/[0-9]{2}$` | ABC123456/78 |
| **Russia** | 10 digits | `^[0-9]{2} [0-9]{2} [0-9]{6}$` | 12 34 567890 |
| **South Africa** | 13 characters | `^[0-9]{8}/[0-9]{2}/[0-9]{2}$` | 12345678/90/12 |
| **South Korea** | 12 characters | `^[0-9]{2}-[0-9]{2}-[0-9]{6}-[0-9]{2}$` | 12-34-567890-12 |
| **Spain** | 9 characters | `^[0-9]{8}[A-Z]$` | 12345678A |
| **Sri Lanka** | 8 characters | `^[A-Z][0-9]{7}$` | B1234567 |
| **Tanzania** | 9 characters | `^[A-Z]{2}[0-9]{7}$` | TZ1234567 |
| **Thailand** | 8 digits | `^[0-9]{8}$` | 12345678 |
| **Turkey** | 6 characters | `^[0-9]{2}[A-Z]{2}[0-9]{2}$` | 12AB34 |
| **Ukraine** | 9 characters | `^[A-Z]{3}[0-9]{6}$` | ABC123456 |
| **United Kingdom** | 16 characters | `^[A-Z]{5}[0-9]{6}[A-Z]{2}[0-9A-Z]{3}$` | SMITH123456AB9CD |
| **United States** | Varies by state | `^[A-Z0-9]{8,12}$` | A123456789 |
| **Uzbekistan** | 9 characters | `^[A-Z]{2}[0-9]{7}$` | UZ1234567 |
| **Vietnam** | 12 digits | `^[0-9]{12}$` | 123456789012 |

### Social Security Numbers by Country

| Country | SSN Format | Regular Expression | Example |
|---------|-----------|-------------------|---------|
| **Algeria** | CNAS: 15 digits | `^[0-9]{15}$` | 123456789012345 |
| **Argentina** | CUIL/CUIT: 11 digits | `^[0-9]{2}-[0-9]{8}-[0-9]$` | 20-12345678-9 |
| **Brazil** | PIS/PASEP: 11 digits | `^[0-9]{3}\.[0-9]{5}\.[0-9]{2}-[0-9]$` | 123.45678.90-1 |
| **Canada** | SIN: 9 digits | `^[0-9]{3}-[0-9]{3}-[0-9]{3}$` | 123-456-789 |
| **France** | INSEE: 15 digits | `^[12][0-9]{2}(0[1-9]\|1[0-2])[0-9AB][0-9]{4}[0-9]{3}[0-9]{2}$` | 123456789012345 |
| **Germany** | Sozialversicherungsnummer: 12 digits | `^[0-9]{2}[0-9]{6}[A-Z][0-9]{3}$` | 12123456A789 |
| **Ghana** | SSNIT: 13 characters | `^[A-Z][0-9]{12}$` | A123456789012 |
| **Indonesia** | BPJS: 13 digits | `^[0-9]{13}$` | 1234567890123 |
| **Italy** | Codice Fiscale | `^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$` | RSSMRA80A01H501A |
| **Japan** | Basic Pension Number: 10 digits | `^[0-9]{4}-[0-9]{6}$` | 1234-567890 |
| **Malaysia** | SOCSO: 12 digits | `^[0-9]{12}$` | 123456789012 |
| **Mexico** | NSS: 11 digits | `^[0-9]{11}$` | 12345678901 |
| **Morocco** | CNSS: 9 digits | `^[0-9]{9}$` | 123456789 |
| **Philippines** | SSS: 10 digits | `^[0-9]{2}-[0-9]{7}-[0-9]$` | 12-3456789-0 |
| **Poland** | PESEL (also SSN): 11 digits | `^[0-9]{11}$` | 12345678901 |
| **Russia** | SNILS: 11 digits | `^[0-9]{3}-[0-9]{3}-[0-9]{3} [0-9]{2}$` | 123-456-789 01 |
| **South Korea** | Resident Registration: 13 digits | `^[0-9]{6}-[1-4][0-9]{6}$` | 123456-1234567 |
| **Spain** | NSS: 12 digits | `^[0-9]{2} [0-9]{10}$` | 12 3456789012 |
| **Thailand** | Social Security: 13 digits | `^[0-9]{13}$` | 1234567890123 |
| **Turkey** | SGK: 11 digits | `^[0-9]{11}$` | 12345678901 |
| **Ukraine** | Social Security: 10 digits | `^[0-9]{10}$` | 1234567890 |
| **United Kingdom** | National Insurance: 9 characters | `^[A-Z]{2}[0-9]{6}[A-Z]$` | AB123456C |
| **United States** | SSN: 9 digits | `^[0-9]{3}-[0-9]{2}-[0-9]{4}$` | 123-45-6789 |
| **Vietnam** | Social Insurance: 10 digits | `^[0-9]{10}$` | 1234567890 |

### License Plate Numbers by Country

#### European Union Standard Format

| Country | Plate Format | Regular Expression | Example |
|---------|-------------|-------------------|--------|
| **Austria** | EU format | `^[A-Z]{1,2}-[0-9]{1,5}[A-Z]{0,2}$` | W-123AB |
| **Belgium** | EU format | `^[A-Z0-9]{1}-[A-Z]{3}-[0-9]{3}$` | 1-ABC-123 |
| **Bulgaria** | EU format | `^[A-Z]{2}[0-9]{4}[A-Z]{2}$` | CB1234AB |
| **Croatia** | EU format | `^[A-Z]{2}[0-9]{3,4}-[A-Z]{1,2}$` | ZG123-A |
| **Czech Republic** | EU format | `^[0-9][A-Z][0-9] [0-9]{4}$` | 1A2 3456 |
| **Denmark** | EU format | `^[A-Z]{2}[0-9]{2}[0-9]{3}$` | AB12345 |
| **Estonia** | EU format | `^[0-9]{3}[A-Z]{3}$` | 123ABC |
| **Finland** | EU format | `^[A-Z]{3}-[0-9]{3}$` | ABC-123 |
| **France** | EU format | `^[A-Z]{2}-[0-9]{3}-[A-Z]{2}$` | AA-123-BB |
| **Germany** | EU format | `^[A-Z]{1,3}-[A-Z]{1,2}[0-9]{1,4}$` | B-MW123 |
| **Greece** | EU format | `^[A-Z]{3}[0-9]{4}$` | ABC1234 |
| **Hungary** | EU format | `^[A-Z]{3}-[0-9]{3}$` | ABC-123 |
| **Ireland** | EU format | `^[0-9]{2,3}-[A-Z]{1,2}-[0-9]{1,6}$` | 12-D-34567 |
| **Italy** | EU format | `^[A-Z]{2}[0-9]{3}[A-Z]{2}$` | AB123CD |
| **Latvia** | EU format | `^[A-Z]{2}-[0-9]{4}$` | AB-1234 |
| **Lithuania** | EU format | `^[A-Z]{3} [0-9]{3}$` | ABC 123 |
| **Luxembourg** | EU format | `^[A-Z]{2}[0-9]{4}$` | AB1234 |
| **Malta** | EU format | `^[A-Z]{3}[0-9]{3}$` | ABC123 |
| **Netherlands** | EU format | `^[0-9]{2}-[A-Z]{3}-[0-9]$` | 12-ABC-3 |
| **Poland** | EU format | `^[A-Z]{2,3}[0-9]{4,5}$` | WA12345 |
| **Portugal** | EU format | `^[0-9]{2}-[A-Z]{2}-[0-9]{2}$` | 12-AB-34 |
| **Romania** | EU format | `^[A-Z]{1,2}[0-9]{2,3}[A-Z]{3}$` | B123ABC |
| **Slovakia** | EU format | `^[A-Z]{2}[0-9]{3}[A-Z]{2}$` | BA123CD |
| **Slovenia** | EU format | `^[A-Z]{2}[0-9]{3}[A-Z]{2}$` | LJ123AB |
| **Spain** | EU format | `^[0-9]{4}[A-Z]{3}$` | 1234ABC |
| **Sweden** | EU format | `^[A-Z]{3}[0-9]{3}$` | ABC123 |

#### Other Countries

| Country | Plate Format | Regular Expression | Example |
|---------|-------------|-------------------|---------|
| **Algeria** | 6 digits + 2 letters + 2 digits | `^[0-9]{6}-[A-Z]{2}-[0-9]{2}$` | 123456-AB-78 |
| **Argentina** | 3 letters + 3 digits | `^[A-Z]{3}[0-9]{3}$` | ABC123 |
| **Bangladesh** | Metro format | `^[A-Z]{4}-[0-9]{2}-[0-9]{4}$` | DHKA-12-3456 |
| **Brazil** | Mercosur format | `^[A-Z]{3}[0-9][A-Z][0-9]{2}$` | ABC1D23 |
| **Canada** | Varies by province | `^[A-Z0-9]{2,8}$` | ABC123 |
| **China** | 7 characters | `^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-Z]{5}$` | 京A12345 |
| **Colombia** | 3 letters + 3 digits | `^[A-Z]{3}[0-9]{3}$` | ABC123 |
| **DR Congo** | Variable format | `^[A-Z]{2,3}[0-9]{3,4}$` | CD1234 |
| **Egypt** | 3 digits + 3 letters | `^[0-9]{3} [أ-ي]{3}$` | 123 أبج |
| **Ethiopia** | 5 digits + 1 letter | `^[0-9]{5}[A-Z]$` | 12345A |
| **Ghana** | 2 letters + 4 digits + 2 letters | `^[A-Z]{2}-[0-9]{4}-[A-Z]{2}$` | GR-1234-AB |
| **India** | 2 letters + 2 digits + 2 letters + 4 digits | `^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}$` | DL01AB1234 |
| **Indonesia** | 1-2 letters + 4 digits + 2-3 letters | `^[A-Z]{1,2}[0-9]{4}[A-Z]{2,3}$` | B1234CD |
| **Iran** | 2 digits + 1 letter + 3 digits + 2 digits | `^[0-9]{2}[A-Z][0-9]{3}[0-9]{2}$` | 12A34567 |
| **Iraq** | 4 digits + 3 letters | `^[0-9]{4}[A-Z]{3}$` | 1234ABC |
| **Japan** | Regional format | `^[ひらがな][0-9]{2,3}[ひらがな][0-9]{2,4}$` | あ123あ45 |
| **Kenya** | 3 letters + 3 digits + 1 letter | `^[A-Z]{3}[0-9]{3}[A-Z]$` | KAA123B |
| **Madagascar** | 4 digits + 2 letters | `^[0-9]{4}[A-Z]{2}$` | 1234AB |
| **Malaysia** | 3 letters + 4 digits | `^[A-Z]{3}[0-9]{4}$` | ABC1234 |
| **Mexico** | 3 letters + 2 digits + 2 letters | `^[A-Z]{3}-[0-9]{2}-[A-Z]{2}$` | ABC-12-DE |
| **Morocco** | 5 digits + 1 letter + 2 digits | `^[0-9]{5}-[A-Z]-[0-9]{2}$` | 12345-A-67 |
| **Myanmar** | 2 letters + 4 digits | `^[A-Z]{2}[0-9]{4}$` | YA1234 |
| **Nepal** | Province format | `^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}$` | BA01PA1234 |
| **Nigeria** | 3 letters + 3 digits + 2 letters | `^[A-Z]{3}[0-9]{3}[A-Z]{2}$` | ABC123DE |
| **Pakistan** | 2 letters + 2 digits + 4 digits | `^[A-Z]{2}-[0-9]{2}-[0-9]{4}$` | AB-12-3456 |
| **Peru** | 3 letters + 3 digits | `^[A-Z]{3}-[0-9]{3}$` | ABC-123 |
| **Philippines** | 3 letters + 4 digits | `^[A-Z]{3}[0-9]{4}$` | ABC1234 |
| **Russia** | 1 letter + 3 digits + 2 letters + region | `^[A-Z][0-9]{3}[A-Z]{2}[0-9]{2,3}$` | A123BC77 |
| **Saudi Arabia** | 3 letters + 3 digits | `^[ا-ي]{3}[0-9]{3}$` | أبج123 |
| **South Africa** | 2 letters + 2 digits + 2 letters + 2 digits | `^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{2}$` | CA12AB34 |
| **South Korea** | 2-3 digits + 1 letter + 4 digits | `^[0-9]{2,3}[가-힣][0-9]{4}$` | 12가3456 |
| **Sri Lanka** | 2-3 letters + 4 digits | `^[A-Z]{2,3}[0-9]{4}$` | ABC1234 |
| **Tanzania** | 1 letter + 3 digits + 3 letters | `^[A-Z][0-9]{3}[A-Z]{3}$` | T123ABC |
| **Thailand** | 2 letters + 4 digits | `^[A-Z]{2}[0-9]{4}$` | AB1234 |
| **Turkey** | 2 digits + 3 letters + 2 digits | `^[0-9]{2} [A-Z]{3} [0-9]{2}$` | 34 ABC 12 |
| **Ukraine** | 2 letters + 4 digits + 2 letters | `^[A-Z]{2}[0-9]{4}[A-Z]{2}$` | AB1234CD |
| **United Kingdom** | 2 letters + 2 digits + 3 letters | `^[A-Z]{2}[0-9]{2}[A-Z]{3}$` | AB12CDE |
| **United States** | Varies by state | `^[A-Z0-9]{1,8}$` | ABC123 |
| **Uzbekistan** | 2 digits + 1 letter + 3 digits + 2 letters | `^[0-9]{2}[A-Z][0-9]{3}[A-Z]{2}$` | 01A234BC |
| **Venezuela** | 3 letters + 2 digits + 1 letter | `^[A-Z]{3}[0-9]{2}[A-Z]$` | ABC12D |
| **Vietnam** | 2 digits + 1 letter + 5 digits | `^[0-9]{2}[A-Z]-[0-9]{5}$` | 29A-12345 |

### Postal/ZIP Codes by Country

| Country | ZIP/Postal Format | Regular Expression | Example |
|---------|------------------|-------------------|---------|
| **Algeria** | 5 digits | `^[0-9]{5}$` | 16000 |
| **Argentina** | 4 digits or 8 characters | `^([0-9]{4}\|[A-Z][0-9]{4}[A-Z]{3})$` | 1000 / C1000AAA |
| **Bangladesh** | 4 digits | `^[0-9]{4}$` | 1000 |
| **Brazil** | 8 digits with hyphen | `^[0-9]{5}-[0-9]{3}$` | 01310-100 |
| **Canada** | 6 characters alternating | `^[A-Z][0-9][A-Z] [0-9][A-Z][0-9]$` | M5V 3A8 |
| **China** | 6 digits | `^[0-9]{6}$` | 100000 |
| **Colombia** | 6 digits | `^[0-9]{6}$` | 110111 |
| **DR Congo** | No standardized system | `^N/A$` | N/A |
| **Egypt** | 5 digits | `^[0-9]{5}$` | 11511 |
| **Ethiopia** | 4 digits | `^[0-9]{4}$` | 1000 |
| **France** | 5 digits | `^[0-9]{5}$` | 75001 |
| **Germany** | 5 digits | `^[0-9]{5}$` | 10115 |
| **Ghana** | No standardized system | `^N/A$` | N/A |
| **India** | 6 digits | `^[0-9]{6}$` | 110001 |
| **Indonesia** | 5 digits | `^[0-9]{5}$` | 10110 |
| **Iran** | 10 digits with hyphen | `^[0-9]{5}-[0-9]{5}$` | 11369-11111 |
| **Iraq** | 5 digits | `^[0-9]{5}$` | 10001 |
| **Italy** | 5 digits | `^[0-9]{5}$` | 00118 |
| **Japan** | 7 digits with hyphen | `^[0-9]{3}-[0-9]{4}$` | 100-0001 |
| **Kenya** | 5 digits | `^[0-9]{5}$` | 00100 |
| **Madagascar** | 3 digits | `^[0-9]{3}$` | 101 |
| **Malaysia** | 5 digits | `^[0-9]{5}$` | 50000 |
| **Mexico** | 5 digits | `^[0-9]{5}$` | 01000 |
| **Morocco** | 5 digits | `^[0-9]{5}$` | 10000 |
| **Myanmar** | 5 digits | `^[0-9]{5}$` | 11181 |
| **Nepal** | 5 digits | `^[0-9]{5}$` | 44600 |
| **Nigeria** | 6 digits | `^[0-9]{6}$` | 100001 |
| **Pakistan** | 5 digits | `^[0-9]{5}$` | 44000 |
| **Peru** | 5 digits or 8 characters | `^([0-9]{5}\|LIMA[0-9]{2})$` | 15001 / LIMA01 |
| **Philippines** | 4 digits | `^[0-9]{4}$` | 1000 |
| **Poland** | 5 digits with hyphen | `^[0-9]{2}-[0-9]{3}$` | 00-001 |
| **Russia** | 6 digits | `^[0-9]{6}$` | 101000 |
| **Saudi Arabia** | 5 digits with hyphen | `^[0-9]{5}-[0-9]{4}$` | 11564-2283 |
| **South Africa** | 4 digits | `^[0-9]{4}$` | 0001 |
| **South Korea** | 5 digits or 6 digits | `^[0-9]{5,6}$` | 03001 |
| **Spain** | 5 digits | `^[0-9]{5}$` | 28001 |
| **Sri Lanka** | 5 digits | `^[0-9]{5}$` | 00100 |
| **Tanzania** | No standardized system | `^N/A$` | N/A |
| **Thailand** | 5 digits | `^[0-9]{5}$` | 10100 |
| **Turkey** | 5 digits | `^[0-9]{5}$` | 06100 |
| **Ukraine** | 5 digits | `^[0-9]{5}$` | 01001 |
| **United Kingdom** | 6-8 characters | `^[A-Z]{1,2}[0-9][A-Z0-9]? [0-9][A-Z]{2}$` | SW1A 1AA |
| **United States** | 5 digits or 9 digits | `^[0-9]{5}(-[0-9]{4})?$` | 10001 / 10001-1234 |
| **Uzbekistan** | 6 digits | `^[0-9]{6}$` | 100000 |
| **Venezuela** | 4 digits with letter | `^[0-9]{4}-[A-Z]$` | 1010-A |
| **Vietnam** | 6 digits | `^[0-9]{6}$` | 100000 |

### Financial & Legal Codes

| Standard | Format | Regular Expression | Description | Example |
|----------|--------|-------------------|-------------|---------|
| **ISIN** | 12 characters | `^[A-Z]{2}[A-Z0-9]{9}[0-9]$` | International Securities ID Number | US1234567890 |
| **LEI** | 20 characters | `^[A-Z0-9]{20}$` | Legal Entity Identifier | 12345678901234567890 |

---

## 📚 Publishing & Media Standards

### Book & Publication Numbers

| Standard | Format | Regular Expression | Description | Example |
|----------|--------|-------------------|-------------|---------|
| **ISBN-10** | 10 digits with check | `^[0-9]{9}[0-9X]$` | International Standard Book Number (old format) | 0123456789 |
| **ISBN-13** | 13 digits | `^978[0-9]{10}$` | International Standard Book Number (current) | 9780123456789 |
| **ISSN** | 8 digits with hyphen | `^[0-9]{4}-[0-9]{3}[0-9X]$` | International Standard Serial Number | 1234-5679 |
| **ISMN** | 13 digits starting with 979 | `^979[0-9]{10}$` | International Standard Music Number | 9790123456789 |

### Media & Entertainment Codes

| Standard | Format | Regular Expression | Description | Example |
|----------|--------|-------------------|-------------|---------|
| **ISRC** | 12 characters | `^[A-Z]{2}[A-Z0-9]{3}[0-9]{7}$` | International Standard Recording Code | USRC17607839 |
| **ISWC** | Work identifier | `^T-[0-9]{9}-[0-9]$` | International Standard Work Code | T-123456789-0 |
| **ISAN** | 24 hex digits | `^[0-9A-F]{24}$` | International Standard Audiovisual Number | 123456789ABCDEF012345678 |

---

## 🛒 Retail & Product Identification

### Product Barcodes

| Standard | Format | Regular Expression | Description | Example |
|----------|--------|-------------------|-------------|---------|
| **UPC-A** | 12 digits | `^[0-9]{12}$` | Universal Product Code | 123456789012 |
| **EAN-13** | 13 digits | `^[0-9]{13}$` | European Article Number | 1234567890123 |
| **EAN-8** | 8 digits | `^[0-9]{8}$` | European Article Number (short) | 12345678 |
| **GTIN-14** | 14 digits | `^[0-9]{14}$` | Global Trade Item Number | 12345678901234 |

### Global Trade & Location Numbers

| Standard | Format | Regular Expression | Description | Example |
|----------|--------|-------------------|-------------|---------|
| **GLN** | 13 digits | `^[0-9]{13}$` | Global Location Number | 1234567890123 |
| **GCN** | 13 digits | `^[0-9]{13}$` | Global Coupon Number | 1234567890123 |
| **GRAI** | Variable length | `^[0-9]{8,14}$` | Global Returnable Asset Identifier | 12345678901234 |
| **GIAI** | Variable length | `^[0-9]{7,30}$` | Global Individual Asset Identifier | 1234567890123456789012345 |

---

## 🏭 Manufacturing & Industry

### Industrial Identifiers

### Common Industrial Identifiers

| Code Type | Format | Regular Expression | Description | Example |
|-----------|--------|-------------------|-------------|---------|
| **Serial Number (Generic)** | Alphanumeric | `^[A-Z0-9]{6,20}$` | Generic serial number format | ABC123456789 |
| **Lot/Batch Number** | Various formats | `^(LOT\|BATCH\|L\|B)[0-9A-Z]{4,12}$` | Manufacturing lot numbers | LOT2024001 |
| **Part Number** | Alphanumeric with hyphens | `^[A-Z0-9]{2,6}-[A-Z0-9]{2,10}(-[A-Z0-9]{1,6})?$` | Manufacturing part numbers | ABC-123456-X1 |
| **Model Number** | Various formats | `^[A-Z0-9]{2,4}-[A-Z0-9]{4,8}$` | Product model numbers | XYZ-1234A |
| **Asset Tag** | Company prefix + number | `^[A-Z]{2,4}[0-9]{6,10}$` | Asset tracking tags | COMP1234567890 |
| **Work Order** | Numeric with prefix | `^(WO\|JO\|SO)[0-9]{6,10}$` | Work order numbers | WO2024001234 |
| **Purchase Order** | Alphanumeric | `^PO[0-9A-Z]{6,12}$` | Purchase order numbers | PO20240001AB |
| **Invoice Number** | Various formats | `^(INV\|IN)[0-9]{6,12}$` | Invoice identification | INV202400001 |
| **Container ID** | ISO format | `^[A-Z]{4}[0-9]{7}$` | Shipping container ID | ABCD1234567 |
| **Pallet ID** | Variable format | `^(PLT\|PAL)[0-9A-Z]{6,12}$` | Pallet identification | PLT123456789 |

### Supply Chain & Logistics

| Standard | Format | Regular Expression | Description | Example |
|----------|--------|-------------------|-------------|---------|
| **SSCC** | 18 digits | `^[0-9]{18}$` | Serial Shipping Container Code | 123456789012345678 |
| **GSRN** | 18 digits | `^[0-9]{18}$` | Global Service Relation Number | 123456789012345678 |
| **GDTI** | Variable length | `^[0-9]{13,30}$` | Global Document Type Identifier | 12345678901234567890 |
| **GINC** | Variable length | `^[0-9]{13,30}$` | Global Identification Number for Consignment | 1234567890123456789012 |
| **GSIN** | 17 digits | `^[0-9]{17}$` | Global Shipment Identification Number | 12345678901234567 |

---

## 🎯 Usage Guidelines

### Combining Patterns

### Combining Multiple Patterns

For complex filtering scenarios, you can combine patterns using OR logic:

```regex
# URLs or Phone Numbers
^(https?://[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(\:[0-9]{1,5})?(\/[^\s]*)?|\+[1-9][0-9]{1,14})$

# IMEI or Serial Numbers
^([0-9]{15}|[A-Z0-9]{6,20})$

# Standard Product Codes
^([0-9]{8}|[0-9]{12}|[0-9]{13}|[0-9]{14})$
```

### Testing Your Patterns

Before implementing regex patterns in production:

1. **Use Online Testers**: Test patterns with tools like regex101.com
2. **Validate with Sample Data**: Test with known good and bad examples
3. **Performance Testing**: Ensure patterns don't significantly impact scan speed
4. **Document Patterns**: Keep clear documentation of pattern purposes

### Best Practices

- **Start Simple**: Begin with basic patterns and refine as needed
- **Test Thoroughly**: Validate with real-world data samples
- **Consider Localization**: Some formats vary by region
- **Performance Impact**: Complex patterns may affect scanning speed
- **False Positives**: Balance precision with practical usability
- **Update Regularly**: Standards may change over time

This comprehensive collection provides robust patterns for most common barcode filtering scenarios. Choose patterns based on your specific use case and data requirements.