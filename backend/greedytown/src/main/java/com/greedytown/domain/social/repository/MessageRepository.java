package com.greedytown.domain.social.repository;

import com.greedytown.domain.social.model.FriendUserList;
import com.greedytown.domain.social.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByMessageTo_UserSeq(Long userSeq);

    Long deleteAllByMessageTo_UserSeq(Long userSeq);

    Long countAllByMessageTo_UserSeqAndMessageCheckFalse(Long userSeq);

}
