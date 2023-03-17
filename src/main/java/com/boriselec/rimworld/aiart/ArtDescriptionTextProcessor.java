package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;

public class ArtDescriptionTextProcessor {
    private static final String TALE_DATE_REGEX = " on [^ ]+ of (Aprimay|Jugust|Septober|Decembary)[^.]+.";
    // some modes adds info in <i> tags
    private static final String ITALIC_TAG = "<i>.*</i>";

    public static String getDescription(Request request) {
        String description = request.thingDesc() + " " + request.artDesc();
        return description.replaceAll(TALE_DATE_REGEX, ".")
                .replaceAll(ITALIC_TAG, "")
                .replaceAll("\\n", "");
    }
}
