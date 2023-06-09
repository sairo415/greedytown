package com.greedytown.domain.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.greedytown.domain.item.model.MoneyLog;
import com.greedytown.domain.social.model.Stat;
import com.greedytown.domain.social.repository.MoneyLogRepository;
import com.greedytown.domain.social.repository.StatRepository;
import com.greedytown.domain.user.dto.*;
import com.greedytown.domain.user.model.User;
import com.greedytown.domain.user.repository.UserRepository;
import com.greedytown.global.config.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final RedisTemplate redisTemplate;

    private final StatRepository statRepository;

    private final MoneyLogRepository moneyLogRepository;

    @Override
    @Transactional
    public String insertUser(UserDto userDto) {
        User registUser = null;
        String message = "";
        userDto.setUserPassword(bCryptPasswordEncoder.encode(userDto.getUserPassword()));
        User user = User.builder()
                .userNickname(userDto.getUserNickname())
                .userEmail(userDto.getUserEmail())
                .userPassword(userDto.getUserPassword())
                .userMoney(0L)
                .userJoinDate(new Date())
                .build();
        try {
            registUser = userRepository.save(user);
        } catch (Exception e) {
            message = "회원가입 실패";
            log.error("회원가입 실패");
            return message;
        }
        try {
            Stat stat = new Stat();
            stat.setUserSeq(registUser);
            statRepository.save(stat);

        } catch (Exception e){
            message = "스탯 실패";
            log.error("스탯 실패");
            return message;
        }
        message = "다 성공";

        return message;
    }



    @Override
    public boolean duplicatedEmail(String userEmail) {
        User user = userRepository.findByUserEmail(userEmail);
        return user != null;
    }

    @Override
    public boolean duplicatedNickname(String userNickname) {
        User user = userRepository.findByUserNickname(userNickname);
        return user != null;
    }

    @Override
    public Map<String, String> reissue(TokenDto tokenDto) {
        Map<String, String> response = new HashMap<>();
        DecodedJWT refreshJwt = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(tokenDto.getRefreshToken());
        Date expiration = refreshJwt.getExpiresAt();
        long now = new Date().getTime();
        if(expiration.getTime() - now < 0) { // 만료되었으면
            response.put("message", "리프레시 토큰 만료");
        }
        // 레디스에서 리프레시 토큰 찾기
        User user = userRepository.findByUserEmail(tokenDto.getUserEmail());
        String refreshToken = "";
        try {
            refreshToken = (String)redisTemplate.opsForValue().get("RT:" + tokenDto.getUserEmail());
        } catch (NullPointerException n) { // 로그아웃해서 레디스에 리프레시 토큰이 없으면
            response.put("message", "Refresh Token이 유효하지 않습니다.");
            return response;
        }
        // 레디스에 저장된 리프레시 토큰과 일치하지 않으면
        if(!refreshToken.equals(tokenDto.getRefreshToken())) {
            response.put("message", "Refresh Token 정보가 일치하지 않습니다.");
            return response;
        }
        // access token 재발급
        String accessToken = JWT.create()
                .withSubject(user.getUserEmail())
                .withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.ACCESS_EXPIRATION_TIME))
                .withClaim("id", user.getUserSeq())
                .withClaim("username", user.getUserEmail())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
        response.put("message", "success");
        response.put("accessToken", JwtProperties.TOKEN_PREFIX+accessToken);
        return response;
    }

    @Override
    @Transactional
    public Map<String, String> logout(User user, String accessToken) {
        Map<String, String> response = new HashMap<>();

        String userEmail = user.getUserEmail();
        accessToken = accessToken.replace(JwtProperties.TOKEN_PREFIX, "");

        try {
            // Redis에서 User email로 저장된 Refresh Token이 있는지 확인 후 있을면 삭제한다.
            if (null != redisTemplate.opsForValue().get("RT:"+userEmail)){
                // Refresh Token을 삭제
                redisTemplate.delete("RT:"+userEmail);
            }

            // 해당 Access Token 유효시간을 가지고 와서 BlackList에 저장하기
            DecodedJWT accessJwt = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(accessToken);
            long expirationAt = accessJwt.getExpiresAt().getTime();
            long now = new Date().getTime();

            long expiration = expirationAt - now;
            redisTemplate.opsForValue().set(accessToken,"logout", expiration, TimeUnit.MILLISECONDS);
            response.put("message", "success");
            return response;
        } catch (Exception e) {
            response.put("message", "fail");
            return response;
        }
    }

    @Override
    public StatDto updateStat(User user, StatDto statDto) {

        System.out.println(statDto.getUserClearTime());
        Stat best = statRepository.findById(user.getUserSeq()).get();
        if(null != best.getUserClearTime()) {
            int now = statDto.getUserClearTime().getMinutes()*60 + statDto.getUserClearTime().getSeconds();
            int before = best.getUserClearTime().getMinutes()*60 + best.getUserClearTime().getSeconds();
            System.out.println(now);
            System.out.println(before);
            if(now > before) {
                Stat stat = new Stat();
                stat.setUserClearTime(statDto.getUserClearTime());
                stat.setStatSeq(user.getUserSeq());
                statRepository.save(stat);
            }
        } else {
            Stat stat = new Stat();
            stat.setUserClearTime(statDto.getUserClearTime());
            stat.setStatSeq(user.getUserSeq());
            statRepository.save(stat);
        }
        return statDto;
    }

    @Override
    public UserInfoDto getUserInfo(User user) {
        UserInfoDto userInfoDto = UserInfoDto.builder()
                .userNickname(user.getUserNickname())
                .userMoney(user.getUserMoney())
                .userJoinDate(user.getUserJoinDate())
                .build();
        return userInfoDto;
    }

    @Override
    @Transactional
    public Map<String, String> earnMoney(User user, EarnMoneyDto earnMoneyDto) {
        Map<String, String> response = new HashMap<>();
        try {
            System.out.println("moneyLog 세팅 전");
            // 현금 흐름 테이블에 insert
            MoneyLog moneyLog = MoneyLog.builder()
                    .moneyLogTime(new Date())
                    .moneyLogGameinfo(earnMoneyDto.getGameInfo())
                    .moneyLogMoney(earnMoneyDto.getMoney())
                    .userSeq(user)
                    .build();
            System.out.println("현금 흐름 테이블 insert 전");
            moneyLogRepository.save(moneyLog);

            System.out.println("user money 세팅 전");
            // user 테이블 update
            user.setUserMoney(user.getUserMoney()+earnMoneyDto.getMoney());
            System.out.println("user money update 전");
            userRepository.save(user);

            response.put("message", "success");
            response.put("money", user.getUserMoney().toString());
            return response;
        } catch (Exception e) {
            response.put("message", "fail");
            return response;
        }
    }
}
