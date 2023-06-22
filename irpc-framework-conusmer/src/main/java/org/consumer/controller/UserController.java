package org.consumer.controller;

import common.IRpcReference;
import interfaces.user.UserRpcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @IRpcReference
    private UserRpcService userService;


    @GetMapping(value = "/get-userid")
    public String test() {
        return userService.getUserId();
    }

    @GetMapping(value = "/find-my-goods")
    public List<Map<String, String>> test(String userId) {
        List<Map<String, String>> myGoods = userService.findMyGoods(userId);
        return myGoods;
    }


}
