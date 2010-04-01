package com.kaching.platform.common.values;

import static com.kaching.platform.common.values.Country.AX;
import static com.kaching.platform.common.values.Country.US;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CountryTest {

  @Test
  public void getNumber() throws Exception {
    assertEquals(233, US.getNumber()); 
  }

  @Test
  public void getCountryName() throws Exception {
    assertEquals("United States", US.getCountryName()); 
  }
  
  @Test
  public void getAlpha2() throws Exception {
    assertEquals("US", US.getAlpha2()); 
  }
  
  @Test
  public void utf8CountryName() throws Exception {
    assertEquals("\u00c5land Islands", AX.getCountryName());
  }

}
