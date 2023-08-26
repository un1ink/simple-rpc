package com.un1ink.serialize.hessian;



import com.caucho.hessian4.io.HessianInput;
import com.caucho.hessian4.io.HessianOutput;
import com.un1ink.exception.SerializeException;
import com.un1ink.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        log.info("HessianSerializer serialize with obj: [{}]", obj.toString());
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SerializeException("Serialization failed");
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            Object o = hessianInput.readObject();

            return clazz.cast(o);

        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }

    }
}
