package ru.yandex.practicum.kafka.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory;
    private BinaryEncoder binaryEncoder;

    public GeneralAvroSerializer() {
        this.encoderFactory = EncoderFactory.get();
    }

    public GeneralAvroSerializer(EncoderFactory encoderFactory) {
        this.encoderFactory = encoderFactory;
    }

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] result;
            binaryEncoder = encoderFactory.binaryEncoder(os, binaryEncoder);
            if (data != null) {
                DatumWriter<SpecificRecordBase> datumWriter = new SpecificDatumWriter<>(data.getSchema());
                datumWriter.write(data, binaryEncoder);
                binaryEncoder.flush();
                result = os.toByteArray();
            } else {
                result = new byte[0];
            }
            return result;
        } catch (IOException e) {
            throw new SerializationException("Error serializing Avro message. Topic: " + topic +
                    ". Message: " + e.getMessage(), e);
        }
    }
}
