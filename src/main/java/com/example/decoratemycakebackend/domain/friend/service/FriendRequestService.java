package com.example.decoratemycakebackend.domain.friend.service;

import com.example.decoratemycakebackend.domain.friend.dto.*;
import com.example.decoratemycakebackend.domain.friend.entity.FriendRequest;
import com.example.decoratemycakebackend.domain.friend.entity.FriendRequestStatus;
import com.example.decoratemycakebackend.domain.friend.repository.FriendRequestRepository;
import com.example.decoratemycakebackend.domain.member.entity.Member;
import com.example.decoratemycakebackend.domain.member.mapper.MemberMapper;
import com.example.decoratemycakebackend.domain.member.repository.MemberRepository;
import com.example.decoratemycakebackend.global.error.CustomException;
import com.example.decoratemycakebackend.global.error.ErrorCode;
import com.example.decoratemycakebackend.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    // 친구 요청 보내기
    public void sendFriendRequest(FriendRequestDto friendRequestDto) {

        // 두 계정의 유효성 확인
        Member sender = memberRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member receiver = memberRepository.findByEmail(friendRequestDto.getReceiverEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 기존에 수신자가 발신자에게 먼저 보낸 요청이 있는지 확인
        Optional<FriendRequest> existingReceiverRequest = friendRequestRepository.findBySenderAndReceiver(receiver, sender);
        if (existingReceiverRequest.isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_RECEIVED_REQUEST);
        }

        // 기존 요청 확인
        Optional<FriendRequest> existingRequest = friendRequestRepository.findBySenderAndReceiver(sender, receiver);

        if (existingRequest.isPresent()) {
            FriendRequest friendRequest = existingRequest.get();
            switch (friendRequest.getStatus()) {
                case REJECTED, DELETED:
                    // 거절 또는 삭제된 요청이었던 상태를 승인 대기중 상태로 변경
                    FriendRequest updatedRequest = friendRequest.updateToPending(friendRequestDto.getMessage());
                    friendRequestRepository.save(updatedRequest);
                    break;
                case PENDING:
                    // 이미 승인 대기중인 요청이 있는 경우
                    throw new CustomException(ErrorCode.DUPLICATE_FRIEND_REQUEST);
                case ACCEPTED:
                    // 이미 친구 관계인 경우
                    throw new CustomException(ErrorCode.ALREADY_FRIEND);
            }
        } else {
            // 신규 요청이라면 새로운 친구 요청 엔티티 생성
            FriendRequest friendRequest = FriendRequest.builder()
                    .receiver(receiver)
                    .sender(sender)
                    .status(FriendRequestStatus.PENDING)
                    .message(friendRequestDto.getMessage())
                    .build();

            friendRequestRepository.save(friendRequest);
        }
    }

    // 친구 요청 수락/거절 메서드
    public String confirmFriendRequest(FriendRequestAnswerDto friendRequestAnswerDto) {
        // 유효한 계정인지 확인
        Member sender = memberRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member receiver = memberRepository.findByEmail(friendRequestAnswerDto.getReceiverEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 두 계정간에 요청이 존재하는지 확인, 요청에 대한 답신이므로 두 매개변수를 반전시켰음.
        FriendRequest friendRequest = friendRequestRepository.findBySenderAndReceiver(receiver, sender)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (friendRequestAnswerDto.isAccepted()) {
            FriendRequest acceptedRequest = friendRequest.acceptRequest();
            friendRequestRepository.save(acceptedRequest);
            return "친구 요청이 수락되었습니다!";
        } else {
            FriendRequest rejectedRequest = friendRequest.rejectRequest();
            friendRequestRepository.save(rejectedRequest);

            return "친구 요청이 거부되었습니다.";
        }
    }

    // 친구 목록 열람
    public List<FriendListResponseDto> getFriendList() {
        // 로그인 된 유저의 이메일의 유효성 검사
        Member member = memberRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 해당 멤버에 매핑된 친구 요청 목록중 ACCEPTED 상태인 것들만 가져오기
        List<FriendRequest> acceptedFriendRequests = friendRequestRepository.findAcceptedFriendRequestsByMember(member);

        // 로그인 한 유저가 받은 친구 요청인지, 보낸 친구 요청인지 판단하여 해당 엔티티를 dto로 변환, 클라이언트로 반환.
        return acceptedFriendRequests.stream()
                .map(friendRequest -> {
                    Member friend = friendRequest.getReceiver().equals(member) ? friendRequest.getSender() : friendRequest.getReceiver();
                    return memberMapper.toFriendListResponseDto(friend);
                })
                .collect(Collectors.toList());
    }

    // 유저가 받은 친구 요청 리스트 열람. 친구 요청 상태가 PENDING으로 되어있는 것들만 추출하여 그 발신자 목록을 반환함.
    public List<FriendRequestListResponseDto> getFriendRequestList() {
        Member member = memberRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 해당 유저에게 매핑된 친구 요청 엔티티중 PENDING 상태인 것들을 골라서 리스트로 할당
        List<FriendRequest> friendRequests = friendRequestRepository.findByReceiverAndStatus(member, FriendRequestStatus.PENDING);

        // 각 요청을 순회하며 발신자의 정보를 dto로 변환하여 클라이언트로 반환
        return friendRequests.stream()
                .map(friendRequest -> memberMapper.toFriendRequestListResponseDto(friendRequest.getSender()))
                .collect(Collectors.toList());

    }

    // 친구 삭제
    @Transactional
    public void deleteFriend(String friendEmail) {
        Member currentMember = memberRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        log.info("Current member: {}", currentMember.getEmail());

        Member friendMember = memberRepository.findByEmail(friendEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        log.info("Friend member: {}", friendMember.getEmail());

        // 두 멤버가 이미 친구 상태인지 조회
        FriendRequest friendRequest = friendRequestRepository.findBySenderAndReceiverAndStatus(friendMember, currentMember, FriendRequestStatus.ACCEPTED)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FRIEND));

        // 친구 요청 상태를 DELETED로 변경. 실제로 제거하지는 않았음.
        FriendRequest deletedRequest = friendRequest.deleteRequest();
        friendRequestRepository.save(deletedRequest);
        log.info("Friendship deleted between {} and {}", currentMember.getEmail(), friendMember.getEmail());
    }

    // 친구 관계 양방향으로 확인하고 그래도 없으면 false 반환
    public boolean isFriend(Member currentMember, Member someone) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findBySenderAndReceiverAndStatus(someone, currentMember, FriendRequestStatus.ACCEPTED);

        if (!friendRequest.isPresent()) {
            friendRequest = friendRequestRepository.findBySenderAndReceiverAndStatus(currentMember, someone, FriendRequestStatus.ACCEPTED);
        }

        return friendRequest.isPresent();
    }
}
