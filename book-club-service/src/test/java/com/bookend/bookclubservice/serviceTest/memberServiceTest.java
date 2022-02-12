package com.bookend.bookclubservice.serviceTest;


import com.bookend.bookclubservice.model.Member;
import com.bookend.bookclubservice.repository.MemberRepository;
import com.bookend.bookclubservice.service.MemberService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.runner.RunWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class memberServiceTest {

    @Mock
    MemberRepository memberRepository;
    @InjectMocks
    MemberService memberService;

    @Test
    public void shouldSaveMember(){
        Member member = new Member();
        member.setId((long)1);
        member.setUserName("testdeneme");
        given(memberRepository.save(any(Member.class))).willReturn(member);
        final Member expected = memberService.save(member.getId(),member.getUserName());
        assertThat(expected).isNotNull();
        assertEquals(expected,member);
    }
    @Test
    public void shouldNotSaveWhenIdOrUsernameNull(){
        Member member = new Member();
        member.setId(null);
        member.setUserName("testdeneme");
        Member member2 = new Member();
        member2.setId((long)1);
        member2.setUserName(null);
        assertThrows(IllegalArgumentException.class,()->{
            memberService.save(member.getId(),member.getUserName());
        });
        assertThrows(IllegalArgumentException.class,()->{
            memberService.save(member2.getId(),member2.getUserName());
        });
        verify(memberRepository, never()).save(any(Member.class));
    }
}
