package de.sven_torben.cqrs.domain;

import de.sven_torben.cqrs.domain.events.ConcurrencyException;

import java.util.UUID;

public interface IStoreAggregates<RootT extends IAmAnAggregateRoot> {

  void store(RootT root) throws ConcurrencyException;

  RootT retrieveWithId(UUID id) throws ConcurrencyException;
}
