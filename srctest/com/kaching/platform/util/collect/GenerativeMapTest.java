package com.kaching.platform.util.collect;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;

import com.kaching.platform.util.collect.GenerativeMap;

public class GenerativeMapTest {

  @Test
  public void generateValues() {
    Map<Long, String> hexStringFromLong = new GenerativeMap<Long, String>() {
      @Override protected String compute(Long key) {
        return Long.toHexString(key);
    }};

    assertEquals("1b", hexStringFromLong.get(27l));
    assertEquals("cafebabe", hexStringFromLong.get(3405691582l));
    assertEquals("0", hexStringFromLong.get(0l));
    assertEquals("ffffffffffffff9f", hexStringFromLong.get(-97l));
  }

  @Test
  public void identicalValues() {
    Map<Integer, String[]> stringArrays = new GenerativeMap<Integer, String[]>() {
      @Override protected String[] compute(Integer key) {
        return new String[key];
    }};

    assertNotNull(stringArrays.get(9));
    assertEquals(stringArrays.get(65), stringArrays.get(65));
  }
}
