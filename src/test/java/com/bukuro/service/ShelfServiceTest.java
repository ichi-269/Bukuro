package com.bukuro.service;

import com.bukuro.dto.BookDto;
import com.bukuro.entity.Book;
import com.bukuro.entity.ReadingRecord;
import com.bukuro.entity.ReadingRecord.ReadingStatus;
import com.bukuro.entity.User;
import com.bukuro.exception.DuplicateRecordException;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.BookRepository;
import com.bukuro.repository.ReadingRecordRepository;
import com.bukuro.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShelfServiceTest {

    @Mock
    private ReadingRecordRepository readingRecordRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookSearchService bookSearchService;

    @InjectMocks
    private ShelfService shelfService;

    @Test
    @DisplayName("新規書籍をDBに保存して本棚に追加する")
    void addToShelf_newBook_savesBookAndRecord() {
        BookDto dto = BookDto.builder()
                .isbn("9784774192178").title("テスト駆動開発")
                .author("Kent Beck").publisher("オーム社")
                .coverUrl("https://cover.openbd.jp/9784774192178.jpg")
                .build();
        Book savedBook = Book.builder().id(1L).isbn("9784774192178")
                .title("テスト駆動開発").author("Kent Beck").publisher("オーム社").build();
        User user = User.builder().id(10L).email("test@example.com").build();

        when(bookRepository.findByIsbn("9784774192178")).thenReturn(Optional.empty());
        when(bookSearchService.searchByIsbn("9784774192178")).thenReturn(dto);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(userRepository.getReferenceById(10L)).thenReturn(user);
        when(readingRecordRepository.existsByUserIdAndBookId(10L, 1L)).thenReturn(false);
        when(readingRecordRepository.save(any(ReadingRecord.class))).thenAnswer(i -> i.getArgument(0));

        ReadingRecord result = shelfService.addToShelf(10L, "9784774192178");

        assertThat(result.getStatus()).isEqualTo(ReadingStatus.WANT_TO_READ);
        verify(bookRepository).save(any(Book.class));
        verify(readingRecordRepository).save(any(ReadingRecord.class));
    }

    @Test
    @DisplayName("既存書籍はDB保存せずに本棚に追加する")
    void addToShelf_existingBook_skipsSave() {
        Book existingBook = Book.builder().id(2L).isbn("9784774192178").title("既存書籍").author("著者").build();
        User user = User.builder().id(10L).build();

        when(bookRepository.findByIsbn("9784774192178")).thenReturn(Optional.of(existingBook));
        when(readingRecordRepository.existsByUserIdAndBookId(10L, 2L)).thenReturn(false);
        when(userRepository.getReferenceById(10L)).thenReturn(user);
        when(readingRecordRepository.save(any(ReadingRecord.class))).thenAnswer(i -> i.getArgument(0));

        shelfService.addToShelf(10L, "9784774192178");

        verify(bookRepository, never()).save(any());
        verify(bookSearchService, never()).searchByIsbn(any());
    }

    @Test
    @DisplayName("同じユーザーが同じ本を重複登録しようとすると DuplicateRecordException が発生する")
    void addToShelf_duplicate_throwsDuplicateRecordException() {
        Book book = Book.builder().id(1L).isbn("9784774192178").title("本").author("著者").build();
        when(bookRepository.findByIsbn("9784774192178")).thenReturn(Optional.of(book));
        when(readingRecordRepository.existsByUserIdAndBookId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> shelfService.addToShelf(10L, "9784774192178"))
                .isInstanceOf(DuplicateRecordException.class);
    }

    @Test
    @DisplayName("ステータスを DONE 以外に変更すると rating が null にリセットされる")
    void updateStatus_toReading_resetsRating() {
        User user = User.builder().id(10L).build();
        Book book = Book.builder().id(1L).build();
        ReadingRecord record = ReadingRecord.builder()
                .id(1L).user(user).book(book)
                .status(ReadingStatus.DONE).rating(5)
                .build();

        when(readingRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(readingRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReadingRecord result = shelfService.updateStatus(1L, ReadingStatus.READING, 10L);

        assertThat(result.getStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(result.getRating()).isNull();
    }

    @Test
    @DisplayName("ステータスを DONE に変更しても rating は保持される")
    void updateStatus_toDone_keepsRating() {
        User user = User.builder().id(10L).build();
        Book book = Book.builder().id(1L).build();
        ReadingRecord record = ReadingRecord.builder()
                .id(1L).user(user).book(book)
                .status(ReadingStatus.READING).rating(null)
                .build();

        when(readingRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(readingRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReadingRecord result = shelfService.updateStatus(1L, ReadingStatus.DONE, 10L);

        assertThat(result.getStatus()).isEqualTo(ReadingStatus.DONE);
    }

    @Test
    @DisplayName("存在しない recordId で updateStatus を呼ぶと ResourceNotFoundException が発生する")
    void updateStatus_notFound_throwsResourceNotFoundException() {
        when(readingRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shelfService.updateStatus(99L, ReadingStatus.READING, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("他ユーザーのレコードを操作しようとすると AccessDeniedException が発生する")
    void updateStatus_otherUsersRecord_throwsAccessDeniedException() {
        User owner = User.builder().id(20L).build();
        ReadingRecord record = ReadingRecord.builder()
                .id(1L).user(owner)
                .status(ReadingStatus.WANT_TO_READ)
                .build();

        when(readingRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> shelfService.updateStatus(1L, ReadingStatus.READING, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("本棚からレコードを削除できる")
    void remove_validRecord_deletesRecord() {
        User user = User.builder().id(10L).build();
        ReadingRecord record = ReadingRecord.builder().id(1L).user(user)
                .status(ReadingStatus.WANT_TO_READ).build();

        when(readingRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        shelfService.remove(1L, 10L);

        verify(readingRecordRepository).delete(record);
    }

    @Test
    @DisplayName("存在しない recordId で remove を呼ぶと ResourceNotFoundException が発生する")
    void remove_notFound_throwsResourceNotFoundException() {
        when(readingRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shelfService.remove(99L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("他ユーザーのレコードを削除しようとすると AccessDeniedException が発生する")
    void remove_otherUsersRecord_throwsAccessDeniedException() {
        User owner = User.builder().id(20L).build();
        ReadingRecord record = ReadingRecord.builder().id(1L).user(owner)
                .status(ReadingStatus.WANT_TO_READ).build();

        when(readingRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> shelfService.remove(1L, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("addToShelf で BookSearchService が BookNotFoundException をスローすると伝播する")
    void addToShelf_openBdNotFound_propagatesBookNotFoundException() {
        when(bookRepository.findByIsbn("9999999999999")).thenReturn(Optional.empty());
        when(bookSearchService.searchByIsbn("9999999999999"))
                .thenThrow(new com.bukuro.exception.BookNotFoundException("9999999999999"));

        assertThatThrownBy(() -> shelfService.addToShelf(10L, "9999999999999"))
                .isInstanceOf(com.bukuro.exception.BookNotFoundException.class);
    }

    @Test
    @DisplayName("getShelf はユーザーの全読書記録を返す")
    void getShelf_returnsAllRecordsForUser() {
        User user = User.builder().id(10L).build();
        Book book1 = Book.builder().id(1L).title("本1").author("著者").build();
        Book book2 = Book.builder().id(2L).title("本2").author("著者").build();
        List<ReadingRecord> records = List.of(
                ReadingRecord.builder().id(1L).user(user).book(book1).status(ReadingStatus.WANT_TO_READ).build(),
                ReadingRecord.builder().id(2L).user(user).book(book2).status(ReadingStatus.DONE).build()
        );
        when(readingRecordRepository.findByUserIdOrderByIdDesc(10L)).thenReturn(records);

        List<ReadingRecord> result = shelfService.getShelf(10L);

        assertThat(result).hasSize(2);
    }
}
