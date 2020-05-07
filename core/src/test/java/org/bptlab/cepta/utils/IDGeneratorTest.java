package org.bptlab.cepta.test.utils;

import org.junit.Assert;
import org.junit.Test;
import org.bptlab.cepta.utils.IDGenerator;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IDGeneratorTest {

    protected boolean hasDuplicates(List<String> ids)
    {
        Set<String> s = new HashSet<>();
        for (String i : ids)
        {
            if (s.contains(i)) return true;
            s.add(i);
        }
        return false;
    }

    @Test
  public void testGeneratesUniqueIds() {
        List<String> ids = Arrays.asList(new String[1000]).parallelStream().map(i -> IDGenerator.newCeptaID())
                .collect(Collectors.toList());
        Assert.assertFalse(hasDuplicates(ids));
  }

}