package com.bookend.bookclubservice.serviceTest;


import com.bookend.bookclubservice.expection.ClubAlreadyExistException;
import com.bookend.bookclubservice.expection.NotMemberException;
import com.bookend.bookclubservice.kafka.MessageProducer;
import com.bookend.bookclubservice.model.Club;
import com.bookend.bookclubservice.model.Invitation;
import com.bookend.bookclubservice.model.Member;
import com.bookend.bookclubservice.model.Post;
import com.bookend.bookclubservice.payload.request.*;
import com.bookend.bookclubservice.repository.ClubRepository;
import com.bookend.bookclubservice.repository.InvitationRepository;
import com.bookend.bookclubservice.repository.MemberRepository;
import com.bookend.bookclubservice.repository.PostRepository;
import com.bookend.bookclubservice.service.ClubService;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class clubServiceTest {
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private MessageProducer messageProducer;
    @Mock
    private InvitationRepository invitationRepository;
    @InjectMocks
    private ClubService clubService;

    @Test
    public void shouldGetMyClubsWithUsername(){
        Member member = new Member((long)1,"testUser");
        given(memberRepository.findByUserName(any(String.class))).willReturn(member);
        Club club = new Club((long) 1,"test","test",member,true);
        List<Club> clubs = new ArrayList<>();
        clubs.add(club);
        given(clubRepository.findByOwner(member)).willReturn(clubs);
        final List<Club> expected = clubService.getMyClubs("testUser");
        assertThat(expected).isNotNull();
        assertEquals(expected,clubs);
    }
    @Test
    public void shouldNotGetMyClubsIfUsernameDoesNotExist(){
        given(memberRepository.findByUserName(any(String.class))).willReturn(null);

        assertThrows(IllegalArgumentException.class,()->{
            clubService.getMyClubs("testUser");
        });
    }
    @Test
    public void shouldGetMyInvitationsWithUsername(){
        Member member = new Member((long)1,"testUser");
        given(memberRepository.findByUserName(any(String.class))).willReturn(member);
        Club club = new Club((long) 1,"test","test",member,true);
        Invitation invitation = new Invitation((long) 1,club,member);
        List<Invitation> invitations = new ArrayList<>();
        invitations.add(invitation);
        given(invitationRepository.findInvitationsByInvitedPerson(member)).willReturn(invitations);
        final List<Invitation> expected = clubService.getMemberInvitations("testUser");
        assertThat(expected).isNotNull();
        assertEquals(expected,invitations);
    }
    @Test
    public void shouldNotGetMyInvitationsIfUsernameDoesNotExist(){
        given(memberRepository.findByUserName(any(String.class))).willReturn(null);
        assertThrows(IllegalArgumentException.class,()->{
            clubService.getMemberInvitations("testUser");
        });
    }
    @Test
    public void shouldGetWriterPostsWithUsername(){
        Member member = new Member((long)1,"testUser");
        given(memberRepository.findByUserName(any(String.class))).willReturn(member);
        Post post = new Post();
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        given(postRepository.findByWriter(member)).willReturn(posts);
        final List<Post> expected = clubService.getWriterPosts("testUser");
        assertThat(expected).isNotNull();
        assertEquals(expected,posts);
    }
    @Test
    public void shouldNotGetWriterPostsIfUsernameDoesNotExist(){
        given(memberRepository.findByUserName(any(String.class))).willReturn(null);
        assertThrows(IllegalArgumentException.class,()->{
            clubService.getWriterPosts("testUser");
        });
    }

    @Test
    public void shouldNotSaveClubIfOwnerDoesNotExistWithGivenUserName(){
        given(memberRepository.findByUserName(any(String.class))).willReturn(null);
        NewClubRequest newClubRequest = new NewClubRequest("clubname","desc","username",true);
        assertThrows(IllegalArgumentException.class,()->{
            clubService.saveClub(newClubRequest);
        });
    }
    @Test
    public void shouldNotSaveClubIfClubExistsWithGivenClubName(){
        Member member = new Member((long)1,"testUser");
        given(memberRepository.findByUserName(any(String.class))).willReturn(member);
        NewClubRequest newClubRequest = new NewClubRequest("clubname","desc","username",true);
        List<Club> clubs = new ArrayList<>();
        Club club = new Club((long) 1,"clubname","test",member,true);
        clubs.add(club);
        given(clubRepository.findAll()).willReturn(clubs);
        assertThrows(ClubAlreadyExistException.class,()->{
            clubService.saveClub(newClubRequest);
        });
    }
    @Test
    public void shouldSaveClub(){
        Member member = new Member((long)1,"testUser");
        given(memberRepository.findByUserName(any(String.class))).willReturn(member);
        NewClubRequest newClubRequest = new NewClubRequest("clubname","desc","username",true);
        List<Club> clubs = new ArrayList<>();
        Club club = new Club((long) 1,"clubname2","test",member,true);
        clubs.add(club);
        given(clubRepository.findAll()).willReturn(clubs);
        given(clubRepository.save(any(Club.class))).willReturn(club);
        Club saved =clubService.saveClub(newClubRequest);
        assertThat(saved).isNotNull();
        verify(clubRepository).save(any(Club.class));
    }
    @Test
    public void shouldNotSaveNewMemberToClubIfUserNotExists(){
        given(memberRepository.findByUserName(any(String.class))).willReturn(null);
        NewClubMemberRequest newClubMemberRequest = new NewClubMemberRequest((long)1,(long)2);
        boolean result = clubService.newMember(newClubMemberRequest,"user2");
        assertThat(result).isEqualTo(false);
        verify(clubRepository, never()).save(any(Club.class));
        verify(memberRepository, never()).save(any(Member.class));

    }
    @Test
    public void shouldNotSaveNewMemberToClubIfUserIsOwnerOfClub(){
        Member member = new Member((long)2,"owner");
        Club club = new Club((long)1," "," ",member,true);
        given(clubRepository.findById((long)1)).willReturn(java.util.Optional.of(club));
        given(memberRepository.findByUserName("owner")).willReturn(member);

        NewClubMemberRequest  newClubMemberRequest = new NewClubMemberRequest((long)1,(long)2);
        boolean result = clubService.newMember(newClubMemberRequest,"owner");
        assertThat(result).isEqualTo(false);
        verify(clubRepository, never()).save(any(Club.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    public void shouldNotSaveNewMemberToClubIfUserAllreadMemberOfClub(){
        Member member = new Member((long)2,"owner");
        Member member2 = new Member((long)2,"member");
        Club club = new Club((long)1," "," ",member,true);
        club.getMembers().add(member2);
        given(clubRepository.findById((long)1)).willReturn(java.util.Optional.of(club));
        given(memberRepository.findByUserName("member")).willReturn(member2);
        NewClubMemberRequest  newClubMemberRequest = new NewClubMemberRequest((long)1,(long)2);
        boolean result = clubService.newMember(newClubMemberRequest,"member");
        assertThat(result).isEqualTo(false);
        verify(clubRepository, never()).save(any(Club.class));
        verify(memberRepository, never()).save(any(Member.class));
    }
    @Test
    public void shouldSaveNewMemberToClub(){
        Member member = new Member((long)2,"owner");
        Member member2 = new Member((long)2,"member");
        Member member3 = new Member((long)2,"member2");

        Club club = new Club((long)1," "," ",member,true);
        club.getMembers().add(member2);
        given(clubRepository.findById((long)1)).willReturn(java.util.Optional.of(club));
        given(memberRepository.findByUserName("member2")).willReturn(member3);
        NewClubMemberRequest  newClubMemberRequest = new NewClubMemberRequest((long)1,(long)2);
        boolean result = clubService.newMember(newClubMemberRequest,"member2");
        assertThat(result).isEqualTo(true);
        verify(clubRepository).save(any(Club.class));
        verify(memberRepository).save(any(Member.class));
    }
    @Test
    public void shouldNotSendInvitationifUserNotExist(){
        given(memberRepository.findByUserName(any(String.class))).willReturn(null);
        InvitationRequest invitationRequest = new InvitationRequest((long)1,"invitation");
        assertThrows(IllegalArgumentException.class,()->{
            clubService.invitePerson(invitationRequest);
        });
    }
    @Test
    public void shouldNotSendInvitationifUserAllreadyInvited(){
        InvitationRequest  invitationRequest = new InvitationRequest((long)1,"invitationMem");
        Member member = new Member((long)1,"invitationMem");
        Club club = new Club((long)1,"","",member,true);
        Invitation invitation = new Invitation((long)1,club,member);
        given(memberRepository.findByUserName("invitationMem")).willReturn(member);

        given(clubRepository.findById((long)1)).willReturn(java.util.Optional.of(club));
        given(invitationRepository.findByClubAndInvitedPerson(club,member)).willReturn(invitation);
        assertThrows(IllegalArgumentException.class,()->{
            clubService.invitePerson(invitationRequest);
        });
    }
    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSendInvitation(){
        InvitationRequest  invitationRequest = new InvitationRequest((long)1,"invitationMem"); ///???
        Member member = new Member((long)1,"invitationMem");
        //Member invite_member = new Member((long)2,"invite");//--------????
        Club club = new Club((long)1,"","",member,true);
        Invitation invitation = new Invitation((long)1,club,member);
        given(memberRepository.findByUserName("invitationMem")).willReturn(member);
        //given(memberRepository.findByUserName("invite")).willReturn(invite_member);//?????????

        given(clubRepository.findById((long)1)).willReturn(java.util.Optional.of(club));
        given(invitationRepository.findByClubAndInvitedPerson(club,member)).willReturn(null);
        //given(invitationRepository.save(any(Invitation.class))).willReturn(invitation);
        Invitation expected =  clubService.invitePerson(invitationRequest);
        assertThat(expected).isNotNull();
        verify(invitationRepository).save(any(Invitation.class));
    }
    @Test
    public void shouldNotReplyInvitationIfInvitationNotExistsWithGivenId(){
        InvitationReply invitationReply = new InvitationReply((long)1, EInvitationReply.ACCEPT);
        given(invitationRepository.findById(any(Long.class))).willReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,()->{
            clubService.replyInvitation(invitationReply);
        });
    }
    @Test
    public void shouldReplyInvitation(){
        InvitationReply invitationReply = new InvitationReply((long)1, EInvitationReply.ACCEPT);
        Member member = new Member((long)1,"invitationMem");

        Club club = new Club((long)1,"","",member,true);
        Invitation invitation = new Invitation((long)1,club,member);

        given(invitationRepository.findById((long)1)).willReturn(java.util.Optional.of(invitation));
        given(clubRepository.save(any(Club.class))).willReturn(club);
        given(memberRepository.save(any(Member.class))).willReturn(member);

        clubService.replyInvitation(invitationReply);
    }
    @Test
    public void shouldNotSavePostIfClubDoesNotExistWithGivenId(){
        NewPostRequest newPostRequest = new NewPostRequest("title","text",(long)1);
        given(clubRepository.findById((long)1)).willReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,()->{
            clubService.savePost(newPostRequest, "member");
        });
    }
    @Test
    public void shouldNotSavePostIfWriterDoesNotExistWithGivenId(){
        NewPostRequest newPostRequest = new NewPostRequest("title","text",(long)1);
        Member member = new Member((long)1,"member");
        Club club = new Club((long)1,"","",member,true);
        given(memberRepository.findByUserName("member")).willReturn(null);
        given(clubRepository.findById((long)1)).willReturn(Optional.of(club));
        assertThrows(IllegalArgumentException.class,()->{
            clubService.savePost(newPostRequest, "member");
        });
    }
    @Test
    public void shouldNotSavePostIfMemberDoesNotHaveMembershiptToTheClub(){
        NewPostRequest newPostRequest = new NewPostRequest("title","text",(long)1);
        Member member = new Member((long)1,"member");
        Member member2 = new Member((long)2,"notmember");
        Club club = new Club((long)1,"","",member,true);
        given(memberRepository.findByUserName("member")).willReturn(member2);
        given(clubRepository.findById((long)1)).willReturn(Optional.of(club));
        assertThrows(NotMemberException.class,()->{
            clubService.savePost(newPostRequest, "member");
        });
    }
    @Test
    public void shouldSavePost() throws NotMemberException {
        NewPostRequest newPostRequest = new NewPostRequest("title","text",(long)1);
        Member member = new Member((long)1,"member");
        Club club = new Club((long)1,"","",member,true);
        Post post = new Post();
        given(memberRepository.findByUserName("member")).willReturn(member);
        given(clubRepository.findById((long)1)).willReturn(Optional.of(club));
        given(postRepository.save(any(Post.class))).willReturn(post);

        Post expected = clubService.savePost(newPostRequest,"member");
        assertThat(expected).isNotNull();
        verify(postRepository).save(any(Post.class));
    }
    @Test
    public void shouldNotFindPostWithIdIfPostNotExistWithId(){
        given(postRepository.findPostById(any(Long.class))).willReturn(null);
        assertThrows(IllegalArgumentException.class,()->{
            clubService.findPostByID((long)1);
        });
    }
    @Test
    public void shouldFindPostWithId(){
        Post post = new Post();
        given(postRepository.findPostById(any(Long.class))).willReturn(post);
        Post expected = clubService.findPostByID((long)1);
        assertThat(expected).isNotNull();
    }
    @Test
    public void shouldNotFindClubWithIdIfPostNotExistWithId(){
        given(clubRepository.findClubById(any(Long.class))).willReturn(null);
        assertThrows(IllegalArgumentException.class,()->{
            clubService.findByID((long)1);
        });
    }
    @Test
    public void shouldFindClubWithId(){
        Club club = new Club();
        given(clubRepository.findClubById(any(Long.class))).willReturn(club);
        Club expected = clubService.findByID((long)1);
        assertThat(expected).isNotNull();
    }
    @Test
    public void shouldFindClubPostsWithClubId(){
        List<Post> posts = new ArrayList<>();
        given(postRepository.findByClubId(any(Long.class))).willReturn(posts);
        List<Post> expected = clubService.getClubPosts((long)1);
        assertThat(expected).isNotNull();
    }
    @Test
    public void shouldFindAllClubs(){
        Member member = new Member((long)1,"member");
        Club club = new Club((long)1,"Klasik Okurlar","desc",member,false);
        List<Club> clubs = new ArrayList<Club>();
        clubs.add(club);
        given(clubRepository.findAll()).willReturn(clubs);
        List<Club> expected = clubService.getAll();
        assertThat(expected).isNotNull();
        assertThat(expected).isNotEmpty();
    }
    @Test
    public void shouldNotFindMembershipClubsWithUserNameIfUserNotExistWithThisId(){
        given(memberRepository.findByUserName(any(String.class))).willReturn(null);
        assertThrows(IllegalArgumentException.class,()->{
            clubService.getMembershipClub("member");
        });
    }
    @Test
    public void shouldFindMembershipClubsWithUserName(){
        Member member = new Member((long)1,"member");

        given(memberRepository.findByUserName(any(String.class))).willReturn(member);
        List<Club> expected = clubService.getMembershipClub("member");
        assertThat(expected).isNotNull();

    }

    /*
    @Test
    public void shouldGetAllClubs(){
        Member member = new Member((long)1,"member");
        Club club = new Club((long)1,"Klasik Okurlar","desc",member,false);
        List<Club> clubs = new ArrayList<Club>();
        clubs.add(club);
        when(clubRepository.findAll()).thenReturn(clubs);
        List<Club> expected = clubService.getAll();
        assertNotNull(expected);
    }*/


}
