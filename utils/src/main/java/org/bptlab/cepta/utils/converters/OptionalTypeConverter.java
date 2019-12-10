package org.bptlab.cepta.utils.converters;

import java.util.Optional;
import picocli.CommandLine.ITypeConverter;

public class OptionalTypeConverter implements ITypeConverter<Optional<String>> {
  public Optional<String> convert(String value) throws Exception {
    return Optional.of(value);
  }
}