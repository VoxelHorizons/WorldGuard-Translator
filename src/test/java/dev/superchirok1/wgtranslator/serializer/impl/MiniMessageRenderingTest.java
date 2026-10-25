package dev.superchirok1.wgtranslator.serializer.impl;

import dev.superchirok1.wgtranslator.serializer.MessagePipeline;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MiniMessageRenderingTest {
    private final MiniMessageSerializer serializer = new MiniMessageSerializer();

    @Test
    void normalizesBothTemplateColoursAndPlaceholderSectionCodes() {
        String glyph = "\u00a7f\uE123";
        String input = "&f%voxelcore_font_pvp% &cYou cannot PVP here. &fMaybe visit the &ePVP arena?";
        String result = assertDoesNotThrow(() -> MessagePipeline.render(input,
                raw -> raw.replace("%voxelcore_font_pvp%", glyph), serializer));

        assertTrue(result.contains("\u00a7f\uE123"), result);
        assertTrue(result.contains("\u00a7cYou cannot PVP here."), result);
        assertTrue(result.contains("\u00a7ePVP arena?"), result);
        assertFalse(result.contains("&c"), result);
    }

    @Test
    void preservesMiniMessageFormattingAlongsideLegacyCodes() {
        String result = assertDoesNotThrow(() -> MessagePipeline.render(
                "<gold>Welcome</gold> &cDenied <bold>again</bold> \u00a7aSafe",
                raw -> raw, serializer));

        assertTrue(result.contains("Welcome"), result);
        assertTrue(result.contains("\u00a7cDenied"), result);
        assertTrue(result.contains("again"), result);
        assertTrue(result.contains("\u00a7aSafe"), result);
        assertFalse(result.contains("<gold>"), result);
    }

    @Test
    void retainsRgbColoursFromAllSupportedLegacyForms() {
        for (String colour : new String[] {
                "&#12AbCd", "&<#12AbCd>", "<#12AbCd>",
                "\u00a7x\u00a71\u00a72\u00a7A\u00a7b\u00a7C\u00a7d",
                "&x&1&2&A&b&C&d"
        }) {
            String result = assertDoesNotThrow(() -> serializer.colorize(colour + "Hello"), colour);
            assertTrue(result.toLowerCase(Locale.ROOT)
                    .contains("\u00a7x\u00a71\u00a72\u00a7a\u00a7b\u00a7c\u00a7d"), colour + " => " + result);
            assertTrue(result.endsWith("Hello"), colour + " => " + result);
        }
    }

    @Test
    void convertsStylesAndResetCodesWithoutLeakingRawMiniMessage() {
        String result = serializer.colorize("&lBold &rNormal &nUnderline &oItalic &mStrike &kHidden");
        assertTrue(result.contains("Bold"), result);
        assertTrue(result.contains("Normal"), result);
        assertTrue(result.contains("Underline"), result);
        assertTrue(result.contains("Italic"), result);
        assertTrue(result.contains("Strike"), result);
        assertTrue(result.contains("Hidden"), result);
        assertFalse(result.contains("<bold>"), result);
    }

    @Test
    void leavesMiniMessageTagArgumentsIntact() {
        assertEquals("<click:run_command:'/warp &f'>Go</click>",
                LegacyMiniMessageConverter.normalize("<click:run_command:'/warp &f'>Go</click>"));
        assertEquals("<hover:show_text:'&c reminder'>Hover</hover>",
                LegacyMiniMessageConverter.normalize("<hover:show_text:'&c reminder'>Hover</hover>"));
    }

    @Test
    void normalizerRewritesOnlyValidLegacySequences() {
        assertEquals("Example &z stays &not-colour",
                LegacyMiniMessageConverter.normalize("Example &z stays &not-colour"));
        assertEquals("<reset><white><reset><white>\uE123 <reset><red>No PVP",
                LegacyMiniMessageConverter.normalize("&f\u00a7f\uE123 &cNo PVP"));
        assertNull(serializer.colorize(null));
        assertEquals("", serializer.colorize(""));
    }
}
