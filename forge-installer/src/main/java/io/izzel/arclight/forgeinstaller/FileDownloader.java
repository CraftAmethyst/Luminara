package io.izzel.arclight.forgeinstaller;

import javax.net.ssl.SSLException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public record FileDownloader(
    String url,
    String target,
    String hash
) implements Supplier<Path> {
    static final int CONNECT_TIMEOUT_MILLIS = 15_000;
    static final int READ_TIMEOUT_MILLIS = 15_000;
    static final int MAX_REDIRECTS = 8;

    static InputStream read(String url) throws IOException {
        return read(
            url,
            current -> (HttpURLConnection) current.openConnection()
        );
    }

    static InputStream read(String url, ConnectionFactory connections)
        throws IOException {
        URL current = new URL(url);
        Set<String> history = new LinkedHashSet<>();
        for (int redirects = 0; ; redirects++) {
            if (!"https".equalsIgnoreCase(current.getProtocol())) {
                throw new IOException(
                    "Refusing non-HTTPS download URL: " + current
                );
            }
            if (!history.add(current.toExternalForm())) {
                throw new IOException(
                    "Redirect loop: " + String.join(" -> ", history)
                );
            }

            HttpURLConnection connection = connections.open(current);
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            boolean streamReturned = false;
            try {
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    streamReturned = true;
                    return new FilterInputStream(input) {
                        @Override
                        public void close() throws IOException {
                            try {
                                super.close();
                            } finally {
                                connection.disconnect();
                            }
                        }
                    };
                }
                if (isRedirect(responseCode)) {
                    if (redirects >= MAX_REDIRECTS) {
                        throw new IOException(
                            "Too many redirects (maximum " +
                                MAX_REDIRECTS +
                                "): " +
                                current
                        );
                    }
                    String location = connection.getHeaderField("Location");
                    if (location == null || location.isBlank()) {
                        throw new IOException(
                            "Redirect without Location header: " + current
                        );
                    }
                    URL next = new URL(current, location);
                    if (!"https".equalsIgnoreCase(next.getProtocol())) {
                        throw new IOException(
                            "Refusing non-HTTPS redirect: " + next
                        );
                    }
                    current = next;
                    continue;
                }
                if (
                    responseCode == HttpURLConnection.HTTP_NOT_FOUND ||
                        responseCode == HttpURLConnection.HTTP_FORBIDDEN
                ) {
                    throw new IOException(
                        "Not found: " + current + " (HTTP " + responseCode + ")"
                    );
                }
                throw new RemoteException(
                    "HTTP " + responseCode + " " + current
                );
            } finally {
                if (!streamReturned) connection.disconnect();
            }
        }
    }

    private static boolean isRedirect(int responseCode) {
        return (
            responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                responseCode == 307 ||
                responseCode == 308
        );
    }

    static Path download(
        String url,
        Path path,
        String expectedHash,
        ConnectionFactory connections
    ) {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        boolean complete = false;
        try {
            Files.deleteIfExists(temporary);
            if (Files.isDirectory(path)) throw new FileAlreadyExistsException(
                path.toString()
            );
            if (Files.isRegularFile(path)) {
                if (Util.hash(path).equalsIgnoreCase(expectedHash)) return path;
                Files.delete(path);
            }
            if (path.getParent() != null) Files.createDirectories(
                path.getParent()
            );
            try (InputStream stream = read(url, connections)) {
                Files.copy(
                    stream,
                    temporary,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }
            String actualHash = Util.hash(temporary);
            if (!actualHash.equalsIgnoreCase(expectedHash)) {
                throw new IOException(
                    "Hash mismatch, expected %s but found %s from %s".formatted(
                        expectedHash,
                        actualHash,
                        url
                    )
                );
            }
            try {
                Files.move(
                    temporary,
                    path,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(
                    temporary,
                    path,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }
            complete = true;
            return path;
        } catch (AccessDeniedException e) {
            throw new IllegalStateException(
                "Access denied for file " + e.getFile(),
                e
            );
        } catch (SocketTimeoutException | SSLException e) {
            throw new IllegalStateException("Timed out downloading " + url, e);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to download " + url + " to " + path,
                e
            );
        } finally {
            if (!complete) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Preserve the original download failure.
                }
            }
        }
    }

    @Override
    public Path get() {
        return download(
            url,
            Paths.get(target),
            hash,
            current -> (HttpURLConnection) current.openConnection()
        );
    }

    @FunctionalInterface
    interface ConnectionFactory {
        HttpURLConnection open(URL url) throws IOException;
    }
}
