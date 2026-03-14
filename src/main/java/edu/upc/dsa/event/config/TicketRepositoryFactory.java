package edu.upc.dsa.event.config;

import edu.upc.dsa.event.repository.JdbcTicketRepository;
import edu.upc.dsa.event.repository.MongoTicketRepository;
import edu.upc.dsa.event.repository.TicketRepository;

public final class TicketRepositoryFactory {

    private TicketRepositoryFactory() {
    }

    public static TicketRepository create() {
        if (PersistenceProvider.isMongo()) {
            return new MongoTicketRepository();
        }
        return new JdbcTicketRepository();
    }
}

