package com.ratecommentservice.serviceTest;


import com.ratecommentservice.exception.BookNotFound;
import com.ratecommentservice.exception.PostCommentNotFound;
import com.ratecommentservice.exception.RateNotFound;
import com.ratecommentservice.kafka.Producer;
import com.ratecommentservice.model.Book;
import com.ratecommentservice.model.Comment;
import com.ratecommentservice.model.PostComment;
import com.ratecommentservice.model.Rate;
import com.ratecommentservice.payload.KafkaMessage;
import com.ratecommentservice.payload.RateRequest;
import com.ratecommentservice.repository.BookRepository;
import com.ratecommentservice.repository.RateRepository;
import com.ratecommentservice.service.BookServiceImpl;
import com.ratecommentservice.service.RateServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RateServiceTest {
    @Mock
    private RateRepository rateRepository;
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private RateServiceImpl rateService;
    @Mock
    private Producer producer;
    @Mock
    private Book bookMock;

    private static final String RATE_TOPIC = "new-rate";


    @Test
    public void shouldGetUserCommentsWithGivenUsernameSuccessfully(){
        List<Rate> rates = new ArrayList<>();
        final Book book = new Book("5", "Yuzbasının Kızı");
        final Rate rate = new Rate(book,"huri",3.0 );
        rates.add(rate);
        given(rateRepository.findByUsername("huri")).willReturn(rates);
        final List<Rate> expected = rateService.getUserRates("huri");
        assertEquals(rates,expected);
    }

    @Test
    public void shouldGetBookAverageRateWithGivenBookIdSuccessfully() throws BookNotFound, RateNotFound {
        String id = "5";
        final Book book = new Book(id, "Yuzbasının Kızı");
        //final Rate rate = new Rate(book,"huri",3.0 );
        book.setAverageRate(2.5);
        given(bookRepository.findBookByBookId(id)).willReturn(book);
        final Double expected = rateService.getBookAverageRate(id);
        assertEquals(book.getAverageRate(),expected);
    }

    @Test
    public void failToGetBookAverageRateIfGivenBookIdDoesNotMatch() throws BookNotFound, RateNotFound {
        String id = "5";
        given(bookRepository.findBookByBookId(id)).willReturn(null);
        assertThrows(BookNotFound.class,()->{
            rateService.getBookAverageRate(id);
        });
    }

    @Test
    public void failToGetBookAverageRateIfBookAverageRateIsNull() throws BookNotFound, RateNotFound{
        String id = "5";
        final Book book = new Book(id, "Yuzbasının Kızı");
        given(bookRepository.findBookByBookId(id)).willReturn(book);
        assertThrows(RateNotFound.class,()->{
            rateService.getBookAverageRate(id);
        });
    }

    @Test
    public void shouldGetRateWithGivenRateIdSuccessfully() throws BookNotFound, RateNotFound {
        String id = "5";
        Long rateId = Long.valueOf("7");
        final Book book = new Book(id, "Yuzbasının Kızı");
        final Rate rate = new Rate(rateId,book,"huri",3.0 );
        given(rateRepository.findByRateId(rateId)).willReturn(rate);
        final Rate expected = rateService.findByRateID(rateId);
        assertEquals(rate,expected);
    }

    @Test
    public void failToGetRateIfRateIdDoesNotMatch() throws BookNotFound, RateNotFound{
        Long rateId = Long.valueOf("7");
        given(rateRepository.findByRateId(rateId)).willReturn(null);
        assertThrows(RateNotFound.class,()->{
            rateService.findByRateID(rateId);
        });
    }
    @Test
    public void failToGetRateIfGivenBookIdDoesNotMatch() throws BookNotFound, RateNotFound {
        String id = "5";
        given(bookRepository.findBookByBookId(id)).willReturn(null);
        assertThrows(BookNotFound.class,()->{
            rateService.findRateByBookIdandUsername(id,"huri");
        });
    }

    @Test
    public void failToGetRateIfBookAndUsernameDoesNotMatch() throws BookNotFound, RateNotFound {
        String id = "5";
        final Book book = new Book(id, "Yuzbasının Kızı");
        given(bookRepository.findBookByBookId(id)).willReturn(book);
        given(rateRepository.findByBookAndUsername(book,"huri")).willReturn(null);
        assertThrows(RateNotFound.class,()->{
            rateService.findRateByBookIdandUsername(id,"huri");
        });
    }

    @Test
    public void shouldDeleteRateWithGivenRate()  {
        final Long rateId = Long.valueOf(7);
        final Book book = new Book("5", "Yuzbasının Kızı");
        final Rate rate = new Rate(rateId,book,"huri",3.0 );
        rateService.deleteRate(rate);
        verify(rateRepository,times(1)).delete(any(Rate.class));
    }

    @Test
    public void shouldDeleteRatesWithGivenBookId()  {
        List<Rate> rates = new ArrayList<>();
        final Long rateId = Long.valueOf(7);
        final Book book = new Book("5", "Yuzbasının Kızı");
        final Rate rate = new Rate(rateId,book,"huri",3.0 );
        rates.add(rate);
        given(bookRepository.findBookByBookId("5")).willReturn(book);
        given(rateRepository.findByBook(book)).willReturn(rates);
        //rates.forEach(ra -> rateRepository.delete(ra));
        rateService.deleteRateByBookId("5");
        verify(rateRepository,times(1)).delete(any(Rate.class));
    }

    @Test
    public void shouldSaveGivenRateSuccessfully() throws BookNotFound, RateNotFound {
        String id = "5";
        final Long rateId = Long.valueOf(7);
        final Book book = new Book(id, "Yuzbasının Kızı");
        final Rate rate = new Rate(rateId,book,"huri",3.0 );
        final RateRequest rateRequest = new RateRequest(3.0, id, "Yuzbasının Kızı");
        given(bookRepository.findBookByBookId(rateRequest.getBookId())).willReturn(book);
        given(rateRepository.findByBookAndUsername(book,"huri")).willReturn(rate);
        rate.setRate(rateRequest.getRate());
        book.getRates().add(rate);
        book.setAverageRate(book.calAv());
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(rateRepository.save(any(Rate.class))).thenReturn(rate);
        final Rate saved = rateService.save(rateRequest, "huri");
        assertNotNull(saved);
        verify(rateRepository).save(any(Rate.class));
        verify(producer).publishNewRate(any()); // New Line added
    }



    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldSaveNonExistingRateWhenBookDoesNotExist() throws BookNotFound, RateNotFound {
        final String id = "5";
        final String username = "huri";
        final Long rateId = Long.valueOf(7);

        Book book = new Book();
        final RateRequest rateRequest = new RateRequest(3.0, id, "Yuzbasının Kızı");
        given(bookRepository.findBookByBookId(rateRequest.getBookId())).willReturn(null);
        book = new Book(rateRequest.getBookId(),rateRequest.getBookname());
        Rate rate = new Rate(rateId,book,username,rateRequest.getRate());
        book.setRates(new ArrayList(Arrays.asList(rate)));

        given(bookRepository.save(any(Book.class))).willReturn(book);
        given(rateRepository.findByBookAndUsername(book,username)).willReturn(null);
        given(rateRepository.save(any(Rate.class))).willReturn(rate);


        final Rate saved = rateService.save(rateRequest, username);
        assertNotNull(saved);
        verify(rateRepository,times(2)).save(any(Rate.class));
    }
}
