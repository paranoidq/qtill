package me.qtill.commons.collection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CollectionUtilTest {

    @Test
    public void testCollectionEmpty() {
        List<String> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        assertTrue(CollectionUtil.isEmpty(list));
        assertTrue(CollectionUtil.isEmpty(set));
    }
}