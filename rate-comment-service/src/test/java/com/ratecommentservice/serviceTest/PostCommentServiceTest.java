package com.ratecommentservice.serviceTest;

import com.ratecommentservice.exception.PostCommentNotFound;
import com.ratecommentservice.model.Book;
import com.ratecommentservice.model.PostComment;
import com.ratecommentservice.repository.PostCommentRepository;
import com.ratecommentservice.service.PostCommentService;
import com.ratecommentservice.service.PostCommentServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PostCommentServiceTest {

    @Mock
    private PostCommentRepository postCommentRepository;
    @InjectMocks
    private PostCommentServiceImpl postCommentService;
    @Mock
    private Collections collections;


    @Test
    public void getPostCommentIfPostIdHaveMatchSuccesfully() throws PostCommentNotFound {
        List<PostComment> comments = new ArrayList<>();
        String id = "5";
        String id1 = "6";
        final PostComment postComment = new PostComment(Long.valueOf(id),"huri","Amazing");
        final PostComment postComment1 = new PostComment(Long.valueOf(id1),"didem","Super");
        comments.add(postComment);
        comments.add(postComment1);
        given(postCommentRepository.findAllByPostIDOrderByDateAsc(Long.valueOf(id))).willReturn(comments);
        final List<PostComment> expected = postCommentService.getCommentsByPostID(Long.valueOf(id));
        assertNotNull(expected);
        assertEquals(comments.get(0).getCommentId(),expected.get(0).getCommentId());
    }

    @Test
    public void failToGetPostCommentIfPostIdDoesNotMatch() throws PostCommentNotFound {
        List<PostComment> comments = new ArrayList<>();
        given(postCommentRepository.findAllByPostIDOrderByDateAsc(Long.valueOf("5"))).willReturn(comments);
        assertThrows(PostCommentNotFound.class,()->{
            postCommentService.getCommentsByPostID(Long.valueOf("5"));
        });
    }

    @Test
    public void shouldSaveGivenPostCommentSuccessfully() throws PostCommentNotFound {
        String id = "5";
        final PostComment postComment = new PostComment(Long.valueOf(id),"huri","Amazing");
        given(postCommentRepository.save(any(PostComment.class))).willReturn(postComment);
        final PostComment saved = postCommentService.commentPost(postComment);
        //assertThat(saved).isNotNull();
        assertNotNull(saved);
        verify(postCommentRepository).save(any(PostComment.class));
    }

    @Test
    public void failToSavePostCommentIfCommentIsNull() throws PostCommentNotFound {
        assertThrows(PostCommentNotFound.class,()->{
            postCommentService.commentPost(null);
        });
        verify(postCommentRepository, never()).save(any(PostComment.class));
    }

}
