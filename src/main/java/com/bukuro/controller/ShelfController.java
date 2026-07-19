package com.bukuro.controller;

import com.bukuro.dto.IsbnRequest;
import com.bukuro.dto.ShelfEntryDto;
import com.bukuro.dto.ShelfStatusUpdateRequest;
import com.bukuro.entity.Post;
import com.bukuro.entity.ReadingRecord;
import com.bukuro.entity.ReadingRecord.ReadingStatus;
import com.bukuro.service.PostService;
import com.bukuro.service.ShelfService;
import com.bukuro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shelf")
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfService shelfService;
    private final UserService userService;
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ShelfEntryDto> add(@Valid @RequestBody IsbnRequest request,
                                             @AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        ReadingRecord record = shelfService.addToShelf(userId, request.getIsbn());
        return ResponseEntity.status(HttpStatus.CREATED).body(ShelfEntryDto.from(record, null));
    }

    @GetMapping
    public Map<String, List<ShelfEntryDto>> shelf(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        List<ReadingRecord> records = shelfService.getShelf(userId);

        Map<Long, Long> postIdByBookId = postService.findByUserId(userId).stream()
                .collect(Collectors.toMap(
                        p -> p.getBook().getId(),
                        Post::getId,
                        (existing, newer) -> existing
                ));

        return Map.of(
                "wantToRead", toDtoList(records, ReadingStatus.WANT_TO_READ, postIdByBookId),
                "reading", toDtoList(records, ReadingStatus.READING, postIdByBookId),
                "done", toDtoList(records, ReadingStatus.DONE, postIdByBookId)
        );
    }

    @PatchMapping("/{recordId}")
    public ShelfEntryDto updateStatus(@PathVariable Long recordId,
                                      @Valid @RequestBody ShelfStatusUpdateRequest request,
                                      @AuthenticationPrincipal UserDetails principal) {
        ReadingStatus readingStatus = ReadingStatus.valueOf(request.getStatus());
        Long userId = getUserId(principal);
        ReadingRecord record = shelfService.updateStatus(recordId, readingStatus, userId);
        return ShelfEntryDto.from(record, null);
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> delete(@PathVariable Long recordId,
                                       @AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        shelfService.remove(recordId, userId);
        return ResponseEntity.noContent().build();
    }

    private List<ShelfEntryDto> toDtoList(List<ReadingRecord> records, ReadingStatus status,
                                          Map<Long, Long> postIdByBookId) {
        return records.stream()
                .filter(r -> r.getStatus() == status)
                .map(r -> ShelfEntryDto.from(r, postIdByBookId.get(r.getBook().getId())))
                .collect(Collectors.toList());
    }

    private Long getUserId(UserDetails principal) {
        return userService.getUserByEmail(principal.getUsername()).getId();
    }
}
