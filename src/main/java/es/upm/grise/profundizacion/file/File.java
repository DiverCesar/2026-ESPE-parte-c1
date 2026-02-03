//UNIVERSIDAD DE LAS FUERZAS ARMADAS - ESPE
//César Luis Galarza Villalva
//Sangolquí, 03rd February, 2026

package es.upm.grise.profundizacion.file;

import java.util.ArrayList;
import java.util.List;

public class File {

    // Especificación: content implementado como ArrayList<Character>
    private List<Character> content;
    private FileType type;
    private String path;
    
    // Composición: File usa FileUtils
    private FileUtils fileUtils;

    /**
     * Constructor.
     * La especificación indica que 'content' debe estar vacío pero no null.
     */
    public File(String path, FileType type) {
        this.path = path;
        this.type = type;
        this.content = new ArrayList<>();
        this.fileUtils = new FileUtils(); // Instancia por defecto
    }

    // Método para inyectar FileUtils (útil para testing/mocking futuro si se requiere)
    public void setFileUtils(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    public String getPath() {
        return path;
    }

    public FileType getType() {
        return type;
    }

    /**
     * Añade contenido a un archivo de tipo PROPERTY.
     * @param newContent Array de caracteres a añadir.
     * @throws InvalidContentException Si newContent es null.
     * @throws WrongFileTypeException Si el archivo es de tipo IMAGE.
     */
    public void addProperty(char[] newContent) throws InvalidContentException, WrongFileTypeException {
        if (newContent == null) {
            throw new InvalidContentException("El contenido proporcionado no puede ser null.");
        }
        if (this.type == FileType.IMAGE) {
            throw new WrongFileTypeException("No se pueden añadir propiedades a un archivo de tipo IMAGE.");
        }

        for (char c : newContent) {
            this.content.add(c);
        }
    }

    /**
     * Añade contenido binario (bytes simulados) a un archivo.
     * Aunque la especificación textual detalla addProperty, el diagrama UML incluye addImageBytes.
     * Por coherencia, validamos que el tipo sea correcto.
     * @param newContent Array de caracteres representando bytes.
     * @throws WrongFileTypeException Si se intenta añadir imagen a un archivo PROPERTY.
     * @throws InvalidContentException Si newContent es null.
     */
    public void addImageBytes(char[] newContent) throws WrongFileTypeException, InvalidContentException {
        if (newContent == null) {
            throw new InvalidContentException("El contenido de imagen no puede ser null.");
        }
        if (this.type == FileType.PROPERTY) {
            throw new WrongFileTypeException("No se pueden añadir bytes de imagen a un archivo de tipo PROPERTY.");
        }
        
        for (char c : newContent) {
            this.content.add(c);
        }
    }

    /**
     * Elimina una cantidad específica de caracteres del contenido.
     * @param numberChars Número de caracteres a eliminar desde el final.
     */
    public void removeContent(int numberChars) {
        int size = this.content.size();
        if (numberChars > size) {
            this.content.clear();
        } else {
            // Eliminar desde el final es más eficiente en ArrayList y lógico para un 'buffer' de archivo
            this.content.subList(size - numberChars, size).clear();
        }
    }

    /**
     * Obtiene el tamaño lógico del archivo (número de caracteres almacenados).
     * @return Tamaño del contenido.
     */
    public long getDiskSize() {
        return this.content.size();
    }

    /**
     * Calcula el CRC32 del contenido.
     * Transforma el ArrayList<Character> a byte[] antes de llamar a FileUtils.
     * @return Valor CRC32 o 0 si el contenido está vacío.
     */
    public long getCRC32() {
        if (this.content.isEmpty()) {
            return 0;
        }

        // Conversión requerida: ArrayList<Character> -> byte[]
        // Especificación: Usar el byte menos significativo (c & 0xFF)
        byte[] bytes = new byte[this.content.size()];
        for (int i = 0; i < this.content.size(); i++) {
            char c = this.content.get(i);
            bytes[i] = (byte) (c & 0xFF);
        }

        return fileUtils.calculateCRC32(bytes);
    }
}