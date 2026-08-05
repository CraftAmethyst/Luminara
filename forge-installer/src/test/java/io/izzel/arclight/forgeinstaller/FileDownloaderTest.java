package io.izzel.arclight.forgeinstaller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloaderTest {

    @TempDir
    Path directory;

    private static String sha1(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-1").digest(bytes)
        );
    }

    @Test
    void configuresTimeoutsAndDisconnectsAfterRead() throws Exception {
        FakeConnection connection = new FakeConnection(
            new URL("https://example.test/file"),
            200,
            null,
            "payload".getBytes()
        );

        byte[] bytes;
        try (
            InputStream input = FileDownloader.read(
                connection.getURL().toString(),
                ignored -> connection
            )
        ) {
            bytes = input.readAllBytes();
        }

        assertArrayEquals("payload".getBytes(), bytes);
        assertEquals(
            FileDownloader.CONNECT_TIMEOUT_MILLIS,
            connection.getConnectTimeout()
        );
        assertEquals(
            FileDownloader.READ_TIMEOUT_MILLIS,
            connection.getReadTimeout()
        );
        assertTrue(connection.disconnected);
    }

    @Test
    void rejectsNonHttpsRedirects() throws Exception {
        FakeConnection connection = new FakeConnection(
            new URL("https://example.test/file"),
            302,
            "http://example.test/insecure",
            new byte[0]
        );

        IOException exception = assertThrows(IOException.class, () ->
            FileDownloader.read(
                connection.getURL().toString(),
                ignored -> connection
            )
        );

        assertTrue(exception.getMessage().contains("non-HTTPS redirect"));
        assertTrue(connection.disconnected);
    }

    @Test
    void capsAndDisconnectsRedirects() {
        AtomicInteger sequence = new AtomicInteger();
        List<FakeConnection> connections = new ArrayList<>();

        IOException exception = assertThrows(IOException.class, () ->
            FileDownloader.read("https://example.test/0", current -> {
                int next = sequence.incrementAndGet();
                FakeConnection connection = new FakeConnection(
                    current,
                    302,
                    "https://example.test/" + next,
                    new byte[0]
                );
                connections.add(connection);
                return connection;
            })
        );

        assertTrue(exception.getMessage().contains("Too many redirects"));
        assertEquals(FileDownloader.MAX_REDIRECTS + 1, connections.size());
        assertTrue(
            connections.stream().allMatch(connection -> connection.disconnected)
        );
    }

    @Test
    void deletesPartialFileAfterHashMismatch() throws Exception {
        byte[] body = "corrupt".getBytes();
        Path target = directory.resolve("artifact.jar");
        FakeConnection connection = new FakeConnection(
            new URL("https://example.test/artifact"),
            200,
            null,
            body
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () ->
                FileDownloader.download(
                    connection.getURL().toString(),
                    target,
                    sha1("expected".getBytes()),
                    ignored -> connection
                )
        );

        assertTrue(exception.getCause().getMessage().contains("Hash mismatch"));
        assertFalse(Files.exists(target));
        assertFalse(Files.exists(target.resolveSibling("artifact.jar.tmp")));
        assertTrue(connection.disconnected);
    }

    @Test
    void deletesPartialFileAfterReadFailure() throws Exception {
        Path target = directory.resolve("artifact.jar");
        FakeConnection connection = new FakeConnection(
            new URL("https://example.test/artifact"),
            200,
            null,
            new byte[0]
        ) {
            @Override
            public InputStream getInputStream() {
                return new InputStream() {
                    private boolean emitted;

                    @Override
                    public int read() throws IOException {
                        if (!emitted) {
                            emitted = true;
                            return 'x';
                        }
                        throw new SocketTimeoutException("simulated timeout");
                    }
                };
            }
        };

        assertThrows(IllegalStateException.class, () ->
            FileDownloader.download(
                connection.getURL().toString(),
                target,
                sha1("x".getBytes()),
                ignored -> connection
            )
        );

        assertFalse(Files.exists(target));
        assertFalse(Files.exists(target.resolveSibling("artifact.jar.tmp")));
        assertTrue(connection.disconnected);
    }

    private static class FakeConnection extends HttpURLConnection {

        private final int status;
        private final String location;
        private final byte[] body;
        private boolean disconnected;

        FakeConnection(URL url, int status, String location, byte[] body) {
            super(url);
            this.status = status;
            this.location = location;
            this.body = body;
        }

        @Override
        public int getResponseCode() {
            return status;
        }

        @Override
        public String getHeaderField(String name) {
            return "Location".equalsIgnoreCase(name) ? location : null;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public void disconnect() {
            disconnected = true;
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() {
        }
    }
}
