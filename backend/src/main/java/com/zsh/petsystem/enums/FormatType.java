package com.zsh.petsystem.enums;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FormatType {

    JSON("JSON"),
    XML("XML");

    private String format;

    public static FormatType of(String format) {
        for (FormatType formatType : FormatType.values()) {
            if(formatType.getFormat().equalsIgnoreCase(format)){
                return formatType;
            }
        }
        return null;
    }
}
