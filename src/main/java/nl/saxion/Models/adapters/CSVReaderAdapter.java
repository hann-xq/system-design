package nl.saxion.Models.adapters;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CSVReaderAdapter<T> implements FileReaderAdapter<T> {
    private final Class<T> type;

    public CSVReaderAdapter(Class<T> type) {
        this.type = type;
    }

    @Override
    public void read(String filename, ObjectProcessor<T> processor) {
        try (FileReader reader = new FileReader(filename)) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(type)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<T> objects = csvToBean.parse();
            for (T object : objects) {
                processor.process(object);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
