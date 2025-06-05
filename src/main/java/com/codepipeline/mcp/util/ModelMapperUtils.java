package com.codepipeline.mcp.util;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ModelMapperUtils {
    
    private static final ModelMapper modelMapper;
    
    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }
    
    private ModelMapperUtils() {
        // Private constructor to prevent instantiation
    }
    
    public static <D, T> D map(final T entity, Class<D> outClass) {
        return entity != null ? modelMapper.map(entity, outClass) : null;
    }
    
    public static <D, T> List<D> mapAll(final Collection<T> entityList, Class<D> outClass) {
        return entityList.stream()
                .map(entity -> map(entity, outClass))
                .collect(Collectors.toList());
    }
    
    public static <S, D> D map(final S source, D destination) {
        if (source != null && destination != null) {
            modelMapper.map(source, destination);
            return destination;
        }
        return null;
    }
}
