package de.sven_torben.cqrs.domain.events;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(DataProviderRunner.class)
public final class ByVersionComparatorTest {

  @DataProvider
  public static Object[][] testcases() {
    EventDescriptor lowVersion =
        new EventDescriptor(UUID.randomUUID(), 10L, mock(IAmAnEvent.class));
    EventDescriptor highVersion =
        new EventDescriptor(UUID.randomUUID(), 99L, mock(IAmAnEvent.class));
    return new Object[][] {
        { lowVersion, highVersion, -1 },
        { highVersion, lowVersion, 1 },
        { lowVersion, lowVersion, 0 },
    };
  }

  @Test
  @UseDataProvider("testcases")
  public void test(EventDescriptor lhs, EventDescriptor rhs, int expectedResult) {
    assertThat(EventDescriptor.BY_VERSION_COMPARATOR.compare(lhs, rhs),
        is(equalTo(expectedResult)));
  }

}
