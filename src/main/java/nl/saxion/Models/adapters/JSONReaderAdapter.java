package nl.saxion.Models.adapters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JSONReaderAdapter<T> implements FileReaderAdapter<T> {
    private final Class<T> type;

    public JSONReaderAdapter(Class<T> type) {
        this.type = type;
    }

    @Override
    public void read(String filename, ObjectProcessor<T> processor) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(filename);
            List<T> objects = objectMapper.readValue(file, new TypeReference<List<T>>() {});
            for (T object : objects) {
                processor.process(object);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
