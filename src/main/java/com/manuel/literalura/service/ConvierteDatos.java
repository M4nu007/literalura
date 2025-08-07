package com.manuel.literalura.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvierteDatos implements IConvierteDatos{
    private ObjectMapper objectMapper = new ObjectMapper(); //el obkect maper nos permite leer esos valores q biene de la API

    @Override
    public <T> T obtenerDatos(String json, Class<T> clase) {
        try {
            return objectMapper.readValue(json,clase);//aqui lo transforma a una clase
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
