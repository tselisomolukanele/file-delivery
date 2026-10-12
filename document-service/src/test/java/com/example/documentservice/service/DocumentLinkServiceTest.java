package com.example.documentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentLinkServiceTest {

    private DocumentLinkService service;
    private ValueCommands<String, DocumentLinkEntry> valueCommands;

    @BeforeEach
    void setUp() throws Exception {
        RedisDataSource redisDataSource = mock(RedisDataSource.class);
        valueCommands = mock(ValueCommands.class);
        when(redisDataSource.value(DocumentLinkEntry.class)).thenReturn(valueCommands);

        service = new DocumentLinkService(redisDataSource);

        Field baseUrlField = DocumentLinkService.class.getDeclaredField("baseUrl");
        baseUrlField.setAccessible(true);
        baseUrlField.set(service, "https://example.com");

        Field ttlField = DocumentLinkService.class.getDeclaredField("tokenTtl");
        ttlField.setAccessible(true);
        ttlField.setInt(service, 300);
    }

    @Test
    void createDocumentLinkStoresEntryAndReturnsLink() {
        DocumentLinkEntry result = service.createDocumentLink("invoice.pdf", "alice");

        assertNotNull(result.getId());
        assertEquals("invoice.pdf", result.getDocumentId());
        assertEquals("alice", result.getOwnerId());
        assertTrue(result.getLink().startsWith("https://example.com/document/download/"));
        verify(valueCommands).set(anyString(), any(DocumentLinkEntry.class), any(SetArgs.class));
    }

    @Test
    void getDocumentLinkReturnsStoredEntry() {
        DocumentLinkEntry expected = new DocumentLinkEntry();
        expected.setId("abc-123");
        expected.setDocumentId("invoice.pdf");
        expected.setOwnerId("alice");
        expected.setLink("https://example.com/document/download/abc-123");

        when(valueCommands.getdel("abc-123")).thenReturn(expected);

        DocumentLinkEntry result = service.getDocumentLink("abc-123");

        assertEquals(expected, result);
    }

    @Test
    void getDocumentLinkThrowsWhenEntryIsMissing() {
        when(valueCommands.getdel("missing")).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.getDocumentLink("missing"));
    }
}
