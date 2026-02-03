package es.upm.grise.profundizacion.file;

public class WrongFileTypeException extends Exception {
    public WrongFileTypeException(String message) {
        super(message);
    }
}