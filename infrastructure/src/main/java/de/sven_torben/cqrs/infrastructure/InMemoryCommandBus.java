package de.sven_torben.cqrs.infrastructure;

import de.sven_torben.cqrs.domain.IAmACommand;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public final class InMemoryCommandBus extends InMemoryBus<IAmACommand>
    implements ITransferCommands {

  @Override
  protected void handle(final IAmACommand msg, final Collection<Consumer<IAmACommand>> handlers) {
    Objects.requireNonNull(handlers);
    if (handlers.size() > 1) {
      throw new IllegalStateException("cannot send to more than one handler");
    }
    handlers.stream().forEach(handler -> msg.dispatch(handler));
  }

}
