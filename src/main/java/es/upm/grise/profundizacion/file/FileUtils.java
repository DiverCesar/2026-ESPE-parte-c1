package es.upm.grise.profundizacion.file;

import java.util.zip.CRC32;

public class FileUtils {

    /**
     * Calcula el CRC32 de un array de bytes.
     * @param bytes Datos sobre los cuales calcular el CRC32.
     * @return Valor CRC32 como long.
     */
    public long calculateCRC32(byte[] bytes) {
        CRC32 crc = new CRC32();
        crc.update(bytes);
        return crc.getValue();
    }
}