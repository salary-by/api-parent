package by.salary.serviceuser.controller;

import by.salary.serviceuser.model.UserAuthenticationRequestDTO;
import by.salary.serviceuser.model.UserAuthenticationResponseDTO;
import by.salary.serviceuser.service.UserAuthenticationService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Controller
@NoArgsConstructor
@RequestMapping("/account")
public class UserAuthenticationController {

    private UserAuthenticationService userAuthenticationService;

    @Autowired
    public UserAuthenticationController(UserAuthenticationService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }


    @RequestMapping("/account")
    public UserAuthenticationResponseDTO auth(@RequestBody UserAuthenticationRequestDTO userAuthenticationRequestDTO) {
        return userAuthenticationService.auth(userAuthenticationRequestDTO);
    }
}
