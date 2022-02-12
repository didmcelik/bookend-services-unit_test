package com.bookend.bookservice.serviceTest;

import com.bookend.bookservice.exception.AlreadyExist;
import com.bookend.bookservice.exception.MandatoryFieldException;
import com.bookend.bookservice.exception.NotFoundException;
import com.bookend.bookservice.kafka.Producer;
import com.bookend.bookservice.model.Genre;
import com.bookend.bookservice.repository.GenreRepository;
import com.bookend.bookservice.service.GenreServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class GenreServiceTest {
    @Spy
    private GenreServiceImpl genreServiceSpy;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private Producer producer;
    @InjectMocks
    private GenreServiceImpl genreService;

    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldSaveNewGenreIfGenreDoesNotExistAlready() throws AlreadyExist, MandatoryFieldException {
        final Genre genre = new Genre("5asd25dfgf","Journal");
        //doReturn(null).when(genreServiceSpy).findByGenre(genre.getGenre());
        when(genreRepository.findByGenre(genre.getGenre())).thenReturn(null);
        when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        final Genre expected = genreService.addNewGenre(genre.getGenre());
        assertEquals(expected,genre);
        verify(genreRepository).save(any(Genre.class));
        verify(producer).publishGenre(any()); //New line added.
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToSaveNewGenreIfGenreAlreadyExist(){
        final Genre genre = new Genre("5asd25dfgf","Journal");
        //doReturn(genre).when(genreServiceSpy).findByGenre(genre.getGenre());
        when(genreRepository.findByGenre(genre.getGenre())).thenReturn(genre);
        assertThrows(AlreadyExist.class,()->{
            genreService.addNewGenre(genre.getGenre());
        });
        verify(genreRepository,never()).save(any(Genre.class));
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToSAveNewGenreIfGenreFieldEmptyString(){
        assertThrows(MandatoryFieldException.class,()->{
            genreService.addNewGenre("");
        });
        verify(genreRepository,never()).save(any(Genre.class));
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToSAveNewGenreIfGenreFieldNull(){
        assertThrows(MandatoryFieldException.class,()->{
            genreService.addNewGenre(null);
        });
        verify(genreRepository,never()).save(any(Genre.class));
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFindGenreIfGenreExistWithGivenID() throws NotFoundException {
        final Genre genre = new Genre("5asd25dfgf","Journal");
        when(genreRepository.findGenreById(genre.getId())).thenReturn(genre);
        final Genre expected = genreService.findById(genre.getId());
        assertThat(expected).isNotNull();
        assertEquals(expected,genre);

    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToFindGenreIfNoGenreExistWithGivenID(){
        final Genre genre = new Genre("5asd25dfgf","Journal");
        when(genreRepository.findGenreById(genre.getId())).thenReturn(null);
        assertThrows(NotFoundException.class,()->{
            genreService.findById(genre.getId());
        });
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldUpdateGenre() throws NotFoundException, AlreadyExist, MandatoryFieldException {
        final Genre oldGenre = new Genre("5asd25dfgf","poem");
        final Genre newGenre = new Genre("5asd25dfgf","Poem");
        //doReturn(oldGenre).when(genreServiceSpy).findById(oldGenre.getId());
        //doReturn(null).when(genreServiceSpy).findByGenre(newGenre.getGenre());
        when(genreRepository.findByGenre(newGenre.getGenre())).thenReturn(null);
       // when(genreRepository.findGenreById(oldGenre.getId())).thenReturn(oldGenre);
        when(genreRepository.save(any())).thenReturn(newGenre);
        final Genre expected = genreService.update(newGenre);
        assertThat(expected).isNotNull();
        assertEquals(expected.getGenre().toLowerCase(),newGenre.getGenre().toLowerCase());
        verify(producer).publishGenre(any()); // New line added.
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToUpdateGenreIfGenreNAmeAlreadyOccupied(){
        final Genre newGenre = new Genre("5asd25dfgf","Poem");
        //doReturn(newGenre).when(genreServiceSpy).findByGenre(newGenre.getGenre());
        when(genreRepository.findByGenre(newGenre.getGenre())).thenReturn(newGenre);
        assertThrows(AlreadyExist.class,()->{
            genreService.update(newGenre);
        });
        verify(genreRepository,never()).save(any(Genre.class));

    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToUpdateGenreIfGenreNAmeEmptyString() throws NotFoundException {
        final Genre oldGenre = new Genre("5asd25dfgf","poem");
        final Genre newGenre = new Genre("5asd25dfgf","");
        //doReturn(oldGenre).when(genreServiceSpy).findById(oldGenre.getId());
        //doReturn(null).when(genreServiceSpy).findByGenre(newGenre.getGenre());
        when(genreRepository.findByGenre(newGenre.getGenre())).thenReturn(null);
        //when(genreRepository.findGenreById(oldGenre.getId())).thenReturn(oldGenre);
        assertThrows(MandatoryFieldException.class,()->{
            genreService.update(newGenre);
        });
        verify(genreRepository,never()).save(any(Genre.class));
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToUpdateGenreIfGenreNAmeNull() throws NotFoundException {
        final Genre oldGenre = new Genre("5asd25dfgf","poem");
        final Genre newGenre = new Genre("5asd25dfgf",null);
        //doReturn(oldGenre).when(genreServiceSpy).findById(oldGenre.getId());
        //doReturn(null).when(genreServiceSpy).findByGenre(newGenre.getGenre());
        when(genreRepository.findByGenre(newGenre.getGenre())).thenReturn(null);
        //when(genreRepository.findGenreById(oldGenre.getId())).thenReturn(oldGenre);
        assertThrows(MandatoryFieldException.class,()->{
            genreService.update(newGenre);
        });
        verify(genreRepository,never()).save(any(Genre.class));
    }

/*
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToUpdateGenreIfGenreDoesNotExist() throws NotFoundException {
        final Genre genre = new Genre("5asd25dfgf","poem");
        doReturn(null).when(genreServiceSpy).findById(genre.getId());
        assertThrows(NotFoundException.class,()->{
            genreService.update(genre);
        });
        verify(genreRepository,never()).save(any(Genre.class));

    }*/

/*
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFailToUpdateGenreIfGenreDoesNotExists(){
        final Genre oldGenre = new Genre("5asd25dfgf","poem");

        try {
            doThrow(NotFoundException.class).when(genreServiceSpy).findById(oldGenre.getId());
        } catch (NotFoundException e) {
            assertTrue(e instanceof NotFoundException);
            assertEquals(e.getMessage(),"No genre is match with given id");
        }
        verify(genreRepository,never()).save(any(Genre.class));

    }*/

    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldReturnAllGenres(){
        final Genre genre1 = new Genre("5asd25dfgf","Journal");
        final Genre genre2 = new Genre("5asd24dfgf","Science-Fiction");
        final Genre genre3 = new Genre("5asd26dfgf","Fiction");
        final Genre genre4 = new Genre("5asd27dfgf","Classics");
        final List<Genre> genres = Arrays.asList(genre1,genre2,genre3,genre4);
        when(genreRepository.findAll()).thenReturn(genres);
        final List<Genre> expected = genreService.findAll();
        assertThat(expected).isNotNull();
        assertEquals(expected,genres);
    }
    @MockitoSettings(strictness = Strictness.WARN)
    @Test
    public void shouldFindGenreWithGivenGenreField(){
        final Genre genre = new Genre("5asd25dfgf","Journal");
        when(genreRepository.findByGenre(genre.getGenre())).thenReturn(genre);
        final Genre expected = genreService.findByGenre(genre.getGenre());
        assertThat(expected).isNotNull();
        assertEquals(genre,expected);
    }
}
