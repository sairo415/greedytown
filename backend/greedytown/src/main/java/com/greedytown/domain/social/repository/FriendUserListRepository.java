package com.greedytown.domain.social.repository;

import com.greedytown.domain.social.model.FriendUserList;
import com.greedytown.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendUserListRepository extends JpaRepository<FriendUserList, Long> {

    Boolean existsByFriendFrom_UserSeqAndFriendTo_UserSeqAndFriendAcceptIsTrue(Long friendFrom, Long friendTo);

    Boolean existsByFriendTo_UserSeqAndFriendFrom_UserSeqAndFriendAcceptIsTrue(Long friendTo, Long friendFrom);

    //요청 보기
    List<FriendUserList>findAllByFriendTo_UserSeqAndFriendAcceptIsFalse(Long friendTo);

    //내 친구 목록 보기
    List<FriendUserList> findAllByFriendTo_UserSeqAndFriendAcceptIsTrue(Long friendFrom);
    List<FriendUserList> findAllByFriendFrom_UserSeqAndFriendAcceptIsTrue(Long friendFrom);
    

    Integer deleteByFriendFrom_userSeqAndFriendTo_userSeq(Long fromFriend,Long toFriend);
    Integer deleteByFriendTo_userSeqAndFriendFrom_userSeq(Long fromFriend,Long toFriend);



}
