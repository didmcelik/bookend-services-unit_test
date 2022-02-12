package com.bookend.messageservice.serviceTest;

import com.bookend.messageservice.exception.MandatoryFieldException;
import com.bookend.messageservice.exception.MessageNotFound;
import com.bookend.messageservice.exception.UserNotFound;
import com.bookend.messageservice.model.Message;
import com.bookend.messageservice.repository.MessageRepository;
import com.bookend.messageservice.service.MessageServiceImp;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private List<Message> messagesFromSender;
    @Mock
    private List<Message> messagesFromReceiver;
    @InjectMocks
    private MessageServiceImp messageService;
    @Test
    public void shouldGetMessageWithGivenIdSuccesfully() throws MessageNotFound {
        final String id = "ajsdhj23e";
        final Message message = new Message("ajsdhj23e","huri", "didem", "Anna Karenina", "Did you read?", new Date());
        given(messageRepository.findMessageById(id)).willReturn(message);
        final Message expected = messageService.getById(id);
        assertNotNull(expected);
        assertEquals(message,expected);
    }

    @Test
    public void failToGetMessageIfIdDoesNotMatch() throws MessageNotFound {
        final String id = "ajsdhj23e";
        given(messageRepository.findMessageById(id)).willReturn(null);
        assertThrows(MessageNotFound.class,()->{
            messageService.getById(id);
        });
    }

    @Test
    public void shouldGetMessagesByReceiverIfReceiverMatchAnyReceiver() throws MessageNotFound{
        final String receiver = "didem";
        final List<Message> msgList = new ArrayList<>();
        msgList.add(new Message("ajsdhj23e","huri","didem","hello","how are you",new Date()));
        msgList.add(new Message("ajsdhj232","huri","didem","hello","how are you",new Date()));
        given(messageRepository.findMessageByReceiver(receiver)).willReturn(msgList);
        final List<Message> expected = messageService.findMessageByReceiver(receiver);
        assertNotNull(expected);
        assertEquals(msgList,expected);
    }
    @Test
    public void failToGetMessageByReceiverIfReceiverDoesNotMatchAnyReceiver() throws MessageNotFound {
        final String receiver = "huri";
        given(messageRepository.findMessageByReceiver(receiver)).willReturn(null);
        assertThrows(MessageNotFound.class,()->{
            messageService.findMessageByReceiver(receiver);
        });
    }

    @Test
    public void shouldGetMessagesBySenderIfSenderMatchAnySender() throws MessageNotFound {
        final String sender = "huri";
        final List<Message> msg = new ArrayList<>();
        msg.add(new Message("huri","didem","hello","how are you",new Date())) ;
        given(messageRepository.findMessageBySender("huri")).willReturn(msg);
        final List<Message> expected = messageService.findMessageBySender(sender);
        assertNotNull(expected);
        assertEquals(msg,expected);
    }

    @Test
    public void failToGetMessageBySenderIfSenderDoesNotMatchAnySender() throws MessageNotFound {
        final String sender = "huri";
        given(messageRepository.findMessageBySender(sender)).willReturn(null);
        assertThrows(MessageNotFound.class,()->{
            messageService.findMessageBySender(sender);
        });
    }

    @Test
    public void failToSaveMessageIfSenderIsEmpty() {

        Message msg = new Message(null,"didem","hello","how are you",new Date());
        assertThrows(MandatoryFieldException.class,() -> {
            messageService.saveOrUpdate(msg);
        });
        verify(messageRepository, never()).save(any(Message.class));

    }

    @Test
    public void failToSaveMessageIfReceiverIsEmpty() {

        Message msg = new Message("huri",null,"hello","how are you",new Date());
        assertThrows(MandatoryFieldException.class,() -> {
            messageService.saveOrUpdate(msg);
        });
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    public void failToSaveMessageIfSubjectIsEmpty() {
        Message msg = new Message("huri","didem",null,"how are you",new Date());
        assertThrows(MandatoryFieldException.class,() -> {
            messageService.saveOrUpdate(msg);
        });
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    public void failToSaveMessageIfTextIsEmpty() {
        Message msg = new Message("huri","didem","hi",null,new Date());
        assertThrows(MandatoryFieldException.class,() -> {
            messageService.saveOrUpdate(msg);
        });
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    public void failToSaveMessageIfDateIsEmpty() {
        Message msg = new Message("huri","didem","hi","how are you",null);
        assertThrows(MandatoryFieldException.class,() -> {
            messageService.saveOrUpdate(msg);
        });
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    public void shouldSaveMessageSuccessfully() throws MandatoryFieldException {
        Message msg = new Message("huri","didem","hi","how are you",new Date());
        given(messageRepository.save(msg)).willReturn(msg);
        final Message saved = messageService.saveOrUpdate(msg);
        assertThat(saved).isNotNull();
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    public void shouldDeleteMessageSuccessfully() throws UserNotFound, MessageNotFound {
        List<Message> msgList = new ArrayList<>();
        List<Message> msgList2 = new ArrayList<>();
        Message msg = new Message("ajsdhj23e","huri","didem","hi","how are you",new Date());
        Message msg2 = new Message("ajsdhj45","didem","huri","hi","how are you",new Date());
        msgList.add(msg);
        msgList2.add(msg2);
        given(messageRepository.findMessageBySender("huri")).willReturn(msgList);
        given(messageRepository.findMessageByReceiver("huri")).willReturn(msgList2);
        given(messageRepository.findMessageById(msg.getId())).willReturn(msg);
        messageService.deleteMessage(msg,"huri");
        verify(messageRepository,times(1)).delete(msg);
    }

    @Test
    public void failToDeleteMessageIfGivenMessageDoesNotExist() throws UserNotFound, MessageNotFound {
        Message msg = new Message("huri","didem","hi","how are you",new Date());
        given(messageRepository.findMessageBySender("huri")).willReturn(null);
        assertThrows(UserNotFound.class,() -> {
            messageService.deleteMessage(msg,"huri");
        });
    }

    @Test
    public void failToDeleteMessageIfGivenMessageDoesNotExistReceiver() throws UserNotFound, MessageNotFound {
        Message msg = new Message("huri","didem","hi","how are you",new Date());
        given(messageRepository.findMessageByReceiver("huri")).willReturn(null);
        assertThrows(UserNotFound.class,() -> {
            messageService.deleteMessage(msg,"huri");
        });
    }
    @Test
    public void failToDeleteMessageIfMessageDoesNotExist() throws UserNotFound, MessageNotFound {
        Message msg = new Message("sad","huri","didem","hi","how are you",new Date());
        Message msg1 = new Message("ajsdhj23e","huri","didem","hi","how are you",new Date());
        Message msg2 = new Message("ajsdhj45","didem","huri","hi","how are you",new Date());
        List<Message> msg1List = new ArrayList<>();
        List<Message> msg2List = new ArrayList<>();
        msg1List.add(msg1);
        msg2List.add(msg2);
        given(messageRepository.findMessageBySender("huri")).willReturn(msg1List);
        given(messageRepository.findMessageByReceiver("huri")).willReturn(msg2List);
        given(messageRepository.findMessageById(msg.getId())).willReturn(null);
        assertThrows(MessageNotFound.class,() -> {
            messageService.deleteMessage(msg,"huri");
        });
    }


    @Test
    public void findChatByUserName() throws MessageNotFound {
        List<Message> msg1 = new ArrayList<>();
        msg1.add(new Message("huri","didem","hi","how are you",new Date()));
        List<Message> msg2 = new ArrayList<>();
        msg2.add( new Message("didem","huri","hi","Fine",new Date()));
        List<Message> chat = new ArrayList<>();
        for(Message msg:msg1){
            if(msg.getReceiver().equalsIgnoreCase("didem")){
                chat.add(msg);
            }
        }
        for(Message msg:msg2){
            if(msg.getReceiver().equalsIgnoreCase("huri")){
                chat.add(msg);
            }
        }

        given(messageRepository.findMessageBySender("huri")).willReturn(msg1);
        given(messageRepository.findMessageBySender("didem")).willReturn(msg2);
        final List<Message> expected = messageService.findChatByUserName("huri", "didem");
        Collections.sort(chat, (o1, o2) -> o1.getSendDate().compareTo(o2.getSendDate()));
        Collections.reverse(chat);
        assertNotNull(expected);
        assertEquals(chat,expected);


    }

}
