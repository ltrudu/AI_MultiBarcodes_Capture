package com.zebra.ai_multibarcodes_capture.autocapture;

import com.zebra.ai_multibarcodes_capture.autocapture.models.PredefinedRegex;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a list of predefined regex patterns organized by category.
 */
public class PredefinedRegexProvider {

    private static List<PredefinedRegex> predefinedRegexList;

    public static List<PredefinedRegex> getPredefinedRegexList() {
        if (predefinedRegexList == null) {
            predefinedRegexList = createPredefinedRegexList();
        }
        return predefinedRegexList;
    }

    private static List<PredefinedRegex> createPredefinedRegexList() {
        List<PredefinedRegex> list = new ArrayList<>();

        // Web URLs
        list.add(new PredefinedRegex("Web URLs", "HTTP URL", "^http://.*$"));
        list.add(new PredefinedRegex("Web URLs", "HTTPS URL", "^https://.*$"));
        list.add(new PredefinedRegex("Web URLs", "Any Web URL", "^https?://.*$"));

        // IP Addresses
        list.add(new PredefinedRegex("IP Addresses", "IPv4 Address", "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
        list.add(new PredefinedRegex("IP Addresses", "IPv4 with Port", "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}$"));

        // MAC Addresses
        list.add(new PredefinedRegex("MAC Addresses", "MAC (Colon format)", "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$"));
        list.add(new PredefinedRegex("MAC Addresses", "MAC (Hyphen format)", "^([0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}$"));
        list.add(new PredefinedRegex("MAC Addresses", "MAC (Cisco format)", "^([0-9A-Fa-f]{4}\\.){2}[0-9A-Fa-f]{4}$"));

        // Protocol URIs
        list.add(new PredefinedRegex("Protocol URIs", "mailto: URI", "^mailto:.*$"));
        list.add(new PredefinedRegex("Protocol URIs", "FTP URI", "^ftp://.*$"));
        list.add(new PredefinedRegex("Protocol URIs", "SFTP URI", "^sftp://.*$"));
        list.add(new PredefinedRegex("Protocol URIs", "SSH URI", "^ssh://.*$"));
        list.add(new PredefinedRegex("Protocol URIs", "tel: URI", "^tel:.*$"));
        list.add(new PredefinedRegex("Protocol URIs", "sms: URI", "^sms:.*$"));

        // Product Barcodes
        list.add(new PredefinedRegex("Product Barcodes", "UPC-A (12 digits)", "^\\d{12}$"));
        list.add(new PredefinedRegex("Product Barcodes", "EAN-13 (13 digits)", "^\\d{13}$"));
        list.add(new PredefinedRegex("Product Barcodes", "EAN-8 (8 digits)", "^\\d{8}$"));
        list.add(new PredefinedRegex("Product Barcodes", "GTIN-14 (14 digits)", "^\\d{14}$"));

        // Device IDs
        list.add(new PredefinedRegex("Device IDs", "IMEI (15 digits)", "^\\d{15}$"));
        list.add(new PredefinedRegex("Device IDs", "Serial Number (alphanumeric)", "^[A-Za-z0-9]{6,20}$"));

        // Book/Media
        list.add(new PredefinedRegex("Book/Media", "ISBN-10", "^\\d{9}[\\dX]$"));
        list.add(new PredefinedRegex("Book/Media", "ISBN-13", "^97[89]\\d{10}$"));
        list.add(new PredefinedRegex("Book/Media", "ISSN (8 digits)", "^\\d{4}-?\\d{3}[\\dX]$"));

        // Industrial
        list.add(new PredefinedRegex("Industrial", "Part Number", "^[A-Z]{2,3}-\\d{4,8}$"));
        list.add(new PredefinedRegex("Industrial", "Lot/Batch Number", "^LOT[A-Z0-9]{6,10}$"));
        list.add(new PredefinedRegex("Industrial", "Container ID", "^[A-Z]{4}\\d{7}$"));

        // Supply Chain
        list.add(new PredefinedRegex("Supply Chain", "SSCC (18 digits)", "^\\d{18}$"));
        list.add(new PredefinedRegex("Supply Chain", "GSIN (17 digits)", "^\\d{17}$"));

        // Location
        list.add(new PredefinedRegex("Location", "GLN (13 digits)", "^\\d{13}$"));

        // Phone Numbers
        list.add(new PredefinedRegex("Phone Numbers", "US Phone", "^\\+?1?[-.]?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}$"));
        list.add(new PredefinedRegex("Phone Numbers", "France Phone", "^\\+?33[-.]?[1-9][-.]?\\d{2}[-.]?\\d{2}[-.]?\\d{2}[-.]?\\d{2}$"));
        list.add(new PredefinedRegex("Phone Numbers", "Germany Phone", "^\\+?49[-.]?\\d{3,5}[-.]?\\d{4,8}$"));
        list.add(new PredefinedRegex("Phone Numbers", "UK Phone", "^\\+?44[-.]?\\d{4}[-.]?\\d{6}$"));
        list.add(new PredefinedRegex("Phone Numbers", "International Phone", "^\\+?\\d{1,4}[-.]?\\d{6,14}$"));

        // Custom Patterns
        list.add(new PredefinedRegex("Custom", "Digits Only", "^\\d+$"));
        list.add(new PredefinedRegex("Custom", "Letters Only", "^[A-Za-z]+$"));
        list.add(new PredefinedRegex("Custom", "Alphanumeric", "^[A-Za-z0-9]+$"));
        list.add(new PredefinedRegex("Custom", "Starts with SKU-", "^SKU-.*$"));
        list.add(new PredefinedRegex("Custom", "Contains hyphen", "^.*-.*$"));

        return list;
    }

    /**
     * Filter predefined regex list by search query.
     *
     * @param query The search query
     * @return Filtered list of predefined regex patterns
     */
    public static List<PredefinedRegex> filterPredefinedRegex(String query) {
        List<PredefinedRegex> allPatterns = getPredefinedRegexList();

        if (query == null || query.isEmpty()) {
            return new ArrayList<>(allPatterns);
        }

        String lowerQuery = query.toLowerCase();
        List<PredefinedRegex> filtered = new ArrayList<>();

        for (PredefinedRegex regex : allPatterns) {
            if (regex.getSearchableText().contains(lowerQuery)) {
                filtered.add(regex);
            }
        }

        return filtered;
    }
}
