package dev.superchirok1.wgtranslator.serializer;

import dev.superchirok1.wgtranslator.serializer.impl.LegacyAmpersandSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagePipelineTest {
    private final LegacyAmpersandSerializer serializer = new LegacyAmpersandSerializer();

    @Test
    void resolvesVoxelCorePvpGlyphBeforeRenderingLegacyColors() {
        String input = "&f%voxelcore_font_pvp% &cYou cannot PVP here. &fMaybe visit the &ePVP arena?";
        String output = MessagePipeline.render(input,
                raw -> raw.replace("%voxelcore_font_pvp%", "\u00a7f" + (char) 0xE123), serializer);
        assertEquals("\u00a7f\u00a7f" + (char) 0xE123 +
                " \u00a7cYou cannot PVP here. \u00a7fMaybe visit the \u00a7ePVP arena?", output);
    }

    @Test
    void leavesPlaceholderLiteralWhenPlaceholderApiIsUnavailable() {
        assertEquals("\u00a7f%voxelcore_font_pvp% \u00a7cDenied",
                MessagePipeline.render("&f%voxelcore_font_pvp% &cDenied", raw -> raw, serializer));
    }

    @Test
    void colorizesComponentsInsertedAfterWorldGuardWhatMacro() {
        String message = "&fYou cannot %what% here!"
                .replace("%what%", "&eattack players &cnow");
        assertEquals("\u00a7fYou cannot \u00a7eattack players \u00a7cnow here!",
                MessagePipeline.render(message, raw -> raw, serializer));
    }

    @Test
    void retainsExistingHexAndSupportsBracketedHex() {
        assertEquals("\u00a7x\u00a7f\u00a7f\u00a75\u00a7a\u00a70\u00a70" +
                        " \u00a7x\u00a7a\u00a7b\u00a7c\u00a7d\u00a7e\u00a7f" +
                        " \u00a7x\u00a71\u00a72\u00a73\u00a74\u00a75\u00a76",
                MessagePipeline.render("&#ff5a00 &<#abcdef> <#123456>", raw -> raw, serializer));
    }

    @Test
    void supportsLegacyStyleAndUppercaseFormattingCodes() {
        assertEquals("\u00a7fwhite \u00a7cbold \u00a7lheading \u00a7rreset",
                MessagePipeline.render("&Fwhite &Cbold &lheading &Rreset", raw -> raw, serializer));
    }

    @Test
    void runsPlaceholderProcessingBeforeSerialization() {
        assertEquals("\u00a7aplayer", MessagePipeline.render("%custom%", 
                raw -> raw.replace("%custom%", "&aplayer"), serializer));
    }

    @Test
    void preservesNullAndEmptyStrings() {
        assertNull(MessagePipeline.render(null, raw -> raw, serializer));
        assertEquals("", MessagePipeline.render("", raw -> raw, serializer));
    }
}
