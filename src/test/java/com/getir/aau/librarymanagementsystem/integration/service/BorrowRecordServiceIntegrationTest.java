package com.getir.aau.librarymanagementsystem.integration.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowItemRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowRecordRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowRecordResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.*;
import com.getir.aau.librarymanagementsystem.repository.*;
import com.getir.aau.librarymanagementsystem.service.BorrowRecordService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BorrowRecordServiceIntegrationTest {

    @Autowired
    private BorrowRecordService service;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BorrowRecordRepository recordRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private User regularUser;
    private User librarian;
    private Book book1;
    private Book book2;
    private Category category;
    private Author author;
    private static final int MAX_BORROW_LIMIT = 5;

    @BeforeEach
    void setUp() {
        // Clear the security context before each test
        SecurityContextHolder.clearContext();
        author = authorRepository.save(
                Author.builder().name("John Doe").description("Well-known fiction writer").build()
        );

        category = categoryRepository.save(
                Category.builder().name("Fiction").build()
        );
        // Create roles
        Role userRole = Role.builder()
                .id(1L)
                .name(ERole.ROLE_USER)
                .description("ROLE_USER")
                .build();

        Role librarianRole = Role.builder()
                .id(2L)
                .name(ERole.ROLE_LIBRARIAN)
                .description("ROLE_LIBRARIAN")
                .build();

        regularUser = userRepository.save(
                User.builder()
                        .email("user@domain.com")
                        .firstName("Test")
                        .lastName("User")
                        .password("pw")
                        .phoneNumber("1234567890")
                        .role(userRole)
                        .build()
        );
        librarian = userRepository.save(
                User.builder()
                        .email("lib@domain.com")
                        .firstName("Lib")
                        .lastName("Admin")
                        .password("pw")
                        .phoneNumber("1234567890")
                        .role(librarianRole)
                        .build()
        );

        book1 = bookRepository.save(
                Book.builder()
                        .title("Book One")
                        .author(author)
                        .isbn("111-1234567890")
                        .category(category)
                        .description("A thrilling adventure book")
                        .publicationDate(LocalDate.of(2022, 1, 1))
                        .genre("Adventure")
                        .numberOfCopies(2)
                        .available(true)
                        .build()
        );

        book2 = bookRepository.save(
                Book.builder()
                        .title("Book Two")
                        .author(author)
                        .isbn("222-0987654321")
                        .category(category)
                        .description("A romantic mystery novel")
                        .publicationDate(LocalDate.of(2023, 3, 15))
                        .genre("Mystery")
                        .numberOfCopies(1)
                        .available(true)
                        .build()
        );
    }

    // Helper method to set up authentication
    private void setUpAuthentication(User user, String role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), user.getPassword(), authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("BorrowBooks Scenarios")
    class BorrowBooksTests {

        @Test
        @DisplayName("Successful borrow reduces copies and creates items")
        void successBorrow() {
            // Set up authentication for the regular user
            setUpAuthentication(regularUser, "ROLE_USER");

            BorrowRecordRequestDto dto = new BorrowRecordRequestDto(
                    regularUser.getId(), List.of(new BorrowItemRequestDto(book1.getId()), new BorrowItemRequestDto(book2.getId())));

            BorrowRecordResponseDto resp = service.borrowBooks(dto);

            assertThat(resp).isNotNull();
            assertThat(resp.items()).hasSize(2);

            Book updatedBook1 = bookRepository.findById(book1.getId()).orElseThrow();
            Book updatedBook2 = bookRepository.findById(book2.getId()).orElseThrow();

            assertThat(updatedBook1.getNumberOfCopies()).isEqualTo(1);
            assertThat(updatedBook2.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("Unavailable book throws ResourceAlreadyExistsException")
        void unavailableBook() {
            // Set up authentication for the regular user
            setUpAuthentication(regularUser, "ROLE_USER");

            Book unavailableBook = bookRepository.save(
                    Book.builder()
                            .title("No Copies")
                            .isbn("333")
                            .numberOfCopies(0)
                            .available(false)
                            .author(author)
                            .category(category)
                            .description("Out of stock book for test")
                            .genre("Drama")
                            .publicationDate(LocalDate.of(2020, 5, 10))
                            .build()
            );

            BorrowRecordRequestDto dto = new BorrowRecordRequestDto(
                    regularUser.getId(),
                    List.of(new BorrowItemRequestDto(unavailableBook.getId()))
            );

            assertThrows(ResourceAlreadyExistsException.class, () -> service.borrowBooks(dto));
        }

        @Test
        @DisplayName("Unknown user throws ResourceNotFoundException")
        void unknownUser() {
            // Set up authentication for the regular user
            setUpAuthentication(regularUser, "ROLE_USER");

            BorrowRecordRequestDto dto = new BorrowRecordRequestDto(
                    9999L,
                    List.of(new BorrowItemRequestDto(book1.getId()))
            );

            assertThrows(ResourceNotFoundException.class, () -> service.borrowBooks(dto));
        }

        @Test
        @DisplayName("Exceeding limit throws IllegalStateException")
        void exceedLimit() {
            // Set up authentication for the regular user
            setUpAuthentication(regularUser, "ROLE_USER");

            // Create active borrow items to reach the max limit
            IntStream.range(0, MAX_BORROW_LIMIT).forEach(i -> {
                Book tempBook = bookRepository.save(
                        Book.builder()
                                .title("Temp Book " + i)
                                .isbn("TEMP-" + i)
                                .numberOfCopies(1)
                                .author(author)
                                .category(category)
                                .description("Temporary book for testing")
                                .genre("Test")
                                .publicationDate(LocalDate.of(2020, 5, 10))
                                .available(true)
                                .build()
                );

                BorrowRecord br = BorrowRecord.builder()
                        .user(regularUser)
                        .borrowDate(LocalDate.now())
                        .dueDate(LocalDate.now().plusDays(14))
                        .build();

                BorrowItem item = BorrowItem.builder()
                        .user(regularUser)
                        .book(tempBook)
                        .borrowDate(LocalDate.now())
                        .dueDate(LocalDate.now().plusDays(14))
                        .returned(false)
                        .build();

                br.addItem(item);
                recordRepository.save(br);
                tempBook.borrow(); // Simulate book being borrowed
                bookRepository.save(tempBook);
            });

            BorrowRecordRequestDto dto = new BorrowRecordRequestDto(
                    regularUser.getId(),
                    List.of(new BorrowItemRequestDto(book1.getId()))
            );

            assertThrows(IllegalStateException.class, () -> service.borrowBooks(dto));
        }
    }

    @Nested
    @DisplayName("Access Control Scenarios")
    class AccessControlTests {

        @Test
        @DisplayName("Librarian can view another user's records")
        void librarianView() {
            // Set up authentication for the librarian
            setUpAuthentication(librarian, "ROLE_LIBRARIAN");

            Page<BorrowRecordResponseDto> page = service.getByUser(regularUser.getId(), PageRequest.of(0, 5));
            assertThat(page).isNotNull();
        }

        @Test
        @DisplayName("Regular user forbidden from viewing others records")
        void userForbidden() {
            // Set up authentication for a different regular user
            User anotherUser = userRepository.save(
                    User.builder()
                            .email("another@domain.com")
                            .firstName("Another")
                            .lastName("User")
                            .password("pw")
                            .role(regularUser.getRole())
                            .phoneNumber("0987654321")
                            .build()
            );
            setUpAuthentication(anotherUser, "ROLE_USER");

            assertThrows(AccessDeniedException.class, () ->
                    service.getByUser(regularUser.getId(), PageRequest.of(0, 5))
            );
        }

        @Test
        @DisplayName("Unknown user in getByUser throws ResourceNotFoundException")
        void unknownUserGetBy() {
            // Set up authentication for the librarian
            setUpAuthentication(librarian, "ROLE_LIBRARIAN");

            assertThrows(ResourceNotFoundException.class, () ->
                    service.getByUser(999L, PageRequest.of(0, 1))
            );
        }
    }

    @Nested
    @DisplayName("Querying Scenarios")
    class QueryTests {

        @Test
        @DisplayName("Filter returns only matching records")
        void filterByEmailAndDate() {
            // Set up authentication for the librarian
            setUpAuthentication(librarian, "ROLE_LIBRARIAN");

            // Create a borrow record for the regular user
            BorrowRecord r = BorrowRecord.builder()
                    .user(regularUser)
                    .borrowDate(LocalDate.now().minusDays(3))
                    .dueDate(LocalDate.now())
                    .build();

            r.addItem(BorrowItem.builder()
                    .user(regularUser)
                    .book(book1)
                    .borrowDate(r.getBorrowDate())
                    .dueDate(r.getDueDate())
                    .build());

            recordRepository.save(r);

            Page<BorrowRecordResponseDto> res = service.filter(
                    regularUser.getEmail(),
                    LocalDate.now().minusDays(7),
                    LocalDate.now(),
                    PageRequest.of(0, 5)
            );

            assertThat(res.getContent()).allMatch(rr -> rr.userId().equals(regularUser.getId()));
        }

        @Test
        @DisplayName("getAll returns sorted records")
        void getAllSorted() {
            // Set up authentication for the librarian
            setUpAuthentication(librarian, "ROLE_LIBRARIAN");

            recordRepository.save(BorrowRecord.builder()
                    .user(regularUser)
                    .borrowDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(14))
                    .build());

            Page<BorrowRecordResponseDto> all = service.getAll(PageRequest.of(0, 10));
            assertThat(all.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("getById for existing record as librarian")
        void getByIdExists() {
            // Set up authentication for the librarian
            setUpAuthentication(librarian, "ROLE_LIBRARIAN");

            BorrowRecord r = recordRepository.save(BorrowRecord.builder()
                    .user(regularUser)
                    .borrowDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(14))
                    .build());

            BorrowRecordResponseDto dto = service.getById(r.getId());
            assertThat(dto.id()).isEqualTo(r.getId());
        }

        @Test
        @DisplayName("getById throws when not found")
        void getByIdNotFound() {
            // Set up authentication for the librarian
            setUpAuthentication(librarian, "ROLE_LIBRARIAN");

            assertThrows(ResourceNotFoundException.class, () -> service.getById(9999L));
        }

        @Test
        @DisplayName("Active records only show unreturned items")
        void activeRecords() {
            // Set up authentication for the librarian
            setUpAuthentication(librarian, "ROLE_LIBRARIAN");

            BorrowRecord r = recordRepository.save(BorrowRecord.builder()
                    .user(regularUser)
                    .borrowDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(14))
                    .build());

            r.addItem(BorrowItem.builder()
                    .user(regularUser)
                    .book(book1)
                    .borrowDate(r.getBorrowDate())
                    .dueDate(r.getDueDate())
                    .returned(false)
                    .build());

            recordRepository.save(r);

            List<BorrowRecordResponseDto> act = service.getActiveRecordsByUser(regularUser.getId());
            assertThat(act).allMatch(rr -> rr.items().stream().anyMatch(i -> !i.returned()));
        }

        @Test
        @DisplayName("isBookAvailableForBorrowing reflects dynamic stock")
        void bookAvailability() {
            Book b = bookRepository.save(Book.builder()
                    .title("Test")
                    .isbn("555")
                    .numberOfCopies(0)
                    .available(false)
                    .author(author)
                    .category(category)
                    .description("Test book")
                    .genre("Test")
                    .publicationDate(LocalDate.of(2020, 5, 10))
                    .build());

            assertThat(service.isBookAvailableForBorrowing(b.getId())).isFalse();
        }
    }
}