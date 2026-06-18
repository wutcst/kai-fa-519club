package cn.edu.whut.sept.zuul.infrastructure.server.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cn.edu.whut.sept.zuul.infrastructure.auth.UserAccount;
import cn.edu.whut.sept.zuul.infrastructure.persistence.DatabaseProvider;
import cn.edu.whut.sept.zuul.infrastructure.persistence.FriendRepository;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.FriendRequestDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.FriendViewDto;
import cn.edu.whut.sept.zuul.infrastructure.social.UserPresenceRegistry;
import cn.edu.whut.sept.zuul.infrastructure.social.UserPresenceStatus;

/**
 * 好友关系与状态查询。
 */
@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserPresenceRegistry presenceRegistry;

    public FriendService(UserPresenceRegistry presenceRegistry) {
        this.friendRepository = new FriendRepository(DatabaseProvider.getDefault());
        this.presenceRegistry = presenceRegistry;
    }

    public List<FriendViewDto> listFriends(long userId) {
        return friendRepository.listFriends(userId).stream()
            .map(account -> toFriendView(account))
            .collect(Collectors.toList());
    }

    public List<FriendRequestDto> listIncomingFriendRequests(long userId) {
        return friendRepository.listIncomingFriendRequests(userId).stream()
            .map(this::toFriendRequest)
            .collect(Collectors.toList());
    }

    public Object sendFriendRequest(long userId, String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("请填写好友用户名");
        }
        UserAccount target = friendRepository.findByUsername(username.trim())
            .orElseThrow(() -> new IllegalArgumentException("未找到该用户"));
        if (target.getId() == userId) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }
        if (friendRepository.areFriends(userId, target.getId())) {
            throw new IllegalArgumentException("你们已经是好友了");
        }
        if (friendRepository.hasPendingRequest(userId, target.getId())) {
            throw new IllegalArgumentException("好友申请已发送，请等待对方处理");
        }
        if (friendRepository.hasPendingRequest(target.getId(), userId)) {
            friendRepository.addFriendship(userId, target.getId());
            friendRepository.deleteAllFriendRequestsBetween(userId, target.getId());
            return toFriendView(target);
        }
        friendRepository.createFriendRequest(userId, target.getId());
        return toFriendRequest(target);
    }

    public FriendViewDto acceptFriendRequest(long userId, long fromUserId) {
        UserAccount sender = friendRepository.findIncomingRequestSender(userId, fromUserId)
            .orElseThrow(() -> new IllegalArgumentException("好友申请不存在或已处理"));
        friendRepository.addFriendship(userId, sender.getId());
        friendRepository.deleteAllFriendRequestsBetween(userId, sender.getId());
        return toFriendView(sender);
    }

    public void rejectFriendRequest(long userId, long fromUserId) {
        if (friendRepository.findIncomingRequestSender(userId, fromUserId).isEmpty()) {
            throw new IllegalArgumentException("好友申请不存在或已处理");
        }
        friendRepository.deleteFriendRequest(fromUserId, userId);
    }

    public void removeFriend(long userId, long friendUserId) {
        if (!friendRepository.areFriends(userId, friendUserId)) {
            throw new IllegalArgumentException("好友不存在");
        }
        friendRepository.removeFriendship(userId, friendUserId);
    }

    private FriendViewDto toFriendView(UserAccount account) {
        UserPresenceStatus status = presenceRegistry.resolveStatus(account.getId());
        FriendViewDto dto = new FriendViewDto();
        dto.setUserId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setDisplayName(account.getDisplayName());
        dto.setAvatarUrl(account.getAvatarUrl());
        dto.setStatus(status.name());
        dto.setStatusLabel(status.getLabel());
        dto.setRoomId(presenceRegistry.resolveRoomId(account.getId()));
        return dto;
    }

    private FriendRequestDto toFriendRequest(UserAccount account) {
        FriendRequestDto dto = new FriendRequestDto();
        dto.setUserId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setDisplayName(account.getDisplayName());
        dto.setAvatarUrl(account.getAvatarUrl());
        dto.setCreatedAtMs(System.currentTimeMillis());
        return dto;
    }
}
