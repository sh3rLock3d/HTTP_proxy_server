package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIP {

    public static byte[] compress(String data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(data.length());
        GZIPOutputStream outputStream = new GZIPOutputStream(byteArrayOutputStream);
        outputStream.write(data.getBytes(StandardCharsets.UTF_16));
        outputStream.flush();
        outputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static String decompress(byte[] compressedData) throws IOException {
        StringBuilder outStringBuilder = new StringBuilder();
        if (isCompressed(compressedData)) {
            GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(compressedData));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStringBuilder.append(line);
            }
        } else {
            outStringBuilder.append(compressedData);
        }
        return outStringBuilder.toString();
    }

    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
