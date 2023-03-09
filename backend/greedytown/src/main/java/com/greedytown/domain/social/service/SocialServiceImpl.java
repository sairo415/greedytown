package com.greedytown.domain.social.service;

import com.greedytown.domain.item.dto.BuyItemDto;
import com.greedytown.domain.item.dto.BuyItemReturnDto;
import com.greedytown.domain.item.dto.ItemDto;
import com.greedytown.domain.item.model.Item;
import com.greedytown.domain.item.model.ItemUserList;
import com.greedytown.domain.item.repository.ItemRepository;
import com.greedytown.domain.item.repository.ItemUserListRepository;
import com.greedytown.domain.item.service.ItemService;
import com.greedytown.domain.social.dto.MyFriendDto;
import com.greedytown.domain.social.dto.RankingDto;
import com.greedytown.domain.social.model.FriendUserList;
import com.greedytown.domain.social.model.FriendUserListPK;
import com.greedytown.domain.social.repository.FriendUserListRepository;
import com.greedytown.domain.user.model.User;
import com.greedytown.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private final UserRepository userRepository;
    private final FriendUserListRepository friendUserListRepository;


    //랭킹을 본다.
    @Override
    public List<RankingDto> getUserRanking() {
        List<RankingDto> list = new ArrayList<>();
        for(User user : userRepository.findAllByOrderByUserClearTimeDesc()){
            RankingDto rankingDto = RankingDto.builder().
                                    userNickname(user.getUserNickName()).
                                    clearTime(user.getUserClearTime().toString()).
                                    build();
            list.add(rankingDto);
        }
        return list;
    }

    @Override
    public Void insertFriend(User user, Long friendIndex) {
        FriendUserList friendUserList = new FriendUserList();
        User friend = userRepository.findUserByUserIndex(friendIndex);
        friendUserList.setUserIndexA(user);
        friendUserList.setUserIndexB(friend);
        friendUserListRepository.save(friendUserList);
        return null;
    }

    @Override
    public Boolean isFriend(User user, Long friendIndex) {
        User friend = userRepository.findUserByUserIndex(friendIndex);
        Boolean check = friendUserListRepository.existsByUserIndexA_UserIndexAndUserIndexB_UserIndex(user.getUserIndex(),friend.getUserIndex());
        if(check) return true;
//        user.getUserIndex();
        check = friendUserListRepository.existsByUserIndexB_UserIndexAndUserIndexA_UserIndex(user.getUserIndex(),friend.getUserIndex());
        if(check) return true;
        return false;
    }

    @Override
    public List<MyFriendDto> getMyFriendList(User user) {

        List<MyFriendDto> myFriendDtos = new ArrayList<>();

        for(FriendUserList friendUserList : friendUserListRepository.findAllByUserIndexA_userIndex(user.getUserIndex())){
            User user1 = userRepository.findUserByUserIndex(friendUserList.getUserIndexA().getUserIndex());
            MyFriendDto myFriendDto = MyFriendDto.builder().
                                      userIndex(user1.getUserIndex()).
                                      userNickname(user1.getUserNickName()).
                                      build();
            myFriendDtos.add(myFriendDto);
        }
        for(FriendUserList friendUserList : friendUserListRepository.findAllByUserIndexB_userIndex(user.getUserIndex())){
            User user1 = userRepository.findUserByUserIndex(friendUserList.getUserIndexA().getUserIndex());
            MyFriendDto myFriendDto = MyFriendDto.builder().
                    userIndex(user1.getUserIndex()).
                    userNickname(user1.getUserNickName()).
                    build();
            myFriendDtos.add(myFriendDto);
        }

        return myFriendDtos;

    }
}