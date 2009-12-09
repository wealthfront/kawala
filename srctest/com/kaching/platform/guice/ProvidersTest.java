package com.kaching.platform.guice;

import static com.google.common.collect.Sets.newHashSet;
import static com.kaching.platform.testing.Assert.assertContains;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;

public class ProvidersTest {

  @Test
  public void provider() {
    assertEquals(1L, Providers.provider(1L).get());
    assertEquals("hi", Providers.provider("hi").get());
  }

  @Test(expected = NullPointerException.class)
  public void providerOfNullCannotBeCreated() {
    Providers.provider(null);
  }

  @Test
  public void random() {
    Set<Long> set = ImmutableSet.of(1L, 2L);
    Provider<Long> provider = Providers.random(set);
    Set<Long> provided = newHashSet();
    for (int i = 0; i < 100; i++) {
      Long n = provider.get();
      assertContains(set, n);
      provided.add(n);
    }
    assertEquals(set, provided);
  }

  @Test
  public void randomFromSingleton() {
    Provider<Long> provider = Providers.random(singleton(1L));
    for (int i = 0; i < 10; i++) {
      assertEquals(1L, provider.get());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void randomFromEmptyList() {
    Providers.random(emptyList());
  }

  @Test(expected = NullPointerException.class)
  public void randomFromListContainingNull() {
    Providers.random(asList(1L, null));
  }

}
