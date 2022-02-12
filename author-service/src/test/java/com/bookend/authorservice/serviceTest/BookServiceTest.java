package com.bookend.authorservice.serviceTest;

import com.bookend.authorservice.exception.MandatoryFieldException;
import com.bookend.authorservice.exception.NotFoundException;
import com.bookend.authorservice.exception.AuthorAlreadyExists;
import com.bookend.authorservice.exception.NotFoundException;
import com.bookend.authorservice.exception.MandatoryFieldException;
import com.bookend.authorservice.model.Author;
import com.bookend.authorservice.repository.AuthorRepository;
import com.bookend.authorservice.service.AuthorServiceImpl;
import com.bookend.authorservice.model.Book;
import com.bookend.authorservice.payload.AuthorRequest;
import com.bookend.authorservice.repository.BookRepository;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;
import com.bookend.authorservice.service.BookServiceImpl;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.Spy;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import org.mockito.junit.MockitoJUnitRunner;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookServiceImpl bookService;
    @Spy
    private BookServiceImpl bookServiceSpy;

    @Test
    public void shouldReturnBookWithGivenId() throws NotFoundException {
        final String id = "2dfa8sd92hjhaf";
        final Book book = new Book(id,new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.now(),LocalDate.now()));
        given(bookRepository.findByBookId(id)).willReturn(book);
        final Book expected = bookService.findByBookid(id);

        assertEquals(expected,book);
    }
    @Test
    public void shouldSaveBook() throws MandatoryFieldException {
        final String id = "2dfa8sd92hjhaf";
        final Book book = new Book(id,new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.now(),LocalDate.now()));
        given(bookRepository.save(book)).willReturn(book);
        final Book expected = bookService.save(book);

        assertEquals(expected,book);

    }
    @Test
    public void shouldFailReturnBookWithGivenId()  {
        final String id = "2dfa8sd92hjhaf";
        // final Book book = new Book(id,new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.now(),LocalDate.now()));
        given(bookRepository.findByBookId(id)).willReturn(null);

        assertThrows(NotFoundException.class,()->{
            bookService.findByBookid(id);
        });
    }
    @Test
    public void shouldFailSaveBookIfIDIsEmpty() throws MandatoryFieldException {
        final Book book = new Book(null,new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.now(),LocalDate.now()));
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(book);
        });
        verify(bookRepository,never()).save(any(Book.class));

    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    public void shouldDelete() throws NotFoundException {
        final String id = "2dfa8sd92hjhaf";
        final Book book = new Book(id,new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.now(),LocalDate.now()));

        doReturn(book).when(bookServiceSpy).findByBookid(id);
        when(bookRepository.findByBookId(id)).thenReturn(book);
        bookService.deleteByBookId(id);
        verify(bookRepository,times(1)).deleteByBookId(id);
    }



}