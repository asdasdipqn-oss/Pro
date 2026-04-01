package cn.edu.ccst.manpower_resource;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密测试类
 * 运行此类的main方法可以获取加密后的密码
 */
public class PasswordEncoderTest {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("===========================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("===========================================");
        
        // 验证密码是否匹配
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + matches);
        
        // 为了方便，可以直接复制这个加密密码到SQL中
        System.out.println("\n请复制以下加密密码用于SQL插入:");
        System.out.println(encodedPassword);
    }
}
