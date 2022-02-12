package com.bookend.bookservice.serviceTest;
import com.bookend.bookservice.exception.AlreadyExist;
import com.bookend.bookservice.exception.MandatoryFieldException;
import com.bookend.bookservice.exception.NotFoundException;
import com.bookend.bookservice.kafka.Producer;
import com.bookend.bookservice.model.Book;
import com.bookend.bookservice.model.Genre;
import com.bookend.bookservice.model.KafkaMessage;
import com.bookend.bookservice.model.SortedLists;
import com.bookend.bookservice.payload.BookRequest;
import com.bookend.bookservice.repository.BookRepository;
import com.bookend.bookservice.service.BookServiceImpl;
import com.bookend.bookservice.service.GenreService;
import com.bookend.bookservice.service.SortService;
import io.swagger.models.auth.In;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.stream.Collectors;
@RunWith(MockitoJUnitRunner.class)
public class BookServiceTest {
    private static final String BOOK_TOPIC = "adding-book";
    private static final String DELETE_TOPIC = "deleting-book";
    @Spy
    private BookServiceImpl bookServiceSpy;
    @Mock
    private BookRepository bookRepository;

    @Mock
    private Book bookMock;
    @Mock
    private GenreService genreService;
    @Mock
    private SortService sortService;
    @Mock
    private Producer producer;
    @InjectMocks
    private BookServiceImpl bookService;
    @Test
    public void shouldReturnBookWithGivenId() throws NotFoundException {
        final String id = "ash2jhs45";
        final Genre genre = new Genre("5asd23dfgf","Classics");
        final Book book = new Book(Integer.valueOf("123"),genre,"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        given(bookRepository.findBookById(id)).willReturn(book);
        final Book expected = bookService.getById(id);
        assertNotNull(expected);
        assertEquals(book,expected);

    }
    @Test
    public void shouldFailToReturnIfBookDoesNotExistWithGivenId(){
        final String id = "ash2jhs45";
        given(bookRepository.findBookById(id)).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            bookService.getById(id);
        });
    }
    @Test
    public void shouldReturnAllBooks(){
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        given(bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookName"))).willReturn(books);
        final List<Book> expected = bookService.getAll();
        assertNotNull(expected);
        assertEquals(books,expected);
    }
    @Test
    public void shouldReturnAuthorsBooksWithGivenId() throws NotFoundException {
        String authorid = "45afs34";
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",false,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Ivan Gonçarov","45afs34",false,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Ivan Gonçarov","45afs34",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        given(bookRepository.findByAuthorid(authorid)).willReturn(books);
        final List<Book> expected = bookService.findByAuthor(authorid);
        assertNotNull(expected);
        assertEquals(books,expected);
    }
    @Test
    public void shouldFailToReturnAuthorsBooksIfNoBookExistsWithGivenAuthorID(){
        String authorid = "45afs34";
        given(bookRepository.findByAuthorid(authorid)).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            bookService.findByAuthor(authorid);
        });
    }
    @Test
    public void shouldReturnAllBookThatAreNotVerified() throws NotFoundException {
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        given(bookRepository.findBookByVerifiedIsFalse()).willReturn(books);
        final List<Book> expected = bookService.findBookByVerifiedIsFalse();
        assertNotNull(expected);
        assertEquals(books,expected);
    }
    @Test
    public void shouldFailToReturnUnverifiedBooksIfNoBookisUnverified(){
        given(bookRepository.findBookByVerifiedIsFalse()).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            bookService.findBookByVerifiedIsFalse();
        });
    }
    @Test
    public void shouldDeleteBookWithGivenId() throws NotFoundException {
        final String bookId = "ash2jhs45";
        final Genre genre = new Genre("5asd23dfgf","Classics");
        final Book book = new Book(Integer.valueOf("123"),genre,"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        given(bookRepository.findBookById(bookId)).willReturn(book);
        bookService.delete(bookId);
        verify(bookRepository,times(1)).delete(any(Book.class));
        verify(producer).deleteBook(any()); //New Line added.
    }

    @Test
    public void shouldFailToDeleteBookIfNoBookExistsWithGivenId(){
        final String bookId = "ash2jhs45";
        given(bookRepository.findBookById(bookId)).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            bookService.delete(bookId);
        });
    }
    @Test
    public void shouldFailToSaveIfBookNameIsEmptyString(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics","....",true,"","Ivan Gonçarov","asfd54adsd","123654789123");
        assertThrows(MandatoryFieldException.class,()->{
           bookService.save(request);
        });
    }
    @Test
    public void shouldFailToSaveIfBookNameIsNull(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics","....",true,null,"Ivan Gonçarov","asfd54adsd","123654789123");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });

    }
    @Test
    public void shouldFailToSaveIfAuthorIDIsEmptyString(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics","....",true,"Oblomov","Ivan Gonçarov","","123654789123");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });
    }
    @Test
    public void shouldFailToSaveIfAuthorIDIsNull(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics","....",true,"Oblomov","Ivan Gonçarov",null,"123654789123");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });

    } @Test
    public void shouldFailToSaveIfDescriptionIsEmptyString(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics","",true,"Oblomov","Ivan Gonçarov","asfd54adsd","123654789123");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });
    }
    @Test
    public void shouldFailToSaveIfDescriptionIsNull(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics",null,true,"Oblomov","Ivan Gonçarov","asfd54adsd","123654789123");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });

    }
    @Test
    public void shouldFailToSaveIfPageIsNull(){
        final BookRequest request = new BookRequest(null,"Classics","....",true,"Oblomov","Ivan Gonçarov","asfd54adsd","123654789123");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });

    }
    @Test
    public void shouldFailToSaveIfISBNIsEmptyString(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics","...",true,"Oblomov","Ivan Gonçarov","asfd54adsd","");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });
    }
    @Test
    public void shouldFailToSaveIfISBNIsNull(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics","...",true,"Oblomov","Ivan Gonçarov","asfd54adsd",null);
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });

    }
    @Test
    public void shouldFailToSaveIfGenreIsEmptyString(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),"","....",true,"Oblomov","Ivan Gonçarov","asfd54adsd","123654789123");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });
    }
    @Test
    public void shouldFailToSaveIfGenreIsNull(){
        final BookRequest request = new BookRequest(Integer.valueOf("123"),null,"...",true,"Oblomov","Ivan Gonçarov","asfd54adsd","123654789123");
        assertThrows(MandatoryFieldException.class,()->{
            bookService.save(request);
        });
    }


    @Test
    public void shouldSaveIfBookNameHaveMatchButAuthorIsDifferent() throws AlreadyExist, MandatoryFieldException {
        final String name = "Günlükler";
        final Genre journal = new Genre("5asd25dfgf","Journal");
        final Genre classics = new Genre("5asd25dfg4f","Classics");
        final BookRequest request = new BookRequest(Integer.valueOf("456"),"Classics","...",true,"Günlükler","Oguz Atay","45af5s34","1234567271234");
        final Book tobeSaved = new Book("ash1jhs45",Integer.valueOf("456"),classics,"...","Günlükler","Oguz Atay","45af5s34",true,"1234567271234");
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),journal,"...","Günlükler","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),journal,".....","Günlükler","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),journal,".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        when(bookRepository.findBookByBookName(name)).thenReturn(books);
        when(genreService.findByGenre(request.getGenre())).thenReturn(null);
        when(genreService.addNewGenre(request.getGenre())).thenReturn(classics);
        when(bookRepository.save(any(Book.class))).thenReturn(tobeSaved);
        final Book expected = bookService.save(request);
        assertNotNull(expected);
        assertEquals(request.getBookName(),expected.getBookName());
        verify(bookRepository).save(any(Book.class));
        verify(producer).publishBook(any());//TODO
        verify(genreService).addNewGenre(any()); //New Line Added
    }

    @Test
    public void shouldSaveIfBookNameAuthorHaveMatchButGenreIsDifferent() throws AlreadyExist, MandatoryFieldException {
        final String name = "Günlükler";
        final Genre genre = new Genre("5asd25dfgf","Journal");
        final BookRequest request = new BookRequest(Integer.valueOf("456"),"Journal","...",true,"Günlükler","Oguz Atay","45af5s34","1234567271234");
        final Book tobeSaved = new Book("ash1jhs45",Integer.valueOf("456"),genre,"...","Günlükler","Oguz Atay","45af5s34",true,"1234567271234");
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Fiction"),"...","Günlükler","Oguz Atay","45af5s34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),genre,".....","Günlükler","Oğuz Atay","45af5s34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),genre,".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        when(bookRepository.findBookByBookName(name)).thenReturn(books);
        when(genreService.findByGenre(request.getGenre())).thenReturn(genre);
        when(bookRepository.save(any(Book.class))).thenReturn(tobeSaved);
        final Book expected = bookService.save(request);
        assertNotNull(expected);
        assertEquals(request.getBookName(),expected.getBookName());
        //assertEquals(book0.getAuthorid(),expected.getAuthorid());

        verify(bookRepository).save(any(Book.class));
        verify(producer).publishBook(any());//TODO
    }

    @Test
    public void shouldSaveIfBookNameGenreHaveMatchButAuthorIsDifferent() throws AlreadyExist, MandatoryFieldException {
        final String name = "Günlükler";
        final Genre genre = new Genre("5asd25dfgf","Journal");
        final BookRequest request = new BookRequest(Integer.valueOf("456"),"Journal","...",true,"Günlükler","Oguz Atay","45af5s34","1234567271234");
        final Book tobeSaved = new Book("ash1jhs45",Integer.valueOf("456"),genre,"...","Günlükler","Oguz Atay","45af5s34",true,"1234567271234");
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Fiction"),"...","Günlükler","Edmondo de Amicis","44afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),genre,".....","Günlükler","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd23dfgf","Fiction"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        when(bookRepository.findBookByBookName(name)).thenReturn(books);
        when(genreService.findByGenre(request.getGenre())).thenReturn(null);
        when(bookRepository.save(any(Book.class))).thenReturn(tobeSaved);
        final Book expected = bookService.save(request);
        assertNotNull(expected);
        assertEquals(request.getBookName(),expected.getBookName());
        //assertEquals(book0.getAuthorid(),expected.getAuthorid());


        verify(bookRepository).save(any(Book.class));
        verify(producer).publishBook(any());//TODO
    }

    @Test
    public void shouldSaveIfBookNameAuthorGenreHaveMatchButISBNIsDifferent() throws AlreadyExist, MandatoryFieldException {
        final String name = "Günlükler";
        final Genre genre = new Genre("5asd25dfgf","Journal");
        final BookRequest request = new BookRequest(Integer.valueOf("456"),"Journal","...",true,"Günlükler","Oguz Atay","45af5s34","1234567271234");
        final Book tobeSaved = new Book("ash1jhs45",Integer.valueOf("456"),genre,"...","Günlükler","Oguz Atay","45af5s34",true,"1234567271234");
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),genre,"...","Günlükler","Oguz Atay","45af5s34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),genre,".....","Günlükler","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),genre,".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        when(bookRepository.findBookByBookName(name)).thenReturn(books);
        when(genreService.findByGenre(request.getGenre())).thenReturn(genre);
        when(bookRepository.save(any(Book.class))).thenReturn(tobeSaved);
        final Book expected = bookService.save(request);
        assertNotNull(expected);
        assertEquals(request.getBookName(),expected.getBookName());
        verify(bookRepository).save(any(Book.class));
        verify(producer).publishBook(any());//TODO
    }
/*

    when(bookRepository.save(any(Book.class))).thenCallRealMethod();
    doCallRealMethod().when(book).setBookName(any(String.class));
    doCallRealMethod().when(book).getBookName();

    */

    /*

        doNothing().when(bookMock).setBookName(any(String.class));
        doNothing().when(bookMock).setAuthor(any(String.class));
        doNothing().when(bookMock).setAuthorid(any(String.class));
        doNothing().when(bookMock).setDescription(any(String.class));
        doNothing().when(bookMock).setPage(any(Integer.class));
        doNothing().when(bookMock).setGenre(any());
        doNothing().when(bookMock).setVerified(any(Boolean.class));
        doNothing().when(bookMock).setISBN(any(String.class));


        verify(bookMock).setBookName(any());
        verify(bookMock).setAuthor(any());
        verify(bookMock).setAuthorid(any());
        verify(bookMock).setDescription(any());
        verify(bookMock).setPage(any());
        verify(bookMock).setGenre(any());
        verify(bookMock).setISBN(any());
        verify(bookMock).setVerified(any(Boolean.class));
     */
    @Test
    public void shouldNotSaveIfBookNameAuthorGenreISBNHaveMatch(){
        final String name = "Günlükler";
        final BookRequest request = new BookRequest(Integer.valueOf("456"),"Journal","...",true,"Günlükler","Oguz Atay","45af5s34","1234567891234");
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd25dfgf","Journal"),"...","Günlükler","Oguz Atay","45af5s34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Edmondo de Amicis","45af5s34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45af5s34",false,"1234567891234");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        when(bookRepository.findBookByBookName(name)).thenReturn(books);
        assertThrows(AlreadyExist.class,()->{
            bookService.save(request);
        });
        verify(bookRepository,never()).save(any(Book.class));
    }

//yeni eklenen method
/*
    @Test
    public void shouldSaveIfBookNameGenreHaveISBNMatchButAuthorDifferent() throws AlreadyExist, MandatoryFieldException{
        final String name = "Günlükler";
        final Genre genre = new Genre("5asd25dfgf","Journal");
        final BookRequest request = new BookRequest(Integer.valueOf("456"),"Journal","...",true,"Günlükler","Oguz Atay","45af5s34","1234567891234");
        final Book tobeSaved = new Book("ash1jhs45",Integer.valueOf("456"),genre,"...","Günlükler","Oguz Atay","45af5s34",true,"1234567891234");
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd25dfgf","Journal"),"...","Günlükler","Oguz Atay","45af5s341",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Edmondo de Amicis","45af5s34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45af5s34",false,"12345678912342");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        when(bookRepository.findBookByBookName(name)).thenReturn(books);
        when(bookRepository.save(any(Book.class))).thenReturn(tobeSaved);
        Book expected = bookService.save(request);
        assertNotNull(expected);
        verify(bookRepository).save(any(Book.class));
    }


    //yeni eklenen method

    @Test
    public void shouldSaveIfBookNameAuthorHaveISBNMatchButGenreDifferent() throws AlreadyExist, MandatoryFieldException{
        final String name = "Günlükler";
        final Genre genre = new Genre("5asd25dfgf","Journal");
        final BookRequest request = new BookRequest(Integer.valueOf("456"),"Journal","...",true,"Günlükler","Oguz Atay","45af5s34","1234567891234");
        final Book tobeSaved = new Book("ash1jhs45",Integer.valueOf("456"),genre,"...","Günlükler","Oguz Atay","45af5s34",true,"1234567891234");
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd25dfg","Günlük"),"...","Günlükler","Oguz Atay","45af5s34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Edmondo de Amicis","45af5s34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45af5s34",false,"12345678912342");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        when(bookRepository.findBookByBookName(name)).thenReturn(books);
        when(bookRepository.save(any(Book.class))).thenReturn(tobeSaved);
        Book expected = bookService.save(request);
        assertNotNull(expected);
        verify(bookRepository).save(any(Book.class));
    }*/


    @Test
    public void shouldReturnSortedBooksByRate() throws NotFoundException {
        final SortedLists sortedLists = new SortedLists();
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> sortedByRate = new ArrayList<>();
        sortedByRate.add(book0);
        sortedByRate.add(book1);
        sortedByRate.add(book2);
        final List<Book> sortedByComment = Arrays.asList(book1,book1,book0);
        sortedLists.setSortedByComment(sortedByComment);
        sortedLists.setSortedByRate(sortedByRate);
        when(sortService.findOne()).thenReturn(sortedLists);
        final List<Book> expected = bookService.search(null,null,true,false);
        assertNotNull(expected);
        //assertEquals(expected.get(0),sortedByRate.get(2));
        Collections.reverse(sortedByRate);
        assertEquals(expected.get(0).getBookName(),sortedByRate.get(0).getBookName());
        assertEquals(expected.get(2),sortedByRate.get(2));
    }

    /*


        assertEquals(expected.get(0).getRate(),sortedByRate.get(0).getRate());
     */
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldReturnAllBooksIfGivenSearchFieldsAreFalseOrNull() throws NotFoundException {

        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);

       // Mockito.doReturn(books).when(bookServiceSpy).getAll();
        when(bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookName"))).thenReturn(books);
        final List<Book> expected = bookService.search(null,null,false,false);
        assertNotNull(expected);
        verify(sortService,never()).findOne();

    }
    private boolean compareTwoList(List<Book> l1,List<Book> l2){
        List<Book> cp = new ArrayList<Book>(l1);
        for ( Object o : l2 ) {
            if ( !cp.remove( o ) ) {
                return false;
            }
        }
        return cp.isEmpty();
    }
    @Test
    public void shouldReturnSortedBooksByNumberOfComments() throws NotFoundException {
        final SortedLists sortedLists = new SortedLists();
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> sortedByRate = Arrays.asList(book0,book1,book2);
        final List<Book> sortedByComment = Arrays.asList(book1,book1,book0);
        sortedLists.setSortedByComment(sortedByComment);
        sortedLists.setSortedByRate(sortedByRate);
        when(sortService.findOne()).thenReturn(sortedLists);
        final List<Book> expected = bookService.search(null,null,false,true);
        assertNotNull(expected);
        Collections.reverse(sortedLists.getSortedByComment());
        assertEquals(expected.get(0),sortedLists.getSortedByComment().get(0));
        assertEquals(expected.get(2),sortedLists.getSortedByComment().get(2));
    }
    //assertTrue(expected.equals(sortedLists.getSortedByComment()));

    /*


     */
    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    public void shouldReturnBooksFilteredByGivenTitle() throws NotFoundException {
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        //Mockito.doReturn(books).when(bookServiceSpy).getAll();
        when(bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookName"))).thenReturn(books);
        final List<Book> expected = bookService.search("Ob",null,false,false);
        assertNotNull(expected);
        verify(sortService,never()).findOne();

    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldReturnBooksFilteredByGenre() throws NotFoundException {
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        //Mockito.doReturn(books).when(bookServiceSpy).getAll();
        when(bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookName"))).thenReturn(books);
        final List<Book> expected = bookService.search(null,"Classics",false,false);
        assertNotNull(expected);
        verify(sortService,never()).findOne();
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToReturnBooksFilteredByGivenTitle()  {
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        //Mockito.doReturn(books).when(bookServiceSpy).getAll();
        when(bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookName"))).thenReturn(books);
        assertThrows(NotFoundException.class,()->{
            bookService.search("Kacıs",null,false,false);
        });
        verify(sortService,never()).findOne();
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToReturnBooksFilteredByGenre(){
        final Book book0 = new Book("ash2jhs45",Integer.valueOf("456"),new Genre("5asd23dfgf","Classics"),"...","Oblomov","Ivan Gonçarov","45afs34",true,"1234567891234");
        final Book book1 = new Book("ash2jhs25",Integer.valueOf("141"),new Genre("5asd24dsdgf","Fiction"),".....","Cocuk Kalbi","Edmondo de Amicis","44afs34",true,"1254566891234");
        final Book book2 = new Book("ash2sdhs44",Integer.valueOf("360"),new Genre("5asd25dfgf","Journal"),".....","Günlükler","Sylvia Plath","45afs84",false,"1234567891129");
        final List<Book> books = Arrays.asList(book0,book1,book2);
        //Mockito.doReturn(books).when(bookServiceSpy).getAll();
        when(bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookName"))).thenReturn(books);
        assertThrows(NotFoundException.class,()->{
            bookService.search(null,"Science-Fiction",false,false);
        });
        verify(sortService,never()).findOne();
    }
    @Test
    public void verifyBook() throws NotFoundException {
        final String id = "ash2jhs45";
        final Genre genre = new Genre("5asd23dfgf","Classics");
        final Book book = new Book(Integer.valueOf("123"),genre,"...","Oblomov","Ivan Gonçarov","45afs34",false,"1234567891234");
        given(bookRepository.findBookById(id)).willReturn(book);
        //book.setVerified(Boolean.TRUE); ----> This line deleted.
        given(bookRepository.save(any(Book.class))).willReturn(book);
        final Book expected = bookService.verify(id);
        assertNotNull(expected);
        assertTrue(expected.getVerified()); //New line added.
    }


    @Test
    public void failToVerifyWhenIDHaveNotMatch(){
        final String id = "ash2jhs45";
        given(bookRepository.findBookById(id)).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            bookService.verify(id);
        });
    }
    @Test
    public void updateBook(){
        final String id = "ash2jhs45";
        final Genre genre = new Genre("5asd23dfgf","Classics");
        final Book book = new Book(Integer.valueOf("123"),genre,"...","Oblomov","Ivan Gonçarov","45afs34",false,"1234567891234");
        given(bookRepository.save(any(Book.class))).willReturn(book);
        final Book expected = bookService.update(book);
        assertNotNull(expected);

    }




    //final BookRequest request = new BookRequest(Integer.valueOf("123"),"Classics","....",true,"Oblomov","Ivan Gonçarov","asfd54adsd","123654789123");



}
