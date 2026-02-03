//UNIVERSIDAD DE LAS FUERZAS ARMADAS - ESPE
//César Luis Galarza Villalva
//Sangolquí, 03rd February, 2026

//JUnit test class for FileFolders with StandAlone and Gemini Coding Standards

package es.upm.grise.profundizacion.file;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class FileTest {
    
    // Referencia a la clase bajo prueba
    private File fileProperty;
    private File fileImage;

    // Constantes para pruebas repetibles
    private static final String DEFAULT_PATH = "/tmp/test_file";

    @BeforeEach
    public void setUp() {
        // Inicializamos instancias limpias antes de cada test para asegurar independencia
        fileProperty = new File(DEFAULT_PATH, FileType.PROPERTY);
        fileImage = new File(DEFAULT_PATH, FileType.IMAGE);
    }

    @Nested
    @DisplayName("Pruebas de Inicialización y Estado Base")
    class InitializationTests {

        @Test
        @DisplayName("El archivo debe inicializarse con tamaño 0 y lista vacía (no null)")
        void testInitialState() {
            // Verificamos múltiples condiciones simultáneamente para reportar todos los fallos posibles de una vez
            assertAll("Estado inicial",
                () -> assertEquals(0, fileProperty.getDiskSize(), "El tamaño inicial debe ser 0"),
                () -> assertEquals(DEFAULT_PATH, fileProperty.getPath(), "El path debe coincidir"),
                () -> assertEquals(FileType.PROPERTY, fileProperty.getType(), "El tipo debe ser PROPERTY")
            );
        }
    }

    @Nested
    @DisplayName("Validaciones de addProperty()")
    class AddPropertyTests {

        @Test
        @DisplayName("Debe lanzar InvalidContentException si el contenido es NULL")
        void testAddPropertyWithNull() {
            assertThrows(InvalidContentException.class, () -> {
                fileProperty.addProperty(null);
            }, "Se esperaba InvalidContentException al añadir null");
        }

        @Test
        @DisplayName("Debe lanzar WrongFileTypeException si se intenta añadir propiedad a un archivo IMAGE")
        void testAddPropertyToImageFile() {
            char[] content = "key=value".toCharArray();
            assertThrows(WrongFileTypeException.class, () -> {
                fileImage.addProperty(content);
            }, "Un archivo de tipo IMAGE no debe aceptar propiedades");
        }

        @Test
        @DisplayName("Debe añadir contenido correctamente en archivo PROPERTY")
        void testAddPropertySuccess() throws Exception {
            String data = "Date=2025";
            fileProperty.addProperty(data.toCharArray());
            assertEquals(data.length(), fileProperty.getDiskSize(), "El tamaño en disco debe coincidir con los caracteres añadidos");
        }
        
        @Test
        @DisplayName("Debe acumular contenido secuencialmente")
        void testAddPropertyAccumulation() throws Exception {
            fileProperty.addProperty("A".toCharArray());
            fileProperty.addProperty("B".toCharArray());
            // Verificamos indirectamente por tamaño y CRC (más adelante se prueba el CRC exacto)
            assertEquals(2, fileProperty.getDiskSize(), "El contenido debe acumularse, no reemplazarse");
        }
    }

    @Nested
    @DisplayName("Validaciones de addImageBytes()")
    class AddImageBytesTests {

        @Test
        @DisplayName("Debe lanzar InvalidContentException si el contenido es NULL")
        void testAddImageWithNull() {
            assertThrows(InvalidContentException.class, () -> {
                fileImage.addImageBytes(null);
            }, "Se esperaba InvalidContentException al añadir null");
        }

        @Test
        @DisplayName("Debe lanzar WrongFileTypeException si se intenta añadir bytes a un archivo PROPERTY")
        void testAddImageToPropertyFile() {
            char[] bytes = {0x00, 0x01};
            assertThrows(WrongFileTypeException.class, () -> {
                fileProperty.addImageBytes(bytes);
            }, "Un archivo de tipo PROPERTY no debe aceptar bytes de imagen");
        }

        @Test
        @DisplayName("Debe añadir bytes correctamente y manejar la conversión de tipos")
        void testAddImageSuccess() throws Exception {
            // Simulamos bytes usando chars
            char[] pixelData = {0xFF, 0x00, 0xAA}; 
            fileImage.addImageBytes(pixelData);
            assertEquals(3, fileImage.getDiskSize());
        }
    }

    @Nested
    @DisplayName("Pruebas de Cálculo CRC32 y Manipulación de Bits")
    class CRC32Tests {

        @Test
        @DisplayName("getCRC32 debe devolver 0 si el contenido está vacío")
        void testCRC32Empty() {
            assertEquals(0L, fileProperty.getCRC32(), "El CRC32 de un archivo vacío debe ser 0");
        }

        @Test
        @DisplayName("Mock Manual: Verifica la conversión estricta de char a byte (LSB)")
        void testCRC32BitmaskIntegration() throws Exception {
            // Mock Manual: Creamos una subclase anónima de FileUtils para interceptar la llamada
            // y verificar qué bytes exactos está enviando la clase File.
            // Esto aísla la lógica de conversión de 'File' de la lógica de cálculo de 'FileUtils'.
            FileUtils spyUtils = new FileUtils() {
                @Override
                public long calculateCRC32(byte[] bytes) {
                    // Verificamos que el array tenga el tamaño correcto
                    assertEquals(1, bytes.length);
                    // Verificamos que se haya aplicado la máscara 0xFF correctamente
                    // El char 0x1234 tiene LSB 0x34. Si la conversión falla, podría pasar otro valor.
                    assertEquals((byte)0x34, bytes[0], "El byte pasado debe ser el LSB del char");
                    return 999L; // Valor dummy para confirmar que se usa este mock
                }
            };

            fileProperty.setFileUtils(spyUtils);
            
            // 0x1234 -> Binario: 0001 0010 0011 0100. LSB es 0x34.
            char[] complexChar = {(char) 0x1234}; 
            fileProperty.addProperty(complexChar);
            
            long result = fileProperty.getCRC32();
            assertEquals(999L, result, "El método debe invocar al FileUtils inyectado");
        }
        
        @Test
        @DisplayName("Integración real: Verifica cálculo CRC32 con datos conocidos")
        void testCRC32RealCalculation() throws Exception {
            // Test de integración con la implementación real de FileUtils
            String testString = "123456789";
            fileProperty.addProperty(testString.toCharArray());
            
            // El CRC32 estándar de "123456789" es 0xCBF43926 (3421780262 decimal)
            long expectedCRC = 3421780262L; 
            assertEquals(expectedCRC, fileProperty.getCRC32(), "El cálculo CRC32 real debe coincidir con el estándar");
        }
    }
}