package edu.upc.dsa.event.config;

import edu.upc.dsa.event.repository.MongoTicketRepository;
import edu.upc.dsa.event.repository.TicketRepository;

public final class TicketRepositoryFactory {

    private TicketRepositoryFactory() {
    }

    public static TicketRepository create() {
        return new MongoTicketRepository();
    }
}


