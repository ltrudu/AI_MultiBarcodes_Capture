package com.zebra.ai_multibarcodes_capture.models;

public class LanguageItem {
    private String languageName;
    private String languageCode;
    private String flagEmoji;

    public LanguageItem(String languageName, String languageCode, String flagEmoji) {
        this.languageName = languageName;
        this.languageCode = languageCode;
        this.flagEmoji = flagEmoji;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getFlagEmoji() {
        return flagEmoji;
    }

    public void setFlagResourceId(String flagEmoji) {
        this.flagEmoji = flagEmoji;
    }

    @Override
    public String toString() {
        return languageName; // This will be displayed in the spinner
    }
}