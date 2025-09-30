# Common Regex Expressions for Barcode Filtering

This comprehensive guide provides robust regular expressions for filtering barcodes based on common data patterns. These expressions are optimized to minimize false positives and false negatives.

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
- [Social Security Numbers by Country](#social-security-numbers-by-country)
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
| **Algeria** | +213 | `^\+213[567][0-9]{8}$` | +213555123456 |
| **Argentina** | +54 | `^\+54(11\|[2-9][0-9])[0-9]{7,8}$` | +541112345678 |
| **Bangladesh** | +880 | `^\+880[13-9][0-9]{8}$` | +8801712345678 |
| **Brazil** | +55 | `^\+55[1-9][1-9][0-9]{8}$` | +5511987654321 |
| **Canada** | +1 | `^\+1[2-9][0-9]{2}[2-9][0-9]{6}$` | +14165551234 |
| **China** | +86 | `^\+86(13[0-9]\|14[57]\|15[0-35-9]\|17[678]\|18[0-9])[0-9]{8}$` | +8613812345678 |
| **Colombia** | +57 | `^\+57[13][0-9]{9}$` | +573123456789 |
| **DR Congo** | +243 | `^\+243[89][0-9]{8}$` | +243812345678 |
| **Egypt** | +20 | `^\+20[12][0-9]{8}$` | +201234567890 |
| **Ethiopia** | +251 | `^\+251[79][0-9]{8}$` | +251912345678 |
| **France** | +33 | `^\+33[67][0-9]{8}$` | +33612345678 |
| **Germany** | +49 | `^\+49[0-9]{10,11}$` | +4915123456789 |
| **Ghana** | +233 | `^\+233[2-5][0-9]{8}$` | +233241234567 |
| **India** | +91 | `^\+91[6-9][0-9]{9}$` | +919876543210 |
| **Indonesia** | +62 | `^\+62[8][1-9][0-9]{7,9}$` | +62812345678 |
| **Iran** | +98 | `^\+98[9][0-9]{9}$` | +989123456789 |
| **Iraq** | +964 | `^\+964[7][0-9]{9}$` | +9647812345678 |
| **Italy** | +39 | `^\+39[3][0-9]{9}$` | +393123456789 |
| **Japan** | +81 | `^\+81[7-9][0-9]{8}$` | +818012345678 |
| **Kenya** | +254 | `^\+254[7][0-9]{8}$` | +254712345678 |
| **Madagascar** | +261 | `^\+261[23][0-9]{7}$` | +26132123456 |
| **Malaysia** | +60 | `^\+60[1][0-9]{8,9}$` | +60123456789 |
| **Mexico** | +52 | `^\+52[1][0-9]{10}$` | +5215512345678 |
| **Morocco** | +212 | `^\+212[67][0-9]{8}$` | +212612345678 |
| **Myanmar** | +95 | `^\+95[9][0-9]{8,9}$` | +959123456789 |
| **Nepal** | +977 | `^\+977[9][8][0-9]{8}$` | +9779812345678 |
| **Nigeria** | +234 | `^\+234[78][0-9]{9}$` | +2348123456789 |
| **Pakistan** | +92 | `^\+92[3][0-9]{9}$` | +923123456789 |
| **Peru** | +51 | `^\+51[9][0-9]{8}$` | +51987654321 |
| **Philippines** | +63 | `^\+63[9][0-9]{9}$` | +639123456789 |
| **Poland** | +48 | `^\+48[5-9][0-9]{8}$` | +48512345678 |
| **Russia** | +7 | `^\+7[9][0-9]{9}$` | +79123456789 |
| **Saudi Arabia** | +966 | `^\+966[5][0-9]{8}$` | +966512345678 |
| **South Africa** | +27 | `^\+27[6-8][0-9]{8}$` | +27821234567 |
| **South Korea** | +82 | `^\+82[1][0-9]{8,9}$` | +821012345678 |
| **Spain** | +34 | `^\+34[67][0-9]{8}$` | +34612345678 |
| **Sri Lanka** | +94 | `^\+94[7][0-9]{8}$` | +94712345678 |
| **Tanzania** | +255 | `^\+255[67][0-9]{8}$` | +255712345678 |
| **Thailand** | +66 | `^\+66[689][0-9]{8}$` | +66812345678 |
| **Turkey** | +90 | `^\+90[5][0-9]{9}$` | +905123456789 |
| **Ukraine** | +380 | `^\+380[6-9][0-9]{8}$` | +380671234567 |
| **United Kingdom** | +44 | `^\+44[7][0-9]{9}$` | +447712345678 |
| **United States** | +1 | `^\+1[2-9][0-9]{2}[2-9][0-9]{6}$` | +14155551234 |
| **Uzbekistan** | +998 | `^\+998[9][0-9]{8}$` | +998901234567 |
| **Venezuela** | +58 | `^\+58[4][0-9]{9}$` | +584123456789 |
| **Vietnam** | +84 | `^\+84[3-9][0-9]{8}$` | +84912345678 |

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