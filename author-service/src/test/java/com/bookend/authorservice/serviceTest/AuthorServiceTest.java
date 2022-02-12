package com.bookend.authorservice.serviceTest;

import com.bookend.authorservice.exception.*;
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

import java.util.*;

import org.junit.Test;
import org.mockito.Spy;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import org.mockito.junit.MockitoJUnitRunner;


import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorServiceTest {

    @InjectMocks
    private AuthorServiceImpl authorService;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookServiceImpl bookService;

    @Spy
    private AuthorServiceImpl authorServiceSpy;


    @Test
    public void shouldGetAuthorSuccesfully() throws NotFoundException {
        final String id = "ajsdhj23e";
        final Author author = new Author("ajsdhj23e","Ahmet Umit");
        given(authorRepository.findAuthorById(id)).willReturn(author);
        final Author expected = authorService.getById(id);
        assertNotNull(expected);
        assertEquals(author,expected);
    }
    @Test
    public void failToGetByIDIfIdDoesNotMatchAnyAuthor() {
        final String id = "ajsdhj23e";
        given(authorRepository.findAuthorById(id)).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            authorService.getById(id);
        });

    }
    @Test
    public void failToUpdateAuthorBooksIfAuthorIdDoesNotMatchAnyExistingAuthor(){
        final Author author = new Author("ajsdhj23e","Ahmet Umit");
        final Book book = new Book("a4ds7dsf8vsd",author);
        final Map<String,String> msg =  new HashMap<>();
        msg.put("author","");
        msg.put("book","a4ds7dsf8vsd");
        when(authorRepository.findAuthorById(msg.get("author"))).thenReturn(null);
        assertThrows(NotFoundException.class, ()->{
            authorService.update(msg);
        });
        verify(authorRepository,never()).save(any(Author.class));

    }

    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldUpdateAuthorBooks() throws MandatoryFieldException, NotFoundException {
        final Author author = new Author("ajsdhj23e","Ahmet Umit");
        final Book book = new Book("a4ds7dsf8vsd",author);
        final Map<String,String> msg =  new HashMap<>();
        msg.put("author","ajsdhj23e");
        msg.put("book","a4ds7dsf8vsd");
        given(bookService.save(any(Book.class))).willReturn(book);
        when(authorRepository.findAuthorById(msg.get("author"))).thenReturn(author);
        given(authorRepository.save(any(Author.class))).willReturn(author);
        Mockito.doReturn(author).when(authorServiceSpy).getById(author.getId());
        final Author expected = authorService.update(msg);
        assertNotNull(expected);
        verify(authorRepository).save(any(Author.class));
    }
    @Test
    public void authorIsNotSavedIfNameFieldIsEmpty(){
        final AuthorRequest request = new AuthorRequest(null,"He is ..","1998-02-11" ,"2002-02-07");
        assertThrows(MandatoryFieldException.class,() -> {
            authorService.save(request);
        });
        verify(authorRepository, never()).save(any(Author.class));


    }
    @Test
    public void authorIsNotSavedIfBirthDateIsEmpty(){
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is..",null ,"2002-02-07");
        assertThrows(MandatoryFieldException.class,() -> {
            authorService.save(request);
        });
        verify(authorRepository, never()).save(any(Author.class));
    }
    @Test
    public void authorIsNotSavedIfBiographyIsEmpty(){
        final AuthorRequest request = new AuthorRequest("Ahmet Umit",null,"1998-02-11" ,"2002-02-07");
        assertThrows(MandatoryFieldException.class,() -> {
            authorService.save(request);
        });
        verify(authorRepository, never()).save(any(Author.class));
    }
    @Test
    public void authorIsNotSavedIfAuthorAlreadyExists(){
        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.parse("1998-02-11"),LocalDate.parse("2002-02-07"));
        final Author author1 = new Author("ajsdhj24e","Ahmet Umit","Umit was born..", LocalDate.now(),LocalDate.now());
        final List<Author> authors = Arrays.asList(author,author1);
        given(authorRepository.findByName(author.getName())).willReturn(authors);
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is ..","1998-02-11","2002-02-07");
        assertThrows(AuthorAlreadyExists.class,()->{
            authorService.save(request);
        });
        verify(authorRepository, never()).save(any(Author.class));
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldSaveIfAuthorRequestDeathDateIsNullAndNameBirthdayHaveMatchButDeathDayIsDifferent() throws MandatoryFieldException, AuthorAlreadyExists,NullAuthorException {
        final Author toBeSaved = new Author("ajsdhj23k","Ahmet Umit","He is ..", LocalDate.parse("1990-02-11"),null);
        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.parse("1998-02-11"),LocalDate.parse("2002-02-07"));
        final Author author1 = new Author("ajsdhj24e","Ahmet Umit","Umit was born..", LocalDate.now(),null);
        final List<Author> authors = Arrays.asList(author,author1);
        given(authorRepository.findByName(author.getName())).willReturn(authors);
        given(authorRepository.save(any(Author.class))).willReturn(toBeSaved);
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is ..","1998-02-11",null);
        final Author saved = authorService.save(request);
        assertNotNull(saved);
        verify(authorRepository).save(any(Author.class));

    }
    @Test
    public void shouldNotSaveIfAuthorRequestDeathDateIsNullAndNameBirthDayDeathDayAreSameWithRequest(){
        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.parse("1998-02-11"),null);
        final Author author1 = new Author("ajsdhj24e","Ahmet Umit","Umit was born..", LocalDate.now(),null);
        final List<Author> authors = Arrays.asList(author,author1);
        given(authorRepository.findByName(author.getName())).willReturn(authors);
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is ..","1998-02-11",null);
        assertThrows(AuthorAlreadyExists.class,()->{
            authorService.save(request);
        });
        verify(authorRepository, never()).save(any(Author.class));
    }
    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    public void shouldSaveIfAuthorRequestDeathDateIsNotNullAndNameBirthdayHaveMatchButDeathDayIsDifferent() throws MandatoryFieldException, AuthorAlreadyExists, NullAuthorException {

        final Author toBeSaved = new Author("ajsdhj23k","Ahmet Umit","He is ..", LocalDate.parse("1998-02-11"),LocalDate.parse("2002-02-08"));
        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.parse("1998-02-11"),null);
        final Author author1 = new Author("ajsdhj24e","Ahmet Umit","Umit was born..", LocalDate.now(),null);
        final List<Author> authors = Arrays.asList(author,author1);
        given(authorRepository.findByName(author.getName())).willReturn(authors);
        when(authorRepository.save(any(Author.class))).thenReturn(toBeSaved);
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is ..","1998-02-11","2002-02-08");
        final Author saved = authorService.save(request);
        assertNotNull(saved);
        verify(authorRepository).save(any(Author.class));
    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    public void shouldSaveIfAuthorRequestDeathDateIsNotNullAndNameBirthdayHaveMatchButDeathDayIsDifferent_secondCade() throws MandatoryFieldException, AuthorAlreadyExists, NullAuthorException {

        final Author toBeSaved = new Author("ajsdhj23k","Ahmet Umit","He is ..", LocalDate.parse("1998-02-11"),LocalDate.parse("2002-02-08"));
        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.parse("1998-02-11"),LocalDate.parse("2003-02-08"));
        final Author author1 = new Author("ajsdhj24e","Ahmet Umit","Umit was born..", LocalDate.now(),null);
        final List<Author> authors = Arrays.asList(author,author1);
        given(authorRepository.findByName(author.getName())).willReturn(authors);
        when(authorRepository.save(any(Author.class))).thenReturn(toBeSaved);
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is ..","1998-02-11","2002-02-08");
        final Author saved = authorService.save(request);
        assertNotNull(saved);
        assertNotEquals(saved.getDateOfDeath(),request.getDateOfDeath());
        verify(authorRepository).save(any(Author.class));
    }




    @Test
    public void shouldNotSaveIfAuthorRequestDeathDateIsNotNullAndNameBirthDayDeathDayAreSameWithRequest()  {

        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.parse("1998-02-11"),LocalDate.parse("2002-02-07"));
        final Author author1 = new Author("ajsdhj24e","Ahmet Umit","Umit was born..", LocalDate.now(),null);
        final List<Author> authors = Arrays.asList(author,author1);
        given(authorRepository.findByName(author.getName())).willReturn(authors);
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is ..","1998-02-11","2002-02-07");
        assertThrows(AuthorAlreadyExists.class,()->{
            authorService.save(request);
        });
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    public void shouldSaveWhenAuthorRequestOnlyNameHasMatchAndOtherFieldsAreDifferent() throws MandatoryFieldException, AuthorAlreadyExists,NullAuthorException {
        final Author toBeSaved = new Author("ajsdhj23k","Ahmet Umit","He is ..", LocalDate.parse("1990-04-14"),LocalDate.parse("2000-12-28"));
        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.parse("1998-02-11"),LocalDate.parse("2002-02-07"));
        final Author author1 = new Author("ajsdhj24e","Ahmet Umit","Umit was born..", LocalDate.now(),LocalDate.now());
        final List<Author> authors = Arrays.asList(author,author1);
        given(authorRepository.findByName(author.getName())).willReturn(authors);
        when(authorRepository.save(any(Author.class))).thenReturn(toBeSaved);
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is ..","1990-04-14","2000-12-28");
        final Author saved = authorService.save(request);
        assertNotNull(saved);
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    public void shouldReturnAllAuthor(){
        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long life ..", LocalDate.now(),LocalDate.now());
        final Author author1 = new Author("ajsdhj24e","Selcuk Aydemir","Aydemir was born..", LocalDate.now(),LocalDate.now());
        final Author author2= new Author("ajsdhj21e","Andy Weir","Andy is ...", LocalDate.now(),LocalDate.now());
        final List<Author> authors = Arrays.asList(author,author1,author2);
        given(authorRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).willReturn(authors);
        final List<Author> expected = authorService.getAll();
        assertNotNull(expected);
        assertEquals(authors,expected);
    }
    @Test
    public void shouldReturnAllAuthorMatchesGivenTitle(){
        final String title = "umit";
        final Author author = new Author("ajsdhj23e","Ahmet Umit","Long", LocalDate.of(1998,01,11),null);
        List<Author> authors = Arrays.asList(author);
        given(authorRepository.findByNameContainingIgnoreCase(title)).willReturn(authors);
        final List<Author> expected = authorService.search(title);
        assertNotNull(expected);
        assertEquals(authors,expected);

    }


    @Test
    public void shouldSaveIfAuthorDoesNotExist()throws MandatoryFieldException, AuthorAlreadyExists,NullAuthorException{
        final Author author = new Author("ajsdhj23e","Ahmet Umit","He is ..", LocalDate.now(),LocalDate.now());
        ArrayList<Author> arrayList = new ArrayList<Author>();
        given(authorRepository.findByName(any(String.class))).willReturn(arrayList);
        final AuthorRequest request = new AuthorRequest("Ahmet Umit","He is ..","1998-02-11" ,"2002-02-07");
        given(authorRepository.save(any(Author.class))).willReturn(author);
        final Author saved = authorService.save(request);
        assertEquals(author,saved);
        verify(authorRepository).save(any(Author.class));

    }



}