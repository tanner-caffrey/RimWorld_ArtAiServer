package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.translate.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArtDescriptionTextProcessorTest {
    @Test
    public void testArtDateClear() {
        String artDesc = "This representation tells the story of Mayrén completing work on a machine pistol on 13th of Septober, 5502.";
        String thingDesc = "A torso-sized piece of material sculpted into an artistic form.";
        Request request = new Request(artDesc, thingDesc, "", Language.ENGLISH);

        String description = ArtDescriptionTextProcessor.getDescription(request);

        Assertions.assertEquals("A torso-sized piece of material sculpted into an artistic form. " +
                "This representation tells the story of Mayrén completing work on a machine pistol.",
                description);
    }

    @Test
    public void testTagClear() {
        String artDesc = "test.";
        String thingDesc = "test.<i><color=#66E0E4FF>Roo's Painting Expansion</color></i>";
        Request request = new Request(artDesc, thingDesc, "", Language.ENGLISH);

        String description = ArtDescriptionTextProcessor.getDescription(request);

        Assertions.assertEquals("test. test.", description);
    }

    @Test
    public void testLineBreakClear() {
        String artDesc = "test.";
        String thingDesc = "test.\n\n test.";
        Request request = new Request(artDesc, thingDesc, "", Language.ENGLISH);

        String description = ArtDescriptionTextProcessor.getDescription(request);

        Assertions.assertEquals("test. test. test.", description);
    }
}