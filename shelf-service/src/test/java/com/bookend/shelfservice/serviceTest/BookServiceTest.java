package com.bookend.shelfservice.serviceTest;

import com.bookend.shelfservice.exception.AlreadyExists;
import com.bookend.shelfservice.exception.NotFoundException;
import com.bookend.shelfservice.exception.ShelfNotFound;
import com.bookend.shelfservice.exception.ShelfsBookNotFound;
import com.bookend.shelfservice.model.Shelf;
import com.bookend.shelfservice.model.ShelfsBook;
import com.bookend.shelfservice.model.Tag;
import com.bookend.shelfservice.payload.BookRequest;
import com.bookend.shelfservice.repository.BookRepository;
import com.bookend.shelfservice.repository.ShelfRepository;
import com.bookend.shelfservice.service.BookServiceImpl;
import com.bookend.shelfservice.service.TagServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ShelfRepository shelfRepository;
    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    public void getShelfsBookIfIdHaveMatchSuccessfully() throws ShelfsBookNotFound {
        String id = "5";
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Romantic"));
        final Shelf shelf = new Shelf("Recently Read","eda", tags);
        final ShelfsBook shelfsBook = new ShelfsBook(id, "The Fairy Tales",shelf);
        given(bookRepository.findBookById(Long.valueOf(id))).willReturn(shelfsBook);
        final ShelfsBook expected = bookService.getById(id);
        assertNotNull(expected);
        assertEquals(shelfsBook,expected);
    }

    @Test
    public void failToGetShelfsBooksIfIdDoesNotMatch() throws ShelfsBookNotFound {
        final Long id = Long.valueOf(5);
        given(bookRepository.findBookById(id)).willReturn(null);
        assertThrows(ShelfsBookNotFound.class,()->{
            bookService.getById(id.toString());
        });
    }

    @Test
    public void failToAddShelfsBookIfBookAlreadyExists(){
        List<ShelfsBook> books = new ArrayList<>();
        String id = "5";

        final Shelf shelf = new Shelf("Recently Read","eda");
        final BookRequest bookRequest = new BookRequest(id, "The Tales");
        final ShelfsBook shelfsBook = new ShelfsBook(id, "The Fairy Tales",shelf);
        books.add(shelfsBook);

        given(bookRepository.findShelfsBookByShelf(shelf)).willReturn(books);
        assertThrows(AlreadyExists.class,()->{
            bookService.saveOrUpdate(bookRequest,shelf);
        });
    }

    @Test
    public void shouldSaveGivenShelfsBookSuccessfully() throws AlreadyExists {
        String id = "5";
        final Shelf shelf = new Shelf("Recently Read","eda");
        final BookRequest bookRequest = new BookRequest(id, "The Fairy Tales");
        final ShelfsBook shelfsBook = new ShelfsBook(id, "The Fairy Tales",shelf);
        given(bookRepository.save(any(ShelfsBook.class))).willReturn(shelfsBook);
        final ShelfsBook saved = bookService.saveOrUpdate(bookRequest,shelf);
        assertNotNull(saved);
        verify(bookRepository).save(any(ShelfsBook.class));
    }

    @Test
    public void shouldDeleteBookWithGivenShelfIdAndBookId() throws NotFoundException {
        final Long id = Long.valueOf(5);
        final Shelf shelf = new Shelf(id,"Recently Read","eda", new ArrayList<Tag>());
        final ShelfsBook shelfsBook = new ShelfsBook("2", "The Fairy Tales",shelf);
        given(shelfRepository.findShelfById(id)).willReturn(shelf);
        given(bookRepository.findByBookIDAndShelf("2",shelf)).willReturn(shelfsBook);
        bookService.delete("2",id.toString());
        verify(bookRepository,times(1)).delete(any(ShelfsBook.class));
    }
    @Test
    public void failToDeleteShelfWithGivenShelfDoesNotExist() throws NotFoundException {
        final Long id = Long.valueOf(5);
        given(shelfRepository.findShelfById(id)).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            bookService.delete("2",id.toString());
        });
    }
    @Test
    public void failToDeleteShelfWithGivenShelfsBookDoesNotExist() throws NotFoundException {
        final Long id = Long.valueOf(5);
        final Shelf shelf = new Shelf(id,"Recently Read","eda", new ArrayList<Tag>());
        given(shelfRepository.findShelfById(id)).willReturn(shelf);
        given(bookRepository.findByBookIDAndShelf("2",shelf)).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            bookService.delete("2",id.toString());
        });
    }
    @Test
    public void shouldDeleteBookFromAllShelves() throws NotFoundException {
        List<ShelfsBook> shelfsBooks = new ArrayList<>();
        final Long id = Long.valueOf(5);
        final Shelf shelf = new Shelf(id,"Recently Read","eda", new ArrayList<Tag>());
        final ShelfsBook shelfsBook = new ShelfsBook("2", "The Fairy Tales",shelf);
        shelfsBooks.add(shelfsBook);
        given(bookRepository.findShelfsBookByBookID("2")).willReturn(shelfsBooks);
        bookService.deleteFromShelves("2");
        verify(bookRepository,times(1)).delete(any(ShelfsBook.class));
    }

    @Test
    public void failToDeleteBookFromAllShelvesIfBookDoesNotExistInShelves() throws NotFoundException {
        given(bookRepository.findShelfsBookByBookID("2")).willReturn(null);
        assertThrows(NotFoundException.class,()->{
            bookService.deleteFromShelves("2");
        });
    }


}
