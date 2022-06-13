package com.mt.controller;

import com.mt.entity.User;
import com.mt.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;


@Api(description = "用户管理", value = "用户管理")
@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

    /**
     * 注册用户 默认开启白名单
     *
     * @param user
     */
    @PostMapping("/signup")
    public Result signup(@RequestBody User user) {
        User bizUser = null;
        if (Objects.isNull(user.getUsername()) || user.getUsername().isEmpty()) {
            return Result.error(1100, "用户名不可为空");
        } else {
            bizUser = userRepository.findByUsername(user.getUsername());
            if (!Objects.isNull(bizUser)) {
                return Result.error(1000, "用户名已经存在");
            }
        }
        if (!Objects.isNull(user.getPhone()) && !user.getPhone().isEmpty()) {
            bizUser = userRepository.findByPhone(user.getPhone());
            if (!Objects.isNull(bizUser)) {
                return Result.error(1002, "手机号已经存在");
            }
            String phoneRegex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
            if (!user.getPhone().matches(phoneRegex)) {
                return Result.error(1012, "手机号格式不对");
            }
        }

        if (!Objects.isNull(user.getEmail()) && !user.getEmail().isEmpty()) {
            String emailRegex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            if (!user.getEmail().matches(emailRegex)) {
                return Result.error(1011, "邮箱格式不对");
            }
            bizUser = userRepository.findByEmail(user.getEmail());
            if (!Objects.isNull(bizUser)) {
                return Result.error(1001, "邮箱已经存在");
            }
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return Result.ok(userRepository.save(user));
    }


    /**
     * 获取用户列表
     *
     * @return
     */
    @ApiModelProperty(value = "获取用户列表")
    @GetMapping("/userList")
    public Map<String, Object> userList() {
        List<User> users = userRepository.findAll();
        logger.info("users: {}", users);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("users", users);
        return map;
    }

    /**
     * 获取用户权限
     *
     * @return
     */
    @ApiModelProperty(value = "获取用户权限")
    @GetMapping("/authorityList")
    public List<String> authorityList() {
        List<String> authentication = getAuthentication();
        return authentication;
    }


    public Boolean verify(User user) {

        return false;
    }

}
