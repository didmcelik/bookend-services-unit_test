package com.bookend.shelfservice.serviceTest;

import com.bookend.shelfservice.exception.AlreadyExists;
import com.bookend.shelfservice.exception.MandatoryFieldException;
import com.bookend.shelfservice.exception.NotFoundException;
import com.bookend.shelfservice.exception.ShelfNotFound;
import com.bookend.shelfservice.model.Shelf;
import com.bookend.shelfservice.model.ShelfsBook;
import com.bookend.shelfservice.model.Tag;
import com.bookend.shelfservice.payload.ShelfRequest;
import com.bookend.shelfservice.repository.ShelfRepository;
import com.bookend.shelfservice.repository.TagRepository;
import com.bookend.shelfservice.service.ShelfServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShelfServiceTest {
    @Mock
    private ShelfRepository shelfRepository;
    @Mock
    private TagRepository tagRepository;
    @InjectMocks
    private ShelfServiceImpl shelfService;

    @Test
    public void getShelfIfIdHaveMatchSuccesfully() throws ShelfNotFound {
        final Long id = Long.valueOf(5);
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Romantic"));
        final Shelf shelf = new Shelf(id,"Recently Read","eda", tags);
        given(shelfRepository.findShelfById(id)).willReturn(shelf);
        final Shelf expected = shelfService.getById(id);
        assertNotNull(expected);
        assertEquals(shelf,expected);
    }

    @Test
    public void failToGetShelfIfIdDoesNotMatch() throws ShelfNotFound {
        final Long id = Long.valueOf(5);
        given(shelfRepository.findShelfById(id)).willReturn(null);
        assertThrows(ShelfNotFound.class,()->{
            shelfService.getById(id);
        });
    }

    @Test
    public void shouldSaveShelfWithEmptyTagList() throws MandatoryFieldException, AlreadyExists {
        final Shelf shelf = new Shelf("Recently Read","eda");
        List<String> tags = new ArrayList<>();
        final ShelfRequest shelfRequest = new ShelfRequest("Recently Read", tags);
        given(shelfRepository.save(any(Shelf.class))).willReturn(shelf);
        final Shelf saved = shelfService.saveOrUpdate(shelfRequest, "eda");
        assertNotNull(saved);
        verify(shelfRepository).save(any(Shelf.class));

    }

    @Test
    public void failToSaveShelfIfShelfNameIsEmpty(){
        final ShelfRequest shelfRequest = new ShelfRequest(null, new ArrayList<String>());
        assertThrows(MandatoryFieldException.class,() -> {
            shelfService.saveOrUpdate(shelfRequest, "eda");
        });
        verify(shelfRepository, never()).save(any(Shelf.class));
    }




    @Test
    public void failToSaveIfShelfNameAlreadyInUse() throws MandatoryFieldException {
        final String username = "eda";
        List<Shelf> shelves = new ArrayList<>();
        final Shelf shelf = new Shelf("Recently Read",username);
        final Shelf shelf2 = new Shelf("read",username);
        shelves.add(shelf);
        shelves.add(shelf2);
        final ShelfRequest shelfRequest = new ShelfRequest("read", new ArrayList<String>());
        given(shelfRepository.findShelvesByUsername(username)).willReturn(shelves);
        assertThrows(AlreadyExists.class, ()->{
            shelfService.saveOrUpdate(shelfRequest, username);
        });
        verify(shelfRepository,never()).save(any(Shelf.class));
    }

    @Test
    public void shouldSaveShelfWhenTagListIsNotEmpty() throws ShelfNotFound, MandatoryFieldException, AlreadyExists {
        List<String> tagNames = new ArrayList<>();
        tagNames.add("Romantic");
        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag("Romantic");
        given(tagRepository.findByTag("Romantic")).willReturn(tag);
        tags.add(tag);
        final String username = "eda";
        List<Shelf> shelves = new ArrayList<>();
        final Shelf shelf1 = new Shelf("Recently Read",username);
        final Shelf shelf2 = new Shelf("read",username);
        shelves.add(shelf1);
        shelves.add(shelf2);
        final ShelfRequest shelfRequest = new ShelfRequest("To Read",tagNames);
        final Shelf shelf = new Shelf(shelfRequest.getShelfname(),username, tags);
        given(shelfRepository.save(any(Shelf.class))).willReturn(shelf);
        given(shelfRepository.findShelvesByUsername(username)).willReturn(shelves);
        final Shelf saved = shelfService.saveOrUpdate(shelfRequest, "eda");
        assertNotNull(saved);
        verify(shelfRepository).save(any(Shelf.class));
    }



    @Test
    public void shouldSave() {
        List<String> tagNames = new ArrayList<>();
        tagNames.add("Romantic");
        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag("Romantic");
        given(tagRepository.findByTag("Romantic")).willReturn(tag);
        tags.add(tag);
        final ShelfRequest shelfRequest = new ShelfRequest("To Read",tagNames);
        final Shelf shelf = new Shelf(shelfRequest.getShelfname(),"eda", tags);
        given(shelfRepository.save(any(Shelf.class))).willReturn(shelf);
        String myString = "this string has been constructed";
        assertDoesNotThrow(() -> shelfService.saveOrUpdate(shelfRequest, "eda"));
        verify(shelfRepository).save(any(Shelf.class));


    }
    @Test
    public void shouldReturnAllShelvesWithGivenUsername(){
        List<Shelf> shelves = new ArrayList<>();
        final Shelf shelf = new Shelf("Recently Read","eda");
        final Shelf shelf2 = new Shelf("To Read","eda");
        shelves.add(shelf);
        shelves.add(shelf2);
        given(shelfRepository.findShelvesByUsername("eda")).willReturn(shelves);
        final List<Shelf> expected = shelfService.findShelvesByUsername("eda");
        assertEquals(shelves,expected);
    }

    @Test
    public void shouldDeleteShelfWithGivenShelf() throws NotFoundException {
        final Long id = Long.valueOf(5);
        final Shelf shelf = new Shelf(id,"Recently Read","eda", new ArrayList<Tag>());
        given(shelfRepository.findShelfById(id)).willReturn(shelf);
        shelfService.deleteShelf(shelf);
        verify(shelfRepository,times(1)).delete(any(Shelf.class));
    }


    @Test
    public void shouldFailToDeleteShelfIfNoShelfExistsWithGivenShelf() {
        final Long id = Long.valueOf(5);
        final Shelf shelf = new Shelf(id,"Recently Read","eda", new ArrayList<Tag>());
        when(shelfRepository.findShelfById(id)).thenReturn(null);
        assertThrows(NotFoundException.class,()->{
            shelfService.deleteShelf(shelf);
        });
    }

    @Test
    public void shouldGetBooksWithGivenShelfId() throws ShelfNotFound, NotFoundException {
        List<ShelfsBook> books = new ArrayList<>();
        final Long id = Long.valueOf(5);
        final Shelf shelf = new Shelf(id,"Recently Read","eda", new ArrayList<Tag>());
        ShelfsBook shelfsBook = new ShelfsBook("abcdef2121", "Sefiller", shelf);
        books.add(shelfsBook);
        shelf.setShelfsBooks(books);
        given(shelfRepository.findShelfById(id)).willReturn(shelf);
        //given(shelfRepository.findShelfById(id).getShelfsBooks()).willReturn(books);
        final List<ShelfsBook> expected = shelfService.getBooks(id);
        assertNotNull(expected);
        assertEquals(books,expected);
    }



}
