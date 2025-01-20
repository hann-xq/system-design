package nl.saxion.Models.adapters;

import java.util.ArrayList;
import java.util.List;

public class ObjectCollector<T> implements ObjectProcessor<T> {
    private final List<T> objects = new ArrayList<>();

    @Override
    public void process(T object) {
        objects.add(object);
    }

    public List<T> getObjects() {
        return objects;
    }
}
