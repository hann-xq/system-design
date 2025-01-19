package nl.saxion.Models.adapters;

public interface FileReaderAdapter<T> {
    void read(String filename, ObjectProcessor<T> processor);
}
