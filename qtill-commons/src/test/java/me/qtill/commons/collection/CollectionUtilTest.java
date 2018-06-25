package me.qtill.commons.collection;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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

    @Test
    public void testMap() {
        Map<String, String> map = new HashMap<>();
        assertTrue(CollectionUtil.isEmpty(map));
    }
}