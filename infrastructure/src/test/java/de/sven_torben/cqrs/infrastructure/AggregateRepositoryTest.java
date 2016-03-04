package de.sven_torben.cqrs.infrastructure;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import de.sven_torben.cqrs.domain.EventMockA;
import de.sven_torben.cqrs.domain.EventMockB;
import de.sven_torben.cqrs.domain.IAmAnAggregateRoot;
import de.sven_torben.cqrs.infrastructure.AggregateRepository;
import de.sven_torben.cqrs.domain.AggregateRootMock;
import de.sven_torben.cqrs.domain.ConcurrencyException;
import de.sven_torben.cqrs.domain.IAmAnEvent;
import de.sven_torben.cqrs.domain.IStoreEvents;

public class AggregateRepositoryTest {

	private AggregateRepository<AggregateRootMock> cut;
	private IStoreEvents eventStoreMock;
	
	@Before
	public void setUp()
	{
		eventStoreMock = mock(IStoreEvents.class);
		cut = new AggregateRepository<AggregateRootMock>(eventStoreMock, AggregateRootMock.class);
	}
	
	@Test
	public void testConstructor()
	{
		try {
			new AggregateRepository<IAmAnAggregateRoot>(null, null);
			fail();
		} catch (IllegalArgumentException e)
		{
			ok();
		}
		
		try {
			new AggregateRepository<IAmAnAggregateRoot>(eventStoreMock, null);
			fail();
		} catch (IllegalArgumentException e)
		{
			ok();
		}
		
		try {
			new AggregateRepository<IAmAnAggregateRoot>(null, IAmAnAggregateRoot.class);
			fail();
		} catch (IllegalArgumentException e)
		{
			ok();
		}
		
		new AggregateRepository<AggregateRootMock>(eventStoreMock, AggregateRootMock.class);
		
	}
	
	@Test
	public void testRetrieval() {
		ArrayList<IAmAnEvent> events = new ArrayList<IAmAnEvent>();
		events.add(new EventMockA());
		events.add(new EventMockB());
		when(eventStoreMock.getEventsForAggregate(any(UUID.class))).thenReturn(events);
		UUID id = UUID.randomUUID();
		
		AggregateRootMock root = cut.retrieveWithId(id);
		
		assertEquals(id, root.getId());
		assertEquals(0, root.getUncommittedEvents().size());
		assertTrue(root.aHasBeenCalled);
		assertTrue(root.bHasBeenCalled);
		verify(eventStoreMock, times(1)).getEventsForAggregate(id);
	}

	@Test
	public void testRetrievalMustNotWorkForInterfaces() {
		AggregateRepository<IAmAnAggregateRoot> cut = new AggregateRepository<IAmAnAggregateRoot>(eventStoreMock, IAmAnAggregateRoot.class);
		ArrayList<IAmAnEvent> events = new ArrayList<IAmAnEvent>();
		events.add(new EventMockA());
		events.add(new EventMockB());
		when(eventStoreMock.getEventsForAggregate(any(UUID.class))).thenReturn(events);
		assertNull(cut.retrieveWithId(UUID.randomUUID()));
	}
	
	@Test
	public void testThatEventsDoNotGetCommittedWhenExceptionOccurs()
	{
		AggregateRootMock mock = new AggregateRootMock(UUID.randomUUID(), 1);
		mock.doA();
		mock.doB();
		
		try {
			doThrow(new ConcurrencyException(2, 1)).when(eventStoreMock).save(mock.getId(), mock.getUncommittedEvents(), mock.getVersion());
		} catch (ConcurrencyException e) {
			fail();
		}
		try {
			cut.store(mock);
		} catch (ConcurrencyException e) {
			ok();
		}
		
		assertEquals(2, mock.getUncommittedEvents().size());
	}

	@Test
	public void testStoringAggregateRoots() throws ConcurrencyException
	{
		AggregateRootMock root = new AggregateRootMock();
		root.doA();
		root.doB();
		
		Collection<IAmAnEvent> events = root.getUncommittedEvents();
	
		try {
			cut.store(null);
		} catch (IllegalArgumentException e)
		{
			ok();
		}
		cut.store(root);
		
		verify(eventStoreMock, times(1)).save(eq(root.getId()), eq(events), eq(root.getVersion()));
	}
	
	private void ok() {
	}
}