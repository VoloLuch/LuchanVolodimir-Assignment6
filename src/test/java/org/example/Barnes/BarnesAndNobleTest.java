package org.example.Barnes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Specification based testing:
 *  - test constructor initializes as false
 *  - method getPriceForCart:
 *    - input null | returns null
 *    - given order | correct total
 *    - exceeds available stock | adds unavailable books to summary
 *    - correctly calls buyBook on process
 *
 */
public class BarnesAndNobleTest {
    private BookDatabase bd;
    private BuyBookProcess process;
    private BarnesAndNoble bAn;

    @BeforeEach
    void setUp(){
        bd = mock(BookDatabase.class);
        process = mock(BuyBookProcess.class);
        bAn = new BarnesAndNoble(bd, process);
    }

    @Test
    @DisplayName("Specification-based: Constructor")
    void testConstructor(){
        assertThat(bAn).isNotNull();
        assertThat(bd).isNotNull();
        assertThat(process).isNotNull();
    }

    @Test
    @DisplayName("Specification-based: getPrice(null) = null")
    void testNullGetPrice(){
        assertThat(bAn.getPriceForCart(null)).isNull();
    }

    @Test
    @DisplayName("Specification-based: getPrice calculates total")
    void testGetPriceForCartCalculateTotal(){
        Map<String, Integer> order= new HashMap<>();
        order.put("12345-1", 2);
        Book book = new Book("12345-1", 10, 5);
        when(bd.findByISBN("12345-1")).thenReturn(book);
        PurchaseSummary summary = bAn.getPriceForCart(order);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalPrice()).isEqualTo(20);
        verify(process).buyBook(book, 2);
    }

    @Test
    @DisplayName("Structural-Based: update summary")
    void testUpdateSummary(){
        Map<String, Integer> order= new HashMap<>();
        order.put("12345-2", 3);
        Book book = new Book("12345-2", 10, 5);
        when(bd.findByISBN("12345-2")).thenReturn(book);
        PurchaseSummary summary = bAn.getPriceForCart(order);

        assertThat(summary.getTotalPrice()).isEqualTo(30);
        verify(process).buyBook(book, 3);
    }

    @Test
    @DisplayName("Structural-based: handle empty order")
    void testCartEmptyOrder(){
        Map<String, Integer> order = new HashMap<>();
        PurchaseSummary summary = bAn.getPriceForCart(order);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalPrice()).isEqualTo(0);
        assertThat(summary.getUnavailable()).isEmpty();
    }

    @Test
    @DisplayName("Structural-based: handle unavailable")
    void testCartUnavailableBook(){
        Map<String, Integer> order = new HashMap<>();
        order.put("12345-3", 3);
        Book book = new Book("12345-3", 10, 2);
        when(bd.findByISBN("12345-3")).thenReturn(book);
        PurchaseSummary summary = bAn.getPriceForCart(order);

        assertThat(summary.getTotalPrice()).isEqualTo(20);
        assertThat(summary.getUnavailable()).hasSize(1);
        verify(process).buyBook(book, 2);
    }

    @Test
    @DisplayName("Structural-based: non-existing ISBN")
    void testCartISBNNotExist(){
        Map<String, Integer> order = new HashMap<>();
        order.put("non-existing", 2);
        when(bd.findByISBN("non-existing")).thenReturn(null);
        PurchaseSummary summary = bAn.getPriceForCart(order);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalPrice()).isEqualTo(0);
        assertThat(summary.getUnavailable()).hasSize(1);
    }

}
