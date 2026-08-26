package io.izzel.arclight.i18n;

import org.junit.jupiter.api.Test;

import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocaleResourcesTest {

    private static final List<String> LOCALES = List.of(
        "en_us", "es_es", "fr_fr", "it_it", "ko_kr", "ru_ru", "zh_cn"
    );

    @Test
    void bundledLocalesAreValidUtf8WithoutReplacementCharacters() throws Exception {
        for (String locale : LOCALES) {
            byte[] bytes;
            try (var stream = getClass().getResourceAsStream("/META-INF/i18n/" + locale + ".yml")) {
                assertNotNull(stream, "Missing locale: " + locale);
                bytes = stream.readAllBytes();
            }

            var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
            String content = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            assertFalse(content.indexOf('\uFFFD') >= 0, "Replacement character in locale: " + locale);
        }
    }

    @Test
    void chineseAsyncSaveCommentIsIntact() throws Exception {
        try (var stream = getClass().getResourceAsStream("/META-INF/i18n/zh_cn.yml")) {
            assertNotNull(stream);
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(content.contains("- 如果在指定时间内保存未完成，服务器将继续关闭流程"));
        }
    }
}
