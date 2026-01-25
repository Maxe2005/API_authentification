//package com.imt.API_authentification.service;
//
//import com.imt.API_authentification.persistence.dao.UserMongoDAO;
//import com.imt.API_authentification.persistence.dto.UserMongoDTO;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceTest {
//
//    @Mock
//    private UserMongoDAO userMongoDAO;
//
//    @InjectMocks
//    private UserService userService;
//
//    @Test
//    void getUser_shouldReturnUser_whenUserExists() {
//        String username = "testuser";
//        UserMongoDTO expectedUser = new UserMongoDTO(UUID.randomUUID(), username, "password");
//        when(userMongoDAO.findByUsername(username)).thenReturn(expectedUser);
//
//        UserMongoDTO actualUser = userService.getUser(username);
//
//        assertEquals(expectedUser, actualUser);
//    }
//
//    @Test
//    void getUser_shouldReturnNull_whenUserDoesNotExist() {
//        String username = "testuser";
//        when(userMongoDAO.findByUsername(username)).thenReturn(null);
//
//        UserMongoDTO actualUser = userService.getUser(username);
//
//        assertNull(actualUser);
//    }
//
//    @Test
//    void register_shouldSaveUserAndReturnTrue() {
//        String username = "testuser";
//        String password = "password";
//
//        boolean result = userService.register(username, password);
//
//        assertTrue(result);
//        verify(userMongoDAO).save(any(UserMongoDTO.class));
//    }
//}
