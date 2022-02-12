package com.bookend.shelfservice.serviceTest;

import com.bookend.shelfservice.exception.AlreadyExists;
import com.bookend.shelfservice.exception.ShelfNotFound;
import com.bookend.shelfservice.exception.ShelfsBookNotFound;
import com.bookend.shelfservice.exception.TagNotFound;
import com.bookend.shelfservice.model.Shelf;
import com.bookend.shelfservice.model.Tag;
import com.bookend.shelfservice.payload.GenreMessage;
import com.bookend.shelfservice.repository.ShelfRepository;
import com.bookend.shelfservice.repository.TagRepository;
import com.bookend.shelfservice.service.ShelfServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TagServiceTest {
    @Mock
    private TagRepository tagRepository;
    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    public void getTagIfIdHaveMatchSuccesfully() throws TagNotFound {
        final String id = "abdhsdj21515";
        final Tag tag = new Tag(id,"Horror");
        given(tagRepository.findTagById(id)).willReturn(tag);
        final Tag expected = tagService.findByID(id);
        assertNotNull(expected);
        assertEquals(tag,expected);
    }

    @Test
    public void failToGetShelfIfIdDoesNotMatch() throws TagNotFound {
        final String id = "abdhsdj21515";
        given(tagRepository.findTagById(id)).willReturn(null);
        assertThrows(TagNotFound.class,()->{
            tagService.findByID(id);
        });
    }
    @Test
    public void shouldReturnAllTags(){
        List<Tag> tags = new ArrayList<>();
        final Tag tag = new Tag("dsfdsfd32","Horror");
        final Tag tag2 = new Tag("dsfdaks56","Thriller");
        tags.add(tag);
        tags.add(tag2);
        given(tagRepository.findAll()).willReturn(tags);
        final List<Tag> expected = tagService.allTag();
        assertEquals(tags,expected);
    }
    @Test
    public void shouldUpdateTagWhenTagIDHasMatch() throws AlreadyExists {
        final String id = "dsfdsfd32";
        final String genre = "Horror";
        GenreMessage message = new GenreMessage(id,genre);
        final Tag tag = new Tag(id,"horor");
        given(tagRepository.findTagById(message.getId())).willReturn(tag);
        tag.setTag(genre);
        given(tagRepository.save(any(Tag.class))).willReturn(tag);
        final Tag saved = tagService.save(message);
        assertNotNull(saved);
        verify(tagRepository).save(any(Tag.class));
    }
    @Test
    public void shouldUpdateTagWhenTagIDHasNotMatch() throws AlreadyExists {
        final String id = "dsfdsfd32";
        final String genre = "Horror";
        GenreMessage message = new GenreMessage(id,genre);
        final Tag tag = new Tag(id,"horor");
        given(tagRepository.findTagById(message.getId())).willReturn(null);
        tag.setTag(genre);
        given(tagRepository.save(any(Tag.class))).willReturn(tag);
        final Tag saved = tagService.save(message);
        assertNotNull(saved);
        verify(tagRepository).save(any(Tag.class));
    }
    @Test
    public void failToUpdateTagWhenTagNameAlreadyExists()  {
        final String id = "dsfdsfd32";
        final String genre = "Horror";
        GenreMessage message = new GenreMessage(id,genre);
        final Tag tag = new Tag(id,"horor");
        given(tagRepository.findByTag(message.getGenre())).willReturn(tag);
       assertThrows(AlreadyExists.class,()->{
           tagService.save(message);
       });

        verify(tagRepository,never()).save(any(Tag.class));
    }




}
