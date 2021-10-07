package com.cxy7.data.fense.server.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.mail.SimpleMailSender;
import com.cxy7.data.fense.server.dao.UserDao;
import com.cxy7.data.fense.server.model.User;
import com.cxy7.data.fense.utils.AesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 12:33 下午
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private UserDao userDao;
    @Value("${encrypt.key.user.password}")
    private String KEY;
    @Autowired
    private SimpleMailSender mailSender;
    @Value("${encrypt.key.iforgot.email}")
    private String IFOROT_KEY;
//    @Value("${server.host}")
//    private String HOST;
//    @Value("${server.port}")
//    private int PORT;

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    public User login(String username, String password) {
        // 对密码进行AES处理，然后跟数据库进行比对
//        password = DigestUtils.md5Hex(password);
        password = AesUtil.encrypt(password, KEY);
        User user = userDao.findByNameAndPassword(username, password);
        if (user == null) {
            // 利用邮箱登录
            user = userDao.findByEmailAndPassword(username, password);
        }
        return user;
    }

    /**
     * 注册用户
     * @param username
     * @param password
     * @param email
     * @return
     */
    public User register(String username, String password, String email){
        User user = new User();
        user.setName(username);
        // 对密码进行AES处理
//        password = DigestUtils.md5Hex(password);
        password = AesUtil.encrypt(password, KEY);
        user.setPassword(password);
        user.setEmail(email);
        Date now = new Date();
        user.setLastLoginTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsAdmin(0); // 非管理员
        userDao.save(user);

        return user;
    }

    public String getResetHref(User user) {
        // TODO 给用户邮箱发送密码重置邮件
        StringBuilder sb = new StringBuilder();
        sb.append(user.getName()).append("您好！<br/>");
        sb.append("请点击以下链接进行密码重置操作：");
        String token = AesUtil.encrypt(user.getEmail(), IFOROT_KEY);
        String pattern = "http://%s:%d/user/resetPassword?token=%s";
        // TODO: 重置密码url
//        String href = String.format(pattern, HOST, PORT, token);
//        sb.append(href);
        mailSender.send(user.getEmail(), null, "密码重置邮件", sb.toString(), null);
        return sb.toString();
    }

    public String decryptToken(String token) {
        return AesUtil.decrypt(token, IFOROT_KEY);
    }

    public void resetPassword(String email, String password) {
        Optional<User> op = findUserByEmail(email);
        if (!op.isPresent()) {
            logger.info("未找到该用户:{}", email);
            return;
        }
        User user = op.get();
        password = AesUtil.encrypt(password, KEY);

        user.setPassword(password);
        userDao.save(user);
    }

    public JSONObject list(PageRequest pageable) {
        long total = userDao.count();

        List<User> users = userDao.findAll();
        JSONArray arr = new JSONArray();
        for (User user : users) {
            JSONObject obj = new JSONObject();
            obj.put("id", user.getId());
            obj.put("name", user.getName());
            obj.put("pass", user.getPassword());
            obj.put("email", user.getEmail());
            obj.put("last_login_time", df.format(user.getLastLoginTime()));
            obj.put("create_time", df.format(user.getCreateTime()));
            obj.put("update_time", df.format(user.getUpdateTime()));
            obj.put("edit_url", "<a target='_blank' href='/user/edit?id=" + user.getId() + "'>編輯模式</a>");
            arr.add(obj);
        }
        JSONObject obj = new JSONObject();
        obj.put("rows", arr);
        obj.put("records", total);
        obj.put("page", pageable.getPageNumber() + 1);
        obj.put("total", totalPage((int)total, pageable.getPageSize()));
        return obj;
    }

    public int totalPage(int total, int pageSize) {
        int rel = total % pageSize;
        int totalPage = rel == 0 ? total / pageSize : total / pageSize + 1;
        return totalPage;
    }

    public boolean edit(int id, String name, String password, String email) {
        boolean succ = false;
        Optional<User> op = userDao.getOne(id);
        if (op.isPresent()) {
            User user = op.get();
            user.setName(name);
            password = AesUtil.encrypt(password, KEY);
            user.setPassword(password);
            user.setEmail(email);

            userDao.update(user);
            succ = true;
        }
        return succ;
    }

    public void delete(int id) {
        userDao.deleteById(id);
    }

    /**
     * 检查名字是否重复
     * @param name
     * @return
     */
    public boolean checkDuplicateName(String name) {
        Optional<User> op = userDao.getOneByName(name);
        return op.isPresent();
    }

    /**
     * 检查邮箱是否存在
     * @param email
     * @return
     */
    public Optional<User> findUserByEmail(String email) {
        return userDao.getOneByEmail(email);
    }

    public String listForGrant() {
        StringBuilder sb = new StringBuilder();
        sb.append("<select>");
        String pattern = "<option value='%d'>%s</option>";
        List<User> users = userDao.findAll();
        for (User user : users) {
            String option = String.format(pattern, user.getId(), user.getName());
            sb.append(option);
        }
        sb.append("</select>");
        return sb.toString();
    }

    public User findByNameAndPassword(String name, String password) {
        password = AesUtil.encrypt(password, KEY);
        return userDao.findByNameAndPassword(name, password);
    }
}
