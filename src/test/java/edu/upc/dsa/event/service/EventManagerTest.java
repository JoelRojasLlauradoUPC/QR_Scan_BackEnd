package edu.upc.dsa.event.service;

import edu.upc.dsa.event.exception.ApiException;
import edu.upc.dsa.event.model.Ticket;
import edu.upc.dsa.event.repository.InMemoryTicketRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventManagerTest {

    private EventManager eventManager;

    @Before
    public void setUp() {
        InMemoryTicketRepository repository = new InMemoryTicketRepository();
        repository.upsert(new Ticket(
                "Leonardo",
                "Andreoli",
                "delegacio.eetac@upc.edu",
                4,
                false,
                "HnmdIOCoTKusuKcCSyQCugr9v30fcQ5J",
                2,
                false
        ));
        repository.upsert(new Ticket(
                "Ada",
                "Lovelace",
                "ada@example.com",
                0,
                true,
                "USED_HASH",
                1,
                true
        ));
        this.eventManager = new EventManager(repository);
    }

    @Test
    public void verifyShouldReturnTicketByHash() throws Exception {
        Ticket ticket = eventManager.verify("HnmdIOCoTKusuKcCSyQCugr9v30fcQ5J");
        assertEquals("Leonardo", ticket.getNombre());
        assertEquals(4, ticket.getTipo());
    }

    @Test
    public void enterShouldConsumeTicket() throws Exception {
        Ticket consumed = eventManager.enter("HnmdIOCoTKusuKcCSyQCugr9v30fcQ5J");
        assertTrue(consumed.isConsumed());
    }

    @Test(expected = ApiException.class)
    public void enterShouldFailIfAlreadyConsumed() throws Exception {
        eventManager.enter("USED_HASH");
    }

    @Test
    public void listAllShouldReturnAllTickets() throws Exception {
        List<Ticket> tickets = eventManager.listAllTickets();
        assertEquals(2, tickets.size());
    }

    @Test
    public void listByEmailShouldFilterTickets() throws Exception {
        List<Ticket> tickets = eventManager.listTicketsByEmail("ada@example.com");
        assertEquals(1, tickets.size());
        assertEquals("USED_HASH", tickets.get(0).getHash());
    }

    @Test
    public void listUsedShouldReturnOnlyConsumedTickets() throws Exception {
        List<Ticket> tickets = eventManager.listUsedTickets();
        assertEquals(1, tickets.size());
        assertTrue(tickets.get(0).isConsumed());
    }

    @Test
    public void listUnusedShouldReturnOnlyAvailableTickets() throws Exception {
        List<Ticket> tickets = eventManager.listUnusedTickets();
        assertEquals(1, tickets.size());
        assertEquals("HnmdIOCoTKusuKcCSyQCugr9v30fcQ5J", tickets.get(0).getHash());
    }
}


