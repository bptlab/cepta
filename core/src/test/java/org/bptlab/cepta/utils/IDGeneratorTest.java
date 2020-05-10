package org.bptlab.cepta.test.utils;

import org.javatuples.Quartet;
import org.junit.Assert;
import org.junit.Test;
import org.bptlab.cepta.utils.IDGenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IDGeneratorTest {

    protected boolean hasDuplicates(List<String> ids) {
        Set<String> s = new HashSet<>();
        for (String i : ids) {
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

    @Test
    public void testHashIsDeterministic() {
        List<Quartet<Object,Object,String, String>> shouldEqual = Arrays.asList(
                new Quartet<>("12", 12, IDGenerator.hashed("12"), IDGenerator.hashed(12L)),
                new Quartet<>(12L, 12, IDGenerator.hashed(12L), IDGenerator.hashed(12)),
                new Quartet<>("12", "12", IDGenerator.hashed("12"), IDGenerator.hashed("12")),
                new Quartet<>(12L, "12", IDGenerator.hashed(12L), IDGenerator.hashed("12"))
        );
        for (Quartet se: shouldEqual) {
            if (!se.getValue2().equals(se.getValue3())) {
                Assert.fail(String.format("Expected hashed(%s) == hashed(%s) but got %s != %s", se.getValue0(), se.getValue1(), se.getValue2(), se.getValue3()));
            }
        }
        List<Quartet<Object,Object,String, String>> shouldDiffer = Arrays.asList(
                new Quartet<>("12.0", 12L, IDGenerator.hashed("12.0"), IDGenerator.hashed(12L)),
                new Quartet<>("12A", "12B", IDGenerator.hashed("12A"), IDGenerator.hashed("12B")),
                new Quartet<>("a", "A", IDGenerator.hashed("a"), IDGenerator.hashed("A")),
                new Quartet<>(12F, 12L, IDGenerator.hashed(12F), IDGenerator.hashed(12L))
        );
        for (Quartet sd: shouldDiffer) {
            if (sd.getValue2().equals(sd.getValue3())) {
                Assert.fail(String.format("Expected hashed(%s) != hashed(%s) but got %s == %s", sd.getValue0(), sd.getValue1(), sd.getValue2(), sd.getValue3()));
            }
        }
    }

}